package com.payments.domain.tenant;

import com.payments.domain.shared.*;
import java.time.Instant;
import lombok.*;

/** Domain Event: User Added */
@Value
@AllArgsConstructor
public class UserAddedEvent implements DomainEvent {
  TenantId tenantId;
  UserId userId;
  String username;
  String role;

  @Override
  public String getEventType() {
    return "UserAdded";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
