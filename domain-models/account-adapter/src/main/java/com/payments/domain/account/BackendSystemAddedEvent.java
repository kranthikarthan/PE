package com.payments.domain.account;

import com.payments.domain.shared.*;
import lombok.*;
import java.time.Instant;

/**
 * Domain Event: Backend System Added
 */
@Value
@AllArgsConstructor
public class BackendSystemAddedEvent implements DomainEvent {
    AccountAdapterId adapterId;
    BackendSystemId systemId;
    String systemName;
    String systemType;
    
    @Override
    public String getEventType() {
        return "BackendSystemAdded";
    }
    
    @Override
    public Instant getOccurredAt() {
        return Instant.now();
    }
}
