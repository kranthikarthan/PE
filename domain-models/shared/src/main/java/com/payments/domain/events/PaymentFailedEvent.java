package com.payments.domain.events;

import com.payments.domain.shared.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Domain event for payment failed
 */
@Data
@Builder
public class PaymentFailedEvent implements DomainEvent {
    
    private PaymentId paymentId;
    private TenantId tenantId;
    private Money amount;
    private String reason;
    private Instant occurredAt;
    
    public PaymentFailedEvent(PaymentId paymentId, TenantId tenantId, Money amount, String reason) {
        this.paymentId = paymentId;
        this.tenantId = tenantId;
        this.amount = amount;
        this.reason = reason;
        this.occurredAt = Instant.now();
    }
    
    public PaymentFailedEvent(PaymentId paymentId, TenantId tenantId, Money amount, String reason, Instant occurredAt) {
        this.paymentId = paymentId;
        this.tenantId = tenantId;
        this.amount = amount;
        this.reason = reason;
        this.occurredAt = occurredAt;
    }
    
    @Override
    public String getEventType() {
        return "PaymentFailed";
    }
    
    @Override
    public Instant getOccurredAt() {
        return this.occurredAt;
    }
}
