package com.payments.accountadapter.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test Configuration
 * 
 * Configuration for integration tests:
 * - WireMock server setup
 * - Test profiles
 * - Mock services
 */
@Slf4j
@TestConfiguration
@ActiveProfiles("test")
public class TestConfig {

    /**
     * WireMock server for external service mocking
     */
    @Bean
    @Primary
    public WireMockServer wireMockServer() {
        WireMockConfiguration config = WireMockConfiguration.options()
                .port(8089)
                .extensions(new com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer(false))
                .usingFilesUnderClasspath("wiremock");
        
        WireMockServer server = new WireMockServer(config);
        server.start();
        
        log.info("WireMock server started on port: {}", server.port());
        return server;
    }
}
