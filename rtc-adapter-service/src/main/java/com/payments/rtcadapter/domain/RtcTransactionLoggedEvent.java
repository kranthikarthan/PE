package com.payments.rtcadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain event for logging an RTC transaction */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RtcTransactionLoggedEvent implements DomainEvent {
  private ClearingAdapterId adapterId;
  private String transactionId;
  private String status;
  private Instant occurredAt;

  @Override
  public String getEventType() {
    return "RtcTransactionLogged";
  }
}
