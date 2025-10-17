package com.payments.saga.service;

import com.payments.domain.shared.TenantContext;

/** Interface for resolving tenant context dynamically */
public interface TenantContextResolverInterface {

  /** Resolve tenant context from tenant ID and business unit ID */
  TenantContext resolve(String tenantId, String businessUnitId);

  /** Resolve tenant context from payment ID */
  TenantContext resolveFromPaymentId(String paymentId);

  /** Clear the tenant context cache */
  void clearCache();

  /** Clear cache for a specific tenant */
  void clearCacheForTenant(String tenantId);
}
