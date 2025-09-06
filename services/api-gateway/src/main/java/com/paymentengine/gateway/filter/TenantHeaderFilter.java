package com.paymentengine.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Gateway filter to ensure tenant context is properly propagated
 */
@Component
public class TenantHeaderFilter extends AbstractGatewayFilterFactory<TenantHeaderFilter.Config> {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantHeaderFilter.class);
    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String DEFAULT_TENANT = "default";
    
    public TenantHeaderFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            var request = exchange.getRequest();
            var response = exchange.getResponse();
            
            // Extract tenant ID from various sources
            String tenantId = extractTenantId(request);
            
            if (tenantId == null || tenantId.trim().isEmpty()) {
                tenantId = DEFAULT_TENANT;
            }
            
            logger.debug("Processing request for tenant: {} - {}", tenantId, request.getPath());
            
            // Add tenant header to downstream request
            var modifiedRequest = request.mutate()
                    .header(TENANT_HEADER, tenantId)
                    .build();
            
            // Add tenant header to response for tracing
            response.getHeaders().add("X-Response-Tenant-ID", tenantId);
            
            var modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();
            
            return chain.filter(modifiedExchange)
                    .doOnSuccess(aVoid -> logger.debug("Request completed for tenant: {}", tenantId))
                    .doOnError(throwable -> logger.error("Request failed for tenant: {} - {}", tenantId, throwable.getMessage()));
        };
    }
    
    private String extractTenantId(org.springframework.http.server.reactive.ServerHttpRequest request) {
        // 1. Check explicit tenant header
        String tenantId = request.getHeaders().getFirst(TENANT_HEADER);
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            return tenantId.trim();
        }
        
        // 2. Check query parameter
        tenantId = request.getQueryParams().getFirst("tenantId");
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            return tenantId.trim();
        }
        
        // 3. Extract from path (e.g., /api/v1/tenants/{tenantId}/...)
        String path = request.getPath().value();
        if (path.contains("/tenants/")) {
            String[] pathParts = path.split("/");
            for (int i = 0; i < pathParts.length - 1; i++) {
                if ("tenants".equals(pathParts[i]) && i + 1 < pathParts.length) {
                    String pathTenantId = pathParts[i + 1];
                    if (isValidTenantId(pathTenantId)) {
                        return pathTenantId;
                    }
                }
            }
        }
        
        // 4. Extract from JWT token (if available)
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String jwtTenantId = extractTenantFromJWT(authorization.substring(7));
            if (jwtTenantId != null) {
                return jwtTenantId;
            }
        }
        
        // 5. Check user ID header (fallback)
        String userId = request.getHeaders().getFirst("X-User-ID");
        if (userId != null) {
            // This would typically lookup the user's tenant from a cache or service
            // For now, return default
            logger.debug("Found user ID {}, using default tenant", userId);
        }
        
        return null;
    }
    
    private String extractTenantFromJWT(String token) {
        try {
            // This would decode the JWT and extract tenant claim
            // For now, return null to use other methods
            // In a real implementation, you'd use a JWT library:
            // Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
            // return claims.get("tenantId", String.class);
            return null;
        } catch (Exception e) {
            logger.debug("Could not extract tenant from JWT: {}", e.getMessage());
            return null;
        }
    }
    
    private boolean isValidTenantId(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            return false;
        }
        
        // Tenant ID should be alphanumeric with hyphens/underscores, max 50 chars
        return tenantId.matches("^[a-zA-Z0-9-_]{1,50}$");
    }
    
    public static class Config {
        private boolean enabled = true;
        private String defaultTenant = DEFAULT_TENANT;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getDefaultTenant() {
            return defaultTenant;
        }
        
        public void setDefaultTenant(String defaultTenant) {
            this.defaultTenant = defaultTenant;
        }
    }
}