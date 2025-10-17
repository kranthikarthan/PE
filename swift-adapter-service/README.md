# SWIFT Adapter Service

## Overview

The SWIFT Adapter Service provides integration with the SWIFT network for international payments, supporting MT103/pacs.008 messaging, sanctions screening, and foreign exchange (FX) conversion.

## Features

- **International Payments**: Process cross-border payments via SWIFT network
- **ISO 20022 Support**: Handle pacs.008 (Customer Credit Transfer) and pacs.002 (Payment Status Report) messages
- **MT103 Support**: Process traditional MT103 Customer Transfer messages
- **Sanctions Screening**: Integrated compliance checking for regulatory requirements
- **FX Conversion**: Foreign exchange rate conversion and processing
- **Multi-tenant Support**: Row-level security with tenant context
- **Domain Events**: Event-driven architecture with audit trail
- **Route Management**: Payment route configuration and management
- **Message Logging**: Comprehensive message exchange logging

## Architecture

### Domain Model

The service follows Domain-Driven Design (DDD) principles with proper domain model alignment:

- **SwiftAdapter**: Aggregate root for SWIFT adapter configurations
- **SwiftPaymentMessage**: Payment message entities with sanctions screening and FX conversion
- **SwiftTransactionLog**: Transaction logging for audit and reconciliation
- **SwiftSettlementRecord**: Settlement records for correspondent bank processing
- **ClearingRoute**: Payment route management
- **ClearingMessageLog**: Message exchange logging

### Key Components

- **SwiftAdapterService**: Manages adapter lifecycle and configuration
- **SwiftPaymentProcessingService**: Handles payment processing with sanctions screening
- **SwiftIso20022Service**: Processes ISO 20022 messages (pacs.008/pacs.002)
- **SwiftAdapterController**: REST API for adapter management
- **SwiftPaymentProcessingController**: REST API for payment processing

## API Endpoints

### Adapter Management

- `POST /api/v1/swift-adapters` - Create SWIFT adapter
- `GET /api/v1/swift-adapters/{adapterId}` - Get adapter by ID
- `PUT /api/v1/swift-adapters/{adapterId}/configuration` - Update configuration
- `POST /api/v1/swift-adapters/{adapterId}/activate` - Activate adapter
- `POST /api/v1/swift-adapters/{adapterId}/deactivate` - Deactivate adapter
- `GET /api/v1/swift-adapters/tenant/{tenantId}` - Get adapters by tenant
- `DELETE /api/v1/swift-adapters/{adapterId}` - Delete adapter

### Payment Processing

- `POST /api/v1/swift-payments/process` - Process SWIFT payment
- `POST /api/v1/swift-payments/pacs008` - Process pacs.008 message
- `POST /api/v1/swift-payments/pacs002` - Process pacs.002 message
- `POST /api/v1/swift-payments/mt103` - Process MT103 message
- `GET /api/v1/swift-payments/transaction/{transactionId}` - Get payment by transaction ID
- `GET /api/v1/swift-payments/message/{messageId}` - Get payment by message ID
- `GET /api/v1/swift-payments/adapter/{adapterId}` - Get payments by adapter
- `GET /api/v1/swift-payments/status/{status}` - Get payments by status
- `GET /api/v1/swift-payments/currency/{currency}` - Get payments by currency

## Configuration

### Application Properties

```yaml
spring:
  application:
    name: swift-adapter-service
  datasource:
    url: jdbc:postgresql://localhost:5432/swift_adapter
    username: ${DB_USERNAME:swift_adapter}
    password: ${DB_PASSWORD:swift_adapter}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

swift:
  adapter:
    default-timeout: 30
    default-retry-attempts: 3
    sanctions-screening:
      enabled: true
      timeout: 5
      retry-attempts: 2
    fx-conversion:
      enabled: true
      timeout: 10
      retry-attempts: 2
```

### Database Schema

The service uses PostgreSQL with the following key tables:

- `swift_adapters` - Adapter configurations
- `swift_payment_messages` - Payment messages with sanctions screening
- `swift_transaction_logs` - Transaction audit logs
- `swift_settlement_records` - Settlement records
- `swift_clearing_routes` - Payment routes
- `swift_clearing_message_logs` - Message exchange logs

## Domain Events

The service publishes domain events for integration and audit:

- `SwiftAdapterCreatedEvent` - Adapter creation
- `SwiftAdapterConfigurationUpdatedEvent` - Configuration updates
- `SwiftAdapterActivatedEvent` - Adapter activation
- `SwiftAdapterDeactivatedEvent` - Adapter deactivation
- `SwiftRouteAddedEvent` - Route addition
- `SwiftMessageLoggedEvent` - Message logging

## Security

- **Multi-tenant Support**: Row-level security with tenant context
- **Encryption**: End-to-end encryption for sensitive data
- **Audit Logging**: Comprehensive audit trail for compliance
- **Access Control**: Role-based access control (RBAC)

## Monitoring

- **Health Checks**: Spring Boot Actuator endpoints
- **Metrics**: Micrometer metrics for monitoring
- **Logging**: Structured logging with correlation IDs
- **Tracing**: Distributed tracing support

## Testing

- **Unit Tests**: Comprehensive unit test coverage
- **Integration Tests**: Testcontainers for database testing
- **Contract Tests**: API contract testing
- **Performance Tests**: Load testing for high-volume scenarios

## Deployment

### Docker

```dockerfile
FROM openjdk:17-jre-slim
COPY target/swift-adapter-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: swift-adapter-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: swift-adapter-service
  template:
    metadata:
      labels:
        app: swift-adapter-service
    spec:
      containers:
      - name: swift-adapter-service
        image: swift-adapter-service:latest
        ports:
        - containerPort: 8080
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

## Development

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 13+
- Docker (for testing)

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

## Contributing

1. Follow the established coding standards
2. Write comprehensive tests
3. Update documentation
4. Ensure all checks pass

## License

This project is licensed under the MIT License.
