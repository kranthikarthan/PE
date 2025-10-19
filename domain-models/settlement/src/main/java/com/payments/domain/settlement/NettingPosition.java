package com.payments.domain.settlement;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA entity for netting positions in settlement calculations.
 *
 * <p>This entity represents a netting position for a specific participant and currency, containing
 * the net amount after all debits and credits have been calculated. It supports multi-currency
 * netting and provides comprehensive position tracking for settlement operations.
 *
 * @since PE-408
 */
@Entity
@Table(name = "netting_positions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NettingPosition {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "netting_cycle_id", nullable = false)
  private Long nettingCycleId;

  @Column(name = "participant_id", nullable = false, length = 50)
  private String participantId;

  @Column(name = "participant_name", length = 255)
  private String participantName;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "net_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal netAmount;

  @Column(name = "debit_amount", precision = 19, scale = 4)
  private BigDecimal debitAmount = BigDecimal.ZERO;

  @Column(name = "credit_amount", precision = 19, scale = 4)
  private BigDecimal creditAmount = BigDecimal.ZERO;

  @Column(name = "transaction_count")
  private Integer transactionCount = 0;

  @Column(name = "debit_count")
  private Integer debitCount = 0;

  @Column(name = "credit_count")
  private Integer creditCount = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "position_type", nullable = false, length = 20)
  private PositionType positionType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private PositionStatus status = PositionStatus.ACTIVE;

  @Column(name = "settlement_date")
  private LocalDateTime settlementDate;

  @Column(name = "value_date")
  private LocalDateTime valueDate;

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

  /** Enumeration of position types. */
  public enum PositionType {
    DEBIT,
    CREDIT,
    ZERO
  }

  /** Enumeration of position statuses. */
  public enum PositionStatus {
    ACTIVE,
    SETTLED,
    CANCELLED,
    REVERSED
  }

  /**
   * Calculates the net amount from debit and credit amounts.
   *
   * @return the calculated net amount
   */
  public BigDecimal calculateNetAmount() {
    if (debitAmount == null) debitAmount = BigDecimal.ZERO;
    if (creditAmount == null) creditAmount = BigDecimal.ZERO;

    return creditAmount.subtract(debitAmount);
  }

  /**
   * Determines the position type based on net amount.
   *
   * @return the position type
   */
  public PositionType determinePositionType() {
    BigDecimal net = calculateNetAmount();
    if (net.compareTo(BigDecimal.ZERO) > 0) {
      return PositionType.CREDIT;
    } else if (net.compareTo(BigDecimal.ZERO) < 0) {
      return PositionType.DEBIT;
    } else {
      return PositionType.ZERO;
    }
  }

  /**
   * Checks if the position is a debit position.
   *
   * @return true if debit position, false otherwise
   */
  public boolean isDebitPosition() {
    return positionType == PositionType.DEBIT;
  }

  /**
   * Checks if the position is a credit position.
   *
   * @return true if credit position, false otherwise
   */
  public boolean isCreditPosition() {
    return positionType == PositionType.CREDIT;
  }

  /**
   * Checks if the position is a zero position.
   *
   * @return true if zero position, false otherwise
   */
  public boolean isZeroPosition() {
    return positionType == PositionType.ZERO;
  }

  /**
   * Checks if the position is active.
   *
   * @return true if active, false otherwise
   */
  public boolean isActive() {
    return status == PositionStatus.ACTIVE;
  }

  /**
   * Checks if the position is settled.
   *
   * @return true if settled, false otherwise
   */
  public boolean isSettled() {
    return status == PositionStatus.SETTLED;
  }

  /**
   * Updates the position with new amounts.
   *
   * @param debitAmount the new debit amount
   * @param creditAmount the new credit amount
   */
  public void updateAmounts(BigDecimal debitAmount, BigDecimal creditAmount) {
    this.debitAmount = debitAmount != null ? debitAmount : BigDecimal.ZERO;
    this.creditAmount = creditAmount != null ? creditAmount : BigDecimal.ZERO;
    this.netAmount = calculateNetAmount();
    this.positionType = determinePositionType();
  }

  /**
   * Adds a transaction to the position.
   *
   * @param amount the transaction amount
   * @param isDebit true if debit transaction, false if credit
   */
  public void addTransaction(BigDecimal amount, boolean isDebit) {
    if (amount == null) return;

    if (isDebit) {
      this.debitAmount = this.debitAmount.add(amount);
      this.debitCount++;
    } else {
      this.creditAmount = this.creditAmount.add(amount);
      this.creditCount++;
    }

    this.transactionCount++;
    this.netAmount = calculateNetAmount();
    this.positionType = determinePositionType();
  }

  /**
   * Removes a transaction from the position.
   *
   * @param amount the transaction amount
   * @param isDebit true if debit transaction, false if credit
   */
  public void removeTransaction(BigDecimal amount, boolean isDebit) {
    if (amount == null) return;

    if (isDebit) {
      this.debitAmount = this.debitAmount.subtract(amount);
      this.debitCount = Math.max(0, this.debitCount - 1);
    } else {
      this.creditAmount = this.creditAmount.subtract(amount);
      this.creditCount = Math.max(0, this.creditCount - 1);
    }

    this.transactionCount = Math.max(0, this.transactionCount - 1);
    this.netAmount = calculateNetAmount();
    this.positionType = determinePositionType();
  }

  /**
   * Settles the position.
   *
   * @param settlementDate the settlement date
   * @param valueDate the value date
   */
  public void settle(LocalDateTime settlementDate, LocalDateTime valueDate) {
    this.status = PositionStatus.SETTLED;
    this.settlementDate = settlementDate;
    this.valueDate = valueDate;
  }

  /** Cancels the position. */
  public void cancel() {
    this.status = PositionStatus.CANCELLED;
  }

  /** Reverses the position. */
  public void reverse() {
    this.status = PositionStatus.REVERSED;
  }

  /**
   * Gets the absolute net amount.
   *
   * @return the absolute net amount
   */
  public BigDecimal getAbsoluteNetAmount() {
    return netAmount.abs();
  }

  /**
   * Gets the position summary.
   *
   * @return the position summary string
   */
  public String getSummary() {
    return String.format(
        "Position[%s] %s %s - %s (%s)", participantId, currency, netAmount, positionType, status);
  }
}
