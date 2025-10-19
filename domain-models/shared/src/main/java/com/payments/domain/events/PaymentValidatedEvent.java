package com.payments.domain.events;

import com.payments.domain.shared.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Domain event for payment validation
 */
@Data
@Builder
public class PaymentValidatedEvent implements DomainEvent {
    
    private PaymentId paymentId;
    private TenantId tenantId;
    private Money amount;
    private Instant occurredAt;
    
    public PaymentValidatedEvent(PaymentId paymentId, TenantId tenantId, Money amount) {
        this.paymentId = paymentId;
        this.tenantId = tenantId;
        this.amount = amount;
        this.occurredAt = Instant.now();
    }
    
    public PaymentValidatedEvent(PaymentId paymentId, TenantId tenantId, Money amount, Instant occurredAt) {
        this.paymentId = paymentId;
        this.tenantId = tenantId;
        this.amount = amount;
        this.occurredAt = occurredAt;
    }
    
    @Override
    public String getEventType() {
        return "PaymentValidated";
    }
    
    @Override
    public Instant getOccurredAt() {
        return this.occurredAt;
    }
}
