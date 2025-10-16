package com.payments.saga.repository;

import com.payments.saga.domain.SagaStatus;
import com.payments.saga.entity.SagaEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Saga entities */
@Repository
public interface SagaRepository extends JpaRepository<SagaEntity, String> {

  List<SagaEntity> findByTenantIdAndBusinessUnitId(String tenantId, String businessUnitId);

  List<SagaEntity> findByCorrelationId(String correlationId);

  List<SagaEntity> findByPaymentId(String paymentId);

  List<SagaEntity> findByStatus(SagaStatus status);

  List<SagaEntity> findByStatusAndStartedAtBefore(SagaStatus status, Instant before);

  @Query(
      "SELECT s FROM SagaEntity s WHERE s.tenantId = :tenantId AND s.businessUnitId = :businessUnitId AND s.status IN :statuses")
  List<SagaEntity> findByTenantAndBusinessUnitAndStatusIn(
      @Param("tenantId") String tenantId,
      @Param("businessUnitId") String businessUnitId,
      @Param("statuses") List<SagaStatus> statuses);

  @Query("SELECT s FROM SagaEntity s WHERE s.status = :status AND s.startedAt < :timeoutThreshold")
  List<SagaEntity> findTimedOutSagas(
      @Param("status") SagaStatus status, @Param("timeoutThreshold") Instant timeoutThreshold);

  @Query(
      "SELECT COUNT(s) FROM SagaEntity s WHERE s.tenantId = :tenantId AND s.businessUnitId = :businessUnitId AND s.status = :status")
  long countByTenantAndBusinessUnitAndStatus(
      @Param("tenantId") String tenantId,
      @Param("businessUnitId") String businessUnitId,
      @Param("status") SagaStatus status);

  @Query(
      "SELECT s FROM SagaEntity s WHERE s.correlationId = :correlationId ORDER BY s.startedAt DESC")
  List<SagaEntity> findByCorrelationIdOrderByStartedAtDesc(
      @Param("correlationId") String correlationId);

  @Query("SELECT s FROM SagaEntity s WHERE s.paymentId = :paymentId ORDER BY s.startedAt DESC")
  List<SagaEntity> findByPaymentIdOrderByStartedAtDesc(@Param("paymentId") String paymentId);
}
