package com.payments.rtcadapter.domain;

import com.payments.domain.clearing.ClearingNetwork;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain event for RTC adapter creation */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RtcAdapterCreatedEvent implements DomainEvent {

  private ClearingAdapterId adapterId;
  private String adapterName;
  private ClearingNetwork network;
  private Instant occurredAt;

  @Override
  public String getEventType() {
    return "RtcAdapterCreated";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
