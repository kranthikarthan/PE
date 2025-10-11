# Domain-Driven Design (DDD) Implementation Guide

## Overview

This document provides the **complete Domain-Driven Design implementation** for the Payments Engine. DDD formalizes the implicit bounded contexts you already have and adds strategic design patterns for managing complexity.

---

## Bounded Context Map

### Strategic Design

```
┌─────────────────────────────────────────────────────────────────┐
│                    PAYMENTS ENGINE SYSTEM                        │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  PAYMENT CONTEXT (Core Domain)                                   │
│  ────────────────────────────────────────                       │
│  Ubiquitous Language: Payment, Initiate, Validate, Clear       │
│  Aggregate: Payment                                              │
│  Services: Payment Initiation, Validation                       │
│  Events: PaymentInitiated, PaymentValidated, PaymentCompleted  │
└─────────────────────────────────────────────────────────────────┘
              │
              │ publishes events to
              ▼
┌─────────────────────────────────────────────────────────────────┐
│  CLEARING CONTEXT (Core Domain)                                  │
│  ────────────────────────────────                               │
│  Ubiquitous Language: Clear, Submit, Settle, Reconcile         │
│  Aggregate: ClearingBatch                                       │
│  Services: SAMOS Adapter, BankservAfrica Adapter, RTC Adapter  │
│  Events: ClearingSubmitted, ClearingCompleted                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  TENANT CONTEXT (Supporting Domain)                              │
│  ────────────────────────────────                               │
│  Ubiquitous Language: Tenant, Onboard, Configure, Quota        │
│  Aggregate: Tenant                                              │
│  Services: Tenant Management                                    │
│  Events: TenantOnboarded, ConfigChanged, QuotaExceeded        │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  ACCOUNT CONTEXT (Anti-Corruption Layer)                         │
│  ────────────────────────────────────────                       │
│  External: Core Banking Systems (8 systems)                    │
│  ACL: Account Adapter Service                                  │
│  Pattern: Customer-Supplier (we conform to their model)       │
│  Protection: Translates external models to domain models       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  FRAUD CONTEXT (Supporting Domain)                               │
│  ────────────────────────────────────                           │
│  External: Fraud Scoring API                                   │
│  ACL: Fraud Scoring Service (in Validation Service)           │
│  Pattern: Anti-Corruption Layer                               │
│  Protection: Protects from external API changes                │
└─────────────────────────────────────────────────────────────────┘
```

### Context Relationships

```
Payment Context --publishes--> Clearing Context
Payment Context --conforms--> Tenant Context
Payment Context --uses ACL--> Account Context
Payment Context --uses ACL--> Fraud Context

Clearing Context --publishes--> Settlement Context
Settlement Context --publishes--> Reconciliation Context
```

---

## 1. Payment Context (Core Domain)

### Bounded Context Definition

**Responsibility**: Manage the lifecycle of payments from initiation to completion.

**Ubiquitous Language**:
- **Payment**: A request to transfer money
- **Initiate**: Start a new payment
- **Validate**: Check if payment meets all requirements
- **Clear**: Submit payment to clearing system
- **Settle**: Finalize the payment
- **Fail**: Mark payment as failed
- **Compensate**: Rollback a payment

### Aggregates

#### Payment Aggregate (Root)

