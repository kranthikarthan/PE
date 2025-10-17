package com.payments.samosadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Domain event for SAMOS message logging
 */
@Value
@AllArgsConstructor
public class SamosMessageLoggedEvent implements DomainEvent {
  ClearingAdapterId adapterId;
  String direction;
  String messageType;
  Integer statusCode;
  Instant occurredAt;

  @Override
  public String getEventType() {
    return "SamosMessageLogged";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
