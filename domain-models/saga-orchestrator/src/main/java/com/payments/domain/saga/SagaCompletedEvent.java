package com.payments.domain.saga;

import com.payments.domain.shared.*;
import lombok.*;
import java.time.Instant;

/**
 * Domain Event: Saga Completed
 */
@Value
@AllArgsConstructor
public class SagaCompletedEvent implements DomainEvent {
    SagaId sagaId;
    String sagaName;
    Instant completedAt;
    
    @Override
    public String getEventType() {
        return "SagaCompleted";
    }
    
    @Override
    public Instant getOccurredAt() {
        return completedAt;
    }
}
