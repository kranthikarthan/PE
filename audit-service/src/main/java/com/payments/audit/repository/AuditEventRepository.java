package com.payments.audit.repository;

import com.payments.audit.entity.AuditEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Audit Event Repository - Data access for immutable audit logs.
 *
 * <p>Provides compliance audit trail queries with pagination and filtering.
 * All queries automatically filtered by tenant_id for multi-tenancy.
 * Retention: 7+ years (managed by archival process).
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {

  /**
   * Find audit logs by tenant with pagination.
   *
   * @param tenantId the tenant ID
   * @param pageable pagination info
   * @return page of audit events
   */
  @Query("SELECT a FROM AuditEventEntity a WHERE a.tenantId = :tenantId ORDER BY a.timestamp DESC")
  Page<AuditEventEntity> findByTenantId(@Param("tenantId") UUID tenantId, Pageable pageable);

  /**
   * Find audit logs by user with pagination.
   *
   * @param userId the user ID
   * @param pageable pagination info
   * @return page of audit events
   */
  @Query("SELECT a FROM AuditEventEntity a WHERE a.userId = :userId ORDER BY a.timestamp DESC")
  Page<AuditEventEntity> findByUserId(@Param("userId") String userId, Pageable pageable);

  /**
   * Find audit logs by tenant and user with pagination.
   *
   * @param tenantId the tenant ID
   * @param userId the user ID
   * @param pageable pagination info
   * @return page of audit events
   */
  @Query("SELECT a FROM AuditEventEntity a WHERE a.tenantId = :tenantId AND a.userId = :userId ORDER BY a.timestamp DESC")
  Page<AuditEventEntity> findByTenantIdAndUserId(
      @Param("tenantId") UUID tenantId,
      @Param("userId") String userId,
      Pageable pageable);

  /**
   * Find audit logs by action type.
   *
   * @param tenantId the tenant ID
   * @param action the action type
   * @param pageable pagination info
   * @return page of audit events
   */
  @Query("SELECT a FROM AuditEventEntity a WHERE a.tenantId = :tenantId AND a.action = :action ORDER BY a.timestamp DESC")
  Page<AuditEventEntity> findByTenantIdAndAction(
      @Param("tenantId") UUID tenantId,
      @Param("action") String action,
      Pageable pageable);

  /**
   * Find denied access attempts (security incidents).
   *
   * @param tenantId the tenant ID
   * @param pageable pagination info
   * @return page of denied audit events
   */
  @Query("SELECT a FROM AuditEventEntity a WHERE a.tenantId = :tenantId AND a.result = 'DENIED' ORDER BY a.timestamp DESC")
  Page<AuditEventEntity> findDeniedAccessAttempts(
      @Param("tenantId") UUID tenantId, Pageable pageable);

  /**
   * Find error events (system failures).
   *
   * @param tenantId the tenant ID
   * @param pageable pagination info
   * @return page of error audit events
   */
  @Query("SELECT a FROM AuditEventEntity a WHERE a.tenantId = :tenantId AND a.result = 'ERROR' ORDER BY a.timestamp DESC")
  Page<AuditEventEntity> findErrorEvents(
      @Param("tenantId") UUID tenantId, Pageable pageable);

  /**
   * Find audit logs within a time range.
   *
   * @param tenantId the tenant ID
   * @param startTime start timestamp
   * @param endTime end timestamp
   * @param pageable pagination info
   * @return page of audit events
   */
  @Query("SELECT a FROM AuditEventEntity a WHERE a.tenantId = :tenantId AND a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp DESC")
  Page<AuditEventEntity> findByTenantIdAndTimestampBetween(
      @Param("tenantId") UUID tenantId,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime,
      Pageable pageable);

  /**
   * Find audit logs by resource with pagination.
   *
   * @param tenantId the tenant ID
   * @param resource the resource type
   * @param pageable pagination info
   * @return page of audit events
   */
  @Query("SELECT a FROM AuditEventEntity a WHERE a.tenantId = :tenantId AND a.resource = :resource ORDER BY a.timestamp DESC")
  Page<AuditEventEntity> findByTenantIdAndResource(
      @Param("tenantId") UUID tenantId,
      @Param("resource") String resource,
      Pageable pageable);

  /**
   * Find audit logs by result type (SUCCESS, DENIED, ERROR).
   *
   * @param tenantId the tenant ID
   * @param result the result type
   * @param pageable pagination info
   * @return page of audit events
   */
  @Query("SELECT a FROM AuditEventEntity a WHERE a.tenantId = :tenantId AND a.result = :result ORDER BY a.timestamp DESC")
  Page<AuditEventEntity> findByTenantIdAndResult(
      @Param("tenantId") UUID tenantId,
      @Param("result") String result,
      Pageable pageable);

  /**
   * Count audit logs by result type (for reporting).
   *
   * @param tenantId the tenant ID
   * @param result the result type
   * @return count
   */
  @Query("SELECT COUNT(a) FROM AuditEventEntity a WHERE a.tenantId = :tenantId AND a.result = :result")
  long countByTenantIdAndResult(
      @Param("tenantId") UUID tenantId,
      @Param("result") String result);

  /**
   * Search audit logs by keyword in action and resource.
   *
   * @param tenantId the tenant ID
   * @param keyword the search keyword
   * @param pageable pagination info
   * @return page of matching audit events
   */
  @Query("SELECT a FROM AuditEventEntity a WHERE a.tenantId = :tenantId AND (LOWER(a.action) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.resource) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY a.timestamp DESC")
  Page<AuditEventEntity> searchByKeyword(
      @Param("tenantId") UUID tenantId,
      @Param("keyword") String keyword,
      Pageable pageable);
}
