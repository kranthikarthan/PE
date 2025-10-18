package com.payments.iam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * TokenValidationResponse - Response from /api/auth/validate endpoint.
 *
 * <p>Contains:
 * - User ID (from JWT 'sub' claim)
 * - Tenant ID (from JWT 'tenant_id' claim)
 * - User's roles in this tenant (from database)
 * - All security authorities (ROLE_*, SCOPE_*, TENANT_*)
 * - Validation status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {

  private String userId;

  private UUID tenantId;

  private List<String> roles;

  private List<String> authorities;

  private boolean valid;
}
