package com.payments.bankservafricaadapter.domain;

import com.payments.bankservafricaadapter.exception.InvalidBankservAfricaSettlementRecordException;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BankservAfrica Settlement Record Entity
 *
 * <p>Represents settlement records for BankservAfrica processing: - Daily settlement tracking -
 * Transaction count monitoring - Settlement status management - Reference tracking
 */
@Entity
@Table(
    name = "bankservafrica_settlement_records",
    indexes = {
        @Index(name = "idx_settlement_date", columnList = "settlement_date")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
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

  @Column(name = "status", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private SettlementStatus status;

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
    record.status = SettlementStatus.PENDING;
    record.onCreate();

    return record;
  }

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
    updatedAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }

  /** Update settlement status */
  public void updateStatus(SettlementStatus status, String settlementReference) {
    this.status = status;
    this.settlementReference = settlementReference;
  }

  /** Mark as processing */
  public void markAsProcessing() {
    updateStatus(SettlementStatus.PROCESSING, null);
  }

  /** Mark as completed */
  public void markAsCompleted(String settlementReference) {
    updateStatus(SettlementStatus.COMPLETED, settlementReference);
  }

  /** Mark as failed */
  public void markAsFailed() {
    updateStatus(SettlementStatus.FAILED, null);
  }

  /** Add transaction to count */
  public void addTransaction(BigDecimal amount) {
    if (amount == null) {
      throw new InvalidBankservAfricaSettlementRecordException("Transaction amount cannot be null");
    }
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new InvalidBankservAfricaSettlementRecordException(
          "Transaction amount cannot be negative");
    }
    try {
      this.transactionCount++;
      this.totalAmount = this.totalAmount.add(amount);
    } catch (ArithmeticException e) {
      throw new InvalidBankservAfricaSettlementRecordException(
          "Arithmetic overflow in transaction amount", e);
    }
  }

  /** Check if settlement is pending */
  public boolean isPending() {
    return SettlementStatus.PENDING.equals(this.status);
  }

  /** Check if settlement is processing */
  public boolean isProcessing() {
    return SettlementStatus.PROCESSING.equals(this.status);
  }

  /** Check if settlement is completed */
  public boolean isCompleted() {
    return SettlementStatus.COMPLETED.equals(this.status);
  }

  /** Check if settlement is failed */
  public boolean isFailed() {
    return SettlementStatus.FAILED.equals(this.status);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BankservAfricaSettlementRecord)) return false;
    BankservAfricaSettlementRecord that = (BankservAfricaSettlementRecord) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
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
        BigDecimal.valueOf(this.transactionCount), new MathContext(2, RoundingMode.HALF_UP));
  }
}
