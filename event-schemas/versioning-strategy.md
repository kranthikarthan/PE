# Event Schema Versioning Strategy

## Overview

This document defines the versioning strategy for event schemas in the Payments Engine. We use semantic versioning with backward compatibility guarantees to ensure smooth evolution of the event-driven architecture.

## Versioning Principles

### 1. Semantic Versioning (SemVer)
- **Major (X.0.0)**: Breaking changes that require consumer updates
- **Minor (X.Y.0)**: New fields added (backward compatible)
- **Patch (X.Y.Z)**: Bug fixes, documentation updates

### 2. Backward Compatibility
- **Additive Changes Only**: New fields can be added without breaking existing consumers
- **Optional Fields**: New fields are always optional with sensible defaults
- **Deprecation Process**: Fields are marked as deprecated before removal

### 3. Forward Compatibility
- **Unknown Fields**: Consumers must ignore unknown fields
- **Version Negotiation**: Consumers specify supported versions
- **Graceful Degradation**: Fallback to older versions when needed

## Versioning Implementation

### Event Schema Structure

```yaml
# Base event structure with versioning
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
    eventType:
      type: string
    timestamp:
      type: string
      format: date-time
    correlationId:
      type: string
      format: uuid
    causationId:
      type: string
      format: uuid
    source:
      type: string
    version:
      type: string
      pattern: '^\d+\.\d+\.\d+$'
      description: Semantic version of the event schema
    tenantId:
      type: string
    businessUnitId:
      type: string
```

### Version Evolution Examples

#### PaymentInitiated Event Evolution

**Version 1.0.0 (Initial)**
```yaml
PaymentInitiatedPayload:
  allOf:
    - $ref: '#/components/schemas/EventMetadata'
    - type: object
      required:
        - paymentId
        - sourceAccount
        - destinationAccount
        - amount
        - currency
        - paymentType
      properties:
        paymentId:
          type: string
        sourceAccount:
          type: string
        destinationAccount:
          type: string
        amount:
          type: number
        currency:
          type: string
        paymentType:
          type: string
```

**Version 1.1.0 (Added priority field)**
```yaml
PaymentInitiatedPayload:
  allOf:
    - $ref: '#/components/schemas/EventMetadata'
    - type: object
      required:
        - paymentId
        - sourceAccount
        - destinationAccount
        - amount
        - currency
        - paymentType
      properties:
        paymentId:
          type: string
        sourceAccount:
          type: string
        destinationAccount:
          type: string
        amount:
          type: number
        currency:
          type: string
        paymentType:
          type: string
        priority:  # NEW FIELD - Optional
          type: string
          enum: [NORMAL, HIGH, URGENT]
          default: NORMAL
```

**Version 2.0.0 (Breaking change - required field)**
```yaml
PaymentInitiatedPayload:
  allOf:
    - $ref: '#/components/schemas/EventMetadata'
    - type: object
      required:
        - paymentId
        - sourceAccount
        - destinationAccount
        - amount
        - currency
        - paymentType
        - initiatedBy  # BREAKING CHANGE - Now required
      properties:
        paymentId:
          type: string
        sourceAccount:
          type: string
        destinationAccount:
          type: string
        amount:
          type: number
        currency:
          type: string
        paymentType:
          type: string
        priority:
          type: string
          enum: [NORMAL, HIGH, URGENT]
          default: NORMAL
        initiatedBy:  # Now required
          type: string
```

## Consumer Version Support

### Version Negotiation

```java
@Component
public class EventConsumer {
    
    @EventListener
    public void handlePaymentInitiated(PaymentInitiatedEvent event) {
        // Check if we support this version
        if (!isVersionSupported(event.getVersion())) {
            log.warn("Unsupported event version: {}", event.getVersion());
            return;
        }
        
        // Process based on version
        switch (getMajorVersion(event.getVersion())) {
            case 1:
                handleV1Event(event);
                break;
            case 2:
                handleV2Event(event);
                break;
            default:
                log.error("Unknown major version: {}", event.getVersion());
        }
    }
    
    private boolean isVersionSupported(String version) {
        return version.startsWith("1.") || version.startsWith("2.");
    }
    
    private int getMajorVersion(String version) {
        return Integer.parseInt(version.split("\\.")[0]);
    }
}
```

### Backward Compatibility Handling

```java
public class PaymentInitiatedEventV1 {
    private String paymentId;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private String currency;
    private String paymentType;
    // No priority field in V1
}

public class PaymentInitiatedEventV2 {
    private String paymentId;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private String currency;
    private String paymentType;
    private String priority = "NORMAL"; // Default value for V1 compatibility
    private String initiatedBy; // Required in V2
}
```

## Migration Strategy

### 1. Deprecation Process

```yaml
# Mark field as deprecated
properties:
  oldField:
    type: string
    deprecated: true
    description: "Deprecated in v2.0.0, use newField instead"
  newField:
    type: string
    description: "Replacement for oldField"
```

### 2. Gradual Migration

