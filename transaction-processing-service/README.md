# Transaction Processing Service

## Overview

The Transaction Processing Service is responsible for processing payment transactions, maintaining double-entry bookkeeping, and ensuring financial integrity. It implements comprehensive transaction lifecycle management with event sourcing and audit trails.

## Features

- **Double-Entry Bookkeeping**: Ensures financial integrity with balanced ledger entries
- **Transaction Lifecycle**: Complete transaction processing from creation to completion
- **Event Sourcing**: Comprehensive event store for transaction history
- **Ledger Management**: Real-time balance calculations and validation
- **Multi-tenant Support**: Tenant-specific transaction isolation
- **Audit Trail**: Complete transaction audit and compliance reporting

## API Endpoints

### Health Check
```
GET /transaction-processing/actuator/health
```

### Transaction Operations
```
POST /transaction-processing/api/v1/transactions
GET  /transaction-processing/api/v1/transactions/{transactionId}
GET  /transaction-processing/api/v1/transactions
PUT  /transaction-processing/api/v1/transactions/{transactionId}/status
```

### Ledger Operations
```
GET  /transaction-processing/api/v1/ledger/entries
GET  /transaction-processing/api/v1/ledger/balance/{accountId}
GET  /transaction-processing/api/v1/ledger/accounts
```

### Event Operations
```
GET  /transaction-processing/api/v1/events/{transactionId}
GET  /transaction-processing/api/v1/events
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `docker` |
| `SPRING_DATASOURCE_URL` | Database connection URL | `jdbc:postgresql://postgres:5432/transaction_processing` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `payments_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `payments_password` |
| `SPRING_REDIS_HOST` | Redis host | `redis` |
| `SPRING_REDIS_PORT` | Redis port | `6379` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | `kafka:29092` |

### Transaction Configuration

```yaml
transaction-processing:
  ledger:
    balance-cache-ttl: 1800 # 30 minutes
    enable-double-entry-validation: true
  events:
    enable-event-sourcing: true
    event-retention-days: 90
  clearing:
    supported-systems:
      - CHAPS
      - BACS
      - FPS
      - SWIFT
```

## Dependencies

### External Services
- **PostgreSQL**: Transaction and ledger persistence
- **Redis**: Balance caching and session management
- **Kafka**: Event publishing and consumption
- **Clearing Systems**: External clearing system integration

### Internal Dependencies
- **Domain Models**: Transaction entities and value objects
- **Contracts**: API contracts and DTOs
- **Shared Config**: Common configuration properties
- **Shared Telemetry**: Observability and monitoring

## Health Checks

The service exposes comprehensive health checks:

- **Database Health**: PostgreSQL connection and query validation
- **Redis Health**: Cache connectivity and operations
- **Kafka Health**: Message processing status
- **Ledger Health**: Double-entry validation status
- **Event Store Health**: Event sourcing system status

## Transaction Lifecycle

### Transaction States
1. **CREATED**: Transaction initialized
2. **PROCESSING**: Transaction being processed
3. **CLEARING**: Sent to clearing system
4. **COMPLETED**: Successfully processed
5. **FAILED**: Processing failed
6. **CANCELLED**: Transaction cancelled

### Double-Entry Bookkeeping
- **Debit Entry**: Account debited (money out)
- **Credit Entry**: Account credited (money in)
- **Balance Validation**: Ensures debits equal credits
- **Account Balances**: Real-time balance calculations

## Ledger Management

### Account Types
- **Asset Accounts**: Cash, investments, receivables
- **Liability Accounts**: Payables, deposits, loans
- **Equity Accounts**: Capital, retained earnings
- **Revenue Accounts**: Income, fees, interest
- **Expense Accounts**: Costs, fees, charges

### Balance Calculations
- **Real-time Updates**: Immediate balance updates
- **Cached Balances**: Redis-cached for performance
- **Validation**: Continuous double-entry validation
- **Reconciliation**: Periodic balance reconciliation

## Event Sourcing

### Event Types
- **TransactionCreatedEvent**: Transaction initialization
- **TransactionProcessingEvent**: Processing started
- **TransactionClearingEvent**: Sent to clearing
- **TransactionCompletedEvent**: Successfully completed
- **TransactionFailedEvent**: Processing failed

### Event Store
- **Event Persistence**: All events stored in database
- **Event Replay**: Ability to replay events for recovery
- **Event Queries**: Complex event queries and analytics
- **Event Retention**: Configurable event retention periods

## Monitoring

### Metrics
- Transaction processing rates
- Ledger balance accuracy
- Event processing performance
- Cache hit/miss ratios
- Double-entry validation success

### Logging
- Structured JSON logging
- Transaction correlation IDs
- Event sourcing traces
- Balance calculation logs

## Runbook

### Common Issues

#### Service Won't Start
1. Check database connectivity: `curl http://localhost:8085/transaction-processing/actuator/health`
2. Verify Redis connection: Check logs for Redis connection errors
3. Validate Kafka connectivity: Ensure Kafka is running and accessible

#### Transaction Failures
1. Check ledger validation: Review double-entry validation logs
2. Verify account balances: Ensure sufficient funds available
3. Check clearing system connectivity: Ensure external systems are accessible

#### Balance Discrepancies
1. Run balance reconciliation: Check for ledger inconsistencies
2. Review event store: Validate event sequence and integrity
3. Check cache consistency: Ensure Redis cache is synchronized

### Troubleshooting Commands

```bash
# Check service health
curl -f http://localhost:8085/transaction-processing/actuator/health

# View service logs
docker-compose logs transaction-processing-service

# Check ledger balance
curl -X GET http://localhost:8085/transaction-processing/api/v1/ledger/balance/{accountId}

# Get transaction events
curl -X GET http://localhost:8085/transaction-processing/api/v1/events/{transactionId}
```

## Development

### Local Setup
```bash
# Start dependencies
docker-compose up -d postgres redis kafka

# Run the service
mvn spring-boot:run -pl transaction-processing-service
```

### Testing
```bash
# Run unit tests
mvn test -pl transaction-processing-service

# Run integration tests
mvn test -pl transaction-processing-service -Dtest=*IntegrationTest

# Run ledger tests
mvn test -pl transaction-processing-service -Dtest=*LedgerTest
```

## API Examples

### Create Transaction
```bash
curl -X POST http://localhost:8085/transaction-processing/api/v1/transactions \
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

### Get Transaction Status
```bash
curl -X GET http://localhost:8085/transaction-processing/api/v1/transactions/{transactionId} \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456"
```

### Get Account Balance
```bash
curl -X GET http://localhost:8085/transaction-processing/api/v1/ledger/balance/{accountId} \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456"
```

### Get Transaction Events
```bash
curl -X GET http://localhost:8085/transaction-processing/api/v1/events/{transactionId} \
  -H "X-Tenant-ID: tenant-123" \
  -H "X-Business-Unit-ID: bu-456"
```

## Security

- **Transaction Isolation**: Complete transaction isolation between tenants
- **Audit Logging**: Comprehensive audit trail for all transactions
- **Data Encryption**: Sensitive financial data encryption
- **Access Control**: Role-based access to transaction operations

## Performance Optimization

### Caching Strategy
- **Balance Caching**: Redis-cached account balances
- **Transaction Caching**: Frequently accessed transaction data
- **Event Caching**: Recent event data caching

### Database Optimization
- **Indexing**: Optimized database indexes for queries
- **Partitioning**: Event store partitioning by date
- **Connection Pooling**: Optimized database connection management