```java
package com.payments.domain.payment;

import com.payments.domain.shared.*;
import lombok.*;

/**
 * Payment Aggregate Root
 * 
 * Consistency Boundary: Payment + PaymentDetails + StatusHistory
 * Business Rules Enforced:
 * - Payment can only be initiated once
 * - Validated payments can be cleared
 * - Failed payments cannot be cleared
 * - Status transitions must be valid
 */
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
public class Payment {
    
    @EmbeddedId
    private PaymentId id;
    
    @Embedded
    private TenantContext tenantContext;
    
    @Embedded
    private Money amount;
    
    @Embedded
    private AccountNumber sourceAccount;
    
    @Embedded
    private AccountNumber destinationAccount;
    
    @Embedded
    private PaymentReference reference;
    
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    @Enumerated(EnumType.STRING)
    private Priority priority;
    
    private String initiatedBy;
    
    private Instant initiatedAt;
    
    private Instant completedAt;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "payment_id")
    private List<StatusChange> statusHistory = new ArrayList<>();
    
    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    // ─────────────────────────────────────────────────────────
    // FACTORY METHOD (Create new payment)
    // ─────────────────────────────────────────────────────────
    
    public static Payment initiate(
        PaymentId id,
        TenantContext tenantContext,
        Money amount,
        AccountNumber sourceAccount,
        AccountNumber destinationAccount,
        PaymentReference reference,
        PaymentType paymentType,
        Priority priority,
        String initiatedBy
    ) {
        // Business validation
        if (amount.isNegativeOrZero()) {
            throw new InvalidPaymentException("Amount must be positive");
        }
        
        if (sourceAccount.equals(destinationAccount)) {
            throw new InvalidPaymentException("Source and destination accounts must be different");
        }
        
        // Create payment
        Payment payment = new Payment();
        payment.id = id;
        payment.tenantContext = tenantContext;
        payment.amount = amount;
        payment.sourceAccount = sourceAccount;
        payment.destinationAccount = destinationAccount;
        payment.reference = reference;
        payment.paymentType = paymentType;
        payment.priority = priority;
        payment.initiatedBy = initiatedBy;
        payment.status = PaymentStatus.INITIATED;
        payment.initiatedAt = Instant.now();
        
        // Record status change
        payment.addStatusChange(null, PaymentStatus.INITIATED, "Payment initiated");
        
        // Domain event
        payment.registerEvent(new PaymentInitiatedEvent(
            payment.id,
            payment.tenantContext,
            payment.amount,
            payment.sourceAccount,
            payment.destinationAccount,
            payment.paymentType,
            payment.initiatedAt
        ));
        
        return payment;
    }
    
    // ─────────────────────────────────────────────────────────
    // BUSINESS METHODS (Behavior, not getters/setters!)
    // ─────────────────────────────────────────────────────────
    
    /**
     * Validate the payment
     * Precondition: Payment must be INITIATED
     */
    public void validate(ValidationResult validationResult) {
        // Guard: Can only validate INITIATED payments
        if (this.status != PaymentStatus.INITIATED) {
            throw new InvalidStateTransitionException(
                "Can only validate INITIATED payments. Current status: " + this.status
            );
        }
        
        if (validationResult.isValid()) {
            this.status = PaymentStatus.VALIDATED;
            addStatusChange(PaymentStatus.INITIATED, PaymentStatus.VALIDATED, 
                "Payment validated successfully");
            
            registerEvent(new PaymentValidatedEvent(
                this.id,
                this.tenantContext,
                validationResult
            ));
        } else {
            fail(validationResult.getReason());
        }
    }
    
    /**
     * Submit payment to clearing
     * Precondition: Payment must be VALIDATED
     */
    public void submitToClearing(ClearingSystemReference clearingRef) {
        // Guard: Can only clear VALIDATED payments
        if (this.status != PaymentStatus.VALIDATED) {
            throw new InvalidStateTransitionException(
                "Can only clear VALIDATED payments. Current status: " + this.status
            );
        }
        
        this.status = PaymentStatus.CLEARING;
        addStatusChange(PaymentStatus.VALIDATED, PaymentStatus.CLEARING, 
            "Submitted to clearing: " + clearingRef.getValue());
        
        registerEvent(new PaymentSubmittedToClearingEvent(
            this.id,
            this.tenantContext,
            clearingRef
        ));
    }
    
    /**
     * Mark payment as cleared (clearing confirmed)
     */
    public void markCleared(ClearingConfirmation confirmation) {
        // Guard: Payment must be in CLEARING status
        if (this.status != PaymentStatus.CLEARING) {
            throw new InvalidStateTransitionException(
                "Can only mark CLEARING payments as cleared. Current status: " + this.status
            );
        }
        
        this.status = PaymentStatus.CLEARED;
        addStatusChange(PaymentStatus.CLEARING, PaymentStatus.CLEARED, 
            "Clearing confirmed: " + confirmation.getConfirmationNumber());
        
        registerEvent(new PaymentClearedEvent(
            this.id,
            this.tenantContext,
            confirmation
        ));
    }
    
    /**
     * Complete the payment (final status)
     */
    public void complete() {
        // Guard: Payment must be CLEARED
        if (this.status != PaymentStatus.CLEARED) {
            throw new InvalidStateTransitionException(
                "Can only complete CLEARED payments. Current status: " + this.status
            );
        }
        
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = Instant.now();
        addStatusChange(PaymentStatus.CLEARED, PaymentStatus.COMPLETED, 
            "Payment completed successfully");
        
        registerEvent(new PaymentCompletedEvent(
            this.id,
            this.tenantContext,
            this.amount,
            this.completedAt
        ));
    }
    
    /**
     * Fail the payment
     * Can be called from any non-final state
     */
    public void fail(String reason) {
        // Guard: Cannot fail already completed/failed payments
        if (this.status == PaymentStatus.COMPLETED || this.status == PaymentStatus.FAILED) {
            throw new InvalidStateTransitionException(
                "Cannot fail payment in " + this.status + " status"
            );
        }
        
        PaymentStatus previousStatus = this.status;
        this.status = PaymentStatus.FAILED;
        addStatusChange(previousStatus, PaymentStatus.FAILED, reason);
        
        registerEvent(new PaymentFailedEvent(
            this.id,
            this.tenantContext,
            reason,
            previousStatus
        ));
    }
    
    // ─────────────────────────────────────────────────────────
    // QUERY METHODS (Read-only, expose state)
    // ─────────────────────────────────────────────────────────
    
    public boolean isInProgress() {
        return this.status != PaymentStatus.COMPLETED && 
               this.status != PaymentStatus.FAILED;
    }
    
    public boolean canBeCleared() {
        return this.status == PaymentStatus.VALIDATED;
    }
    
    public PaymentId getId() {
        return id;
    }
    
    public TenantContext getTenantContext() {
        return tenantContext;
    }
    
    public Money getAmount() {
        return amount; // Money is immutable, safe to return
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    // ─────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────
    
    private void addStatusChange(PaymentStatus from, PaymentStatus to, String reason) {
        statusHistory.add(new StatusChange(from, to, reason, Instant.now()));
    }
    
    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
}

/**
 * Status Change (Entity within Payment Aggregate)
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class StatusChange {
    @Enumerated(EnumType.STRING)
    private PaymentStatus fromStatus;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus toStatus;
    
    private String reason;
    
    private Instant changedAt;
}
```

