package com.davithayrapetyan.weathermonitoring.threshold

import com.davithayrapetyan.weathermonitoring.SensorData
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TemperatureThresholdCheckerTest {

    @Test
    fun `should return true when temperature exceeds threshold`() {
        val checker = TemperatureThresholdChecker(25.0)
        val sensorData = SensorData(sensorId = "t1", warehouseId = "w1", value = 30.0)
        assertTrue(checker.check(sensorData))
    }

    @Test
    fun `should return false when temperature does not exceed threshold`() {
        val checker = TemperatureThresholdChecker(25.0)
        val sensorData = SensorData(sensorId = "t1", warehouseId = "w1", value = 20.0)
        assertFalse(checker.check(sensorData))
    }
}
