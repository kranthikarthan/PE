package com.payments.settlement.service;

import com.payments.domain.settlement.SettlementOrchestration;
import com.payments.settlement.repository.SettlementOrchestrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for settlement orchestration operations.
 *
 * <p>This service provides comprehensive orchestration functionality for
 * settlement workflows including coordination, monitoring, and execution.
 * It implements advanced orchestration patterns for settlement processing.
 *
 * @since PE-410
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementOrchestrationService {
  
  private final SettlementOrchestrationRepository settlementOrchestrationRepository;
  
  /**
   * Creates a new settlement orchestration.
   *
   * @param orchestration the orchestration to create
   * @return the created orchestration
   */
  @Transactional
  public SettlementOrchestration createOrchestration(SettlementOrchestration orchestration) {
    log.info("Creating settlement orchestration: {}", orchestration.getOrchestrationName());
    
    orchestration.setStatus(SettlementOrchestration.OrchestrationStatus.INITIATED);
    orchestration.setStartTime(LocalDateTime.now());
    
    SettlementOrchestration savedOrchestration = settlementOrchestrationRepository.save(orchestration);
    
    log.info("Settlement orchestration created with ID: {}", savedOrchestration.getId());
    return savedOrchestration;
  }
  
  /**
   * Starts coordination for an orchestration.
   *
   * @param orchestrationId the orchestration ID
   * @return the updated orchestration
   */
  @Transactional
  public SettlementOrchestration startCoordination(Long orchestrationId) {
    log.info("Starting coordination for orchestration: {}", orchestrationId);
    
    SettlementOrchestration orchestration = settlementOrchestrationRepository.findById(orchestrationId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement orchestration not found: " + orchestrationId));
    
    if (!orchestration.isActive()) {
      throw new IllegalStateException("Orchestration is not active: " + orchestrationId);
    }
    
    orchestration.startCoordination();
    SettlementOrchestration savedOrchestration = settlementOrchestrationRepository.save(orchestration);
    
    log.info("Coordination started for orchestration: {}", orchestrationId);
    return savedOrchestration;
  }
  
  /**
   * Moves orchestration to execution phase.
   *
   * @param orchestrationId the orchestration ID
   * @return the updated orchestration
   */
  @Transactional
  public SettlementOrchestration startExecution(Long orchestrationId) {
    log.info("Starting execution for orchestration: {}", orchestrationId);
    
    SettlementOrchestration orchestration = settlementOrchestrationRepository.findById(orchestrationId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement orchestration not found: " + orchestrationId));
    
    if (!orchestration.isActive()) {
      throw new IllegalStateException("Orchestration is not active: " + orchestrationId);
    }
    
    orchestration.startExecution();
    SettlementOrchestration savedOrchestration = settlementOrchestrationRepository.save(orchestration);
    
    log.info("Execution started for orchestration: {}", orchestrationId);
    return savedOrchestration;
  }
  
  /**
   * Moves orchestration to monitoring phase.
   *
   * @param orchestrationId the orchestration ID
   * @return the updated orchestration
   */
  @Transactional
  public SettlementOrchestration startMonitoring(Long orchestrationId) {
    log.info("Starting monitoring for orchestration: {}", orchestrationId);
    
    SettlementOrchestration orchestration = settlementOrchestrationRepository.findById(orchestrationId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement orchestration not found: " + orchestrationId));
    
    if (!orchestration.isActive()) {
      throw new IllegalStateException("Orchestration is not active: " + orchestrationId);
    }
    
    orchestration.startMonitoring();
    SettlementOrchestration savedOrchestration = settlementOrchestrationRepository.save(orchestration);
    
    log.info("Monitoring started for orchestration: {}", orchestrationId);
    return savedOrchestration;
  }
  
  /**
   * Completes an orchestration.
   *
   * @param orchestrationId the orchestration ID
   * @return the updated orchestration
   */
  @Transactional
  public SettlementOrchestration completeOrchestration(Long orchestrationId) {
    log.info("Completing orchestration: {}", orchestrationId);
    
    SettlementOrchestration orchestration = settlementOrchestrationRepository.findById(orchestrationId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement orchestration not found: " + orchestrationId));
    
    if (!orchestration.isActive()) {
      throw new IllegalStateException("Orchestration is not active: " + orchestrationId);
    }
    
    orchestration.complete();
    SettlementOrchestration savedOrchestration = settlementOrchestrationRepository.save(orchestration);
    
    log.info("Orchestration completed: {}", orchestrationId);
    return savedOrchestration;
  }
  
  /**
   * Fails an orchestration.
   *
   * @param orchestrationId the orchestration ID
   * @param errorMessage the error message
   * @return the updated orchestration
   */
  @Transactional
  public SettlementOrchestration failOrchestration(Long orchestrationId, String errorMessage) {
    log.info("Failing orchestration: {} with error: {}", orchestrationId, errorMessage);
    
    SettlementOrchestration orchestration = settlementOrchestrationRepository.findById(orchestrationId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement orchestration not found: " + orchestrationId));
    
    orchestration.fail(errorMessage);
    SettlementOrchestration savedOrchestration = settlementOrchestrationRepository.save(orchestration);
    
    log.info("Orchestration failed: {}", orchestrationId);
    return savedOrchestration;
  }
  
  /**
   * Cancels an orchestration.
   *
   * @param orchestrationId the orchestration ID
   * @return the updated orchestration
   */
  @Transactional
  public SettlementOrchestration cancelOrchestration(Long orchestrationId) {
    log.info("Cancelling orchestration: {}", orchestrationId);
    
    SettlementOrchestration orchestration = settlementOrchestrationRepository.findById(orchestrationId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement orchestration not found: " + orchestrationId));
    
    orchestration.cancel();
    SettlementOrchestration savedOrchestration = settlementOrchestrationRepository.save(orchestration);
    
    log.info("Orchestration cancelled: {}", orchestrationId);
    return savedOrchestration;
  }
  
  /**
   * Suspends an orchestration.
   *
   * @param orchestrationId the orchestration ID
   * @return the updated orchestration
   */
  @Transactional
  public SettlementOrchestration suspendOrchestration(Long orchestrationId) {
    log.info("Suspending orchestration: {}", orchestrationId);
    
    SettlementOrchestration orchestration = settlementOrchestrationRepository.findById(orchestrationId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement orchestration not found: " + orchestrationId));
    
    orchestration.suspend();
    SettlementOrchestration savedOrchestration = settlementOrchestrationRepository.save(orchestration);
    
    log.info("Orchestration suspended: {}", orchestrationId);
    return savedOrchestration;
  }
  
  /**
   * Resumes an orchestration.
   *
   * @param orchestrationId the orchestration ID
   * @return the updated orchestration
   */
  @Transactional
  public SettlementOrchestration resumeOrchestration(Long orchestrationId) {
    log.info("Resuming orchestration: {}", orchestrationId);
    
    SettlementOrchestration orchestration = settlementOrchestrationRepository.findById(orchestrationId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement orchestration not found: " + orchestrationId));
    
    orchestration.resume();
    SettlementOrchestration savedOrchestration = settlementOrchestrationRepository.save(orchestration);
    
    log.info("Orchestration resumed: {}", orchestrationId);
    return savedOrchestration;
  }
  
  /**
   * Updates orchestration progress.
   *
   * @param orchestrationId the orchestration ID
   * @param currentPhase the current phase
   * @param phaseProgress the phase progress percentage
   * @return the updated orchestration
   */
  @Transactional
  public SettlementOrchestration updateProgress(Long orchestrationId, String currentPhase, BigDecimal phaseProgress) {
    log.debug("Updating progress for orchestration: {} - {} ({}%)", orchestrationId, currentPhase, phaseProgress);
    
    SettlementOrchestration orchestration = settlementOrchestrationRepository.findById(orchestrationId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement orchestration not found: " + orchestrationId));
    
    orchestration.updateProgress(currentPhase, phaseProgress);
    SettlementOrchestration savedOrchestration = settlementOrchestrationRepository.save(orchestration);
    
    return savedOrchestration;
  }
  
  /**
   * Completes a phase in the orchestration.
   *
   * @param orchestrationId the orchestration ID
   * @return the updated orchestration
   */
  @Transactional
  public SettlementOrchestration completePhase(Long orchestrationId) {
    log.info("Completing phase for orchestration: {}", orchestrationId);
    
    SettlementOrchestration orchestration = settlementOrchestrationRepository.findById(orchestrationId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement orchestration not found: " + orchestrationId));
    
    orchestration.completePhase();
    SettlementOrchestration savedOrchestration = settlementOrchestrationRepository.save(orchestration);
    
    log.info("Phase completed for orchestration: {}", orchestrationId);
    return savedOrchestration;
  }
  
  /**
   * Gets an orchestration by ID.
   *
   * @param orchestrationId the orchestration ID
   * @return the orchestration
   */
  @Transactional(readOnly = true)
  public Optional<SettlementOrchestration> getOrchestration(Long orchestrationId) {
    return settlementOrchestrationRepository.findById(orchestrationId);
  }
  
  /**
   * Gets orchestration by orchestration ID.
   *
   * @param orchestrationId the orchestration ID
   * @return the orchestration
   */
  @Transactional(readOnly = true)
  public Optional<SettlementOrchestration> getOrchestrationByOrchestrationId(String orchestrationId) {
    return settlementOrchestrationRepository.findByOrchestrationId(orchestrationId);
  }
  
  /**
   * Gets orchestration statistics.
   *
   * @param orchestrationId the orchestration ID
   * @return orchestration statistics
   */
  @Transactional(readOnly = true)
  public Map<String, Object> getOrchestrationStatistics(Long orchestrationId) {
    SettlementOrchestration orchestration = settlementOrchestrationRepository.findById(orchestrationId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement orchestration not found: " + orchestrationId));
    
    Map<String, Object> statistics = Map.of(
        "orchestrationId", orchestration.getOrchestrationId(),
        "orchestrationName", orchestration.getOrchestrationName(),
        "status", orchestration.getStatus(),
        "currentPhase", orchestration.getCurrentPhase(),
        "progress", orchestration.calculateProgress(),
        "totalPhases", orchestration.getTotalPhases(),
        "completedPhases", orchestration.getCompletedPhases(),
        "participantCount", orchestration.getParticipantCount(),
        "workflowCount", orchestration.getWorkflowCount(),
        "positionCount", orchestration.getPositionCount(),
        "totalSettlementAmount", orchestration.getTotalSettlementAmount(),
        "currency", orchestration.getCurrency(),
        "durationMinutes", orchestration.calculateDurationMinutes(),
        "estimatedCompletionTime", orchestration.calculateEstimatedCompletionTime()
    );
    
    return statistics;
  }
  
  /**
   * Gets orchestration summary.
   *
   * @param orchestrationId the orchestration ID
   * @return the orchestration summary
   */
  @Transactional(readOnly = true)
  public String getOrchestrationSummary(Long orchestrationId) {
    SettlementOrchestration orchestration = settlementOrchestrationRepository.findById(orchestrationId)
        .orElseThrow(() -> new IllegalArgumentException("Settlement orchestration not found: " + orchestrationId));
    
    return orchestration.getSummary();
  }
}
