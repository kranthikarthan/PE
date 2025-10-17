package com.payments.rtcadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain event for adding an RTC payment message */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RtcPaymentMessageAddedEvent implements DomainEvent {
  private ClearingAdapterId adapterId;
  private String paymentMessageId;
  private String transactionId;
  private Instant occurredAt;

  @Override
  public String getEventType() {
    return "RtcPaymentMessageAdded";
  }
}
