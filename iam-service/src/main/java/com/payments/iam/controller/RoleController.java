package com.payments.iam.controller;

import com.payments.iam.annotation.RoleRequired;
import com.payments.iam.dto.RoleAssignmentRequest;
import com.payments.iam.service.RoleService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * RoleController - Role management endpoints (admin-only).
 *
 * <p>Endpoints:
 * - GET /api/roles/{userId}/{tenantId} - List user roles
 * - POST /api/roles/{userId}/{tenantId} - Assign role
 * - DELETE /api/roles/{userId}/{tenantId}/{roleId} - Revoke role
 *
 * <p>All endpoints require: bank_admin role in the tenant
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Management", description = "RBAC role assignment and revocation (admin-only)")
public class RoleController {

  private final RoleService roleService;

  /**
   * Get all roles for a user in a tenant.
   *
   * @param userId user ID
   * @param tenantId tenant UUID
   * @return list of role names
   */
  @GetMapping("/{userId}/{tenantId}")
  @RoleRequired(roles = {"bank_admin"}, description = "List user roles")
  @Timed(value = "role.list_user_roles", description = "Time to list user roles")
  @Operation(
      summary = "Get user roles",
      description = "Get all roles assigned to a user in a tenant")
  @ApiResponse(
      responseCode = "200",
      description = "List of roles")
  @ApiResponse(responseCode = "403", description = "Insufficient privileges")
  public ResponseEntity<List<String>> getUserRoles(
      @PathVariable String userId,
      @PathVariable UUID tenantId) {
    log.info("Listing roles for user: {} in tenant: {}", userId, tenantId);

    List<String> roles = roleService.getUserRoles(userId, tenantId);

    return ResponseEntity.ok(roles);
  }

  /**
   * Assign a role to a user in a tenant.
   *
   * @param userId user ID
   * @param tenantId tenant UUID
   * @param request role assignment request (contains role name)
   * @return 201 Created
   */
  @PostMapping("/{userId}/{tenantId}")
  @RoleRequired(roles = {"bank_admin"}, description = "Assign role to user")
  @Timed(value = "role.assign_role", description = "Time to assign role")
  @Operation(
      summary = "Assign role to user",
      description = "Assign a role to a user in a specific tenant")
  @ApiResponse(responseCode = "201", description = "Role assigned successfully")
  @ApiResponse(responseCode = "400", description = "Invalid request")
  @ApiResponse(responseCode = "403", description = "Insufficient privileges")
  @ApiResponse(responseCode = "404", description = "Role not found")
  public ResponseEntity<Void> assignRole(
      @PathVariable String userId,
      @PathVariable UUID tenantId,
      @RequestBody RoleAssignmentRequest request) {
    log.info(
        "Assigning role {} to user: {} in tenant: {}",
        request.getRoleName(),
        userId,
        tenantId);

    // Get current user for audit trail
    String assignedBy = getCurrentUserId();

    roleService.assignRole(userId, tenantId, request.getRoleName(), assignedBy);

    log.info("Successfully assigned role to user");

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  /**
   * Revoke a role from a user in a tenant.
   *
   * @param userId user ID
   * @param tenantId tenant UUID
   * @param roleId role ID (UUID)
   * @return 204 No Content
   */
  @DeleteMapping("/{userId}/{tenantId}/{roleId}")
  @RoleRequired(roles = {"bank_admin"}, description = "Revoke role from user")
  @Timed(value = "role.revoke_role", description = "Time to revoke role")
  @Operation(
      summary = "Revoke role from user",
      description = "Revoke a role from a user in a specific tenant")
  @ApiResponse(responseCode = "204", description = "Role revoked successfully")
  @ApiResponse(responseCode = "403", description = "Insufficient privileges")
  @ApiResponse(responseCode = "404", description = "Role not found")
  public ResponseEntity<Void> revokeRole(
      @PathVariable String userId,
      @PathVariable UUID tenantId,
      @PathVariable UUID roleId) {
    log.info(
        "Revoking role {} from user: {} in tenant: {}",
        roleId,
        userId,
        tenantId);

    // Get current user for audit trail
    String revokedBy = getCurrentUserId();

    // Get role name from ID (for business logic)
    String roleName = roleService.getRole(roleId.toString())
        .map(role -> role.getName())
        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

    roleService.revokeRole(userId, tenantId, roleName, revokedBy);

    log.info("Successfully revoked role from user");

    return ResponseEntity.noContent().build();
  }

  /**
   * Extract current user ID from SecurityContext.
   *
   * @return current user ID
   */
  private String getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null ? authentication.getName() : "SYSTEM";
  }
}