### Value Objects

```java
package com.payments.domain.shared;

/**
 * Money - Value Object (Immutable)
 * 
 * Encapsulates amount and currency with business rules
 */
@Embeddable
@Value // Lombok: Immutable, equals/hashCode based on fields
public class Money {
    
    BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    Currency currency;
    
    // Private constructor - use factory methods
    private Money(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        
        // Store with consistent scale (2 decimal places)
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }
    
    // Factory methods
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }
    
    public static Money zar(BigDecimal amount) {
        return new Money(amount, Currency.ZAR);
    }
    
    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }
    
    // Business methods
    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money subtract(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }
    
    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier), this.currency);
    }
    
    public boolean isGreaterThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public boolean isLessThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }
    
    public boolean isNegativeOrZero() {
        return this.amount.compareTo(BigDecimal.ZERO) <= 0;
    }
    
    private void assertSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new CurrencyMismatchException(
                "Cannot operate on different currencies: " + 
                this.currency + " and " + other.currency
            );
        }
    }
}

/**
 * PaymentId - Value Object (Entity ID)
 */
@Embeddable
@Value
public class PaymentId {
    String value;
    
    private PaymentId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PaymentId cannot be null or blank");
        }
        this.value = value;
    }
    
    public static PaymentId of(String value) {
        return new PaymentId(value);
    }
    
    public static PaymentId generate() {
        return new PaymentId("PAY-" + UUID.randomUUID().toString());
    }
}

/**
 * AccountNumber - Value Object
 */
@Embeddable
@Value
public class AccountNumber {
    String value;
    
    private AccountNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AccountNumber cannot be null or blank");
        }
        // Validate format (South African account numbers are 11 digits)
        if (!value.matches("\\d{11}")) {
            throw new IllegalArgumentException(
                "Invalid account number format. Must be 11 digits"
            );
        }
        this.value = value;
    }
    
    public static AccountNumber of(String value) {
        return new AccountNumber(value);
    }
}

/**
 * PaymentReference - Value Object
 */
@Embeddable
@Value
public class PaymentReference {
    String value;
    
    private PaymentReference(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PaymentReference cannot be null or blank");
        }
        if (value.length() > 200) {
            throw new IllegalArgumentException("PaymentReference cannot exceed 200 characters");
        }
        this.value = value;
    }
    
    public static PaymentReference of(String value) {
        return new PaymentReference(value);
    }
}

/**
 * TenantContext - Value Object
 */
@Embeddable
@Value
public class TenantContext {
    String tenantId;
    String tenantName;
    String businessUnitId;
    String businessUnitName;
    
    public static TenantContext of(
        String tenantId, 
        String tenantName,
        String businessUnitId,
        String businessUnitName
    ) {
        return new TenantContext(tenantId, tenantName, businessUnitId, businessUnitName);
    }
}
```

