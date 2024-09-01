package com.davithayrapetyan.weathermonitoring.threshold

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ThresholdCheckerFactoryTest {

    private val config = ConfigFactory.parseString(
        """
        thresholds {
            temperature {
                prefix = "t"
                value = 25.0
            }
            humidity {
                prefix = "h"
                value = 60.0
            }
        }
        """.trimIndent()
    )

    private val factory = ThresholdCheckerFactory(config.getConfig("thresholds"))

    @Test
    fun `should create TemperatureThresholdChecker for temperature sensor`() {
        val checker = factory.create("t1")
        assertNotNull(checker)
        assert(checker is TemperatureThresholdChecker)
    }

    @Test
    fun `should create HumidityThresholdChecker for humidity sensor`() {
        val checker = factory.create("h1")
        assertNotNull(checker)
        assert(checker is HumidityThresholdChecker)
    }

    @Test
    fun `should return null for unknown sensor type`() {
        val checker = factory.create("unknown1")
        assertNull(checker)
    }
}
