# Event-Driven Architecture - Event Schemas (AsyncAPI 2.0)

## Overview
This document defines all events used in the payments engine using AsyncAPI 2.0 specification. Events enable asynchronous, decoupled communication between microservices.

---

## Event Naming Convention

**Format**: `{Domain}.{Entity}.{Action}.{Version}`

Examples:
- `payment.payment.initiated.v1`
- `payment.transaction.completed.v1`
- `account.funds.reserved.v1`

---

## Event Categories

### 1. Command Events (Request-Response Pattern)
Used when response is required

### 2. Domain Events (Fire-and-Forget Pattern)
Used for state changes that others need to know about

### 3. Integration Events (External Systems)
Used for external system communication

---

## AsyncAPI Master Document

```yaml
asyncapi: 2.6.0
info:
  title: Payments Engine Event Bus
  version: 1.0.0
  description: Event-driven communication for South African Payments Engine
  contact:
    name: Payments Team
    email: payments@example.com

servers:
  production:
    url: paymentengine.servicebus.windows.net
    protocol: amqps
    description: Azure Service Bus Production

channels:
  payment/initiated:
    description: Payment initiation events
    subscribe:
      message:
        $ref: '#/components/messages/PaymentInitiated'
  
  payment/validated:
    description: Payment validation events
    subscribe:
      message:
        $ref: '#/components/messages/PaymentValidated'
  
  payment/validation-failed:
    description: Payment validation failure events
    subscribe:
      message:
        $ref: '#/components/messages/ValidationFailed'
  
  account/funds-reserved:
    description: Funds reservation events
    subscribe:
      message:
        $ref: '#/components/messages/FundsReserved'
  
  account/insufficient-funds:
    description: Insufficient funds events
    subscribe:
      message:
        $ref: '#/components/messages/InsufficientFunds'
  
  routing/determined:
    description: Routing determination events
    subscribe:
      message:
        $ref: '#/components/messages/RoutingDetermined'
  
  transaction/created:
    description: Transaction creation events
    subscribe:
      message:
        $ref: '#/components/messages/TransactionCreated'
  
  transaction/processing:
    description: Transaction processing events
    subscribe:
      message:
        $ref: '#/components/messages/TransactionProcessing'
  
  clearing/submitted:
    description: Clearing submission events
    subscribe:
      message:
        $ref: '#/components/messages/ClearingSubmitted'
  
  clearing/acknowledged:
    description: Clearing acknowledgment events
    subscribe:
      message:
        $ref: '#/components/messages/ClearingAcknowledged'
  
  clearing/completed:
    description: Clearing completion events
    subscribe:
      message:
        $ref: '#/components/messages/ClearingCompleted'
  
  clearing/failed:
    description: Clearing failure events
    subscribe:
      message:
        $ref: '#/components/messages/ClearingFailed'
  
  payment/completed:
    description: Payment completion events
    subscribe:
      message:
        $ref: '#/components/messages/PaymentCompleted'
  
  payment/failed:
    description: Payment failure events
    subscribe:
      message:
        $ref: '#/components/messages/PaymentFailed'
  
  limit/consumed:
    description: Limit consumed (used) events
    subscribe:
      message:
        $ref: '#/components/messages/LimitConsumed'
  
  limit/released:
    description: Limit released (reservation cancelled) events
    subscribe:
      message:
        $ref: '#/components/messages/LimitReleased'
  
  settlement/batch-created:
    description: Settlement batch creation events
    subscribe:
      message:
        $ref: '#/components/messages/SettlementBatchCreated'
  
  settlement/completed:
    description: Settlement completion events
    subscribe:
      message:
        $ref: '#/components/messages/SettlementCompleted'
  
  saga/started:
    description: Saga start events
    subscribe:
      message:
        $ref: '#/components/messages/SagaStarted'
  
  saga/step-completed:
    description: Saga step completion events
    subscribe:
      message:
        $ref: '#/components/messages/SagaStepCompleted'
  
  saga/compensating:
    description: Saga compensation events
    subscribe:
      message:
        $ref: '#/components/messages/SagaCompensating'
  
  saga/completed:
    description: Saga completion events
    subscribe:
      message:
        $ref: '#/components/messages/SagaCompleted'

components:
  messages:
    PaymentInitiated:
      name: PaymentInitiated
      title: Payment Initiated Event
      summary: Published when a payment is initiated
      contentType: application/json
      payload:
        $ref: '#/components/schemas/PaymentInitiatedPayload'
    
    PaymentValidated:
      name: PaymentValidated
      title: Payment Validated Event
      summary: Published when payment passes validation
      contentType: application/json
      payload:
        $ref: '#/components/schemas/PaymentValidatedPayload'
    
    ValidationFailed:
      name: ValidationFailed
      title: Validation Failed Event
      summary: Published when payment fails validation
      contentType: application/json
      payload:
        $ref: '#/components/schemas/ValidationFailedPayload'
    
    FundsReserved:
      name: FundsReserved
      title: Funds Reserved Event
      summary: Published when funds are reserved on account
      contentType: application/json
      payload:
        $ref: '#/components/schemas/FundsReservedPayload'
    
    InsufficientFunds:
      name: InsufficientFunds
      title: Insufficient Funds Event
      summary: Published when account has insufficient funds
      contentType: application/json
      payload:
        $ref: '#/components/schemas/InsufficientFundsPayload'
    
    RoutingDetermined:
      name: RoutingDetermined
      title: Routing Determined Event
      summary: Published when payment routing is determined
      contentType: application/json
      payload:
        $ref: '#/components/schemas/RoutingDeterminedPayload'
    
    TransactionCreated:
      name: TransactionCreated
      title: Transaction Created Event
      summary: Published when transaction record is created
      contentType: application/json
      payload:
        $ref: '#/components/schemas/TransactionCreatedPayload'
    
    TransactionProcessing:
      name: TransactionProcessing
      title: Transaction Processing Event
      summary: Published when transaction enters processing state
      contentType: application/json
      payload:
        $ref: '#/components/schemas/TransactionProcessingPayload'
    
    ClearingSubmitted:
      name: ClearingSubmitted
      title: Clearing Submitted Event
      summary: Published when payment submitted to clearing system
      contentType: application/json
      payload:
        $ref: '#/components/schemas/ClearingSubmittedPayload'
    
    ClearingAcknowledged:
      name: ClearingAcknowledged
      title: Clearing Acknowledged Event
      summary: Published when clearing system acknowledges receipt
      contentType: application/json
      payload:
        $ref: '#/components/schemas/ClearingAcknowledgedPayload'
    
    ClearingCompleted:
      name: ClearingCompleted
      title: Clearing Completed Event
      summary: Published when clearing process completes
      contentType: application/json
      payload:
        $ref: '#/components/schemas/ClearingCompletedPayload'
    
    ClearingFailed:
      name: ClearingFailed
      title: Clearing Failed Event
      summary: Published when clearing process fails
      contentType: application/json
      payload:
        $ref: '#/components/schemas/ClearingFailedPayload'
    
    PaymentCompleted:
      name: PaymentCompleted
      title: Payment Completed Event
      summary: Published when payment completes successfully
      contentType: application/json
      payload:
        $ref: '#/components/schemas/PaymentCompletedPayload'
    
    PaymentFailed:
      name: PaymentFailed
      title: Payment Failed Event
      summary: Published when payment fails
      contentType: application/json
      payload:
        $ref: '#/components/schemas/PaymentFailedPayload'
    
    SettlementBatchCreated:
      name: SettlementBatchCreated
      title: Settlement Batch Created Event
      summary: Published when settlement batch is created
      contentType: application/json
      payload:
        $ref: '#/components/schemas/SettlementBatchCreatedPayload'
    
    SettlementCompleted:
      name: SettlementCompleted
      title: Settlement Completed Event
      summary: Published when settlement completes
      contentType: application/json
      payload:
        $ref: '#/components/schemas/SettlementCompletedPayload'
    
    SagaStarted:
      name: SagaStarted
      title: Saga Started Event
      summary: Published when saga orchestration starts
      contentType: application/json
      payload:
        $ref: '#/components/schemas/SagaStartedPayload'
    
    SagaStepCompleted:
      name: SagaStepCompleted
      title: Saga Step Completed Event
      summary: Published when a saga step completes
      contentType: application/json
      payload:
        $ref: '#/components/schemas/SagaStepCompletedPayload'
    
    SagaCompensating:
      name: SagaCompensating
      title: Saga Compensating Event
      summary: Published when saga enters compensation phase
      contentType: application/json
      payload:
        $ref: '#/components/schemas/SagaCompensatingPayload'
    
    SagaCompleted:
      name: SagaCompleted
      title: Saga Completed Event
      summary: Published when saga completes
      contentType: application/json
      payload:
        $ref: '#/components/schemas/SagaCompletedPayload'
    
    LimitConsumed:
      name: LimitConsumed
      title: Limit Consumed Event
      summary: Published when customer limit is consumed after successful payment
      contentType: application/json
      payload:
        $ref: '#/components/schemas/LimitConsumedPayload'
    
    LimitReleased:
      name: LimitReleased
      title: Limit Released Event
      summary: Published when reserved limit is released (payment failed/cancelled)
      contentType: application/json
      payload:
        $ref: '#/components/schemas/LimitReleasedPayload'

  schemas:
    EventMetadata:
      type: object
      required:
        - eventId
        - eventType
        - timestamp
        - correlationId
        - source
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
          description: Event schema version
          default: "1.0"
    
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
              pattern: '^PAY-\d{4}-[A-Z0-9]{10}$'
            idempotencyKey:
              type: string
              format: uuid
            sourceAccount:
              type: string
              pattern: '^\d{10,20}$'
            destinationAccount:
              type: string
              pattern: '^\d{10,20}$'
            amount:
              type: number
              format: double
              minimum: 0.01
              maximum: 999999999.99
            currency:
              type: string
              enum: [ZAR]
            reference:
              type: string
              maxLength: 200
            paymentType:
              type: string
              enum: [EFT, RTC, RTGS, DEBIT_ORDER]
            initiatedBy:
              type: string
            initiatedAt:
              type: string
              format: date-time
    
    PaymentValidatedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - paymentId
            - validationStatus
          properties:
            paymentId:
              type: string
            validationStatus:
              type: string
              enum: [VALID, INVALID]
            validationRules:
              type: array
              items:
                type: object
                properties:
                  ruleId:
                    type: string
                  ruleName:
                    type: string
                  result:
                    type: string
                    enum: [PASS, FAIL]
            fraudScore:
              type: number
              format: double
              minimum: 0.0
              maximum: 1.0
            riskLevel:
              type: string
              enum: [LOW, MEDIUM, HIGH, CRITICAL]
            validatedAt:
              type: string
              format: date-time
    
    ValidationFailedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - paymentId
            - failureReasons
          properties:
            paymentId:
              type: string
            failureReasons:
              type: array
              items:
                type: object
                properties:
                  code:
                    type: string
                  message:
                    type: string
                  field:
                    type: string
            fraudScore:
              type: number
              format: double
            failedAt:
              type: string
              format: date-time
    
    FundsReservedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - holdId
            - accountNumber
            - amount
            - reference
          properties:
            holdId:
              type: string
            accountNumber:
              type: string
            amount:
              type: number
              format: double
            reference:
              type: string
            expiresAt:
              type: string
              format: date-time
            reservedAt:
              type: string
              format: date-time
    
    InsufficientFundsPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - paymentId
            - accountNumber
            - requestedAmount
            - availableBalance
          properties:
            paymentId:
              type: string
            accountNumber:
              type: string
            requestedAmount:
              type: number
              format: double
            availableBalance:
              type: number
              format: double
            shortfall:
              type: number
              format: double
    
    RoutingDeterminedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - paymentId
            - clearingSystem
            - channel
          properties:
            paymentId:
              type: string
            clearingSystem:
              type: string
              enum: [SAMOS, BANKSERV_ACH, BANKSERV_RTC, SASWITCH]
            channel:
              type: string
            estimatedCompletionTime:
              type: string
              format: date-time
            routingReason:
              type: string
            determinedAt:
              type: string
              format: date-time
    
    TransactionCreatedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - transactionId
            - paymentId
            - debitAccount
            - creditAccount
            - amount
          properties:
            transactionId:
              type: string
              pattern: '^TXN-\d{4}-[A-Z0-9]{10}$'
            paymentId:
              type: string
            debitAccount:
              type: string
            creditAccount:
              type: string
            amount:
              type: number
              format: double
            currency:
              type: string
            transactionType:
              type: string
            createdAt:
              type: string
              format: date-time
    
    TransactionProcessingPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - transactionId
            - status
          properties:
            transactionId:
              type: string
            status:
              type: string
              enum: [PROCESSING]
            processingStartedAt:
              type: string
              format: date-time
    
    ClearingSubmittedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - clearingReference
            - transactionId
            - clearingSystem
          properties:
            clearingReference:
              type: string
            transactionId:
              type: string
            clearingSystem:
              type: string
            submittedAt:
              type: string
              format: date-time
    
    ClearingAcknowledgedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - clearingReference
            - transactionId
          properties:
            clearingReference:
              type: string
            transactionId:
              type: string
            acknowledgedAt:
              type: string
              format: date-time
    
    ClearingCompletedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - clearingReference
            - transactionId
            - clearingStatus
          properties:
            clearingReference:
              type: string
            transactionId:
              type: string
            clearingStatus:
              type: string
              enum: [COMPLETED, SETTLED]
            settlementDetails:
              type: object
              properties:
                settlementDate:
                  type: string
                  format: date
                settlementAmount:
                  type: number
                  format: double
            completedAt:
              type: string
              format: date-time
    
    ClearingFailedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - clearingReference
            - transactionId
            - failureReason
          properties:
            clearingReference:
              type: string
            transactionId:
              type: string
            failureReason:
              type: string
            errorCode:
              type: string
            failedAt:
              type: string
              format: date-time
    
    PaymentCompletedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - paymentId
            - transactionId
            - finalStatus
          properties:
            paymentId:
              type: string
            transactionId:
              type: string
            finalStatus:
              type: string
              enum: [COMPLETED, SETTLED]
            completedAt:
              type: string
              format: date-time
    
    PaymentFailedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - paymentId
            - failureStage
            - failureReason
          properties:
            paymentId:
              type: string
            transactionId:
              type: string
            failureStage:
              type: string
              enum: [VALIDATION, RESERVATION, ROUTING, PROCESSING, CLEARING]
            failureReason:
              type: string
            errorCode:
              type: string
            failedAt:
              type: string
              format: date-time
    
    SettlementBatchCreatedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - batchId
            - batchDate
            - clearingSystem
          properties:
            batchId:
              type: string
            batchDate:
              type: string
              format: date
            clearingSystem:
              type: string
            transactionCount:
              type: integer
            totalAmount:
              type: number
              format: double
            createdAt:
              type: string
              format: date-time
    
    SettlementCompletedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - batchId
            - settlementStatus
          properties:
            batchId:
              type: string
            settlementStatus:
              type: string
              enum: [COMPLETED, PARTIALLY_SETTLED]
            settledCount:
              type: integer
            failedCount:
              type: integer
            totalSettledAmount:
              type: number
              format: double
            completedAt:
              type: string
              format: date-time
    
    SagaStartedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - sagaId
            - sagaType
          properties:
            sagaId:
              type: string
            sagaType:
              type: string
              enum: [PAYMENT_SAGA, SETTLEMENT_SAGA]
            sagaPayload:
              type: object
            startedAt:
              type: string
              format: date-time
    
    SagaStepCompletedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - sagaId
            - stepName
            - stepStatus
          properties:
            sagaId:
              type: string
            stepName:
              type: string
            stepStatus:
              type: string
              enum: [COMPLETED, FAILED]
            stepResult:
              type: object
            completedAt:
              type: string
              format: date-time
    
    SagaCompensatingPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - sagaId
            - compensationReason
          properties:
            sagaId:
              type: string
            compensationReason:
              type: string
            failedStep:
              type: string
            compensationStartedAt:
              type: string
              format: date-time
    
    SagaCompletedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - sagaId
            - finalStatus
          properties:
            sagaId:
              type: string
            finalStatus:
              type: string
              enum: [COMPLETED, COMPENSATED, FAILED]
            completedSteps:
              type: array
              items:
                type: string
            completedAt:
              type: string
              format: date-time
    
    LimitConsumedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - customerId
            - paymentId
            - amount
            - paymentType
          properties:
            customerId:
              type: string
            paymentId:
              type: string
            amount:
              type: number
              format: double
            paymentType:
              type: string
              enum: [EFT, RTC, RTGS, DEBIT_ORDER, CARD]
            reservationId:
              type: string
            dailyUsedBefore:
              type: number
              format: double
            dailyUsedAfter:
              type: number
              format: double
            monthlyUsedBefore:
              type: number
              format: double
            monthlyUsedAfter:
              type: number
              format: double
            dailyAvailable:
              type: number
              format: double
            monthlyAvailable:
              type: number
              format: double
            consumedAt:
              type: string
              format: date-time
    
    LimitReleasedPayload:
      allOf:
        - $ref: '#/components/schemas/EventMetadata'
        - type: object
          required:
            - customerId
            - paymentId
            - amount
            - reservationId
          properties:
            customerId:
              type: string
            paymentId:
              type: string
            amount:
              type: number
              format: double
            paymentType:
              type: string
              enum: [EFT, RTC, RTGS, DEBIT_ORDER, CARD]
            reservationId:
              type: string
            releaseReason:
              type: string
              enum: [PAYMENT_FAILED, PAYMENT_CANCELLED, VALIDATION_FAILED, TIMEOUT]
            releasedAt:
              type: string
              format: date-time
```

