package com.payments.settlement.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.settlement.SettlementServiceApplication;
import com.payments.settlement.event.SettlementEventPublisher;
import com.payments.settlement.event.SettlementEvents;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(classes = SettlementServiceApplication.class)
@Testcontainers
@DisplayName("Settlement Kafka publishing with Testcontainers")
@ActiveProfiles("test")
class SettlementKafkaContainerTest {

  private static final DockerImageName KAFKA_IMAGE =
      DockerImageName.parse("confluentinc/cp-kafka:7.5.1");

  @Container static KafkaContainer kafka = new KafkaContainer(KAFKA_IMAGE);

  @DynamicPropertySource
  static void registerProps(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
  }

  @Autowired private SettlementEventPublisher publisher;

  @Test
  void shouldPublishToBatchCreatedTopic() throws Exception {
    // Arrange
    var event =
        new SettlementEvents.SettlementBatchCreatedEvent(
            "B-IT-" + UUID.randomUUID(), "RTC", Instant.now(), 0, java.math.BigDecimal.ZERO);
    String topic = "settlement-batch-created"; // matches default in application.yml

    // Create a consumer against the Testcontainers Kafka
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "settlement-it-" + UUID.randomUUID());
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
      consumer.subscribe(Collections.singletonList(topic));

      // Act
      publisher.publish(event);

      // Assert
      ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
      boolean found = false;
      for (var r : records.records(topic)) {
        if (event.batchId().equals(r.key())) {
          found = true;
          break;
        }
      }
      assertThat(found).isTrue();
    }
  }
}
