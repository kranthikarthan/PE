package com.payments.bankservafricaadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain event for BankservAfrica adapter creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankservAfricaAdapterCreatedEvent implements DomainEvent {
    
    private ClearingAdapterId adapterId;
    private String adapterName;
    private ClearingNetwork network;
    private Instant createdAt;
    
    @Override
    public String getEventType() {
        return "BankservAfricaAdapterCreated";
    }
    
    @Override
    public Instant getOccurredAt() {
        return createdAt;
    }
}
