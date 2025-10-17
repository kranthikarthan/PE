package com.payments.samosadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Domain event for SAMOS adapter activation
 */
@Value
@AllArgsConstructor
public class SamosAdapterActivatedEvent implements DomainEvent {
  ClearingAdapterId adapterId;
  String activatedBy;
  Instant occurredAt;

  @Override
  public String getEventType() {
    return "SamosAdapterActivated";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
