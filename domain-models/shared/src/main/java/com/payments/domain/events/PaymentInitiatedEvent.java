package com.payments.domain.events;

import com.payments.domain.shared.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Domain event for payment initiation
 */
@Data
@Builder
public class PaymentInitiatedEvent implements DomainEvent {
    
    private PaymentId paymentId;
    private TenantId tenantId;
    private Money amount;
    private Instant occurredAt;
    
    public PaymentInitiatedEvent(PaymentId paymentId, TenantId tenantId, Money amount) {
        this.paymentId = paymentId;
        this.tenantId = tenantId;
        this.amount = amount;
        this.occurredAt = Instant.now();
    }
    
    public PaymentInitiatedEvent(PaymentId paymentId, TenantId tenantId, Money amount, Instant occurredAt) {
        this.paymentId = paymentId;
        this.tenantId = tenantId;
        this.amount = amount;
        this.occurredAt = occurredAt;
    }
    
    @Override
    public String getEventType() {
        return "PaymentInitiated";
    }
    
    @Override
    public Instant getOccurredAt() {
        return this.occurredAt;
    }
}
