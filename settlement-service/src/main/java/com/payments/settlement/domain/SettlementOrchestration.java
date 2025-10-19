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
 * JPA entity for settlement orchestration management.
 *
 * <p>This entity represents a settlement orchestration that coordinates
 * multiple settlement workflows and participants. It provides comprehensive
 * orchestration management including coordination, monitoring, and execution.
 *
 * @since PE-410
 */
@Entity
@Table(name = "settlement_orchestrations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementOrchestration {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "orchestration_id", nullable = false, unique = true, length = 100)
  private String orchestrationId;
  
  @Column(name = "orchestration_name", nullable = false, length = 255)
  private String orchestrationName;
  
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "orchestration_type", nullable = false, length = 20)
  private OrchestrationType orchestrationType;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private OrchestrationStatus status = OrchestrationStatus.INITIATED;
  
  @Column(name = "current_phase", length = 100)
  private String currentPhase;
  
  @Column(name = "phase_progress", precision = 5, scale = 2)
  private BigDecimal phaseProgress = BigDecimal.ZERO;
  
  @Column(name = "total_phases")
  private Integer totalPhases = 0;
  
  @Column(name = "completed_phases")
  private Integer completedPhases = 0;
  
  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;
  
  @Column(name = "end_time")
  private LocalDateTime endTime;
  
  @Column(name = "estimated_completion_time")
  private LocalDateTime estimatedCompletionTime;
  
  @Column(name = "participant_count")
  private Integer participantCount = 0;
  
  @Column(name = "workflow_count")
  private Integer workflowCount = 0;
  
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
   * Enumeration of orchestration types.
   */
  public enum OrchestrationType {
    NETTING_SETTLEMENT, POSITION_SETTLEMENT, BATCH_SETTLEMENT, REAL_TIME_SETTLEMENT, MULTI_CURRENCY_SETTLEMENT
  }
  
  /**
   * Enumeration of orchestration statuses.
   */
  public enum OrchestrationStatus {
    INITIATED, COORDINATING, EXECUTING, MONITORING, COMPLETED, FAILED, CANCELLED, SUSPENDED
  }
  
  /**
   * Checks if the orchestration is initiated.
   *
   * @return true if initiated, false otherwise
   */
  public boolean isInitiated() {
    return status == OrchestrationStatus.INITIATED;
  }
  
  /**
   * Checks if the orchestration is coordinating.
   *
   * @return true if coordinating, false otherwise
   */
  public boolean isCoordinating() {
    return status == OrchestrationStatus.COORDINATING;
  }
  
  /**
   * Checks if the orchestration is executing.
   *
   * @return true if executing, false otherwise
   */
  public boolean isExecuting() {
    return status == OrchestrationStatus.EXECUTING;
  }
  
  /**
   * Checks if the orchestration is monitoring.
   *
   * @return true if monitoring, false otherwise
   */
  public boolean isMonitoring() {
    return status == OrchestrationStatus.MONITORING;
  }
  
  /**
   * Checks if the orchestration is completed.
   *
   * @return true if completed, false otherwise
   */
  public boolean isCompleted() {
    return status == OrchestrationStatus.COMPLETED;
  }
  
  /**
   * Checks if the orchestration is failed.
   *
   * @return true if failed, false otherwise
   */
  public boolean isFailed() {
    return status == OrchestrationStatus.FAILED;
  }
  
  /**
   * Checks if the orchestration is cancelled.
   *
   * @return true if cancelled, false otherwise
   */
  public boolean isCancelled() {
    return status == OrchestrationStatus.CANCELLED;
  }
  
  /**
   * Checks if the orchestration is suspended.
   *
   * @return true if suspended, false otherwise
   */
  public boolean isSuspended() {
    return status == OrchestrationStatus.SUSPENDED;
  }
  
  /**
   * Checks if the orchestration is active (not completed, failed, or cancelled).
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
    if (totalPhases == null || totalPhases == 0) return BigDecimal.ZERO;
    return BigDecimal.valueOf(completedPhases)
        .divide(BigDecimal.valueOf(totalPhases), 4, BigDecimal.ROUND_HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }
  
  /**
   * Calculates the orchestration duration in minutes.
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
    if (startTime == null || totalPhases == null || totalPhases == 0) return null;
    if (completedPhases == null || completedPhases == 0) return null;
    
    long elapsedMinutes = java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
    if (elapsedMinutes == 0) return null;
    
    double progressRatio = (double) completedPhases / totalPhases;
    if (progressRatio == 0) return null;
    
    long estimatedTotalMinutes = (long) (elapsedMinutes / progressRatio);
    return startTime.plusMinutes(estimatedTotalMinutes);
  }
  
  /**
   * Updates the orchestration progress.
   *
   * @param currentPhase the current phase
   * @param phaseProgress the phase progress percentage
   */
  public void updateProgress(String currentPhase, BigDecimal phaseProgress) {
    this.currentPhase = currentPhase;
    this.phaseProgress = phaseProgress;
    this.phaseProgress = this.phaseProgress != null ? this.phaseProgress : BigDecimal.ZERO;
  }
  
  /**
   * Completes a phase in the orchestration.
   */
  public void completePhase() {
    if (completedPhases == null) completedPhases = 0;
    completedPhases++;
    this.phaseProgress = BigDecimal.ZERO;
  }
  
  /**
   * Starts the orchestration coordination.
   */
  public void startCoordination() {
    this.status = OrchestrationStatus.COORDINATING;
    this.startTime = LocalDateTime.now();
  }
  
  /**
   * Moves to execution phase.
   */
  public void startExecution() {
    this.status = OrchestrationStatus.EXECUTING;
  }
  
  /**
   * Moves to monitoring phase.
   */
  public void startMonitoring() {
    this.status = OrchestrationStatus.MONITORING;
  }
  
  /**
   * Completes the orchestration.
   */
  public void complete() {
    this.status = OrchestrationStatus.COMPLETED;
    this.endTime = LocalDateTime.now();
    this.completedPhases = totalPhases;
    this.phaseProgress = BigDecimal.valueOf(100);
  }
  
  /**
   * Fails the orchestration.
   *
   * @param errorMessage the error message
   */
  public void fail(String errorMessage) {
    this.status = OrchestrationStatus.FAILED;
    this.endTime = LocalDateTime.now();
    this.errorMessage = errorMessage;
  }
  
  /**
   * Cancels the orchestration.
   */
  public void cancel() {
    this.status = OrchestrationStatus.CANCELLED;
    this.endTime = LocalDateTime.now();
  }
  
  /**
   * Suspends the orchestration.
   */
  public void suspend() {
    this.status = OrchestrationStatus.SUSPENDED;
  }
  
  /**
   * Resumes the orchestration.
   */
  public void resume() {
    if (this.status == OrchestrationStatus.SUSPENDED) {
      this.status = OrchestrationStatus.COORDINATING;
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
   * Checks if the orchestration can be retried.
   *
   * @return true if can be retried, false otherwise
   */
  public boolean canRetry() {
    if (retryCount == null) retryCount = 0;
    if (maxRetries == null) maxRetries = 3;
    return retryCount < maxRetries;
  }
  
  /**
   * Updates the orchestration with settlement information.
   *
   * @param participantCount the participant count
   * @param workflowCount the workflow count
   * @param positionCount the position count
   * @param totalSettlementAmount the total settlement amount
   * @param currency the currency
   */
  public void updateSettlementInfo(Integer participantCount, Integer workflowCount, Integer positionCount, 
                                  BigDecimal totalSettlementAmount, String currency) {
    this.participantCount = participantCount;
    this.workflowCount = workflowCount;
    this.positionCount = positionCount;
    this.totalSettlementAmount = totalSettlementAmount;
    this.currency = currency;
  }
  
  /**
   * Gets the orchestration summary.
   *
   * @return the orchestration summary string
   */
  public String getSummary() {
    return String.format("Orchestration[%s] %s - %s (%d/%d phases, %.2f%%)", 
        orchestrationId, orchestrationName, status, completedPhases, totalPhases, 
        calculateProgress());
  }
}
