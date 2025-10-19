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
 * JPA entity for settlement position management.
 *
 * <p>This entity represents a settlement position that tracks the settlement
 * status and progress for individual netting positions. It provides comprehensive
 * position tracking including settlement amounts, participant coordination,
 * and settlement status management.
 *
 * @since PE-409
 */
@Entity
@Table(name = "settlement_positions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementPosition {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "workflow_id", nullable = false)
  private Long workflowId;
  
  @Column(name = "netting_position_id", nullable = false)
  private Long nettingPositionId;
  
  @Column(name = "participant_id", nullable = false, length = 50)
  private String participantId;
  
  @Column(name = "participant_name", length = 255)
  private String participantName;
  
  @Column(name = "currency", nullable = false, length = 3)
  private String currency;
  
  @Column(name = "net_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal netAmount;
  
  @Column(name = "settlement_amount", precision = 19, scale = 4)
  private BigDecimal settlementAmount;
  
  @Column(name = "settled_amount", precision = 19, scale = 4)
  private BigDecimal settledAmount = BigDecimal.ZERO;
  
  @Column(name = "remaining_amount", precision = 19, scale = 4)
  private BigDecimal remainingAmount;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "position_type", nullable = false, length = 20)
  private PositionType positionType;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private SettlementStatus status = SettlementStatus.PENDING;
  
  @Column(name = "settlement_date")
  private LocalDateTime settlementDate;
  
  @Column(name = "value_date")
  private LocalDateTime valueDate;
  
  @Column(name = "settlement_reference", length = 100)
  private String settlementReference;
  
  @Column(name = "priority", nullable = false)
  private Integer priority = 5;
  
  @Column(name = "retry_count")
  private Integer retryCount = 0;
  
  @Column(name = "max_retries")
  private Integer maxRetries = 3;
  
  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;
  
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
   * Enumeration of position types.
   */
  public enum PositionType {
    DEBIT, CREDIT, ZERO
  }
  
  /**
   * Enumeration of settlement statuses.
   */
  public enum SettlementStatus {
    PENDING, PROCESSING, SETTLED, FAILED, CANCELLED, REVERSED
  }
  
  /**
   * Checks if the position is pending.
   *
   * @return true if pending, false otherwise
   */
  public boolean isPending() {
    return status == SettlementStatus.PENDING;
  }
  
  /**
   * Checks if the position is processing.
   *
   * @return true if processing, false otherwise
   */
  public boolean isProcessing() {
    return status == SettlementStatus.PROCESSING;
  }
  
  /**
   * Checks if the position is settled.
   *
   * @return true if settled, false otherwise
   */
  public boolean isSettled() {
    return status == SettlementStatus.SETTLED;
  }
  
  /**
   * Checks if the position is failed.
   *
   * @return true if failed, false otherwise
   */
  public boolean isFailed() {
    return status == SettlementStatus.FAILED;
  }
  
  /**
   * Checks if the position is cancelled.
   *
   * @return true if cancelled, false otherwise
   */
  public boolean isCancelled() {
    return status == SettlementStatus.CANCELLED;
  }
  
  /**
   * Checks if the position is reversed.
   *
   * @return true if reversed, false otherwise
   */
  public boolean isReversed() {
    return status == SettlementStatus.REVERSED;
  }
  
  /**
   * Checks if the position is active (not failed, cancelled, or reversed).
   *
   * @return true if active, false otherwise
   */
  public boolean isActive() {
    return !isFailed() && !isCancelled() && !isReversed();
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
   * Calculates the remaining amount.
   *
   * @return the remaining amount
   */
  public BigDecimal calculateRemainingAmount() {
    if (settlementAmount == null) settlementAmount = netAmount;
    if (settledAmount == null) settledAmount = BigDecimal.ZERO;
    
    return settlementAmount.subtract(settledAmount);
  }
  
  /**
   * Calculates the settlement progress percentage.
   *
   * @return the progress percentage
   */
  public BigDecimal calculateProgress() {
    if (settlementAmount == null || settlementAmount.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    
    if (settledAmount == null) settledAmount = BigDecimal.ZERO;
    
    return settledAmount.divide(settlementAmount, 4, BigDecimal.ROUND_HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }
  
  /**
   * Checks if the position is fully settled.
   *
   * @return true if fully settled, false otherwise
   */
  public boolean isFullySettled() {
    return calculateRemainingAmount().compareTo(BigDecimal.ZERO) == 0;
  }
  
  /**
   * Checks if the position is partially settled.
   *
   * @return true if partially settled, false otherwise
   */
  public boolean isPartiallySettled() {
    if (settledAmount == null) settledAmount = BigDecimal.ZERO;
    return settledAmount.compareTo(BigDecimal.ZERO) > 0 && !isFullySettled();
  }
  
  /**
   * Updates the settlement amount.
   *
   * @param settlementAmount the new settlement amount
   */
  public void updateSettlementAmount(BigDecimal settlementAmount) {
    this.settlementAmount = settlementAmount;
    this.remainingAmount = calculateRemainingAmount();
  }
  
  /**
   * Adds to the settled amount.
   *
   * @param amount the amount to add
   */
  public void addSettledAmount(BigDecimal amount) {
    if (amount == null) return;
    if (settledAmount == null) settledAmount = BigDecimal.ZERO;
    
    this.settledAmount = this.settledAmount.add(amount);
    this.remainingAmount = calculateRemainingAmount();
  }
  
  /**
   * Starts processing the position.
   */
  public void startProcessing() {
    this.status = SettlementStatus.PROCESSING;
  }
  
  /**
   * Settles the position.
   *
   * @param settlementDate the settlement date
   * @param valueDate the value date
   * @param settlementReference the settlement reference
   */
  public void settle(LocalDateTime settlementDate, LocalDateTime valueDate, String settlementReference) {
    this.status = SettlementStatus.SETTLED;
    this.settlementDate = settlementDate;
    this.valueDate = valueDate;
    this.settlementReference = settlementReference;
    this.settledAmount = settlementAmount;
    this.remainingAmount = BigDecimal.ZERO;
  }
  
  /**
   * Fails the position.
   *
   * @param errorMessage the error message
   */
  public void fail(String errorMessage) {
    this.status = SettlementStatus.FAILED;
    this.errorMessage = errorMessage;
  }
  
  /**
   * Cancels the position.
   */
  public void cancel() {
    this.status = SettlementStatus.CANCELLED;
  }
  
  /**
   * Reverses the position.
   */
  public void reverse() {
    this.status = SettlementStatus.REVERSED;
  }
  
  /**
   * Increments the retry count.
   */
  public void incrementRetryCount() {
    if (retryCount == null) retryCount = 0;
    retryCount++;
  }
  
  /**
   * Checks if the position can be retried.
   *
   * @return true if can be retried, false otherwise
   */
  public boolean canRetry() {
    if (retryCount == null) retryCount = 0;
    if (maxRetries == null) maxRetries = 3;
    return retryCount < maxRetries;
  }
  
  /**
   * Gets the position summary.
   *
   * @return the position summary string
   */
  public String getSummary() {
    return String.format("SettlementPosition[%s] %s %s - %s (%.2f%%)", 
        participantId, currency, netAmount, status, calculateProgress());
  }
}
