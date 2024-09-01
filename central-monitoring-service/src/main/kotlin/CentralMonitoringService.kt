package com.davithayrapetyan.weathermonitoring

import com.davithayrapetyan.weathermonitoring.threshold.ThresholdCheckerFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import reactor.core.publisher.Flux
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.receiver.ReceiverRecord
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

class CentralMonitoringService(config: Config) {
    private val objectMapper = jacksonObjectMapper()

    private val kafkaBootstrapServers = config.getString("kafka.bootstrap-servers")
    private val groupIdSensors = config.getString("kafka.group-id-sensors")
    private val groupIdWarnings = config.getString("kafka.group-id-warnings")
    private val autoOffsetReset = config.getString("kafka.auto-offset-reset")
    private val sensorTopics = config.getStringList("sensor-topics")
    private val warningTopics = config.getStringList("warning-topics")
    private val monitoringInterval = config.getDuration("monitoring.interval")

    private val thresholds = config.getConfig("thresholds")
    private val thresholdCheckerFactory = ThresholdCheckerFactory(thresholds)

    private val sensorReceiverOptions = ReceiverOptions.create<String, String>(mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBootstrapServers,
        ConsumerConfig.GROUP_ID_CONFIG to groupIdSensors,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to autoOffsetReset
    ))
        .withKeyDeserializer(StringDeserializer())
        .withValueDeserializer(StringDeserializer())
        .subscription(sensorTopics)

    private val warningReceiverOptions = ReceiverOptions.create<String, String>(mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBootstrapServers,
        ConsumerConfig.GROUP_ID_CONFIG to groupIdWarnings,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to autoOffsetReset
    ))
        .withKeyDeserializer(StringDeserializer())
        .withValueDeserializer(StringDeserializer())
        .subscription(warningTopics)

    private val lastMessageTimes = ConcurrentHashMap<String, Long>()

    fun monitor() {
        monitorSensors()
        monitorWarnings()
        startWarehouseTimeoutMonitor()
    }

    private fun monitorSensors() {
        val sensorFlux: Flux<ReceiverRecord<String, String>> = KafkaReceiver.create(sensorReceiverOptions).receive()

        sensorFlux.map { record ->
            objectMapper.readValue<SensorData>(record.value())
        }.subscribe { sensorData ->
            lastMessageTimes[sensorData.warehouseId] = System.currentTimeMillis()
            val checker = thresholdCheckerFactory.create(sensorData.sensorId)
            if (checker?.check(sensorData) == true) {
                println("ALARM: ${sensorData.warehouseId} - ${sensorData.sensorId} exceeded threshold with value ${sensorData.value}")
            } else {
                println("ERROR: No threshold checker found or threshold not exceeded for sensor ID: ${sensorData.sensorId}")
            }
        }
    }

    private fun monitorWarnings() {
        val warningFlux: Flux<ReceiverRecord<String, String>> = KafkaReceiver.create(warningReceiverOptions).receive()

        warningFlux.map { record ->
            objectMapper.readValue<WarningMessage>(record.value())
        }.subscribe { warningMessage ->
            lastMessageTimes[warningMessage.warehouseId] = System.currentTimeMillis()
            processWarning(warningMessage)
        }
    }

    private fun startWarehouseTimeoutMonitor() {
        Flux.interval(monitoringInterval)
            .flatMap {
                Mono.fromCallable {
                    val currentTime = System.currentTimeMillis()
                    lastMessageTimes.entries.filter { entry ->
                        currentTime - entry.value > monitoringInterval.toMillis()
                    }
                }
            }
            .doOnNext { silentWarehouses ->
                silentWarehouses.forEach { warehouseEntry ->
                    println("WARNING: No data received from warehouse ${warehouseEntry.key} for over ${monitoringInterval.toMinutes()} minutes")
                }
            }
            .subscribe()
    }

    private fun processWarning(warningMessage: WarningMessage) {
        println("WARNING: ${warningMessage.warehouseId} - Port ${warningMessage.port} reported: ${warningMessage.message}")
    }
}

fun main() {
    val config = ConfigFactory.load()

    val monitoringService = CentralMonitoringService(config)
    monitoringService.monitor()
}
