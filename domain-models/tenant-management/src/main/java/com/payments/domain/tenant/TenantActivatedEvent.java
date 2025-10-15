package com.payments.domain.tenant;

import com.payments.domain.shared.*;
import java.time.Instant;
import lombok.*;

/** Domain Event: Tenant Activated */
@Value
@AllArgsConstructor
public class TenantActivatedEvent implements DomainEvent {
  TenantId tenantId;
  String activatedBy;

  @Override
  public String getEventType() {
    return "TenantActivated";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
