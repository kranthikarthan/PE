package com.payments.domain.payment;

import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

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