---

## Event Flow Diagram

### Happy Path: Successful Payment (with Limit Check)

```
1. PaymentInitiatedEvent
   ↓
2. PaymentValidatedEvent (includes limit reservation)
   ↓
3. FundsReservedEvent (hold on account)
   ↓
4. RoutingDeterminedEvent
   ↓
5. TransactionCreatedEvent
   ↓
6. TransactionProcessingEvent
   ↓
7. ClearingSubmittedEvent
   ↓
8. ClearingAcknowledgedEvent
   ↓
9. ClearingCompletedEvent
   ↓
10. LimitConsumedEvent (reservation converted to actual usage)
    ↓
11. SettlementCompletedEvent
    ↓
12. PaymentCompletedEvent
```

### Failure Path: Limit Exceeded

```
1. PaymentInitiatedEvent
   ↓
2. ValidationFailedEvent (DAILY_LIMIT_EXCEEDED / MONTHLY_LIMIT_EXCEEDED / PAYMENT_TYPE_LIMIT_EXCEEDED)
   ↓
3. PaymentFailedEvent
   (No limit reservation created, no compensation needed)
```

### Failure Path: Other Validation Failed

```
1. PaymentInitiatedEvent
   ↓
2. ValidationFailedEvent (KYC, FICA, Fraud, etc.)
   ↓
3. PaymentFailedEvent
```

