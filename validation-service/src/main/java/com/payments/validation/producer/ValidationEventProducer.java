package com.payments.validation.producer;

import com.payments.contracts.events.PaymentValidatedEvent;
import com.payments.contracts.events.ValidationFailedEvent;
import com.payments.validation.config.EventTopicsConfig;
import com.payments.validation.service.CorrelationService;
import com.payments.validation.service.TenantContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Validation Event Producer
 * 
 * Produces validation result events to Kafka topics:
 * - PaymentValidatedEvent: Successful validation results
 * - ValidationFailedEvent: Failed validation results
 * - FraudDetectedEvent: Fraud detection alerts
 * - RiskAssessmentEvent: Risk assessment results
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EventTopicsConfig eventTopicsConfig;
    private final CorrelationService correlationService;
    private final TenantContextService tenantContextService;

    /**
     * Publish successful validation result
     * 
     * @param event PaymentValidatedEvent
     * @param correlationId Correlation ID for tracing
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     */
    public void publishPaymentValidated(
            PaymentValidatedEvent event,
            String correlationId,
            String tenantId,
            String businessUnitId) {
        
        log.info("Publishing PaymentValidatedEvent: {} for payment: {}", 
                event.getEventId(), event.getPaymentId().getValue());
        
        String topic = eventTopicsConfig.PAYMENT_VALIDATED_TOPIC;
        String key = event.getPaymentId().getValue();
        
        Map<String, Object> headers = createHeaders(correlationId, tenantId, businessUnitId);
        
        sendEvent(topic, key, event, headers, "PaymentValidatedEvent");
    }

    /**
     * Publish failed validation result
     * 
     * @param event ValidationFailedEvent
     * @param correlationId Correlation ID for tracing
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     */
    public void publishValidationFailed(
            ValidationFailedEvent event,
            String correlationId,
            String tenantId,
            String businessUnitId) {
        
        log.info("Publishing ValidationFailedEvent: {} for payment: {}", 
                event.getEventId(), event.getPaymentId().getValue());
        
        String topic = eventTopicsConfig.VALIDATION_FAILED_TOPIC;
        String key = event.getPaymentId().getValue();
        
        Map<String, Object> headers = createHeaders(correlationId, tenantId, businessUnitId);
        
        sendEvent(topic, key, event, headers, "ValidationFailedEvent");
    }

    /**
     * Publish fraud detection alert
     * 
     * @param paymentId Payment ID
     * @param fraudScore Fraud score (0-100)
     * @param fraudReasons List of fraud reasons
     * @param correlationId Correlation ID for tracing
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     */
    public void publishFraudDetected(
            String paymentId,
            int fraudScore,
            java.util.List<String> fraudReasons,
            String correlationId,
            String tenantId,
            String businessUnitId) {
        
        log.warn("Publishing FraudDetectedEvent for payment: {} with score: {}", 
                paymentId, fraudScore);
        
        // TODO: Create FraudDetectedEvent when schema is defined
        log.debug("FraudDetectedEvent publishing not yet implemented");
    }

    /**
     * Publish risk assessment result
     * 
     * @param paymentId Payment ID
     * @param riskLevel Risk level (LOW, MEDIUM, HIGH, CRITICAL)
     * @param riskScore Risk score (0-100)
     * @param correlationId Correlation ID for tracing
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     */
    public void publishRiskAssessment(
            String paymentId,
            String riskLevel,
            int riskScore,
            String correlationId,
            String tenantId,
            String businessUnitId) {
        
        log.info("Publishing RiskAssessmentEvent for payment: {} with level: {} and score: {}", 
                paymentId, riskLevel, riskScore);
        
        // TODO: Create RiskAssessmentEvent when schema is defined
        log.debug("RiskAssessmentEvent publishing not yet implemented");
    }

    /**
     * Send event to Kafka topic
     */
    private void sendEvent(
            String topic,
            String key,
            Object event,
            Map<String, Object> headers,
            String eventType) {
        
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Successfully sent {} to topic: {}, partition: {}, offset: {}", 
                            eventType, topic, result.getRecordMetadata().partition(), 
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send {} to topic: {}", eventType, topic, ex);
                    throw new RuntimeException("Failed to publish " + eventType, ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error sending {} to topic: {}", eventType, topic, e);
            throw new RuntimeException("Failed to publish " + eventType, e);
        }
    }

    /**
     * Create headers for Kafka message
     */
    private Map<String, Object> createHeaders(String correlationId, String tenantId, String businessUnitId) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("X-Correlation-ID", correlationId);
        headers.put("X-Tenant-ID", tenantId);
        headers.put("X-Business-Unit-ID", businessUnitId);
        headers.put("X-Source", "validation-service");
        headers.put("X-Timestamp", Instant.now().toEpochMilli());
        headers.put("X-Event-Type", "validation-result");
        headers.put("X-Service-Version", "1.0.0");
        headers.put("X-Processing-Time", Instant.now().toString());
        return headers;
    }

    /**
     * Create headers using current context
     */
    private Map<String, Object> createHeadersFromContext() {
        String correlationId = correlationService.getCurrentCorrelationIdOrGenerate();
        String tenantId = tenantContextService.getCurrentTenantId();
        String businessUnitId = tenantContextService.getCurrentBusinessUnitId();
        
        if (tenantId == null || businessUnitId == null) {
            log.warn("Missing tenant context when creating headers - correlationId: {}", correlationId);
            tenantId = "unknown";
            businessUnitId = "unknown";
        }
        
        return createHeaders(correlationId, tenantId, businessUnitId);
    }
}
