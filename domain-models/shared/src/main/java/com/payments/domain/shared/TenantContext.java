package com.payments.domain.shared;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

/** TenantContext - Value Object */
@Embeddable
@Value
@Builder
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

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String tenantId;
    private String tenantName;
    private String businessUnitId;
    private String businessUnitName;

    public Builder tenantId(String tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public Builder tenantName(String tenantName) {
      this.tenantName = tenantName;
      return this;
    }

    public Builder businessUnitId(String businessUnitId) {
      this.businessUnitId = businessUnitId;
      return this;
    }

    public Builder businessUnitName(String businessUnitName) {
      this.businessUnitName = businessUnitName;
      return this;
    }

    public TenantContext build() {
      return new TenantContext(tenantId, tenantName, businessUnitId, businessUnitName);
    }
  }
}
