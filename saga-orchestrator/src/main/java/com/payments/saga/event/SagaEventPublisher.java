package com.payments.saga.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.saga.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Publisher for saga events to Kafka
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // Kafka Topics for saga events
    private static final String SAGA_STARTED_TOPIC = "saga.started";
    private static final String SAGA_STEP_STARTED_TOPIC = "saga.step.started";
    private static final String SAGA_STEP_COMPLETED_TOPIC = "saga.step.completed";
    private static final String SAGA_STEP_FAILED_TOPIC = "saga.step.failed";
    private static final String SAGA_COMPLETED_TOPIC = "saga.completed";
    private static final String SAGA_COMPENSATION_STARTED_TOPIC = "saga.compensation.started";
    private static final String SAGA_COMPENSATED_TOPIC = "saga.compensated";

    /**
     * Publish saga started event
     */
    public void publishSagaStarted(SagaStartedEvent event) {
        publishEvent(SAGA_STARTED_TOPIC, event.getSagaId().getValue(), event, event.getEventData());
    }

    /**
     * Publish saga step started event
     */
    public void publishSagaStepStarted(SagaStepStartedEvent event) {
        publishEvent(SAGA_STEP_STARTED_TOPIC, event.getSagaId().getValue(), event, event.getEventData());
    }

    /**
     * Publish saga step completed event
     */
    public void publishSagaStepCompleted(SagaStepCompletedEvent event) {
        publishEvent(SAGA_STEP_COMPLETED_TOPIC, event.getSagaId().getValue(), event, event.getEventData());
    }

    /**
     * Publish saga step failed event
     */
    public void publishSagaStepFailed(SagaStepFailedEvent event) {
        publishEvent(SAGA_STEP_FAILED_TOPIC, event.getSagaId().getValue(), event, event.getEventData());
    }

    /**
     * Publish saga completed event
     */
    public void publishSagaCompleted(SagaCompletedEvent event) {
        publishEvent(SAGA_COMPLETED_TOPIC, event.getSagaId().getValue(), event, event.getEventData());
    }

    /**
     * Publish saga compensation started event
     */
    public void publishSagaCompensationStarted(SagaCompensationStartedEvent event) {
        publishEvent(SAGA_COMPENSATION_STARTED_TOPIC, event.getSagaId().getValue(), event, event.getEventData());
    }

    /**
     * Publish saga compensated event
     */
    public void publishSagaCompensated(SagaCompensatedEvent event) {
        publishEvent(SAGA_COMPENSATED_TOPIC, event.getSagaId().getValue(), event, event.getEventData());
    }

    /**
     * Generic method to publish saga events
     */
    private void publishEvent(String topic, String sagaId, SagaEvent event, Map<String, Object> eventData) {
        try {
            // Create the message payload
            Map<String, Object> message = Map.of(
                "eventId", event.getEventId(),
                "sagaId", sagaId,
                "eventType", event.getEventType(),
                "tenantId", event.getTenantContext().getTenantId(),
                "businessUnitId", event.getTenantContext().getBusinessUnitId(),
                "correlationId", event.getCorrelationId(),
                "occurredAt", event.getOccurredAt().toString(),
                "eventData", eventData
            );

            String messageJson = objectMapper.writeValueAsString(message);
            
            // Send to Kafka
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, sagaId, messageJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Successfully published {} event for saga {} to topic {}: offset={}",
                            event.getEventType(), sagaId, topic, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish {} event for saga {} to topic {}: {}",
                            event.getEventType(), sagaId, topic, ex.getMessage(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error publishing {} event for saga {}: {}", event.getEventType(), sagaId, e.getMessage(), e);
        }
    }
}






