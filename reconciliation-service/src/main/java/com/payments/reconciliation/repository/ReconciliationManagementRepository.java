package com.payments.reconciliation.repository;

import com.payments.reconciliation.entity.ReconciliationRunEntity;
import com.payments.reconciliation.entity.ReconciliationExceptionEntity;
import com.payments.reconciliation.service.ReconciliationManagementService.ReconciliationStatisticsData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reconciliation Management Repository
 * 
 * Provides data access for reconciliation management operations.
 * Enables operations teams to query and manage reconciliation processes.
 */
@Repository
public interface ReconciliationManagementRepository extends JpaRepository<ReconciliationRunEntity, String> {

    /**
     * Find reconciliation runs with filtering
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:clearingSystem IS NULL OR r.clearingSystem = :clearingSystem) " +
           "AND (:startDate IS NULL OR r.startedAt >= :startDate) " +
           "AND (:endDate IS NULL OR r.startedAt <= :endDate)")
    Page<ReconciliationRunEntity> findReconciliationRuns(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("status") String status,
        @Param("clearingSystem") String clearingSystem,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);

    /**
     * Find reconciliation run by ID, tenant, and business unit
     */
    ReconciliationRunEntity findByRunIdAndTenantIdAndBusinessUnitId(
        String runId, String tenantId, String businessUnitId);

    /**
     * Check if there's a running reconciliation for a clearing system
     */
    @Query("SELECT COUNT(r) > 0 FROM ReconciliationRunEntity r WHERE r.clearingSystem = :clearingSystem " +
           "AND r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId AND r.status = 'RUNNING'")
    boolean hasRunningReconciliation(
        @Param("clearingSystem") String clearingSystem,
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId);

    /**
     * Find reconciliation runs by clearing system
     */
    List<ReconciliationRunEntity> findByClearingSystemAndTenantIdAndBusinessUnitId(
        String clearingSystem, String tenantId, String businessUnitId);

    /**
     * Find reconciliation runs by status
     */
    List<ReconciliationRunEntity> findByStatusAndTenantIdAndBusinessUnitId(
        String status, String tenantId, String businessUnitId);

    /**
     * Find reconciliation runs by date range
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId " +
           "AND r.startedAt BETWEEN :startDate AND :endDate")
    List<ReconciliationRunEntity> findByDateRange(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * Count reconciliation runs by status
     */
    @Query("SELECT COUNT(r) FROM ReconciliationRunEntity r WHERE r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId " +
           "AND r.status = :status")
    long countByStatus(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("status") String status);

    /**
     * Count total reconciliation runs
     */
    @Query("SELECT COUNT(r) FROM ReconciliationRunEntity r WHERE r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId")
    long countByTenantAndBusinessUnit(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId);

    /**
     * Get reconciliation statistics
     */
    @Query("SELECT " +
           "COUNT(r) as totalRuns, " +
           "COUNT(CASE WHEN r.status = 'COMPLETED' THEN 1 END) as successfulRuns, " +
           "COUNT(CASE WHEN r.status = 'FAILED' THEN 1 END) as failedRuns, " +
           "COUNT(CASE WHEN r.status = 'RUNNING' THEN 1 END) as runningRuns, " +
           "CASE WHEN COUNT(r) > 0 THEN " +
           "  (COUNT(CASE WHEN r.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(r)) " +
           "ELSE 0 END as successRate, " +
           "CASE WHEN COUNT(CASE WHEN r.status = 'COMPLETED' THEN 1 END) > 0 THEN " +
           "  AVG(CASE WHEN r.status = 'COMPLETED' THEN " +
           "    EXTRACT(EPOCH FROM (r.completedAt - r.startedAt)) " +
           "  END) " +
           "ELSE 0 END as averageProcessingTime " +
           "FROM ReconciliationRunEntity r " +
           "WHERE r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId " +
           "AND (:startDate IS NULL OR r.startedAt >= :startDate) " +
           "AND (:endDate IS NULL OR r.startedAt <= :endDate) " +
           "AND (:clearingSystem IS NULL OR r.clearingSystem = :clearingSystem)")
    ReconciliationStatisticsData getReconciliationStatistics(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("clearingSystem") String clearingSystem);

