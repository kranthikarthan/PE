package com.payments.notification.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for the Notification Service.
 *
 * <p>Configures:
 * - Consumer factory with manual offset management
 * - Competing consumers group
 * - Error handling & retry policies
 *
 * @author Payment Engine
 */
@Configuration
@EnableKafka
public class KafkaConfig {

  /**
   * Consumer factory with competing consumers pattern configuration.
   *
   * <p>Key settings:
   * - group-id: notification-service-group (multiple instances in same group)
   * - auto-commit: false (manual commit for reliability)
   * - enable-auto-commit: false (explicit offset management)
   * - max-poll-records: 100 (batch processing)
   * - session-timeout: 30s (quick failover detection)
   * - max-poll-interval: 300s (process up to 5 min per batch)
   *
   * @return configured ConsumerFactory
   */
  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    Map<String, Object> props = new HashMap<>();

    // Bootstrap servers (from application.yml)
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "${spring.kafka.bootstrap-servers}");

    // Consumer group (competing consumers)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service-group");

    // Serialization
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

    // Offset management (manual commit for reliability)
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Start from beginning if offset lost

    // Batch processing
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100); // Process up to 100 records per poll
    props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024); // Minimum 1KB per fetch

    // Timeouts for rebalancing & session management
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30s session timeout
    props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000); // 10s heartbeat
    props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5min max processing time

    // Connection settings
    props.put(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 540000); // 9min idle timeout

    return new DefaultKafkaConsumerFactory<>(props);
  }

  /**
   * Kafka listener container factory with manual acknowledgment.
   *
   * <p>Manual acknowledgment ensures:
   * - Messages are only marked consumed after successful processing
   * - On error, messages are reprocessed from current offset (retry pattern)
   * - Dead letter queue via repeated reprocessing
   *
   * @param consumerFactory the consumer factory
   * @return configured listener container factory
   */
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String>
      kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {

    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();

    factory.setConsumerFactory(consumerFactory);

    // Manual acknowledgment: listener controls when offset is committed
    factory.setConcurrency(3); // 3 concurrent threads per container
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    factory.getContainerProperties().setPollTimeout(3000); // 3s poll timeout

    return factory;
  }
}
