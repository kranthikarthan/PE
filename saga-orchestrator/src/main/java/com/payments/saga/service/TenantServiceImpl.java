package com.payments.saga.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Implementation of TenantService for managing tenant information */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {

  // In-memory cache for tenant information
  // In a real implementation, this would be backed by a database
  private final Map<String, String> tenantCache = new ConcurrentHashMap<>();

  @Override
  public String getTenantName(String tenantId) {
    if (tenantId == null) {
      throw new IllegalArgumentException("Tenant ID cannot be null");
    }

    // Check cache first
    String tenantName = tenantCache.get(tenantId);
    if (tenantName != null) {
      log.debug("Retrieved tenant name from cache: {} -> {}", tenantId, tenantName);
      return tenantName;
    }

    try {
      // In a real implementation, this would query a database or external service
      // For now, we'll use a simple mapping strategy
      tenantName = resolveTenantNameFromId(tenantId);

      // Cache the result
      tenantCache.put(tenantId, tenantName);

      log.debug("Resolved and cached tenant name: {} -> {}", tenantId, tenantName);
      return tenantName;

    } catch (Exception e) {
      log.error("Failed to resolve tenant name for ID: {}", tenantId, e);
      throw new RuntimeException("Failed to resolve tenant name: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean isTenantActive(String tenantId) {
    if (tenantId == null) {
      return false;
    }

    try {
      // In a real implementation, this would check tenant status in the database
      // For now, we'll assume all tenants are active
      log.debug("Checking if tenant is active: {}", tenantId);
      return true;

    } catch (Exception e) {
      log.error("Failed to check tenant status for ID: {}", tenantId, e);
      return false;
    }
  }

  @Override
  public void clearCache() {
    tenantCache.clear();
    log.debug("Cleared tenant cache");
  }

  @Override
  public void clearCacheForTenant(String tenantId) {
    if (tenantId != null) {
      tenantCache.remove(tenantId);
      log.debug("Cleared cache for tenant: {}", tenantId);
    }
  }

  private String resolveTenantNameFromId(String tenantId) {
    // Simple mapping strategy - in a real implementation, this would query a database
    switch (tenantId.toLowerCase()) {
      case "tenant-1":
        return "Primary Tenant";
      case "tenant-2":
        return "Secondary Tenant";
      case "default-tenant":
        return "Default Tenant";
      default:
        // For unknown tenants, use a formatted version of the ID
        return "Tenant " + tenantId;
    }
  }
}
