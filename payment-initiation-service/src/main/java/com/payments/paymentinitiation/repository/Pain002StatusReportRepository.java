package com.payments.paymentinitiation.repository;

import com.payments.paymentinitiation.entity.Pain002StatusReportEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for pain.002 status report entities */
@Repository
public interface Pain002StatusReportRepository
    extends JpaRepository<Pain002StatusReportEntity, UUID> {

  /** Find pain.002 status report by message ID */
  Optional<Pain002StatusReportEntity> findByMessageId(String messageId);

  /** Find pain.002 status reports by tenant and business unit */
  List<Pain002StatusReportEntity> findByTenantIdAndBusinessUnitId(
      String tenantId, String businessUnitId);

  /** Find pain.002 status reports by tenant, business unit, and date range */
  @Query(
      "SELECT p FROM Pain002StatusReportEntity p WHERE p.tenantId = :tenantId AND p.businessUnitId = :businessUnitId "
          + "AND p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
  List<Pain002StatusReportEntity> findByTenantAndBusinessUnitAndDateRange(
      @Param("tenantId") String tenantId,
      @Param("businessUnitId") String businessUnitId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /** Count pain.002 status reports by tenant and business unit */
  long countByTenantIdAndBusinessUnitId(String tenantId, String businessUnitId);

  /** Find pain.002 status reports by correlation ID */
  @Query(
      "SELECT p FROM Pain002StatusReportEntity p WHERE p.id IN "
          + "(SELECT c.pain002StatusReport.id FROM Pain001Pain002CorrelationEntity c WHERE c.correlationId = :correlationId)")
  List<Pain002StatusReportEntity> findByCorrelationId(@Param("correlationId") String correlationId);

  /** Find pain.002 status reports for a specific pain.001 message */
  @Query(
      "SELECT p FROM Pain002StatusReportEntity p WHERE p.id IN "
          + "(SELECT c.pain002StatusReport.id FROM Pain001Pain002CorrelationEntity c WHERE c.pain001Message.id = :pain001MessageId)")
  List<Pain002StatusReportEntity> findByPain001MessageId(
      @Param("pain001MessageId") UUID pain001MessageId);
}
