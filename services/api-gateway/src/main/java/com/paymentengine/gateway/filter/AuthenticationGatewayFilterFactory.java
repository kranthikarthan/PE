package com.paymentengine.gateway.filter;

import com.paymentengine.gateway.security.JwtAuthenticationManager;
import com.paymentengine.gateway.service.ConfigurationHierarchyService;
import com.paymentengine.gateway.dto.multilevel.ResolvedAuthConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * Custom Gateway Filter for JWT Authentication
 */
@Component
public class AuthenticationGatewayFilterFactory 
    extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationGatewayFilterFactory.class);
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Autowired
    private JwtAuthenticationManager jwtAuthenticationManager;

    @Autowired
    private ConfigurationHierarchyService configurationHierarchyService;
    
    public AuthenticationGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Skip authentication for excluded paths
            String path = request.getURI().getPath();
            if (isExcludedPath(path, config.getExcludedPaths())) {
                return chain.filter(exchange);
            }
            
            // Extract token from Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String token = extractToken(authHeader);
            
            if (token == null) {
                return handleApiKeyAuth(exchange, chain);
            }
            
            // Validate JWT token
            if (!jwtAuthenticationManager.isTokenValid(token)) {
                return handleUnauthorized(exchange, "Invalid or expired token");
            }
            
            // Add user context to request headers
            return addUserContextAndContinue(exchange, chain, token);
        };
    }
    
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }
    
    private boolean isExcludedPath(String path, List<String> excludedPaths) {
        if (excludedPaths == null) {
            return false;
        }
        
        return excludedPaths.stream()
            .anyMatch(excludedPath -> {
                if (excludedPath.endsWith("/**")) {
                    String prefix = excludedPath.substring(0, excludedPath.length() - 3);
                    return path.startsWith(prefix);
                } else if (excludedPath.endsWith("/*")) {
                    String prefix = excludedPath.substring(0, excludedPath.length() - 2);
                    return path.startsWith(prefix) && !path.substring(prefix.length()).contains("/");
                } else {
                    return path.equals(excludedPath);
                }
            });
    }
    
    private Mono<Void> handleApiKeyAuth(ServerWebExchange exchange,
                                       org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String tenantId = request.getHeaders().getFirst("X-Tenant-ID");
        if (tenantId == null || tenantId.isBlank()) {
            return handleUnauthorized(exchange, "Missing tenant identifier for API key authentication");
        }

        String serviceType = Optional.ofNullable(request.getHeaders().getFirst("X-Service-Type"))
                .orElse("api-gateway");
        String endpoint = request.getURI().getPath();
        String paymentType = request.getHeaders().getFirst("X-Payment-Type");

        Optional<ResolvedAuthConfiguration> resolved = configurationHierarchyService.resolveConfigurationPrecedence(
                tenantId, serviceType, endpoint, paymentType);

        if (resolved.isEmpty()) {
            return handleUnauthorized(exchange, "No API key configuration found for tenant");
        }

        ResolvedAuthConfiguration configuration = resolved.get();
        String headerName = Optional.ofNullable(configuration.getApiKeyHeaderName()).orElse("X-API-Key");
        String providedKey = request.getHeaders().getFirst(headerName);

        if (providedKey == null || providedKey.isBlank()) {
            return handleUnauthorized(exchange, "Missing API key header: " + headerName);
        }

        if (configuration.getApiKey() == null || !configuration.getApiKey().equals(providedKey)) {
            logger.warn("API key validation failed for tenant {} on endpoint {}", tenantId, endpoint);
            return handleUnauthorized(exchange, "Invalid API key");
        }

        ServerHttpRequest mutatedRequest = request.mutate()
            .header("X-Auth-Type", "API_KEY")
            .header("X-API-Key-Validated", "true")
            .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
    
    private Mono<Void> addUserContextAndContinue(ServerWebExchange exchange, 
                                                 org.springframework.cloud.gateway.filter.GatewayFilterChain chain, 
                                                 String token) {
        try {
            String userId = jwtAuthenticationManager.extractUserId(token);
            String username = jwtAuthenticationManager.extractUsername(token);
            
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-Auth-Type", "JWT")
                .header("X-User-ID", userId != null ? userId : "")
                .header("X-Username", username != null ? username : "")
                .header("X-Token-Valid", "true")
                .build();
            
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
            
        } catch (Exception e) {
            logger.error("Error processing authentication: {}", e.getMessage());
            return handleUnauthorized(exchange, "Authentication processing error");
        }
    }
    
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        String errorResponse = String.format(
            "{\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"%s\",\"timestamp\":\"%s\"}}",
            message, java.time.Instant.now().toString()
        );
        
        org.springframework.core.io.buffer.DataBuffer buffer = response.bufferFactory()
            .wrap(errorResponse.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("excludedPaths");
    }
    
    public static class Config {
        private List<String> excludedPaths;
        
        public List<String> getExcludedPaths() {
            return excludedPaths;
        }
        
        public void setExcludedPaths(List<String> excludedPaths) {
            this.excludedPaths = excludedPaths;
        }
    }
}