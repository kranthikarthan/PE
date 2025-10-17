package com.payments.samosadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Domain event for SAMOS adapter deactivation
 */
@Value
@AllArgsConstructor
public class SamosAdapterDeactivatedEvent implements DomainEvent {
  ClearingAdapterId adapterId;
  String reason;
  String deactivatedBy;
  Instant occurredAt;

  @Override
  public String getEventType() {
    return "SamosAdapterDeactivated";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
