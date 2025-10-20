package com.payments.e2e.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import javax.annotation.PreDestroy;

/**
 * E2E Test Configuration
 * 
 * Provides test infrastructure including:
 * - TestContainers for PostgreSQL, Kafka, Redis
 * - WireMock servers for external service mocking
 * - Test data builders and utilities
 */
@TestConfiguration
@ActiveProfiles("e2e")
public class E2ETestConfiguration {

    // TestContainers
    private static PostgreSQLContainer<?> postgresContainer;
    private static KafkaContainer kafkaContainer;
    private static GenericContainer<?> redisContainer;
    
    // WireMock Servers
    private static WireMockServer coreBankingMock;
    private static WireMockServer fraudApiMock;
    private static WireMockServer clearingSystemMock;

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public PostgreSQLContainer<?> postgresContainer() {
        if (postgresContainer == null) {
            postgresContainer = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("payments_e2e_test")
                    .withUsername("payments_test")
                    .withPassword("payments_test")
                    .withInitScript("test-data/init-test-db.sql");
            postgresContainer.start();
        }
        return postgresContainer;
    }

    @Bean
    public KafkaContainer kafkaContainer() {
        if (kafkaContainer == null) {
            kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
                    .withEmbeddedZookeeper();
            kafkaContainer.start();
        }
        return kafkaContainer;
    }

    @Bean
    public GenericContainer<?> redisContainer() {
        if (redisContainer == null) {
            redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);
            redisContainer.start();
        }
        return redisContainer;
    }

    @Bean
    public WireMockServer coreBankingMock() {
        if (coreBankingMock == null) {
            coreBankingMock = new WireMockServer(
                WireMockConfiguration.options()
                    .port(8089)
                    .usingFilesUnderDirectory("src/test/resources/wiremock/core-banking")
            );
            coreBankingMock.start();
        }
        return coreBankingMock;
    }

    @Bean
    public WireMockServer fraudApiMock() {
        if (fraudApiMock == null) {
            fraudApiMock = new WireMockServer(
                WireMockConfiguration.options()
                    .port(8090)
                    .usingFilesUnderDirectory("src/test/resources/wiremock/fraud-api")
            );
            fraudApiMock.start();
        }
        return fraudApiMock;
    }

    @Bean
    public WireMockServer clearingSystemMock() {
        if (clearingSystemMock == null) {
            clearingSystemMock = new WireMockServer(
                WireMockConfiguration.options()
                    .port(8091)
                    .usingFilesUnderDirectory("src/test/resources/wiremock/clearing-systems")
            );
            clearingSystemMock.start();
        }
        return clearingSystemMock;
    }

    @PreDestroy
    public void cleanup() {
        if (postgresContainer != null) {
            postgresContainer.stop();
        }
        if (kafkaContainer != null) {
            kafkaContainer.stop();
        }
        if (redisContainer != null) {
            redisContainer.stop();
        }
        if (coreBankingMock != null) {
            coreBankingMock.stop();
        }
        if (fraudApiMock != null) {
            fraudApiMock.stop();
        }
        if (clearingSystemMock != null) {
            clearingSystemMock.stop();
        }
    }
}
