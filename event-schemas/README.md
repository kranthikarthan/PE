# Event Schemas for Payments Engine

## Overview

This directory contains AsyncAPI 2.0 specifications for all events used in the Payments Engine. The event-driven architecture enables asynchronous, decoupled communication between microservices with comprehensive versioning, backward compatibility, and multi-tenancy support.

## Files Structure

```
event-schemas/
├── README.md                           # This file
├── asyncapi-master.yaml               # Master AsyncAPI specification
├── versioning-strategy.md             # Event versioning strategy
├── event-flows.md                     # Event flow documentation
├── schemas/                           # Individual event schemas
│   ├── payment-events.yaml           # Payment-related events
│   ├── transaction-events.yaml       # Transaction events
│   ├── clearing-events.yaml          # Clearing system events
│   ├── settlement-events.yaml        # Settlement events
│   ├── saga-events.yaml              # Saga orchestration events
│   ├── notification-events.yaml     # Notification events
│   ├── audit-events.yaml            # Audit events
│   └── tenant-events.yaml           # Tenant management events
└── examples/                         # Event examples
    ├── payment-initiated-v1.json    # Payment initiated event example
    ├── payment-completed-v1.json    # Payment completed event example
    └── saga-started-v1.json         # Saga started event example
```

## Event Categories

### 1. Payment Events
- **PaymentInitiated**: Payment initiation
- **PaymentValidated**: Payment validation success
- **ValidationFailed**: Payment validation failure
- **PaymentCompleted**: Payment completion
- **PaymentFailed**: Payment failure

### 2. Transaction Events
- **TransactionCreated**: Transaction record creation
- **TransactionProcessing**: Transaction processing start
- **TransactionCompleted**: Transaction completion
- **TransactionFailed**: Transaction failure

### 3. Clearing Events
- **ClearingSubmitted**: Clearing system submission
- **ClearingAcknowledged**: Clearing system acknowledgment
- **ClearingCompleted**: Clearing process completion
- **ClearingFailed**: Clearing process failure

### 4. Settlement Events
- **SettlementBatchCreated**: Settlement batch creation
- **SettlementCompleted**: Settlement completion
- **SettlementFailed**: Settlement failure

### 5. Saga Events
- **SagaStarted**: Saga orchestration start
- **SagaStepCompleted**: Saga step completion
- **SagaCompensating**: Saga compensation start
- **SagaCompleted**: Saga completion

### 6. Notification Events
- **NotificationSent**: Notification delivery
- **NotificationFailed**: Notification failure

### 7. Audit Events
- **AuditEventLogged**: Audit event logging

### 8. Tenant Events
- **TenantCreated**: Tenant creation
- **TenantUpdated**: Tenant updates

## Event Naming Convention

**Format**: `{Domain}.{Entity}.{Action}.v{Version}`

**Examples**:
- `payment.payment.initiated.v1`
- `transaction.transaction.created.v1`
- `clearing.clearing.submitted.v1`
- `saga.saga.started.v1`

## Event Structure

### Base Event Metadata

Every event includes the following metadata:

```yaml
EventMetadata:
  type: object
  required:
    - eventId
    - eventType
    - timestamp
    - correlationId
    - source
    - version
    - tenantId
  properties:
    eventId:
      type: string
      format: uuid
      description: Unique event identifier
    eventType:
      type: string
      description: Type of event
    timestamp:
      type: string
      format: date-time
      description: Event occurrence timestamp (ISO 8601)
    correlationId:
      type: string
      format: uuid
      description: Correlation ID for tracing across services
    causationId:
      type: string
      format: uuid
      description: ID of event that caused this event
    source:
      type: string
      description: Source service that published event
    version:
      type: string
      pattern: '^\d+\.\d+\.\d+$'
      description: Semantic version of the event schema
    tenantId:
      type: string
      description: Tenant identifier for multi-tenancy
    businessUnitId:
      type: string
      description: Business unit identifier within tenant
```

## Versioning Strategy

### Semantic Versioning (SemVer)
- **Major (X.0.0)**: Breaking changes that require consumer updates
- **Minor (X.Y.0)**: New fields added (backward compatible)
- **Patch (X.Y.Z)**: Bug fixes, documentation updates

### Backward Compatibility
- **Additive Changes Only**: New fields can be added without breaking existing consumers
- **Optional Fields**: New fields are always optional with sensible defaults
- **Deprecation Process**: Fields are marked as deprecated before removal

### Forward Compatibility
- **Unknown Fields**: Consumers must ignore unknown fields
- **Version Negotiation**: Consumers specify supported versions
- **Graceful Degradation**: Fallback to older versions when needed

## Event Flow Patterns

### 1. Happy Path Flow
```
PaymentInitiated → PaymentValidated → FundsReserved → RoutingDetermined → 
TransactionCreated → ClearingSubmitted → ClearingCompleted → PaymentCompleted
```

### 2. Validation Failure Flow
```
PaymentInitiated → ValidationFailed → PaymentFailed
```

### 3. Compensation Flow
```
PaymentInitiated → PaymentValidated → TransactionCreated → ClearingFailed → 
SagaCompensating → FundsReleased → LimitReleased → PaymentFailed
```

## Multi-Tenancy Support

### Tenant Isolation
- Every event includes `tenantId` and `businessUnitId`
- Row-Level Security (RLS) policies enforce tenant isolation
- Event routing based on tenant context

### Tenant-Specific Events
- **TenantCreated**: New tenant onboarding
- **TenantUpdated**: Tenant configuration changes
- **UserAuthenticated**: User authentication events
- **UserAuthorizationFailed**: Authorization failures

## Security Features

### Event Encryption
- Sensitive data encrypted at rest
- Event payload encryption for sensitive information
- Key rotation support

