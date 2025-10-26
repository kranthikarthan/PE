package com.payments.paymentinitiation.repository;

import com.payments.paymentinitiation.entity.Pain001AuditLogEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for pain.001 audit log entities */
@Repository
public interface Pain001AuditLogRepository extends JpaRepository<Pain001AuditLogEntity, UUID> {

  /** Find audit logs by pain.001 message ID and tenant */
  @Query(
      "SELECT a FROM Pain001AuditLogEntity a WHERE a.pain001Message.id = :pain001MessageId AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
  List<Pain001AuditLogEntity> findByPain001MessageIdAndTenantId(
      @Param("pain001MessageId") UUID pain001MessageId, @Param("tenantId") String tenantId);

  /** Find audit logs by action and tenant */
  @Query(
      "SELECT a FROM Pain001AuditLogEntity a WHERE a.action = :action AND a.tenantId = :tenantId ORDER BY a.createdAt DESC")
  List<Pain001AuditLogEntity> findByActionAndTenantId(
      @Param("action") Pain001AuditLogEntity.Action action, @Param("tenantId") String tenantId);

  /** Find audit logs by tenant and business unit */
  @Query(
      "SELECT a FROM Pain001AuditLogEntity a WHERE a.tenantId = :tenantId AND a.businessUnitId = :businessUnitId ORDER BY a.createdAt DESC")
  Page<Pain001AuditLogEntity> findByTenantIdAndBusinessUnitId(
      @Param("tenantId") String tenantId,
      @Param("businessUnitId") String businessUnitId,
      Pageable pageable);

  /** Find audit logs within date range and tenant */
  @Query(
      "SELECT a FROM Pain001AuditLogEntity a WHERE a.tenantId = :tenantId AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
  Page<Pain001AuditLogEntity> findByTenantIdAndDateRange(
      @Param("tenantId") String tenantId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);

  /** Count audit logs by action and tenant */
  @Query(
      "SELECT COUNT(a) FROM Pain001AuditLogEntity a WHERE a.action = :action AND a.tenantId = :tenantId")
  long countByActionAndTenantId(
      @Param("action") Pain001AuditLogEntity.Action action, @Param("tenantId") String tenantId);

  /** Find audit logs by tenant */
  @Query(
      "SELECT a FROM Pain001AuditLogEntity a WHERE a.tenantId = :tenantId ORDER BY a.createdAt DESC")
  Page<Pain001AuditLogEntity> findByTenantId(@Param("tenantId") String tenantId, Pageable pageable);
}
