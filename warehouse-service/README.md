# Warehouse Service

## Overview

The `WarehouseService` is a Kotlin-based service designed to monitor sensor data from a warehouse. It listens on specified UDP ports for incoming sensor data, processes the data, and forwards it to an Apache Kafka cluster. Additionally, the service monitors for timeouts on each port and sends warnings to Kafka if no data is received within a specified period.

## Project Structure

```
com.davithayrapetyan.weathermonitoring
├── src
│   └── main
│       ├── kotlin 
│       │   └── WarehouseService.kt
│       └── resources
│           └── application.conf
└── Dockerfile
```

## Configuration

### `application.conf`

The configuration file `application.conf` is used to set default values for the warehouse ID, UDP ports, Kafka bootstrap servers, and sensor data timeout.

```
warehouse {
  id = "default-warehouse"
  ports = [3344, 3355]
}

kafka {
bootstrap-servers = "kafka:9092"
}

timeout {
no-sensor-data = 60000 # 60 seconds
}
```

### Environment Variables

You can override the configuration using environment variables when running the service:

- `WAREHOUSE_ID`: The ID of the warehouse.
- `KAFKA_BOOTSTRAP_SERVERS`: The Kafka bootstrap servers.
- `NO_SENSOR_DATA_TIMEOUT`: Timeout in milliseconds for detecting no sensor data.
- `WAREHOUSE_PORTS`: Comma-separated list of UDP ports to listen on.

## Running the Service

### Prerequisites

- Java 21 or higher
- Apache Kafka
- Docker (if running the service inside a container)

### Running Locally

1. Build the project:

   ```./gradlew build```

2. Run the service:

   ```java -jar build/libs/warehouse-service-1.0-SNAPSHOT.jar```

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
COPY warehouse-service/build/libs/warehouse-service-${VERSION}.jar warehouse-service.jar

# Define default environment variables
ENV WAREHOUSE_ID="default-warehouse"
ENV KAFKA_BOOTSTRAP_SERVERS="kafka:9092"
ENV NO_SENSOR_DATA_TIMEOUT=60000
ENV WAREHOUSE_PORTS=3344,3355

# Specify the entry point for the container, allowing configurable arguments
ENTRYPOINT sh -c 'java -jar warehouse-service.jar \
--warehouse.id=${WAREHOUSE_ID} \
--kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS} \
--timeout.no-sensor-data=${NO_SENSOR_DATA_TIMEOUT} \
--warehouse.ports=${WAREHOUSE_PORTS}'
```
To build and run the Docker container:

1. Build the Docker image:


   ```docker build -t warehouse-service:1.0 .```


2. Run the Docker container:


   ```docker run -e WAREHOUSE_ID="my-warehouse" -e KAFKA_BOOTSTRAP_SERVERS="kafka:9092" warehouse-service:1.0```

## Logging

The service uses SLF4J with the default logging configuration. Logs are output to the console and include important events such as sensor data reception, warnings, and errors.

## Extending the Service

To extend the service, you can modify or add the following components:

- **Timeout Logic**: Customize the timeout logic in the `checkForTimeouts` method.
- **Sensor Data Processing**: Modify how sensor data is processed in the `processMessage` method.
