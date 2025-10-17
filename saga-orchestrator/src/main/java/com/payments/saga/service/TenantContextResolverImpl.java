package com.payments.saga.service;

import com.payments.domain.shared.TenantContext;
import com.payments.saga.exception.TenantContextResolutionException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Implementation of TenantContextResolver for resolving tenant context dynamically */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantContextResolverImpl implements TenantContextResolverInterface {

  private final TenantService tenantService;
  private final BusinessUnitService businessUnitService;

  // Cache for resolved tenant contexts to improve performance
  private final Map<String, TenantContext> contextCache = new ConcurrentHashMap<>();

  @Override
  public TenantContext resolve(String tenantId, String businessUnitId) {
    if (tenantId == null || businessUnitId == null) {
      throw new TenantContextResolutionException("Tenant ID and Business Unit ID cannot be null");
    }

    String cacheKey = tenantId + ":" + businessUnitId;

    // Check cache first
    TenantContext cachedContext = contextCache.get(cacheKey);
    if (cachedContext != null) {
      log.debug(
          "Using cached tenant context for tenant: {}, business unit: {}",
          tenantId,
          businessUnitId);
      return cachedContext;
    }

    try {
      // Resolve tenant name
      String tenantName = resolveTenantName(tenantId);

      // Resolve business unit name
      String businessUnitName = resolveBusinessUnitName(tenantId, businessUnitId);

      // Create tenant context
      TenantContext context =
          TenantContext.of(tenantId, tenantName, businessUnitId, businessUnitName);

      // Cache the resolved context
      contextCache.put(cacheKey, context);

      log.debug(
          "Resolved tenant context: tenant={}({}), businessUnit={}({})",
          tenantName,
          tenantId,
          businessUnitName,
          businessUnitId);

      return context;

    } catch (Exception e) {
      log.error(
          "Failed to resolve tenant context for tenant: {}, business unit: {}",
          tenantId,
          businessUnitId,
          e);
      throw new TenantContextResolutionException(
          "Failed to resolve tenant context: " + e.getMessage(), e);
    }
  }

  @Override
  public TenantContext resolveFromPaymentId(String paymentId) {
    if (paymentId == null) {
      throw new TenantContextResolutionException("Payment ID cannot be null");
    }

    try {
      // This would typically involve looking up the payment to get tenant/business unit info
      // For now, we'll use a default resolution strategy
      log.debug("Resolving tenant context from payment ID: {}", paymentId);

      // In a real implementation, you would:
      // 1. Look up the payment entity
      // 2. Extract tenant and business unit information
      // 3. Resolve the context

      // For now, return a default context (this should be replaced with actual logic)
      return resolve("default-tenant", "default-business-unit");

    } catch (Exception e) {
      log.error("Failed to resolve tenant context from payment ID: {}", paymentId, e);
      throw new TenantContextResolutionException(
          "Failed to resolve tenant context from payment ID: " + e.getMessage(), e);
    }
  }

  @Override
  public void clearCache() {
    contextCache.clear();
    log.debug("Cleared tenant context cache");
  }

  @Override
  public void clearCacheForTenant(String tenantId) {
    if (tenantId == null) {
      return;
    }

    contextCache.entrySet().removeIf(entry -> entry.getKey().startsWith(tenantId + ":"));
    log.debug("Cleared tenant context cache for tenant: {}", tenantId);
  }

  private String resolveTenantName(String tenantId) {
    try {
      return tenantService.getTenantName(tenantId);
    } catch (Exception e) {
      log.warn("Failed to resolve tenant name for ID: {}, using ID as name", tenantId, e);
      return tenantId; // Fallback to using ID as name
    }
  }

  private String resolveBusinessUnitName(String tenantId, String businessUnitId) {
    try {
      return businessUnitService.getBusinessUnitName(tenantId, businessUnitId);
    } catch (Exception e) {
      log.warn(
          "Failed to resolve business unit name for tenant: {}, business unit: {}, using ID as name",
          tenantId,
          businessUnitId,
          e);
      return businessUnitId; // Fallback to using ID as name
    }
  }
}
