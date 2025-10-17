# Payments Engine Docker Setup

This directory contains the Docker configuration for running the Payments Engine locally with all its dependencies.

## Overview

The Docker setup includes:
- **Infrastructure Services**: PostgreSQL, Redis, Kafka, Zookeeper
- **Payments Engine Services**: All 6 microservices
- **Monitoring & Observability**: Prometheus, Grafana, Jaeger

## Quick Start

### Prerequisites

- Docker Desktop (Windows/Mac) or Docker Engine (Linux)
- Docker Compose v2.0+
- At least 8GB RAM available for Docker
- Ports 3000, 5432, 6379, 8081-8086, 9090, 9092, 16686 available

### Start All Services

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Start Individual Services

```bash
# Start only infrastructure
docker-compose up -d postgres redis kafka zookeeper

# Start specific service
docker-compose up -d payment-initiation-service
```

## Service Endpoints

### Payments Engine Services

| Service | Port | Health Check | API Documentation |
|---------|------|--------------|-------------------|
| Payment Initiation | 8081 | http://localhost:8081/actuator/health | http://localhost:8081/swagger-ui.html |
| Validation | 8082 | http://localhost:8082/actuator/health | http://localhost:8082/swagger-ui.html |
| Account Adapter | 8083 | http://localhost:8083/actuator/health | http://localhost:8083/swagger-ui.html |
| Routing | 8084 | http://localhost:8084/actuator/health | http://localhost:8084/swagger-ui.html |
| Transaction Processing | 8085 | http://localhost:8085/actuator/health | http://localhost:8085/swagger-ui.html |
| Saga Orchestrator | 8086 | http://localhost:8086/actuator/health | http://localhost:8086/swagger-ui.html |

### Infrastructure Services

| Service | Port | Description |
|---------|------|-------------|
| PostgreSQL | 5432 | Main database |
| Redis | 6379 | Caching layer |
| Kafka | 9092 | Message broker |
| Prometheus | 9090 | Metrics collection |
| Grafana | 3000 | Monitoring dashboards |
| Jaeger | 16686 | Distributed tracing |

## Health Checks

All services include health checks that verify:
- Service is running and responding
- Database connectivity
- Redis connectivity
- Kafka connectivity

### Check Service Health

```bash
# Check all services
docker-compose ps

# Check specific service logs
docker-compose logs payment-initiation-service

# Check service health endpoint
curl http://localhost:8081/actuator/health
```

## Monitoring & Observability

### Prometheus Metrics

- **URL**: http://localhost:9090
- **Purpose**: Metrics collection and alerting
- **Services**: All microservices expose metrics on `/actuator/prometheus`

### Grafana Dashboards

- **URL**: http://localhost:3000
- **Username**: admin
- **Password**: admin
- **Purpose**: Visualization and monitoring dashboards

### Jaeger Tracing

- **URL**: http://localhost:16686
- **Purpose**: Distributed tracing across services
- **Features**: Request tracing, performance analysis

## Database Setup

### PostgreSQL Databases

The setup creates separate databases for each service:
- `payment_initiation` - Payment Initiation Service
- `validation` - Validation Service
- `account_adapter` - Account Adapter Service
- `routing` - Routing Service
- `transaction_processing` - Transaction Processing Service
- `saga_orchestrator` - Saga Orchestrator Service

### Database Connection

```bash
# Connect to PostgreSQL
docker exec -it payments-postgres psql -U payments_user -d payments_engine

# List all databases
docker exec -it payments-postgres psql -U payments_user -c "\l"
```

## Kafka Topics

The setup automatically creates the following topics:
- `payment.initiated.v1`
- `payment.validated.v1`
- `payment.failed.v1`
- `payment.completed.v1`
- `transaction.created.v1`
- `transaction.completed.v1`
- `saga.started.v1`
- `saga.completed.v1`

### Kafka Management

```bash
# List topics
docker exec -it payments-kafka kafka-topics --bootstrap-server localhost:9092 --list

# View topic messages
docker exec -it payments-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic payment.initiated.v1 --from-beginning
```

## Development Workflow

### 1. Start Infrastructure

```bash
# Start only infrastructure services
docker-compose up -d postgres redis kafka zookeeper prometheus grafana jaeger
```

### 2. Build and Start Services

```bash
# Build all services
docker-compose build

# Start specific service
docker-compose up -d payment-initiation-service
```

### 3. View Logs

```bash
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f payment-initiation-service
```

### 4. Test Services

```bash
# Test payment initiation
curl -X POST http://localhost:8081/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000.00,
    "currency": "ZAR",
    "sourceAccount": "1234567890",
    "destinationAccount": "0987654321",
    "paymentType": "EFT",
    "priority": "HIGH"
  }'
```

## Troubleshooting

### Common Issues

1. **Port Conflicts**
   ```bash
   # Check which ports are in use
   netstat -tulpn | grep :8081
   
   # Stop conflicting services
   sudo systemctl stop postgresql
   ```

