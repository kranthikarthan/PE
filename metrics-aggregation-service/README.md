# Metrics Aggregation Service

## Overview

The Metrics Aggregation Service provides real-time metrics collection, aggregation, and alert management for the Payments Engine. This service enables comprehensive monitoring, alerting, and analytics across all 22 microservices.

**Service ID**: #22  
**Port**: 8022  
**Database**: TimescaleDB (PostgreSQL extension) + Redis (cache)  

## Features

### 📊 Real-Time Metrics Collection
- Automatic metrics collection from all 22 microservices
- Prometheus metrics scraping and parsing
- Time-series data storage with TimescaleDB
- Real-time metrics aggregation and caching

### 🚨 Advanced Alert Management
- Configurable alert rules with complex conditions
- Multi-channel notifications (email, Slack, webhooks)
- Alert lifecycle management (trigger, acknowledge, resolve)
- Severity-based alerting (LOW, MEDIUM, HIGH, CRITICAL)

### 📈 Analytics & Dashboards
- Real-time dashboard data aggregation
- Time-series data for charts and visualizations
- Metrics summaries and statistics
- Performance analytics and reporting

### ⚡ High-Performance Architecture
- Reactive programming with WebFlux
- TimescaleDB for time-series optimization
- Redis caching for fast data access
- Continuous aggregates for efficient queries

## API Endpoints

### Metrics Collection
- `GET /api/metrics/v1/services/{service}/metrics` - Get service metrics
- `GET /api/metrics/v1/services/{service}/summary` - Get metrics summary
- `GET /api/metrics/v1/services/{service}/timeseries` - Get time series data
- `GET /api/metrics/v1/dashboard` - Get dashboard data

### Alert Management
- `GET /api/metrics/v1/alert-rules` - Get all alert rules
- `POST /api/metrics/v1/alert-rules` - Create alert rule
- `PUT /api/metrics/v1/alert-rules/{ruleId}` - Update alert rule
- `GET /api/metrics/v1/alerts` - Get active alerts
- `POST /api/metrics/v1/alerts/{alertId}/acknowledge` - Acknowledge alert
- `POST /api/metrics/v1/alerts/{alertId}/resolve` - Resolve alert

### Collection Management
- `GET /api/metrics/v1/collection/status` - Get collection status
- `POST /api/metrics/v1/collection/trigger` - Trigger collection

## Security & RBAC

### Roles
- **PLATFORM_ADMIN**: Full access to all metrics and alert management
- **OPS_ADMIN**: Alert rule management, collection control
- **OPS_OPERATOR**: Alert acknowledgment and resolution
- **OPS_VIEWER**: Read-only access to metrics and alerts

### Authentication
- OAuth2 JWT token authentication
- Role-based access control (RBAC)
- Audit logging for all alert management actions

## Technology Stack

- **Framework**: Spring Boot 3.2.0 + WebFlux
- **Language**: Java 17
- **Database**: TimescaleDB (PostgreSQL extension) + Redis
- **Metrics**: Prometheus + Micrometer
- **Reactive**: WebFlux + Reactor
- **Security**: Spring Security + OAuth2

## Configuration

### Environment Variables
```bash
# Database
DB_USERNAME=payments
DB_PASSWORD=payments
DB_URL=jdbc:postgresql://localhost:5432/payments_metrics

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Notifications
SMTP_HOST=localhost
SMTP_PORT=587
SMTP_USERNAME=
SMTP_PASSWORD=
SLACK_WEBHOOK_URL=
WEBHOOK_URL=
```

### Application Properties
```yaml
server:
  port: 8022

app:
  metrics:
    collection:
      enabled: true
      interval: 30s
      services:
        - payment-initiation-service
        - validation-service
        # ... all 22 services
  alerts:
    evaluation:
      enabled: true
      interval: 30s
```

## Database Schema

