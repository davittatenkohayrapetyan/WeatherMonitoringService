# Central Monitoring Service

## Overview

The `CentralMonitoringService` is a Kotlin-based service designed to monitor sensor data and warnings from multiple warehouses. It receives data from Apache Kafka, checks the sensor data against predefined thresholds, and monitors the activity of each warehouse. If no data is received from a warehouse within a specified interval, the service raises a timeout warning.

## Project Structure

```
com.davithayrapetyan.weathermonitoring
├── src
│   └── main
│       ├── kotlin
│       │   ├── CentralMonitoringService.kt
│       │   └── threshold
│       │       ├── HumidityThresholdChecker.kt
│       │       ├── TemperatureThresholdChecker.kt
│       │       ├── ThresholdChecker.kt
│       │       └── ThresholdCheckerFactory.kt
│       └── resources
│           └── application.conf
└── Dockerfile
```

## Configuration

### `application.conf`

The configuration file `application.conf` is used to set default values for Kafka settings, sensor topics, warning topics, monitoring intervals, and thresholds for sensor data.

```
kafka {
  bootstrap-servers = "kafka:9092"
  group-id-sensors = "sensor-group"
  group-id-warnings = "warning-group"
  auto-offset-reset = "earliest"
}

sensor-topics = ["sensors"]
warning-topics = ["sensor-warnings"]

monitoring {
interval = 10 minutes
}

thresholds {
temperature {
prefix = "t\\d+"
value = 25.0
}
humidity {
prefix = "h\\d+"
value = 60.0
}
}
```

### Environment Variables

You can override the configuration using environment variables when running the service:

- `KAFKA_BOOTSTRAP_SERVERS`: The Kafka bootstrap servers.
- `GROUP_ID_SENSORS`: The Kafka group ID for sensor topics.
- `GROUP_ID_WARNINGS`: The Kafka group ID for warning topics.
- `AUTO_OFFSET_RESET`: Kafka auto offset reset configuration.
- `SENSOR_TOPICS`: List of Kafka topics for sensor data.
- `WARNING_TOPICS`: List of Kafka topics for warning messages.
- `MONITORING_INTERVAL`: Interval for monitoring warehouse activity.

## Running the Service

### Prerequisites

- Java 21 or higher
- Apache Kafka
- Docker (if running the service inside a container)

### Running Locally

1. Build the project:

   ```
   ./gradlew build
   ```

2. Run the service:

   ```
   java -jar build/libs/central-monitoring-service-1.0-SNAPSHOT.jar
   ```

### Running with Docker

A Dockerfile is provided to containerize the service.

```
# Use a base image with JDK
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# ARG to pass the version
ARG VERSION=1.0-SNAPSHOT

# Copy the compiled JAR file into the container
COPY central-monitoring-service/build/libs/central-monitoring-service-${VERSION}.jar central-monitoring-service.jar

# Define default environment variables
ENV KAFKA_BOOTSTRAP_SERVERS="kafka:9092"
ENV GROUP_ID_SENSORS="sensor-group"
ENV GROUP_ID_WARNINGS="warning-group"
ENV AUTO_OFFSET_RESET="earliest"
ENV SENSOR_TOPICS="sensors"
ENV WARNING_TOPICS="sensor-warnings"
ENV MONITORING_INTERVAL="10 minutes"

# Specify the entry point for the container, allowing configurable arguments
ENTRYPOINT sh -c 'java -jar central-monitoring-service.jar \
--kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS} \
--group-id-sensors=${GROUP_ID_SENSORS} \
--group-id-warnings=${GROUP_ID_WARNINGS} \
--auto-offset-reset=${AUTO_OFFSET_RESET} \
--sensor-topics=${SENSOR_TOPICS} \
--warning-topics=${WARNING_TOPICS} \
--monitoring.interval=${MONITORING_INTERVAL}'
```

To build and run the Docker container:

1. Build the Docker image:

   ```
   docker build -t central-monitoring-service:1.0 .
   ```

2. Run the Docker container:

   ```
   docker run -e KAFKA_BOOTSTRAP_SERVERS="kafka:9092" -e GROUP_ID_SENSORS="sensor-group" central-monitoring-service:1.0
   ```

## Logging

The service outputs logs to the console, detailing sensor data monitoring, warnings, and any threshold breaches. The logging level and format can be configured using standard SLF4J logging mechanisms.

## Extending the Service

To extend the service, you can modify or add the following components:

- **Thresholds**: Define new thresholds in the configuration for additional sensor types.
- **Warning Processing**: Customize the handling of warning messages in the `processWarning` method.
- **Monitoring Logic**: Adjust the monitoring interval and add custom checks in the `startWarehouseTimeoutMonitor` method.

## Contributing

Contributions are welcome. Please submit a pull request or open an issue on the GitHub repository.

## License

This project is licensed under the MIT License. See the LICENSE file for details.
