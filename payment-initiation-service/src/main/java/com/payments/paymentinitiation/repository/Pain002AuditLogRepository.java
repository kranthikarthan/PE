package com.payments.paymentinitiation.repository;

import com.payments.paymentinitiation.entity.Pain002AuditLogEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for pain.002 audit log entities */
@Repository
public interface Pain002AuditLogRepository extends JpaRepository<Pain002AuditLogEntity, UUID> {

  /** Find audit logs by pain.002 status report ID */
  List<Pain002AuditLogEntity> findByStatusReportId(UUID statusReportId);

  /** Find audit logs by action */
  List<Pain002AuditLogEntity> findByAction(Pain002AuditLogEntity.Action action);

  /** Find audit logs within date range */
  @Query(
      "SELECT a FROM Pain002AuditLogEntity a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
  List<Pain002AuditLogEntity> findByDateRange(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  /** Count audit logs by action */
  long countByAction(Pain002AuditLogEntity.Action action);
}