### Compensation Path: Payment Failed After Validation

```
1-2. [Normal flow up to PaymentValidatedEvent - Limit Reserved]
   ↓
3-7. [Continue processing]
   ↓
8. ClearingFailedEvent
   ↓
9. SagaCompensatingEvent
   ↓
10. [Compensation steps:]
    - Release funds hold (FundsReleasedEvent)
    - Release limit reservation (LimitReleasedEvent)
    - Cancel transaction
    ↓
11. PaymentFailedEvent
```

---

## Event Subscription Matrix

| Service | Subscribes To | Publishes |
|---------|---------------|-----------|
| Payment Initiation | - | PaymentInitiatedEvent |
| Validation Service | PaymentInitiatedEvent | PaymentValidatedEvent, ValidationFailedEvent, LimitConsumedEvent, LimitReleasedEvent |
| Account Adapter | - | FundsReservedEvent, InsufficientFundsEvent |
| Routing Service | PaymentValidatedEvent | RoutingDeterminedEvent |
| Transaction Processing | RoutingDeterminedEvent | TransactionCreatedEvent, TransactionProcessingEvent |
| Clearing Adapters | TransactionCreatedEvent | ClearingSubmittedEvent, ClearingAcknowledgedEvent, ClearingCompletedEvent, ClearingFailedEvent |
| Settlement Service | ClearingCompletedEvent | SettlementBatchCreatedEvent, SettlementCompletedEvent |
| Notification Service | PaymentCompletedEvent, PaymentFailedEvent, ValidationFailedEvent (limit exceeded) | - |
| Saga Orchestrator | All Events | SagaStartedEvent, SagaStepCompletedEvent, SagaCompensatingEvent, SagaCompletedEvent |
| Audit Service | All Events | - |

