package com.payments.domain.saga;

import com.payments.domain.shared.*;
import lombok.*;
import java.time.Instant;

/**
 * Domain Event: Saga Step Completed
 */
@Value
@AllArgsConstructor
public class SagaStepCompletedEvent implements DomainEvent {
    SagaId sagaId;
    SagaStepId stepId;
    String stepName;
    String result;
    
    @Override
    public String getEventType() {
        return "SagaStepCompleted";
    }
    
    @Override
    public Instant getOccurredAt() {
        return Instant.now();
    }
}
