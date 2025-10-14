package com.payments.domain.saga;

import com.payments.domain.shared.*;
import lombok.*;
import java.time.Instant;

/**
 * Domain Event: Saga Step Added
 */
@Value
@AllArgsConstructor
public class SagaStepAddedEvent implements DomainEvent {
    SagaId sagaId;
    SagaStepId stepId;
    String stepName;
    String serviceName;
    
    @Override
    public String getEventType() {
        return "SagaStepAdded";
    }
    
    @Override
    public Instant getOccurredAt() {
        return Instant.now();
    }
}
