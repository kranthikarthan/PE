package com.paymentengine.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * API Gateway Application for Payment Engine
 * 
 * Provides centralized entry point with:
 * - Request routing and load balancing
 * - Authentication and authorization
 * - Rate limiting and throttling
 * - Circuit breaker integration
 * - Request/response transformation
 * - Comprehensive monitoring
 */
@SpringBootApplication(scanBasePackages = {
    "com.paymentengine.gateway",
    "com.paymentengine.shared"
})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Define routes programmatically for complex routing logic
     * Most routes are defined in application.yml, but complex ones can be defined here
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Health check route - no authentication required
            .route("health-check", r -> r
                .path("/health", "/actuator/health")
                .uri("http://localhost:8080"))
            
            // API documentation route
            .route("api-docs", r -> r
                .path("/api-docs/**", "/swagger-ui/**")
                .uri("http://localhost:8080"))
            
            // Webhook routes - special handling for external systems
            .route("webhooks", r -> r
                .path("/webhooks/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Gateway-Source", "webhook")
                    .circuitBreaker(c -> c
                        .setName("webhook-circuit-breaker")
                        .setFallbackUri("forward:/fallback/webhook")))
                .uri("lb://core-banking-service"))
            
            .build();
    }
}