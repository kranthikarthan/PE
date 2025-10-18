package com.payments.tenant.repository;

import com.payments.tenant.entity.TenantEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Tenant Repository - Provides database access for tenant entities.
 *
 * <p>All queries are automatically scoped to the current tenant via Row-Level Security (RLS).
 * Results are cached in Redis for performance (10-minute TTL).
 *
 * <p>Performance: O(1) lookups via primary key, O(log N) for searches with indexes.
 */
@Repository
public interface TenantRepository extends JpaRepository<TenantEntity, String> {

  /**
   * Find tenant by ID with Redis caching.
   *
   * <p>Cache Key: `tenant:{tenantId}`  
   * Cache TTL: 10 minutes  
   * Performance: O(1) with cache hit, O(log N) on cache miss
   *
   * @param tenantId Tenant identifier
   * @return Tenant if found
   */
  @Override
  @Cacheable(value = "tenants", key = "#tenantId")
  Optional<TenantEntity> findById(String tenantId);

  /**
   * Find tenant by name (for lookup by human-readable name).
   *
   * <p>Performance: O(log N) with index on tenant_name
   *
   * @param tenantName Tenant name to search
   * @return Tenant if found
   */
  @Query("SELECT t FROM TenantEntity t WHERE LOWER(t.tenantName) = LOWER(:tenantName)")
  @Cacheable(value = "tenants_by_name", key = "#tenantName")
  Optional<TenantEntity> findByTenantNameIgnoreCase(@Param("tenantName") String tenantName);

  /**
   * Find tenant by email (for customer service lookups).
   *
   * <p>Performance: O(log N) with index on contact_email
   *
   * @param email Contact email to search
   * @return Tenant if found
   */
  @Query("SELECT t FROM TenantEntity t WHERE t.contactEmail = :email")
  @Cacheable(value = "tenants_by_email", key = "#email")
  Optional<TenantEntity> findByContactEmail(@Param("email") String email);

  /**
   * Find all active tenants with pagination.
   *
   * <p>Performance: O(log N) with index on status and created_at
   *
   * @param status Tenant status to filter by
   * @param pageable Pagination info
   * @return Page of tenants
   */
  @Query("SELECT t FROM TenantEntity t WHERE t.status = :status ORDER BY t.createdAt DESC")
  Page<TenantEntity> findByStatus(
      @Param("status") TenantEntity.TenantStatus status, Pageable pageable);

  /**
   * Find all tenants by type (for analytics/grouping).
   *
   * <p>Performance: O(log N) with index on tenant_type
   *
   * @param type Tenant type to filter by
   * @param pageable Pagination info
   * @return Page of tenants
   */
  @Query("SELECT t FROM TenantEntity t WHERE t.tenantType = :type ORDER BY t.createdAt DESC")
  Page<TenantEntity> findByTenantType(
      @Param("type") TenantEntity.TenantType type, Pageable pageable);

  /**
   * Find all active tenants (status = ACTIVE).
   *
   * <p>Performance: O(log N) with index on status
   *
   * @param pageable Pagination info
   * @return Page of active tenants
   */
  @Query("SELECT t FROM TenantEntity t WHERE t.status = 'ACTIVE' ORDER BY t.createdAt DESC")
  Page<TenantEntity> findAllActive(Pageable pageable);

  /**
   * Count active tenants (for dashboard/metrics).
   *
   * <p>Performance: O(log N) with index on status
   *
   * @return Count of active tenants
   */
  @Query("SELECT COUNT(t) FROM TenantEntity t WHERE t.status = 'ACTIVE'")
  long countActive();

  /**
   * Find tenants pending approval (admin view).
   *
   * <p>Performance: O(log N) with index on status
   *
   * @param pageable Pagination info
   * @return Page of tenants pending approval
   */
  @Query(
      "SELECT t FROM TenantEntity t WHERE t.status = 'PENDING_APPROVAL' ORDER BY t.createdAt ASC")
  Page<TenantEntity> findPendingApproval(Pageable pageable);

  /**
   * Check if tenant exists (for validation).
   *
   * <p>Performance: O(1) with primary key
   *
   * @param tenantId Tenant identifier
   * @return true if tenant exists
   */
  @Override
  boolean existsById(String tenantId);

  /**
   * Find tenants by country (for regional queries).
   *
   * <p>Performance: O(log N) with index on country
   *
   * @param country Country code (ISO 3166-1 alpha-3)
   * @param pageable Pagination info
   * @return Page of tenants
   */
  @Query("SELECT t FROM TenantEntity t WHERE t.country = :country ORDER BY t.createdAt DESC")
  Page<TenantEntity> findByCountry(@Param("country") String country, Pageable pageable);

  /**
   * Find suspended tenants (for operational review).
   *
   * <p>Performance: O(log N) with index on status
   *
   * @param pageable Pagination info
   * @return Page of suspended tenants
   */
  @Query("SELECT t FROM TenantEntity t WHERE t.status = 'SUSPENDED' ORDER BY t.updatedAt DESC")
  Page<TenantEntity> findSuspended(Pageable pageable);

  /**
   * Find recently created tenants (for onboarding tracking).
   *
   * <p>Performance: O(log N) with index on created_at
   *
   * @param limit Maximum number to return
   * @return List of recently created tenants
   */
  @Query(
      value =
          "SELECT * FROM tenants ORDER BY created_at DESC LIMIT :limit",
      nativeQuery = true)
  List<TenantEntity> findRecentlyCreated(@Param("limit") int limit);
}
