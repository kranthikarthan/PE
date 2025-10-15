package com.payments.validation.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import static org.mockito.Mockito.mock;

/**
 * Test Configuration
 * 
 * Provides mock beans for testing:
 * - KafkaTemplate mock
 * - ProducerFactory mock
 * - Other test-specific configurations
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public KafkaTemplate<String, Object> mockKafkaTemplate() {
        return mock(KafkaTemplate.class);
    }

    @Bean
    @Primary
    public ProducerFactory<String, Object> mockProducerFactory() {
        return mock(ProducerFactory.class);
    }
}
