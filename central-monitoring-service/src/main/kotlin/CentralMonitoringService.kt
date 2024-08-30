package com.davithayrapetyan.weathermonitoring

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

class CentralMonitoringService(config: Config) {
    private val objectMapper = jacksonObjectMapper()

    private val kafkaBootstrapServers = config.getString("kafka.bootstrap-servers")
    private val groupIdSensors = config.getString("kafka.group-id-sensors")
    private val groupIdWarnings = config.getString("kafka.group-id-warnings")
    private val autoOffsetReset = config.getString("kafka.auto-offset-reset")
    private val sensorTopics = config.getStringList("sensor-topics")
    private val warningTopics = config.getStringList("warning-topics")

    private val thresholds = config.getConfig("thresholds")

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

    fun monitor() {
        monitorSensors()
        monitorWarnings()
    }

    private fun monitorSensors() {
        val sensorFlux: Flux<ReceiverRecord<String, String>> = KafkaReceiver.create(sensorReceiverOptions).receive()

        sensorFlux.map { record ->
            objectMapper.readValue<SensorData>(record.value())
        }.subscribe { sensorData ->
            checkThreshold(sensorData)
        }
    }

    private fun monitorWarnings() {
        val warningFlux: Flux<ReceiverRecord<String, String>> = KafkaReceiver.create(warningReceiverOptions).receive()

        warningFlux.map { record ->
            objectMapper.readValue<WarningMessage>(record.value())
        }.subscribe { warningMessage ->
            processWarning(warningMessage)
        }
    }

    private fun checkThreshold(sensorData: SensorData) {
        val thresholdEntry = thresholds.root().entries.find {
            val config = thresholds.getConfig(it.key)
            val prefixPattern = config.getString("prefix").toRegex()
            prefixPattern.containsMatchIn(sensorData.sensorId)
        }

        if (thresholdEntry != null) {
            val config = thresholds.getConfig(thresholdEntry.key)
            val thresholdValue = config.getDouble("value")
            if (sensorData.value > thresholdValue) {
                println("ALARM: ${sensorData.warehouseId} - ${sensorData.sensorId} exceeded threshold with value ${sensorData.value}")
            }
        } else {
            println("No threshold configured for sensor ID: ${sensorData.sensorId}")
        }
    }

    private fun processWarning(warningMessage: WarningMessage) {
        println("WARNING: ${warningMessage.warehouseId} - Port ${warningMessage.port} reported: ${warningMessage.message}")
    }
}

fun main(args: Array<String>) {
    val config = ConfigFactory.load()

    val monitoringService = CentralMonitoringService(config)
    monitoringService.monitor()
}
