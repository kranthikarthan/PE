package com.paymentengine.middleware.service;

import com.paymentengine.middleware.entity.TenantConfiguration;
import com.paymentengine.middleware.dto.TenantCloneRequest;
import com.paymentengine.middleware.dto.TenantCloneResponse;
import com.paymentengine.middleware.dto.TenantExportRequest;
import com.paymentengine.middleware.dto.TenantExportResponse;
import com.paymentengine.middleware.dto.TenantImportRequest;
import com.paymentengine.middleware.dto.TenantImportResponse;

import java.util.List;
import java.util.Map;

/**
 * Service interface for tenant cloning and migration operations
 */
public interface TenantCloningService {

    /**
     * Clone a tenant configuration to a new tenant
     */
    TenantCloneResponse cloneTenant(TenantCloneRequest request);

    /**
     * Clone a tenant configuration to a different environment
     */
    TenantCloneResponse cloneTenantToEnvironment(TenantCloneRequest request);

    /**
     * Clone a specific version of a tenant configuration
     */
    TenantCloneResponse cloneTenantVersion(TenantCloneRequest request);

    /**
     * Export tenant configuration for migration
     */
    TenantExportResponse exportTenant(TenantExportRequest request);

    /**
     * Import tenant configuration from export
     */
    TenantImportResponse importTenant(TenantImportRequest request);

    /**
     * Get all available tenants for cloning
     */
    List<String> getAvailableTenants();

    /**
     * Get all versions of a tenant
     */
    List<String> getTenantVersions(String tenantId);

    /**
     * Get tenant configuration history
     */
    List<TenantConfiguration> getTenantHistory(String tenantId);

    /**
     * Compare two tenant configurations
     */
    Map<String, Object> compareTenantConfigurations(String tenantId1, String version1, String tenantId2, String version2);

    /**
     * Validate tenant configuration before cloning
     */
    Map<String, Object> validateTenantConfiguration(String tenantId, String version);

    /**
     * Get cloning statistics
     */
    Map<String, Object> getCloningStatistics();

    /**
     * Rollback tenant configuration to a previous version
     */
    TenantCloneResponse rollbackTenant(String tenantId, String targetVersion);

    /**
     * Create tenant configuration template
     */
    TenantCloneResponse createTemplate(String tenantId, String version, String templateName);

    /**
     * Apply template to tenant
     */
    TenantCloneResponse applyTemplate(String tenantId, String templateName, Map<String, String> overrides);
}