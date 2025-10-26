package com.payments.paymentinitiation.repository;

import com.payments.paymentinitiation.entity.Pain001Pain002CorrelationEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for pain.001/pain.002 correlation entities */
@Repository
public interface Pain001Pain002CorrelationRepository
    extends JpaRepository<Pain001Pain002CorrelationEntity, UUID> {

  /** Find correlation by correlation ID */
  Optional<Pain001Pain002CorrelationEntity> findByCorrelationId(String correlationId);

  /** Find correlations by pain.001 message ID */
  List<Pain001Pain002CorrelationEntity> findByPain001MessageId(UUID pain001MessageId);

  /** Find correlations by pain.002 status report ID */
  List<Pain001Pain002CorrelationEntity> findByPain002StatusReportId(UUID pain002StatusReportId);

  /** Find correlations by tenant and business unit */
  List<Pain001Pain002CorrelationEntity> findByTenantIdAndBusinessUnitId(
      String tenantId, String businessUnitId);

  /** Find correlations by correlation status */
  List<Pain001Pain002CorrelationEntity> findByCorrelationStatus(
      Pain001Pain002CorrelationEntity.CorrelationStatus correlationStatus);

  /** Find correlations by tenant, business unit, and correlation status */
  List<Pain001Pain002CorrelationEntity> findByTenantIdAndBusinessUnitIdAndCorrelationStatus(
      String tenantId,
      String businessUnitId,
      Pain001Pain002CorrelationEntity.CorrelationStatus correlationStatus);

  /** Check if correlation exists for pain.001 message */
  boolean existsByPain001MessageId(UUID pain001MessageId);

  /** Check if correlation exists for pain.002 status report */
  boolean existsByPain002StatusReportId(UUID pain002StatusReportId);

  /** Find pending correlations */
  @Query(
      "SELECT c FROM Pain001Pain002CorrelationEntity c WHERE c.correlationStatus = 'PENDING' "
          + "AND c.createdAt < :cutoffTime ORDER BY c.createdAt ASC")
  List<Pain001Pain002CorrelationEntity> findPendingCorrelations(
      @Param("cutoffTime") java.time.LocalDateTime cutoffTime);

  /** Count correlations by status */
  long countByCorrelationStatus(
      Pain001Pain002CorrelationEntity.CorrelationStatus correlationStatus);

  /** Count correlations by tenant and business unit */
  long countByTenantIdAndBusinessUnitId(String tenantId, String businessUnitId);
}
