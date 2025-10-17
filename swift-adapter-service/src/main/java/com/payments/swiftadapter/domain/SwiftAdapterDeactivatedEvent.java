package com.payments.swiftadapter.domain;

import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain event for SWIFT adapter deactivation */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwiftAdapterDeactivatedEvent implements DomainEvent {

  private String adapterId;
  private String tenantId;
  private String reason;
  private Instant occurredAt;

  @Override
  public String getEventType() {
    return "SwiftAdapterDeactivated";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
