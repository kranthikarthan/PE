# Payments Engine Contracts

This module contains shared contracts, DTOs, and event schemas for the Payments Engine Phase 1 services.

## Overview

The contracts module provides a centralized location for all shared data structures used across the Payments Engine services. This ensures consistency and reduces duplication across services.

## Module Structure

```
contracts/
├── src/main/java/com/payments/contracts/
│   ├── events/           # Domain events and event DTOs
│   ├── payment/          # Payment-related DTOs
│   ├── validation/       # Validation-related DTOs
│   ├── account/          # Account-related DTOs
│   ├── routing/          # Routing-related DTOs
│   ├── transaction/      # Transaction-related DTOs
│   ├── saga/            # Saga-related DTOs
│   └── shared/          # Shared value objects and enums
```

## Event Contracts

### Base Event Structure

All domain events extend from `BaseEvent` which provides:
- Event identification (eventId, correlationId, causationId)
- Event metadata (source, version, timestamp)
- Tenant context (tenantId, businessUnitId)

### Payment Events

- **PaymentInitiatedEvent**: Published when a payment is initiated
- **PaymentValidatedEvent**: Published when a payment is validated
- **PaymentSubmittedToClearingEvent**: Published when a payment is submitted to clearing
- **PaymentClearedEvent**: Published when a payment is cleared
- **PaymentCompletedEvent**: Published when a payment is completed
- **PaymentFailedEvent**: Published when a payment fails

### Transaction Events

- **TransactionCreatedEvent**: Published when a transaction is created
- **TransactionCompletedEvent**: Published when a transaction is completed

### Saga Events

- **SagaStartedEvent**: Published when a saga orchestration starts
- **SagaStepCompletedEvent**: Published when a saga step completes
- **SagaCompletedEvent**: Published when a saga orchestration completes
- **SagaCompensatedEvent**: Published when a saga compensation completes

## Service DTOs

### Payment Service DTOs

- **PaymentInitiationRequest**: Request for initiating a payment
- **PaymentInitiationResponse**: Response for payment initiation
- **PaymentStatus**: Payment status enumeration
- **PaymentType**: Payment type enumeration
- **Priority**: Payment priority enumeration

### Validation Service DTOs

- **ValidationRequest**: Request for payment validation
- **ValidationResponse**: Response for payment validation
- **ValidationStatus**: Validation status enumeration
- **RiskLevel**: Risk level enumeration
- **RuleType**: Rule type enumeration
- **FailedRule**: Failed rule DTO
- **ValidationResponseDto**: Event DTO for validation response
- **FailedRuleDto**: Event DTO for failed rules

### Account Adapter DTOs

- **AccountValidationRequest**: Request for account validation
- **AccountValidationResponse**: Response for account validation
- **AccountBalanceRequest**: Request for account balance
- **AccountBalanceResponse**: Response for account balance

### Routing Service DTOs

- **RoutingRequest**: Request for payment routing
- **RoutingResponse**: Response for payment routing

### Transaction Processing DTOs

- **CreateTransactionRequest**: Request for creating a transaction
- **TransactionResponse**: Response for transaction operations

## Shared Value Objects

### Money

Represents monetary amounts with currency:
- Amount value (BigDecimal)
- Currency code (Currency)
- Validation rules
- Helper methods (isPositive, isZero, isNegative)

### TenantContext

Represents tenant and business unit context:
- Tenant identification
- Business unit identification
- Context metadata
- Factory methods for common use cases

## Enums

### TransactionType

Defines the types of transactions supported:
- PAYMENT, TRANSFER, ADJUSTMENT, REVERSAL, REFUND, FEE, INTEREST, SETTLEMENT

### TransactionStatus

Defines the possible statuses of a transaction:
- CREATED, PROCESSING, COMPLETED, FAILED, CANCELLED, PENDING, REVERSED

### SagaStatus

Defines the possible statuses of a saga orchestration:
- STARTED, RUNNING, COMPLETED, FAILED, COMPENSATED, CANCELLED, TIMEOUT

## Usage

### Adding Dependencies

Add the contracts module as a dependency in your service's `pom.xml`:

```xml
<dependency>
    <groupId>com.payments</groupId>
    <artifactId>contracts</artifactId>
    <version>${project.version}</version>
</dependency>
```

### Using Contracts

Import the contracts in your service code:

```java
import com.payments.contracts.payment.PaymentInitiationRequest;
import com.payments.contracts.events.PaymentInitiatedEvent;
import com.payments.contracts.shared.Money;
import com.payments.contracts.shared.TenantContext;
```

### Event Publishing

Use the event contracts for publishing domain events:

```java
PaymentInitiatedEvent event = PaymentInitiatedEvent.builder()
    .eventId(UUID.randomUUID())
    .eventType("PaymentInitiated")
    .timestamp(Instant.now())
    .correlationId(correlationId)
    .source("payment-initiation-service")
    .version("1.0.0")
    .tenantId(tenantId)
    .businessUnitId(businessUnitId)
    .paymentId(paymentId)
    .tenantContext(tenantContext)
    .amount(amount)
    .sourceAccount(sourceAccount)
    .destinationAccount(destinationAccount)
    .paymentType(paymentType)
    .initiatedAt(initiatedAt)
    .build();
```

## Validation

All DTOs include Jakarta validation annotations for:
- Required fields (@NotNull, @NotBlank)
- Field constraints (@Size, @Positive, @DecimalMin)
- Custom validation rules

## OpenAPI Documentation

All DTOs include OpenAPI annotations for:
- Schema descriptions
- Field documentation
- Example values
- Required field indicators

## Versioning

The contracts module follows semantic versioning:
- **Major version**: Breaking changes to contracts
- **Minor version**: New contracts or non-breaking changes
- **Patch version**: Bug fixes and documentation updates

## Best Practices

1. **Immutable DTOs**: Use `@Builder` pattern for immutable DTOs
2. **Validation**: Include appropriate validation annotations
3. **Documentation**: Add comprehensive OpenAPI documentation
4. **Consistency**: Follow consistent naming and structure patterns
5. **Backward Compatibility**: Maintain backward compatibility when possible

## Contributing

When adding new contracts:

1. Follow the existing package structure
2. Include comprehensive validation annotations
3. Add OpenAPI documentation
4. Include unit tests for validation
5. Update this README with new contracts

## Dependencies

- **Spring Boot**: Validation and OpenAPI support
- **Jackson**: JSON serialization
- **Lombok**: Boilerplate reduction
- **Jakarta Validation**: Bean validation
- **OpenAPI**: API documentation
