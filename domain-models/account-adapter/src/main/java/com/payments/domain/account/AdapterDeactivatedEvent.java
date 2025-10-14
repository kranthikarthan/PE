package com.payments.domain.account;

import com.payments.domain.shared.*;
import lombok.*;
import java.time.Instant;

/**
 * Domain Event: Adapter Deactivated
 */
@Value
@AllArgsConstructor
public class AdapterDeactivatedEvent implements DomainEvent {
    AccountAdapterId adapterId;
    String reason;
    String deactivatedBy;
    
    @Override
    public String getEventType() {
        return "AdapterDeactivated";
    }
    
    @Override
    public Instant getOccurredAt() {
        return Instant.now();
    }
}
