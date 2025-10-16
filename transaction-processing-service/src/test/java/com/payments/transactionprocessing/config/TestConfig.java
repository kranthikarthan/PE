package com.payments.transactionprocessing.config;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@TestConfiguration
public class TestConfig {

  @Bean
  @Primary
  public KafkaTemplate<String, Object> kafkaTemplate() {
    return mock(KafkaTemplate.class);
  }

  @Bean
  @Primary
  public ProducerFactory<String, Object> producerFactory() {
    return mock(ProducerFactory.class);
  }
}
