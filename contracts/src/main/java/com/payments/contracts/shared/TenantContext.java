package com.payments.contracts.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tenant Context Value Object
 *
 * <p>Represents tenant and business unit context: - Tenant identification - Business unit
 * identification - Context metadata - Immutable value object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tenant context value object")
public class TenantContext {

  @NotBlank(message = "Tenant ID is required")
  @Schema(description = "Tenant identifier", required = true, example = "tenant-001")
  private String tenantId;

  @Schema(description = "Tenant name", example = "Acme Corporation")
  private String tenantName;

  @NotBlank(message = "Business unit ID is required")
  @Schema(description = "Business unit identifier", required = true, example = "bu-001")
  private String businessUnitId;

  @Schema(description = "Business unit name", example = "Retail Banking")
  private String businessUnitName;

  @Schema(description = "Additional context metadata")
  private java.util.Map<String, String> metadata;

  /**
   * Create a simple tenant context with just IDs
   *
   * @param tenantId Tenant identifier
   * @param businessUnitId Business unit identifier
   * @return TenantContext instance
   */
  public static TenantContext of(String tenantId, String businessUnitId) {
    return TenantContext.builder().tenantId(tenantId).businessUnitId(businessUnitId).build();
  }

  /**
   * Create a full tenant context with names
   *
   * @param tenantId Tenant identifier
   * @param tenantName Tenant name
   * @param businessUnitId Business unit identifier
   * @param businessUnitName Business unit name
   * @return TenantContext instance
   */
  public static TenantContext of(
      String tenantId, String tenantName, String businessUnitId, String businessUnitName) {
    return TenantContext.builder()
        .tenantId(tenantId)
        .tenantName(tenantName)
        .businessUnitId(businessUnitId)
        .businessUnitName(businessUnitName)
        .build();
  }
}
