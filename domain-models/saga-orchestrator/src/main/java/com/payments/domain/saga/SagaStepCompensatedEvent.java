package com.payments.domain.saga;

import com.payments.domain.shared.*;
import java.time.Instant;
import lombok.*;

/** Domain Event: Saga Step Compensated */
@Value
@AllArgsConstructor
public class SagaStepCompensatedEvent implements DomainEvent {
  SagaId sagaId;
  SagaStepId stepId;
  String stepName;
  String compensationResult;

  @Override
  public String getEventType() {
    return "SagaStepCompensated";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
