package com.payments.settlement.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity for netting transactions in settlement calculations.
 *
 * <p>This entity represents individual transactions that are included in
 * netting calculations. It provides comprehensive transaction tracking
 * including amounts, participants, and settlement information.
 *
 * @since PE-408
 */
@Entity
@Table(name = "netting_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NettingTransaction {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
  private String transactionId;
  
  @Column(name = "netting_cycle_id", nullable = false)
  private Long nettingCycleId;
  
  @Column(name = "debtor_participant_id", nullable = false, length = 50)
  private String debtorParticipantId;
  
  @Column(name = "debtor_participant_name", length = 255)
  private String debtorParticipantName;
  
  @Column(name = "creditor_participant_id", nullable = false, length = 50)
  private String creditorParticipantId;
  
  @Column(name = "creditor_participant_name", length = 255)
  private String creditorParticipantName;
  
  @Column(name = "amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;
  
  @Column(name = "currency", nullable = false, length = 3)
  private String currency;
  
  @Column(name = "reference", length = 255)
  private String reference;
  
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_type", nullable = false, length = 20)
  private TransactionType transactionType;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private TransactionStatus status = TransactionStatus.PENDING;
  
  @Column(name = "transaction_date", nullable = false)
  private LocalDateTime transactionDate;
  
  @Column(name = "value_date")
  private LocalDateTime valueDate;
  
  @Column(name = "settlement_date")
  private LocalDateTime settlementDate;
  
  @Column(name = "priority", nullable = false)
  private Integer priority = 5;
  
  @Column(name = "business_unit_id", length = 50)
  private String businessUnitId;
  
  @Column(name = "tenant_id", nullable = false, length = 50)
  private String tenantId;
  
  @Column(name = "metadata", columnDefinition = "JSONB")
  private String metadata;
  
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
  
  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
  
  @Column(name = "created_by", length = 100)
  private String createdBy;
  
  @Column(name = "updated_by", length = 100)
  private String updatedBy;
  
  /**
   * Enumeration of transaction types.
   */
  public enum TransactionType {
    PAYMENT, TRANSFER, SETTLEMENT, ADJUSTMENT, REVERSAL
  }
  
  /**
   * Enumeration of transaction statuses.
   */
  public enum TransactionStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REVERSED
  }
  
  /**
   * Checks if the transaction is pending.
   *
   * @return true if pending, false otherwise
   */
  public boolean isPending() {
    return status == TransactionStatus.PENDING;
  }
  
  /**
   * Checks if the transaction is processing.
   *
   * @return true if processing, false otherwise
   */
  public boolean isProcessing() {
    return status == TransactionStatus.PROCESSING;
  }
  
  /**
   * Checks if the transaction is completed.
   *
   * @return true if completed, false otherwise
   */
  public boolean isCompleted() {
    return status == TransactionStatus.COMPLETED;
  }
  
  /**
   * Checks if the transaction is failed.
   *
   * @return true if failed, false otherwise
   */
  public boolean isFailed() {
    return status == TransactionStatus.FAILED;
  }
  
  /**
   * Checks if the transaction is cancelled.
   *
   * @return true if cancelled, false otherwise
   */
  public boolean isCancelled() {
    return status == TransactionStatus.CANCELLED;
  }
  
  /**
   * Checks if the transaction is reversed.
   *
   * @return true if reversed, false otherwise
   */
  public boolean isReversed() {
    return status == TransactionStatus.REVERSED;
  }
  
  /**
   * Checks if the transaction is active (not failed, cancelled, or reversed).
   *
   * @return true if active, false otherwise
   */
  public boolean isActive() {
    return !isFailed() && !isCancelled() && !isReversed();
  }
  
  /**
   * Checks if the transaction is a payment.
   *
   * @return true if payment, false otherwise
   */
  public boolean isPayment() {
    return transactionType == TransactionType.PAYMENT;
  }
  
  /**
   * Checks if the transaction is a transfer.
   *
   * @return true if transfer, false otherwise
   */
  public boolean isTransfer() {
    return transactionType == TransactionType.TRANSFER;
  }
  
  /**
   * Checks if the transaction is a settlement.
   *
   * @return true if settlement, false otherwise
   */
  public boolean isSettlement() {
    return transactionType == TransactionType.SETTLEMENT;
  }
  
  /**
   * Checks if the transaction is an adjustment.
   *
   * @return true if adjustment, false otherwise
   */
  public boolean isAdjustment() {
    return transactionType == TransactionType.ADJUSTMENT;
  }
  
  /**
   * Checks if the transaction is a reversal.
   *
   * @return true if reversal, false otherwise
   */
  public boolean isReversal() {
    return transactionType == TransactionType.REVERSAL;
  }
  
  /**
   * Starts processing the transaction.
   */
  public void startProcessing() {
    this.status = TransactionStatus.PROCESSING;
  }
  
  /**
   * Completes the transaction.
   */
  public void complete() {
    this.status = TransactionStatus.COMPLETED;
  }
  
  /**
   * Fails the transaction.
   */
  public void fail() {
    this.status = TransactionStatus.FAILED;
  }
  
  /**
   * Cancels the transaction.
   */
  public void cancel() {
    this.status = TransactionStatus.CANCELLED;
  }
  
  /**
   * Reverses the transaction.
   */
  public void reverse() {
    this.status = TransactionStatus.REVERSED;
  }
  
  /**
   * Settles the transaction.
   *
   * @param settlementDate the settlement date
   * @param valueDate the value date
   */
  public void settle(LocalDateTime settlementDate, LocalDateTime valueDate) {
    this.status = TransactionStatus.COMPLETED;
    this.settlementDate = settlementDate;
    this.valueDate = valueDate;
  }
  
  /**
   * Gets the absolute amount.
   *
   * @return the absolute amount
   */
  public BigDecimal getAbsoluteAmount() {
    return amount.abs();
  }
  
  /**
   * Checks if the transaction is a debit for the debtor participant.
   *
   * @return true if debit, false otherwise
   */
  public boolean isDebitForDebtor() {
    return amount.compareTo(BigDecimal.ZERO) > 0;
  }
  
  /**
   * Checks if the transaction is a credit for the creditor participant.
   *
   * @return true if credit, false otherwise
   */
  public boolean isCreditForCreditor() {
    return amount.compareTo(BigDecimal.ZERO) > 0;
  }
  
  /**
   * Gets the transaction summary.
   *
   * @return the transaction summary string
   */
  public String getSummary() {
    return String.format("Transaction[%s] %s -> %s %s %s (%s)", 
        transactionId, debtorParticipantId, creditorParticipantId, 
        currency, amount, status);
  }
}
