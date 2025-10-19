package com.payments.settlement.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class KafkaSettlementEventPublisher implements SettlementEventPublisher {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Value("${app.kafka.topics.settlement-batch-created:settlement-batch-created}")
  private String batchCreatedTopic;

  @Value("${app.kafka.topics.settlement-completed:settlement-completed}")
  private String settlementCompletedTopic;

  @Override
  public void publish(SettlementEvents.SettlementBatchCreatedEvent event) {
    try {
      String key = event.batchId();
      String value = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(batchCreatedTopic, key, value);
    } catch (Exception e) {
      log.warn("Failed to publish SettlementBatchCreatedEvent: {}", e.getMessage());
    }
  }

  @Override
  public void publish(SettlementEvents.SettlementCompletedEvent event) {
    try {
      String key = event.batchId();
      String value = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(settlementCompletedTopic, key, value);
    } catch (Exception e) {
      log.warn("Failed to publish SettlementCompletedEvent: {}", e.getMessage());
    }
  }
}
