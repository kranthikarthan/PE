package com.payments.iam.repository;

import com.payments.iam.entity.AuditEventEntity;
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
 * AuditEventRepository - Data access for audit logs.
 *
 * <p>Provides compliance audit trail queries with pagination.
 * Retention: 90+ days (managed by archival process).
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
   * Find audit logs by action type.
   *
   * @param tenantId the tenant ID
   * @param action the action type
   * @param pageable pagination info
   * @return page of audit events
   */
  @Query(
      "SELECT a FROM AuditEventEntity a WHERE a.tenantId = :tenantId AND a.action = :action "
          + "ORDER BY a.timestamp DESC")
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
  @Query(
      "SELECT a FROM AuditEventEntity a WHERE a.tenantId = :tenantId "
          + "AND a.result = 'DENIED' ORDER BY a.timestamp DESC")
  Page<AuditEventEntity> findDeniedAccessAttempts(
      @Param("tenantId") UUID tenantId, Pageable pageable);

  /**
   * Find error events (failures).
   *
   * @param tenantId the tenant ID
   * @param pageable pagination info
   * @return page of error audit events
   */
  @Query(
      "SELECT a FROM AuditEventEntity a WHERE a.tenantId = :tenantId "
          + "AND a.result = 'ERROR' ORDER BY a.timestamp DESC")
  Page<AuditEventEntity> findErrorEvents(
      @Param("tenantId") UUID tenantId, Pageable pageable);

  /**
   * Find audit logs within a time range.
   *
   * @param tenantId the tenant ID
   * @param startTime start timestamp
   * @param endTime end timestamp
   * @return list of audit events
   */
  @Query(
      "SELECT a FROM AuditEventEntity a WHERE a.tenantId = :tenantId "
          + "AND a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp DESC")
  List<AuditEventEntity> findByTenantIdAndTimestampBetween(
      @Param("tenantId") UUID tenantId,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  /**
   * Count audit logs by result (for reporting).
   *
   * @param tenantId the tenant ID
   * @param result the result type
   * @return count
   */
  @Query("SELECT COUNT(a) FROM AuditEventEntity a WHERE a.tenantId = :tenantId AND a.result = :result")
  long countByTenantIdAndResult(@Param("tenantId") UUID tenantId, @Param("result") String result);
}
