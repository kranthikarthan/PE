# Saga Orchestrator Service

## Overview

The Saga Orchestrator Service manages distributed transaction coordination using the Saga pattern. It ensures data consistency across multiple services by orchestrating compensating transactions and handling failure scenarios gracefully.

## Features

- **Saga Orchestration**: Coordinate distributed transactions across services
- **Compensation Logic**: Handle transaction rollbacks and compensations
- **Event-Driven**: Event-based saga execution and coordination
- **Failure Recovery**: Automatic failure detection and recovery
- **Multi-tenant Support**: Tenant-specific saga isolation
- **Audit Trail**: Complete saga execution history

## API Endpoints

### Health Check
```
GET /saga-orchestrator/actuator/health
```

### Saga Management
```
POST /saga-orchestrator/api/v1/sagas
GET  /saga-orchestrator/api/v1/sagas/{sagaId}
GET  /saga-orchestrator/api/v1/sagas
PUT  /saga-orchestrator/api/v1/sagas/{sagaId}/compensate
```

### Saga Steps
```
GET  /saga-orchestrator/api/v1/sagas/{sagaId}/steps
POST /saga-orchestrator/api/v1/sagas/{sagaId}/steps/{stepId}/execute
POST /saga-orchestrator/api/v1/sagas/{sagaId}/steps/{stepId}/compensate
```

### Saga Events
```
GET  /saga-orchestrator/api/v1/sagas/{sagaId}/events
GET  /saga-orchestrator/api/v1/events
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |
| `SPRING_DATASOURCE_URL` | Database connection URL | `jdbc:postgresql://postgres:5432/saga_orchestrator` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `payments_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `payments_password` |
| `SPRING_REDIS_HOST` | Redis host | `redis` |
| `SPRING_REDIS_PORT` | Redis port | `6379` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | `kafka:29092` |

### Saga Configuration

```yaml
saga:
  orchestrator:
    timeout: 300000 # 5 minutes
    retry-attempts: 3
    retry-delay: 5000 # 5 seconds
  compensation:
    timeout: 60000 # 1 minute
    retry-attempts: 2
    retry-delay: 2000 # 2 seconds
  events:
    enable-event-sourcing: true
    event-retention-days: 90
```

## Dependencies

### External Services
- **PostgreSQL**: Saga state and event persistence
- **Redis**: Saga state caching
- **Kafka**: Event consumption and publishing
- **Payment Services**: Integration with payment processing services

### Internal Dependencies
- **Domain Models**: Saga entities and value objects
- **Contracts**: API contracts and DTOs
- **Shared Config**: Common configuration properties
- **Shared Telemetry**: Observability and monitoring

## Health Checks

The service exposes comprehensive health checks:

- **Database Health**: PostgreSQL connection and query validation
- **Redis Health**: Cache connectivity and operations
- **Kafka Health**: Message processing status
- **Saga Engine Health**: Saga orchestration engine status
- **Event Store Health**: Event sourcing system status

## Saga Patterns

### Choreography Saga
- **Event-Driven**: Services communicate via events
- **Decentralized**: No central coordinator
- **Loose Coupling**: Services are loosely coupled
- **Use Case**: Simple, well-defined workflows

### Orchestration Saga
- **Centralized**: Central orchestrator coordinates
- **Explicit Control**: Explicit step-by-step control
- **Tight Coupling**: Services are tightly coupled
- **Use Case**: Complex, multi-step workflows

## Saga Lifecycle

### Saga States
1. **STARTED**: Saga initialization
2. **RUNNING**: Saga execution in progress
3. **COMPLETED**: All steps completed successfully
4. **FAILED**: Saga execution failed
5. **COMPENSATING**: Compensation in progress
6. **COMPENSATED**: Compensation completed

### Step States
1. **PENDING**: Step waiting to execute
2. **RUNNING**: Step execution in progress
3. **COMPLETED**: Step completed successfully
4. **FAILED**: Step execution failed
5. **COMPENSATING**: Step compensation in progress
6. **COMPENSATED**: Step compensation completed

