# Account Adapter Service

## Overview

The Account Adapter Service provides integration with external core banking systems and account management services. It implements resilience patterns, OAuth2 authentication, and comprehensive error handling for reliable account operations.

## Features

- **Core Banking Integration**: Seamless integration with external banking systems
- **OAuth2 Authentication**: Secure token-based authentication
- **Resilience Patterns**: Circuit breaker, retry, and timeout mechanisms
- **Account Validation**: Real-time account balance and status validation
- **Caching**: Redis-based caching for improved performance
- **Multi-tenant Support**: Tenant-specific account access

## API Endpoints

### Health Check
```
GET /account-adapter-service/actuator/health
```

### Account Operations
```
GET  /account-adapter/api/v1/accounts/{accountId}
GET  /account-adapter/api/v1/accounts/{accountId}/balance
POST /account-adapter/api/v1/accounts/validate
GET  /account-adapter/api/v1/accounts/{accountId}/transactions
```

### Token Management
```
POST /account-adapter/api/v1/auth/token
GET  /account-adapter/api/v1/auth/token/refresh
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |
| `SPRING_DATASOURCE_URL` | Database connection URL | `jdbc:postgresql://postgres:5432/account_adapter` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `payments_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `payments_password` |
| `SPRING_REDIS_HOST` | Redis host | `redis` |
| `SPRING_REDIS_PORT` | Redis port | `6379` |
| `ACCOUNT_SERVICE_URL` | External account service URL | `http://localhost:8080` |
| `ACCOUNT_SERVICE_OAUTH_TOKEN_URI` | OAuth2 token endpoint | `http://localhost:8080/oauth/token` |

### Resilience Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      account-service:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 10
  retry:
    instances:
      account-service:
        max-attempts: 3
        wait-duration: 1s
  timelimiter:
    instances:
      account-service:
        timeout-duration: 5s
```

## Dependencies

### External Services
- **PostgreSQL**: Token and cache persistence
- **Redis**: OAuth2 token caching
- **External Account Service**: Core banking system integration
- **OAuth2 Provider**: Authentication and authorization

### Internal Dependencies
- **Domain Models**: Account entities and value objects
- **Contracts**: API contracts and DTOs
- **Shared Config**: Common configuration properties
- **Shared Telemetry**: Observability and monitoring

## Health Checks

The service exposes comprehensive health checks:

- **Database Health**: PostgreSQL connection and query validation
- **Redis Health**: Cache connectivity and operations
- **External Service Health**: Core banking system connectivity
- **OAuth2 Health**: Token service availability
- **Circuit Breaker Status**: Resilience pattern health

## Resilience Patterns

### Circuit Breaker
- **Failure Rate Threshold**: 50% failure rate triggers circuit open
- **Wait Duration**: 30 seconds in open state before attempting reset
- **Sliding Window**: 10 requests for failure rate calculation

### Retry Mechanism
- **Max Attempts**: 3 retry attempts for failed requests
- **Wait Duration**: 1 second between retry attempts
- **Exponential Backoff**: Increasing delay between retries

### Timeout Configuration
- **Request Timeout**: 5 seconds for external service calls
- **Connection Timeout**: 2 seconds for connection establishment
- **Read Timeout**: 3 seconds for response reading

## Monitoring

### Metrics
- External service response times
- Circuit breaker state changes
- Retry attempt counts
- Cache hit/miss ratios
- OAuth2 token refresh rates

### Logging
- Structured JSON logging
- Request/response correlation
- Circuit breaker state changes
- OAuth2 token lifecycle events

## Runbook

### Common Issues

#### Service Won't Start
1. Check database connectivity: `curl http://localhost:8083/account-adapter-service/actuator/health`
2. Verify Redis connection: Check logs for Redis connection errors
3. Validate external service connectivity: Ensure core banking system is accessible

#### Authentication Failures
1. Check OAuth2 configuration: Verify token endpoint and credentials
2. Validate token cache: Check Redis for token storage issues
3. Review external service logs: Check for authentication errors

#### Circuit Breaker Issues
1. Monitor circuit breaker state: Check health endpoint for circuit status
2. Review external service health: Ensure core banking system is responding
3. Check retry patterns: Analyze retry attempt logs

### Troubleshooting Commands

```bash
# Check service health
curl -f http://localhost:8083/account-adapter-service/actuator/health

# View service logs
docker-compose logs account-adapter-service

# Check circuit breaker status
curl -X GET http://localhost:8083/account-adapter-service/actuator/health | jq '.components.circuitBreakers'

# Test account validation
curl -X POST http://localhost:8083/account-adapter/api/v1/accounts/validate \
  -H "Content-Type: application/json" \
  -d '{"accountId": "1234567890"}'
```

## Development

### Local Setup
```bash
# Start dependencies
docker-compose up -d postgres redis

# Run the service
mvn spring-boot:run -pl account-adapter-service
```

### Testing
```bash
# Run unit tests
mvn test -pl account-adapter-service

# Run integration tests
mvn test -pl account-adapter-service -Dtest=*IntegrationTest

# Run circuit breaker tests
mvn test -pl account-adapter-service -Dtest=*CircuitBreakerTest
```

## API Examples

### Get Account Balance
```bash
curl -X GET http://localhost:8083/account-adapter/api/v1/accounts/{accountId}/balance \
  -H "Authorization: Bearer {token}" \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456"
```

### Validate Account
```bash
curl -X POST http://localhost:8083/account-adapter/api/v1/accounts/validate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456" \
  -d '{
    "accountId": "1234567890",
    "accountType": "CHECKING"
  }'
```

### Get OAuth2 Token
```bash
curl -X POST http://localhost:8083/account-adapter/api/v1/auth/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id={client_id}&client_secret={client_secret}"
```

## Security

- **OAuth2 Integration**: Secure token-based authentication
- **Token Caching**: Secure token storage and refresh
- **TLS Encryption**: All external communications encrypted
- **Audit Logging**: Complete audit trail for account operations
- **Access Control**: Role-based access to account operations

## Error Handling

### Circuit Breaker States
- **CLOSED**: Normal operation, requests pass through
- **OPEN**: Circuit is open, requests fail fast
- **HALF_OPEN**: Testing if external service has recovered

### Retry Strategies
- **Exponential Backoff**: Increasing delays between retries
- **Jitter**: Random variation to prevent thundering herd
- **Max Attempts**: Configurable retry limits

### Error Mapping
- **Timeout Errors**: Mapped to service unavailable
- **Authentication Errors**: Mapped to unauthorized access
- **Network Errors**: Mapped to service unavailable
- **Business Logic Errors**: Passed through to client
