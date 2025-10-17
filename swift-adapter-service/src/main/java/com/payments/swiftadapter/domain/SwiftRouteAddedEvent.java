package com.payments.swiftadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingRouteId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Domain event for SWIFT route addition
 */
@Value
@AllArgsConstructor
public class SwiftRouteAddedEvent implements DomainEvent {
  ClearingAdapterId adapterId;
  ClearingRouteId routeId;
  String routeName;
  Instant occurredAt;

  @Override
  public String getEventType() {
    return "SwiftRouteAdded";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
