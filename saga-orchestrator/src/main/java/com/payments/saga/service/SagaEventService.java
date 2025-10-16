package com.payments.saga.service;

import com.payments.saga.domain.*;
import com.payments.saga.entity.SagaEventEntity;
import com.payments.saga.event.SagaEventPublisher;
import com.payments.saga.repository.SagaEventRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for managing saga events */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaEventService {

  private final SagaEventRepository sagaEventRepository;
  private final SagaEventPublisher sagaEventPublisher;

  @Transactional
  public void publishEvent(SagaEvent event) {
    log.debug("Publishing saga event: {}", event.getEventType());

    // Save to database
    SagaEventEntity entity = SagaEventEntity.fromDomain(event);
    sagaEventRepository.save(entity);

    // Publish to Kafka
    publishToKafka(event);

    log.debug("Saga event {} published successfully", event.getEventType());
  }

  @Transactional
  public void publishStepStartedEvent(SagaStep step) {
    SagaStepStartedEvent event =
        new SagaStepStartedEvent(
            step.getSagaId(),
            step.getTenantContext(),
            step.getCorrelationId(),
            step.getStepName(),
            step.getStepType(),
            step.getSequence(),
            step.getServiceName(),
            step.getEndpoint());
    publishEvent(event);
  }

  private void publishToKafka(SagaEvent event) {
    try {
      if (event instanceof SagaStartedEvent sagaStartedEvent) {
        sagaEventPublisher.publishSagaStarted(sagaStartedEvent);
      } else if (event instanceof SagaStepStartedEvent stepStartedEvent) {
        sagaEventPublisher.publishSagaStepStarted(stepStartedEvent);
      } else if (event instanceof SagaStepCompletedEvent stepCompletedEvent) {
        sagaEventPublisher.publishSagaStepCompleted(stepCompletedEvent);
      } else if (event instanceof SagaStepFailedEvent stepFailedEvent) {
        sagaEventPublisher.publishSagaStepFailed(stepFailedEvent);
      } else if (event instanceof SagaCompletedEvent sagaCompletedEvent) {
        sagaEventPublisher.publishSagaCompleted(sagaCompletedEvent);
      } else if (event instanceof SagaCompensationStartedEvent compensationStartedEvent) {
        sagaEventPublisher.publishSagaCompensationStarted(compensationStartedEvent);
      } else if (event instanceof SagaCompensatedEvent compensatedEvent) {
        sagaEventPublisher.publishSagaCompensated(compensatedEvent);
      }
    } catch (Exception e) {
      log.error("Failed to publish event to Kafka: {}", e.getMessage(), e);
    }
  }

  @Transactional
  public void publishStepCompletedEvent(SagaStep step, Map<String, Object> outputData) {
    SagaStepCompletedEvent event =
        new SagaStepCompletedEvent(
            step.getSagaId(),
            step.getTenantContext(),
            step.getCorrelationId(),
            step.getStepName(),
            step.getStepType(),
            step.getSequence(),
            step.getServiceName(),
            outputData);
    publishEvent(event);
  }

  @Transactional
  public void publishStepFailedEvent(
      SagaStep step, String errorMessage, Map<String, Object> errorData) {
    SagaStepFailedEvent event =
        new SagaStepFailedEvent(
            step.getSagaId(),
            step.getTenantContext(),
            step.getCorrelationId(),
            step.getStepName(),
            step.getStepType(),
            step.getSequence(),
            step.getServiceName(),
            errorMessage,
            errorData,
            step.getRetryCount(),
            step.getMaxRetries());
    publishEvent(event);
  }

  @Transactional
  public void publishStepCompensationStartedEvent(SagaStep step) {
    // Create a generic compensation started event
    SagaEvent event =
        new SagaEvent(
            step.getSagaId(),
            step.getTenantContext(),
            step.getCorrelationId(),
            "SagaStepCompensationStarted") {
          @Override
          public Map<String, Object> getEventData() {
            return Map.of(
                "stepName", step.getStepName(),
                "stepType", step.getStepType().name(),
                "sequence", step.getSequence(),
                "serviceName", step.getServiceName());
          }
        };
    publishEvent(event);
  }

  @Transactional
  public void publishStepCompensationCompletedEvent(
      SagaStep step, Map<String, Object> compensationResult) {
    // Create a generic compensation completed event
    SagaEvent event =
        new SagaEvent(
            step.getSagaId(),
            step.getTenantContext(),
            step.getCorrelationId(),
            "SagaStepCompensationCompleted") {
          @Override
          public Map<String, Object> getEventData() {
            return Map.of(
                "stepName", step.getStepName(),
                "stepType", step.getStepType().name(),
                "sequence", step.getSequence(),
                "serviceName", step.getServiceName(),
                "compensationResult", compensationResult);
          }
        };
    publishEvent(event);
  }

  @Transactional
  public void publishStepCompensationFailedEvent(SagaStep step, String errorMessage) {
    // Create a generic compensation failed event
    SagaEvent event =
        new SagaEvent(
            step.getSagaId(),
            step.getTenantContext(),
            step.getCorrelationId(),
            "SagaStepCompensationFailed") {
          @Override
          public Map<String, Object> getEventData() {
            return Map.of(
                "stepName", step.getStepName(),
                "stepType", step.getStepType().name(),
                "sequence", step.getSequence(),
                "serviceName", step.getServiceName(),
                "errorMessage", errorMessage);
          }
        };
    publishEvent(event);
  }

  @Transactional(readOnly = true)
  public List<SagaEvent> getEventsBySagaId(SagaId sagaId) {
    log.debug("Getting events for saga {}", sagaId.getValue());

    return sagaEventRepository.findBySagaIdOrderByOccurredAt(sagaId.getValue()).stream()
        .map(this::convertToDomainEvent)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<SagaEvent> getEventsByCorrelationId(String correlationId) {
    log.debug("Getting events by correlation ID {}", correlationId);

    return sagaEventRepository.findByCorrelationId(correlationId).stream()
        .map(this::convertToDomainEvent)
        .collect(Collectors.toList());
  }

  private SagaEvent convertToDomainEvent(SagaEventEntity entity) {
    // This is a simplified conversion - in a real implementation,
    // you would need to handle different event types properly
    return new SagaEvent(
        SagaId.of(entity.getSagaId()),
        com.payments.domain.shared.TenantContext.of(
            entity.getTenantId(), "Tenant", entity.getBusinessUnitId(), "Business Unit"),
        entity.getCorrelationId(),
        entity.getEventType()) {
      @Override
      public Map<String, Object> getEventData() {
        // Parse JSON event data
        if (entity.getEventDataJson() != null) {
          try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(entity.getEventDataJson(), Map.class);
          } catch (Exception e) {
            log.warn("Failed to parse event data JSON: {}", e.getMessage());
          }
        }
        return Map.of();
      }
    };
  }
}