---

## Event Handling Best Practices

### 1. Idempotency
All event consumers MUST be idempotent. Use `eventId` for deduplication.

```java
@Service
public class PaymentEventConsumer {
    
    @Transactional
    public void handlePaymentInitiated(PaymentInitiatedEvent event) {
        // Check if already processed
        if (eventRepository.existsById(event.getEventId())) {
            log.info("Event already processed: {}", event.getEventId());
            return;
        }
        
        // Process event
        processPayment(event);
        
        // Mark as processed
        eventRepository.save(new ProcessedEvent(event.getEventId()));
    }
}
```

### 2. Error Handling
Use Dead Letter Queue (DLQ) for failed events.

```yaml
# Azure Service Bus Topic Configuration
topic:
  name: payment/initiated
  subscription:
    name: validation-service-sub
    maxDeliveryCount: 3
    lockDuration: PT5M
    enableDeadLetteringOnMessageExpiration: true
```

### 3. Event Versioning
Include version in event schema. Support backward compatibility.

```java
public interface EventHandler<T extends Event> {
    String supportedVersion();
    void handle(T event);
}

// Multiple handlers for different versions
@Component
public class PaymentInitiatedV1Handler implements EventHandler<PaymentInitiatedEventV1> {
    public String supportedVersion() { return "1.0"; }
    // ...
}

@Component
public class PaymentInitiatedV2Handler implements EventHandler<PaymentInitiatedEventV2> {
    public String supportedVersion() { return "2.0"; }
    // ...
}
```

