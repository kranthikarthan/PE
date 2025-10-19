package com.payments.settlement.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity for settlement state management.
 *
 * <p>This entity represents the state of a settlement workflow at a specific
 * point in time. It provides comprehensive state tracking including state
 * transitions, participant states, and state validation.
 *
 * @since PE-409
 */
@Entity
@Table(name = "settlement_states")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementState {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "workflow_id", nullable = false)
  private Long workflowId;
  
  @Column(name = "state_name", nullable = false, length = 100)
  private String stateName;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "state_type", nullable = false, length = 20)
  private StateType stateType;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private StateStatus status = StateStatus.ACTIVE;
  
  @Column(name = "previous_state", length = 100)
  private String previousState;
  
  @Column(name = "next_state", length = 100)
  private String nextState;
  
  @Column(name = "state_data", columnDefinition = "JSONB")
  private String stateData;
  
  @Column(name = "participant_id", length = 50)
  private String participantId;
  
  @Column(name = "position_id")
  private Long positionId;
  
  @Column(name = "transaction_id", length = 100)
  private String transactionId;
  
  @Column(name = "state_timestamp", nullable = false)
  private LocalDateTime stateTimestamp;
  
  @Column(name = "duration_seconds")
  private Long durationSeconds;
  
  @Column(name = "retry_count")
  private Integer retryCount = 0;
  
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
   * Enumeration of state types.
   */
  public enum StateType {
    WORKFLOW, PARTICIPANT, POSITION, TRANSACTION, SYSTEM
  }
  
  /**
   * Enumeration of state statuses.
   */
  public enum StateStatus {
    ACTIVE, COMPLETED, FAILED, CANCELLED, SUSPENDED, RETRYING
  }
  
  /**
   * Checks if the state is active.
   *
   * @return true if active, false otherwise
   */
  public boolean isActive() {
    return status == StateStatus.ACTIVE;
  }
  
  /**
   * Checks if the state is completed.
   *
   * @return true if completed, false otherwise
   */
  public boolean isCompleted() {
    return status == StateStatus.COMPLETED;
  }
  
  /**
   * Checks if the state is failed.
   *
   * @return true if failed, false otherwise
   */
  public boolean isFailed() {
    return status == StateStatus.FAILED;
  }
  
  /**
   * Checks if the state is cancelled.
   *
   * @return true if cancelled, false otherwise
   */
  public boolean isCancelled() {
    return status == StateStatus.CANCELLED;
  }
  
  /**
   * Checks if the state is suspended.
   *
   * @return true if suspended, false otherwise
   */
  public boolean isSuspended() {
    return status == StateStatus.SUSPENDED;
  }
  
  /**
   * Checks if the state is retrying.
   *
   * @return true if retrying, false otherwise
   */
  public boolean isRetrying() {
    return status == StateStatus.RETRYING;
  }
  
  /**
   * Checks if the state is a workflow state.
   *
   * @return true if workflow state, false otherwise
   */
  public boolean isWorkflowState() {
    return stateType == StateType.WORKFLOW;
  }
  
  /**
   * Checks if the state is a participant state.
   *
   * @return true if participant state, false otherwise
   */
  public boolean isParticipantState() {
    return stateType == StateType.PARTICIPANT;
  }
  
  /**
   * Checks if the state is a position state.
   *
   * @return true if position state, false otherwise
   */
  public boolean isPositionState() {
    return stateType == StateType.POSITION;
  }
  
  /**
   * Checks if the state is a transaction state.
   *
   * @return true if transaction state, false otherwise
   */
  public boolean isTransactionState() {
    return stateType == StateType.TRANSACTION;
  }
  
  /**
   * Checks if the state is a system state.
   *
   * @return true if system state, false otherwise
   */
  public boolean isSystemState() {
    return stateType == StateType.SYSTEM;
  }
  
  /**
   * Completes the state.
   */
  public void complete() {
    this.status = StateStatus.COMPLETED;
    this.durationSeconds = calculateDurationSeconds();
  }
  
  /**
   * Fails the state.
   *
   * @param errorMessage the error message
   */
  public void fail(String errorMessage) {
    this.status = StateStatus.FAILED;
    this.errorMessage = errorMessage;
    this.durationSeconds = calculateDurationSeconds();
  }
  
  /**
   * Cancels the state.
   */
  public void cancel() {
    this.status = StateStatus.CANCELLED;
    this.durationSeconds = calculateDurationSeconds();
  }
  
  /**
   * Suspends the state.
   */
  public void suspend() {
    this.status = StateStatus.SUSPENDED;
  }
  
  /**
   * Resumes the state.
   */
  public void resume() {
    if (this.status == StateStatus.SUSPENDED) {
      this.status = StateStatus.ACTIVE;
    }
  }
  
  /**
   * Retries the state.
   */
  public void retry() {
    this.status = StateStatus.RETRYING;
    if (retryCount == null) retryCount = 0;
    retryCount++;
  }
  
  /**
   * Calculates the duration in seconds.
   *
   * @return the duration in seconds, or null if not calculable
   */
  public Long calculateDurationSeconds() {
    if (stateTimestamp == null) return null;
    LocalDateTime endTime = LocalDateTime.now();
    return java.time.Duration.between(stateTimestamp, endTime).getSeconds();
  }
  
  /**
   * Transitions to the next state.
   *
   * @param nextState the next state name
   */
  public void transitionTo(String nextState) {
    this.previousState = this.stateName;
    this.stateName = nextState;
    this.stateTimestamp = LocalDateTime.now();
    this.status = StateStatus.ACTIVE;
  }
  
  /**
   * Updates the state data.
   *
   * @param stateData the new state data
   */
  public void updateStateData(String stateData) {
    this.stateData = stateData;
  }
  
  /**
   * Gets the state summary.
   *
   * @return the state summary string
   */
  public String getSummary() {
    return String.format("State[%s] %s - %s (%s)", 
        stateName, stateType, status, 
        stateTimestamp != null ? stateTimestamp.toString() : "N/A");
  }
}
