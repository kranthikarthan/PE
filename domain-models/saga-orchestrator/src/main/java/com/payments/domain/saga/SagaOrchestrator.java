package com.payments.domain.saga;

import com.payments.domain.shared.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.*;

/**
 * Saga Orchestrator Aggregate Root
 *
 * <p>Manages distributed transaction orchestration using the Saga pattern. Coordinates compensation
 * actions and maintains transaction state.
 */
@Entity
@Table(name = "saga_orchestrators")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SagaOrchestrator {

  @EmbeddedId private SagaId id;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id")),
    @AttributeOverride(name = "businessUnitId", column = @Column(name = "business_unit_id"))
  })
  private TenantContext tenantContext;

  private String sagaName;

  @Enumerated(EnumType.STRING)
  private SagaStatus status;

  @Enumerated(EnumType.STRING)
  private SagaType sagaType;

  private String businessKey;

  private String correlationId;

  private Instant startedAt;

  private Instant completedAt;

  private Instant lastUpdatedAt;

  private String initiatedBy;

  private String currentStep;

  private Integer totalSteps;

  private Integer completedSteps;

  private String failureReason;

  private String compensationData;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "saga_id")
  private List<SagaStep> steps = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "saga_id")
  private List<SagaEvent> events = new ArrayList<>();

  @Transient private List<DomainEvent> domainEvents = new ArrayList<>();

  // ─────────────────────────────────────────────────────────
  // FACTORY METHOD
  // ─────────────────────────────────────────────────────────

  public static SagaOrchestrator create(
      SagaId id,
      TenantContext tenantContext,
      String sagaName,
      SagaType sagaType,
      String businessKey,
      String initiatedBy) {
    // Business validation
    if (sagaName == null || sagaName.isBlank()) {
      throw new InvalidSagaException("Saga name cannot be null or blank");
    }

    if (businessKey == null || businessKey.isBlank()) {
      throw new InvalidSagaException("Business key cannot be null or blank");
    }

    SagaOrchestrator saga = new SagaOrchestrator();
    saga.id = id;
    saga.tenantContext = tenantContext;
    saga.sagaName = sagaName;
    saga.sagaType = sagaType;
    saga.businessKey = businessKey;
    saga.correlationId = "SAGA-" + System.currentTimeMillis();
    saga.status = SagaStatus.STARTED;
    saga.startedAt = Instant.now();
    saga.lastUpdatedAt = Instant.now();
    saga.initiatedBy = initiatedBy;
    saga.currentStep = "INITIAL";
    saga.totalSteps = 0;
    saga.completedSteps = 0;

    // Domain event
    saga.registerEvent(
        new SagaStartedEvent(
            saga.id, saga.sagaName, saga.sagaType, saga.businessKey, saga.startedAt));

    return saga;
  }

  // ─────────────────────────────────────────────────────────
  // BUSINESS METHODS
  // ─────────────────────────────────────────────────────────

  /** Add a step to the saga */
  public void addStep(
      SagaStepId stepId,
      String stepName,
      String serviceName,
      String action,
      String compensationAction,
      Integer order,
      String addedBy) {
    SagaStep step =
        SagaStep.create(
            stepId, this.id, stepName, serviceName, action, compensationAction, order, addedBy);

    this.steps.add(step);
    this.totalSteps = this.steps.size();
    this.lastUpdatedAt = Instant.now();

    registerEvent(new SagaStepAddedEvent(this.id, stepId, stepName, serviceName));
  }

  /** Execute the next step in the saga */
  public void executeNextStep(String executedBy) {
    if (this.status != SagaStatus.STARTED && this.status != SagaStatus.IN_PROGRESS) {
      throw new InvalidSagaException("Saga is not in a state that allows step execution");
    }

    SagaStep nextStep = getNextStep();
    if (nextStep == null) {
      // All steps completed
      this.status = SagaStatus.COMPLETED;
      this.completedAt = Instant.now();
      this.currentStep = "COMPLETED";

      registerEvent(new SagaCompletedEvent(this.id, this.sagaName, this.completedAt));
      return;
    }

    this.status = SagaStatus.IN_PROGRESS;
    this.currentStep = nextStep.getStepName();
    this.lastUpdatedAt = Instant.now();

    registerEvent(
        new SagaStepExecutedEvent(this.id, nextStep.getId(), nextStep.getStepName(), executedBy));
  }

  /** Complete a step successfully */
  public void completeStep(SagaStepId stepId, String result, String completedBy) {
    SagaStep step = findStep(stepId);
    if (step == null) {
      throw new InvalidSagaException("Step not found: " + stepId);
    }

    step.markCompleted(result, completedBy);
    this.completedSteps++;
    this.lastUpdatedAt = Instant.now();

    registerEvent(new SagaStepCompletedEvent(this.id, stepId, step.getStepName(), result));

    // Check if all steps are completed
    if (this.completedSteps >= this.totalSteps) {
      this.status = SagaStatus.COMPLETED;
      this.completedAt = Instant.now();
      this.currentStep = "COMPLETED";

      registerEvent(new SagaCompletedEvent(this.id, this.sagaName, this.completedAt));
    }
  }

  /** Fail a step and start compensation */
  public void failStep(SagaStepId stepId, String failureReason, String failedBy) {
    SagaStep step = findStep(stepId);
    if (step == null) {
      throw new InvalidSagaException("Step not found: " + stepId);
    }

    step.markFailed(failureReason, failedBy);
    this.status = SagaStatus.COMPENSATING;
    this.failureReason = failureReason;
    this.lastUpdatedAt = Instant.now();

    registerEvent(new SagaStepFailedEvent(this.id, stepId, step.getStepName(), failureReason));

    // Start compensation process
    startCompensation();
  }

  /** Complete compensation for a step */
  public void completeCompensation(
      SagaStepId stepId, String compensationResult, String compensatedBy) {
    SagaStep step = findStep(stepId);
    if (step == null) {
      throw new InvalidSagaException("Step not found: " + stepId);
    }

    step.markCompensated(compensationResult, compensatedBy);
    this.lastUpdatedAt = Instant.now();

    registerEvent(
        new SagaStepCompensatedEvent(this.id, stepId, step.getStepName(), compensationResult));

    // Check if all steps are compensated
    if (allStepsCompensated()) {
      this.status = SagaStatus.COMPENSATED;
      this.completedAt = Instant.now();
      this.currentStep = "COMPENSATED";

      registerEvent(new SagaCompensatedEvent(this.id, this.sagaName, this.failureReason));
    }
  }

  /** Add an event to the saga */
  public void addEvent(String eventType, String eventData, String eventSource, String addedBy) {
    SagaEvent event =
        SagaEvent.create(
            SagaEventId.generate(), this.id, eventType, eventData, eventSource, addedBy);

    this.events.add(event);
    this.lastUpdatedAt = Instant.now();

    registerEvent(new SagaEventAddedEvent(this.id, event.getId(), eventType, eventSource));
  }

  // ─────────────────────────────────────────────────────────
  // QUERY METHODS
  // ─────────────────────────────────────────────────────────

  public boolean isStarted() {
    return this.status == SagaStatus.STARTED;
  }

  public boolean isInProgress() {
    return this.status == SagaStatus.IN_PROGRESS;
  }

  public boolean isCompleted() {
    return this.status == SagaStatus.COMPLETED;
  }

  public boolean isCompensating() {
    return this.status == SagaStatus.COMPENSATING;
  }

  public boolean isCompensated() {
    return this.status == SagaStatus.COMPENSATED;
  }

  public boolean isFailed() {
    return this.status == SagaStatus.FAILED;
  }

  public SagaStep getNextStep() {
    return steps.stream()
        .filter(step -> step.getStatus() == StepStatus.PENDING)
        .min((s1, s2) -> Integer.compare(s1.getOrder(), s2.getOrder()))
        .orElse(null);
  }

  public List<SagaStep> getCompletedSteps() {
    return steps.stream()
        .filter(step -> step.getStatus() == StepStatus.COMPLETED)
        .collect(java.util.stream.Collectors.toList());
  }

  public List<SagaStep> getFailedSteps() {
    return steps.stream()
        .filter(step -> step.getStatus() == StepStatus.FAILED)
        .collect(java.util.stream.Collectors.toList());
  }

  public List<SagaStep> getCompensatedSteps() {
    return steps.stream()
        .filter(step -> step.getStatus() == StepStatus.COMPENSATED)
        .collect(java.util.stream.Collectors.toList());
  }

  public SagaId getId() {
    return id;
  }

  public String getSagaName() {
    return sagaName;
  }

  public SagaStatus getStatus() {
    return status;
  }

  public SagaType getSagaType() {
    return sagaType;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public List<SagaStep> getSteps() {
    return Collections.unmodifiableList(steps);
  }

  public List<SagaEvent> getEvents() {
    return Collections.unmodifiableList(events);
  }

  public List<DomainEvent> getDomainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  public void clearDomainEvents() {
    this.domainEvents.clear();
  }

  // ─────────────────────────────────────────────────────────
  // PRIVATE HELPERS
  // ─────────────────────────────────────────────────────────

  private SagaStep findStep(SagaStepId stepId) {
    return steps.stream().filter(step -> step.getId().equals(stepId)).findFirst().orElse(null);
  }

  private boolean allStepsCompensated() {
    return steps.stream().allMatch(step -> step.getStatus() == StepStatus.COMPENSATED);
  }

  private void startCompensation() {
    // Find the last completed step and start compensation
    SagaStep lastCompletedStep =
        steps.stream()
            .filter(step -> step.getStatus() == StepStatus.COMPLETED)
            .max((s1, s2) -> Integer.compare(s1.getOrder(), s2.getOrder()))
            .orElse(null);

    if (lastCompletedStep != null) {
      lastCompletedStep.markCompensating();
      registerEvent(
          new SagaCompensationStartedEvent(
              this.id, lastCompletedStep.getId(), lastCompletedStep.getStepName()));
    }
  }

  private void registerEvent(DomainEvent event) {
    this.domainEvents.add(event);
  }
}

