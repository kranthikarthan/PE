package com.payments.domain.transaction;

import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class TransactionProcessingEvent implements DomainEvent {
  TransactionId transactionId;
  TenantContext tenantContext;
  TransactionStatus status;

  @Override
  public String getEventType() {
    return "TransactionProcessing";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}






