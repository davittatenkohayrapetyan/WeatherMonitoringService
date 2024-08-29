package com.davithayrapetyan.weathermonitoring

import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import akka.kafka.Subscriptions
import akka.kafka.javadsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import org.apache.kafka.common.serialization.StringDeserializer
import akka.stream.javadsl.Sink
import akka.stream.javadsl.Source
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord

class CentralMonitoringService(system: ActorSystem) {
    private val temperatureThreshold = 35.0
    private val humidityThreshold = 50.0

    private val materializer: Materializer = ActorMaterializer.create(system)
    private val consumerSettings = ConsumerSettings.create(system, StringDeserializer(), StringDeserializer())
        .withBootstrapServers("kafka:9092")
        .withGroupId("central-monitoring")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    fun monitor() {
        val source: Source<ConsumerRecord<String, String>, Consumer.Control> = Consumer.plainSource(
            consumerSettings, Subscriptions.topics("sensors")
        )

        source.map { record ->
            val data = record.value().split(",")
            SensorData(data[0], data[1], data[2].toDouble())
        }.runWith(Sink.foreach { sensorData ->
            checkThreshold(sensorData)
        }, materializer)
    }

    private fun checkThreshold(sensorData: SensorData) {
        val threshold = when (sensorData.sensorId) {
            "t1" -> temperatureThreshold
            "h1" -> humidityThreshold
            else -> return
        }
        if (sensorData.value > threshold) {
            println("ALARM: ${sensorData.warehouseId} - ${sensorData.sensorId} exceeded threshold with value ${sensorData.value}")
        }
    }
}

fun main() {
    val system = ActorSystem.create("CentralMonitoringSystem")
    val monitoringService = CentralMonitoringService(system)
    monitoringService.monitor()
}
