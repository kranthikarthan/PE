package com.payments.domain.payment;

import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import lombok.AllArgsConstructor;
import lombok.Value;
import java.time.Instant;

@Value
@AllArgsConstructor
class PaymentInitiatedEvent implements DomainEvent {
    PaymentId paymentId;
    TenantContext tenantContext;
    com.payments.domain.shared.Money amount;
    com.payments.domain.shared.AccountNumber sourceAccount;
    com.payments.domain.shared.AccountNumber destinationAccount;
    PaymentType paymentType;
    Instant initiatedAt;

    @Override public String getEventType() { return "PaymentInitiated"; }
    @Override public Instant getOccurredAt() { return initiatedAt; }
}

@Value
@AllArgsConstructor
class PaymentValidatedEvent implements DomainEvent {
    PaymentId paymentId;
    TenantContext tenantContext;
    com.payments.domain.validation.ValidationResult validationResult;

    @Override public String getEventType() { return "PaymentValidated"; }
    @Override public Instant getOccurredAt() { return Instant.now(); }
}

@Value
@AllArgsConstructor
class PaymentSubmittedToClearingEvent implements DomainEvent {
    PaymentId paymentId;
    TenantContext tenantContext;
    ClearingSystemReference clearingSystemReference;

    @Override public String getEventType() { return "PaymentSubmittedToClearing"; }
    @Override public Instant getOccurredAt() { return Instant.now(); }
}

@Value
@AllArgsConstructor
class PaymentClearedEvent implements DomainEvent {
    PaymentId paymentId;
    TenantContext tenantContext;
    ClearingConfirmation clearingConfirmation;

    @Override public String getEventType() { return "PaymentCleared"; }
    @Override public Instant getOccurredAt() { return Instant.now(); }
}

@Value
@AllArgsConstructor
class PaymentCompletedEvent implements DomainEvent {
    PaymentId paymentId;
    TenantContext tenantContext;
    com.payments.domain.shared.Money amount;
    Instant completedAt;

    @Override public String getEventType() { return "PaymentCompleted"; }
    @Override public Instant getOccurredAt() { return completedAt; }
}

@Value
@AllArgsConstructor
class PaymentFailedEvent implements DomainEvent {
    PaymentId paymentId;
    TenantContext tenantContext;
    String reason;
    PaymentStatus previousStatus;

    @Override public String getEventType() { return "PaymentFailed"; }
    @Override public Instant getOccurredAt() { return Instant.now(); }
}


