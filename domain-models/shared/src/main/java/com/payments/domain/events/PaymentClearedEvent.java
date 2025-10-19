package com.payments.domain.events;

import com.payments.domain.shared.*;
import com.payments.domain.valueobjects.ClearingConfirmation;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/** Domain event for payment cleared */
@Data
@Builder
public class PaymentClearedEvent implements DomainEvent {

  private PaymentId paymentId;
  private TenantId tenantId;
  private Money amount;
  private ClearingConfirmation confirmation;
  private Instant occurredAt;

  public PaymentClearedEvent(
      PaymentId paymentId, TenantId tenantId, Money amount, ClearingConfirmation confirmation) {
    this.paymentId = paymentId;
    this.tenantId = tenantId;
    this.amount = amount;
    this.confirmation = confirmation;
    this.occurredAt = Instant.now();
  }

  public PaymentClearedEvent(
      PaymentId paymentId,
      TenantId tenantId,
      Money amount,
      ClearingConfirmation confirmation,
      Instant occurredAt) {
    this.paymentId = paymentId;
    this.tenantId = tenantId;
    this.amount = amount;
    this.confirmation = confirmation;
    this.occurredAt = occurredAt;
  }

  @Override
  public String getEventType() {
    return "PaymentCleared";
  }

  @Override
  public Instant getOccurredAt() {
    return this.occurredAt;
  }
}
