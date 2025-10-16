package com.payments.domain.payment;

import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class PaymentSubmittedToClearingEvent implements DomainEvent {
  PaymentId paymentId;
  TenantContext tenantContext;
  ClearingSystemReference clearingSystemReference;

  @Override
  public String getEventType() {
    return "PaymentSubmittedToClearing";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
