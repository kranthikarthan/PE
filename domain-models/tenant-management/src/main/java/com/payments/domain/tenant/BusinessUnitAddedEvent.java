package com.payments.domain.tenant;

import com.payments.domain.shared.*;
import java.time.Instant;
import lombok.*;

/** Domain Event: Business Unit Added */
@Value
@AllArgsConstructor
public class BusinessUnitAddedEvent implements DomainEvent {
  TenantId tenantId;
  BusinessUnitId businessUnitId;
  String businessUnitName;
  BusinessUnitType businessUnitType;

  @Override
  public String getEventType() {
    return "BusinessUnitAdded";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
