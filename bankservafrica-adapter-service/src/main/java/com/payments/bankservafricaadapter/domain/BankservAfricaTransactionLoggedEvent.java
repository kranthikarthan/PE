package com.payments.bankservafricaadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain event for BankservAfrica transaction logging
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankservAfricaTransactionLoggedEvent implements DomainEvent {
    
    private ClearingAdapterId adapterId;
    private String transactionId;
    private String status;
    private Instant occurredAt;
    
    @Override
    public String getEventType() {
        return "BankservAfricaTransactionLogged";
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
