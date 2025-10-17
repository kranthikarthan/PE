# RTC Adapter Service

## Overview

The RTC Adapter Service provides integration with BankservAfrica's RTC (Real-Time Clearing) system for instant low-value payments in South Africa.

## Key Features

- **Real-Time Clearing**: Instant settlement for low-value payments
- **Amount Limit**: R5,000 per transaction maximum
- **24/7/365 Availability**: Continuous operation
- **ISO 20022 Messaging**: Standard pacs.008/pacs.002 message formats
- **REST API Protocol**: Modern HTTP-based integration
- **Fast Settlement**: Real-time settlement within seconds

## Architecture

### Domain Model
- **RtcAdapter**: Main aggregate root for adapter configurations
- **RtcPaymentMessage**: Payment message entities
- **RtcTransactionLog**: Transaction operation logs
- **RtcSettlementRecord**: Daily settlement records

### Key Components
- **RtcAdapterService**: Adapter configuration management
- **RtcPaymentProcessingService**: Payment processing logic
- **RtcIso20022Service**: ISO 20022 message handling
- **RtcAdapterController**: REST API endpoints

## API Endpoints

### Adapter Management
- `POST /api/v1/rtc/adapters` - Create RTC adapter
- `PUT /api/v1/rtc/adapters/{adapterId}` - Update adapter configuration
- `POST /api/v1/rtc/adapters/{adapterId}/activate` - Activate adapter
- `POST /api/v1/rtc/adapters/{adapterId}/deactivate` - Deactivate adapter
- `GET /api/v1/rtc/adapters/{adapterId}` - Get adapter details

### Payment Processing
- `POST /api/v1/rtc/payments/process` - Process RTC payment
- `POST /api/v1/rtc/payments/{messageId}/submit` - Submit payment to RTC
- `POST /api/v1/rtc/payments/{messageId}/response` - Process payment response
- `GET /api/v1/rtc/payments/{messageId}` - Get payment message

## Configuration

### Application Properties
```yaml
rtc:
  adapter:
    endpoint: https://rtc.bankservafrica.co.za/api/v1
    timeout: 10000
    retry-attempts: 3
    amount-limit: 5000.00
    currency: ZAR
    availability: 24/7/365
```

### Resilience Configuration
- **Circuit Breaker**: 50% failure rate threshold
- **Retry**: 3 attempts with 1-second intervals
- **Timeout**: 10-second timeout duration

## Database Schema

### Tables
- `rtc_adapters` - Adapter configurations
- `rtc_payment_messages` - Payment messages
- `rtc_transaction_logs` - Transaction logs
- `rtc_settlement_records` - Settlement records

## Usage Examples

### Create RTC Adapter
```bash
curl -X POST http://localhost:8083/api/v1/rtc/adapters \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant-123",
    "tenantName": "Acme Bank",
    "businessUnitId": "bu-456",
    "businessUnitName": "Retail Banking",
    "adapterName": "RTC-Production",
    "endpoint": "https://rtc.bankservafrica.co.za/api/v1",
    "createdBy": "admin"
  }'
```

### Process RTC Payment
```bash
curl -X POST http://localhost:8083/api/v1/rtc/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "adapterId": "adapter-123",
    "transactionId": "TXN-2025-001",
    "messageType": "pacs.008",
    "direction": "OUTBOUND",
    "amount": 1500.00,
    "currency": "ZAR",
    "debtorName": "John Doe",
    "debtorAccount": "1234567890",
    "creditorName": "Jane Smith",
    "creditorAccount": "0987654321"
  }'
```

## Monitoring

### Health Checks
- `/actuator/health` - Service health status
- `/actuator/metrics` - Performance metrics
- `/actuator/prometheus` - Prometheus metrics

### Key Metrics
- Payment processing rate
- Success/failure rates
- Response times
- Circuit breaker status

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn test -Dtest=*IntegrationTest
```

## Deployment

### Docker
```bash
docker build -t rtc-adapter-service .
docker run -p 8083:8083 rtc-adapter-service
```

### Kubernetes
```bash
kubectl apply -f k8s/deployments/rtc-adapter.yaml
```

## Dependencies

- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Resilience4j
- OpenFeign
- Lombok
- OpenAPI/Swagger

## Related Services

- **SAMOS Adapter**: High-value RTGS payments
- **BankservAfrica Adapter**: EFT batch processing
- **PayShap Adapter**: Instant P2P payments
- **SWIFT Adapter**: International payments
