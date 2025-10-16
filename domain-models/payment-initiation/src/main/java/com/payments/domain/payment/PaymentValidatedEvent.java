package com.payments.domain.payment;

import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import com.payments.domain.validation.ValidationResult;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class PaymentValidatedEvent implements DomainEvent {
  PaymentId paymentId;
  TenantContext tenantContext;
  ValidationResult validationResult;

  @Override
  public String getEventType() {
    return "PaymentValidated";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}
