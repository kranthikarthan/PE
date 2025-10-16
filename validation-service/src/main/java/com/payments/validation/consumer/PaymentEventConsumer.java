package com.payments.validation.consumer;

import com.payments.contracts.events.PaymentInitiatedEvent;
import com.payments.validation.service.ValidationOrchestrator;
import com.payments.validation.service.CorrelationService;
import com.payments.validation.service.TenantContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Payment Event Consumer
 * 
 * Consumes payment events from Kafka topics:
 * - PaymentInitiatedEvent: Process payment validation
 * - PaymentUpdatedEvent: Re-validate updated payments
 * - AccountChangedEvent: Update validation rules
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final ValidationOrchestrator validationOrchestrator;
    private final CorrelationService correlationService;
    private final TenantContextService tenantContextService;

    /**
     * Consume PaymentInitiatedEvent
     * 
     * @param event Payment initiated event
     * @param headers Kafka headers
     * @param acknowledgment Manual acknowledgment
     */
    @KafkaListener(
        topics = "${validation.topics.inbound.payment-initiated:payment-initiated}",
        groupId = "${spring.kafka.consumer.group-id:validation-service}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentInitiated(
            @Payload PaymentInitiatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header Map<String, Object> headers,
            Acknowledgment acknowledgment) {
        
        log.info("Received PaymentInitiatedEvent: {} from topic: {}, partition: {}, offset: {}", 
                event.getEventId(), topic, partition, offset);
        
        try {
            // Extract and validate correlation and tenant information from headers
            String correlationId = extractAndValidateCorrelationId(headers, event.getEventId().toString());
            String tenantId = extractAndValidateTenantId(headers, event.getEventId().toString());
            String businessUnitId = extractAndValidateBusinessUnitId(headers, event.getEventId().toString());
            
            // Set up correlation context
            correlationService.setCorrelationId(correlationId);
            
            // Set up tenant context
            tenantContextService.setTenantContext(tenantId, businessUnitId);
            
            log.debug("Processing payment validation for payment: {}, tenant: {}, correlation: {}", 
                    event.getPaymentId().getValue(), tenantId, correlationId);
            
            // Orchestrate validation process
            validationOrchestrator.validatePayment(event, correlationId, tenantId, businessUnitId);
            
            // Acknowledge successful processing
            acknowledgment.acknowledge();
            
            log.info("Successfully processed PaymentInitiatedEvent: {} with correlation: {}", 
                    event.getEventId(), correlationId);
            
        } catch (Exception e) {
            log.error("Failed to process PaymentInitiatedEvent: {} with correlation: {}", 
                    event.getEventId(), correlationService.getCurrentCorrelationId(), e);
            // Don't acknowledge - let it retry or go to DLQ
            throw e;
        } finally {
            // Clean up context
            correlationService.clearCorrelationId();
            tenantContextService.clearTenantContext();
        }
    }

    /**
     * Consume PaymentUpdatedEvent (for re-validation)
     */
    @KafkaListener(
        topics = "${validation.topics.inbound.payment-updated:payment-updated}",
        groupId = "${spring.kafka.consumer.group-id:validation-service}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentUpdated(
            @Payload Object event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header Map<String, Object> headers,
            Acknowledgment acknowledgment) {
        
        log.info("Received PaymentUpdatedEvent from topic: {}", topic);
        
        try {
            // TODO: Implement payment update validation logic
            log.debug("Payment update validation not yet implemented");
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process PaymentUpdatedEvent", e);
            throw e;
        }
    }

    /**
     * Consume AccountChangedEvent (for rule updates)
     */
    @KafkaListener(
        topics = "${validation.topics.inbound.account-changed:account-changed}",
        groupId = "${spring.kafka.consumer.group-id:validation-service}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAccountChanged(
            @Payload Object event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header Map<String, Object> headers,
            Acknowledgment acknowledgment) {
        
        log.info("Received AccountChangedEvent from topic: {}", topic);
        
        try {
            // TODO: Implement account change validation logic
            log.debug("Account change validation not yet implemented");
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Failed to process AccountChangedEvent", e);
            throw e;
        }
    }

    /**
     * Extract and validate correlation ID from headers
     */
    private String extractAndValidateCorrelationId(Map<String, Object> headers, String eventId) {
        Object correlationId = headers.get("X-Correlation-ID");
        if (correlationId == null || correlationId.toString().trim().isEmpty()) {
            String generatedCorrelationId = UUID.randomUUID().toString();
            log.warn("No correlation ID found in headers for event: {}, generated: {}", 
                    eventId, generatedCorrelationId);
            return generatedCorrelationId;
        }
        return correlationId.toString();
    }

    /**
     * Extract and validate tenant ID from headers
     */
    private String extractAndValidateTenantId(Map<String, Object> headers, String eventId) {
        Object tenantId = headers.get("X-Tenant-ID");
        if (tenantId == null || tenantId.toString().trim().isEmpty()) {
            log.error("No tenant ID found in headers for event: {}", eventId);
            throw new IllegalArgumentException("Tenant ID is required for payment validation");
        }
        return tenantId.toString();
    }

    /**
     * Extract and validate business unit ID from headers
     */
    private String extractAndValidateBusinessUnitId(Map<String, Object> headers, String eventId) {
        Object businessUnitId = headers.get("X-Business-Unit-ID");
        if (businessUnitId == null || businessUnitId.toString().trim().isEmpty()) {
            log.error("No business unit ID found in headers for event: {}", eventId);
            throw new IllegalArgumentException("Business unit ID is required for payment validation");
        }
        return businessUnitId.toString();
    }
}
