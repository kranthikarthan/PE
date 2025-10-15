package com.payments.domain.account;

import com.payments.domain.shared.*;
import java.time.Instant;
import lombok.*;

/** Domain Event: Adapter Activated */
@Value
@AllArgsConstructor
public class AdapterActivatedEvent implements DomainEvent {
  AccountAdapterId adapterId;
  String activatedBy;

  @Override
  public String getEventType() {
    return "AdapterActivated";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
