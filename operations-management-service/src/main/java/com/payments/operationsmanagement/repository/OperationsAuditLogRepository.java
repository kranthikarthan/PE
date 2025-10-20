package com.payments.operationsmanagement.repository;

import com.payments.operationsmanagement.entity.OperationsAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Operations Audit Log Repository
 * 
 * Repository for operations audit log entities.
 * Provides methods to query audit logs by various criteria.
 */
@Repository
public interface OperationsAuditLogRepository extends JpaRepository<OperationsAuditLogEntity, Long> {

    /**
     * Find audit logs by tenant ID
     */
    List<OperationsAuditLogEntity> findByTenantId(String tenantId);

    /**
     * Find audit logs by user ID
     */
    List<OperationsAuditLogEntity> findByUserId(String userId);

    /**
     * Find audit logs by action type
     */
    List<OperationsAuditLogEntity> findByActionType(OperationsAuditLogEntity.ActionType actionType);

    /**
     * Find audit logs by entity type and entity ID
     */
    List<OperationsAuditLogEntity> findByEntityTypeAndEntityId(
        OperationsAuditLogEntity.EntityType entityType, 
        String entityId
    );

    /**
     * Find audit logs by date range
     */
    @Query("SELECT a FROM OperationsAuditLogEntity a WHERE a.performedAt BETWEEN :startDate AND :endDate")
    List<OperationsAuditLogEntity> findByDateRange(
        @Param("startDate") Instant startDate, 
        @Param("endDate") Instant endDate
    );

    /**
     * Find audit logs by tenant and date range
     */
    @Query("SELECT a FROM OperationsAuditLogEntity a WHERE a.tenantId = :tenantId AND a.performedAt BETWEEN :startDate AND :endDate")
    List<OperationsAuditLogEntity> findByTenantAndDateRange(
        @Param("tenantId") String tenantId,
        @Param("startDate") Instant startDate, 
        @Param("endDate") Instant endDate
    );

    /**
     * Find recent audit logs for a specific entity
     */
    @Query("SELECT a FROM OperationsAuditLogEntity a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.performedAt DESC")
    List<OperationsAuditLogEntity> findRecentByEntity(
        @Param("entityType") OperationsAuditLogEntity.EntityType entityType,
        @Param("entityId") String entityId
    );

    /**
     * Count audit logs by action type
     */
    long countByActionType(OperationsAuditLogEntity.ActionType actionType);

    /**
     * Count audit logs by tenant and action type
     */
    long countByTenantIdAndActionType(String tenantId, OperationsAuditLogEntity.ActionType actionType);
}
