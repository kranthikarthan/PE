package com.payments.domain.saga;

import com.payments.domain.shared.*;
import java.time.Instant;
import lombok.*;

/** Domain Event: Saga Started */
@Value
@AllArgsConstructor
public class SagaStartedEvent implements DomainEvent {
  SagaId sagaId;
  String sagaName;
  SagaType sagaType;
  String businessKey;
  Instant startedAt;

  @Override
  public String getEventType() {
    return "SagaStarted";
  }

  @Override
  public Instant getOccurredAt() {
    return startedAt;
  }
}
