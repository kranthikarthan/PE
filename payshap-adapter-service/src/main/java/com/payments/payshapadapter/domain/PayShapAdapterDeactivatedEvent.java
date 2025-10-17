package com.payments.payshapadapter.domain;

import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain event for PayShap adapter deactivation */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayShapAdapterDeactivatedEvent implements DomainEvent {

  private String adapterId;
  private String tenantId;
  private String reason;
  private Instant occurredAt;

  @Override
  public String getEventType() {
    return "PayShapAdapterDeactivated";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
