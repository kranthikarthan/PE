package com.payments.settlement.repository;

import com.payments.domain.settlement.SettlementMonitoring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for SettlementMonitoring entity.
 *
 * <p>This repository provides data access methods for settlement monitoring
 * operations including CRUD operations, custom queries, and tenant-specific
 * data retrieval.
 *
 * @since PE-411
 */
@Repository
public interface SettlementMonitoringRepository extends JpaRepository<SettlementMonitoring, Long> {
  
  /**
   * Finds monitoring entry by monitoring ID and tenant ID.
   *
   * @param monitoringId the monitoring ID
   * @param tenantId the tenant ID
   * @return the monitoring entry if found
   */
  Optional<SettlementMonitoring> findByMonitoringIdAndTenantId(String monitoringId, UUID tenantId);
  
  /**
   * Finds all monitoring entries by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of monitoring entries
   */
  List<SettlementMonitoring> findByTenantId(UUID tenantId);
  
  /**
   * Finds monitoring entries by tenant ID and monitoring type.
   *
   * @param tenantId the tenant ID
   * @param monitoringType the monitoring type
   * @return list of monitoring entries
   */
  List<SettlementMonitoring> findByTenantIdAndMonitoringType(UUID tenantId, SettlementMonitoring.MonitoringType monitoringType);
  
  /**
   * Finds monitoring entries by tenant ID and status.
   *
   * @param tenantId the tenant ID
   * @param status the status
   * @return list of monitoring entries
   */
  List<SettlementMonitoring> findByTenantIdAndStatus(UUID tenantId, SettlementMonitoring.MonitoringStatus status);
  
  /**
   * Finds monitoring entries by tenant ID and participant ID.
   *
   * @param tenantId the tenant ID
   * @param participantId the participant ID
   * @return list of monitoring entries
   */
  List<SettlementMonitoring> findByTenantIdAndParticipantId(UUID tenantId, String participantId);
  
  /**
   * Finds monitoring entries by tenant ID and workflow ID.
   *
   * @param tenantId the tenant ID
   * @param workflowId the workflow ID
   * @return list of monitoring entries
   */
  List<SettlementMonitoring> findByTenantIdAndWorkflowId(UUID tenantId, Long workflowId);
  
  /**
   * Finds monitoring entries by tenant ID and orchestration ID.
   *
   * @param tenantId the tenant ID
   * @param orchestrationId the orchestration ID
   * @return list of monitoring entries
   */
  List<SettlementMonitoring> findByTenantIdAndOrchestrationId(UUID tenantId, Long orchestrationId);
  
  /**
   * Finds monitoring entries by tenant ID and currency.
   *
   * @param tenantId the tenant ID
   * @param currency the currency
   * @return list of monitoring entries
   */
  List<SettlementMonitoring> findByTenantIdAndCurrency(UUID tenantId, String currency);
  
