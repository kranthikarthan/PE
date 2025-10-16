package com.payments.saga.event;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Consumer for PaymentRoutedEvent to handle routing step completion
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRoutedEventConsumer {

    private final SagaOrchestrator sagaOrchestrator;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment.routed", groupId = "saga-orchestrator")
    public void handlePaymentRouted(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = "X-Correlation-Id", required = false) String correlationId) {
        
        try {
            log.info("Received PaymentRoutedEvent from topic {} (partition: {}, offset: {})", topic, partition, offset);
            
            // Parse the event payload
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            String paymentId = (String) eventData.get("paymentId");
            String clearingSystem = (String) eventData.get("clearingSystem");
            String routingDecision = (String) eventData.get("routingDecision");
            
            log.info("Payment {} routed to {} with decision: {}", paymentId, clearingSystem, routingDecision);
            
            // The saga orchestrator will automatically move to the next step
            // based on the step execution flow
            
        } catch (Exception e) {
            log.error("Failed to process PaymentRoutedEvent: {}", e.getMessage(), e);
        }
    }
}






