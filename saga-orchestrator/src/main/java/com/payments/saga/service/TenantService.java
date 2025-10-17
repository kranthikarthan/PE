package com.payments.saga.service;

/** Service for tenant operations */
public interface TenantService {
  String getTenantName(String tenantId);

  boolean isTenantActive(String tenantId);

  void clearCache();

  void clearCacheForTenant(String tenantId);
}
