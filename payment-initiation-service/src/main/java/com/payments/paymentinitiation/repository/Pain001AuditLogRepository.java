package com.payments.paymentinitiation.repository;

import com.payments.paymentinitiation.entity.Pain001AuditLogEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for pain.001 audit log entities */
@Repository
public interface Pain001AuditLogRepository extends JpaRepository<Pain001AuditLogEntity, UUID> {

  /** Find audit logs by pain.001 message ID */
  List<Pain001AuditLogEntity> findByPain001MessageId(UUID pain001MessageId);

  /** Find audit logs by action */
  List<Pain001AuditLogEntity> findByAction(Pain001AuditLogEntity.Action action);

  /** Find audit logs within date range */
  @Query(
      "SELECT a FROM Pain001AuditLogEntity a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
  List<Pain001AuditLogEntity> findByDateRange(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  /** Count audit logs by action */
  long countByAction(Pain001AuditLogEntity.Action action);
}