### Domain Events

```java
package com.payments.domain.payment.events;

/**
 * Base Domain Event
 */
public interface DomainEvent {
    String getEventId();
    Instant getOccurredAt();
    String getEventType();
}

/**
 * PaymentInitiatedEvent
 */
@Value
public class PaymentInitiatedEvent implements DomainEvent {
    String eventId = UUID.randomUUID().toString();
    Instant occurredAt = Instant.now();
    String eventType = "PaymentInitiated";
    
    PaymentId paymentId;
    TenantContext tenantContext;
    Money amount;
    AccountNumber sourceAccount;
    AccountNumber destinationAccount;
    PaymentType paymentType;
    Instant initiatedAt;
}

/**
 * PaymentValidatedEvent
 */
@Value
public class PaymentValidatedEvent implements DomainEvent {
    String eventId = UUID.randomUUID().toString();
    Instant occurredAt = Instant.now();
    String eventType = "PaymentValidated";
    
    PaymentId paymentId;
    TenantContext tenantContext;
    ValidationResult validationResult;
}

// ... other domain events (PaymentFailedEvent, PaymentCompletedEvent, etc.)
```

### Repository (Port)

```java
package com.payments.domain.payment;

/**
 * Payment Repository - Port (Interface)
 * 
 * This is part of the domain layer.
 * Infrastructure provides the adapter (implementation).
 */
public interface PaymentRepository {
    
    /**
     * Save a payment (insert or update)
     */
    Payment save(Payment payment);
    
    /**
     * Find by payment ID
     */
    Optional<Payment> findById(PaymentId id);
    
    /**
     * Find by idempotency key
     */
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    
    /**
     * Find recent payments for customer
     */
    List<Payment> findRecentByCustomer(String customerId, int limit);
    
    /**
     * Find payments by status
     */
    List<Payment> findByStatus(PaymentStatus status);
}
```

### Domain Service

```java
package com.payments.domain.payment;

/**
 * Payment Domain Service
 * 
 * Contains domain logic that doesn't naturally fit in an aggregate.
 * Examples: Operations spanning multiple aggregates, complex calculations.
 */
@Service
public class PaymentDomainService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    /**
     * Check if duplicate payment exists (idempotency check)
     */
    public boolean isDuplicatePayment(String idempotencyKey) {
        return paymentRepository.findByIdempotencyKey(idempotencyKey).isPresent();
    }
    
    /**
     * Calculate total payment volume for customer (domain logic)
     */
    public Money calculateTotalVolume(String customerId, Period period) {
        List<Payment> payments = paymentRepository.findRecentByCustomer(
            customerId, 
            1000
        );
        
        return payments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
            .filter(p -> isWithinPeriod(p, period))
            .map(Payment::getAmount)
            .reduce(Money.zero(Currency.ZAR), Money::add);
    }
    
    private boolean isWithinPeriod(Payment payment, Period period) {
        // Period calculation logic
        return true;
    }
}
```

---

## 2. Anti-Corruption Layer (ACL)

### Core Banking ACL

