package com.davithayrapetyan.weathermonitoring

import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.*

data class SensorData(val warehouseId: String, val sensorId: String, val value: Int)

class WarehouseService(
    private val warehouseId: String,
    private val udpPorts: List<Int>,
    private val kafkaBootstrapServers: String,
    private val noSensorDataTimeout: Long
) {
//    private val producer: KafkaProducer<String, String>
    private val lastReceivedTimeMap = mutableMapOf<Int, Long>()

//    init {
//        val props = Properties().apply {
//            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers)
//            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
//            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
//        }
//        producer = KafkaProducer(props)
//    }

    fun startListening() = runBlocking {
        udpPorts.forEach { port ->
            lastReceivedTimeMap[port] = System.currentTimeMillis()
            launch(Dispatchers.IO) {
                listenOnPort(port)
            }
        }

        // Launch a coroutine to check for sensor data timeout on each port
        launch(Dispatchers.IO) {
            while (true) {
                delay(1000)
                checkForTimeouts()
            }
        }
    }

    private fun listenOnPort(port: Int) {
        val socket = DatagramSocket(port)
        val buffer = ByteArray(1024)

        println("Listening on UDP port $port")

        while (true) {
            val packet = DatagramPacket(buffer, buffer.size)
            socket.receive(packet)
            val message = String(packet.data, 0, packet.length)
            processMessage(message, port)
        }
    }

    private fun processMessage(message: String, port: Int) {
        val regex = Regex("""sensor_id=(\w+),\s*value=(-?\d+)""")
        val matchResult = regex.find(message)

        if (matchResult != null) {
            lastReceivedTimeMap[port] = System.currentTimeMillis() // Update last received time for this port
            val sensorId = matchResult.groupValues[1]
            val value = matchResult.groupValues[2].toInt()
            val sensorData = SensorData(warehouseId, sensorId, value)
            sendToKafka(sensorData)
        } else {
            println("Invalid message format: $message")
        }
    }

    private fun sendToKafka(sensorData: SensorData) {
        val dataString = "${sensorData.warehouseId},${sensorData.sensorId},${sensorData.value}"
        val key = "${sensorData.warehouseId}:${sensorData.sensorId}"
//        producer.send(ProducerRecord("sensors", key, dataString))
        println("Sent to Kafka: $key -> $dataString")
    }

    private fun checkForTimeouts() {
        val currentTime = System.currentTimeMillis()
        lastReceivedTimeMap.forEach { (port, lastReceivedTime) ->
            if (currentTime - lastReceivedTime > noSensorDataTimeout) {
                val warningMessage = "WARNING: No sensor data received on port $port for $noSensorDataTimeout ms in warehouse $warehouseId"
//                producer.send(ProducerRecord("sensors", "$warehouseId:WARNING:$port", warningMessage))
                println(warningMessage)
                lastReceivedTimeMap[port] = currentTime // Reset the timer to prevent continuous warnings
            }
        }
    }
}

fun main(args: Array<String>) {
    val config = ConfigFactory.load()

    val warehouseId = args.getOrNull(0) ?: config.getString("warehouse.id")
    val udpPorts = args.getOrNull(1)?.split(",")?.map { it.toInt() }
        ?: config.getIntList("warehouse.ports").map { it.toInt() }
    val kafkaBootstrapServers = args.getOrNull(2) ?: config.getString("kafka.bootstrap-servers")
    val noSensorDataTimeout = args.getOrNull(3)?.toLong() ?: config.getLong("timeout.no-sensor-data")

    println("Starting WarehouseService with warehouseId: $warehouseId, ports: $udpPorts, Kafka: $kafkaBootstrapServers")

    val warehouseService = WarehouseService(warehouseId, udpPorts, kafkaBootstrapServers, noSensorDataTimeout)
    warehouseService.startListening()
}
