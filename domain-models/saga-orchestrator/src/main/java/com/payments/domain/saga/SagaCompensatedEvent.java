package com.payments.domain.saga;

import com.payments.domain.shared.*;
import java.time.Instant;
import lombok.*;

/** Domain Event: Saga Compensated */
@Value
@AllArgsConstructor
public class SagaCompensatedEvent implements DomainEvent {
  SagaId sagaId;
  String sagaName;
  String failureReason;

  @Override
  public String getEventType() {
    return "SagaCompensated";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