### 4. Correlation and Causation
Always include `correlationId` and `causationId` for tracing.

```java
public class EventBuilder {
    public static <T> T buildEvent(T payload, String correlationId, String causationId) {
        EventMetadata metadata = new EventMetadata();
        metadata.setEventId(UUID.randomUUID().toString());
        metadata.setCorrelationId(correlationId);
        metadata.setCausationId(causationId);
        metadata.setTimestamp(Instant.now());
        metadata.setSource(ServiceName.getCurrentService());
        // Attach metadata to payload
        return payload;
    }
}
```

### 5. Event Retention
Configure event retention based on compliance requirements.

```yaml
# 7 years retention for audit
topic:
  name: audit/events
  defaultMessageTimeToLive: P2557D  # 7 years

# 30 days for operational events
topic:
  name: payment/initiated
  defaultMessageTimeToLive: P30D
```

---

## Testing Events

### Unit Test Example

```java
@Test
public void shouldPublishPaymentInitiatedEvent() {
    // Arrange
    PaymentRequest request = createPaymentRequest();
    
    // Act
    paymentService.initiatePayment(request);
    
    // Assert
    verify(eventPublisher).publish(argThat(event -> 
        event instanceof PaymentInitiatedEvent &&
        ((PaymentInitiatedEvent) event).getPaymentId() != null
    ));
}
```

