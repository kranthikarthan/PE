package com.payments.payshapadapter.domain;

import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain event for PayShap adapter activation */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayShapAdapterActivatedEvent implements DomainEvent {

  private String adapterId;
  private String tenantId;
  private Instant occurredAt;

  @Override
  public String getEventType() {
    return "PayShapAdapterActivated";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
