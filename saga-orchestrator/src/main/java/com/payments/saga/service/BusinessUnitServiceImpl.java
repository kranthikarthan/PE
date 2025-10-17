package com.payments.saga.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Implementation of BusinessUnitService for managing business unit information */
@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessUnitServiceImpl implements BusinessUnitService {

  // In-memory cache for business unit information
  // In a real implementation, this would be backed by a database
  private final Map<String, String> businessUnitCache = new ConcurrentHashMap<>();

  @Override
  public String getBusinessUnitName(String tenantId, String businessUnitId) {
    if (tenantId == null || businessUnitId == null) {
      throw new IllegalArgumentException("Tenant ID and Business Unit ID cannot be null");
    }

    String cacheKey = tenantId + ":" + businessUnitId;

    // Check cache first
    String businessUnitName = businessUnitCache.get(cacheKey);
    if (businessUnitName != null) {
      log.debug("Retrieved business unit name from cache: {} -> {}", cacheKey, businessUnitName);
      return businessUnitName;
    }

    try {
      // In a real implementation, this would query a database or external service
      // For now, we'll use a simple mapping strategy
      businessUnitName = resolveBusinessUnitNameFromIds(tenantId, businessUnitId);

      // Cache the result
      businessUnitCache.put(cacheKey, businessUnitName);

      log.debug("Resolved and cached business unit name: {} -> {}", cacheKey, businessUnitName);
      return businessUnitName;

    } catch (Exception e) {
      log.error(
          "Failed to resolve business unit name for tenant: {}, business unit: {}",
          tenantId,
          businessUnitId,
          e);
      throw new RuntimeException("Failed to resolve business unit name: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean isBusinessUnitActive(String tenantId, String businessUnitId) {
    if (tenantId == null || businessUnitId == null) {
      return false;
    }

    try {
      // In a real implementation, this would check business unit status in the database
      // For now, we'll assume all business units are active
      log.debug(
          "Checking if business unit is active: tenant={}, businessUnit={}",
          tenantId,
          businessUnitId);
      return true;

    } catch (Exception e) {
      log.error(
          "Failed to check business unit status for tenant: {}, business unit: {}",
          tenantId,
          businessUnitId,
          e);
      return false;
    }
  }

  @Override
  public void clearCache() {
    businessUnitCache.clear();
    log.debug("Cleared business unit cache");
  }

  @Override
  public void clearCacheForTenant(String tenantId) {
    if (tenantId != null) {
      businessUnitCache.entrySet().removeIf(entry -> entry.getKey().startsWith(tenantId + ":"));
      log.debug("Cleared cache for tenant: {}", tenantId);
    }
  }

  private String resolveBusinessUnitNameFromIds(String tenantId, String businessUnitId) {
    // Simple mapping strategy - in a real implementation, this would query a database
    String baseName;

    switch (businessUnitId.toLowerCase()) {
      case "bu-1":
        baseName = "Primary Business Unit";
        break;
      case "bu-2":
        baseName = "Secondary Business Unit";
        break;
      case "default-business-unit":
        baseName = "Default Business Unit";
        break;
      default:
        // For unknown business units, use a formatted version of the ID
        baseName = "Business Unit " + businessUnitId;
        break;
    }

    // Add tenant context to the name for better identification
    return baseName + " (" + tenantId + ")";
  }
}
