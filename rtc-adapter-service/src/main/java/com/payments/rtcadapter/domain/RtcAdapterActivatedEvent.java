package com.payments.rtcadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain event for RTC adapter activation */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RtcAdapterActivatedEvent implements DomainEvent {

  private ClearingAdapterId adapterId;
  private String activatedBy;
  private Instant occurredAt;

  @Override
  public String getEventType() {
    return "RtcAdapterActivated";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
