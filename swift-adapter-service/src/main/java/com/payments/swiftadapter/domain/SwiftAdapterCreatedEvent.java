package com.payments.swiftadapter.domain;

import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain event for SWIFT adapter creation */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwiftAdapterCreatedEvent implements DomainEvent {

  private String adapterId;
  private String tenantId;
  private String adapterName;
  private Instant occurredAt;

  @Override
  public String getEventType() {
    return "SwiftAdapterCreated";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
