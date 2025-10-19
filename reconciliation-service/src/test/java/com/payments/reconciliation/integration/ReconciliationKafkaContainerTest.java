package com.payments.reconciliation.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.reconciliation.ReconciliationServiceApplication;
import com.payments.reconciliation.event.ReconciliationEventPublisher;
import com.payments.reconciliation.event.ReconciliationEvents;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
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

@SpringBootTest(classes = ReconciliationServiceApplication.class)
@ActiveProfiles("test")
@Testcontainers
class ReconciliationKafkaContainerTest {

  private static final DockerImageName KAFKA_IMAGE =
      DockerImageName.parse("confluentinc/cp-kafka:7.5.1");

  @Container static KafkaContainer kafka = new KafkaContainer(KAFKA_IMAGE);

  @DynamicPropertySource
  static void registerProps(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    registry.add("app.kafka.topics.reconciliation-run-started", () -> "recon-run-started");
    registry.add("app.kafka.topics.reconciliation-run-completed", () -> "recon-run-completed");
    // Disable vault in tests if present
    registry.add("spring.cloud.vault.enabled", () -> "false");
    // Optional config imports
    registry.add("spring.config.import", () -> "optional:configserver:,optional:consul:");
  }

  @Autowired private ReconciliationEventPublisher publisher;

  @Test
  void shouldPublishRunStartedEvent_ToKafka() {
    String topic = "recon-run-started";
    String testKey = UUID.randomUUID().toString();
    publisher.publish(
        new ReconciliationEvents.ReconciliationRunStartedEvent(testKey, "RTC", Instant.now()));

    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "recon-it-" + UUID.randomUUID());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
      consumer.subscribe(java.util.Collections.singletonList(topic));
      ConsumerRecords<String, String> records = consumer.poll(java.time.Duration.ofSeconds(10));
      boolean found = false;
      for (var r : records.records(topic)) {
        if (testKey.equals(r.key())) {
          found = true;
          break;
        }
      }
      assertThat(found).isTrue();
    }
  }
}