### Integration Test Example

```java
@SpringBootTest
@TestPropertySource(properties = "spring.cloud.azure.servicebus.namespace=test")
public class EventIntegrationTest {
    
    @Autowired
    private EventPublisher publisher;
    
    @Autowired
    private EventConsumer consumer;
    
    @Test
    public void shouldConsumePublishedEvent() throws Exception {
        // Publish event
        PaymentInitiatedEvent event = createEvent();
        publisher.publish(event);
        
        // Wait for consumption
        await().atMost(5, SECONDS).until(() -> 
            consumer.getProcessedEvents().contains(event.getEventId())
        );
    }
}
```

---

## Event Monitoring

### Key Metrics
1. **Event Publishing Rate**: Events/second per topic
2. **Event Processing Latency**: Time from publish to consume
3. **Dead Letter Queue Size**: Number of failed events
4. **Event Processing Errors**: Error rate per consumer

### Azure Monitor Queries

```kusto
// Events per topic per minute
ServiceBusMessages
| where TimeGenerated > ago(1h)
| summarize Count=count() by bin(TimeGenerated, 1m), Topic
| render timechart

// Processing latency
ServiceBusMessages
| extend Latency = datetime_diff('millisecond', ProcessedTime, EnqueuedTime)
| summarize avg(Latency), max(Latency), percentile(Latency, 95) by Topic

// Dead letter messages
ServiceBusMessages
| where DeadLettered == true
| summarize Count=count() by Topic, ErrorReason
```

---

**Next**: See `04-API-CONTRACTS.md` for REST API specifications
**Next**: See `05-DATABASE-SCHEMAS.md` for database designs
