package com.payments.domain.saga;

import com.payments.domain.shared.*;
import lombok.*;
import java.time.Instant;

/**
 * Domain Event: Saga Compensation Started
 */
@Value
@AllArgsConstructor
public class SagaCompensationStartedEvent implements DomainEvent {
    SagaId sagaId;
    SagaStepId stepId;
    String stepName;
    
    @Override
    public String getEventType() {
        return "SagaCompensationStarted";
    }
    
    @Override
    public Instant getOccurredAt() {
        return Instant.now();
    }
}
