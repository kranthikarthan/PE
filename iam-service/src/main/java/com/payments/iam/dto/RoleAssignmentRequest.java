package com.payments.iam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RoleAssignmentRequest - Request DTO for POST /api/roles endpoint.
 *
 * <p>Contains: Role name to assign to user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentRequest {

  @NotBlank(message = "Role name is required")
  private String roleName;
}
