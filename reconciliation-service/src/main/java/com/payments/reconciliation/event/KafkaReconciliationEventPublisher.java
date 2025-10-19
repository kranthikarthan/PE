package com.payments.reconciliation.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Primary
public class KafkaReconciliationEventPublisher implements ReconciliationEventPublisher {
  private static final Logger log =
      LoggerFactory.getLogger(KafkaReconciliationEventPublisher.class);

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final String startedTopic;
  private final String completedTopic;

  public KafkaReconciliationEventPublisher(
      KafkaTemplate<String, String> kafkaTemplate,
      ObjectMapper objectMapper,
      @Value("${app.kafka.topics.reconciliation-run-started:reconciliation-run-started}")
          String startedTopic,
      @Value("${app.kafka.topics.reconciliation-run-completed:reconciliation-run-completed}")
          String completedTopic) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.startedTopic = startedTopic;
    this.completedTopic = completedTopic;
  }

  @Override
  public void publish(ReconciliationEvents.ReconciliationRunStartedEvent event) {
    send(startedTopic, event.reconciliationId(), event);
  }

  @Override
  public void publish(ReconciliationEvents.ReconciliationRunCompletedEvent event) {
    send(completedTopic, event.reconciliationId(), event);
  }

  private void send(String topic, String key, Object payload) {
    try {
      String value = objectMapper.writeValueAsString(payload);
      kafkaTemplate.send(new ProducerRecord<>(topic, key, value));
      log.info("Published to topic={} key={} payload={}", topic, key, value);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize event for topic={} key={}", topic, key, e);
    }
  }
}
