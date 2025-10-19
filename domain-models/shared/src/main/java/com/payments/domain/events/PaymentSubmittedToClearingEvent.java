package com.payments.domain.events;

import com.payments.domain.shared.*;
import com.payments.domain.valueobjects.ClearingSystemReference;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Domain event for payment submitted to clearing
 */
@Data
@Builder
public class PaymentSubmittedToClearingEvent implements DomainEvent {
    
    private PaymentId paymentId;
    private TenantId tenantId;
    private Money amount;
    private ClearingSystemReference clearingReference;
    private Instant occurredAt;
    
    public PaymentSubmittedToClearingEvent(PaymentId paymentId, TenantId tenantId, Money amount, ClearingSystemReference clearingReference) {
        this.paymentId = paymentId;
        this.tenantId = tenantId;
        this.amount = amount;
        this.clearingReference = clearingReference;
        this.occurredAt = Instant.now();
    }
    
    public PaymentSubmittedToClearingEvent(PaymentId paymentId, TenantId tenantId, Money amount, ClearingSystemReference clearingReference, Instant occurredAt) {
        this.paymentId = paymentId;
        this.tenantId = tenantId;
        this.amount = amount;
        this.clearingReference = clearingReference;
        this.occurredAt = occurredAt;
    }
    
    @Override
    public String getEventType() {
        return "PaymentSubmittedToClearing";
    }
    
    @Override
    public Instant getOccurredAt() {
        return this.occurredAt;
    }
}
