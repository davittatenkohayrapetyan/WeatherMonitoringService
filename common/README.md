# Common Module

This module contains utility classes and data structures used for weather monitoring. It includes serializers and deserializers for data exchanged between components.

## Package

`com.davithayrapetyan.weathermonitoring`

## Overview

### Data Classes

- **SensorData**: Represents the data collected from a sensor in a warehouse.
    - `warehouseId`: ID of the warehouse.
    - `sensorId`: ID of the sensor.
    - `value`: The value reported by the sensor.
    - `version`: Version of the data structure (default: `1`).

- **WarningMessage**: Represents a warning message related to a specific warehouse and port.
    - `warehouseId`: ID of the warehouse.
    - `port`: The port number associated with the warning.
    - `message`: The warning message.
    - `version`: Version of the message structure (default: `1`).

### Serialization and Deserialization

- **SensorDataSerializer**: Custom serializer for the `SensorData` class using Jackson's `ObjectMapper`.
    - Serializes `SensorData` objects to a byte array.
    - Returns `null` if the input data is `null`.

- **WarningMessageSerializer**: Custom serializer for the `WarningMessage` class using Jackson's `ObjectMapper`.
    - Serializes `WarningMessage` objects to a byte array.
    - Returns `null` if the input data is `null`.

- **SensorDataDeserializer**: Custom deserializer for the `SensorData` class.
    - Deserializes a byte array into a `SensorData` object.
    - Returns `null` if the input data is `null`.

- **WarningMessageDeserializer**: Custom deserializer for the `WarningMessage` class.
    - Deserializes a byte array into a `WarningMessage` object.
    - Returns `null` if the input data is `null`.

## Usage

This module is intended to be used in the context of weather monitoring, where sensor data and warning messages are serialized and deserialized for communication between different components of the system.

The serializers and deserializers are designed to work with Apache Kafka but can be used in any context requiring byte array serialization/deserialization.

### Example

```kotlin
val sensorData = SensorData("warehouse123", "sensor456", 25.5)
val serializer = SensorDataSerializer()
val serializedData = serializer.serialize("test-topic", sensorData)
val deserializer = SensorDataDeserializer()
val deserializedData = deserializer.deserialize("test-topic", serializedData)

println(deserializedData)  // Output: SensorData(warehouseId=warehouse123, sensorId=sensor456, value=25.5, version=1)
```