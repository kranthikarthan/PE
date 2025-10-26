package com.payments.paymentinitiation.repository;

import com.payments.paymentinitiation.entity.Pain002AuditLogEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for pain.002 audit log entities */
@Repository
public interface Pain002AuditLogRepository extends JpaRepository<Pain002AuditLogEntity, UUID> {

  /** Find audit logs by pain.002 status report ID and tenant */
  @Query(
      "SELECT a FROM Pain002AuditLogEntity a WHERE a.statusReport.id = :statusReportId AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
  List<Pain002AuditLogEntity> findByStatusReportIdAndTenantId(
      @Param("statusReportId") UUID statusReportId, @Param("tenantId") String tenantId);

  /** Find audit logs by action and tenant */
  @Query(
      "SELECT a FROM Pain002AuditLogEntity a WHERE a.action = :action AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
  List<Pain002AuditLogEntity> findByActionAndTenantId(
      @Param("action") Pain002AuditLogEntity.Action action, @Param("tenantId") String tenantId);

  /** Find audit logs by tenant and business unit */
  @Query(
      "SELECT a FROM Pain002AuditLogEntity a WHERE a.tenantId = :tenantId AND a.businessUnitId = :businessUnitId ORDER BY a.createdAt DESC")
  Page<Pain002AuditLogEntity> findByTenantIdAndBusinessUnitId(
      @Param("tenantId") String tenantId,
      @Param("businessUnitId") String businessUnitId,
      Pageable pageable);

  /** Find audit logs within date range and tenant */
  @Query(
      "SELECT a FROM Pain002AuditLogEntity a WHERE a.tenantId = :tenantId AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
  Page<Pain002AuditLogEntity> findByTenantIdAndDateRange(
      @Param("tenantId") String tenantId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);

  /** Count audit logs by action and tenant */
  @Query(
      "SELECT COUNT(a) FROM Pain002AuditLogEntity a WHERE a.action = :action AND a.tenantId = :tenantId")
  long countByActionAndTenantId(
      @Param("action") Pain002AuditLogEntity.Action action, @Param("tenantId") String tenantId);

  /** Find audit logs by tenant */
  @Query(
      "SELECT a FROM Pain002AuditLogEntity a WHERE a.tenantId = :tenantId ORDER BY a.createdAt DESC")
  Page<Pain002AuditLogEntity> findByTenantId(@Param("tenantId") String tenantId, Pageable pageable);
}
