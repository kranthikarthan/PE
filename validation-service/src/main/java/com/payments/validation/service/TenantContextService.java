package com.payments.validation.service;

import com.payments.domain.shared.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Tenant Context Service
 *
 * <p>Manages tenant context for multi-tenant operations: - Thread-local tenant context storage -
 * Tenant context validation - Context propagation - Multi-tenant data isolation
 */
@Slf4j
@Service
public class TenantContextService {

  private static final ThreadLocal<TenantContext> tenantContextThreadLocal = new ThreadLocal<>();

  /**
   * Set tenant context for current thread
   *
   * @param tenantId Tenant ID
   * @param businessUnitId Business unit ID
   */
  public void setTenantContext(String tenantId, String businessUnitId) {
    if (tenantId == null || tenantId.trim().isEmpty()) {
      throw new IllegalArgumentException("Tenant ID cannot be null or empty");
    }
    if (businessUnitId == null || businessUnitId.trim().isEmpty()) {
      throw new IllegalArgumentException("Business unit ID cannot be null or empty");
    }

    TenantContext context =
        TenantContext.builder().tenantId(tenantId).businessUnitId(businessUnitId).build();

    tenantContextThreadLocal.set(context);
    log.debug("Set tenant context: tenantId={}, businessUnitId={}", tenantId, businessUnitId);
  }

  /**
   * Set tenant context for current thread
   *
   * @param tenantContext Tenant context
   */
  public void setTenantContext(TenantContext tenantContext) {
    if (tenantContext == null) {
      throw new IllegalArgumentException("Tenant context cannot be null");
    }
    if (tenantContext.getTenantId() == null || tenantContext.getTenantId().trim().isEmpty()) {
      throw new IllegalArgumentException("Tenant ID cannot be null or empty");
    }
    if (tenantContext.getBusinessUnitId() == null
        || tenantContext.getBusinessUnitId().trim().isEmpty()) {
      throw new IllegalArgumentException("Business unit ID cannot be null or empty");
    }

    tenantContextThreadLocal.set(tenantContext);
    log.debug("Set tenant context: {}", tenantContext);
  }

  /**
   * Get current tenant context
   *
   * @return Current tenant context or null if not set
   */
  public TenantContext getCurrentTenantContext() {
    return tenantContextThreadLocal.get();
  }

  /**
   * Get current tenant ID
   *
   * @return Current tenant ID or null if not set
   */
  public String getCurrentTenantId() {
    TenantContext context = tenantContextThreadLocal.get();
    return context != null ? context.getTenantId() : null;
  }

  /**
   * Get current business unit ID
   *
   * @return Current business unit ID or null if not set
   */
  public String getCurrentBusinessUnitId() {
    TenantContext context = tenantContextThreadLocal.get();
    return context != null ? context.getBusinessUnitId() : null;
  }

  /** Clear tenant context for current thread */
  public void clearTenantContext() {
    TenantContext context = tenantContextThreadLocal.get();
    tenantContextThreadLocal.remove();
    log.debug("Cleared tenant context: {}", context);
  }

  /**
   * Check if tenant context is set
   *
   * @return true if tenant context is set
   */
  public boolean hasTenantContext() {
    return tenantContextThreadLocal.get() != null;
  }

  /**
   * Validate tenant context
   *
   * @param tenantId Tenant ID to validate
   * @param businessUnitId Business unit ID to validate
   * @return true if tenant context is valid
   */
  public boolean validateTenantContext(String tenantId, String businessUnitId) {
    if (tenantId == null || tenantId.trim().isEmpty()) {
      log.warn("Invalid tenant ID: {}", tenantId);
      return false;
    }
    if (businessUnitId == null || businessUnitId.trim().isEmpty()) {
      log.warn("Invalid business unit ID: {}", businessUnitId);
      return false;
    }
    return true;
  }

  /**
   * Execute with tenant context
   *
   * @param tenantContext Tenant context
   * @param runnable Runnable to execute
   */
  public void executeWithTenantContext(TenantContext tenantContext, Runnable runnable) {
    TenantContext previousContext = getCurrentTenantContext();
    try {
      setTenantContext(tenantContext);
      runnable.run();
    } finally {
      if (previousContext != null) {
        setTenantContext(previousContext);
      } else {
        clearTenantContext();
      }
    }
  }

  /**
   * Execute with tenant context
   *
   * @param tenantId Tenant ID
   * @param businessUnitId Business unit ID
   * @param runnable Runnable to execute
   */
  public void executeWithTenantContext(String tenantId, String businessUnitId, Runnable runnable) {
    TenantContext previousContext = getCurrentTenantContext();
    try {
      setTenantContext(tenantId, businessUnitId);
      runnable.run();
    } finally {
      if (previousContext != null) {
        setTenantContext(previousContext);
      } else {
        clearTenantContext();
      }
    }
  }
}
