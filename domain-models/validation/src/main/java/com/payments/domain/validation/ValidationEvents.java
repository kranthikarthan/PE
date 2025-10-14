package com.payments.domain.validation;

import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import lombok.AllArgsConstructor;
import lombok.Value;
import java.time.Instant;
import java.util.List;

@Value
@AllArgsConstructor
class PaymentValidatedEvent implements DomainEvent {
    PaymentId paymentId;
    TenantContext tenantContext;
    ValidationResult validationResult;

    @Override public String getEventType() { return "PaymentValidated"; }
    @Override public Instant getOccurredAt() { return Instant.now(); }
}

@Value
@AllArgsConstructor
class ValidationFailedEvent implements DomainEvent {
    PaymentId paymentId;
    TenantContext tenantContext;
    List<FailedRule> failedRules;

    @Override public String getEventType() { return "ValidationFailed"; }
    @Override public Instant getOccurredAt() { return Instant.now(); }
}


