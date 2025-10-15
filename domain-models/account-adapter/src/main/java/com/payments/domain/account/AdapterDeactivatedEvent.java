package com.payments.domain.account;

import com.payments.domain.shared.*;
import java.time.Instant;
import lombok.*;

/** Domain Event: Adapter Deactivated */
@Value
@AllArgsConstructor
public class AdapterDeactivatedEvent implements DomainEvent {
  AccountAdapterId adapterId;
  String reason;
  String deactivatedBy;

  @Override
  public String getEventType() {
    return "AdapterDeactivated";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