/** Saga Step (Entity within SagaOrchestrator Aggregate) */
@Entity
@Table(name = "saga_steps")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
class SagaStep {

  @EmbeddedId private SagaStepId id;

  @Embedded private SagaId sagaId;

  private String stepName;

  private String serviceName;

  private String action;

  private String compensationAction;

  private Integer order;

  @Enumerated(EnumType.STRING)
  private StepStatus status;

  private String result;

  private String failureReason;

  private String compensationResult;

  private Instant startedAt;

  private Instant completedAt;

  private Instant compensatedAt;

  private String executedBy;

  private String compensatedBy;

  public static SagaStep create(
      SagaStepId id,
      SagaId sagaId,
      String stepName,
      String serviceName,
      String action,
      String compensationAction,
      Integer order,
      String createdBy) {
    SagaStep step = new SagaStep();
    step.id = id;
    step.sagaId = sagaId;
    step.stepName = stepName;
    step.serviceName = serviceName;
    step.action = action;
    step.compensationAction = compensationAction;
    step.order = order;
    step.status = StepStatus.PENDING;
    step.startedAt = Instant.now();
    step.executedBy = createdBy;

    return step;
  }

  public void markCompleted(String result, String completedBy) {
    this.status = StepStatus.COMPLETED;
    this.result = result;
    this.completedAt = Instant.now();
    this.executedBy = completedBy;
  }

