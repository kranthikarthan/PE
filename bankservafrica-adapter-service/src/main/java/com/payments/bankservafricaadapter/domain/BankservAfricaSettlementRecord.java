package com.payments.bankservafricaadapter.domain;

import com.payments.bankservafricaadapter.exception.InvalidBankservAfricaSettlementRecordException;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.*;

/**
 * BankservAfrica Settlement Record Entity
 *
 * <p>Represents settlement records for BankservAfrica processing: - Daily settlement tracking -
 * Transaction count monitoring - Settlement status management - Reference tracking
 */
@Entity
@Table(name = "bankservafrica_settlement_records")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class BankservAfricaSettlementRecord {

  @EmbeddedId private ClearingMessageId id;

  @Embedded private ClearingAdapterId bankservafricaAdapterId;

  @Column(name = "settlement_date", nullable = false)
  private LocalDate settlementDate;

  @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "currency_code", nullable = false, length = 3)
  private String currencyCode;

  @Column(name = "transaction_count", nullable = false)
  private Integer transactionCount;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "settlement_reference")
  private String settlementReference;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  /** Create a new settlement record */
  public static BankservAfricaSettlementRecord create(
      ClearingMessageId id,
      ClearingAdapterId bankservafricaAdapterId,
      LocalDate settlementDate,
      BigDecimal totalAmount,
      String currencyCode,
      Integer transactionCount) {

    if (settlementDate == null) {
      throw new InvalidBankservAfricaSettlementRecordException("Settlement date cannot be null");
    }
    if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0) {
      throw new InvalidBankservAfricaSettlementRecordException("Total amount cannot be negative");
    }
    if (currencyCode == null || currencyCode.isBlank()) {
      throw new InvalidBankservAfricaSettlementRecordException(
          "Currency code cannot be null or blank");
    }
    if (transactionCount == null || transactionCount < 0) {
      throw new InvalidBankservAfricaSettlementRecordException(
          "Transaction count cannot be negative");
    }

    BankservAfricaSettlementRecord record = new BankservAfricaSettlementRecord();
    record.id = id;
    record.bankservafricaAdapterId = bankservafricaAdapterId;
    record.settlementDate = settlementDate;
    record.totalAmount = totalAmount;
    record.currencyCode = currencyCode;
    record.transactionCount = transactionCount;
    record.status = "PENDING";
    record.createdAt = Instant.now();
    record.updatedAt = Instant.now();

    return record;
  }

  /** Update settlement status */
  public void updateStatus(String status, String settlementReference) {
    this.status = status;
    this.settlementReference = settlementReference;
    this.updatedAt = Instant.now();
  }

  /** Mark as processing */
  public void markAsProcessing() {
    updateStatus("PROCESSING", null);
  }

  /** Mark as completed */
  public void markAsCompleted(String settlementReference) {
    updateStatus("COMPLETED", settlementReference);
  }

  /** Mark as failed */
  public void markAsFailed() {
    updateStatus("FAILED", null);
  }

  /** Add transaction to count */
  public void addTransaction(BigDecimal amount) {
    this.transactionCount++;
    this.totalAmount = this.totalAmount.add(amount);
    this.updatedAt = Instant.now();
  }

  /** Check if settlement is pending */
  public boolean isPending() {
    return "PENDING".equals(this.status);
  }

  /** Check if settlement is processing */
  public boolean isProcessing() {
    return "PROCESSING".equals(this.status);
  }

  /** Check if settlement is completed */
  public boolean isCompleted() {
    return "COMPLETED".equals(this.status);
  }

  /** Check if settlement is failed */
  public boolean isFailed() {
    return "FAILED".equals(this.status);
  }

  /** Check if settlement has transactions */
  public boolean hasTransactions() {
    return this.transactionCount > 0;
  }

  /** Get average transaction amount */
  public BigDecimal getAverageTransactionAmount() {
    if (this.transactionCount == 0) {
      return BigDecimal.ZERO;
    }
    return this.totalAmount.divide(
        BigDecimal.valueOf(this.transactionCount), 2, BigDecimal.ROUND_HALF_UP);
  }
}
