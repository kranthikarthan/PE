package com.payments.saga.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.saga.service.SagaOrchestrator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** Consumer for TransactionCreatedEvent to handle transaction processing step completion */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionCreatedEventConsumer {

  private final SagaOrchestrator sagaOrchestrator;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "transaction.created", groupId = "saga-orchestrator")
  public void handleTransactionCreated(
      @Payload String message,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
      @Header(KafkaHeaders.OFFSET) long offset,
      @Header(value = "X-Correlation-Id", required = false) String correlationId) {

    try {
      log.info(
          "Received TransactionCreatedEvent from topic {} (partition: {}, offset: {})",
          topic,
          partition,
          offset);

      // Parse the event payload
      Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
      String transactionId = (String) eventData.get("transactionId");
      String paymentId = (String) eventData.get("paymentId");
      String status = (String) eventData.get("status");

      log.info(
          "Transaction {} created for payment {} with status: {}",
          transactionId,
          paymentId,
          status);

      // The saga orchestrator will automatically move to the next step
      // based on the step execution flow

    } catch (Exception e) {
      log.error("Failed to process TransactionCreatedEvent: {}", e.getMessage(), e);
    }
  }
}
