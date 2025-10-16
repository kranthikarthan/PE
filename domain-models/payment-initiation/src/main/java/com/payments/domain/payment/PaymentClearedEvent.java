package com.payments.domain.payment;

import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class PaymentClearedEvent implements DomainEvent {
  PaymentId paymentId;
  TenantContext tenantContext;
  ClearingConfirmation clearingConfirmation;

  @Override
  public String getEventType() {
    return "PaymentCleared";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
