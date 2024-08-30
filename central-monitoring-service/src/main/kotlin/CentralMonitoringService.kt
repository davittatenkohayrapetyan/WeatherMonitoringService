package com.davithayrapetyan.weathermonitoring

import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import akka.kafka.Subscriptions
import akka.kafka.javadsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.StringDeserializer
import akka.stream.javadsl.Sink
import akka.stream.javadsl.Source
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord

class CentralMonitoringService(system: ActorSystem, config: Config) {
    private val objectMapper = jacksonObjectMapper()

    private val kafkaBootstrapServers = config.getString("kafka.bootstrap-servers")
    private val groupIdSensors = config.getString("kafka.group-id-sensors")
    private val groupIdWarnings = config.getString("kafka.group-id-warnings")
    private val autoOffsetReset = config.getString("kafka.auto-offset-reset")
    private val sensorTopics = config.getStringList("sensor-topics")
    private val warningTopics = config.getStringList("warning-topics")

    private val thresholds = config.getConfig("thresholds")

    private val materializer: Materializer = ActorMaterializer.create(system)

    private val sensorConsumerSettings = ConsumerSettings.create(system, StringDeserializer(), StringDeserializer())
        .withBootstrapServers(kafkaBootstrapServers)
        .withGroupId(groupIdSensors)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset)

    private val warningConsumerSettings = ConsumerSettings.create(system, StringDeserializer(), StringDeserializer())
        .withBootstrapServers(kafkaBootstrapServers)
        .withGroupId(groupIdWarnings)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset)

    fun monitor() {
        monitorSensors()
        monitorWarnings()
    }

    private fun monitorSensors() {
        val sensorSource: Source<ConsumerRecord<String, String>, Consumer.Control> = Consumer.plainSource(
            sensorConsumerSettings, Subscriptions.topics(*sensorTopics.toTypedArray())
        )

        sensorSource.map { record ->
            objectMapper.readValue<SensorData>(record.value())
        }.runWith(Sink.foreach { sensorData ->
            checkThreshold(sensorData)
        }, materializer)
    }


    private fun monitorWarnings() {
        val warningSource: Source<ConsumerRecord<String, String>, Consumer.Control> = Consumer.plainSource(
            warningConsumerSettings, Subscriptions.topics(*warningTopics.toTypedArray())
        )

        warningSource.map { record ->
            objectMapper.readValue<WarningMessage>(record.value())
        }.runWith(Sink.foreach { warningMessage ->
            processWarning(warningMessage)
        }, materializer)
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
    val system = ActorSystem.create("CentralMonitoringSystem")

    // Load configuration with fallback to application.conf
    val config = if (args.isNotEmpty()) {
        ConfigFactory.parseString(args.joinToString(",")).withFallback(ConfigFactory.load())
    } else {
        ConfigFactory.load()
    }

    val monitoringService = CentralMonitoringService(system, config)
    monitoringService.monitor()
}
