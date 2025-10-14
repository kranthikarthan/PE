package com.payments.domain.account;

import com.payments.domain.shared.*;
import lombok.*;
import java.time.Instant;

/**
 * Domain Event: Account Adapter Created
 */
@Value
@AllArgsConstructor
public class AccountAdapterCreatedEvent implements DomainEvent {
    AccountAdapterId adapterId;
    String adapterName;
    AdapterType adapterType;
    Instant createdAt;
    
    @Override
    public String getEventType() {
        return "AccountAdapterCreated";
    }
    
    @Override
    public Instant getOccurredAt() {
        return createdAt;
    }
}
