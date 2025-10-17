package com.payments.bankservafricaadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain event for BankservAfrica EFT message addition */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankservAfricaEftMessageAddedEvent implements DomainEvent {

  private ClearingAdapterId adapterId;
  private String messageId;
  private String batchId;
  private Instant occurredAt;

  @Override
  public String getEventType() {
    return "BankservAfricaEftMessageAdded";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
