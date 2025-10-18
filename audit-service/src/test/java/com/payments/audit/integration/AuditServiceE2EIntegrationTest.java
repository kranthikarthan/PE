package com.payments.audit.integration;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.audit.entity.AuditEventEntity;
import com.payments.audit.service.AuditEventProcessor.AuditEventPayload;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class AuditServiceE2EIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15").withDatabaseName("testdb").withUsername("test")
          .withPassword("test");

  @Container
  static KafkaContainer kafka = new KafkaContainer(
      DockerImageName.parse("confluentinc/cp-kafka:7.5.1"));

  @Autowired private ObjectMapper objectMapper;

  @Autowired private org.springframework.core.env.Environment env;

  @Autowired private org.springframework.context.ApplicationContext context;

  @Autowired private com.payments.audit.repository.AuditEventRepository repository;

  static KafkaProducer<String, String> producer;

  @BeforeAll
  static void setupKafkaProducer() {
    var props = KafkaTestUtils.producerProps(kafka.getBootstrapServers());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    producer = new KafkaProducer<>(props);
  }

  @DynamicPropertySource
  static void registerKafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    registry.add("spring.kafka.consumer.group-id", () -> "audit-service-group-e2e");
    registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
  }

  @AfterAll
  static void closeKafkaProducer() {
    if (producer != null) {
      producer.close(Duration.ofSeconds(1));
    }
  }

  @Test
  void shouldConsumeAndPersistAuditEvent() throws Exception {
    // Arrange
    String tenantId = UUID.randomUUID().toString();
    AuditEventPayload payload =
        new AuditEventPayload(
            tenantId,
            "user@example.com",
            "LOGIN",
            "USER_ACCOUNT",
            null,
            AuditEventEntity.AuditResult.SUCCESS.name(),
            "{}",
            null,
            "127.0.0.1",
            "JUnit");

    String json = objectMapper.writeValueAsString(payload);

    // Act: publish to Kafka topic consumed by AuditEventConsumer
    producer.send(new ProducerRecord<>("payment-audit-logs", UUID.randomUUID().toString(), json));
    producer.flush();

    // Assert: await persistence in Postgres
    await().atMost(30, SECONDS)
        .pollInterval(Duration.ofSeconds(1))
        .untilAsserted(
            () -> {
              Page<AuditEventEntity> page =
                  repository.findByTenantId(UUID.fromString(tenantId), PageRequest.of(0, 10));
              org.assertj.core.api.Assertions.assertThat(page.getTotalElements()).isGreaterThan(0);
            });
  }
}