    /**
     * Find reconciliation runs by multiple criteria
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:clearingSystem IS NULL OR r.clearingSystem = :clearingSystem) " +
           "AND (:startDate IS NULL OR r.startedAt >= :startDate) " +
           "AND (:endDate IS NULL OR r.startedAt <= :endDate)")
    List<ReconciliationRunEntity> findByMultipleCriteria(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("status") String status,
        @Param("clearingSystem") String clearingSystem,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * Find reconciliation runs by clearing system with pagination
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.clearingSystem = :clearingSystem " +
           "AND r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId")
    Page<ReconciliationRunEntity> findByClearingSystemWithPagination(
        @Param("clearingSystem") String clearingSystem,
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        Pageable pageable);

    /**
     * Find reconciliation runs by status with pagination
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.status = :status " +
           "AND r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId")
    Page<ReconciliationRunEntity> findByStatusWithPagination(
        @Param("status") String status,
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        Pageable pageable);

    /**
     * Find reconciliation runs by date range with pagination
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId " +
           "AND r.startedAt BETWEEN :startDate AND :endDate")
    Page<ReconciliationRunEntity> findByDateRangeWithPagination(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);

    /**
     * Find long-running reconciliation runs
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId " +
           "AND r.status = 'RUNNING' AND r.startedAt < :threshold")
    List<ReconciliationRunEntity> findLongRunningReconciliations(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("threshold") LocalDateTime threshold);

    /**
     * Find failed reconciliation runs
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId " +
           "AND r.status = 'FAILED' AND r.startedAt > :since")
    List<ReconciliationRunEntity> findFailedReconciliationsSince(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("since") LocalDateTime since);

    /**
     * Find reconciliation runs with high exception count
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId " +
           "AND r.unmatchedRecords > :threshold")
    List<ReconciliationRunEntity> findReconciliationsWithHighExceptionCount(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("threshold") Integer threshold);

    /**
     * Find reconciliation runs by user
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.startedBy = :userId " +
           "AND r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId")
    List<ReconciliationRunEntity> findByUser(
        @Param("userId") String userId,
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId);

    /**
     * Find reconciliation runs by clearing system and status
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.clearingSystem = :clearingSystem " +
           "AND r.status = :status AND r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId")
    List<ReconciliationRunEntity> findByClearingSystemAndStatus(
        @Param("clearingSystem") String clearingSystem,
        @Param("status") String status,
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId);

    /**
     * Find reconciliation runs with processing time greater than threshold
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId " +
           "AND r.status = 'COMPLETED' AND (r.completedAt - r.startedAt) > :threshold")
    List<ReconciliationRunEntity> findReconciliationsWithLongProcessingTime(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("threshold") java.time.Duration threshold);

    /**
     * Find reconciliation runs by description
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.description LIKE %:description% " +
           "AND r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId")
    List<ReconciliationRunEntity> findByDescriptionContaining(
        @Param("description") String description,
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId);

    /**
     * Find reconciliation runs by started by user
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.startedBy = :startedBy " +
           "AND r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId")
    List<ReconciliationRunEntity> findByStartedBy(
        @Param("startedBy") String startedBy,
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId);

    /**
     * Find reconciliation runs by stopped by user
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.stoppedBy = :stoppedBy " +
           "AND r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId")
    List<ReconciliationRunEntity> findByStoppedBy(
        @Param("stoppedBy") String stoppedBy,
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId);

    /**
     * Find reconciliation runs by stop reason
     */
    @Query("SELECT r FROM ReconciliationRunEntity r WHERE r.stopReason LIKE %:reason% " +
           "AND r.tenantId = :tenantId AND r.businessUnitId = :businessUnitId")
    List<ReconciliationRunEntity> findByStopReasonContaining(
        @Param("reason") String reason,
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId);
}
