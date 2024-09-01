package com.davithayrapetyan.weathermonitoring.threshold

import com.typesafe.config.Config

class ThresholdCheckerFactory(private val thresholds: Config) {

    fun create(sensorId: String): ThresholdChecker? {
        val thresholdEntry = thresholds.root().entries.find {
            val config = thresholds.getConfig(it.key)
            val prefixPattern = config.getString("prefix").toRegex()
            prefixPattern.containsMatchIn(sensorId)
        }

        return thresholdEntry?.let {
            val config = thresholds.getConfig(it.key)
            val thresholdValue = config.getDouble("value")
            when (it.key) {
                "temperature" -> TemperatureThresholdChecker(thresholdValue)
                "humidity" -> HumidityThresholdChecker(thresholdValue)
                // Add other sensor types as needed
                else -> null
            }
        }
    }
}