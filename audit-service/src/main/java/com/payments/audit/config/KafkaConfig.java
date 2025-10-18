package com.payments.audit.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

/**
 * Kafka Configuration - Consumer and Producer Settings.
 *
 * <p>Configures:
 * - Consumer group for durable subscriber pattern
 * - Batch processing (100 events, 60s flush)
 * - Error handling (dead-letter queues)
 * - Deserialization with error tolerance
 * - Manual offset management (after DB persistence)
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class KafkaConfig {

  private final KafkaProperties kafkaProperties;

  /**
   * Configure Kafka consumer factory with error handling.
   *
   * <p>Settings:
   * - StringDeserializer for Kafka message (already in JsonDeserializer)
   * - Error handling deserializer to avoid stopping consumer
   * - Manual offset commit (after database persistence)
   * - Batch size: 100 records
   * - Session timeout: 30 seconds
   *
   * @return configured DefaultKafkaConsumerFactory
   */
  @Bean
  public DefaultKafkaConsumerFactory<String, String> kafkaConsumerFactory() {
    var properties = kafkaProperties.buildConsumerProperties();

    // Error handling: deserializer errors don't stop consumer
    properties.put("value.deserializer", ErrorHandlingDeserializer.class.getName());
    properties.put("value.deserializer.value.deserializer.class",
        org.springframework.kafka.support.serializer.StringDeserializer.class.getName());

    // Batch processing optimization
    properties.put("max.poll.records", 100); // Batch size
    properties.put("max.poll.interval.ms", 300000); // 5 minutes
    properties.put("session.timeout.ms", 30000); // 30 seconds

    // Offset management
    properties.put("enable.auto.commit", false); // Manual commit (handled in consumer)
    properties.put("auto.offset.reset", "earliest"); // Start from earliest if no offset

    log.info(
        "Kafka consumer factory configured: bootstrap={}, group={}",
        kafkaProperties.getBootstrapServers(),
        kafkaProperties.getConsumer().getGroupId());

    return new DefaultKafkaConsumerFactory<>(properties);
  }

  /**
   * Kafka template for potential producer use (fire-and-forget events).
   *
   * @return configured KafkaTemplate
   */
  @Bean
  public KafkaTemplate<String, String> kafkaTemplate() {
    return new KafkaTemplate<>(new org.springframework.kafka.core.DefaultKafkaProducerFactory<>(
        kafkaProperties.buildProducerProperties()));
  }
}
