# Operations Management Service

## Overview

The Operations Management Service provides comprehensive operations management capabilities for the Payments Engine. This service enables operations teams to monitor, manage, and control all aspects of the payments platform.

**Service ID**: #21  
**Port**: 8021  
**Database**: PostgreSQL + Redis (cache)  

## Features

### 🔍 Service Health Monitoring
- Real-time health status for all 22 microservices
- Service metrics (TPS, error rate, response times)
- Pod-level monitoring and resource usage
- Circuit breaker state monitoring

### ⚡ Circuit Breaker Management
- View circuit breaker states across all services
- Manual circuit breaker control (open/close)
- Circuit breaker metrics and failure rates
- Automatic state transitions

### 🚩 Feature Flag Management
- Integration with Unleash feature flag service
- Toggle feature flags in real-time
- Gradual rollout management
- Feature flag analytics and usage tracking

### 🐳 Kubernetes Integration
- Pod management and monitoring
- Service restart capabilities
- Resource usage tracking
- Node-level information

### 📊 Audit & Compliance
- Complete audit trail for all operations actions
- User action tracking
- Compliance reporting
- Security event logging

## API Endpoints

### Service Health
- `GET /api/ops/v1/services` - Get all service health
- `GET /api/ops/v1/services/{service}` - Get specific service health
- `GET /api/ops/v1/services/{service}/metrics` - Get service metrics
- `GET /api/ops/v1/services/{service}/errors` - Get service errors
- `POST /api/ops/v1/services/{service}/restart` - Restart service

### Circuit Breakers
- `GET /api/ops/v1/circuit-breakers` - Get all circuit breakers
- `GET /api/ops/v1/circuit-breakers/{service}` - Get circuit breaker for service
- `POST /api/ops/v1/circuit-breakers/{service}/open` - Open circuit breaker
- `POST /api/ops/v1/circuit-breakers/{service}/close` - Close circuit breaker

### Feature Flags
- `GET /api/ops/v1/feature-flags` - Get all feature flags
- `PUT /api/ops/v1/feature-flags/{flag}/toggle` - Toggle feature flag
- `PUT /api/ops/v1/feature-flags/{flag}/rollout` - Set rollout percentage

## Security & RBAC

### Roles
- **PLATFORM_ADMIN**: Full access to all operations features
- **OPS_ADMIN**: Service management, circuit breaker control, feature flag management
- **OPS_OPERATOR**: Read-only access to monitoring data
- **OPS_VIEWER**: Read-only access to all sections

### Authentication
- OAuth2 JWT token authentication
- Role-based access control (RBAC)
- Audit logging for all operations actions

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL + Redis
- **Kubernetes**: Kubernetes Java Client
- **Feature Flags**: Unleash
- **Circuit Breakers**: Resilience4j
- **Security**: Spring Security + OAuth2

## Configuration

### Environment Variables
```bash
# Database
DB_USERNAME=payments
DB_PASSWORD=payments
DB_URL=jdbc:postgresql://localhost:5432/payments_operations

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Kubernetes
K8S_NAMESPACE=payments
K8S_CONFIG_PATH=

# Unleash
UNLEASH_API_URL=http://unleash:4242/api
UNLEASH_APP_NAME=operations-management-service
UNLEASH_INSTANCE_ID=operations-management-001
UNLEASH_API_TOKEN=
```

### Application Properties
```yaml
server:
  port: 8021

app:
  kubernetes:
    namespace: payments
  services:
    actuator-port: 8080
  unleash:
    api-url: http://unleash:4242/api
    app-name: operations-management-service
```

## Database Schema

### Tables
- `service_health` - Service health status and metrics
- `pod_info` - Kubernetes pod information
- `operations_audit_log` - Audit trail for all operations

### Key Features
- Automatic timestamp updates
- JSONB support for flexible data storage
- Comprehensive indexing for performance
- Foreign key relationships

## Development

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 13+
- Redis 6+
- Kubernetes cluster access

### Running Locally
```bash
# Start dependencies
docker-compose up -d postgres redis

# Run the service
mvn spring-boot:run

# Or with Docker
docker build -t operations-management-service .
docker run -p 8021:8021 operations-management-service
```

### Testing
```bash
# Run all tests
mvn test

# Run with coverage
mvn jacoco:report

# Integration tests
mvn test -Dtest=*IntegrationTest
```

## Monitoring & Observability

### Health Checks
- `/actuator/health` - Service health status
- `/actuator/info` - Service information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

### Key Metrics
- Service health aggregation time
- Circuit breaker state changes
- Feature flag toggle operations
- Pod restart operations
- API response times

## Deployment

### Docker
```dockerfile
FROM openjdk:17-jre-slim
COPY target/operations-management-service-*.jar app.jar
EXPOSE 8021
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: operations-management-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: operations-management-service
  template:
    metadata:
      labels:
        app: operations-management-service
    spec:
      containers:
      - name: operations-management
        image: operations-management-service:latest
        ports:
        - containerPort: 8021
        env:
        - name: DB_URL
          value: "jdbc:postgresql://postgres:5432/payments_operations"
        - name: REDIS_HOST
          value: "redis"
```

## Troubleshooting

### Common Issues

1. **Kubernetes API Connection Failed**
   - Check Kubernetes configuration
   - Verify service account permissions
   - Ensure cluster connectivity

2. **Feature Flag Service Unavailable**
   - Check Unleash service status
   - Verify API URL and authentication
   - Review network connectivity

3. **Database Connection Issues**
   - Verify PostgreSQL connection
   - Check database credentials
   - Ensure database exists

### Logs
```bash
# View service logs
kubectl logs -f deployment/operations-management-service

# Check specific pod logs
kubectl logs -f pod/operations-management-service-xxx
```

## Contributing

1. Follow the established code patterns
2. Add comprehensive tests for new features
3. Update documentation for API changes
4. Ensure security best practices
5. Follow the audit logging requirements

## License

This service is part of the Payments Engine project and follows the same licensing terms.
