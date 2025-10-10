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
   - API: http://localhost:8082/payment-processing
   - Actuator: http://localhost:8082/payment-processing/actuator
   - Health Check: http://localhost:8082/payment-processing/actuator/health
   - Prometheus Metrics: http://localhost:8082/payment-processing/actuator/prometheus

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
POST /payment-processing/api/v1/iso20022/comprehensive/pain001-to-clearing-system
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
POST /payment-processing/api/v1/iso20022/comprehensive/validate?messageType=pain.001
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
| `SPRING_PROFILES_ACTIVE` | Active profile | local | No |

### Application Profiles

- **local**: Local development environment with debug logging and all actuator endpoints enabled
- **docker**: Docker Compose environment for containerized local development
- **production**: Production environment with security hardening and optimized settings

Note: Additional profiles `dev` and `prod` are available in `application-dev.yml` and `application-prod.yml` for enhanced configuration.

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
- **Prometheus**: Metrics exposed at `/payment-processing/actuator/prometheus`
- **Grafana Dashboards**: Pre-configured dashboards for service health, throughput, and latency
- **Custom Metrics**: Business metrics tracked via Micrometer

### Distributed Tracing
- **Jaeger**: Traces sent to Jaeger for end-to-end request tracking (when configured)
- **Trace ID**: Automatically added to logs via MDC
- **Correlation ID**: Propagated across service boundaries

### Logging
- **Format**: Pattern-based (local/docker), JSON (production with logback-spring.xml)
- **MDC Fields**: `correlationId`, `tenantId`, `messageId`, `transactionId`
- **Log Levels**: Configurable per package
- **Log File**: `logs/payment-processing.log` (configurable)

### Health Checks
- **Main Health**: `/payment-processing/actuator/health`
- **Liveness**: `/payment-processing/actuator/health/liveness` (requires health probes enabled)
- **Readiness**: `/payment-processing/actuator/health/readiness` (requires health probes enabled)

## Deployment

### Docker
```bash
# Build image
make docker-build

# Run container
docker run -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e DATABASE_URL=jdbc:postgresql://postgres:5432/payment_engine \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=yourpassword \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
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
- Check database connectivity: `psql -h localhost -U postgres -d payment_engine`
- Verify Kafka is running: `docker-compose -f docker-compose.dev.yml ps kafka`
- Check logs: `docker-compose -f docker-compose.dev.yml logs payment-processing` (if running via Docker Compose)

#### Messages not being processed
- Check Kafka consumer lag: `make kafka-consumer-lag`
- Verify topic exists: `make kafka-topics`
- Check DLQ for failed messages

#### High latency
- Review Grafana dashboards for bottlenecks
- Check database connection pool settings (Hikari pool size: max 20, min 5)
- Verify Kafka producer configuration (batch size, linger time)
- Check circuit breaker status via `/payment-processing/actuator/health`

### Logs Location
- **Development**: Console output + `logs/payment-processing.log`
- **Production**: `/var/log/payment-processing/application.json` (when logback-spring.xml is active)

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
