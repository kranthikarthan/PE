package com.payments.domain.payment;

import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.AccountNumber;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

@Value
@AllArgsConstructor
public class PaymentInitiatedEvent implements DomainEvent {
  PaymentId paymentId;
  TenantContext tenantContext;
  Money amount;
  AccountNumber sourceAccount;
  AccountNumber destinationAccount;
  PaymentType paymentType;
  Instant initiatedAt;

  @Override
  public String getEventType() {
    return "PaymentInitiated";
  }

  @Override
  public Instant getOccurredAt() {
    return initiatedAt;
  }
}