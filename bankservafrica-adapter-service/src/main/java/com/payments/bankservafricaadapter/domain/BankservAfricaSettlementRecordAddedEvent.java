package com.payments.bankservafricaadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Domain event for BankservAfrica settlement record addition
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankservAfricaSettlementRecordAddedEvent implements DomainEvent {
    
    private ClearingAdapterId adapterId;
    private String recordId;
    private LocalDate settlementDate;
    private Instant occurredAt;
    
    @Override
    public String getEventType() {
        return "BankservAfricaSettlementRecordAdded";
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
