# BankservAfrica Adapter Service

## Overview

The BankservAfrica Adapter Service provides integration with the BankservAfrica clearing network for EFT batch processing, ISO 8583 message handling, and ACH integration. This service enables seamless processing of various payment types through the BankservAfrica network.

## Features

### Core Functionality
- **EFT Batch Processing**: High-volume electronic fund transfers
- **ISO 8583 Message Handling**: Card transaction processing
- **ACH Integration**: Automated Clearing House transactions
- **Multi-tenant Support**: Tenant isolation and business unit management
- **Resilience Patterns**: Circuit breakers, retries, and timeouts
- **Real-time Monitoring**: Comprehensive logging and metrics

### Technical Features
- **Spring Boot 3.2.0**: Modern Java framework
- **PostgreSQL**: Primary database with RLS support
- **Redis Caching**: Performance optimization
- **Resilience4j**: Fault tolerance patterns
- **OpenFeign**: External API integration
- **OpenAPI Documentation**: Swagger UI integration
- **Actuator**: Health checks and metrics

## API Endpoints

### Adapter Management
- `POST /api/v1/bankservafrica-adapters` - Create adapter
- `PUT /api/v1/bankservafrica-adapters/{id}/configuration` - Update configuration
- `POST /api/v1/bankservafrica-adapters/{id}/activate` - Activate adapter
- `POST /api/v1/bankservafrica-adapters/{id}/deactivate` - Deactivate adapter
- `GET /api/v1/bankservafrica-adapters/{id}` - Get adapter by ID
- `GET /api/v1/bankservafrica-adapters/tenant/{tenantId}` - Get adapters by tenant
- `GET /api/v1/bankservafrica-adapters/tenant/{tenantId}/active` - Get active adapters

### EFT Processing
- `POST /api/v1/bankservafrica-eft/process` - Process EFT batch
- `PUT /api/v1/bankservafrica-eft/{id}/status` - Update message status
- `GET /api/v1/bankservafrica-eft/{id}` - Get EFT message
- `GET /api/v1/bankservafrica-eft/batch/{batchId}` - Get messages by batch
- `GET /api/v1/bankservafrica-eft/status/{status}` - Get messages by status

### ISO 8583 Processing
- `POST /api/v1/bankservafrica-iso8583/process` - Process ISO 8583 message
- `PUT /api/v1/bankservafrica-iso8583/{id}/status` - Update message status
- `GET /api/v1/bankservafrica-iso8583/{id}` - Get ISO 8583 message
- `GET /api/v1/bankservafrica-iso8583/transaction/{transactionId}` - Get by transaction ID

### ACH Processing
- `POST /api/v1/bankservafrica-ach/process` - Process ACH transaction
- `PUT /api/v1/bankservafrica-ach/{id}/status` - Update transaction status
- `GET /api/v1/bankservafrica-ach/{id}` - Get ACH transaction
- `GET /api/v1/bankservafrica-ach/batch/{batchId}` - Get transactions by batch

## Configuration

### Application Properties
```yaml
server:
  port: 8083
  servlet:
    context-path: /bankservafrica-adapter

spring:
  application:
    name: bankservafrica-adapter-service
  
  datasource:
    url: jdbc:postgresql://localhost:5432/payments_engine
    username: ${DB_USERNAME:payments_user}
    password: ${DB_PASSWORD:payments_password}

  cache:
    type: redis
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
```

### BankservAfrica Configuration
```yaml
bankservafrica:
  adapter:
    endpoint: ${BANKSERVAFRICA_ENDPOINT:https://api.bankservafrica.co.za}
    api-version: v1
    timeout-seconds: 30
    retry-attempts: 3
    encryption-enabled: true
    batch-size: 1000
    processing-window:
      start-time: "06:00"
      end-time: "18:00"
```

### Resilience4j Configuration
```yaml
resilience4j:
  circuitbreaker:
    instances:
      bankservafrica-service:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 60s
        sliding-window-size: 10
        minimum-number-of-calls: 5
  retry:
    instances:
      bankservafrica-service:
        max-attempts: 3
        wait-duration: 1s
        exponential-backoff-multiplier: 2
  timelimiter:
    instances:
      bankservafrica-service:
        timeout-duration: 30s
```

## Dependencies

### Core Dependencies
- **Spring Boot Starters**: Web, Validation, Data JPA, Actuator
- **Database**: PostgreSQL, Flyway
- **Caching**: Redis
- **Resilience**: Resilience4j
- **API Documentation**: OpenAPI/Swagger
- **External Integration**: OpenFeign

### Domain Dependencies
- **Domain Models**: `domain-models` module
- **Contracts**: `contracts` module
- **Shared Config**: `shared-config` module
- **Shared Telemetry**: `shared-telemetry` module

## Health Checks

### Actuator Endpoints
- `/actuator/health` - Service health status
- `/actuator/info` - Service information
- `/actuator/metrics` - Performance metrics
- `/actuator/prometheus` - Prometheus metrics
- `/actuator/circuitbreakers` - Circuit breaker status
- `/actuator/retries` - Retry statistics

### Health Indicators
- **Database**: PostgreSQL connection
- **Cache**: Redis connection
- **External Services**: BankservAfrica API connectivity
- **Circuit Breakers**: Service availability

## Monitoring

### Metrics
- **Request Count**: API endpoint usage
- **Response Time**: Performance monitoring
- **Error Rate**: Failure tracking
- **Circuit Breaker Status**: Service availability
- **Cache Hit Rate**: Performance optimization

