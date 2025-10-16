package com.payments.saga.service;

import com.payments.domain.shared.TenantContext;
import com.payments.saga.domain.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Core saga orchestrator service for managing saga execution and compensation */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrator {

  private final SagaService sagaService;
  private final SagaStepService sagaStepService;
  private final SagaEventService sagaEventService;
  private final SagaTemplateService sagaTemplateService;
  private final SagaExecutionEngine sagaExecutionEngine;
  private final SagaCompensationEngine sagaCompensationEngine;

  /** Start a new saga with the specified template */
  @Transactional
  public Saga startSaga(
      String templateName,
      TenantContext tenantContext,
      String correlationId,
      String paymentId,
      Map<String, Object> sagaData) {
    log.info(
        "Starting saga with template {} for payment {} (correlation: {})",
        templateName,
        paymentId,
        correlationId);

    // Load template
    SagaTemplate template = sagaTemplateService.getTemplate(templateName);
    if (template == null) {
      throw new IllegalArgumentException("Saga template not found: " + templateName);
    }

    // Create saga
    Saga saga = Saga.create(template.getTemplateName(), tenantContext, correlationId, paymentId);
    saga.setSagaData(sagaData);

    // Add steps from template
    for (SagaStepDefinition stepDef : template.getStepDefinitions()) {
      saga.addStep(
          stepDef.getStepName(),
          stepDef.getStepType(),
          stepDef.getServiceName(),
          stepDef.getEndpoint(),
          stepDef.getCompensationEndpoint(),
          mergeInputData(stepDef.getDefaultInputData(), sagaData));
    }

    // Save saga
    saga = sagaService.saveSaga(saga);

    // Publish saga started event
    SagaStartedEvent startedEvent =
        new SagaStartedEvent(
            saga.getId(), tenantContext, correlationId, saga.getSagaName(), paymentId);
    sagaEventService.publishEvent(startedEvent);

    // Start execution
    sagaExecutionEngine.executeSaga(saga.getId());

    log.info(
        "Saga {} started successfully with {} steps",
        saga.getId().getValue(),
        saga.getTotalSteps());
    return saga;
  }

  /** Execute the next step in a saga */
  @Transactional
  public void executeNextStep(SagaId sagaId) {
    log.debug("Executing next step for saga {}", sagaId.getValue());

    Saga saga =
        sagaService
            .getSaga(sagaId)
            .orElseThrow(
                () -> new IllegalArgumentException("Saga not found: " + sagaId.getValue()));

    if (!saga.isActive()) {
      log.warn("Saga {} is not active, current status: {}", sagaId.getValue(), saga.getStatus());
      return;
    }

    Optional<SagaStep> currentStep = saga.getCurrentStep();
    if (currentStep.isEmpty()) {
      // All steps completed
      completeSaga(saga);
      return;
    }

    SagaStep step = currentStep.get();
    if (step.getStatus() != SagaStepStatus.PENDING) {
      log.warn(
          "Step {} is not pending, current status: {}", step.getId().getValue(), step.getStatus());
      return;
    }

    try {
      // Execute the step
      sagaExecutionEngine.executeStep(step);
    } catch (Exception e) {
      log.error(
          "Failed to execute step {} for saga {}", step.getId().getValue(), sagaId.getValue(), e);
      handleStepFailure(step, e);
    }
  }

  /** Handle step completion */
  @Transactional
  public void handleStepCompletion(SagaStepId stepId, Map<String, Object> outputData) {
    log.info("Handling step completion for step {}", stepId.getValue());

    SagaStep step =
        sagaStepService
            .getStep(stepId)
            .orElseThrow(
                () -> new IllegalArgumentException("Step not found: " + stepId.getValue()));

    if (step.getStatus() != SagaStepStatus.RUNNING) {
      log.warn("Step {} is not running, current status: {}", stepId.getValue(), step.getStatus());
      return;
    }

    // Mark step as completed
    step.markAsCompleted(outputData);
    sagaStepService.saveStep(step);

    // Publish step completed event
    SagaStepCompletedEvent completedEvent =
        new SagaStepCompletedEvent(
            step.getSagaId(),
            step.getTenantContext(),
            step.getCorrelationId(),
            step.getStepName(),
            step.getStepType(),
            step.getSequence(),
            step.getServiceName(),
            outputData);
    sagaEventService.publishEvent(completedEvent);

    // Move to next step
    Saga saga = sagaService.getSaga(step.getSagaId()).orElseThrow();
    saga.moveToNextStep();
    sagaService.saveSaga(saga);

    // Execute next step
    executeNextStep(saga.getId());
  }

  /** Handle step failure */
  @Transactional
  public void handleStepFailure(
      SagaStepId stepId, String errorMessage, Map<String, Object> errorData) {
    log.error("Handling step failure for step {}: {}", stepId.getValue(), errorMessage);

    SagaStep step =
        sagaStepService
            .getStep(stepId)
            .orElseThrow(
                () -> new IllegalArgumentException("Step not found: " + stepId.getValue()));

    handleStepFailure(step, new RuntimeException(errorMessage));
  }

  private void handleStepFailure(SagaStep step, Exception error) {
    // Mark step as failed
    step.markAsFailed(error.getMessage(), Map.of("exception", error.getClass().getSimpleName()));
    sagaStepService.saveStep(step);

    // Publish step failed event
    SagaStepFailedEvent failedEvent =
        new SagaStepFailedEvent(
            step.getSagaId(),
            step.getTenantContext(),
            step.getCorrelationId(),
            step.getStepName(),
            step.getStepType(),
            step.getSequence(),
            step.getServiceName(),
            error.getMessage(),
            step.getErrorData(),
            step.getRetryCount(),
            step.getMaxRetries());
    sagaEventService.publishEvent(failedEvent);

    // Check if step can be retried
    if (step.canRetry()) {
      log.info(
          "Retrying step {} (attempt {}/{})",
          step.getId().getValue(),
          step.getRetryCount() + 1,
          step.getMaxRetries());
      step.incrementRetryCount();
      step.markAsRunning();
      sagaStepService.saveStep(step);

      // Retry execution
      sagaExecutionEngine.executeStep(step);
    } else {
      // Start compensation
      startCompensation(step.getSagaId(), "Step failed: " + error.getMessage());
    }
  }

  /** Start saga compensation */
  @Transactional
  public void startCompensation(SagaId sagaId, String reason) {
    log.info("Starting compensation for saga {}: {}", sagaId.getValue(), reason);

    Saga saga =
        sagaService
            .getSaga(sagaId)
            .orElseThrow(
                () -> new IllegalArgumentException("Saga not found: " + sagaId.getValue()));

    if (saga.getStatus() == SagaStatus.COMPENSATING || saga.getStatus() == SagaStatus.COMPENSATED) {
      log.warn("Saga {} is already in compensation state: {}", sagaId.getValue(), saga.getStatus());
      return;
    }

    // Start compensation
    saga.startCompensation();
    sagaService.saveSaga(saga);

    // Publish compensation started event
    SagaCompensationStartedEvent compensationStartedEvent =
        new SagaCompensationStartedEvent(
            saga.getId(),
            saga.getTenantContext(),
            saga.getCorrelationId(),
            saga.getSagaName(),
            saga.getPaymentId(),
            reason,
            saga.getCompletedStepsCount());
    sagaEventService.publishEvent(compensationStartedEvent);

    // Start compensation process
    sagaCompensationEngine.startCompensation(saga);
  }

  /** Complete saga successfully */
  @Transactional
  public void completeSaga(Saga saga) {
    log.info("Completing saga {}", saga.getId().getValue());

    saga.complete();
    sagaService.saveSaga(saga);

    // Publish saga completed event
    SagaCompletedEvent completedEvent =
        new SagaCompletedEvent(
            saga.getId(),
            saga.getTenantContext(),
            saga.getCorrelationId(),
            saga.getSagaName(),
            saga.getPaymentId(),
            saga.getTotalSteps(),
            saga.getCompletedStepsCount());
    sagaEventService.publishEvent(completedEvent);
  }

  /** Complete saga compensation */
  @Transactional
  public void completeCompensation(SagaId sagaId) {
    log.info("Completing compensation for saga {}", sagaId.getValue());

    Saga saga =
        sagaService
            .getSaga(sagaId)
            .orElseThrow(
                () -> new IllegalArgumentException("Saga not found: " + sagaId.getValue()));

    saga.completeCompensation();
    sagaService.saveSaga(saga);

    // Publish compensation completed event
    SagaCompensatedEvent compensatedEvent =
        new SagaCompensatedEvent(
            saga.getId(),
            saga.getTenantContext(),
            saga.getCorrelationId(),
            saga.getSagaName(),
            saga.getPaymentId(),
            "Compensation completed",
            saga.getCompensatedSteps().size());
    sagaEventService.publishEvent(compensatedEvent);
  }

  /** Get saga status */
  public Optional<Saga> getSagaStatus(SagaId sagaId) {
    return sagaService.getSaga(sagaId);
  }

  /** Get saga steps */
  public List<SagaStep> getSagaSteps(SagaId sagaId) {
    return sagaStepService.getStepsBySagaId(sagaId);
  }

  /** Get saga events */
  public List<SagaEvent> getSagaEvents(SagaId sagaId) {
    return sagaEventService.getEventsBySagaId(sagaId);
  }

  private Map<String, Object> mergeInputData(
      Map<String, Object> defaultData, Map<String, Object> sagaData) {
    if (defaultData == null) {
      return sagaData;
    }
    if (sagaData == null) {
      return defaultData;
    }

    Map<String, Object> merged = new java.util.HashMap<>(defaultData);
    merged.putAll(sagaData);
    return merged;
  }
}
