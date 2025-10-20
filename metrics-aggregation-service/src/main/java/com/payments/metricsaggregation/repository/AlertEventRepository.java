package com.payments.metricsaggregation.repository;

import com.payments.metricsaggregation.entity.AlertEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Alert Event Repository
 * 
 * Repository for alert event entities.
 * Provides queries for alert event management and monitoring.
 */
@Repository
public interface AlertEventRepository extends JpaRepository<AlertEventEntity, Long> {

    /**
     * Find alert events by status
     */
    List<AlertEventEntity> findByStatus(AlertEventEntity.Status status);

    /**
     * Find alert events by status list
     */
    List<AlertEventEntity> findByStatusIn(List<AlertEventEntity.Status> statuses);

    /**
     * Find alert events by service name
     */
    List<AlertEventEntity> findByServiceName(String serviceName);

    /**
     * Find alert events by service name and status
     */
    List<AlertEventEntity> findByServiceNameAndStatus(String serviceName, AlertEventEntity.Status status);

    /**
     * Find alert events by rule ID
     */
    List<AlertEventEntity> findByRuleId(Long ruleId);

    /**
     * Find alert events by rule ID and status
     */
    List<AlertEventEntity> findByRuleIdAndStatus(Long ruleId, AlertEventEntity.Status status);

    /**
     * Check if active alert exists for rule
     */
    boolean existsByRuleIdAndStatusIn(Long ruleId, List<AlertEventEntity.Status> statuses);

    /**
     * Find alert events by time range
     */
    @Query("SELECT a FROM AlertEventEntity a WHERE a.triggeredAt BETWEEN :startTime AND :endTime ORDER BY a.triggeredAt DESC")
    List<AlertEventEntity> findByTimeRange(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    /**
     * Find alert events by service and time range
     */
    @Query("SELECT a FROM AlertEventEntity a WHERE a.serviceName = :serviceName AND a.triggeredAt BETWEEN :startTime AND :endTime ORDER BY a.triggeredAt DESC")
    List<AlertEventEntity> findByServiceAndTimeRange(
        @Param("serviceName") String serviceName, 
        @Param("startTime") Instant startTime, 
        @Param("endTime") Instant endTime);

    /**
     * Find alert events by severity
     */
    List<AlertEventEntity> findBySeverity(AlertEventEntity.AlertRuleEntity.Severity severity);

    /**
     * Find alert events by severity and status
     */
    List<AlertEventEntity> findBySeverityAndStatus(
        AlertEventEntity.AlertRuleEntity.Severity severity, 
        AlertEventEntity.Status status);

    /**
     * Count alert events by status
     */
    long countByStatus(AlertEventEntity.Status status);

    /**
     * Count alert events by service name
     */
    long countByServiceName(String serviceName);

    /**
     * Count alert events by service name and status
     */
    long countByServiceNameAndStatus(String serviceName, AlertEventEntity.Status status);

    /**
     * Find recent alert events
     */
    @Query("SELECT a FROM AlertEventEntity a ORDER BY a.triggeredAt DESC")
    List<AlertEventEntity> findRecentAlerts();

    /**
     * Find alert events by acknowledged by
     */
    List<AlertEventEntity> findByAcknowledgedBy(String acknowledgedBy);

    /**
     * Find alert events by resolved by
     */
    List<AlertEventEntity> findByResolvedBy(String resolvedBy);
}
