package com.paymentengine.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Global filter for request/response logging and correlation tracking
 */
@Component
public class RequestResponseLoggingFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String REQUEST_START_TIME = "REQUEST_START_TIME";
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        return Mono.fromRunnable(() -> preProcess(exchange))
            .then(chain.filter(exchange))
            .doOnSuccess(aVoid -> postProcess(exchange))
            .doOnError(throwable -> errorProcess(exchange, throwable));
    }
    
    private void preProcess(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Generate or extract correlation ID
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        // Set MDC for logging
        MDC.put("correlationId", correlationId);
        MDC.put("requestId", UUID.randomUUID().toString());
        
        // Store start time for performance measurement
        exchange.getAttributes().put(REQUEST_START_TIME, Instant.now());
        
        // Add correlation ID to response headers
        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);
        
        // Mutate request to add correlation ID if not present
        if (request.getHeaders().getFirst(CORRELATION_ID_HEADER) == null) {
            ServerHttpRequest mutatedRequest = request.mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build();
            exchange.mutate().request(mutatedRequest).build();
        }
        
        // Log request details
        logRequest(request, correlationId);
    }
    
    private void postProcess(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        Instant startTime = exchange.getAttribute(REQUEST_START_TIME);
        
        if (startTime != null) {
            long duration = Instant.now().toEpochMilli() - startTime.toEpochMilli();
            
            // Add performance headers
            response.getHeaders().add("X-Response-Time", duration + "ms");
            
            // Log response details
            logResponse(exchange.getRequest(), response, duration);
        }
        
        // Clear MDC
        MDC.clear();
    }
    
    private void errorProcess(ServerWebExchange exchange, Throwable throwable) {
        ServerHttpRequest request = exchange.getRequest();
        Instant startTime = exchange.getAttribute(REQUEST_START_TIME);
        
        long duration = startTime != null 
            ? Instant.now().toEpochMilli() - startTime.toEpochMilli()
            : 0;
        
        logger.error("Request failed - Method: {}, URI: {}, Duration: {}ms, Error: {}", 
            request.getMethod(),
            request.getURI(),
            duration,
            throwable.getMessage(),
            throwable);
        
        // Clear MDC
        MDC.clear();
    }
    
    private void logRequest(ServerHttpRequest request, String correlationId) {
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeaders().getFirst("User-Agent");
        String authType = request.getHeaders().getFirst("X-Auth-Type");
        String userId = request.getHeaders().getFirst("X-User-ID");
        
        logger.info("Incoming request - Method: {}, URI: {}, IP: {}, User-Agent: {}, Auth: {}, User: {}", 
            request.getMethod(),
            request.getURI(),
            clientIp,
            userAgent,
            authType != null ? authType : "None",
            userId != null ? userId : "Anonymous");
        
        // Log headers (excluding sensitive ones)
        if (logger.isDebugEnabled()) {
            request.getHeaders().forEach((name, values) -> {
                if (!isSensitiveHeader(name)) {
                    logger.debug("Request Header - {}: {}", name, values);
                }
            });
        }
    }
    
    private void logResponse(ServerHttpRequest request, ServerHttpResponse response, long duration) {
        logger.info("Outgoing response - Method: {}, URI: {}, Status: {}, Duration: {}ms",
            request.getMethod(),
            request.getURI(),
            response.getStatusCode(),
            duration);
        
        // Log response headers (excluding sensitive ones)
        if (logger.isDebugEnabled()) {
            response.getHeaders().forEach((name, values) -> {
                if (!isSensitiveHeader(name)) {
                    logger.debug("Response Header - {}: {}", name, values);
                }
            });
        }
        
        // Log slow requests
        if (duration > 5000) { // 5 seconds
            logger.warn("Slow request detected - Method: {}, URI: {}, Duration: {}ms",
                request.getMethod(),
                request.getURI(),
                duration);
        }
    }
    
    private String getClientIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null 
            ? request.getRemoteAddress().getAddress().getHostAddress()
            : "unknown";
    }
    
    private boolean isSensitiveHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.contains("authorization") ||
               lowerName.contains("password") ||
               lowerName.contains("token") ||
               lowerName.contains("secret") ||
               lowerName.contains("key");
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}