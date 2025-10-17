package com.payments.bankservafricaadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain event for BankservAfrica ISO 8583 message addition
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankservAfricaIso8583MessageAddedEvent implements DomainEvent {
    
    private ClearingAdapterId adapterId;
    private String messageId;
    private String transactionId;
    private Instant occurredAt;
    
    @Override
    public String getEventType() {
        return "BankservAfricaIso8583MessageAdded";
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
