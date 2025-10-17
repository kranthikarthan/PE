# Validation Service

## Overview

The Validation Service is responsible for enforcing payment validation rules, compliance checks, and risk assessment. It processes payment requests through multiple rule engines and provides comprehensive validation results.

## Features

- **Business Rules Engine**: Enforce business logic and compliance rules
- **Risk Assessment**: Evaluate payment risk factors and scoring
- **Fraud Detection**: Detect potentially fraudulent transactions
- **Compliance Validation**: Ensure regulatory compliance
- **Multi-tenant Support**: Tenant-specific rule configurations

## API Endpoints

### Health Check
```
GET /validation/actuator/health
```

### Validation
```
POST /validation/api/v1/validate
GET  /validation/api/v1/validation-results/{validationId}
GET  /validation/api/v1/validation-results
```

### Rule Management
```
GET  /validation/api/v1/rules
POST /validation/api/v1/rules
PUT  /validation/api/v1/rules/{ruleId}
DELETE /validation/api/v1/rules/{ruleId}
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |
| `SPRING_DATASOURCE_URL` | Database connection URL | `jdbc:postgresql://postgres:5432/validation` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `payments_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `payments_password` |
| `SPRING_REDIS_HOST` | Redis host | `redis` |
| `SPRING_REDIS_PORT` | Redis port | `6379` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | `kafka:29092` |

### Rule Engine Configuration

```yaml
validation:
  rules:
    business:
      enabled: true
      timeout: 5000ms
    compliance:
      enabled: true
      timeout: 3000ms
    fraud:
      enabled: true
      timeout: 2000ms
    risk:
      enabled: true
      timeout: 1000ms
```

## Dependencies

### External Services
- **PostgreSQL**: Validation results persistence
- **Redis**: Rule caching and session management
- **Kafka**: Event consumption and publishing
- **Account Adapter**: Account validation and balance checks

### Internal Dependencies
- **Domain Models**: Validation entities and value objects
- **Contracts**: API contracts and DTOs
- **Shared Config**: Common configuration properties
- **Shared Telemetry**: Observability and monitoring

## Health Checks

The service exposes comprehensive health checks:

- **Database Health**: PostgreSQL connection and query validation
- **Redis Health**: Cache connectivity and operations
- **Rule Engine Health**: All rule engines operational status
- **Kafka Health**: Message processing status

## Rule Engines

### Business Rules Engine
- Payment amount limits
- Account validation
- Business hours validation
- Currency restrictions

### Compliance Rules Engine
- Regulatory compliance checks
- AML (Anti-Money Laundering) validation
- Sanctions screening
- KYC (Know Your Customer) validation

### Fraud Detection Engine
- Transaction pattern analysis
- Velocity checks
- Geographic validation
- Device fingerprinting

### Risk Assessment Engine
- Credit risk evaluation
- Market risk assessment
- Operational risk analysis
- Liquidity risk checks

## Monitoring

### Metrics
- Validation success/failure rates
- Rule execution times
- Risk score distributions
- Fraud detection accuracy
- Cache performance metrics

### Logging
- Structured JSON logging
- Rule execution traces
- Risk assessment details
- Fraud detection alerts

## Runbook

### Common Issues

#### Service Won't Start
1. Check database connectivity: `curl http://localhost:8082/validation/actuator/health`
2. Verify Redis connection: Check logs for Redis connection errors
3. Validate Kafka connectivity: Ensure Kafka is running and accessible

#### Validation Failures
1. Check rule engine status: Review rule engine health indicators
2. Verify rule configurations: Ensure rules are properly loaded
3. Check tenant context: Validate tenant-specific rule access

#### Performance Issues
1. Monitor rule execution times: Check for slow rule evaluations
2. Review cache performance: Monitor rule cache hit rates
3. Analyze database performance: Check for slow queries

### Troubleshooting Commands

```bash
# Check service health
curl -f http://localhost:8082/validation/actuator/health

# View service logs
docker-compose logs validation-service

# Check rule engine status
curl -X GET http://localhost:8082/validation/api/v1/rules

# Test validation endpoint
curl -X POST http://localhost:8082/validation/api/v1/validate \
  -H "Content-Type: application/json" \
  -d '{"paymentId": "test-123", "amount": 100.00}'
```

## Development

### Local Setup
```bash
# Start dependencies
docker-compose up -d postgres redis kafka

# Run the service
mvn spring-boot:run -pl validation-service
```

### Testing
```bash
# Run unit tests
mvn test -pl validation-service

# Run integration tests
mvn test -pl validation-service -Dtest=*IntegrationTest

# Run rule engine tests
mvn test -pl validation-service -Dtest=*RuleEngineTest
```

## API Examples

### Validate Payment
```bash
curl -X POST http://localhost:8082/validation/api/v1/validate \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456" \
  -d '{
    "paymentId": "payment-123",
    "debitAccount": "1234567890",
    "creditAccount": "0987654321",
    "amount": 100.00,
    "currency": "ZAR",
    "reference": "Payment for services"
  }'
```

### Get Validation Results
```bash
curl -X GET http://localhost:8082/validation/api/v1/validation-results/{validationId} \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456"
```

## Rule Configuration

### Business Rules
```yaml
business-rules:
  - name: "amount-limit"
    condition: "amount > 1000000"
    action: "REJECT"
    message: "Amount exceeds maximum limit"
  
  - name: "account-validation"
    condition: "debitAccount != null && creditAccount != null"
    action: "PASS"
    message: "Account validation passed"
```

### Compliance Rules
```yaml
compliance-rules:
  - name: "aml-screening"
    condition: "amount > 50000"
    action: "REVIEW"
    message: "Requires AML review"
  
  - name: "sanctions-check"
    condition: "sanctionsList.contains(creditAccount)"
    action: "REJECT"
    message: "Account on sanctions list"
```

## Security

- **Rule Access Control**: Tenant-specific rule access
- **Audit Logging**: Complete audit trail for rule executions
- **Data Encryption**: Sensitive data encryption at rest and in transit
- **Access Control**: Role-based access to rule management
