package com.payments.domain.shared;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

/** TenantContext - Value Object */
@Embeddable
@Value
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class TenantContext {
  String tenantId;
  @Transient String tenantName;
  String businessUnitId;
  @Transient String businessUnitName;

  public static TenantContext of(
      String tenantId, String tenantName, String businessUnitId, String businessUnitName) {
    return new TenantContext(tenantId, tenantName, businessUnitId, businessUnitName);
  }
}
