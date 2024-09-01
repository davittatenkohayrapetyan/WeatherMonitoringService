package com.davithayrapetyan.weathermonitoring.threshold

import com.davithayrapetyan.weathermonitoring.SensorData

interface ThresholdChecker {
    fun check(sensorData: SensorData): Boolean
}
