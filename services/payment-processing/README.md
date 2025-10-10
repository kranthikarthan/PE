# Payment Processing Service

## Overview
The Payment Processing Service is the core component of the Payment Engine platform, responsible for processing ISO20022 payment messages, orchestrating payment workflows, and integrating with clearing systems and core banking platforms.

## Features
- ✅ **ISO20022 Message Processing**: Full support for pain.001, pacs.008, pacs.002, and other ISO20022 message types
- ✅ **Message Transformation**: Automatic transformation between customer and financial institution message formats
- ✅ **Validation**: Comprehensive message validation against ISO20022 schemas
- ✅ **Kafka Integration**: Event-driven architecture with exactly-once semantics
- ✅ **Dead Letter Queue**: Robust error handling with DLQ pattern
- ✅ **Circuit Breakers**: Resilience4j integration for fault tolerance
- ✅ **Observability**: Metrics, distributed tracing, and structured logging
- ✅ **Multi-tenancy**: Full tenant isolation and configuration
- ✅ **Security**: JWT authentication, rate limiting, and audit logging

## Technology Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.1
- **Messaging**: Apache Kafka
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Monitoring**: Prometheus, Grafana, Jaeger
- **Testing**: JUnit 5, Testcontainers, MockMvc

## Quick Start

### Prerequisites
- Java 17 or higher
- Docker & Docker Compose
- Maven 3.8.6+ (or use included Maven wrapper)

### Local Development Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/payment-engine/payment-processing.git
   cd payment-processing
   ```

2. **Start infrastructure**:
   ```bash
   make dev
   ```
   This starts PostgreSQL, Kafka, Redis, Prometheus, Grafana, and other services.

3. **Build the application**:
   ```bash
   ./mvnw clean install
   ```

4. **Run the application**:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

5. **Access the application**:
   - API: http://localhost:8080
   - Actuator: http://localhost:8081/actuator
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Prometheus Metrics: http://localhost:8081/actuator/prometheus

### Using Make Commands

```bash
# Start development environment
make dev

# Run tests
make test

# Run integration tests
make integration-test

# Build Docker image
make docker-build

# Stop environment
make down

# Clean everything
make clean
```

## API Documentation

### ISO20022 Endpoints

#### Process PAIN.001 (Customer Credit Transfer)
```bash
POST /api/v1/iso20022/comprehensive/pain001-to-clearing-system
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "CstmrCdtTrfInitn": {
    "GrpHdr": {
      "MsgId": "MSG-001",
      "CreDtTm": "2025-10-10T12:00:00",
      "NbOfTxs": "1"
    },
    ...
  }
}
```

#### Validate ISO20022 Message
```bash
POST /api/v1/iso20022/comprehensive/validate?messageType=pain.001
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "CstmrCdtTrfInitn": { ... }
}
```

For complete API documentation, refer to the [Postman Collection](../../postman/ISO20022-Payment-Engine.postman_collection.json).

## Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_HOST` | PostgreSQL host | localhost | Yes |
| `DB_PORT` | PostgreSQL port | 5432 | Yes |
| `DB_NAME` | Database name | payment_engine | Yes |
| `DB_USER` | Database username | - | Yes |
| `DB_PASSWORD` | Database password | - | Yes |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers | localhost:9092 | Yes |
| `REDIS_HOST` | Redis host | localhost | Yes |
| `REDIS_PORT` | Redis port | 6379 | Yes |
| `JWT_SECRET_KEY` | JWT signing key | - | Yes |
| `SPRING_PROFILES_ACTIVE` | Active profile | dev | No |

### Application Profiles

- **dev**: Development environment with debug logging and all actuator endpoints enabled
- **test**: Test environment for integration testing
- **prod**: Production environment with security hardening and JSON logging

## Testing

### Unit Tests
```bash
make test
# or
./mvnw test
```

### Integration Tests (with Testcontainers)
```bash
make integration-test
# or
./mvnw verify
```

### E2E Tests
```bash
make test-e2e
```

### Code Coverage
```bash
make test-coverage
```
Coverage reports are generated at `target/site/jacoco/index.html`.

## Monitoring & Observability

### Metrics
- **Prometheus**: Metrics exposed at `/actuator/prometheus`
- **Grafana Dashboards**: Pre-configured dashboards for service health, throughput, and latency

### Distributed Tracing
- **Jaeger**: Traces sent to Jaeger for end-to-end request tracking
- **Trace ID**: Automatically added to logs via MDC

### Logging
- **Format**: JSON (production), Pattern (development)
- **MDC Fields**: `correlationId`, `tenantId`, `messageId`, `transactionId`
- **Log Levels**: Configurable per package

### Health Checks
- **Liveness**: `/actuator/health/liveness`
- **Readiness**: `/actuator/health/readiness`

## Deployment

### Docker
```bash
# Build image
make docker-build

# Run container
docker run -p 8080:8080 -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=postgres \
  paymentengine/payment-processing:latest
```

### Kubernetes (Helm)
```bash
# Install chart
helm install payment-processing ./helm/payment-processing \
  --namespace payment-engine \
  --values values-prod.yaml

# Upgrade
helm upgrade payment-processing ./helm/payment-processing \
  --namespace payment-engine
```

## Architecture

### Key Components
- **Controllers**: REST API endpoints for ISO20022 message ingestion
- **Services**: Business logic for message transformation, validation, and routing
- **Repositories**: Data access layer for persistence
- **Kafka Producers/Consumers**: Event publishing and consumption
- **Security**: JWT authentication and authorization

### Message Flow
1. Client sends ISO20022 message (e.g., pain.001)
2. Controller validates JWT and tenant
3. Service validates message structure
4. Message is transformed (pain.001 → pacs.008)
5. Transformed message is published to Kafka
6. Message is persisted to database
7. Acknowledgment (pain.002) is returned to client

## Troubleshooting

### Common Issues

#### Application won't start
- Check database connectivity: `psql -h localhost -U payment_user -d payment_engine`
- Verify Kafka is running: `docker-compose ps kafka`
- Check logs: `docker logs payment-processing`

#### Messages not being processed
- Check Kafka consumer lag: `make kafka-consumer-lag`
- Verify topic exists: `make kafka-topics`
- Check DLQ for failed messages

#### High latency
- Review Grafana dashboards for bottlenecks
- Check database connection pool settings
- Verify Kafka producer configuration

### Logs Location
- **Development**: Console output
- **Production**: `/var/log/payment-processing/application.json`

## Contributing
Please refer to [CONTRIBUTING.md](../../CONTRIBUTING.md) for guidelines.

## License
Proprietary - Payment Engine Platform

## Support
- **Documentation**: https://docs.paymentengine.com
- **Issue Tracker**: https://github.com/payment-engine/payment-processing/issues
- **Slack**: #payment-engine

## Changelog
See [CHANGELOG.md](./CHANGELOG.md) for version history.
