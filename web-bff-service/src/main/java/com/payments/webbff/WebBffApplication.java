package com.payments.webbff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Web BFF (Backend for Frontend) Application.
 *
 * <p>This application provides a GraphQL-based API layer for frontend applications,
 * aggregating data from multiple microservices in the Payment Engine.
 *
 * <p>Features:
 * - GraphQL API with comprehensive schema
 * - Real-time subscriptions for live updates
 * - Multi-tenant support with security
 * - Caching and performance optimization
 * - Service integration via OpenFeign
 * - Circuit breaker patterns for resilience
 *
 * @since PE-414
 */
@SpringBootApplication
@EnableFeignClients
@EnableCaching
@EnableAsync
@EnableScheduling
public class WebBffApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebBffApplication.class, args);
    }
}
