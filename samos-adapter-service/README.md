# SAMOS Adapter Service

## Overview

The SAMOS Adapter Service provides integration with the South African Reserve Bank (SARB) SAMOS system for high-value RTGS payments. It supports ISO 20022 messaging format and real-time settlement processing.

## Features

- **High-Value RTGS Payments**: Integration with SARB SAMOS system for real-time gross settlement
- **ISO 20022 Messaging**: Support for pacs.008 (Credit Transfer), pacs.002 (Payment Status Report), and camt.054 (Notification)
- **Resilience Patterns**: Circuit breaker, retry, and timeout mechanisms using Resilience4j
- **Multi-tenant Support**: Full tenant isolation and context management
- **Message Lifecycle Management**: Complete tracking of payment messages from submission to settlement
- **Settlement Tracking**: Real-time settlement record management and confirmation

## API Endpoints

### Health Check
```
GET /samos-adapter/actuator/health
```

### Adapter Management
```
POST   /samos-adapter/api/v1/adapters
GET    /samos-adapter/api/v1/adapters/{adapterId}
GET    /samos-adapter/api/v1/adapters/active
PUT    /samos-adapter/api/v1/adapters/{adapterId}/configuration
POST   /samos-adapter/api/v1/adapters/{adapterId}/activate
POST   /samos-adapter/api/v1/adapters/{adapterId}/deactivate
```

### Payment Processing
```
POST   /samos-adapter/api/v1/payments/submit
POST   /samos-adapter/api/v1/payments/incoming
GET    /samos-adapter/api/v1/payments/{messageId}
GET    /samos-adapter/api/v1/payments/by-payment/{paymentId}
POST   /samos-adapter/api/v1/payments/{messageId}/sent
POST   /samos-adapter/api/v1/payments/{messageId}/received
POST   /samos-adapter/api/v1/payments/{messageId}/failed
```

### Message Management
```
GET    /samos-adapter/api/v1/payments/pending
GET    /samos-adapter/api/v1/payments/failed
GET    /samos-adapter/api/v1/payments/by-status/{status}
GET    /samos-adapter/api/v1/payments/by-type/{messageType}
GET    /samos-adapter/api/v1/payments/by-date-range
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |
| `SPRING_DATASOURCE_URL` | Database connection URL | `jdbc:postgresql://postgres:5432/payments_engine` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `payments_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `payments_password` |
| `SPRING_REDIS_HOST` | Redis host | `redis` |
| `SPRING_REDIS_PORT` | Redis port | `6379` |
| `SAMOS_ENDPOINT` | SAMOS system endpoint | `https://samos.sarb.co.za/rtgs` |
| `SAMOS_CERT_PATH` | Certificate file path | `/etc/certs/samos.p12` |
| `SAMOS_CERT_PASSWORD` | Certificate password | - |

### SAMOS Configuration

```yaml
samos:
  adapter:
    endpoint: ${SAMOS_ENDPOINT:https://samos.sarb.co.za/rtgs}
    api-version: v1
    timeout-seconds: 30
    retry-attempts: 3
    encryption-enabled: true
    certificate-path: ${SAMOS_CERT_PATH:/etc/certs/samos.p12}
    certificate-password: ${SAMOS_CERT_PASSWORD:}
```

### Resilience4j Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      samos-service:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 10
  retry:
    instances:
      samos-service:
        max-attempts: 3
        wait-duration: 1s
        exponential-backoff-multiplier: 2
  timelimiter:
    instances:
      samos-service:
        timeout-duration: 30s