```java
package com.payments.infrastructure.corebanking;

/**
 * Anti-Corruption Layer for Core Banking Systems
 * 
 * Protects our domain model from external system changes.
 * Translates external models to our domain models.
 */
@Service
@Slf4j
public class CoreBankingAntiCorruptionLayer {
    
    @Autowired
    private Map<String, CoreBankingClient> externalClients; // Injected by system type
    
    /**
     * Get account balance (translate external model to domain model)
     */
    public AccountBalance getAccountBalance(AccountNumber accountNumber) {
        // 1. Route to correct external system
        CoreBankingClient client = routeToSystem(accountNumber);
        
        // 2. Call external system
        ExternalAccountDTO externalAccount;
        try {
            externalAccount = client.getAccount(accountNumber.getValue());
        } catch (ExternalSystemException e) {
            log.error("External system call failed for account: {}", accountNumber, e);
            throw new CoreBankingUnavailableException("Core banking system unavailable", e);
        }
        
        // 3. Translate to domain model (PROTECTION FROM EXTERNAL CHANGES)
        return translateToDomainModel(externalAccount);
    }
    
    /**
     * Debit account (translate domain model to external model)
     */
    public DebitResult debitAccount(AccountNumber accountNumber, Money amount, PaymentReference reference) {
        CoreBankingClient client = routeToSystem(accountNumber);
        
        // Translate domain model to external request
        ExternalDebitRequest externalRequest = ExternalDebitRequest.builder()
            .accountNo(accountNumber.getValue())
            .amount(amount.getAmount().doubleValue()) // External system uses double
            .currency(translateCurrency(amount.getCurrency()))
            .reference(reference.getValue())
            .build();
        
        // Call external system
        ExternalDebitResponse externalResponse;
        try {
            externalResponse = client.debit(externalRequest);
        } catch (ExternalSystemException e) {
            log.error("Debit failed for account: {}", accountNumber, e);
            throw new DebitFailedException("Failed to debit account", e);
        }
        
        // Translate response to domain model
        return translateDebitResult(externalResponse);
    }
    
    // ─────────────────────────────────────────────────────────
    // TRANSLATION METHODS (Protect domain from external changes)
    // ─────────────────────────────────────────────────────────
    
    private AccountBalance translateToDomainModel(ExternalAccountDTO external) {
        // Translate external status codes to domain status
        AccountStatus domainStatus = translateStatus(external.getStatusCode());
        
        // Translate external amount format to domain Money
        Money balance = Money.of(
            BigDecimal.valueOf(external.getBalanceAmount()),
            Currency.ZAR
        );
        
        // Build domain model
        return AccountBalance.builder()
            .accountNumber(AccountNumber.of(external.getAcctNo()))
            .balance(balance)
            .availableBalance(Money.of(
                BigDecimal.valueOf(external.getAvailableBalance()),
                Currency.ZAR
            ))
            .status(domainStatus)
            .lastUpdated(parseExternalTimestamp(external.getLastUpdateTs()))
            .build();
    }
    
    private AccountStatus translateStatus(String externalStatusCode) {
        // Protect from external status code changes
        switch (externalStatusCode) {
            case "A":
            case "ACTIVE":
            case "01":
                return AccountStatus.ACTIVE;
                
            case "C":
            case "CLOSED":
            case "99":
                return AccountStatus.CLOSED;
                
            case "F":
            case "FROZEN":
            case "50":
                return AccountStatus.FROZEN;
                
            case "D":
            case "DORMANT":
            case "80":
                return AccountStatus.DORMANT;
                
            default:
                log.warn("Unknown external status code: {}. Treating as UNKNOWN", externalStatusCode);
                return AccountStatus.UNKNOWN;
        }
    }
    
    private String translateCurrency(Currency domainCurrency) {
        // External system might use different currency codes
        switch (domainCurrency) {
            case ZAR:
                return "710"; // External system uses ISO numeric code
            default:
                return domainCurrency.getCurrencyCode();
        }
    }
    
    private DebitResult translateDebitResult(ExternalDebitResponse external) {
        boolean success = "00".equals(external.getResponseCode()) || 
                         "SUCCESS".equals(external.getStatus());
        
        return DebitResult.builder()
            .success(success)
            .transactionReference(external.getTransactionId())
            .responseCode(external.getResponseCode())
            .responseMessage(translateResponseMessage(external.getResponseCode()))
            .build();
    }
    
    private String translateResponseMessage(String responseCode) {
        // Translate cryptic external codes to meaningful messages
        Map<String, String> codeMap = Map.of(
            "00", "Success",
            "01", "Insufficient funds",
            "03", "Invalid account",
            "05", "Account frozen",
            "51", "Insufficient funds",
            "91", "System unavailable"
        );
        
        return codeMap.getOrDefault(responseCode, "Unknown error: " + responseCode);
    }
}
```

