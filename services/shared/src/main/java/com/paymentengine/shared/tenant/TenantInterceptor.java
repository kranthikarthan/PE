package com.paymentengine.shared.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to set tenant context from HTTP headers
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantInterceptor.class);
    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String TENANT_QUERY_PARAM = "tenantId";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = extractTenantId(request);
        
        if (tenantId != null && TenantContext.isValidTenantId(tenantId)) {
            TenantContext.setCurrentTenant(tenantId);
            logger.debug("Set tenant context: {} for request: {}", tenantId, request.getRequestURI());
        } else {
            TenantContext.setCurrentTenant("default");
            logger.debug("Using default tenant for request: {}", request.getRequestURI());
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Clear tenant context after request completion
        TenantContext.clear();
    }
    
    private String extractTenantId(HttpServletRequest request) {
        // Try header first
        String tenantId = request.getHeader(TENANT_HEADER);
        
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            return tenantId.trim();
        }
        
        // Try query parameter
        tenantId = request.getParameter(TENANT_QUERY_PARAM);
        
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            return tenantId.trim();
        }
        
        // Try to extract from path (e.g., /api/v1/tenants/{tenantId}/...)
        String path = request.getRequestURI();
        if (path.contains("/tenants/")) {
            String[] pathParts = path.split("/");
            for (int i = 0; i < pathParts.length - 1; i++) {
                if ("tenants".equals(pathParts[i]) && i + 1 < pathParts.length) {
                    return pathParts[i + 1];
                }
            }
        }
        
        // Try to extract from JWT token (if available)
        String jwtTenantId = extractTenantFromJWT(request);
        if (jwtTenantId != null) {
            return jwtTenantId;
        }
        
        return null;
    }
    
    private String extractTenantFromJWT(HttpServletRequest request) {
        // Extract tenant from JWT token claims
        String userId = request.getHeader("X-User-ID");
        
        if (userId != null) {
            // This would typically lookup the user's tenant from database or JWT claims
            // For now, return default
            return "default";
        }
        
        return null;
    }
}