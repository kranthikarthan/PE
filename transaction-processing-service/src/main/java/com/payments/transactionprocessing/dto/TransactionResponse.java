package com.payments.transactionprocessing.dto;

import com.payments.domain.transaction.Transaction;
import com.payments.domain.transaction.TransactionStatus;
import com.payments.domain.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
  private String transactionId;
  private String tenantId;
  private String businessUnitId;
  private String paymentId;
  private String debitAccount;
  private String creditAccount;
  private BigDecimal amount;
  private String currency;
  private TransactionStatus status;
  private TransactionType transactionType;
  private String clearingSystem;
  private String clearingReference;
  private Instant createdAt;
  private Instant completedAt;
  private String failureReason;

  public static TransactionResponse fromDomain(Transaction transaction) {
    return TransactionResponse.builder()
        .transactionId(transaction.getId().getValue())
        .tenantId(transaction.getTenantContext().getTenantId())
        .businessUnitId(transaction.getTenantContext().getBusinessUnitId())
        .paymentId(transaction.getPaymentId().getValue())
        .debitAccount(transaction.getDebitAccount().getValue())
        .creditAccount(transaction.getCreditAccount().getValue())
        .amount(transaction.getAmount().getAmount())
        .currency(transaction.getAmount().getCurrency().getCurrencyCode())
        .status(transaction.getStatus())
        .transactionType(transaction.getTransactionType())
        .clearingSystem(transaction.getClearingSystem())
        .clearingReference(transaction.getClearingReference())
        .createdAt(transaction.getCreatedAt())
        .completedAt(transaction.getCompletedAt())
        .failureReason(transaction.getFailureReason())
        .build();
  }
}