### Fraud Scoring ACL

```java
package com.payments.infrastructure.fraud;

/**
 * Anti-Corruption Layer for Fraud Scoring API
 * 
 * Protects our domain from external fraud API changes.
 */
@Service
@Slf4j
public class FraudScoringAntiCorruptionLayer {
    
    @Autowired
    private FraudApiClient externalFraudClient;
    
    /**
     * Assess fraud risk (translate between domain and external models)
     */
    public FraudAssessment assessFraudRisk(Payment payment) {
        // 1. Translate domain model to external request
        ExternalFraudRequest externalRequest = buildExternalRequest(payment);
        
        // 2. Call external API
        ExternalFraudResponse externalResponse;
        try {
            externalResponse = externalFraudClient.assessRisk(externalRequest);
        } catch (Exception e) {
            log.error("Fraud API call failed for payment: {}", payment.getId(), e);
            // Fallback to rule-based detection
            return fallbackRuleBasedDetection(payment);
        }
        
        // 3. Translate external response to domain model
        return translateToDomainModel(externalResponse, payment);
    }
    
    private ExternalFraudRequest buildExternalRequest(Payment payment) {
        // External API has different data structure
        return ExternalFraudRequest.builder()
            .transactionId(payment.getId().getValue())
            .amount(payment.getAmount().getAmount().doubleValue())
            .currency(payment.getAmount().getCurrency().getCurrencyCode())
            .sourceAccount(payment.getSourceAccount().getValue())
            .destinationAccount(payment.getDestinationAccount().getValue())
            .transactionType(translatePaymentType(payment.getPaymentType()))
            .timestamp(payment.getInitiatedAt().toEpochMilli())
            .build();
    }
    
    private FraudAssessment translateToDomainModel(
        ExternalFraudResponse external, 
        Payment payment
    ) {
        // External API uses different risk levels
        FraudRiskLevel domainRiskLevel = translateRiskLevel(external.getRiskLevel());
        
        // External API uses different recommendation codes
        FraudRecommendation domainRecommendation = translateRecommendation(
            external.getRecommendationCode()
        );
        
        return FraudAssessment.builder()
            .paymentId(payment.getId())
            .riskScore(external.getScore())
            .riskLevel(domainRiskLevel)
            .recommendation(domainRecommendation)
            .reasons(translateReasons(external.getReasons()))
            .assessedAt(Instant.now())
            .build();
    }
    
    private FraudRiskLevel translateRiskLevel(String externalLevel) {
        // Protect from external API changes
        switch (externalLevel) {
            case "LOW":
            case "L":
            case "1":
                return FraudRiskLevel.LOW;
                
            case "MEDIUM":
            case "M":
            case "2":
                return FraudRiskLevel.MEDIUM;
                
            case "HIGH":
            case "H":
            case "3":
                return FraudRiskLevel.HIGH;
                
            case "CRITICAL":
            case "C":
            case "4":
                return FraudRiskLevel.CRITICAL;
                
            default:
                log.warn("Unknown risk level: {}. Treating as HIGH", externalLevel);
                return FraudRiskLevel.HIGH; // Fail safe
        }
    }
}
```

---

## 3. Application Service (Use Cases)

