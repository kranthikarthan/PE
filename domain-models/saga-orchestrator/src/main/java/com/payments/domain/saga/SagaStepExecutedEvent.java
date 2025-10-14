package com.payments.domain.saga;

import com.payments.domain.shared.*;
import lombok.*;
import java.time.Instant;

/**
 * Domain Event: Saga Step Executed
 */
@Value
@AllArgsConstructor
public class SagaStepExecutedEvent implements DomainEvent {
    SagaId sagaId;
    SagaStepId stepId;
    String stepName;
    String executedBy;
    
    @Override
    public String getEventType() {
        return "SagaStepExecuted";
    }
    
    @Override
    public Instant getOccurredAt() {
        return Instant.now();
    }
}