## Event Handling

### Event Types
- **SagaStartedEvent**: Saga initialization
- **SagaStepStartedEvent**: Step execution started
- **SagaStepCompletedEvent**: Step completed successfully
- **SagaStepFailedEvent**: Step execution failed
- **SagaCompletedEvent**: Saga completed successfully
- **SagaFailedEvent**: Saga execution failed
- **SagaCompensationStartedEvent**: Compensation started
- **SagaCompensatedEvent**: Compensation completed

### Event Consumers
- **PaymentInitiatedEventConsumer**: Handle payment initiation
- **PaymentValidatedEventConsumer**: Handle payment validation
- **PaymentRoutedEventConsumer**: Handle payment routing
- **TransactionCreatedEventConsumer**: Handle transaction creation

## Monitoring

### Metrics
- Saga execution success rates
- Step completion times
- Compensation success rates
- Event processing performance
- Cache hit/miss ratios

### Logging
- Structured JSON logging
- Saga execution traces
- Step performance metrics
- Event processing logs

## Runbook

### Common Issues

#### Service Won't Start
1. Check database connectivity: `curl http://localhost:8086/saga-orchestrator/actuator/health`
2. Verify Redis connection: Check logs for Redis connection errors
3. Validate Kafka connectivity: Ensure Kafka is running and accessible

#### Saga Failures
1. Check saga state: Review saga execution logs
2. Verify step dependencies: Ensure all required services are available
3. Check compensation logic: Validate compensation step implementations

#### Event Processing Issues
1. Monitor event consumers: Check for event processing failures
2. Review event store: Validate event sequence and integrity
3. Check Kafka connectivity: Ensure event producers are working

### Troubleshooting Commands

```bash
# Check service health
curl -f http://localhost:8086/saga-orchestrator/actuator/health

# View service logs
docker-compose logs saga-orchestrator

# Check saga status
curl -X GET http://localhost:8086/saga-orchestrator/api/v1/sagas/{sagaId}

# Get saga events
curl -X GET http://localhost:8086/saga-orchestrator/api/v1/sagas/{sagaId}/events
```

## Development

### Local Setup
```bash
# Start dependencies
docker-compose up -d postgres redis kafka

# Run the service
mvn spring-boot:run -pl saga-orchestrator
```

### Testing
```bash
# Run unit tests
mvn test -pl saga-orchestrator

# Run integration tests
mvn test -pl saga-orchestrator -Dtest=*IntegrationTest

# Run saga tests
mvn test -pl saga-orchestrator -Dtest=*SagaTest
```

## API Examples

### Start Saga
```bash
curl -X POST http://localhost:8086/saga-orchestrator/api/v1/sagas \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456" \
  -d '{
    "sagaType": "PAYMENT_PROCESSING",
    "paymentId": "payment-123",
    "amount": 100.00,
    "currency": "ZAR",
    "debitAccount": "1234567890",
    "creditAccount": "0987654321"
  }'
```

### Get Saga Status
```bash
curl -X GET http://localhost:8086/saga-orchestrator/api/v1/sagas/{sagaId} \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456"
```

### Compensate Saga
```bash
curl -X PUT http://localhost:8086/saga-orchestrator/api/v1/sagas/{sagaId}/compensate \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456"
```

### Get Saga Events
```bash
curl -X GET http://localhost:8086/saga-orchestrator/api/v1/sagas/{sagaId}/events \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456"
```

## Security

- **Saga Isolation**: Complete saga isolation between tenants
- **Audit Logging**: Comprehensive audit trail for all saga operations
- **Data Encryption**: Sensitive saga data encryption
- **Access Control**: Role-based access to saga operations

## Performance Optimization

### Caching Strategy
- **Saga State Caching**: Redis-cached saga states
- **Step Caching**: Frequently accessed step data
- **Event Caching**: Recent event data caching

### Database Optimization
- **Indexing**: Optimized database indexes for queries
- **Partitioning**: Event store partitioning by date
- **Connection Pooling**: Optimized database connection management