2. **Memory Issues**
   ```bash
   # Check Docker memory usage
   docker stats
   
   # Increase Docker memory limit in Docker Desktop
   ```

3. **Database Connection Issues**
   ```bash
   # Check PostgreSQL logs
   docker-compose logs postgres
   
   # Restart PostgreSQL
   docker-compose restart postgres
   ```

4. **Kafka Issues**
   ```bash
   # Check Kafka logs
   docker-compose logs kafka
   
   # Restart Kafka
   docker-compose restart kafka
   ```

### Service Dependencies

Services start in the following order:
1. **Infrastructure**: PostgreSQL, Redis, Kafka, Zookeeper
2. **Services**: Payment Initiation, Validation, Account Adapter, Routing, Transaction Processing, Saga Orchestrator
3. **Monitoring**: Prometheus, Grafana, Jaeger

### Health Check Failures

If health checks fail:
1. Check service logs: `docker-compose logs <service-name>`
2. Verify dependencies are healthy: `docker-compose ps`
3. Check network connectivity: `docker network ls`
4. Restart the service: `docker-compose restart <service-name>`

## Environment Variables

### Service Configuration

Each service can be configured using environment variables:

```yaml
environment:
  SPRING_PROFILES_ACTIVE: docker
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/payment_initiation
  SPRING_DATASOURCE_USERNAME: payments_user
  SPRING_DATASOURCE_PASSWORD: payments_password
  SPRING_REDIS_HOST: redis
  SPRING_REDIS_PORT: 6379
  SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
```

### Custom Configuration

To override default configuration:

1. Create a `.env` file in the project root
2. Add your custom variables:
   ```bash
   POSTGRES_PASSWORD=my_custom_password
   REDIS_PASSWORD=my_redis_password
   ```

## Production Considerations

This Docker setup is designed for **development and testing only**. For production:

1. **Security**: Use proper secrets management
2. **Networking**: Configure proper network security
3. **Persistence**: Use external volumes for data
4. **Monitoring**: Set up proper alerting
5. **Scaling**: Use orchestration platforms (Kubernetes)

## Verified Running Application

### Current Setup

- **Docker Version**: 20.10.24
- **Docker Compose Version**: 2.20.2
- **Operating System**: Ubuntu 22.04 LTS
- **RAM**: 16GB
- **CPU**: 4 cores

### Known Issues and Fixes

1. **Port Conflicts**
   - **Issue**: Port 8081 is often in use by other applications.
   - **Fix**: Use `sudo lsof -i :8081` to find the process and `sudo kill -9 <PID>` to kill it.
   - **Alternative**: Use a different port for the Payments Engine (e.g., 8081-8086).

2. **Memory Issues**
   - **Issue**: Docker Desktop might run out of memory.
   - **Fix**: Increase Docker Desktop memory limit in settings.
   - **Alternative**: Use Docker Engine (Linux) with more RAM.

3. **Database Connection Issues**
   - **Issue**: PostgreSQL might not start or be reachable.
   - **Fix**: Check PostgreSQL logs (`docker-compose logs postgres`) and restart (`docker-compose restart postgres`).
   - **Alternative**: Ensure `SPRING_DATASOURCE_URL` in `.env` is correct.

4. **Kafka Issues**
   - **Issue**: Kafka might not start or be reachable.
   - **Fix**: Check Kafka logs (`docker-compose logs kafka`) and restart (`docker-compose restart kafka`).
   - **Alternative**: Ensure `SPRING_KAFKA_BOOTSTRAP_SERVERS` in `.env` is correct.

### How to Verify

1. **Check All Services**
   ```bash
   # Start all services
   docker-compose up -d
   
   # View logs
   docker-compose logs -f
   
   # Check health endpoints
   curl http://localhost:8081/actuator/health
   curl http://localhost:8082/actuator/health
   curl http://localhost:8083/actuator/health
   curl http://localhost:8084/actuator/health
   curl http://localhost:8085/actuator/health
   curl http://localhost:8086/actuator/health
   ```

2. **Check Monitoring**
   - **Prometheus**: http://localhost:9090
   - **Grafana**: http://localhost:3000 (admin/admin)
   - **Jaeger**: http://localhost:16686

3. **Check Database**
   ```bash
   # Connect to PostgreSQL
   docker exec -it payments-postgres psql -U payments_user -d payments_engine
   
   # List databases
   docker exec -it payments-postgres psql -U payments_user -c "\l"
   ```

4. **Check Kafka**
   ```bash
   # List topics
   docker exec -it payments-kafka kafka-topics --bootstrap-server localhost:9092 --list
   
   # View messages
   docker exec -it payments-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic payment.initiated.v1 --from-beginning
   ```

## Cleanup

### Stop and Remove All

```bash
# Stop all services
docker-compose down

# Remove volumes (WARNING: This deletes all data)
docker-compose down -v

# Remove images
docker-compose down --rmi all
```