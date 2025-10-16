package com.payments.saga.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.domain.shared.TenantContext;
import com.payments.saga.service.SagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumer for PaymentInitiatedEvent to start payment processing sagas
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentInitiatedEventConsumer {

    private final SagaOrchestrator sagaOrchestrator;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment.initiated", groupId = "saga-orchestrator")
    public void handlePaymentInitiated(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = "X-Tenant-Id", required = false) String tenantId,
            @Header(value = "X-Business-Unit-Id", required = false) String businessUnitId,
            @Header(value = "X-Correlation-Id", required = false) String correlationId) {
        
        try {
            log.info("Received PaymentInitiatedEvent from topic {} (partition: {}, offset: {})", topic, partition, offset);
            
            // Parse the event payload
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            String paymentId = (String) eventData.get("paymentId");
            String sagaTemplate = determineSagaTemplate(eventData);
            
            // Create tenant context
            TenantContext tenantContext = TenantContext.of(
                tenantId != null ? tenantId : "default-tenant",
                "Tenant",
                businessUnitId != null ? businessUnitId : "default-bu",
                "Business Unit"
            );
            
            // Start the appropriate saga
            sagaOrchestrator.startSaga(
                sagaTemplate,
                tenantContext,
                correlationId != null ? correlationId : "corr-" + System.currentTimeMillis(),
                paymentId,
                eventData
            );
            
            log.info("Successfully started saga for payment {}", paymentId);
            
        } catch (Exception e) {
            log.error("Failed to process PaymentInitiatedEvent: {}", e.getMessage(), e);
            // In a real implementation, you might want to send to a dead letter queue
        }
    }

    /**
     * Determine the appropriate saga template based on payment characteristics
     */
    private String determineSagaTemplate(Map<String, Object> eventData) {
        // Extract payment characteristics
        Object amount = eventData.get("amount");
        Object paymentType = eventData.get("paymentType");
        Object priority = eventData.get("priority");
        
        // Determine template based on business rules
        if (isHighValuePayment(amount)) {
            return "HighValuePaymentSaga";
        } else if (isFastPayment(paymentType, priority)) {
            return "FastPaymentSaga";
        } else {
            return "PaymentProcessingSaga";
        }
    }

    private boolean isHighValuePayment(Object amount) {
        if (amount instanceof Number) {
            return ((Number) amount).doubleValue() > 10000.0; // $10,000 threshold
        }
        return false;
    }

    private boolean isFastPayment(Object paymentType, Object priority) {
        return "FAST".equals(paymentType) || "URGENT".equals(priority);
    }
}






