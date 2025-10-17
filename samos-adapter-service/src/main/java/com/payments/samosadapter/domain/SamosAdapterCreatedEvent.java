package com.payments.samosadapter.domain;

import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Domain event for SAMOS adapter creation
 */
@Value
@AllArgsConstructor
public class SamosAdapterCreatedEvent implements DomainEvent {
  ClearingAdapterId adapterId;
  String adapterName;
  ClearingNetwork network;
  Instant createdAt;

  @Override
  public String getEventType() {
    return "SamosAdapterCreated";
  }

  @Override
  public Instant getOccurredAt() {
    return createdAt;
  }
}
