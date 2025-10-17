package com.payments.rtcadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain event for adding an RTC settlement record */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RtcSettlementRecordAddedEvent implements DomainEvent {
  private ClearingAdapterId adapterId;
  private String settlementRecordId;
  private LocalDate settlementDate;
  private Instant occurredAt;

  @Override
  public String getEventType() {
    return "RtcSettlementRecordAdded";
  }
}