  /**
   * Finds monitoring entries by tenant ID and date range.
   *
   * @param tenantId the tenant ID
   * @param startDate the start date
   * @param endDate the end date
   * @return list of monitoring entries
   */
  @Query("SELECT m FROM SettlementMonitoring m WHERE m.tenantId = :tenantId AND m.monitoringTimestamp >= :startDate AND m.monitoringTimestamp <= :endDate ORDER BY m.monitoringTimestamp DESC")
  List<SettlementMonitoring> findByTenantIdAndDateRange(@Param("tenantId") UUID tenantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
  
  /**
   * Finds monitoring entries by tenant ID and alert level.
   *
   * @param tenantId the tenant ID
   * @param alertLevel the alert level
   * @return list of monitoring entries
   */
  List<SettlementMonitoring> findByTenantIdAndAlertLevel(UUID tenantId, String alertLevel);
  
  /**
   * Finds monitoring entries by tenant ID and metric name.
   *
   * @param tenantId the tenant ID
   * @param metricName the metric name
   * @return list of monitoring entries
   */
  List<SettlementMonitoring> findByTenantIdAndMetricName(UUID tenantId, String metricName);
  
  /**
   * Finds monitoring entries by tenant ID and business unit ID.
   *
   * @param tenantId the tenant ID
   * @param businessUnitId the business unit ID
   * @return list of monitoring entries
   */
  List<SettlementMonitoring> findByTenantIdAndBusinessUnitId(UUID tenantId, String businessUnitId);
  
  /**
   * Counts monitoring entries by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return the count
   */
  long countByTenantId(UUID tenantId);
  
  /**
   * Counts monitoring entries by tenant ID and status.
   *
   * @param tenantId the tenant ID
   * @param status the status
   * @return the count
   */
  long countByTenantIdAndStatus(UUID tenantId, SettlementMonitoring.MonitoringStatus status);
  
  /**
   * Counts monitoring entries by tenant ID and monitoring type.
   *
   * @param tenantId the tenant ID
   * @param monitoringType the monitoring type
   * @return the count
   */
  long countByTenantIdAndMonitoringType(UUID tenantId, SettlementMonitoring.MonitoringType monitoringType);
  
  /**
   * Finds monitoring entries by tenant ID and alert level with pagination.
   *
   * @param tenantId the tenant ID
   * @param alertLevel the alert level
   * @return list of monitoring entries
   */
  @Query("SELECT m FROM SettlementMonitoring m WHERE m.tenantId = :tenantId AND m.alertLevel = :alertLevel ORDER BY m.monitoringTimestamp DESC")
  List<SettlementMonitoring> findCriticalAlertsByTenantId(@Param("tenantId") UUID tenantId, @Param("alertLevel") String alertLevel);
  
  /**
   * Finds monitoring entries by tenant ID and metric value range.
   *
   * @param tenantId the tenant ID
   * @param minValue the minimum value
   * @param maxValue the maximum value
   * @return list of monitoring entries
   */
  @Query("SELECT m FROM SettlementMonitoring m WHERE m.tenantId = :tenantId AND m.metricValue >= :minValue AND m.metricValue <= :maxValue ORDER BY m.metricValue DESC")
  List<SettlementMonitoring> findByTenantIdAndMetricValueRange(@Param("tenantId") UUID tenantId, @Param("minValue") java.math.BigDecimal minValue, @Param("maxValue") java.math.BigDecimal maxValue);
  
  /**
   * Finds monitoring entries by tenant ID and threshold exceeded.
   *
   * @param tenantId the tenant ID
   * @return list of monitoring entries
   */
  @Query("SELECT m FROM SettlementMonitoring m WHERE m.tenantId = :tenantId AND m.metricValue > m.thresholdValue ORDER BY m.metricValue DESC")
  List<SettlementMonitoring> findThresholdExceededByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds monitoring entries by tenant ID and threshold below.
   *
   * @param tenantId the tenant ID
   * @return list of monitoring entries
   */
  @Query("SELECT m FROM SettlementMonitoring m WHERE m.tenantId = :tenantId AND m.metricValue < m.thresholdValue ORDER BY m.metricValue ASC")
  List<SettlementMonitoring> findThresholdBelowByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds monitoring entries by tenant ID and threshold equals.
   *
   * @param tenantId the tenant ID
   * @return list of monitoring entries
   */
  @Query("SELECT m FROM SettlementMonitoring m WHERE m.tenantId = :tenantId AND m.metricValue = m.thresholdValue ORDER BY m.monitoringTimestamp DESC")
  List<SettlementMonitoring> findThresholdEqualsByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds monitoring entries by tenant ID and duration range.
   *
   * @param tenantId the tenant ID
   * @param minDuration the minimum duration
   * @param maxDuration the maximum duration
   * @return list of monitoring entries
   */
  @Query("SELECT m FROM SettlementMonitoring m WHERE m.tenantId = :tenantId AND m.durationSeconds >= :minDuration AND m.durationSeconds <= :maxDuration ORDER BY m.durationSeconds DESC")
  List<SettlementMonitoring> findByTenantIdAndDurationRange(@Param("tenantId") UUID tenantId, @Param("minDuration") Long minDuration, @Param("maxDuration") Long maxDuration);
  
  /**
   * Finds monitoring entries by tenant ID and created by.
   *
   * @param tenantId the tenant ID
   * @param createdBy the created by user
   * @return list of monitoring entries
   */
  List<SettlementMonitoring> findByTenantIdAndCreatedBy(UUID tenantId, String createdBy);
  
  /**
   * Finds monitoring entries by tenant ID and updated by.
   *
   * @param tenantId the tenant ID
   * @param updatedBy the updated by user
   * @return list of monitoring entries
   */
  List<SettlementMonitoring> findByTenantIdAndUpdatedBy(UUID tenantId, String updatedBy);
  
  /**
   * Finds monitoring entries by tenant ID and monitoring name pattern.
   *
   * @param tenantId the tenant ID
   * @param monitoringNamePattern the monitoring name pattern
   * @return list of monitoring entries
   */
  @Query("SELECT m FROM SettlementMonitoring m WHERE m.tenantId = :tenantId AND m.monitoringName LIKE %:monitoringNamePattern% ORDER BY m.monitoringName ASC")
  List<SettlementMonitoring> findByTenantIdAndMonitoringNamePattern(@Param("tenantId") UUID tenantId, @Param("monitoringNamePattern") String monitoringNamePattern);
  
  /**
   * Finds monitoring entries by tenant ID and description pattern.
   *
   * @param tenantId the tenant ID
   * @param descriptionPattern the description pattern
   * @return list of monitoring entries
   */
  @Query("SELECT m FROM SettlementMonitoring m WHERE m.tenantId = :tenantId AND m.description LIKE %:descriptionPattern% ORDER BY m.monitoringTimestamp DESC")
  List<SettlementMonitoring> findByTenantIdAndDescriptionPattern(@Param("tenantId") UUID tenantId, @Param("descriptionPattern") String descriptionPattern);
}
