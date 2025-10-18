package com.payments.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update Tenant Request DTO.
 *
 * <p>Used for PUT /tenants/{id} endpoint. All fields are optional.
 *
 * <p>Protected fields (cannot be updated):
 * - tenantId (cannot be changed)
 * - status (use activate/suspend/deactivate endpoints)
 * - tenantType (cannot be changed)
 * - registrationNumber (cannot be changed)
 * - taxNumber (cannot be changed)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTenantRequest {

  @Email(message = "contactEmail must be a valid email address")
  private String contactEmail;

  @Size(max = 20, message = "contactPhone must not exceed 20 characters")
  private String contactPhone;

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

  @Size(max = 50, message = "timezone must not exceed 50 characters")
  private String timezone;

  @Size(max = 3, message = "currency must be ISO 4217 code (e.g., ZAR)")
  private String currency;
}
