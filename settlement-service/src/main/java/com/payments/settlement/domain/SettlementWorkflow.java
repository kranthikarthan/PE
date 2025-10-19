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
import java.util.List;

/**
 * JPA entity for settlement workflow management.
 *
 * <p>This entity represents a settlement workflow that orchestrates the
 * settlement process for netting positions. It provides comprehensive
 * workflow management including state tracking, participant coordination,
 * and settlement execution.
 *
 * @since PE-409
 */
@Entity
@Table(name = "settlement_workflows")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementWorkflow {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "workflow_id", nullable = false, unique = true, length = 100)
  private String workflowId;
  
  @Column(name = "workflow_name", nullable = false, length = 255)
  private String workflowName;
  
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;
  
  @Column(name = "netting_cycle_id", nullable = false)
  private Long nettingCycleId;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "workflow_type", nullable = false, length = 20)
  private WorkflowType workflowType;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private WorkflowStatus status = WorkflowStatus.INITIATED;
  
  @Column(name = "current_step", length = 100)
  private String currentStep;
  
  @Column(name = "step_progress", precision = 5, scale = 2)
  private BigDecimal stepProgress = BigDecimal.ZERO;
  
  @Column(name = "total_steps")
  private Integer totalSteps = 0;
  
  @Column(name = "completed_steps")
  private Integer completedSteps = 0;
  
  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;
  
  @Column(name = "end_time")
  private LocalDateTime endTime;
  
  @Column(name = "estimated_completion_time")
  private LocalDateTime estimatedCompletionTime;
  
  @Column(name = "participant_count")
  private Integer participantCount = 0;
  
  @Column(name = "position_count")
  private Integer positionCount = 0;
  
  @Column(name = "total_settlement_amount", precision = 19, scale = 4)
  private BigDecimal totalSettlementAmount = BigDecimal.ZERO;
  
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
  
  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;
  
  @Column(name = "retry_count")
  private Integer retryCount = 0;
  
  @Column(name = "max_retries")
  private Integer maxRetries = 3;
  
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
   * Enumeration of workflow types.
   */
  public enum WorkflowType {
    NETTING_SETTLEMENT, POSITION_SETTLEMENT, BATCH_SETTLEMENT, REAL_TIME_SETTLEMENT
  }
  
  /**
   * Enumeration of workflow statuses.
   */
  public enum WorkflowStatus {
    INITIATED, VALIDATING, PROCESSING, SETTLING, COMPLETED, FAILED, CANCELLED, SUSPENDED
  }
  
  /**
   * Checks if the workflow is initiated.
   *
   * @return true if initiated, false otherwise
   */
  public boolean isInitiated() {
    return status == WorkflowStatus.INITIATED;
  }
  
  /**
   * Checks if the workflow is validating.
   *
   * @return true if validating, false otherwise
   */
  public boolean isValidating() {
    return status == WorkflowStatus.VALIDATING;
  }
  
  /**
   * Checks if the workflow is processing.
   *
   * @return true if processing, false otherwise
   */
  public boolean isProcessing() {
    return status == WorkflowStatus.PROCESSING;
  }
  
  /**
   * Checks if the workflow is settling.
   *
   * @return true if settling, false otherwise
   */
  public boolean isSettling() {
    return status == WorkflowStatus.SETTLING;
  }
  
  /**
   * Checks if the workflow is completed.
   *
   * @return true if completed, false otherwise
   */
  public boolean isCompleted() {
    return status == WorkflowStatus.COMPLETED;
  }
  
  /**
   * Checks if the workflow is failed.
   *
   * @return true if failed, false otherwise
   */
  public boolean isFailed() {
    return status == WorkflowStatus.FAILED;
  }
  
  /**
   * Checks if the workflow is cancelled.
   *
   * @return true if cancelled, false otherwise
   */
  public boolean isCancelled() {
    return status == WorkflowStatus.CANCELLED;
  }
  
  /**
   * Checks if the workflow is suspended.
   *
   * @return true if suspended, false otherwise
   */
  public boolean isSuspended() {
    return status == WorkflowStatus.SUSPENDED;
  }
  
  /**
   * Checks if the workflow is active (not completed, failed, or cancelled).
   *
   * @return true if active, false otherwise
   */
  public boolean isActive() {
    return !isCompleted() && !isFailed() && !isCancelled();
  }
  
  /**
   * Calculates the overall progress percentage.
   *
   * @return the progress percentage
   */
  public BigDecimal calculateProgress() {
    if (totalSteps == null || totalSteps == 0) return BigDecimal.ZERO;
    return BigDecimal.valueOf(completedSteps)
        .divide(BigDecimal.valueOf(totalSteps), 4, BigDecimal.ROUND_HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }
  
  /**
   * Calculates the workflow duration in minutes.
   *
   * @return the duration in minutes, or null if not calculable
   */
  public Long calculateDurationMinutes() {
    if (startTime == null || endTime == null) return null;
    return java.time.Duration.between(startTime, endTime).toMinutes();
  }
  
  /**
   * Calculates the estimated completion time.
   *
   * @return the estimated completion time, or null if not calculable
   */
  public LocalDateTime calculateEstimatedCompletionTime() {
    if (startTime == null || totalSteps == null || totalSteps == 0) return null;
    if (completedSteps == null || completedSteps == 0) return null;
    
    long elapsedMinutes = java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
    if (elapsedMinutes == 0) return null;
    
    double progressRatio = (double) completedSteps / totalSteps;
    if (progressRatio == 0) return null;
    
    long estimatedTotalMinutes = (long) (elapsedMinutes / progressRatio);
    return startTime.plusMinutes(estimatedTotalMinutes);
  }
  
  /**
   * Updates the workflow progress.
   *
   * @param currentStep the current step
   * @param stepProgress the step progress percentage
   */
  public void updateProgress(String currentStep, BigDecimal stepProgress) {
    this.currentStep = currentStep;
    this.stepProgress = stepProgress;
    this.stepProgress = this.stepProgress != null ? this.stepProgress : BigDecimal.ZERO;
  }
  
  /**
   * Completes a step in the workflow.
   */
  public void completeStep() {
    if (completedSteps == null) completedSteps = 0;
    completedSteps++;
    this.stepProgress = BigDecimal.ZERO;
  }
  
  /**
   * Starts the workflow processing.
   */
  public void startProcessing() {
    this.status = WorkflowStatus.PROCESSING;
    this.startTime = LocalDateTime.now();
  }
  
  /**
   * Moves to validation phase.
   */
  public void startValidation() {
    this.status = WorkflowStatus.VALIDATING;
  }
  
  /**
   * Moves to settlement phase.
   */
  public void startSettlement() {
    this.status = WorkflowStatus.SETTLING;
  }
  
  /**
   * Completes the workflow.
   */
  public void complete() {
    this.status = WorkflowStatus.COMPLETED;
    this.endTime = LocalDateTime.now();
    this.completedSteps = totalSteps;
    this.stepProgress = BigDecimal.valueOf(100);
  }
  
  /**
   * Fails the workflow.
   *
   * @param errorMessage the error message
   */
  public void fail(String errorMessage) {
    this.status = WorkflowStatus.FAILED;
    this.endTime = LocalDateTime.now();
    this.errorMessage = errorMessage;
  }
  
  /**
   * Cancels the workflow.
   */
  public void cancel() {
    this.status = WorkflowStatus.CANCELLED;
    this.endTime = LocalDateTime.now();
  }
  
  /**
   * Suspends the workflow.
   */
  public void suspend() {
    this.status = WorkflowStatus.SUSPENDED;
  }
  
  /**
   * Resumes the workflow.
   */
  public void resume() {
    if (this.status == WorkflowStatus.SUSPENDED) {
      this.status = WorkflowStatus.PROCESSING;
    }
  }
  
  /**
   * Increments the retry count.
   */
  public void incrementRetryCount() {
    if (retryCount == null) retryCount = 0;
    retryCount++;
  }
  
  /**
   * Checks if the workflow can be retried.
   *
   * @return true if can be retried, false otherwise
   */
  public boolean canRetry() {
    if (retryCount == null) retryCount = 0;
    if (maxRetries == null) maxRetries = 3;
    return retryCount < maxRetries;
  }
  
  /**
   * Updates the workflow with settlement information.
   *
   * @param participantCount the participant count
   * @param positionCount the position count
   * @param totalSettlementAmount the total settlement amount
   * @param currency the currency
   */
  public void updateSettlementInfo(Integer participantCount, Integer positionCount, 
                                  BigDecimal totalSettlementAmount, String currency) {
    this.participantCount = participantCount;
    this.positionCount = positionCount;
    this.totalSettlementAmount = totalSettlementAmount;
    this.currency = currency;
  }
  
  /**
   * Gets the workflow summary.
   *
   * @return the workflow summary string
   */
  public String getSummary() {
    return String.format("Workflow[%s] %s - %s (%d/%d steps, %.2f%%)", 
        workflowId, workflowName, status, completedSteps, totalSteps, 
        calculateProgress());
  }
}