### Tables
- `metrics_data` - Time-series metrics data (TimescaleDB hypertable)
- `alert_rules` - Alert rule definitions
- `alert_events` - Alert event history

### Key Features
- TimescaleDB hypertables for time-series optimization
- Continuous aggregates for efficient queries
- Data retention policies (30 days)
- Compression policies for historical data
- Comprehensive indexing for performance

### TimescaleDB Features
- Automatic partitioning by time
- Continuous aggregates for real-time summaries
- Data compression for historical data
- Retention policies for data lifecycle management

## Development

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 13+ with TimescaleDB extension
- Redis 6+

### Running Locally
```bash
# Start dependencies
docker-compose up -d postgres redis

# Run the service
mvn spring-boot:run

# Or with Docker
docker build -t metrics-aggregation-service .
docker run -p 8022:8022 metrics-aggregation-service
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
- Metrics collection rate and success
- Alert evaluation performance
- Database query performance
- Cache hit rates
- API response times

## Deployment

### Docker
```dockerfile
FROM openjdk:17-jre-slim
COPY target/metrics-aggregation-service-*.jar app.jar
EXPOSE 8022
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: metrics-aggregation-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: metrics-aggregation-service
  template:
    metadata:
      labels:
        app: metrics-aggregation-service
    spec:
      containers:
      - name: metrics-aggregation
        image: metrics-aggregation-service:latest
        ports:
        - containerPort: 8022
        env:
        - name: DB_URL
          value: "jdbc:postgresql://postgres:5432/payments_metrics"
        - name: REDIS_HOST
          value: "redis"
```

## Alert Rules Examples

### High Error Rate Alert
```yaml
ruleName: "High Error Rate"
serviceName: "payment-initiation-service"
metricName: "http_server_requests_error_rate"
conditionType: "GREATER_THAN"
thresholdValue: 0.05
evaluationWindowSeconds: 300
severity: "HIGH"
notificationChannels: ["email", "slack"]
```

### High Response Time Alert
```yaml
ruleName: "High Response Time"
serviceName: "payment-initiation-service"
metricName: "http_server_requests_duration_seconds"
conditionType: "GREATER_THAN"
thresholdValue: 1.0
evaluationWindowSeconds: 300
severity: "MEDIUM"
notificationChannels: ["email"]
```

### Low Throughput Alert
```yaml
ruleName: "Low Throughput"
serviceName: "payment-initiation-service"
metricName: "http_server_requests_total"
conditionType: "LESS_THAN"
thresholdValue: 10.0
evaluationWindowSeconds: 600
severity: "LOW"
notificationChannels: ["slack"]
```

## Troubleshooting

### Common Issues

1. **TimescaleDB Extension Not Available**
   - Install TimescaleDB extension
   - Verify PostgreSQL version compatibility
   - Check extension permissions

2. **Metrics Collection Failed**
   - Check service connectivity
   - Verify Prometheus endpoints
   - Review network configuration

3. **Alert Rules Not Triggering**
   - Verify metric names and thresholds
   - Check evaluation window settings
   - Review notification channel configuration

### Logs
```bash
# View service logs
kubectl logs -f deployment/metrics-aggregation-service

# Check specific pod logs
kubectl logs -f pod/metrics-aggregation-service-xxx
```

## Performance Optimization

### TimescaleDB Tuning
- Chunk time interval: 1 hour
- Compression policy: 1 day
- Retention policy: 30 days
- Continuous aggregates for common queries

### Redis Caching
- Cache frequently accessed metrics
- TTL-based cache expiration
- Connection pooling for performance

### Reactive Programming
- Non-blocking I/O for metrics collection
- Backpressure handling for high-volume data
- Efficient memory usage with streaming

## Contributing

1. Follow the established code patterns
2. Add comprehensive tests for new features
3. Update documentation for API changes
4. Ensure performance considerations
5. Follow the alert management best practices

## License

This service is part of the Payments Engine project and follows the same licensing terms.
