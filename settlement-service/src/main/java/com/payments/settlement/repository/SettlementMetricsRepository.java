package com.payments.settlement.repository;

import com.payments.domain.settlement.SettlementMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for SettlementMetrics entity.
 *
 * <p>This repository provides data access methods for settlement metrics
 * operations including CRUD operations, custom queries, and tenant-specific
 * data retrieval.
 *
 * @since PE-411
 */
@Repository
public interface SettlementMetricsRepository extends JpaRepository<SettlementMetrics, Long> {
  
  /**
   * Finds metrics by metrics ID and tenant ID.
   *
   * @param metricsId the metrics ID
   * @param tenantId the tenant ID
   * @return the metrics if found
   */
  Optional<SettlementMetrics> findByMetricsIdAndTenantId(String metricsId, UUID tenantId);
  
  /**
   * Finds all metrics by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return list of metrics
   */
  List<SettlementMetrics> findByTenantId(UUID tenantId);
  
  /**
   * Finds metrics by tenant ID and metrics type.
   *
   * @param tenantId the tenant ID
   * @param metricsType the metrics type
   * @return list of metrics
   */
  List<SettlementMetrics> findByTenantIdAndMetricsType(UUID tenantId, SettlementMetrics.MetricsType metricsType);
  
  /**
   * Finds metrics by tenant ID and category.
   *
   * @param tenantId the tenant ID
   * @param category the category
   * @return list of metrics
   */
  List<SettlementMetrics> findByTenantIdAndCategory(UUID tenantId, SettlementMetrics.MetricsCategory category);
  
  /**
   * Finds metrics by tenant ID and metric name.
   *
   * @param tenantId the tenant ID
   * @param metricName the metric name
   * @return list of metrics
   */
  List<SettlementMetrics> findByTenantIdAndMetricName(UUID tenantId, String metricName);
  
  /**
   * Finds metrics by tenant ID and participant ID.
   *
   * @param tenantId the tenant ID
   * @param participantId the participant ID
   * @return list of metrics
   */
  List<SettlementMetrics> findByTenantIdAndParticipantId(UUID tenantId, String participantId);
  
  /**
   * Finds metrics by tenant ID and workflow ID.
   *
   * @param tenantId the tenant ID
   * @param workflowId the workflow ID
   * @return list of metrics
   */
  List<SettlementMetrics> findByTenantIdAndWorkflowId(UUID tenantId, Long workflowId);
  
  /**
   * Finds metrics by tenant ID and orchestration ID.
   *
   * @param tenantId the tenant ID
   * @param orchestrationId the orchestration ID
   * @return list of metrics
   */
  List<SettlementMetrics> findByTenantIdAndOrchestrationId(UUID tenantId, Long orchestrationId);
  
  /**
   * Finds metrics by tenant ID and currency.
   *
   * @param tenantId the tenant ID
   * @param currency the currency
   * @return list of metrics
   */
  List<SettlementMetrics> findByTenantIdAndCurrency(UUID tenantId, String currency);
  
  /**
   * Finds metrics by tenant ID and business unit ID.
   *
   * @param tenantId the tenant ID
   * @param businessUnitId the business unit ID
   * @return list of metrics
   */
  List<SettlementMetrics> findByTenantIdAndBusinessUnitId(UUID tenantId, String businessUnitId);
  
  /**
   * Finds metrics by tenant ID and date range.
   *
   * @param tenantId the tenant ID
   * @param startDate the start date
   * @param endDate the end date
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.metricsTimestamp >= :startDate AND m.metricsTimestamp <= :endDate ORDER BY m.metricsTimestamp DESC")
  List<SettlementMetrics> findByTenantIdAndDateRange(@Param("tenantId") UUID tenantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
  
  /**
   * Finds metrics by tenant ID and period range.
   *
   * @param tenantId the tenant ID
   * @param periodStart the period start
   * @param periodEnd the period end
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.periodStart >= :periodStart AND m.periodEnd <= :periodEnd ORDER BY m.periodStart DESC")
  List<SettlementMetrics> findByTenantIdAndPeriodRange(@Param("tenantId") UUID tenantId, @Param("periodStart") LocalDateTime periodStart, @Param("periodEnd") LocalDateTime periodEnd);
  
  /**
   * Finds metrics by tenant ID and created by.
   *
   * @param tenantId the tenant ID
   * @param createdBy the created by user
   * @return list of metrics
   */
  List<SettlementMetrics> findByTenantIdAndCreatedBy(UUID tenantId, String createdBy);
  
  /**
   * Finds metrics by tenant ID and updated by.
   *
   * @param tenantId the tenant ID
   * @param updatedBy the updated by user
   * @return list of metrics
   */
  List<SettlementMetrics> findByTenantIdAndUpdatedBy(UUID tenantId, String updatedBy);
  
  /**
   * Counts metrics by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return the count
   */
  long countByTenantId(UUID tenantId);
  
  /**
   * Counts metrics by tenant ID and metrics type.
   *
   * @param tenantId the tenant ID
   * @param metricsType the metrics type
   * @return the count
   */
  long countByTenantIdAndMetricsType(UUID tenantId, SettlementMetrics.MetricsType metricsType);
  
