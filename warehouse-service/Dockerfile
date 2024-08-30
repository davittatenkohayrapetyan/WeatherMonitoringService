# Dockerfile for WarehouseService

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

# Specify the entry point for the container, allowing configurable arguments
ENTRYPOINT sh -c 'java -jar warehouse-service.jar \
            --warehouse.id=${WAREHOUSE_ID} \
            --kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS} \
            --timeout.no-sensor-data=${NO_SENSOR_DATA_TIMEOUT}'
