package com.davithayrapetyan.weathermonitoring

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SensorDataTests {

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `SensorData serialization works correctly`() {
        val sensorData = SensorData("warehouse123", "sensor456", 25.5)
        val serializer = SensorDataSerializer()

        val expectedJson = objectMapper.writeValueAsBytes(sensorData)
        val serializedData = serializer.serialize("test-topic", sensorData)

        assertArrayEquals(expectedJson, serializedData)
    }

    @Test
    fun `SensorData deserialization works correctly`() {
        val sensorData = SensorData("warehouse123", "sensor456", 25.5)
        val deserializer = SensorDataDeserializer()

        val jsonData = objectMapper.writeValueAsBytes(sensorData)
        val deserializedData = deserializer.deserialize("test-topic", jsonData)

        assertEquals(sensorData, deserializedData)
    }

    @Test
    fun `WarningMessage serialization works correctly`() {
        val warningMessage = WarningMessage("warehouse123", 8080, "Test warning")
        val serializer = WarningMessageSerializer()

        val expectedJson = objectMapper.writeValueAsBytes(warningMessage)
        val serializedData = serializer.serialize("test-topic", warningMessage)

        assertArrayEquals(expectedJson, serializedData)
    }

    @Test
    fun `WarningMessage deserialization works correctly`() {
        val warningMessage = WarningMessage("warehouse123", 8080, "Test warning")
        val deserializer = WarningMessageDeserializer()

        val jsonData = objectMapper.writeValueAsBytes(warningMessage)
        val deserializedData = deserializer.deserialize("test-topic", jsonData)

        assertEquals(warningMessage, deserializedData)
    }

    @Test
    fun `SensorData serialization with null data returns null`() {
        val serializer = SensorDataSerializer()

        val serializedData = serializer.serialize("test-topic", null)

        assertEquals(null, serializedData)
    }

    @Test
    fun `WarningMessage serialization with null data returns null`() {
        val serializer = WarningMessageSerializer()

        val serializedData = serializer.serialize("test-topic", null)

        assertEquals(null,serializedData)
    }

    @Test
    fun `SensorData deserialization with null data returns null`() {
        val deserializer = SensorDataDeserializer()

        val deserializedData = deserializer.deserialize("test-topic", null)

        assertEquals(null, deserializedData)
    }

    @Test
    fun `WarningMessage deserialization with null data returns null`() {
        val deserializer = WarningMessageDeserializer()

        val deserializedData = deserializer.deserialize("test-topic", null)

        assertEquals(null, deserializedData)
    }
}
