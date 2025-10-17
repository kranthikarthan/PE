# PayShap Adapter Service

## Overview

The PayShap Adapter Service provides instant P2P (Person-to-Person) payment processing with proxy registry integration. This service enables real-time payments up to R3,000 with mobile number and email address lookups for instant settlement.

## Features

### Core Capabilities
- **Instant P2P Payments** - Real-time person-to-person transfers
- **Proxy Registry Integration** - Mobile number and email address lookups
- **Amount Limit** - R3,000 maximum per transaction
- **ISO 20022 Messaging** - Standardized payment message format
- **Real-time Settlement** - Instant processing and settlement
- **24/7/365 Availability** - Continuous operation support

### Technical Features
- **Spring Boot 3.x** - Modern Java framework
- **Resilience4j** - Circuit breakers, retries, and timeouts
- **OpenFeign** - Declarative REST client for external APIs
- **JPA/Hibernate** - Database persistence
- **PostgreSQL** - Primary database
- **Flyway** - Database migrations
- **OpenAPI/Swagger** - API documentation
- **Actuator** - Health checks and metrics

## Architecture

### Domain Model
- **PayShapAdapter** - Adapter configuration and management
- **PayShapPaymentMessage** - Payment messages with proxy integration
- **PayShapTransactionLog** - Transaction audit logs
- **PayShapSettlementRecord** - Settlement records for instant payments
- **PayShapProxyCache** - Proxy registry cache for lookups

### Services
- **PayShapAdapterService** - Adapter management operations
- **PayShapPaymentProcessingService** - Payment processing with proxy integration
- **PayShapIso20022Service** - ISO 20022 message handling

### Controllers
- **PayShapAdapterController** - Adapter management REST API
- **PayShapPaymentProcessingController** - Payment processing REST API

## API Endpoints

### Adapter Management
- `POST /api/v1/payshap/adapters` - Create adapter
- `PUT /api/v1/payshap/adapters/{id}` - Update adapter configuration
- `POST /api/v1/payshap/adapters/{id}/activate` - Activate adapter
- `POST /api/v1/payshap/adapters/{id}/deactivate` - Deactivate adapter
- `GET /api/v1/payshap/adapters/{id}` - Get adapter details
- `GET /api/v1/payshap/adapters/tenant/{tenantId}/business-unit/{businessUnitId}` - Get adapters by tenant

### Payment Processing
- `POST /api/v1/payshap/payments/process` - Process PayShap payment
- `POST /api/v1/payshap/payments/{messageId}/submit` - Submit payment
- `POST /api/v1/payshap/payments/{messageId}/response` - Process payment response
- `GET /api/v1/payshap/payments/{messageId}` - Get payment message
- `GET /api/v1/payshap/payments/adapter/{adapterId}` - Get payments by adapter
- `GET /api/v1/payshap/payments/transaction/{transactionId}` - Get payment by transaction ID

## Configuration

### Application Properties
```yaml
payshap:
  adapter:
    endpoint: https://payshap.sarb.co.za/api
    timeout: 5000
    retry-attempts: 3
    amount-limit: 3000.00
    currency: ZAR
    proxy-registry:
      endpoint: https://proxy.sarb.co.za/api
      timeout: 3000
      retry-attempts: 2
```

### Resilience4j Configuration
- **Circuit Breaker** - 50% failure threshold, 30s wait duration
- **Retry** - 3 attempts with 1s intervals
- **Timeout** - 5s duration for PayShap operations, 3s for proxy registry

## Database Schema

### Tables
- `payshap_adapters` - Adapter configurations
- `payshap_payment_messages` - Payment messages with proxy integration
- `payshap_transaction_logs` - Transaction audit logs
- `payshap_settlement_records` - Settlement records
- `payshap_proxy_cache` - Proxy registry cache

### Key Features
- **Multi-tenant Support** - Tenant and business unit isolation
- **Audit Logging** - Comprehensive transaction logging
- **Proxy Integration** - Mobile/email lookup capabilities
- **Instant Settlement** - Real-time payment processing

## Usage Examples

### Create PayShap Adapter
```bash
curl -X POST http://localhost:8084/api/v1/payshap/adapters \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant-123",
    "tenantName": "Test Tenant",
    "businessUnitId": "bu-456",
    "businessUnitName": "Test Business Unit",
    "adapterName": "Test PayShap Adapter",
    "endpoint": "https://payshap.test.com/api",
    "createdBy": "admin"
  }'
```

### Process PayShap Payment
```bash
curl -X POST http://localhost:8084/api/v1/payshap/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "adapterId": "adapter-123",
    "transactionId": "TXN-123",
    "amount": 1500.00,
    "currency": "ZAR",
    "debtorName": "John Doe",
    "debtorAccount": "1234567890",
    "creditorName": "Jane Smith",
    "creditorMobile": "0821234567",
    "proxyType": "MOBILE",
    "proxyValue": "0821234567",
    "paymentPurpose": "Payment for services"
  }'
```

## Monitoring

### Health Checks
- `GET /actuator/health` - Service health status
- `GET /actuator/info` - Service information
- `GET /actuator/metrics` - Service metrics

### Key Metrics
- Payment processing rates
- Success/failure rates
- Response times
- Circuit breaker status
- Proxy registry lookup performance

## Development

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 13+
- Docker (optional)

### Building
```bash
mvn clean install
```

### Running
```bash
mvn spring-boot:run
```

### Testing
```bash
mvn test
```

## Integration

### External Systems
- **PayShap Network** - South African instant payment system
- **Proxy Registry** - Mobile/email lookup service
- **ISO 20022** - Standardized payment messaging

### Internal Systems
- **Saga Orchestrator** - Payment orchestration
- **Transaction Processing Service** - Core transaction handling
- **Account Adapter Service** - Account management

## Security

### Authentication
- JWT token-based authentication
- Multi-tenant access control
- Role-based permissions

### Data Protection
- Encryption at rest and in transit
- PII data masking
- Audit logging for compliance

## Deployment

### Docker
```bash
docker build -t payshap-adapter-service .
docker run -p 8084:8084 payshap-adapter-service
```

### Kubernetes
```bash
kubectl apply -f k8s/deployments/payshap-adapter.yaml
```

## Troubleshooting

### Common Issues
1. **Circuit Breaker Open** - Check external service availability
2. **Proxy Lookup Failures** - Verify proxy registry connectivity
3. **Amount Limit Exceeded** - Ensure amounts are within R3,000 limit
4. **Database Connection Issues** - Check PostgreSQL connectivity

### Logs
- Application logs: `logs/payshap-adapter-service.log`
- Database logs: Check PostgreSQL logs
- External service logs: Check PayShap and proxy registry logs

## Support

For technical support and questions:
- Check the logs for error details
- Verify configuration settings
- Test external service connectivity
- Review circuit breaker status
