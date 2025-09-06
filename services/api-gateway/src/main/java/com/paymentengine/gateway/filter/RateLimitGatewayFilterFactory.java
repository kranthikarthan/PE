package com.paymentengine.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Custom Rate Limiting Gateway Filter with Redis backend
 */
@Component
public class RateLimitGatewayFilterFactory 
    extends AbstractGatewayFilterFactory<RateLimitGatewayFilterFactory.Config> {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitGatewayFilterFactory.class);
    
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    
    public RateLimitGatewayFilterFactory(ReactiveRedisTemplate<String, String> redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String key = generateRateLimitKey(exchange, config);
            
            return checkRateLimit(key, config)
                .flatMap(allowed -> {
                    if (allowed) {
                        return chain.filter(exchange);
                    } else {
                        return handleRateLimitExceeded(exchange, config);
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Error in rate limiting: {}", e.getMessage());
                    // On Redis error, allow the request to continue
                    return chain.filter(exchange);
                });
        };
    }
    
    private String generateRateLimitKey(ServerWebExchange exchange, Config config) {
        String identifier = getIdentifier(exchange, config);
        String path = exchange.getRequest().getURI().getPath();
        
        // Create a key that includes the identifier, path pattern, and time window
        long windowStart = Instant.now().getEpochSecond() / config.getWindowSizeInSeconds();
        
        return String.format("rate_limit:%s:%s:%d", 
            identifier, 
            sanitizePath(path), 
            windowStart);
    }
    
    private String getIdentifier(ServerWebExchange exchange, Config config) {
        // Priority order: User ID > API Key > IP Address
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }
        
        String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return "api:" + apiKey;
        }
        
        // Fall back to IP address
        String clientIp = getClientIpAddress(exchange);
        return "ip:" + clientIp;
    }
    
    private String getClientIpAddress(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return exchange.getRequest().getRemoteAddress() != null 
            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
            : "unknown";
    }
    
    private String sanitizePath(String path) {
        // Replace path variables with placeholders for consistent rate limiting
        return path.replaceAll("/[0-9a-fA-F-]{36}", "/{id}")  // UUID patterns
                  .replaceAll("/\\d+", "/{id}")                 // Numeric IDs
                  .replaceAll("/[a-zA-Z0-9-_]+", "/{param}");   // Other parameters
    }
    
    private Mono<Boolean> checkRateLimit(String key, Config config) {
        return redisTemplate.opsForValue()
            .increment(key)
            .flatMap(count -> {
                if (count == 1) {
                    // First request in this window, set expiration
                    return redisTemplate.expire(key, Duration.ofSeconds(config.getWindowSizeInSeconds()))
                        .thenReturn(count <= config.getRequestsPerWindow());
                } else {
                    return Mono.just(count <= config.getRequestsPerWindow());
                }
            })
            .doOnNext(allowed -> {
                if (!allowed) {
                    logger.warn("Rate limit exceeded for key: {}", key);
                }
            });
    }
    
    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange, Config config) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        // Add rate limit headers
        response.getHeaders().add("X-RateLimit-Limit", String.valueOf(config.getRequestsPerWindow()));
        response.getHeaders().add("X-RateLimit-Window", String.valueOf(config.getWindowSizeInSeconds()));
        response.getHeaders().add("X-RateLimit-Remaining", "0");
        
        String errorResponse = String.format(
            "{\"error\":{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Rate limit exceeded. Maximum %d requests per %d seconds allowed.\",\"timestamp\":\"%s\"}}",
            config.getRequestsPerWindow(),
            config.getWindowSizeInSeconds(),
            Instant.now().toString()
        );
        
        org.springframework.core.io.buffer.DataBuffer buffer = response.bufferFactory()
            .wrap(errorResponse.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("requestsPerWindow", "windowSizeInSeconds");
    }
    
    public static class Config {
        private int requestsPerWindow = 100;
        private int windowSizeInSeconds = 60;
        
        public int getRequestsPerWindow() {
            return requestsPerWindow;
        }
        
        public void setRequestsPerWindow(int requestsPerWindow) {
            this.requestsPerWindow = requestsPerWindow;
        }
        
        public int getWindowSizeInSeconds() {
            return windowSizeInSeconds;
        }
        
        public void setWindowSizeInSeconds(int windowSizeInSeconds) {
            this.windowSizeInSeconds = windowSizeInSeconds;
        }
    }
}