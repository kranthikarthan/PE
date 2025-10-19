package com.payments.batch.repository;

import com.payments.batch.domain.BatchJobMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BatchJobMetrics entity.
 *
 * <p>This repository provides data access methods for batch job performance metrics
 * including querying by various criteria, performance analysis, and trend calculations.
 *
 * @since PE-406
 */
@Repository
public interface BatchJobMetricsRepository extends JpaRepository<BatchJobMetrics, Long> {
  
  /**
   * Finds metrics by job name and tenant ID.
   *
   * @param jobName the job name
   * @param tenantId the tenant ID
   * @return list of metrics
   */
  List<BatchJobMetrics> findByJobNameAndTenantId(String jobName, String tenantId);
  
  /**
   * Finds metrics by tenant ID with pagination.
   *
   * @param tenantId the tenant ID
   * @param pageable pagination information
   * @return page of metrics
   */
  Page<BatchJobMetrics> findByTenantId(String tenantId, Pageable pageable);
  
  /**
   * Finds metrics by date range and tenant ID.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param tenantId the tenant ID
   * @return list of metrics
   */
  @Query("SELECT m FROM BatchJobMetrics m WHERE m.tenantId = :tenantId " +
         "AND m.metricDate >= :startDate AND m.metricDate <= :endDate " +
         "ORDER BY m.metricDate DESC, m.metricHour DESC")
  List<BatchJobMetrics> findByDateRangeAndTenantId(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds metrics by job name and date range.
   *
   * @param jobName the job name
   * @param startDate the start date
   * @param endDate the end date
   * @param tenantId the tenant ID
   * @return list of metrics
   */
  @Query("SELECT m FROM BatchJobMetrics m WHERE m.jobName = :jobName " +
         "AND m.tenantId = :tenantId AND m.metricDate >= :startDate AND m.metricDate <= :endDate " +
         "ORDER BY m.metricDate DESC, m.metricHour DESC")
  List<BatchJobMetrics> findByJobNameAndDateRange(
      @Param("jobName") String jobName,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds metrics by specific date and tenant ID.
   *
   * @param metricDate the metric date
   * @param tenantId the tenant ID
   * @return list of metrics
   */
  List<BatchJobMetrics> findByMetricDateAndTenantId(LocalDate metricDate, String tenantId);
  
  /**
   * Finds metrics by specific date and job name.
   *
   * @param metricDate the metric date
   * @param jobName the job name
   * @param tenantId the tenant ID
   * @return list of metrics
   */
  List<BatchJobMetrics> findByMetricDateAndJobNameAndTenantId(
      LocalDate metricDate, String jobName, String tenantId);
  
  /**
   * Finds metrics by hour range and tenant ID.
   *
   * @param startHour the start hour
   * @param endHour the end hour
   * @param tenantId the tenant ID
   * @return list of metrics
   */
  @Query("SELECT m FROM BatchJobMetrics m WHERE m.tenantId = :tenantId " +
         "AND m.metricHour >= :startHour AND m.metricHour <= :endHour " +
         "ORDER BY m.metricDate DESC, m.metricHour DESC")
  List<BatchJobMetrics> findByHourRangeAndTenantId(
      @Param("startHour") Integer startHour,
      @Param("endHour") Integer endHour,
      @Param("tenantId") String tenantId);
  
  /**
   * Finds metrics with high failure rates.
   *
   * @param tenantId the tenant ID
   * @param minFailureRate the minimum failure rate percentage
   * @return list of metrics
   */
  @Query("SELECT m FROM BatchJobMetrics m WHERE m.tenantId = :tenantId " +
         "AND m.failureRate >= :minFailureRate " +
         "ORDER BY m.failureRate DESC")
  List<BatchJobMetrics> findHighFailureRateMetrics(
      @Param("tenantId") String tenantId, @Param("minFailureRate") Double minFailureRate);
  
  /**
   * Finds metrics with low success rates.
   *
   * @param tenantId the tenant ID
   * @param maxSuccessRate the maximum success rate percentage
   * @return list of metrics
   */
  @Query("SELECT m FROM BatchJobMetrics m WHERE m.tenantId = :tenantId " +
         "AND m.successRate <= :maxSuccessRate " +
         "ORDER BY m.successRate ASC")
  List<BatchJobMetrics> findLowSuccessRateMetrics(
      @Param("tenantId") String tenantId, @Param("maxSuccessRate") Double maxSuccessRate);
  
  /**
   * Finds metrics with long execution times.
   *
   * @param tenantId the tenant ID
   * @param minExecutionTime the minimum execution time in seconds
   * @return list of metrics
   */
  @Query("SELECT m FROM BatchJobMetrics m WHERE m.tenantId = :tenantId " +
         "AND m.averageExecutionTime >= :minExecutionTime " +
         "ORDER BY m.averageExecutionTime DESC")
  List<BatchJobMetrics> findLongExecutionTimeMetrics(
      @Param("tenantId") String tenantId, @Param("minExecutionTime") Double minExecutionTime);
  
  /**
   * Finds metrics with high memory usage.
   *
   * @param tenantId the tenant ID
   * @param minMemoryUsage the minimum memory usage in MB
   * @return list of metrics
   */
  @Query("SELECT m FROM BatchJobMetrics m WHERE m.tenantId = :tenantId " +
         "AND m.averageMemoryUsage >= :minMemoryUsage " +
         "ORDER BY m.averageMemoryUsage DESC")
  List<BatchJobMetrics> findHighMemoryUsageMetrics(
      @Param("tenantId") String tenantId, @Param("minMemoryUsage") Double minMemoryUsage);
  
  /**
   * Finds metrics with high CPU usage.
   *
   * @param tenantId the tenant ID
   * @param minCpuUsage the minimum CPU usage percentage
   * @return list of metrics
   */
  @Query("SELECT m FROM BatchJobMetrics m WHERE m.tenantId = :tenantId " +
         "AND m.averageCpuUsage >= :minCpuUsage " +
         "ORDER BY m.averageCpuUsage DESC")
  List<BatchJobMetrics> findHighCpuUsageMetrics(
      @Param("tenantId") String tenantId, @Param("minCpuUsage") Double minCpuUsage);
  
  /**
   * Gets aggregated metrics by date range and tenant ID.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param tenantId the tenant ID
   * @return aggregated metrics
   */
  @Query("SELECT " +
         "SUM(m.totalExecutions) as totalExecutions, " +
         "SUM(m.successfulExecutions) as successfulExecutions, " +
         "SUM(m.failedExecutions) as failedExecutions, " +
         "AVG(m.successRate) as averageSuccessRate, " +
         "AVG(m.averageExecutionTime) as averageExecutionTime, " +
         "SUM(m.totalRecordsProcessed) as totalRecordsProcessed " +
         "FROM BatchJobMetrics m WHERE m.tenantId = :tenantId " +
         "AND m.metricDate >= :startDate AND m.metricDate <= :endDate")
  Object[] getAggregatedMetricsByDateRange(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("tenantId") String tenantId);
  
  /**
   * Gets aggregated metrics by job name and date range.
   *
   * @param jobName the job name
   * @param startDate the start date
   * @param endDate the end date
   * @param tenantId the tenant ID
   * @return aggregated metrics
   */
  @Query("SELECT " +
         "SUM(m.totalExecutions) as totalExecutions, " +
         "SUM(m.successfulExecutions) as successfulExecutions, " +
         "SUM(m.failedExecutions) as failedExecutions, " +
         "AVG(m.successRate) as averageSuccessRate, " +
         "AVG(m.averageExecutionTime) as averageExecutionTime, " +
         "SUM(m.totalRecordsProcessed) as totalRecordsProcessed " +
         "FROM BatchJobMetrics m WHERE m.jobName = :jobName " +
         "AND m.tenantId = :tenantId AND m.metricDate >= :startDate AND m.metricDate <= :endDate")
  Object[] getAggregatedMetricsByJobNameAndDateRange(
      @Param("jobName") String jobName,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("tenantId") String tenantId);
  
  /**
   * Gets daily metrics summary by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return daily metrics summary
   */
  @Query("SELECT m.metricDate, " +
         "SUM(m.totalExecutions) as totalExecutions, " +
         "SUM(m.successfulExecutions) as successfulExecutions, " +
         "SUM(m.failedExecutions) as failedExecutions, " +
         "AVG(m.successRate) as averageSuccessRate " +
         "FROM BatchJobMetrics m WHERE m.tenantId = :tenantId " +
         "GROUP BY m.metricDate ORDER BY m.metricDate DESC")
  List<Object[]> getDailyMetricsSummaryByTenantId(@Param("tenantId") String tenantId);
  
  /**
   * Gets hourly metrics summary by date and tenant ID.
   *
   * @param metricDate the metric date
   * @param tenantId the tenant ID
   * @return hourly metrics summary
   */
  @Query("SELECT m.metricHour, " +
         "SUM(m.totalExecutions) as totalExecutions, " +
         "SUM(m.successfulExecutions) as successfulExecutions, " +
         "SUM(m.failedExecutions) as failedExecutions, " +
         "AVG(m.successRate) as averageSuccessRate " +
         "FROM BatchJobMetrics m WHERE m.tenantId = :tenantId " +
         "AND m.metricDate = :metricDate " +
         "GROUP BY m.metricHour ORDER BY m.metricHour ASC")
  List<Object[]> getHourlyMetricsSummaryByDate(
      @Param("metricDate") LocalDate metricDate, @Param("tenantId") String tenantId);
  
  /**
   * Gets top performing jobs by tenant ID.
   *
   * @param tenantId the tenant ID
   * @param limit the limit
   * @return top performing jobs
   */
  @Query("SELECT m.jobName, " +
         "SUM(m.totalExecutions) as totalExecutions, " +
         "AVG(m.successRate) as averageSuccessRate, " +
         "AVG(m.averageExecutionTime) as averageExecutionTime " +
         "FROM BatchJobMetrics m WHERE m.tenantId = :tenantId " +
         "GROUP BY m.jobName " +
         "ORDER BY AVG(m.successRate) DESC, SUM(m.totalExecutions) DESC")
  List<Object[]> getTopPerformingJobsByTenantId(@Param("tenantId") String tenantId, @Param("limit") Integer limit);
  
  /**
   * Gets worst performing jobs by tenant ID.
   *
   * @param tenantId the tenant ID
   * @param limit the limit
   * @return worst performing jobs
   */
  @Query("SELECT m.jobName, " +
         "SUM(m.totalExecutions) as totalExecutions, " +
         "AVG(m.successRate) as averageSuccessRate, " +
         "AVG(m.averageExecutionTime) as averageExecutionTime " +
         "FROM BatchJobMetrics m WHERE m.tenantId = :tenantId " +
         "GROUP BY m.jobName " +
         "ORDER BY AVG(m.successRate) ASC, SUM(m.totalExecutions) DESC")
  List<Object[]> getWorstPerformingJobsByTenantId(@Param("tenantId") String tenantId, @Param("limit") Integer limit);
  
  /**
   * Counts metrics by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return count of metrics
   */
  long countByTenantId(String tenantId);
  
  /**
   * Counts metrics by job name and tenant ID.
   *
   * @param jobName the job name
   * @param tenantId the tenant ID
   * @return count of metrics
   */
  long countByJobNameAndTenantId(String jobName, String tenantId);
  
  /**
   * Deletes old metrics by date and tenant ID.
   *
   * @param cutoffDate the cutoff date
   * @param tenantId the tenant ID
   * @return number of deleted records
   */
  @Query("DELETE FROM BatchJobMetrics m WHERE m.tenantId = :tenantId " +
         "AND m.metricDate < :cutoffDate")
  int deleteOldMetrics(@Param("cutoffDate") LocalDate cutoffDate, @Param("tenantId") String tenantId);
  
  /**
   * Gets metrics statistics by tenant ID.
   *
   * @param tenantId the tenant ID
   * @return metrics statistics
   */
  @Query("SELECT " +
         "COUNT(m) as totalMetrics, " +
         "SUM(m.totalExecutions) as totalExecutions, " +
         "SUM(m.successfulExecutions) as successfulExecutions, " +
         "SUM(m.failedExecutions) as failedExecutions, " +
         "AVG(m.successRate) as averageSuccessRate, " +
         "AVG(m.averageExecutionTime) as averageExecutionTime " +
         "FROM BatchJobMetrics m WHERE m.tenantId = :tenantId")
  Object[] getMetricsStatisticsByTenantId(@Param("tenantId") String tenantId);
}
