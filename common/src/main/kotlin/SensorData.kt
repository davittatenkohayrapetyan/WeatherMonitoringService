package com.davithayrapetyan.weathermonitoring

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.Deserializer

data class SensorData(val warehouseId: String, val sensorId: String, val value: Double, val version: Int = 1)

data class WarningMessage(val warehouseId: String, val port: Int, val message: String, val version: Int = 1)

// Custom serializer for SensorData
class SensorDataSerializer : Serializer<SensorData> {
    private val objectMapper = jacksonObjectMapper()

    override fun serialize(topic: String, data: SensorData?): ByteArray? {
        return if (data == null) {
            null
        } else {
            objectMapper.writeValueAsBytes(data)
        }
    }
}

// Custom serializer for WarningMessage
class WarningMessageSerializer : Serializer<WarningMessage> {
    private val objectMapper = jacksonObjectMapper()

    override fun serialize(topic: String, data: WarningMessage?): ByteArray? {
        return if (data == null) {
            null
        } else {
            objectMapper.writeValueAsBytes(data)
        }
    }
}

// Custom deserializer (if needed) for SensorData
class SensorDataDeserializer : Deserializer<SensorData> {
    private val objectMapper = jacksonObjectMapper()

    override fun deserialize(topic: String, data: ByteArray?): SensorData? {
        return data?.let { objectMapper.readValue(it, SensorData::class.java) }
    }
}

// Custom deserializer (if needed) for WarningMessage
class WarningMessageDeserializer : Deserializer<WarningMessage> {
    private val objectMapper = jacksonObjectMapper()

    override fun deserialize(topic: String, data: ByteArray?): WarningMessage? {
        return data?.let { objectMapper.readValue(it, WarningMessage::class.java) }
    }
}