```

## Dependencies

### External Services
- **PostgreSQL**: Primary database for adapter and message persistence
- **Redis**: Caching and session management
- **SAMOS System**: South African Reserve Bank RTGS system
- **Certificate Store**: mTLS certificate management

### Internal Dependencies
- **Domain Models**: Clearing adapter domain entities
- **Contracts**: API contracts and DTOs
- **Shared Config**: Common configuration properties
- **Shared Telemetry**: Observability and monitoring

## Database Schema

### Core Tables
- `samos_adapters`: SAMOS adapter configurations
- `samos_payment_messages`: ISO 20022 payment messages
- `samos_transaction_logs`: Operation logs and audit trail
- `samos_settlement_records`: Settlement confirmations

### Key Features
- **Row Level Security (RLS)**: Tenant isolation at database level
- **Audit Triggers**: Automatic timestamp updates
- **Indexes**: Optimized for performance
- **Foreign Keys**: Referential integrity

## ISO 20022 Message Types

### Supported Messages
- **pacs.008**: FIToFICstmrCdtTrf (Credit Transfer)
- **pacs.002**: FIToFIPmtStsRpt (Payment Status Report)
- **camt.054**: BkToCstmrDbtCdtNtfctn (Bank to Customer Debit/Credit Notification)

### Message Processing
- **Outbound**: Payment submissions to SAMOS
- **Inbound**: Status reports and notifications from SAMOS
- **Validation**: ISO 20022 format validation
- **Hash Generation**: SHA-256 payload integrity checking

## Health Checks

The service exposes comprehensive health checks via Spring Boot Actuator:

- **Database Health**: PostgreSQL connection and query validation
- **Redis Health**: Cache connectivity and operations
- **SAMOS Connectivity**: External system availability
- **Certificate Health**: mTLS certificate validation
- **Circuit Breaker Status**: Resilience pattern health

## Monitoring

### Metrics
- Payment submission rate
- Message processing success/failure rates
- ISO 20022 validation times
- SAMOS response times
- Circuit breaker state changes
- Settlement confirmation rates

### Logging
- Structured JSON logging
- Correlation ID tracking
- Tenant context logging
- ISO 20022 message logging
- Performance metrics

## Runbook

### Common Issues

#### Service Won't Start
1. Check database connectivity: `curl http://localhost:8080/samos-adapter/actuator/health`
2. Verify Redis connection: Check logs for Redis connection errors
3. Validate SAMOS endpoint: Ensure SAMOS system is accessible
4. Check certificate configuration: Verify certificate path and password

#### Payment Submission Failures
1. Check SAMOS connectivity: Review SAMOS system logs
2. Verify certificate configuration: Ensure mTLS certificates are valid
3. Check ISO 20022 message format: Validate message structure
4. Review circuit breaker status: Check if circuit is open

#### Message Processing Issues
1. Check message validation: Review ISO 20022 format validation
2. Verify tenant context: Ensure proper tenant isolation
3. Review message lifecycle: Check message status transitions
4. Analyze settlement records: Verify settlement confirmations

### Troubleshooting Commands

```bash
# Check service health
curl -f http://localhost:8080/samos-adapter/actuator/health

# Check SAMOS adapter status
curl -H "X-Tenant-Id: {tenant-id}" http://localhost:8080/samos-adapter/api/v1/adapters/active

# Get pending messages
curl -H "X-Tenant-Id: {tenant-id}" http://localhost:8080/samos-adapter/api/v1/payments/pending

# Validate ISO 20022 message
curl -X POST -H "Content-Type: application/json" \
  -d '{"messageId":"MSG-123","messageType":"PACS_008","iso20022Payload":"<xml>...</xml>"}' \
  http://localhost:8080/samos-adapter/api/v1/payments/validate
```

## Security

### mTLS Configuration
- **Certificate-based Authentication**: Client certificates for SAMOS integration
- **Certificate Rotation**: Support for certificate updates
- **Encryption**: All communications encrypted with TLS 1.3

### Tenant Isolation
- **Row Level Security**: Database-level tenant isolation
- **Context Validation**: Tenant context validation on all operations
- **Audit Logging**: Complete audit trail for compliance

## Performance

### Expected Performance
- **Throughput**: 1,000+ payments per minute
- **Latency**: < 2 seconds for payment submission
- **Availability**: 99.9% uptime
- **Settlement Time**: Real-time settlement confirmation

### Scaling Considerations
- **Horizontal Scaling**: Multiple service instances
- **Database Connection Pooling**: Optimized connection management
- **Caching**: Redis-based caching for performance
- **Circuit Breaker**: Automatic failure handling

## Integration

### SAMOS System Integration
- **Real-time Processing**: Immediate payment processing
- **Status Tracking**: Complete payment lifecycle tracking
- **Settlement Confirmation**: Real-time settlement notifications
- **Error Handling**: Comprehensive error management

### Internal Service Integration
- **Payment Initiation Service**: Payment submission integration
- **Saga Orchestrator**: Distributed transaction management
- **Audit Service**: Compliance and audit logging
- **Notification Service**: Status notifications

## Compliance

### Regulatory Requirements
- **SARB Compliance**: South African Reserve Bank regulations
- **ISO 20022 Standards**: International messaging standards
- **Audit Requirements**: Complete audit trail maintenance
- **Data Retention**: 7-year data retention policy

### Security Standards
- **mTLS Authentication**: Mutual TLS for secure communication
- **Data Encryption**: End-to-end data encryption
- **Access Control**: Role-based access control
- **Audit Logging**: Comprehensive security audit logging