```java
package com.payments.application;

/**
 * Payment Application Service
 * 
 * Orchestrates use cases.
 * Translates DTOs to domain models.
 * Publishes domain events.
 */
@Service
@Transactional
@Slf4j
public class PaymentApplicationService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PaymentDomainService paymentDomainService;
    
    @Autowired
    private DomainEventPublisher eventPublisher;
    
    /**
     * Use Case: Initiate Payment
     */
    public PaymentResponse initiatePayment(InitiatePaymentCommand command) {
        log.info("Initiating payment: {}", command);
        
        // 1. Check idempotency
        if (paymentDomainService.isDuplicatePayment(command.getIdempotencyKey())) {
            Optional<Payment> existing = paymentRepository.findByIdempotencyKey(
                command.getIdempotencyKey()
            );
            return PaymentResponse.from(existing.get());
        }
        
        // 2. Translate command (DTO) to domain model
        PaymentId paymentId = PaymentId.generate();
        TenantContext tenantContext = TenantContext.of(
            command.getTenantId(),
            command.getTenantName(),
            command.getBusinessUnitId(),
            command.getBusinessUnitName()
        );
        Money amount = Money.zar(command.getAmount());
        AccountNumber sourceAccount = AccountNumber.of(command.getSourceAccount());
        AccountNumber destinationAccount = AccountNumber.of(command.getDestinationAccount());
        PaymentReference reference = PaymentReference.of(command.getReference());
        
        // 3. Create aggregate (business logic in domain)
        Payment payment = Payment.initiate(
            paymentId,
            tenantContext,
            amount,
            sourceAccount,
            destinationAccount,
            reference,
            command.getPaymentType(),
            command.getPriority(),
            command.getInitiatedBy()
        );
        
        // 4. Save aggregate
        payment = paymentRepository.save(payment);
        
        // 5. Publish domain events
        payment.getDomainEvents().forEach(eventPublisher::publish);
        payment.clearDomainEvents();
        
        log.info("Payment initiated: {}", payment.getId());
        
        // 6. Return response DTO
        return PaymentResponse.from(payment);
    }
    
    /**
     * Use Case: Validate Payment
     */
    public void validatePayment(ValidatePaymentCommand command) {
        log.info("Validating payment: {}", command.getPaymentId());
        
        // 1. Load aggregate
        Payment payment = paymentRepository.findById(command.getPaymentId())
            .orElseThrow(() -> new PaymentNotFoundException(command.getPaymentId()));
        
        // 2. Execute business logic (in aggregate)
        payment.validate(command.getValidationResult());
        
        // 3. Save aggregate
        payment = paymentRepository.save(payment);
        
        // 4. Publish domain events
        payment.getDomainEvents().forEach(eventPublisher::publish);
        payment.clearDomainEvents();
        
        log.info("Payment validated: {}", payment.getId());
    }
}
```

---

## 4. Summary

### What We've Formalized

✅ **Bounded Contexts**: Payment, Clearing, Tenant, Account (ACL), Fraud (ACL)  
✅ **Aggregates**: Payment (with clear consistency boundaries)  
✅ **Value Objects**: Money, PaymentId, AccountNumber, PaymentReference, TenantContext  
✅ **Domain Events**: PaymentInitiated, PaymentValidated, PaymentCompleted, PaymentFailed  
✅ **Repositories**: Ports (interfaces) for data access  
✅ **Domain Services**: Logic that doesn't fit in aggregates  
✅ **Anti-Corruption Layers**: Protect domain from external systems  
✅ **Application Services**: Orchestrate use cases  

### Key DDD Principles Applied

1. **Ubiquitous Language**: Same terms in code and business (Payment, Initiate, Clear, Settle)
2. **Aggregates**: Payment aggregate enforces invariants (status transitions, validation)
3. **Value Objects**: Immutable, no identity (Money, AccountNumber)
4. **Domain Events**: Capture business intent (PaymentInitiated, not "PaymentCreated")
5. **Anti-Corruption Layers**: Protect from external system changes
6. **Bounded Contexts**: Clear boundaries, explicit relationships

---

## Related Documents

- **[02-MICROSERVICES-BREAKDOWN.md](02-MICROSERVICES-BREAKDOWN.md)** - Microservices (map to bounded contexts)
- **[13-MODERN-ARCHITECTURE-PATTERNS.md](13-MODERN-ARCHITECTURE-PATTERNS.md)** - DDD pattern overview

---

**Last Updated**: 2025-10-11  
**Version**: 1.0