```java
@Service
public class EventMigrationService {
    
    public PaymentInitiatedEventV2 migrateV1ToV2(PaymentInitiatedEventV1 v1Event) {
        PaymentInitiatedEventV2 v2Event = new PaymentInitiatedEventV2();
        
        // Copy common fields
        v2Event.setPaymentId(v1Event.getPaymentId());
        v2Event.setSourceAccount(v1Event.getSourceAccount());
        v2Event.setDestinationAccount(v1Event.getDestinationAccount());
        v2Event.setAmount(v1Event.getAmount());
        v2Event.setCurrency(v1Event.getCurrency());
        v2Event.setPaymentType(v1Event.getPaymentType());
        
        // Set defaults for new fields
        v2Event.setPriority("NORMAL");
        v2Event.setInitiatedBy("SYSTEM"); // Default for migrated events
        
        return v2Event;
    }
}
```

### 3. Dual Publishing

```java
@Service
public class EventPublisher {
    
    @Autowired
    private EventBus eventBus;
    
    public void publishPaymentInitiated(PaymentInitiatedEvent event) {
        // Publish both versions during transition period
        eventBus.publish("payment/initiated/v1", event);
        eventBus.publish("payment/initiated/v2", migrateToV2(event));
    }
}
```

## Testing Strategy

### 1. Version Compatibility Tests

```java
@Test
public void shouldHandleV1Events() {
    PaymentInitiatedEventV1 v1Event = createV1Event();
    
    // Should not throw exception
    assertDoesNotThrow(() -> 
        eventConsumer.handlePaymentInitiated(v1Event)
    );
}

@Test
public void shouldHandleV2Events() {
    PaymentInitiatedEventV2 v2Event = createV2Event();
    
    // Should not throw exception
    assertDoesNotThrow(() -> 
        eventConsumer.handlePaymentInitiated(v2Event)
    );
}
```

### 2. Schema Validation Tests

```java
@Test
public void shouldValidateV1Schema() {
    PaymentInitiatedEventV1 event = createV1Event();
    
    // Validate against V1 schema
    assertTrue(schemaValidator.validate(event, "PaymentInitiatedV1"));
}

@Test
public void shouldValidateV2Schema() {
    PaymentInitiatedEventV2 event = createV2Event();
    
    // Validate against V2 schema
    assertTrue(schemaValidator.validate(event, "PaymentInitiatedV2"));
}
```

## Monitoring and Observability

### 1. Version Usage Metrics

```java
@Component
public class EventVersionMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public void recordEventVersion(String eventType, String version) {
        Counter.builder("events.version")
            .tag("eventType", eventType)
            .tag("version", version)
            .register(meterRegistry)
            .increment();
    }
}
```

### 2. Compatibility Monitoring

```java
@Component
public class EventCompatibilityMonitor {
    
    public void monitorCompatibility(String eventType, String version, String consumer) {
        // Track which consumers support which versions
        Gauge.builder("event.compatibility")
            .tag("eventType", eventType)
            .tag("version", version)
            .tag("consumer", consumer)
            .register(meterRegistry, this, obj -> getCompatibilityScore(eventType, version, consumer));
    }
}
```

## Best Practices

### 1. Version Naming Convention

- **Event Type**: `{Domain}.{Entity}.{Action}.v{Version}`
- **Examples**:
  - `payment.payment.initiated.v1`
  - `payment.payment.initiated.v2`
  - `account.funds.reserved.v1`

### 2. Schema Evolution Rules

1. **Additive Changes Only**: Never remove or change existing fields
2. **Optional New Fields**: All new fields must be optional
3. **Default Values**: Provide sensible defaults for new fields
4. **Deprecation Period**: Minimum 6 months before removing deprecated fields

### 3. Consumer Guidelines

1. **Ignore Unknown Fields**: Always ignore fields you don't recognize
2. **Version Checking**: Always check event version before processing
3. **Graceful Degradation**: Handle unsupported versions gracefully
4. **Migration Planning**: Plan for version migrations in advance

### 4. Publisher Guidelines

1. **Version Headers**: Always include version in event metadata
2. **Backward Compatibility**: Ensure new versions don't break existing consumers
3. **Migration Support**: Provide migration tools and documentation
4. **Testing**: Test all version combinations thoroughly

## Implementation Timeline

### Phase 1: Foundation (Weeks 1-2)
- [ ] Implement base event metadata structure
- [ ] Create version negotiation framework
- [ ] Set up schema validation infrastructure

### Phase 2: Core Events (Weeks 3-4)
- [ ] Implement PaymentInitiated v1.0.0
- [ ] Implement PaymentValidated v1.0.0
- [ ] Implement TransactionCreated v1.0.0

### Phase 3: Extended Events (Weeks 5-6)
- [ ] Implement all 30+ event types v1.0.0
- [ ] Add comprehensive schema validation
- [ ] Create migration tools

### Phase 4: Versioning (Weeks 7-8)
- [ ] Implement version 1.1.0 for key events
- [ ] Add version negotiation
- [ ] Create backward compatibility tests

### Phase 5: Production (Weeks 9-10)
- [ ] Deploy to staging environment
- [ ] Perform integration testing
- [ ] Deploy to production with monitoring

## Conclusion

This versioning strategy ensures that the Payments Engine can evolve its event schemas while maintaining backward compatibility and providing a smooth migration path for consumers. The approach balances flexibility with stability, allowing for innovation while protecting existing integrations.

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Author**: AI Agent Orchestrator  
**Review Status**: Ready for Implementation
