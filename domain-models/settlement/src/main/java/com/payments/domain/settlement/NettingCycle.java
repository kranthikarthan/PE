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
 * JPA entity for netting cycles in settlement calculations.
 *
 * <p>This entity represents a netting cycle that groups multiple transactions for netting
 * calculation. It provides comprehensive cycle management including status tracking, participant
 * management, and settlement coordination.
 *
 * @since PE-408
 */
@Entity
@Table(name = "netting_cycles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NettingCycle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "cycle_id", nullable = false, unique = true, length = 100)
  private String cycleId;

  @Column(name = "cycle_name", nullable = false, length = 255)
  private String cycleName;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "cycle_type", nullable = false, length = 20)
  private CycleType cycleType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private CycleStatus status = CycleStatus.ACTIVE;

  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;

  @Column(name = "end_time")
  private LocalDateTime endTime;

  @Column(name = "cut_off_time")
  private LocalDateTime cutOffTime;

  @Column(name = "settlement_date")
  private LocalDateTime settlementDate;

  @Column(name = "value_date")
  private LocalDateTime valueDate;

  @Column(name = "participant_count")
  private Integer participantCount = 0;

  @Column(name = "transaction_count")
  private Integer transactionCount = 0;

  @Column(name = "total_debit_amount", precision = 19, scale = 4)
  private BigDecimal totalDebitAmount = BigDecimal.ZERO;

  @Column(name = "total_credit_amount", precision = 19, scale = 4)
  private BigDecimal totalCreditAmount = BigDecimal.ZERO;

  @Column(name = "net_amount", precision = 19, scale = 4)
  private BigDecimal netAmount = BigDecimal.ZERO;

  @Column(name = "currency", length = 3)
  private String currency;

  @Column(name = "priority", nullable = false)
  private Integer priority = 5;

  @Column(name = "business_unit_id", length = 50)
  private String businessUnitId;

  @Column(name = "tenant_id", nullable = false, length = 50)
  private String tenantId;

  @Column(name = "configuration", columnDefinition = "JSONB")
  private String configuration;

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

  /** Enumeration of cycle types. */
  public enum CycleType {
    DAILY,
    HOURLY,
    REAL_TIME,
    MANUAL,
    SCHEDULED
  }

  /** Enumeration of cycle statuses. */
  public enum CycleStatus {
    ACTIVE,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    SETTLED
  }

  /**
   * Calculates the net amount from total debit and credit amounts.
   *
   * @return the calculated net amount
   */
  public BigDecimal calculateNetAmount() {
    if (totalDebitAmount == null) totalDebitAmount = BigDecimal.ZERO;
    if (totalCreditAmount == null) totalCreditAmount = BigDecimal.ZERO;

    return totalCreditAmount.subtract(totalDebitAmount);
  }

  /**
   * Checks if the cycle is active.
   *
   * @return true if active, false otherwise
   */
  public boolean isActive() {
    return status == CycleStatus.ACTIVE;
  }

  /**
   * Checks if the cycle is processing.
   *
   * @return true if processing, false otherwise
   */
  public boolean isProcessing() {
    return status == CycleStatus.PROCESSING;
  }

  /**
   * Checks if the cycle is completed.
   *
   * @return true if completed, false otherwise
   */
  public boolean isCompleted() {
    return status == CycleStatus.COMPLETED;
  }

  /**
   * Checks if the cycle is settled.
   *
   * @return true if settled, false otherwise
   */
  public boolean isSettled() {
    return status == CycleStatus.SETTLED;
  }

  /**
   * Checks if the cycle is failed.
   *
   * @return true if failed, false otherwise
   */
  public boolean isFailed() {
    return status == CycleStatus.FAILED;
  }

  /**
   * Checks if the cycle is cancelled.
   *
   * @return true if cancelled, false otherwise
   */
  public boolean isCancelled() {
    return status == CycleStatus.CANCELLED;
  }

  /**
   * Checks if the cycle is balanced (net amount is zero).
   *
   * @return true if balanced, false otherwise
   */
  public boolean isBalanced() {
    return calculateNetAmount().compareTo(BigDecimal.ZERO) == 0;
  }

  /**
   * Checks if the cycle is within cut-off time.
   *
   * @return true if within cut-off time, false otherwise
   */
  public boolean isWithinCutOffTime() {
    if (cutOffTime == null) return true;
    return LocalDateTime.now().isBefore(cutOffTime);
  }

  /**
   * Checks if the cycle is ready for settlement.
   *
   * @return true if ready for settlement, false otherwise
   */
  public boolean isReadyForSettlement() {
    return isCompleted() && isBalanced() && !isWithinCutOffTime();
  }

  /**
   * Updates the cycle with new amounts.
   *
   * @param debitAmount the new debit amount
   * @param creditAmount the new credit amount
   */
  public void updateAmounts(BigDecimal debitAmount, BigDecimal creditAmount) {
    this.totalDebitAmount = debitAmount != null ? debitAmount : BigDecimal.ZERO;
    this.totalCreditAmount = creditAmount != null ? creditAmount : BigDecimal.ZERO;
    this.netAmount = calculateNetAmount();
  }

  /**
   * Adds amounts to the cycle.
   *
   * @param debitAmount the debit amount to add
   * @param creditAmount the credit amount to add
   */
  public void addAmounts(BigDecimal debitAmount, BigDecimal creditAmount) {
    if (debitAmount != null) {
      this.totalDebitAmount = this.totalDebitAmount.add(debitAmount);
    }
    if (creditAmount != null) {
      this.totalCreditAmount = this.totalCreditAmount.add(creditAmount);
    }
    this.netAmount = calculateNetAmount();
  }

  /**
   * Removes amounts from the cycle.
   *
   * @param debitAmount the debit amount to remove
   * @param creditAmount the credit amount to remove
   */
  public void removeAmounts(BigDecimal debitAmount, BigDecimal creditAmount) {
    if (debitAmount != null) {
      this.totalDebitAmount = this.totalDebitAmount.subtract(debitAmount);
    }
    if (creditAmount != null) {
      this.totalCreditAmount = this.totalCreditAmount.subtract(creditAmount);
    }
    this.netAmount = calculateNetAmount();
  }

  /** Increments the participant count. */
  public void incrementParticipantCount() {
    this.participantCount++;
  }

  /** Decrements the participant count. */
  public void decrementParticipantCount() {
    this.participantCount = Math.max(0, this.participantCount - 1);
  }

  /** Increments the transaction count. */
  public void incrementTransactionCount() {
    this.transactionCount++;
  }

  /** Decrements the transaction count. */
  public void decrementTransactionCount() {
    this.transactionCount = Math.max(0, this.transactionCount - 1);
  }

  /** Starts the cycle processing. */
  public void startProcessing() {
    this.status = CycleStatus.PROCESSING;
    this.startTime = LocalDateTime.now();
  }

  /** Completes the cycle. */
  public void complete() {
    this.status = CycleStatus.COMPLETED;
    this.endTime = LocalDateTime.now();
  }

  /** Fails the cycle. */
  public void fail() {
    this.status = CycleStatus.FAILED;
    this.endTime = LocalDateTime.now();
  }

  /** Cancels the cycle. */
  public void cancel() {
    this.status = CycleStatus.CANCELLED;
    this.endTime = LocalDateTime.now();
  }

  /**
   * Settles the cycle.
   *
   * @param settlementDate the settlement date
   * @param valueDate the value date
   */
  public void settle(LocalDateTime settlementDate, LocalDateTime valueDate) {
    this.status = CycleStatus.SETTLED;
    this.settlementDate = settlementDate;
    this.valueDate = valueDate;
    this.endTime = LocalDateTime.now();
  }

  /**
   * Gets the cycle duration in minutes.
   *
   * @return the duration in minutes, or null if not calculable
   */
  public Long getDurationMinutes() {
    if (startTime == null || endTime == null) return null;
    return java.time.Duration.between(startTime, endTime).toMinutes();
  }

  /**
   * Gets the cycle summary.
   *
   * @return the cycle summary string
   */
  public String getSummary() {
    return String.format(
        "Cycle[%s] %s - %s (%d participants, %d transactions)",
        cycleId, cycleName, status, participantCount, transactionCount);
  }
}
