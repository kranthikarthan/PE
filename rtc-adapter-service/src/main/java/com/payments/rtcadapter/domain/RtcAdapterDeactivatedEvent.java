package com.payments.rtcadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain event for RTC adapter deactivation */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RtcAdapterDeactivatedEvent implements DomainEvent {

  private ClearingAdapterId adapterId;
  private String reason;
  private String deactivatedBy;
  private Instant occurredAt;

  @Override
  public String getEventType() {
    return "RtcAdapterDeactivated";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
