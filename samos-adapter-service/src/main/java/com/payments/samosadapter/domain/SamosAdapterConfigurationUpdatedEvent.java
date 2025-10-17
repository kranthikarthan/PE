package com.payments.samosadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

/** Domain event for SAMOS adapter configuration updates */
@Value
@AllArgsConstructor
public class SamosAdapterConfigurationUpdatedEvent implements DomainEvent {
  ClearingAdapterId adapterId;
  String endpoint;
  String apiVersion;
  Instant occurredAt;

  @Override
  public String getEventType() {
    return "SamosAdapterConfigurationUpdated";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
