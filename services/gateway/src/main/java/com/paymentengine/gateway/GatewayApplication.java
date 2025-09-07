package com.paymentengine.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.support.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

/**
 * API Gateway Application for ISO 20022 Payment Engine
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ISO 20022 Comprehensive API Routes
                .route("iso20022-comprehensive", r -> r
                        .path("/api/v1/iso20022/comprehensive/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                                .circuitBreaker(config -> config
                                        .setName("iso20022-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/iso20022")
                                )
                                .addRequestHeader("X-Gateway", "payment-engine-gateway")
                                .addResponseHeader("X-Response-Time", "true")
                        )
                        .uri("lb://middleware-service")
                )
                
                // Scheme Configuration API Routes
                .route("scheme-config", r -> r
                        .path("/api/v1/scheme/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                                .circuitBreaker(config -> config
                                        .setName("scheme-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/scheme")
                                )
                        )
                        .uri("lb://middleware-service")
                )
                
                // Clearing System API Routes
                .route("clearing-system", r -> r
                        .path("/api/v1/clearing-system/**")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                                .circuitBreaker(config -> config
                                        .setName("clearing-system-circuit-breaker")
                                        .setFallbackUri("forward:/fallback/clearing-system")
                                )
                        )
                        .uri("lb://middleware-service")
                )
                
                // Health Check Routes
                .route("health-check", r -> r
                        .path("/health/**")
                        .filters(f -> f
                                .stripPrefix(1)
                        )
                        .uri("lb://middleware-service")
                )
                
                // Metrics Routes
                .route("metrics", r -> r
                        .path("/actuator/**")
                        .filters(f -> f
                                .stripPrefix(1)
                        )
                        .uri("lb://middleware-service")
                )
                
                .build();
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(100, 200, 1);
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getHeaders().getFirst("X-User-ID"))
                .defaultIfEmpty("anonymous");
    }

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}