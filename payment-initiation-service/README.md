# Payment Initiation Service

## Overview

The Payment Initiation Service is responsible for creating, validating, and managing payment requests in the payments engine. It serves as the entry point for payment processing workflows and implements comprehensive business rules validation.

## Features

- **Payment Creation**: Create new payment requests with comprehensive validation
- **Business Rules Engine**: Enforce payment business rules and compliance checks
- **Idempotency**: Ensure duplicate payment prevention with idempotency keys
- **Event Publishing**: Publish domain events for downstream processing
- **Multi-tenant Support**: Full tenant isolation and context management

## API Endpoints

### Health Check
```
GET /payment-initiation/actuator/health
```

### Payment Management
```
POST /payment-initiation/api/v1/payments
GET  /payment-initiation/api/v1/payments/{paymentId}
GET  /payment-initiation/api/v1/payments
PUT  /payment-initiation/api/v1/payments/{paymentId}/status
```

### Payment Status
```
GET /payment-initiation/api/v1/payments/{paymentId}/status
GET /payment-initiation/api/v1/payments/{paymentId}/history
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |
| `SPRING_DATASOURCE_URL` | Database connection URL | `jdbc:postgresql://postgres:5432/payment_initiation` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `payments_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `payments_password` |
| `SPRING_REDIS_HOST` | Redis host | `redis` |
| `SPRING_REDIS_PORT` | Redis port | `6379` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | `kafka:29092` |

### Application Profiles

- **`docker`**: Production Docker configuration
- **`local`**: Local development configuration
- **`test`**: Test configuration with in-memory databases

## Dependencies

### External Services
- **PostgreSQL**: Primary database for payment persistence
- **Redis**: Caching and session management
- **Kafka**: Event publishing and messaging
- **Validation Service**: Payment validation rules
- **Account Adapter**: Account balance and validation

### Internal Dependencies
- **Domain Models**: Shared domain entities and value objects
- **Contracts**: API contracts and DTOs
- **Shared Config**: Common configuration properties
- **Shared Telemetry**: Observability and monitoring

## Health Checks

The service exposes comprehensive health checks via Spring Boot Actuator:

- **Database Health**: PostgreSQL connection and query validation
- **Redis Health**: Cache connectivity and operations
- **Disk Space**: Available storage monitoring
- **Custom Health**: Business logic health indicators

## Monitoring

### Metrics
- Payment creation rate
- Validation success/failure rates
- Business rule execution times
- Database connection pool metrics
- Cache hit/miss ratios

### Logging
- Structured JSON logging
- Correlation ID tracking
- Tenant context logging
- Performance metrics

## Runbook

### Common Issues

#### Service Won't Start
1. Check database connectivity: `curl http://localhost:8081/payment-initiation/actuator/health`
2. Verify Redis connection: Check logs for Redis connection errors
3. Validate Kafka connectivity: Ensure Kafka is running and accessible

#### Payment Creation Failures
1. Check business rules validation: Review validation service logs
2. Verify account adapter connectivity: Ensure account service is responding
3. Check tenant context: Validate tenant ID and business unit

#### Performance Issues
1. Monitor database connection pool: Check for connection leaks
2. Review Redis cache performance: Monitor cache hit rates
3. Analyze Kafka producer performance: Check for message backlogs

### Troubleshooting Commands

```bash
# Check service health
curl -f http://localhost:8081/payment-initiation/actuator/health

# View service logs
docker-compose logs payment-initiation-service

# Check database connectivity
docker-compose exec postgres psql -U payments_user -d payment_initiation -c "SELECT 1;"

# Test Redis connectivity
docker-compose exec redis redis-cli ping

# Check Kafka topics
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

## Development

### Local Setup
```bash
# Start dependencies
docker-compose up -d postgres redis kafka

# Run the service
mvn spring-boot:run -pl payment-initiation-service
```

### Testing
```bash
# Run unit tests
mvn test -pl payment-initiation-service

# Run integration tests
mvn test -pl payment-initiation-service -Dtest=*IntegrationTest

# Run contract tests
mvn test -pl payment-initiation-service -Dtest=*ContractTest
```

## API Examples

### Create Payment
```bash
curl -X POST http://localhost:8081/payment-initiation/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456" \
  -d '{
    "debitAccount": "1234567890",
    "creditAccount": "0987654321",
    "amount": 100.00,
    "currency": "ZAR",
    "reference": "Payment for services",
    "idempotencyKey": "unique-key-123"
  }'
```

### Get Payment Status
```bash
curl -X GET http://localhost:8081/payment-initiation/api/v1/payments/{paymentId} \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456"
```

## Security

- **OAuth2 Integration**: Secure API access with OAuth2 tokens
- **Tenant Isolation**: Complete data isolation between tenants
- **Input Validation**: Comprehensive request validation
- **Audit Logging**: Complete audit trail for all operations
