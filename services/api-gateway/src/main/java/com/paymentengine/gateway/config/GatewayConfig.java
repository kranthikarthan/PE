package com.paymentengine.gateway.config;

import com.paymentengine.gateway.filter.AuthenticationGatewayFilterFactory;
import com.paymentengine.gateway.filter.RateLimitGatewayFilterFactory;
import com.paymentengine.gateway.filter.LoggingGatewayFilterFactory;
import com.paymentengine.gateway.filter.RequestResponseLoggingFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Gateway configuration for routing, filtering, and rate limiting
 */
@Configuration
public class GatewayConfig {

    /**
     * Global filter for request/response logging
     */
    @Bean
    public GlobalFilter customGlobalFilter() {
        return new RequestResponseLoggingFilter();
    }

    /**
     * Rate limiter configuration using Redis
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // Default: 100 requests per second, burst capacity of 200
        return new RedisRateLimiter(100, 200, 1);
    }

    /**
     * Key resolver for rate limiting - uses API key or IP address
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            // Try to get API key from header
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (apiKey != null && !apiKey.isEmpty()) {
                return Mono.just("api-key:" + apiKey);
            }
            
            // Try to get user ID from JWT token (set by auth filter)
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            if (userId != null && !userId.isEmpty()) {
                return Mono.just("user:" + userId);
            }
            
            // Fall back to IP address
            String clientIp = getClientIpAddress(exchange);
            return Mono.just("ip:" + clientIp);
        };
    }

    /**
     * Rate limiter for API endpoints
     */
    @Bean
    public RedisRateLimiter apiRateLimiter() {
        // API endpoints: 1000 requests per minute, burst capacity of 1500
        return new RedisRateLimiter(1000, 1500, 60);
    }

    /**
     * Rate limiter for authentication endpoints
     */
    @Bean
    public RedisRateLimiter authRateLimiter() {
        // Auth endpoints: 10 requests per minute, burst capacity of 20
        return new RedisRateLimiter(10, 20, 60);
    }

    /**
     * Rate limiter for webhook endpoints
     */
    @Bean
    public RedisRateLimiter webhookRateLimiter() {
        // Webhook endpoints: 500 requests per minute, burst capacity of 1000
        return new RedisRateLimiter(500, 1000, 60);
    }

    /**
     * Custom authentication filter factory
     */
    @Bean
    public AuthenticationGatewayFilterFactory authenticationGatewayFilterFactory() {
        return new AuthenticationGatewayFilterFactory();
    }

    /**
     * Custom rate limiting filter factory
     */
    @Bean
    public RateLimitGatewayFilterFactory rateLimitGatewayFilterFactory() {
        return new RateLimitGatewayFilterFactory();
    }

    /**
     * Custom logging filter factory
     */
    @Bean
    public LoggingGatewayFilterFactory loggingGatewayFilterFactory() {
        return new LoggingGatewayFilterFactory();
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
    }
}