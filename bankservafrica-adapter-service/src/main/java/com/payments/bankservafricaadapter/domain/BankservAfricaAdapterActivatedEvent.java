package com.payments.bankservafricaadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain event for BankservAfrica adapter activation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankservAfricaAdapterActivatedEvent implements DomainEvent {
    
    private ClearingAdapterId adapterId;
    private String activatedBy;
    private Instant occurredAt;
    
    @Override
    public String getEventType() {
        return "BankservAfricaAdapterActivated";
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
