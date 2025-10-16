package com.payments.saga.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.saga.domain.Saga;
import com.payments.saga.domain.SagaStep;
import com.payments.saga.service.SagaLookupService;
import com.payments.saga.service.SagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Consumer for PaymentValidatedEvent to handle validation step completion
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentValidatedEventConsumer {

    private final SagaOrchestrator sagaOrchestrator;
    private final SagaLookupService sagaLookupService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment.validated", groupId = "saga-orchestrator")
    public void handlePaymentValidated(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = "X-Correlation-Id", required = false) String correlationId) {
        
        try {
            log.info("Received PaymentValidatedEvent from topic {} (partition: {}, offset: {})", topic, partition, offset);
            
            // Parse the event payload
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            String paymentId = (String) eventData.get("paymentId");
            Boolean isValid = (Boolean) eventData.get("isValid");
            String validationResult = (String) eventData.get("validationResult");
            
            if (isValid != null && isValid) {
                // Validation successful - continue with saga
                log.info("Payment {} validation successful, continuing saga", paymentId);
                // The saga orchestrator will automatically move to the next step
            } else {
                // Validation failed - start compensation
                log.warn("Payment {} validation failed: {}", paymentId, validationResult);
                
                // Find the saga and start compensation
                Optional<Saga> saga = sagaLookupService.findSagaByPaymentId(paymentId);
                if (saga.isPresent()) {
                    sagaOrchestrator.startCompensation(saga.get().getId(), "Validation failed: " + validationResult);
                    log.info("Started compensation for saga {} due to validation failure", saga.get().getId().getValue());
                } else {
                    log.error("No saga found for payment {} to start compensation", paymentId);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to process PaymentValidatedEvent: {}", e.getMessage(), e);
        }
    }
}
