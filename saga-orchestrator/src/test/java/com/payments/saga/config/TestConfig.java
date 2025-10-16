package com.payments.saga.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.mock;

/**
 * Test configuration for Saga Orchestrator tests
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return mock(RestTemplate.class);
    }

    @Bean
    @Primary
    public KafkaTemplate<String, String> kafkaTemplate() {
        return mock(KafkaTemplate.class);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}






