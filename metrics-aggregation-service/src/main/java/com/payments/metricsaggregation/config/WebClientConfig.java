package com.payments.metricsaggregation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * WebClient Configuration
 * 
 * Configures WebClient for metrics collection from all services.
 * Provides reactive HTTP client for Prometheus metrics scraping.
 */
@Configuration
public class WebClientConfig {

    /**
     * WebClient bean for metrics collection
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB buffer
            .build();
    }

    /**
     * WebClient with timeout configuration
     */
    @Bean
    public WebClient metricsWebClient() {
        return WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
    }
}
