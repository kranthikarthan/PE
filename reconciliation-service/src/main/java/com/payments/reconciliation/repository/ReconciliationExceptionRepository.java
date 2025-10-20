package com.payments.reconciliation.repository;

import com.payments.reconciliation.entity.ReconciliationExceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reconciliation Exception Repository
 * 
 * Provides data access for reconciliation exceptions.
 */
@Repository
public interface ReconciliationExceptionRepository extends JpaRepository<ReconciliationExceptionEntity, String> {

    /**
     * Find exceptions by run ID
     */
    List<ReconciliationExceptionEntity> findByRunIdAndTenantIdAndBusinessUnitId(
        String runId, String tenantId, String businessUnitId);

    /**
     * Find exceptions by status
     */
    List<ReconciliationExceptionEntity> findByStatusAndTenantIdAndBusinessUnitId(
        String status, String tenantId, String businessUnitId);

    /**
     * Find exceptions by severity
     */
    List<ReconciliationExceptionEntity> findBySeverityAndTenantIdAndBusinessUnitId(
        String severity, String tenantId, String businessUnitId);

    /**
     * Find exceptions by type
     */
    List<ReconciliationExceptionEntity> findByTypeAndTenantIdAndBusinessUnitId(
        String type, String tenantId, String businessUnitId);

    /**
     * Find exceptions by clearing system
     */
    List<ReconciliationExceptionEntity> findByClearingSystemAndTenantIdAndBusinessUnitId(
        String clearingSystem, String tenantId, String businessUnitId);

    /**
     * Find exceptions by assigned user
     */
    List<ReconciliationExceptionEntity> findByAssignedToAndTenantIdAndBusinessUnitId(
        String assignedTo, String tenantId, String businessUnitId);

    /**
     * Find exceptions by date range
     */
    @Query("SELECT e FROM ReconciliationExceptionEntity e WHERE e.tenantId = :tenantId AND e.businessUnitId = :businessUnitId " +
           "AND e.createdAt BETWEEN :startDate AND :endDate")
    List<ReconciliationExceptionEntity> findByDateRange(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * Count exceptions by status
     */
    @Query("SELECT COUNT(e) FROM ReconciliationExceptionEntity e WHERE e.tenantId = :tenantId AND e.businessUnitId = :businessUnitId " +
           "AND e.status = :status")
    long countByStatus(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("status") String status);

    /**
     * Count exceptions by severity
     */
    @Query("SELECT COUNT(e) FROM ReconciliationExceptionEntity e WHERE e.tenantId = :tenantId AND e.businessUnitId = :businessUnitId " +
           "AND e.severity = :severity")
    long countBySeverity(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("severity") String severity);

    /**
     * Find overdue exceptions
     */
    @Query("SELECT e FROM ReconciliationExceptionEntity e WHERE e.tenantId = :tenantId AND e.businessUnitId = :businessUnitId " +
           "AND e.dueDate < :now AND e.status != 'RESOLVED' AND e.status != 'CLOSED'")
    List<ReconciliationExceptionEntity> findOverdueExceptions(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("now") LocalDateTime now);

    /**
     * Find high priority exceptions
     */
    @Query("SELECT e FROM ReconciliationExceptionEntity e WHERE e.tenantId = :tenantId AND e.businessUnitId = :businessUnitId " +
           "AND e.isHighPriority = true AND e.status != 'RESOLVED' AND e.status != 'CLOSED'")
    List<ReconciliationExceptionEntity> findHighPriorityExceptions(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId);

    /**
     * Find escalated exceptions
     */
    @Query("SELECT e FROM ReconciliationExceptionEntity e WHERE e.tenantId = :tenantId AND e.businessUnitId = :businessUnitId " +
           "AND e.isEscalated = true")
    List<ReconciliationExceptionEntity> findEscalatedExceptions(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId);

    /**
     * Find exceptions by multiple criteria
     */
    @Query("SELECT e FROM ReconciliationExceptionEntity e WHERE e.tenantId = :tenantId AND e.businessUnitId = :businessUnitId " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:severity IS NULL OR e.severity = :severity) " +
           "AND (:type IS NULL OR e.type = :type) " +
           "AND (:clearingSystem IS NULL OR e.clearingSystem = :clearingSystem)")
    List<ReconciliationExceptionEntity> findByMultipleCriteria(
        @Param("tenantId") String tenantId,
        @Param("businessUnitId") String businessUnitId,
        @Param("status") String status,
        @Param("severity") String severity,
        @Param("type") String type,
        @Param("clearingSystem") String clearingSystem);

    /**
     * Find exceptions by tenant and business unit
     */
    List<ReconciliationExceptionEntity> findByTenantIdAndBusinessUnitId(String tenantId, String businessUnitId);

    /**
     * Count exceptions by tenant and business unit
     */
    long countByTenantIdAndBusinessUnitId(String tenantId, String businessUnitId);
}
