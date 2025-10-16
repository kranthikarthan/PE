package com.payments.domain.validation;

import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ValidationFailedEvent implements DomainEvent {
  PaymentId paymentId;
  TenantContext tenantContext;
  List<FailedRule> failedRules;

  @Override
  public String getEventType() {
    return "ValidationFailed";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}






