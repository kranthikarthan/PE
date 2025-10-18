package com.payments.iam.repository;

import com.payments.iam.entity.UserRoleEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * UserRoleRepository - Data access for User-Role mappings.
 *
 * <p>Supports multi-tenant queries: users can have different roles in different tenants.
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UUID> {

  /**
   * Find all roles for a user in a specific tenant (cached).
   *
   * <p>Cache: "user_roles" with key = user_id:tenant_id
   *
   * @param userId the user ID
   * @param tenantId the tenant ID
   * @return list of user role mappings
   */
  @Cacheable(value = "user_roles", key = "#userId + ':' + #tenantId")
  @Query(
      "SELECT ur FROM UserRoleEntity ur "
          + "JOIN FETCH ur.role "
          + "WHERE ur.userId = :userId AND ur.tenantId = :tenantId")
  List<UserRoleEntity> findByUserIdAndTenantId(
      @Param("userId") String userId, @Param("tenantId") UUID tenantId);

  /**
   * Check if user has a specific role in a tenant (cached).
   *
   * <p>Cache: "user_role_check" with key = user_id:tenant_id:role_name
   *
   * @param userId the user ID
   * @param tenantId the tenant ID
   * @param roleName the role name
   * @return true if user has the role in the tenant
   */
  @Cacheable(value = "user_role_check", key = "#userId + ':' + #tenantId + ':' + #roleName")
  @Query(
      "SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END "
          + "FROM UserRoleEntity ur "
          + "WHERE ur.userId = :userId AND ur.tenantId = :tenantId AND ur.role.name = :roleName")
  boolean existsByUserIdAndTenantIdAndRoleName(
      @Param("userId") String userId,
      @Param("tenantId") UUID tenantId,
      @Param("roleName") String roleName);

  /**
   * Check if user has any of the specified roles in a tenant.
   *
   * @param userId the user ID
   * @param tenantId the tenant ID
   * @param roleNames list of role names
   * @return true if user has any of the roles
   */
  @Query(
      "SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END "
          + "FROM UserRoleEntity ur "
          + "WHERE ur.userId = :userId AND ur.tenantId = :tenantId "
          + "AND ur.role.name IN :roleNames")
  boolean existsByUserIdAndTenantIdAndRoleNameIn(
      @Param("userId") String userId,
      @Param("tenantId") UUID tenantId,
      @Param("roleNames") List<String> roleNames);

  /**
   * Find all users with a specific role in a tenant (admin query).
   *
   * @param roleName the role name
   * @param tenantId the tenant ID
   * @return list of user role mappings
   */
  @Query(
      "SELECT ur FROM UserRoleEntity ur "
          + "WHERE ur.role.name = :roleName AND ur.tenantId = :tenantId")
  List<UserRoleEntity> findByRoleNameAndTenantId(
      @Param("roleName") String roleName, @Param("tenantId") UUID tenantId);

  /**
   * Delete user roles (no caching, direct removal).
   *
   * @param userId the user ID
   * @param tenantId the tenant ID
   */
  void deleteByUserIdAndTenantId(String userId, UUID tenantId);
}
