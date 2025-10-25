package com.payments.saga.repository;

import com.payments.saga.entity.SagaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Saga Management Repository
 * 
 * Provides data access for saga management operations.
 * Enables operations teams to query and manage sagas.
 */
@Repository
public interface SagaManagementRepository extends JpaRepository<SagaEntity, String> {

    /**
     * Find sagas by tenant and business unit
     */
    Page<SagaEntity> findByTenantIdAndBusinessUnitId(
        String tenantId, 
        String businessUnitId, 
        Pageable pageable);

    /**
     * Find sagas by tenant, business unit, and status
     */
    Page<SagaEntity> findByTenantIdAndBusinessUnitIdAndStatus(
        String tenantId, 
        String businessUnitId, 
        String status, 
        Pageable pageable);

    /**
     * Find saga by ID, tenant, and business unit
     */
    Optional<SagaEntity> findBySagaIdAndTenantIdAndBusinessUnitId(
        String sagaId, 
        String tenantId, 
        String businessUnitId);

    /**
     * Find sagas by correlation ID
     */
    List<SagaEntity> findByCorrelationIdAndTenantIdAndBusinessUnitId(
        String correlationId, 
        String tenantId, 
        String businessUnitId);

    /**
     * Find sagas by payment ID
     */
    List<SagaEntity> findByPaymentIdAndTenantIdAndBusinessUnitId(
        String paymentId, 
        String tenantId, 
        String businessUnitId);

    /**
     * Find sagas by template name
     */
    List<SagaEntity> findByTemplateNameAndTenantIdAndBusinessUnitId(
        String templateName, 
        String tenantId, 
        String businessUnitId);

    /**
     * Find sagas by status
     */
    List<SagaEntity> findByStatusAndTenantIdAndBusinessUnitId(
        String status, 
        String tenantId, 
        String businessUnitId);

    /**
     * Find sagas created between dates
     */
    @Query("SELECT s FROM SagaEntity s WHERE s.tenantId = :tenantId AND s.businessUnitId = :businessUnitId " +
           "AND s.createdAt BETWEEN :startDate AND :endDate")
    List<SagaEntity> findSagasByDateRange(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * Find long-running sagas
     */
    @Query("SELECT s FROM SagaEntity s WHERE s.tenantId = :tenantId AND s.businessUnitId = :businessUnitId " +
           "AND s.status = 'RUNNING' AND s.createdAt < :threshold")
    List<SagaEntity> findLongRunningSagas(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("threshold") LocalDateTime threshold);

    /**
     * Find failed sagas
     */
    @Query("SELECT s FROM SagaEntity s WHERE s.tenantId = :tenantId AND s.businessUnitId = :businessUnitId " +
           "AND s.status = 'FAILED' AND s.updatedAt > :since")
    List<SagaEntity> findFailedSagasSince(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("since") LocalDateTime since);

    /**
     * Count sagas by status
     */
    @Query("SELECT COUNT(s) FROM SagaEntity s WHERE s.tenantId = :tenantId AND s.businessUnitId = :businessUnitId " +
           "AND s.status = :status")
    long countByStatus(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("status") String status);

    /**
     * Count total sagas
     */
    @Query("SELECT COUNT(s) FROM SagaEntity s WHERE s.tenantId = :tenantId AND s.businessUnitId = :businessUnitId")
    long countByTenantAndBusinessUnit(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId);

    /**
     * Find sagas by multiple statuses
     */
    @Query("SELECT s FROM SagaEntity s WHERE s.tenantId = :tenantId AND s.businessUnitId = :businessUnitId " +
           "AND s.status IN :statuses")
    List<SagaEntity> findByStatuses(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("statuses") List<String> statuses);

    /**
     * Find sagas by template and status
     */
    @Query("SELECT s FROM SagaEntity s WHERE s.tenantId = :tenantId AND s.businessUnitId = :businessUnitId " +
           "AND s.templateName = :templateName AND s.status = :status")
    List<SagaEntity> findByTemplateAndStatus(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("templateName") String templateName,
        @Param("status") String status);

    /**
     * Find sagas with execution time greater than threshold
     */
    @Query("SELECT s FROM SagaEntity s WHERE s.tenantId = :tenantId AND s.businessUnitId = :businessUnitId " +
           "AND s.status = 'COMPLETED' AND (s.completedAt - s.createdAt) > :threshold")
    List<SagaEntity> findSagasWithLongExecutionTime(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("threshold") java.time.Duration threshold);

    /**
     * Find sagas by correlation ID with pagination
     */
    @Query("SELECT s FROM SagaEntity s WHERE s.correlationId = :correlationId AND s.tenantId = :tenantId " +
           "AND s.businessUnitId = :businessUnitId")
    Page<SagaEntity> findByCorrelationIdWithPagination(
        @Param("correlationId") String correlationId,
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        Pageable pageable);

    /**
     * Find sagas by payment ID with pagination
     */
    @Query("SELECT s FROM SagaEntity s WHERE s.paymentId = :paymentId AND s.tenantId = :tenantId " +
           "AND s.businessUnitId = :businessUnitId")
    Page<SagaEntity> findByPaymentIdWithPagination(
        @Param("paymentId") String paymentId,
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        Pageable pageable);

    /**
     * Find sagas by template name with pagination
     */
    @Query("SELECT s FROM SagaEntity s WHERE s.templateName = :templateName AND s.tenantId = :tenantId " +
           "AND s.businessUnitId = :businessUnitId")
    Page<SagaEntity> findByTemplateNameWithPagination(
        @Param("templateName") String templateName,
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        Pageable pageable);

    /**
     * Find sagas by status with pagination
     */
    @Query("SELECT s FROM SagaEntity s WHERE s.status = :status AND s.tenantId = :tenantId " +
           "AND s.businessUnitId = :businessUnitId")
    Page<SagaEntity> findByStatusWithPagination(
        @Param("status") String status,
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        Pageable pageable);

    /**
     * Find sagas by date range with pagination
     */
    @Query("SELECT s FROM SagaEntity s WHERE s.tenantId = :tenantId AND s.businessUnitId = :businessUnitId " +
           "AND s.createdAt BETWEEN :startDate AND :endDate")
    Page<SagaEntity> findByDateRangeWithPagination(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);

    /**
     * Find sagas by multiple criteria with pagination
     */
    @Query("SELECT s FROM SagaEntity s WHERE s.tenantId = :tenantId AND s.businessUnitId = :businessUnitId " +
           "AND (:status IS NULL OR s.status = :status) " +
           "AND (:templateName IS NULL OR s.templateName = :templateName) " +
           "AND (:startDate IS NULL OR s.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR s.createdAt <= :endDate)")
    Page<SagaEntity> findByMultipleCriteria(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("status") String status,
        @Param("templateName") String templateName,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
}
