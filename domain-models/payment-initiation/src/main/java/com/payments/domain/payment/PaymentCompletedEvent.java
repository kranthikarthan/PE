package com.payments.domain.payment;

import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class PaymentCompletedEvent implements DomainEvent {
  PaymentId paymentId;
  TenantContext tenantContext;
  Money amount;
  Instant completedAt;

  @Override
  public String getEventType() {
    return "PaymentCompleted";
  }

  @Override
  public Instant getOccurredAt() {
    return completedAt;
  }
}
