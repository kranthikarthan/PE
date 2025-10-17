package com.payments.payshapadapter.domain;

import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain event for PayShap adapter creation */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayShapAdapterCreatedEvent implements DomainEvent {

  private String adapterId;
  private String tenantId;
  private String adapterName;
  private Instant occurredAt;

  @Override
  public String getEventType() {
    return "PayShapAdapterCreated";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
