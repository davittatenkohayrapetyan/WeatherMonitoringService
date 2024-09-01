package com.davithayrapetyan.weathermonitoring.threshold

import com.davithayrapetyan.weathermonitoring.SensorData

class TemperatureThresholdChecker(private val thresholdValue: Double) : ThresholdChecker {
    override fun check(sensorData: SensorData): Boolean {
        return sensorData.value > thresholdValue
    }
}