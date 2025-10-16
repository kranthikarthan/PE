package com.payments.domain.transaction;

import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class TransactionFailedEvent implements DomainEvent {
  TransactionId transactionId;
  TenantContext tenantContext;
  String reason;
  TransactionStatus previousStatus;

  @Override
  public String getEventType() {
    return "TransactionFailed";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}






