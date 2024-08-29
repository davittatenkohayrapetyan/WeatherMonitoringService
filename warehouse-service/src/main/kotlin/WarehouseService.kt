package com.davithayrapetyan.weathermonitoring

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

class WarehouseService(private val warehouseId: String) {
    private val producer: KafkaProducer<String, String>

    init {
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        }
        producer = KafkaProducer(props)
    }

    fun sendSensorData(sensorId: String, value: Double) {
        val sensorData = SensorData(warehouseId, sensorId, value)
        val dataString = "${sensorData.warehouseId},${sensorData.sensorId},${sensorData.value}"
        producer.send(ProducerRecord("sensors", sensorId, dataString))
    }
}

fun main() {
    val warehouseService = WarehouseService("warehouse-1")
    warehouseService.sendSensorData("t1", 36.0)
    warehouseService.sendSensorData("h1", 55.0)
}
