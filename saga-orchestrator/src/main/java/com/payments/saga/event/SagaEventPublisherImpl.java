package com.payments.saga.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.saga.domain.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Implementation of SagaEventPublisher for publishing saga events to Kafka */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaEventPublisherImpl implements SagaEventPublisher {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void publishSagaStarted(SagaStartedEvent event) {
    publishEvent("saga.started", event);
  }

  @Override
  public void publishSagaStepStarted(SagaStepStartedEvent event) {
    publishEvent("saga.step.started", event);
  }

  @Override
  public void publishSagaStepCompleted(SagaStepCompletedEvent event) {
    publishEvent("saga.step.completed", event);
  }

  @Override
  public void publishSagaStepFailed(SagaStepFailedEvent event) {
    publishEvent("saga.step.failed", event);
  }

  @Override
  public void publishSagaCompleted(SagaCompletedEvent event) {
    publishEvent("saga.completed", event);
  }

  @Override
  public void publishSagaFailed(SagaFailedEvent event) {
    publishEvent("saga.failed", event);
  }

  @Override
  public void publishSagaCompensationStarted(SagaCompensationStartedEvent event) {
    publishEvent("saga.compensation.started", event);
  }

  @Override
  public void publishSagaCompensated(SagaCompensatedEvent event) {
    publishEvent("saga.compensated", event);
  }

  @Override
  public void publishToDeadLetterQueue(String topic, String key, String message, Exception error) {
    try {
      Map<String, Object> deadLetterMessage =
          Map.of(
              "originalTopic", topic,
              "originalKey", key,
              "originalMessage", message,
              "error", error.getMessage(),
              "timestamp", System.currentTimeMillis());

      String deadLetterJson = objectMapper.writeValueAsString(deadLetterMessage);
      kafkaTemplate.send("saga.dead.letter", key, deadLetterJson);

      log.warn(
          "Published message to dead letter queue: topic={}, key={}, error={}",
          topic,
          key,
          error.getMessage());
    } catch (Exception e) {
      log.error("Failed to publish to dead letter queue: {}", e.getMessage(), e);
    }
  }

  private void publishEvent(String topic, SagaEvent event) {
    try {
      String message = serializeEvent(event);
      String key = event.getSagaId().getValue();

      CompletableFuture<Void> future =
          kafkaTemplate
              .send(topic, key, message)
              .thenAccept(
                  result -> {
                    log.debug(
                        "Successfully published event {} to topic {}", event.getEventType(), topic);
                  })
              .exceptionally(
                  throwable -> {
                    log.error(
                        "Failed to publish event {} to topic {}: {}",
                        event.getEventType(),
                        topic,
                        throwable.getMessage());
                    publishToDeadLetterQueue(
                        topic,
                        key,
                        message,
                        throwable instanceof Exception
                            ? (Exception) throwable
                            : new RuntimeException(throwable));
                    return null;
                  });

      // Wait for completion to ensure proper error handling
      future.join();

    } catch (Exception e) {
      log.error(
          "Failed to serialize or publish event {}: {}", event.getEventType(), e.getMessage(), e);
      publishToDeadLetterQueue(topic, event.getSagaId().getValue(), "{}", e);
    }
  }

  private String serializeEvent(SagaEvent event) {
    try {
      Map<String, Object> eventData =
          Map.of(
              "eventId", event.getEventId(),
              "sagaId", event.getSagaId().getValue(),
              "eventType", event.getEventType(),
              "correlationId", event.getCorrelationId(),
              "tenantId", event.getTenantContext().getTenantId(),
              "businessUnitId", event.getTenantContext().getBusinessUnitId(),
              "occurredAt", event.getOccurredAt().toString(),
              "eventData", event.getEventData());

      return objectMapper.writeValueAsString(eventData);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize event: " + event.getEventType(), e);
    }
  }
}
