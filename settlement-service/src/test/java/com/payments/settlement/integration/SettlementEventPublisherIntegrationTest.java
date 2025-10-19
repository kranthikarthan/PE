package com.payments.settlement.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.settlement.SettlementServiceApplication;
import com.payments.settlement.event.SettlementEvents;
import java.time.Instant;
import java.util.Map;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = SettlementServiceApplication.class)
@EmbeddedKafka(
    partitions = 1,
    topics = {"settlement-batch-created"})
@ActiveProfiles("test")
class SettlementEventPublisherIntegrationTest {

  @Autowired private KafkaTemplate<String, String> kafkaTemplate;
  @Autowired private EmbeddedKafkaBroker embeddedKafka;

  @Test
  void shouldPublishBatchCreatedEvent_ToKafkaTopic() throws Exception {
    // Arrange
    var event =
        new SettlementEvents.SettlementBatchCreatedEvent(
            "B-1", "RTC", Instant.now(), 0, java.math.BigDecimal.ZERO);

    // Set consumer to read from topic
    Map<String, Object> consumerProps =
        KafkaTestUtils.consumerProps("test-group", "true", embeddedKafka);
    consumerProps.put(
        org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    var cf =
        new DefaultKafkaConsumerFactory<>(
            consumerProps, new StringDeserializer(), new StringDeserializer());
    var consumer = cf.createConsumer();
    embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "settlement-batch-created");
    // No container needed; directly poll using KafkaTestUtils

    // Act
    var om = new ObjectMapper().findAndRegisterModules();
    var json = om.writeValueAsString(event);
    kafkaTemplate.send("settlement-batch-created", event.batchId(), json).get();

    // Assert
    var singleRecord = KafkaTestUtils.getSingleRecord(consumer, "settlement-batch-created");
    assertThat(singleRecord.value()).contains("\"batchId\":\"B-1\"");

    consumer.close();
  }
}
