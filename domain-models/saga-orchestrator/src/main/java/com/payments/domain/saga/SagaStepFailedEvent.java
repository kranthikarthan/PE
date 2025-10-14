package com.payments.domain.saga;

import com.payments.domain.shared.*;
import lombok.*;
import java.time.Instant;

/**
 * Domain Event: Saga Step Failed
 */
@Value
@AllArgsConstructor
public class SagaStepFailedEvent implements DomainEvent {
    SagaId sagaId;
    SagaStepId stepId;
    String stepName;
    String failureReason;
    
    @Override
    public String getEventType() {
        return "SagaStepFailed";
    }
    
    @Override
    public Instant getOccurredAt() {
        return Instant.now();
    }
}