  /**
   * Counts metrics by tenant ID and category.
   *
   * @param tenantId the tenant ID
   * @param category the category
   * @return the count
   */
  long countByTenantIdAndCategory(UUID tenantId, SettlementMetrics.MetricsCategory category);
  
  /**
   * Finds metrics by tenant ID and metric value range.
   *
   * @param tenantId the tenant ID
   * @param minValue the minimum value
   * @param maxValue the maximum value
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.metricValue >= :minValue AND m.metricValue <= :maxValue ORDER BY m.metricValue DESC")
  List<SettlementMetrics> findByTenantIdAndMetricValueRange(@Param("tenantId") UUID tenantId, @Param("minValue") java.math.BigDecimal minValue, @Param("maxValue") java.math.BigDecimal maxValue);
  
  /**
   * Finds metrics by tenant ID and threshold exceeded.
   *
   * @param tenantId the tenant ID
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.metricValue > m.thresholdMax ORDER BY m.metricValue DESC")
  List<SettlementMetrics> findThresholdExceededByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds metrics by tenant ID and threshold below.
   *
   * @param tenantId the tenant ID
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.metricValue < m.thresholdMin ORDER BY m.metricValue ASC")
  List<SettlementMetrics> findThresholdBelowByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds metrics by tenant ID and within threshold.
   *
   * @param tenantId the tenant ID
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.metricValue >= m.thresholdMin AND m.metricValue <= m.thresholdMax ORDER BY m.metricValue DESC")
  List<SettlementMetrics> findWithinThresholdByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds metrics by tenant ID and target exceeded.
   *
   * @param tenantId the tenant ID
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.metricValue >= m.targetValue ORDER BY m.metricValue DESC")
  List<SettlementMetrics> findTargetExceededByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds metrics by tenant ID and target below.
   *
   * @param tenantId the tenant ID
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.metricValue < m.targetValue ORDER BY m.metricValue ASC")
  List<SettlementMetrics> findTargetBelowByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds metrics by tenant ID and baseline exceeded.
   *
   * @param tenantId the tenant ID
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.metricValue > m.baselineValue ORDER BY m.metricValue DESC")
  List<SettlementMetrics> findBaselineExceededByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds metrics by tenant ID and baseline below.
   *
   * @param tenantId the tenant ID
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.metricValue < m.baselineValue ORDER BY m.metricValue ASC")
  List<SettlementMetrics> findBaselineBelowByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds metrics by tenant ID and metrics name pattern.
   *
   * @param tenantId the tenant ID
   * @param metricsNamePattern the metrics name pattern
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.metricsName LIKE %:metricsNamePattern% ORDER BY m.metricsName ASC")
  List<SettlementMetrics> findByTenantIdAndMetricsNamePattern(@Param("tenantId") UUID tenantId, @Param("metricsNamePattern") String metricsNamePattern);
  
  /**
   * Finds metrics by tenant ID and description pattern.
   *
   * @param tenantId the tenant ID
   * @param descriptionPattern the description pattern
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.description LIKE %:descriptionPattern% ORDER BY m.metricsTimestamp DESC")
  List<SettlementMetrics> findByTenantIdAndDescriptionPattern(@Param("tenantId") UUID tenantId, @Param("descriptionPattern") String descriptionPattern);
  
  /**
   * Finds metrics by tenant ID and metric unit.
   *
   * @param tenantId the tenant ID
   * @param metricUnit the metric unit
   * @return list of metrics
   */
  List<SettlementMetrics> findByTenantIdAndMetricUnit(UUID tenantId, String metricUnit);
  
  /**
   * Finds metrics by tenant ID and performance status.
   *
   * @param tenantId the tenant ID
   * @param performanceStatus the performance status
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.metricValue >= m.targetValue ORDER BY m.metricValue DESC")
  List<SettlementMetrics> findMeetsTargetByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds metrics by tenant ID and performance status.
   *
   * @param tenantId the tenant ID
   * @param performanceStatus the performance status
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.metricValue < m.targetValue ORDER BY m.metricValue ASC")
  List<SettlementMetrics> findBelowTargetByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds metrics by tenant ID and performance status.
   *
   * @param tenantId the tenant ID
   * @param performanceStatus the performance status
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.metricValue > m.baselineValue ORDER BY m.metricValue DESC")
  List<SettlementMetrics> findAboveBaselineByTenantId(@Param("tenantId") UUID tenantId);
  
  /**
   * Finds metrics by tenant ID and performance status.
   *
   * @param tenantId the tenant ID
   * @param performanceStatus the performance status
   * @return list of metrics
   */
  @Query("SELECT m FROM SettlementMetrics m WHERE m.tenantId = :tenantId AND m.metricValue < m.baselineValue ORDER BY m.metricValue ASC")
  List<SettlementMetrics> findBelowBaselineByTenantId(@Param("tenantId") UUID tenantId);
}
