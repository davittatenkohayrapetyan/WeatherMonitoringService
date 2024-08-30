# Dockerfile for CentralMonitoringService

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
ENV KAFKA_GROUP_ID_SENSORS="central-monitoring-sensors"
ENV KAFKA_GROUP_ID_WARNINGS="central-monitoring-warnings"
ENV KAFKA_AUTO_OFFSET_RESET="earliest"

# Specify the entry point for the container, allowing configurable arguments
ENTRYPOINT sh -c 'java -jar central-monitoring-service.jar \
            --kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS} \
            --kafka.group-id-sensors=${KAFKA_GROUP_ID_SENSORS} \
            --kafka.group-id-warnings=${KAFKA_GROUP_ID_WARNINGS} \
            --kafka.auto-offset-reset=${KAFKA_AUTO_OFFSET_RESET}'