### Event Authentication
- JWT tokens for event authentication
- Service-to-service authentication
- API key management

### Event Authorization
- Role-based access control (RBAC)
- Attribute-based access control (ABAC)
- Tenant-level authorization

## Performance Considerations

### Event Batching
- Batch multiple events for efficiency
- Configurable batch sizes
- Batch timeout settings

### Event Filtering
- Tenant-based filtering
- Event type filtering
- Version-based filtering

### Event Compression
- GZIP compression for large events
- Configurable compression levels
- Compression ratio monitoring

## Monitoring and Observability

### Key Metrics
- **Event Publishing Rate**: Events per second per topic
- **Event Processing Latency**: Time from publish to consume
- **Dead Letter Queue Size**: Number of failed events
- **Event Processing Errors**: Error rate per consumer

### Health Checks
- **Event Bus Connectivity**: Azure Service Bus health
- **Event Processing Health**: Consumer health status
- **Dead Letter Queue Health**: Failed event monitoring

### Alerting
- **High Error Rates**: Consumer error rate alerts
- **Dead Letter Queue Growth**: Failed event accumulation
- **Processing Latency**: Slow event processing alerts

## Testing Strategy

### Unit Testing
- Event schema validation
- Event payload testing
- Version compatibility testing

### Integration Testing
- End-to-end event flows
- Cross-service communication
- Error handling scenarios

### Load Testing
- High-volume event processing
- Performance under load
- Scalability testing

## Deployment Strategy

### Environment Configuration
- **Development**: Local event bus simulation
- **Staging**: Azure Service Bus staging
- **Production**: Azure Service Bus production

### Blue-Green Deployment
- Zero-downtime event schema updates
- Gradual consumer migration
- Rollback capabilities

### Feature Flags
- Event version toggling
- Consumer enable/disable
- A/B testing support

## Best Practices

### 1. Event Design
- **Idempotency**: All events must be idempotent
- **Atomicity**: Events represent atomic business operations
- **Consistency**: Event schemas must be consistent across versions
- **Durability**: Events must be persisted reliably

### 2. Consumer Implementation
- **Idempotent Processing**: Handle duplicate events gracefully
- **Error Handling**: Implement retry and dead letter queue patterns
- **Version Support**: Support multiple event versions
- **Monitoring**: Implement comprehensive monitoring

### 3. Publisher Implementation
- **Event Validation**: Validate events before publishing
- **Version Management**: Include correct version information
- **Error Handling**: Handle publishing failures gracefully
- **Monitoring**: Track publishing metrics

### 4. Schema Evolution
- **Backward Compatibility**: Maintain backward compatibility
- **Deprecation Process**: Follow proper deprecation procedures
- **Migration Support**: Provide migration tools and documentation
- **Testing**: Test all version combinations

## Tools and Libraries

### AsyncAPI Tools
- **AsyncAPI Generator**: Generate code from specifications
- **AsyncAPI Studio**: Visual schema editor
- **AsyncAPI CLI**: Command-line tools

### Event Processing
- **Spring Cloud Stream**: Event-driven microservices
- **Azure Service Bus**: Message broker
- **Apache Kafka**: Alternative message broker

### Monitoring
- **Azure Monitor**: Event monitoring
- **Application Insights**: Application performance monitoring
- **Prometheus**: Metrics collection
- **Grafana**: Visualization and alerting

## Examples

### Payment Initiated Event (v1.0.0)
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "payment.payment.initiated.v1",
  "timestamp": "2025-10-11T10:30:00Z",
  "correlationId": "550e8400-e29b-41d4-a716-446655440001",
  "causationId": "550e8400-e29b-41d4-a716-446655440002",
  "source": "PaymentInitiationService",
  "version": "1.0.0",
  "tenantId": "TENANT-001",
  "businessUnitId": "BU-001",
  "paymentId": "PAY-2025-0001",
  "idempotencyKey": "550e8400-e29b-41d4-a716-446655440003",
  "sourceAccount": "1234567890",
  "destinationAccount": "0987654321",
  "amount": 1000.00,
  "currency": "ZAR",
  "reference": "Payment for services",
  "paymentType": "EFT",
  "priority": "NORMAL",
  "initiatedBy": "user@example.com",
  "initiatedAt": "2025-10-11T10:30:00Z"
}
```

### Payment Completed Event (v1.0.0)
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440004",
  "eventType": "payment.payment.completed.v1",
  "timestamp": "2025-10-11T10:35:00Z",
  "correlationId": "550e8400-e29b-41d4-a716-446655440001",
  "causationId": "550e8400-e29b-41d4-a716-446655440000",
  "source": "TransactionProcessingService",
  "version": "1.0.0",
  "tenantId": "TENANT-001",
  "businessUnitId": "BU-001",
  "paymentId": "PAY-2025-0001",
  "transactionId": "TXN-2025-0001",
  "finalStatus": "COMPLETED",
  "completedAt": "2025-10-11T10:35:00Z"
}
```

## Next Steps

1. **Review Schemas**: Validate all event schemas against requirements
2. **Generate Code**: Use AsyncAPI Generator to create client libraries
3. **Implement Consumers**: Build event consumers for each service
4. **Testing**: Implement comprehensive testing strategy
5. **Monitoring**: Set up monitoring and alerting
6. **Deployment**: Deploy to staging and production environments

## Support and Documentation

- **Schema Documentation**: Auto-generated from AsyncAPI specifications
- **API Documentation**: OpenAPI specifications for all services
- **Runbook**: Operational procedures for event management
- **Troubleshooting Guide**: Common issues and solutions

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Author**: AI Agent Orchestrator  
**Review Status**: Ready for Implementation
