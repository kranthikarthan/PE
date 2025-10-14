package com.payments.domain.saga;

import com.payments.domain.shared.*;
import lombok.*;
import java.time.Instant;

/**
 * Domain Event: Saga Event Added
 */
@Value
@AllArgsConstructor
public class SagaEventAddedEvent implements DomainEvent {
    SagaId sagaId;
    SagaEventId eventId;
    String eventType;
    String eventSource;
    
    @Override
    public String getEventType() {
        return "SagaEventAdded";
    }
    
    @Override
    public Instant getOccurredAt() {
        return Instant.now();
    }
}
