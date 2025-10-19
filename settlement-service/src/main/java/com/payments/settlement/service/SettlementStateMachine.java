package com.payments.settlement.service;

import com.payments.settlement.domain.SettlementWorkflow;
import com.payments.settlement.domain.SettlementState;
import com.payments.settlement.domain.SettlementPosition;
import com.payments.settlement.repository.SettlementWorkflowRepository;
import com.payments.settlement.repository.SettlementStateRepository;
import com.payments.settlement.repository.SettlementPositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for settlement state machine operations.
 *
 * <p>This service provides comprehensive state machine functionality for
 * settlement workflows including state transitions, validation, and coordination.
 * It implements advanced state management patterns for settlement processing.
 *
 * @since PE-409
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementStateMachine {
  
  private final SettlementWorkflowRepository settlementWorkflowRepository;
  private final SettlementStateRepository settlementStateRepository;
  private final SettlementPositionRepository settlementPositionRepository;
  
  /**
   * Transitions a workflow to the next state.
   *
   * @param workflowId the workflow ID
   * @param nextState the next state name
   * @return the updated workflow
   */
  @Transactional
  public SettlementWorkflow transitionWorkflow(Long workflowId, String nextState) {
    log.info("Transitioning workflow {} to state: {}", workflowId, nextState);
    
    SettlementWorkflow workflow = settlementWorkflowRepository.findById(workflowId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement workflow not found: " + workflowId));
    
    if (!workflow.isActive()) {
      throw new IllegalStateException("Workflow is not active: " + workflowId);
    }
    
    // Create state record
    SettlementState state = SettlementState.builder()
        .workflowId(workflowId)
        .stateName(nextState)
        .stateType(SettlementState.StateType.WORKFLOW)
        .status(SettlementState.StateStatus.ACTIVE)
        .previousState(workflow.getCurrentStep())
        .nextState(null)
        .stateTimestamp(LocalDateTime.now())
        .businessUnitId(workflow.getBusinessUnitId())
        .tenantId(workflow.getTenantId())
        .createdBy(workflow.getCreatedBy())
        .build();
    
    settlementStateRepository.save(state);
    
    // Update workflow
    workflow.setCurrentStep(nextState);
    workflow.updateProgress(nextState, BigDecimal.ZERO);
    
    // Update workflow status based on state
    updateWorkflowStatus(workflow, nextState);
    
    SettlementWorkflow savedWorkflow = settlementWorkflowRepository.save(workflow);
    
    log.info("Workflow {} transitioned to state: {}", workflowId, nextState);
    return savedWorkflow;
  }
  
  /**
   * Transitions a position to the next state.
   *
   * @param positionId the position ID
   * @param nextState the next state name
   * @return the updated position
   */
  @Transactional
  public SettlementPosition transitionPosition(Long positionId, String nextState) {
    log.info("Transitioning position {} to state: {}", positionId, nextState);
    
    SettlementPosition position = settlementPositionRepository.findById(positionId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement position not found: " + positionId));
    
    if (!position.isActive()) {
      throw new IllegalStateException("Position is not active: " + positionId);
    }
    
    // Create state record
    SettlementState state = SettlementState.builder()
        .workflowId(position.getWorkflowId())
        .stateName(nextState)
        .stateType(SettlementState.StateType.POSITION)
        .status(SettlementState.StateStatus.ACTIVE)
        .previousState(position.getStatus().name())
        .nextState(null)
        .positionId(positionId)
        .participantId(position.getParticipantId())
        .stateTimestamp(LocalDateTime.now())
        .businessUnitId(position.getBusinessUnitId())
        .tenantId(position.getTenantId())
        .createdBy(position.getCreatedBy())
        .build();
    
    settlementStateRepository.save(state);
    
    // Update position status based on state
    updatePositionStatus(position, nextState);
    
    SettlementPosition savedPosition = settlementPositionRepository.save(position);
    
    log.info("Position {} transitioned to state: {}", positionId, nextState);
    return savedPosition;
  }
  
  /**
   * Validates a state transition.
   *
   * @param currentState the current state
   * @param nextState the next state
   * @return true if valid transition, false otherwise
   */
  public boolean validateTransition(String currentState, String nextState) {
    log.debug("Validating transition from {} to {}", currentState, nextState);
    
    // Define valid state transitions
    Map<String, List<String>> validTransitions = Map.of(
        "INITIATED", List.of("VALIDATING", "PROCESSING", "FAILED", "CANCELLED"),
        "VALIDATING", List.of("PROCESSING", "FAILED", "CANCELLED"),
        "PROCESSING", List.of("SETTLING", "COMPLETED", "FAILED", "CANCELLED", "SUSPENDED"),
        "SETTLING", List.of("COMPLETED", "FAILED", "CANCELLED", "SUSPENDED"),
        "COMPLETED", List.of("REVERSED"),
        "FAILED", List.of("RETRYING", "CANCELLED"),
        "CANCELLED", List.of("REVERSED"),
        "SUSPENDED", List.of("PROCESSING", "CANCELLED"),
        "RETRYING", List.of("PROCESSING", "FAILED", "CANCELLED")
    );
    
    List<String> allowedTransitions = validTransitions.get(currentState);
    if (allowedTransitions == null) {
      log.warn("Unknown current state: {}", currentState);
      return false;
    }
    
    boolean isValid = allowedTransitions.contains(nextState);
    log.debug("Transition from {} to {} is {}", currentState, nextState, isValid ? "valid" : "invalid");
    
    return isValid;
  }
  
  /**
   * Gets the current state of a workflow.
   *
   * @param workflowId the workflow ID
   * @return the current state
   */
  @Transactional(readOnly = true)
  public Optional<SettlementState> getCurrentWorkflowState(Long workflowId) {
    return settlementStateRepository.findCurrentWorkflowState(workflowId);
  }
  
  /**
   * Gets the current state of a position.
   *
   * @param positionId the position ID
   * @return the current state
   */
  @Transactional(readOnly = true)
  public Optional<SettlementState> getCurrentPositionState(Long positionId) {
    return settlementStateRepository.findCurrentPositionState(positionId);
  }
  
  /**
   * Gets the state history for a workflow.
   *
   * @param workflowId the workflow ID
   * @return list of states
   */
  @Transactional(readOnly = true)
  public List<SettlementState> getWorkflowStateHistory(Long workflowId) {
    return settlementStateRepository.findByWorkflowIdOrderByStateTimestampDesc(workflowId);
  }
  
  /**
   * Gets the state history for a position.
   *
   * @param positionId the position ID
   * @return list of states
   */
  @Transactional(readOnly = true)
  public List<SettlementState> getPositionStateHistory(Long positionId) {
    return settlementStateRepository.findByPositionIdOrderByStateTimestampDesc(positionId);
  }
  
  /**
   * Completes a state.
   *
   * @param stateId the state ID
   */
  @Transactional
  public void completeState(Long stateId) {
    log.info("Completing state: {}", stateId);
    
    SettlementState state = settlementStateRepository.findById(stateId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement state not found: " + stateId));
    
    state.complete();
    settlementStateRepository.save(state);
    
    log.info("State {} completed", stateId);
  }
  
  /**
   * Fails a state.
   *
   * @param stateId the state ID
   * @param errorMessage the error message
   */
  @Transactional
  public void failState(Long stateId, String errorMessage) {
    log.info("Failing state: {} with error: {}", stateId, errorMessage);
    
    SettlementState state = settlementStateRepository.findById(stateId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement state not found: " + stateId));
    
    state.fail(errorMessage);
    settlementStateRepository.save(state);
    
    log.info("State {} failed with error: {}", stateId, errorMessage);
  }
  
  /**
   * Retries a state.
   *
   * @param stateId the state ID
   */
  @Transactional
  public void retryState(Long stateId) {
    log.info("Retrying state: {}", stateId);
    
    SettlementState state = settlementStateRepository.findById(stateId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement state not found: " + stateId));
    
    if (!state.canRetry()) {
      throw new IllegalStateException("State cannot be retried: " + stateId);
    }
    
    state.retry();
    settlementStateRepository.save(state);
    
    log.info("State {} retried", stateId);
  }
  
  /**
   * Suspends a state.
   *
   * @param stateId the state ID
   */
  @Transactional
  public void suspendState(Long stateId) {
    log.info("Suspending state: {}", stateId);
    
    SettlementState state = settlementStateRepository.findById(stateId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement state not found: " + stateId));
    
    state.suspend();
    settlementStateRepository.save(state);
    
    log.info("State {} suspended", stateId);
  }
  
  /**
   * Resumes a state.
   *
   * @param stateId the state ID
   */
  @Transactional
  public void resumeState(Long stateId) {
    log.info("Resuming state: {}", stateId);
    
    SettlementState state = settlementStateRepository.findById(stateId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement state not found: " + stateId));
    
    state.resume();
    settlementStateRepository.save(state);
    
    log.info("State {} resumed", stateId);
  }
  
  /**
   * Updates workflow status based on state.
   *
   * @param workflow the workflow
   * @param stateName the state name
   */
  private void updateWorkflowStatus(SettlementWorkflow workflow, String stateName) {
    switch (stateName.toUpperCase()) {
      case "VALIDATING":
        workflow.startValidation();
        break;
      case "PROCESSING":
        workflow.startProcessing();
        break;
      case "SETTLING":
        workflow.startSettlement();
        break;
      case "COMPLETED":
        workflow.complete();
        break;
      case "FAILED":
        workflow.fail("Workflow failed in state: " + stateName);
        break;
      case "CANCELLED":
        workflow.cancel();
        break;
      case "SUSPENDED":
        workflow.suspend();
        break;
      default:
        log.warn("Unknown workflow state: {}", stateName);
    }
  }
  
  /**
   * Updates position status based on state.
   *
   * @param position the position
   * @param stateName the state name
   */
  private void updatePositionStatus(SettlementPosition position, String stateName) {
    switch (stateName.toUpperCase()) {
      case "PROCESSING":
        position.startProcessing();
        break;
      case "SETTLED":
        position.settle(LocalDateTime.now(), LocalDateTime.now(), "AUTO-" + System.currentTimeMillis());
        break;
      case "FAILED":
        position.fail("Position failed in state: " + stateName);
        break;
      case "CANCELLED":
        position.cancel();
        break;
      case "REVERSED":
        position.reverse();
        break;
      default:
        log.warn("Unknown position state: {}", stateName);
    }
  }
  
  /**
   * Gets state machine statistics.
   *
   * @param workflowId the workflow ID
   * @return state machine statistics
   */
  @Transactional(readOnly = true)
  public Map<String, Object> getStateMachineStatistics(Long workflowId) {
    List<SettlementState> states = getWorkflowStateHistory(workflowId);
    
    Map<String, Object> statistics = Map.of(
        "totalStates", states.size(),
        "activeStates", states.stream().filter(SettlementState::isActive).count(),
        "completedStates", states.stream().filter(SettlementState::isCompleted).count(),
        "failedStates", states.stream().filter(SettlementState::isFailed).count(),
        "averageDurationSeconds", states.stream()
            .filter(s -> s.getDurationSeconds() != null)
            .mapToLong(SettlementState::getDurationSeconds)
            .average()
            .orElse(0.0)
    );
    
    return statistics;
  }
}
