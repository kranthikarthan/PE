package com.payments.swiftadapter.fixtures;

import com.payments.domain.shared.TenantContext;

/**
 * Test data builder for TenantContext
 *
 * <p>Provides a fluent API for creating test instances of TenantContext with sensible defaults.
 *
 * <p>Example usage:
 *
 * <pre>
 * TenantContext context = TenantContextTestDataBuilder.aTenantContext()
 *     .withTenantId("custom-tenant")
 *     .build();
 * </pre>
 */
public class TenantContextTestDataBuilder {

  private String tenantId;
  private String tenantName;
  private String businessUnitId;
  private String businessUnitName;

  private TenantContextTestDataBuilder() {
    this.tenantId = "tenant-test-001";
    this.tenantName = "Test Tenant";
    this.businessUnitId = "business-unit-test-001";
    this.businessUnitName = "Test Business Unit";
  }

  public static TenantContextTestDataBuilder aTenantContext() {
    return new TenantContextTestDataBuilder();
  }

  public TenantContextTestDataBuilder withTenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  public TenantContextTestDataBuilder withTenantName(String tenantName) {
    this.tenantName = tenantName;
    return this;
  }

  public TenantContextTestDataBuilder withBusinessUnitId(String businessUnitId) {
    this.businessUnitId = businessUnitId;
    return this;
  }

  public TenantContextTestDataBuilder withBusinessUnitName(String businessUnitName) {
    this.businessUnitName = businessUnitName;
    return this;
  }

  public TenantContext build() {
    return TenantContext.of(tenantId, tenantName, businessUnitId, businessUnitName);
  }
}
