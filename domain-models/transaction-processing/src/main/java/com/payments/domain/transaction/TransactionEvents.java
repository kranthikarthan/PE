package com.payments.domain.transaction;

import com.payments.domain.shared.DomainEvent;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
class TransactionCreatedEvent implements DomainEvent {
  TransactionId transactionId;
  TenantContext tenantContext;
  PaymentId paymentId;
  com.payments.domain.shared.AccountNumber debitAccount;
  com.payments.domain.shared.AccountNumber creditAccount;
  com.payments.domain.shared.Money amount;
  Instant createdAt;

  @Override
  public String getEventType() {
    return "TransactionCreated";
  }

  @Override
  public Instant getOccurredAt() {
    return createdAt;
  }
}

@Value
@AllArgsConstructor
class TransactionProcessingEvent implements DomainEvent {
  TransactionId transactionId;
  TenantContext tenantContext;
  TransactionStatus status;

  @Override
  public String getEventType() {
    return "TransactionProcessing";
  }

  @Override
  public Instant getOccurredAt() {
    return Instant.now();
  }
}

@Value
@AllArgsConstructor
class TransactionClearingEvent implements DomainEvent {
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

@Value
@AllArgsConstructor
class TransactionCompletedEvent implements DomainEvent {
  TransactionId transactionId;
  TenantContext tenantContext;
  com.payments.domain.shared.Money amount;
  Instant completedAt;

  @Override
  public String getEventType() {
    return "TransactionCompleted";
  }

  @Override
  public Instant getOccurredAt() {
    return completedAt;
  }
}

@Value
@AllArgsConstructor
class TransactionFailedEvent implements DomainEvent {
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
