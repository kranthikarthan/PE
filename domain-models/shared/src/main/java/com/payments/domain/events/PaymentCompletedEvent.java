package com.payments.domain.events;

import com.payments.domain.shared.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Domain event for payment completed
 */
@Data
@Builder
public class PaymentCompletedEvent implements DomainEvent {
    
    private PaymentId paymentId;
    private TenantId tenantId;
    private Money amount;
    private Instant occurredAt;
    
    public PaymentCompletedEvent(PaymentId paymentId, TenantId tenantId, Money amount) {
        this.paymentId = paymentId;
        this.tenantId = tenantId;
        this.amount = amount;
        this.occurredAt = Instant.now();
    }
    
    public PaymentCompletedEvent(PaymentId paymentId, TenantId tenantId, Money amount, Instant occurredAt) {
        this.paymentId = paymentId;
        this.tenantId = tenantId;
        this.amount = amount;
        this.occurredAt = occurredAt;
    }
    
    @Override
    public String getEventType() {
        return "PaymentCompleted";
    }
    
    @Override
    public Instant getOccurredAt() {
        return this.occurredAt;
    }
}
