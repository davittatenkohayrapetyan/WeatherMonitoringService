package com.davithayrapetyan.weathermonitoring.threshold

import com.davithayrapetyan.weathermonitoring.SensorData
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HumidityThresholdCheckerTest {

    @Test
    fun `should return true when humidity exceeds threshold`() {
        val checker = HumidityThresholdChecker(60.0)
        val sensorData = SensorData(sensorId = "h1", warehouseId = "w1", value = 65.0)
        assertTrue(checker.check(sensorData))
    }

    @Test
    fun `should return false when humidity does not exceed threshold`() {
        val checker = HumidityThresholdChecker(60.0)
        val sensorData = SensorData(sensorId = "h1", warehouseId = "w1", value = 55.0)
        assertFalse(checker.check(sensorData))
    }
}
