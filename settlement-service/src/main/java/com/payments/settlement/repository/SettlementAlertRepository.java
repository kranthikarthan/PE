package com.payments.settlement.repository;

import com.payments.domain.settlement.SettlementAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for SettlementAlert entity.
 *
 * <p>This repository provides data access methods for settlement alert
 * operations including CRUD operations, custom queries, and tenant-specific
 * data retrieval.
 *
 * @since PE-411
 */
@Repository
public interface SettlementAlertRepository extends JpaRepository<SettlementAlert, Long> {
  
  /**
   * Finds alert by alert ID and tenant ID.
   *
   * @param alertId the alert ID
   * @param tenantId the tenant ID
   * @return the alert if found
   */
  Optional<SettlementAlert> findByAlertIdAndTenantId(String alertId, UUID tenantId);
  
  /**
   * Finds all alerts by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of alerts
   */
  List<SettlementAlert> findByTenantId(UUID tenantId);
  
  /**
   * Finds alerts by tenant ID and alert type.
   *
   * @param tenantId the tenant ID
   * @param alertType the alert type
   * @return list of alerts
   */
  List<SettlementAlert> findByTenantIdAndAlertType(UUID tenantId, SettlementAlert.AlertType alertType);
  
  /**
   * Finds alerts by tenant ID and severity.
   *
   * @param tenantId the tenant ID
   * @param severity the severity
   * @return list of alerts
   */
  List<SettlementAlert> findByTenantIdAndSeverity(UUID tenantId, SettlementAlert.AlertSeverity severity);
  
  /**
   * Finds alerts by tenant ID and status.
   *
   * @param tenantId the tenant ID
   * @param status the status
   * @return list of alerts
   */
  List<SettlementAlert> findByTenantIdAndStatus(UUID tenantId, SettlementAlert.AlertStatus status);
  
  /**
   * Finds alerts by tenant ID and participant ID.
   *
   * @param tenantId the tenant ID
   * @param participantId the participant ID
   * @return list of alerts
   */
  List<SettlementAlert> findByTenantIdAndParticipantId(UUID tenantId, String participantId);
  
  /**
   * Finds alerts by tenant ID and workflow ID.
   *
   * @param tenantId the tenant ID
   * @param workflowId the workflow ID
   * @return list of alerts
   */
  List<SettlementAlert> findByTenantIdAndWorkflowId(UUID tenantId, Long workflowId);
  
  /**
   * Finds alerts by tenant ID and orchestration ID.
   *
   * @param tenantId the tenant ID
   * @param orchestrationId the orchestration ID
   * @return list of alerts
   */
  List<SettlementAlert> findByTenantIdAndOrchestrationId(UUID tenantId, Long orchestrationId);
  
  /**
   * Finds alerts by tenant ID and monitoring ID.
   *
   * @param tenantId the tenant ID
   * @param monitoringId the monitoring ID
   * @return list of alerts
   */
  List<SettlementAlert> findByTenantIdAndMonitoringId(UUID tenantId, Long monitoringId);
  
  /**
   * Finds alerts by tenant ID and business unit ID.
   *
   * @param tenantId the tenant ID
   * @param businessUnitId the business unit ID
   * @return list of alerts
   */
  List<SettlementAlert> findByTenantIdAndBusinessUnitId(UUID tenantId, String businessUnitId);
  
