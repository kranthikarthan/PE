package com.paymentengine.shared.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-local tenant context for multi-tenant operations
 */
public class TenantContext {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    private static final String DEFAULT_TENANT = "default";
    
    /**
     * Set the current tenant for the thread
     */
    public static void setCurrentTenant(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            tenantId = DEFAULT_TENANT;
        }
        
        logger.debug("Setting current tenant: {}", tenantId);
        currentTenant.set(tenantId);
    }
    
    /**
     * Get the current tenant for the thread
     */
    public static String getCurrentTenant() {
        String tenant = currentTenant.get();
        if (tenant == null) {
            tenant = DEFAULT_TENANT;
            setCurrentTenant(tenant);
        }
        return tenant;
    }
    
    /**
     * Clear the tenant context
     */
    public static void clear() {
        logger.debug("Clearing tenant context");
        currentTenant.remove();
    }
    
    /**
     * Execute a block of code with a specific tenant context
     */
    public static <T> T executeWithTenant(String tenantId, java.util.function.Supplier<T> operation) {
        String previousTenant = getCurrentTenant();
        try {
            setCurrentTenant(tenantId);
            return operation.get();
        } finally {
            setCurrentTenant(previousTenant);
        }
    }
    
    /**
     * Execute a block of code with a specific tenant context (void return)
     */
    public static void executeWithTenant(String tenantId, Runnable operation) {
        String previousTenant = getCurrentTenant();
        try {
            setCurrentTenant(tenantId);
            operation.run();
        } finally {
            setCurrentTenant(previousTenant);
        }
    }
    
    /**
     * Check if multi-tenancy is enabled
     */
    public static boolean isMultiTenancyEnabled() {
        return !DEFAULT_TENANT.equals(getCurrentTenant());
    }
    
    /**
     * Get tenant-prefixed key for caching or storage
     */
    public static String getTenantKey(String key) {
        return getCurrentTenant() + ":" + key;
    }
    
    /**
     * Get tenant-specific table name (if using schema-per-tenant)
     */
    public static String getTenantTableName(String baseTableName) {
        String tenant = getCurrentTenant();
        if (DEFAULT_TENANT.equals(tenant)) {
            return baseTableName;
        }
        return tenant + "_" + baseTableName;
    }
    
    /**
     * Validate tenant ID format
     */
    public static boolean isValidTenantId(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            return false;
        }
        
        // Tenant ID should be alphanumeric with hyphens, max 50 chars
        return tenantId.matches("^[a-zA-Z0-9-_]{1,50}$");
    }
}