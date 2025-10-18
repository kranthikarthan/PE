package com.payments.iam.service;

import com.payments.iam.entity.RoleEntity;
import com.payments.iam.entity.UserRoleEntity;
import com.payments.iam.repository.RoleRepository;
import com.payments.iam.repository.UserRoleRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * RoleService - Business logic for RBAC (Role-Based Access Control).
 *
 * <p>Responsibilities:
 * - Assign roles to users within tenants
 * - Revoke roles from users
 * - Check if user has a specific role
 * - List all roles for a user
 * - Cache management for performance
 *
 * <p>All operations are multi-tenant aware:
 * - Same user can have different roles in different tenants
 * - Role assignments are scoped to tenant_id
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;
  private final AuditService auditService;

  /**
   * Assign a role to a user within a tenant.
   *
   * <p>Operations:
   * 1. Validate role exists
   * 2. Check if assignment already exists (idempotent)
   * 3. Create UserRole mapping
   * 4. Evict cache for this user-tenant combination
   * 5. Log audit trail
   *
   * @param userId user ID from JWT (e.g., user@example.com)
   * @param tenantId tenant UUID
   * @param roleName role name (e.g., "bank_admin")
   * @param assignedBy user performing the assignment (for audit)
   * @throws IllegalArgumentException if role doesn't exist
   */
  @Transactional
  @CacheEvict(
      value = {"user_roles", "user_role_check"},
      allEntries = false,
      key = "#userId + ':' + #tenantId")
  @Timed(value = "role.assign", description = "Time to assign role to user")
  public void assignRole(String userId, UUID tenantId, String roleName, String assignedBy) {
    log.info(
        "Assigning role {} to user {} in tenant {}",
        roleName,
        userId,
        tenantId);

    // Validate role exists
    RoleEntity role = roleRepository
        .findByName(roleName)
        .orElseThrow(
            () -> {
              log.error("Role not found: {}", roleName);
              return new IllegalArgumentException("Role not found: " + roleName);
            });

    // Check if role already assigned (idempotent)
    if (userRoleRepository.existsByUserIdAndTenantIdAndRoleName(userId, tenantId, roleName)) {
      log.warn("Role {} already assigned to user {} in tenant {}", roleName, userId, tenantId);
      return; // Idempotent: no-op if already assigned
    }

    // Create user-role mapping
    UserRoleEntity userRole = UserRoleEntity.builder()
        .userId(userId)
        .tenantId(tenantId)
        .role(role)
        .createdBy(assignedBy)
        .build();

    userRoleRepository.save(userRole);
    log.info("Successfully assigned role {} to user {}", roleName, userId);

    // Log audit trail
    auditService.logSuccess(
        tenantId,
        userId,
        "ROLE_ASSIGNED",
        "user_role",
        null,
        String.format("Role '%s' assigned to user '%s'", roleName, userId));
  }

  /**
   * Revoke a role from a user within a tenant.
   *
   * <p>Operations:
   * 1. Find the user-role mapping
   * 2. Delete the mapping
   * 3. Evict cache
   * 4. Log audit trail
   *
   * @param userId user ID
   * @param tenantId tenant UUID
   * @param roleName role name
   * @param revokedBy user performing the revocation (for audit)
   */
  @Transactional
  @CacheEvict(
      value = {"user_roles", "user_role_check"},
      allEntries = false,
      key = "#userId + ':' + #tenantId")
  @Timed(value = "role.revoke", description = "Time to revoke role from user")
  public void revokeRole(String userId, UUID tenantId, String roleName, String revokedBy) {
    log.info(
        "Revoking role {} from user {} in tenant {}",
        roleName,
        userId,
        tenantId);

    RoleEntity role = roleRepository
        .findByName(roleName)
        .orElseThrow(
            () -> new IllegalArgumentException("Role not found: " + roleName));

    // Find and delete user-role mapping
    List<UserRoleEntity> userRoles = userRoleRepository.findByUserIdAndTenantId(userId, tenantId);
    long deleted = userRoles.stream()
        .filter(ur -> ur.getRole().getId().equals(role.getId()))
        .peek(ur -> {
          userRoleRepository.delete(ur);
          log.info("Deleted user-role mapping for {} with role {}", userId, roleName);
        })
        .count();

    if (deleted == 0) {
      log.warn("Role {} not found for user {} in tenant {}", roleName, userId, tenantId);
    } else {
      log.info("Successfully revoked role {} from user {}", roleName, userId);

      // Log audit trail
      auditService.logSuccess(
          tenantId,
          userId,
          "ROLE_REVOKED",
          "user_role",
          null,
          String.format("Role '%s' revoked from user '%s'", roleName, userId));
    }
  }

  /**
   * Get all roles for a user in a tenant (cached).
   *
   * @param userId user ID
   * @param tenantId tenant UUID
   * @return list of role names
   */
  @Cacheable(value = "user_roles", key = "#userId + ':' + #tenantId")
  @Timed(value = "role.list_user", description = "Time to list user roles")
  public List<String> getUserRoles(String userId, UUID tenantId) {
    log.debug("Fetching roles for user {} in tenant {}", userId, tenantId);

    return userRoleRepository
        .findByUserIdAndTenantId(userId, tenantId)
        .stream()
        .map(ur -> ur.getRole().getName())
        .collect(Collectors.toList());
  }

  /**
   * Check if user has a specific role in a tenant (cached).
   *
   * <p>Fast path: O(1) Redis lookup
   *
   * @param userId user ID
   * @param tenantId tenant UUID
   * @param roleName role name
   * @return true if user has the role
   */
  @Cacheable(value = "user_role_check", key = "#userId + ':' + #tenantId + ':' + #roleName")
  @Timed(value = "role.has_role", description = "Time to check if user has role")
  public boolean hasRole(String userId, UUID tenantId, String roleName) {
    return userRoleRepository.existsByUserIdAndTenantIdAndRoleName(userId, tenantId, roleName);
  }

  /**
   * Check if user has ANY of the specified roles in a tenant.
   *
   * <p>Useful for multi-role authorization (e.g., "admin OR manager")
   *
   * @param userId user ID
   * @param tenantId tenant UUID
   * @param roleNames list of role names
   * @return true if user has any of the roles
   */
  @Timed(value = "role.has_any_role", description = "Time to check if user has any role")
  public boolean hasAnyRole(String userId, UUID tenantId, List<String> roleNames) {
    return userRoleRepository.existsByUserIdAndTenantIdAndRoleNameIn(
        userId,
        tenantId,
        roleNames);
  }

  /**
   * Get all users with a specific role in a tenant (admin query).
   *
   * <p>Used for: listing admins, finding backup operators, etc.
   *
   * @param roleName role name
   * @param tenantId tenant UUID
   * @return list of user IDs with the role
   */
  @Timed(value = "role.list_role_members", description = "Time to list users with role")
  public List<String> getUsersWithRole(String roleName, UUID tenantId) {
    log.debug("Fetching users with role {} in tenant {}", roleName, tenantId);

    return userRoleRepository
        .findByRoleNameAndTenantId(roleName, tenantId)
        .stream()
        .map(UserRoleEntity::getUserId)
        .collect(Collectors.toList());
  }

  /**
   * Remove all roles from a user in a tenant.
   *
   * <p>Useful for: user deprovisioning, org changes, access revocation
   *
   * @param userId user ID
   * @param tenantId tenant UUID
   * @param removedBy user performing the removal (for audit)
   */
  @Transactional
  @CacheEvict(
      value = {"user_roles", "user_role_check"},
      allEntries = false,
      key = "#userId + ':' + #tenantId")
  @Timed(value = "role.remove_all", description = "Time to remove all roles from user")
  public void removeAllRoles(String userId, UUID tenantId, String removedBy) {
    log.info("Removing all roles from user {} in tenant {}", userId, tenantId);

    List<UserRoleEntity> userRoles = userRoleRepository.findByUserIdAndTenantId(userId, tenantId);
    long count = userRoles.size();

    userRoleRepository.deleteByUserIdAndTenantId(userId, tenantId);
    log.info("Removed {} roles from user {}", count, userId);

    // Log audit trail
    auditService.logSuccess(
        tenantId,
        userId,
        "ALL_ROLES_REMOVED",
        "user_role",
        null,
        String.format("All %d roles removed from user '%s'", count, userId));
  }

  /**
   * Get a specific role by name.
   *
   * @param roleName role name
   * @return Optional containing the role if found
   */
  public Optional<RoleEntity> getRole(String roleName) {
    return roleRepository.findByName(roleName);
  }

  /**
   * Initialize system roles (called once at startup).
   *
   * <p>Creates 5 pre-defined roles if they don't exist
   */
  @Transactional
  public void initializeSystemRoles() {
    log.info("Initializing system roles");

    for (RoleEntity.RoleName roleName : RoleEntity.RoleName.values()) {
      if (!roleRepository.existsByName(roleName.getValue())) {
        RoleEntity role = RoleEntity.builder()
            .name(roleName.getValue())
            .description(roleName.getDescription())
            .build();

        roleRepository.save(role);
        log.info("Created role: {} - {}", roleName.getValue(), roleName.getDescription());
      }
    }

    log.info("System roles initialization complete");
  }
}
