package com.payments.payshapadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Domain event for PayShap message logging
 */
@Value
@AllArgsConstructor
public class PayShapMessageLoggedEvent implements DomainEvent {
  ClearingAdapterId adapterId;
  String direction;
  String messageType;
  Integer statusCode;
  Instant occurredAt;

  @Override
  public String getEventType() {
    return "PayShapMessageLogged";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
