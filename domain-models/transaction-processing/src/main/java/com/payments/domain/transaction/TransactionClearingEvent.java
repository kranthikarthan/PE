package com.payments.domain.transaction;

import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class TransactionClearingEvent implements DomainEvent {
  TransactionId transactionId;
  TenantContext tenantContext;
  String clearingSystem;
  String clearingReference;

  @Override
  public String getEventType() {
    return "TransactionClearing";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
