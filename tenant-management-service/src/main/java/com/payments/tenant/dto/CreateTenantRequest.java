package com.payments.tenant.dto;

import com.payments.tenant.entity.TenantEntity;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create Tenant Request DTO.
 *
 * <p>Used for POST /tenants endpoint. Validated before reaching service layer.
 *
 * <p>Validation:
 * - tenantName: required, 3-200 chars
 * - tenantType: required, valid enum value
 * - contactEmail: required, valid email format
 * - country: optional, ISO 3166-1 alpha-3 format
 * - timezone: optional, valid Java timezone
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTenantRequest {

  @NotBlank(message = "tenantName is required")
  @Size(min = 3, max = 200, message = "tenantName must be between 3 and 200 characters")
  private String tenantName;

  @NotNull(message = "tenantType is required")
  private TenantEntity.TenantType tenantType;

  @NotBlank(message = "contactEmail is required")
  @Email(message = "contactEmail must be a valid email address")
  private String contactEmail;

  @Size(max = 20, message = "contactPhone must not exceed 20 characters")
  private String contactPhone;

  @Size(max = 50, message = "registrationNumber must not exceed 50 characters")
  private String registrationNumber;

  @Size(max = 50, message = "taxNumber must not exceed 50 characters")
  private String taxNumber;

  @Size(max = 200, message = "addressLine1 must not exceed 200 characters")
  private String addressLine1;

  @Size(max = 200, message = "addressLine2 must not exceed 200 characters")
  private String addressLine2;

  @Size(max = 100, message = "city must not exceed 100 characters")
  private String city;

  @Size(max = 100, message = "province must not exceed 100 characters")
  private String province;

  @Size(max = 10, message = "postalCode must not exceed 10 characters")
  private String postalCode;

  @Pattern(regexp = "^[A-Z]{3}$", message = "country must be ISO 3166-1 alpha-3 format (e.g., ZAF)")
  private String country;

  @Size(max = 50, message = "timezone must not exceed 50 characters")
  private String timezone;

  @Size(max = 3, message = "currency must be ISO 4217 code (e.g., ZAR)")
  private String currency;
}
