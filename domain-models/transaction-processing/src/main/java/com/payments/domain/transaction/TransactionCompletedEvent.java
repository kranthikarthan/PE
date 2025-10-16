package com.payments.domain.transaction;

import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class TransactionCompletedEvent implements DomainEvent {
  TransactionId transactionId;
  TenantContext tenantContext;
  com.payments.domain.shared.Money amount;
  Instant completedAt;

  @Override
  public String getEventType() {
    return "TransactionCompleted";
  }

  @Override
  public Instant getOccurredAt() {
    return completedAt;
  }
}