  public void markFailed(String failureReason, String failedBy) {
    this.status = StepStatus.FAILED;
    this.failureReason = failureReason;
    this.executedBy = failedBy;
  }

  public void markCompensating() {
    this.status = StepStatus.COMPENSATING;
  }

  public void markCompensated(String compensationResult, String compensatedBy) {
    this.status = StepStatus.COMPENSATED;
    this.compensationResult = compensationResult;
    this.compensatedAt = Instant.now();
    this.compensatedBy = compensatedBy;
  }
}

/** Saga Event (Entity within SagaOrchestrator Aggregate) */
@Entity
@Table(name = "saga_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
class SagaEvent {

  @EmbeddedId private SagaEventId id;

  @Embedded private SagaId sagaId;

  private String eventType;

  private String eventData;

  private String eventSource;

  private Instant occurredAt;

  private String createdBy;

  public static SagaEvent create(
      SagaEventId id,
      SagaId sagaId,
      String eventType,
      String eventData,
      String eventSource,
      String createdBy) {
    SagaEvent event = new SagaEvent();
    event.id = id;
    event.sagaId = sagaId;
    event.eventType = eventType;
    event.eventData = eventData;
    event.eventSource = eventSource;
    event.occurredAt = Instant.now();
    event.createdBy = createdBy;

    return event;
  }
}
