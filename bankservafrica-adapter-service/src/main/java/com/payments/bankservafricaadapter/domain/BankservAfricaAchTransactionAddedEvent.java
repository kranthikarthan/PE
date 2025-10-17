package com.payments.bankservafricaadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.DomainEvent;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain event for BankservAfrica ACH transaction addition */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankservAfricaAchTransactionAddedEvent implements DomainEvent {

  private ClearingAdapterId adapterId;
  private String transactionId;
  private String achBatchId;
  private Instant occurredAt;

  @Override
  public String getEventType() {
    return "BankservAfricaAchTransactionAdded";
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }
}
