# Routing Service

## Overview

The Routing Service is responsible for determining the optimal clearing system and routing path for payment transactions. It implements intelligent routing algorithms, rule-based decision making, and comprehensive routing analytics.

## Features

- **Intelligent Routing**: AI-powered routing decisions based on multiple factors
- **Rule-Based Routing**: Configurable routing rules and conditions
- **Clearing System Integration**: Support for multiple clearing systems (CHAPS, BACS, FPS, SWIFT)
- **Routing Analytics**: Comprehensive routing performance metrics
- **Multi-tenant Support**: Tenant-specific routing configurations
- **Caching**: Redis-based routing decision caching

## API Endpoints

### Health Check
```
GET /routing-service/actuator/health
```

### Routing Operations
```
POST /routing-service/api/v1/routing/decide
GET  /routing-service/api/v1/routing/decisions/{decisionId}
GET  /routing-service/api/v1/routing/decisions
GET  /routing-service/api/v1/routing/analytics
```

### Rule Management
```
GET  /routing-service/api/v1/rules
POST /routing-service/api/v1/rules
PUT  /routing-service/api/v1/rules/{ruleId}
DELETE /routing-service/api/v1/rules/{ruleId}
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |
| `SPRING_DATASOURCE_URL` | Database connection URL | `jdbc:postgresql://postgres:5432/routing` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `payments_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `payments_password` |
| `SPRING_REDIS_HOST` | Redis host | `redis` |
| `SPRING_REDIS_PORT` | Redis port | `6379` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | `kafka:29092` |

### Routing Configuration

```yaml
routing:
  service:
    cache:
      ttl: 3600 # 1 hour
      max-size: 10000
    algorithms:
      - name: "cost-optimization"
        weight: 0.4
      - name: "speed-optimization"
        weight: 0.3
      - name: "reliability-optimization"
        weight: 0.3
  clearing-systems:
    - name: "CHAPS"
      enabled: true
      cost: 0.50
      speed: "fast"
      reliability: 0.99
    - name: "BACS"
      enabled: true
      cost: 0.10
      speed: "slow"
      reliability: 0.95
    - name: "FPS"
      enabled: true
      cost: 0.25
      speed: "instant"
      reliability: 0.98
```

## Dependencies

### External Services
- **PostgreSQL**: Routing rules and decision persistence
- **Redis**: Routing decision caching
- **Kafka**: Event consumption and publishing
- **Clearing Systems**: External clearing system integration

### Internal Dependencies
- **Domain Models**: Routing entities and value objects
- **Contracts**: API contracts and DTOs
- **Shared Config**: Common configuration properties
- **Shared Telemetry**: Observability and monitoring

## Health Checks

The service exposes comprehensive health checks:

- **Database Health**: PostgreSQL connection and query validation
- **Redis Health**: Cache connectivity and operations
- **Routing Engine Health**: All routing algorithms operational status
- **Clearing System Health**: External clearing system connectivity

## Routing Algorithms

### Cost Optimization
- **Factor**: Transaction cost minimization
- **Weight**: 40% of total decision
- **Metrics**: Clearing fees, processing costs, currency conversion

### Speed Optimization
- **Factor**: Transaction speed maximization
- **Weight**: 30% of total decision
- **Metrics**: Processing time, settlement time, cut-off times

### Reliability Optimization
- **Factor**: Transaction success rate maximization
- **Weight**: 30% of total decision
- **Metrics**: Success rates, error rates, system availability

## Clearing Systems

### CHAPS (Clearing House Automated Payment System)
- **Speed**: Fast (same-day settlement)
- **Cost**: High
- **Reliability**: Very High
- **Use Case**: High-value, time-critical payments

### BACS (Bankers' Automated Clearing Services)
- **Speed**: Slow (3-day settlement)
- **Cost**: Low
- **Reliability**: High
- **Use Case**: Bulk payments, payroll

### FPS (Faster Payments Service)
- **Speed**: Instant
- **Cost**: Medium
- **Reliability**: High
- **Use Case**: Real-time payments, retail transactions

### SWIFT
- **Speed**: Variable (1-3 days)
- **Cost**: High
- **Reliability**: Very High
- **Use Case**: International payments, high-value transactions

## Monitoring

### Metrics
- Routing decision accuracy
- Clearing system performance
- Cost optimization effectiveness
- Speed optimization results
- Cache hit/miss ratios

### Logging
- Structured JSON logging
- Routing decision traces
- Algorithm performance metrics
- Clearing system interactions

## Runbook

### Common Issues

#### Service Won't Start
1. Check database connectivity: `curl http://localhost:8084/routing-service/actuator/health`
2. Verify Redis connection: Check logs for Redis connection errors
3. Validate Kafka connectivity: Ensure Kafka is running and accessible

#### Routing Failures
1. Check routing rules: Review rule configuration and status
2. Verify clearing system connectivity: Ensure external systems are accessible
3. Check algorithm configuration: Validate routing algorithm parameters

#### Performance Issues
1. Monitor cache performance: Check Redis cache hit rates
2. Review algorithm execution times: Analyze routing decision performance
3. Check database performance: Monitor query execution times

### Troubleshooting Commands

```bash
# Check service health
curl -f http://localhost:8084/routing-service/actuator/health

# View service logs
docker-compose logs routing-service

# Check routing rules
curl -X GET http://localhost:8084/routing-service/api/v1/rules

# Test routing decision
curl -X POST http://localhost:8084/routing-service/api/v1/routing/decide \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "payment-123",
    "amount": 100.00,
    "currency": "ZAR",
    "debitAccount": "1234567890",
    "creditAccount": "0987654321"
  }'
```

## Development

### Local Setup
```bash
# Start dependencies
docker-compose up -d postgres redis kafka

# Run the service
mvn spring-boot:run -pl routing-service
```

### Testing
```bash
# Run unit tests
mvn test -pl routing-service

# Run integration tests
mvn test -pl routing-service -Dtest=*IntegrationTest

# Run routing algorithm tests
mvn test -pl routing-service -Dtest=*RoutingAlgorithmTest
```

## API Examples

### Get Routing Decision
```bash
curl -X POST http://localhost:8084/routing-service/api/v1/routing/decide \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456" \
  -d '{
    "paymentId": "payment-123",
    "amount": 100.00,
    "currency": "ZAR",
    "debitAccount": "1234567890",
    "creditAccount": "0987654321",
    "paymentType": "TRANSFER",
    "urgency": "NORMAL"
  }'
```

### Get Routing Analytics
```bash
curl -X GET http://localhost:8084/routing-service/api/v1/routing/analytics \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456"
```

### Create Routing Rule
```bash
curl -X POST http://localhost:8084/routing-service/api/v1/rules \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456" \
  -d '{
    "name": "high-value-chaps",
    "condition": "amount > 100000",
    "action": "ROUTE_TO_CHAPS",
    "priority": 1,
    "enabled": true
  }'
```

## Security

- **Rule Access Control**: Tenant-specific routing rule access
- **Audit Logging**: Complete audit trail for routing decisions
- **Data Encryption**: Sensitive routing data encryption
- **Access Control**: Role-based access to routing management

## Performance Optimization

### Caching Strategy
- **Decision Caching**: Cache routing decisions for similar transactions
- **Rule Caching**: Cache routing rules for faster evaluation
- **Clearing System Status**: Cache clearing system availability

### Algorithm Optimization
- **Parallel Processing**: Concurrent algorithm execution
- **Lazy Loading**: Load rules and configurations on demand
- **Batch Processing**: Process multiple routing requests together
