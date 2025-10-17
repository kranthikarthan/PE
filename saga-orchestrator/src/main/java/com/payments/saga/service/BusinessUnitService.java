package com.payments.saga.service;

/** Service for business unit operations */
public interface BusinessUnitService {
  String getBusinessUnitName(String tenantId, String businessUnitId);

  boolean isBusinessUnitActive(String tenantId, String businessUnitId);

  void clearCache();

  void clearCacheForTenant(String tenantId);
}