  /**
   * Finds alerts by tenant ID and date range.
   *
   * @param tenantId the tenant ID
   * @param startDate the start date
   * @param endDate the end date
   * @return list of alerts
   */
  @Query("SELECT a FROM SettlementAlert a WHERE a.tenantId = :tenantId AND a.alertTimestamp >= :startDate AND a.alertTimestamp <= :endDate ORDER BY a.alertTimestamp DESC")
  List<SettlementAlert> findByTenantIdAndDateRange(@Param("tenantId") UUID tenantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
  
  /**
   * Finds alerts by tenant ID and acknowledged by.
   *
   * @param tenantId the tenant ID
   * @param acknowledgedBy the acknowledged by user
   * @return list of alerts
   */
  List<SettlementAlert> findByTenantIdAndAcknowledgedBy(UUID tenantId, String acknowledgedBy);
  
  /**
   * Finds alerts by tenant ID and resolved by.
   *
   * @param tenantId the tenant ID
   * @param resolvedBy the resolved by user
   * @return list of alerts
   */
  List<SettlementAlert> findByTenantIdAndResolvedBy(UUID tenantId, String resolvedBy);
  
  /**
   * Finds alerts by tenant ID and created by.
   *
   * @param tenantId the tenant ID
   * @param createdBy the created by user
   * @return list of alerts
   */
  List<SettlementAlert> findByTenantIdAndCreatedBy(UUID tenantId, String createdBy);
  
  /**
   * Finds alerts by tenant ID and updated by.
   *
   * @param tenantId the tenant ID
   * @param updatedBy the updated by user
   * @return list of alerts
   */
  List<SettlementAlert> findByTenantIdAndUpdatedBy(UUID tenantId, String updatedBy);
  
  /**
   * Counts alerts by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return the count
   */
  long countByTenantId(UUID tenantId);
  
  /**
   * Counts alerts by tenant ID and status.
   *
   * @param tenantId the tenant ID
   * @param status the status
   * @return the count
   */
  long countByTenantIdAndStatus(UUID tenantId, SettlementAlert.AlertStatus status);
  
  /**
   * Counts alerts by tenant ID and severity.
   *
   * @param tenantId the tenant ID
   * @param severity the severity
   * @return the count
   */
  long countByTenantIdAndSeverity(UUID tenantId, SettlementAlert.AlertSeverity severity);
  
  /**
   * Counts alerts by tenant ID and alert type.
   *
   * @param tenantId the tenant ID
   * @param alertType the alert type
   * @return the count
   */
  long countByTenantIdAndAlertType(UUID tenantId, SettlementAlert.AlertType alertType);
  
  /**
   * Finds critical alerts by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of critical alerts
   */
  @Query("SELECT a FROM SettlementAlert a WHERE a.tenantId = :tenantId AND a.severity = 'CRITICAL' ORDER BY a.alertTimestamp DESC")
  List<SettlementAlert> findCriticalAlertsByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds high severity alerts by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of high severity alerts
   */
  @Query("SELECT a FROM SettlementAlert a WHERE a.tenantId = :tenantId AND a.severity = 'HIGH' ORDER BY a.alertTimestamp DESC")
  List<SettlementAlert> findHighSeverityAlertsByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds active alerts by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of active alerts
   */
  @Query("SELECT a FROM SettlementAlert a WHERE a.tenantId = :tenantId AND a.status = 'ACTIVE' ORDER BY a.alertTimestamp DESC")
  List<SettlementAlert> findActiveAlertsByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds acknowledged alerts by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of acknowledged alerts
   */
  @Query("SELECT a FROM SettlementAlert a WHERE a.tenantId = :tenantId AND a.status = 'ACKNOWLEDGED' ORDER BY a.alertTimestamp DESC")
  List<SettlementAlert> findAcknowledgedAlertsByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds resolved alerts by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of resolved alerts
   */
  @Query("SELECT a FROM SettlementAlert a WHERE a.tenantId = :tenantId AND a.status = 'RESOLVED' ORDER BY a.alertTimestamp DESC")
  List<SettlementAlert> findResolvedAlertsByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds suppressed alerts by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of suppressed alerts
   */
  @Query("SELECT a FROM SettlementAlert a WHERE a.tenantId = :tenantId AND a.status = 'SUPPRESSED' ORDER BY a.alertTimestamp DESC")
  List<SettlementAlert> findSuppressedAlertsByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds expired alerts by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of expired alerts
   */
  @Query("SELECT a FROM SettlementAlert a WHERE a.tenantId = :tenantId AND a.status = 'EXPIRED' ORDER BY a.alertTimestamp DESC")
  List<SettlementAlert> findExpiredAlertsByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds alerts by tenant ID and alert name pattern.
   *
   * @param tenantId the tenant ID
   * @param alertNamePattern the alert name pattern
   * @return list of alerts
   */
  @Query("SELECT a FROM SettlementAlert a WHERE a.tenantId = :tenantId AND a.alertName LIKE %:alertNamePattern% ORDER BY a.alertName ASC")
  List<SettlementAlert> findByTenantIdAndAlertNamePattern(@Param("tenantId") UUID tenantId, @Param("alertNamePattern") String alertNamePattern);
  
  /**
   * Finds alerts by tenant ID and alert message pattern.
   *
   * @param tenantId the tenant ID
   * @param alertMessagePattern the alert message pattern
   * @return list of alerts
   */
  @Query("SELECT a FROM SettlementAlert a WHERE a.tenantId = :tenantId AND a.alertMessage LIKE %:alertMessagePattern% ORDER BY a.alertTimestamp DESC")
  List<SettlementAlert> findByTenantIdAndAlertMessagePattern(@Param("tenantId") UUID tenantId, @Param("alertMessagePattern") String alertMessagePattern);
  
  /**
   * Finds alerts by tenant ID and resolution notes pattern.
   *
   * @param tenantId the tenant ID
   * @param resolutionNotesPattern the resolution notes pattern
   * @return list of alerts
   */
  @Query("SELECT a FROM SettlementAlert a WHERE a.tenantId = :tenantId AND a.resolutionNotes LIKE %:resolutionNotesPattern% ORDER BY a.alertTimestamp DESC")
  List<SettlementAlert> findByTenantIdAndResolutionNotesPattern(@Param("tenantId") UUID tenantId, @Param("resolutionNotesPattern") String resolutionNotesPattern);
  
  /**
   * Finds alerts by tenant ID and duration range.
   *
   * @param tenantId the tenant ID
   * @param minDuration the minimum duration in minutes
   * @param maxDuration the maximum duration in minutes
   * @return list of alerts
   */
  @Query("SELECT a FROM SettlementAlert a WHERE a.tenantId = :tenantId AND a.alertTimestamp >= :startTime AND a.alertTimestamp <= :endTime ORDER BY a.alertTimestamp DESC")
  List<SettlementAlert> findByTenantIdAndDurationRange(@Param("tenantId") UUID tenantId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
  
  /**
   * Finds alerts by tenant ID and acknowledgment time range.
   *
   * @param tenantId the tenant ID
   * @param startTime the start time
   * @param endTime the end time
   * @return list of alerts
   */
  @Query("SELECT a FROM SettlementAlert a WHERE a.tenantId = :tenantId AND a.acknowledgedAt >= :startTime AND a.acknowledgedAt <= :endTime ORDER BY a.acknowledgedAt DESC")
  List<SettlementAlert> findByTenantIdAndAcknowledgmentTimeRange(@Param("tenantId") UUID tenantId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
  
  /**
   * Finds alerts by tenant ID and resolution time range.
   *
   * @param tenantId the tenant ID
   * @param startTime the start time
   * @param endTime the end time
   * @return list of alerts
   */
  @Query("SELECT a FROM SettlementAlert a WHERE a.tenantId = :tenantId AND a.resolvedAt >= :startTime AND a.resolvedAt <= :endTime ORDER BY a.resolvedAt DESC")
  List<SettlementAlert> findByTenantIdAndResolutionTimeRange(@Param("tenantId") UUID tenantId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
