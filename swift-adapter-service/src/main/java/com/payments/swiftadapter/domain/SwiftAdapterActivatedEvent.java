package com.payments.swiftadapter.domain;

import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain event for SWIFT adapter activation */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwiftAdapterActivatedEvent implements DomainEvent {

  private String adapterId;
  private String tenantId;
  private Instant occurredAt;

  @Override
  public String getEventType() {
    return "SwiftAdapterActivated";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
