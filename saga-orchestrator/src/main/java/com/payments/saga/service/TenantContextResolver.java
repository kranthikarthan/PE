package com.payments.saga.service;

import com.payments.domain.shared.TenantContext;
import com.payments.saga.exception.TenantContextResolutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service for resolving tenant context from IDs */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantContextResolver {

  private final TenantService tenantService;
  private final BusinessUnitService businessUnitService;

  public TenantContext resolve(String tenantId, String businessUnitId, String correlationId) {
    try {
      // Resolve tenant name
      String tenantName = resolveTenantName(tenantId);

      // Resolve business unit name
      String businessUnitName = resolveBusinessUnitName(businessUnitId);

      return TenantContext.of(tenantId, tenantName, businessUnitId, businessUnitName);

    } catch (Exception e) {
      log.error(
          "Failed to resolve tenant context for tenant {} and business unit {}",
          tenantId,
          businessUnitId,
          e);
      throw new TenantContextResolutionException("Failed to resolve tenant context", e);
    }
  }

  private String resolveTenantName(String tenantId) {
    if (tenantId == null || tenantId.equals("default-tenant")) {
      return "Default Tenant";
    }

    try {
      return tenantService.getTenantName(tenantId);
    } catch (Exception e) {
      log.warn("Failed to resolve tenant name for {}", tenantId, e);
      return "Unknown Tenant";
    }
  }

  private String resolveBusinessUnitName(String businessUnitId) {
    if (businessUnitId == null || businessUnitId.equals("default-bu")) {
      return "Default Business Unit";
    }

    try {
      return businessUnitService.getBusinessUnitName(tenantId, businessUnitId);
    } catch (Exception e) {
      log.warn("Failed to resolve business unit name for {}", businessUnitId, e);
      return "Unknown Business Unit";
    }
  }
}