### Logging
- **Structured Logging**: JSON format
- **Correlation IDs**: Request tracing
- **Performance Logging**: Response times
- **Error Logging**: Exception tracking

## Database Schema

### Core Tables
- `bankservafrica_adapters` - Adapter configurations
- `bankservafrica_eft_messages` - EFT batch messages
- `bankservafrica_iso8583_messages` - ISO 8583 messages
- `bankservafrica_ach_transactions` - ACH transactions
- `bankservafrica_transaction_logs` - Operation audit trail
- `bankservafrica_settlement_records` - Settlement tracking

### Security Features
- **Row Level Security (RLS)**: Tenant isolation
- **Audit Triggers**: Automatic timestamp updates
- **Performance Indexes**: Query optimization

## Resilience Patterns

### Circuit Breaker
- **Failure Threshold**: 50% failure rate
- **Wait Duration**: 60 seconds in open state
- **Sliding Window**: 10 requests
- **Minimum Calls**: 5 requests before opening

### Retry Logic
- **Max Attempts**: 3 retries
- **Wait Duration**: 1 second base
- **Exponential Backoff**: 2x multiplier
- **Retry Exceptions**: Network and timeout errors

### Timeout Management
- **BankservAfrica Service**: 30 seconds
- **ISO 8583 Service**: 15 seconds
- **ACH Service**: 20 seconds

## Runbook

### Starting the Service
```bash
# Using Maven
mvn spring-boot:run

# Using JAR
java -jar bankservafrica-adapter-service.jar

# Using Docker
docker run -p 8083:8083 bankservafrica-adapter-service
```

### Health Check
```bash
curl http://localhost:8083/bankservafrica-adapter/actuator/health
```

### API Documentation
```bash
# Swagger UI
http://localhost:8083/bankservafrica-adapter/swagger-ui.html

# OpenAPI JSON
http://localhost:8083/bankservafrica-adapter/v3/api-docs
```

### Common Operations

#### Create Adapter
```bash
curl -X POST http://localhost:8083/bankservafrica-adapter/api/v1/bankservafrica-adapters \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant-001",
    "businessUnitId": "bu-001",
    "adapterName": "BankservAfrica-Prod",
    "network": "BANKSERVAFRICA",
    "endpoint": "https://api.bankservafrica.co.za",
    "createdBy": "admin"
  }'
```

#### Process EFT Batch
```bash
curl -X POST http://localhost:8083/bankservafrica-adapter/api/v1/bankservafrica-eft/process \
  -H "Content-Type: application/json" \
  -d '{
    "adapterId": "adapter-uuid",
    "batchId": "batch-001",
    "messageType": "EFT_BATCH",
    "direction": "OUTBOUND",
    "payload": "EFT batch data"
  }'
```

### Troubleshooting

#### Common Issues
1. **Database Connection**: Check PostgreSQL connectivity
2. **Redis Connection**: Verify Redis server status
3. **External API**: Check BankservAfrica endpoint availability
4. **Circuit Breaker**: Monitor service health

#### Log Analysis
```bash
# Check service logs
tail -f logs/bankservafrica-adapter-service.log

# Filter error logs
grep "ERROR" logs/bankservafrica-adapter-service.log

# Check performance
grep "processing time" logs/bankservafrica-adapter-service.log
```

## Performance

### Expected Performance
- **Throughput**: 10,000+ EFT messages per minute
- **Latency**: < 5 seconds for EFT processing
- **Availability**: 99.9% uptime
- **Batch Size**: 1,000 messages per batch

### Optimization
- **Caching**: Redis for frequently accessed data
- **Connection Pooling**: HikariCP for database connections
- **Async Processing**: Non-blocking operations
- **Batch Processing**: Efficient bulk operations

## Security

### Authentication
- **OAuth2**: Token-based authentication
- **mTLS**: Mutual TLS for external communication
- **API Keys**: Service-to-service authentication

### Data Protection
- **Encryption**: End-to-end encryption
- **Audit Logging**: Comprehensive audit trail
- **Tenant Isolation**: Row-level security
- **Data Masking**: Sensitive data protection

## Integration

### External Systems
- **BankservAfrica API**: Primary clearing network
- **ISO 8583 Gateway**: Card transaction processing
- **ACH Network**: Automated clearing house
- **Monitoring**: Prometheus, Grafana

### Internal Services
- **Payment Initiation**: Transaction origination
- **Validation Service**: Payment validation
- **Saga Orchestrator**: Distributed transactions
- **Audit Service**: Compliance tracking

## Development

### Local Development
```bash
# Start dependencies
docker-compose up -d postgres redis

# Run tests
mvn test

# Start service
mvn spring-boot:run
```

### Testing
```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Load testing
mvn gatling:test
```

## Deployment

### Docker
```dockerfile
FROM openjdk:17-jre-slim
COPY target/bankservafrica-adapter-service.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bankservafrica-adapter-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: bankservafrica-adapter-service
  template:
    metadata:
      labels:
        app: bankservafrica-adapter-service
    spec:
      containers:
      - name: bankservafrica-adapter-service
        image: bankservafrica-adapter-service:latest
        ports:
        - containerPort: 8083
        env:
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
```

## Support

### Documentation
- **API Documentation**: Swagger UI
- **Architecture**: System design documents
- **Runbook**: Operational procedures
- **Troubleshooting**: Common issues and solutions

### Monitoring
- **Health Checks**: Service availability
- **Metrics**: Performance monitoring
- **Alerts**: Automated notifications
- **Dashboards**: Real-time status

### Contact
- **Development Team**: payments-dev@company.com
- **Operations Team**: payments-ops@company.com
- **Support**: payments-support@company.com
