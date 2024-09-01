# Weather Monitoring System

This project is a weather monitoring system composed of multiple services: `small-warehouse`, `big-warehouse`, `failing-warehouse`, and a `central-monitoring-service`. These services communicate via Apache Kafka and are managed using Docker Compose.

## Project Structure

- **common module**: Contains shared utilities, data structures, and custom serializers/deserializers.
- **warehouse-service module**: Represents warehouses with sensors that send data to the Kafka broker.
- **central-monitoring-service module**: Monitors the data received from warehouses and generates warnings if necessary.

### Sub-modules

This project is organized into several sub-modules, each of which has its own `README.md` file for detailed information:

- **[Common Module](common/README.md)**: Shared utilities, data structures, and custom serializers/deserializers.
- **[Warehouse Service Module](warehouse-service/README.md)**: Handles the threshold for individual warehouses, including sensor data handling and communication with the Kafka broker.
- **[Central Monitoring Service Module](central-monitoring-service/README.md)**: Monitors the data received from various warehouses, aggregates information, and generates warnings.

## Docker Compose Setup

The `docker-compose.yml` file is used to spin up the entire project, including multiple warehouse services and a central monitoring service. The setup includes:

- **Zookeeper**: Manages the Kafka broker.
- **Kafka**: Serves as the message broker for communication between services.
- **small-warehouse**: A warehouse service listening on ports `3344` and `3355`.
- **big-warehouse**: A larger warehouse service listening on ports `3366` and `3377`.
- **failing-warehouse**: A warehouse service meant to simulate failure, listening on port `3344`.
- **central-monitoring-service**: Aggregates data from all warehouses and monitors for issues.

## How to Use

### Step 1: Build and Start Services

First, ensure you have Docker and Docker Compose installed on your machine. Then, navigate to the project directory and run:

```
docker-compose up --build
```

This command will build the Docker images and start all the services defined in the `docker-compose.yml` file.

### Step 2: Simulate Sensor Data

To test the sensors, you can simulate sensor data using the `netcat` (nc) command. For example, to send data to the `small-warehouse` service on port `3344`, run:

```
echo -n "sensor_id=t1, value=20" | nc -u -w1 localhost 3344
```

You can repeat this command with different values and ports to simulate data for different sensors and warehouses.

### Step 3: Test System Behavior

The system is designed to handle warehouse failures. To test this, you can manually stop the `failing-warehouse` service and observe how the `central-monitoring-service` reacts:

```
docker-compose stop failing-warehouse
```

This should trigger the `central-monitoring-service` to generate a warning due to the lack of sensor data from the `failing-warehouse`.

### Step 4: View Logs

To monitor the logs for any of the services, use the following command:

```
docker-compose logs -f &lt;service-name&gt;
```

For example, to view the logs for the `central-monitoring-service`, run:

```
docker-compose logs -f central-monitoring-service
```

### Step 5: Clean Up

To stop and remove all containers, networks, and volumes created by Docker Compose, run:

```
docker-compose down
```

## Additional Information

- **Kafka Topics**: The system uses separate Kafka topics for sensor data and warning messages. The `central-monitoring-service` listens to these topics to process the data.
- **Timeout Handling**: Each warehouse is configured with a timeout for receiving sensor data. If no data is received within the timeout period, a warning message is generated and sent to the central monitoring service.

## Notes

- The `failing-warehouse` service is configured to simulate a failing warehouse by stopping it manually. This is useful for testing the robustness of the system.
- You can add more warehouses or modify the existing ones by editing the `docker-compose.yml` file.
