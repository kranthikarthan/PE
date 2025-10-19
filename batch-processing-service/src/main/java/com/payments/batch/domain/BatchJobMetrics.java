package com.payments.batch.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity for batch job performance metrics.
 *
 * <p>This entity represents aggregated performance metrics for batch jobs
 * including execution statistics, success rates, and resource usage.
 * It provides comprehensive performance monitoring capabilities.
 *
 * @since PE-406
 */
@Entity
@Table(name = "batch_job_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobMetrics {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "job_name", nullable = false, length = 255)
  private String jobName;
  
  @Column(name = "tenant_id", nullable = false, length = 50)
  private String tenantId;
  
  @Column(name = "business_unit_id", length = 50)
  private String businessUnitId;
  
  @Column(name = "metric_date", nullable = false)
  private LocalDate metricDate;
  
  @Column(name = "metric_hour", nullable = false)
  private Integer metricHour;
  
  @Column(name = "total_executions")
  private Long totalExecutions = 0L;
  
  @Column(name = "successful_executions")
  private Long successfulExecutions = 0L;
  
  @Column(name = "failed_executions")
  private Long failedExecutions = 0L;
  
  @Column(name = "running_executions")
  private Long runningExecutions = 0L;
  
  @Column(name = "success_rate", precision = 5, scale = 2)
  private BigDecimal successRate = BigDecimal.ZERO;
  
  @Column(name = "failure_rate", precision = 5, scale = 2)
  private BigDecimal failureRate = BigDecimal.ZERO;
  
  @Column(name = "average_execution_time", precision = 10, scale = 2)
  private BigDecimal averageExecutionTime = BigDecimal.ZERO;
  
  @Column(name = "min_execution_time", precision = 10, scale = 2)
  private BigDecimal minExecutionTime = BigDecimal.ZERO;
  
  @Column(name = "max_execution_time", precision = 10, scale = 2)
  private BigDecimal maxExecutionTime = BigDecimal.ZERO;
  
  @Column(name = "total_execution_time", precision = 10, scale = 2)
  private BigDecimal totalExecutionTime = BigDecimal.ZERO;
  
  @Column(name = "average_processing_rate", precision = 10, scale = 2)
  private BigDecimal averageProcessingRate = BigDecimal.ZERO;
  
  @Column(name = "total_records_processed")
  private Long totalRecordsProcessed = 0L;
  
  @Column(name = "total_records_failed")
  private Long totalRecordsFailed = 0L;
  
  @Column(name = "total_records_skipped")
  private Long totalRecordsSkipped = 0L;
  
  @Column(name = "average_memory_usage", precision = 10, scale = 2)
  private BigDecimal averageMemoryUsage = BigDecimal.ZERO;
  
  @Column(name = "max_memory_usage", precision = 10, scale = 2)
  private BigDecimal maxMemoryUsage = BigDecimal.ZERO;
  
  @Column(name = "average_cpu_usage", precision = 5, scale = 2)
  private BigDecimal averageCpuUsage = BigDecimal.ZERO;
  
  @Column(name = "max_cpu_usage", precision = 5, scale = 2)
  private BigDecimal maxCpuUsage = BigDecimal.ZERO;
  
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private java.time.LocalDateTime createdAt;
  
  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private java.time.LocalDateTime updatedAt;
  
  /**
   * Calculates the success rate percentage.
   *
   * @return success rate percentage
   */
  public BigDecimal calculateSuccessRate() {
    if (totalExecutions != null && totalExecutions > 0 && successfulExecutions != null) {
      return BigDecimal.valueOf(successfulExecutions)
          .divide(BigDecimal.valueOf(totalExecutions), 4, BigDecimal.ROUND_HALF_UP)
          .multiply(BigDecimal.valueOf(100));
    }
    return BigDecimal.ZERO;
  }
  
  /**
   * Calculates the failure rate percentage.
   *
   * @return failure rate percentage
   */
  public BigDecimal calculateFailureRate() {
    if (totalExecutions != null && totalExecutions > 0 && failedExecutions != null) {
      return BigDecimal.valueOf(failedExecutions)
          .divide(BigDecimal.valueOf(totalExecutions), 4, BigDecimal.ROUND_HALF_UP)
          .multiply(BigDecimal.valueOf(100));
    }
    return BigDecimal.ZERO;
  }
  
  /**
   * Calculates the average execution time.
   *
   * @return average execution time in seconds
   */
  public BigDecimal calculateAverageExecutionTime() {
    if (totalExecutions != null && totalExecutions > 0 && totalExecutionTime != null) {
      return totalExecutionTime.divide(BigDecimal.valueOf(totalExecutions), 2, BigDecimal.ROUND_HALF_UP);
    }
    return BigDecimal.ZERO;
  }
  
  /**
   * Calculates the average processing rate.
   *
   * @return average processing rate (records per second)
   */
  public BigDecimal calculateAverageProcessingRate() {
    if (totalExecutionTime != null && totalExecutionTime.compareTo(BigDecimal.ZERO) > 0 && totalRecordsProcessed != null) {
      return BigDecimal.valueOf(totalRecordsProcessed)
          .divide(totalExecutionTime, 2, BigDecimal.ROUND_HALF_UP);
    }
    return BigDecimal.ZERO;
  }
  
  /**
   * Updates the metrics with new execution data.
   *
   * @param executionTime the execution time in seconds
   * @param recordsProcessed the number of records processed
   * @param recordsFailed the number of records failed
   * @param recordsSkipped the number of records skipped
   * @param memoryUsage the memory usage in MB
   * @param cpuUsage the CPU usage percentage
   * @param isSuccessful whether the execution was successful
   */
  public void updateMetrics(BigDecimal executionTime, Long recordsProcessed, Long recordsFailed, 
                           Long recordsSkipped, BigDecimal memoryUsage, BigDecimal cpuUsage, 
                           boolean isSuccessful) {
    
    // Update execution counts
    if (totalExecutions == null) totalExecutions = 0L;
    totalExecutions++;
    
    if (isSuccessful) {
      if (successfulExecutions == null) successfulExecutions = 0L;
      successfulExecutions++;
    } else {
      if (failedExecutions == null) failedExecutions = 0L;
      failedExecutions++;
    }
    
    // Update execution times
    if (executionTime != null) {
      if (totalExecutionTime == null) totalExecutionTime = BigDecimal.ZERO;
      totalExecutionTime = totalExecutionTime.add(executionTime);
      
      if (minExecutionTime == null || executionTime.compareTo(minExecutionTime) < 0) {
        minExecutionTime = executionTime;
      }
      
      if (maxExecutionTime == null || executionTime.compareTo(maxExecutionTime) > 0) {
        maxExecutionTime = executionTime;
      }
    }
    
    // Update record counts
    if (recordsProcessed != null) {
      if (totalRecordsProcessed == null) totalRecordsProcessed = 0L;
      totalRecordsProcessed += recordsProcessed;
    }
    
    if (recordsFailed != null) {
      if (totalRecordsFailed == null) totalRecordsFailed = 0L;
      totalRecordsFailed += recordsFailed;
    }
    
    if (recordsSkipped != null) {
      if (totalRecordsSkipped == null) totalRecordsSkipped = 0L;
      totalRecordsSkipped += recordsSkipped;
    }
    
    // Update resource usage
    if (memoryUsage != null) {
      if (averageMemoryUsage == null) averageMemoryUsage = BigDecimal.ZERO;
      // Simple moving average calculation
      averageMemoryUsage = averageMemoryUsage.add(memoryUsage).divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP);
      
      if (maxMemoryUsage == null || memoryUsage.compareTo(maxMemoryUsage) > 0) {
        maxMemoryUsage = memoryUsage;
      }
    }
    
    if (cpuUsage != null) {
      if (averageCpuUsage == null) averageCpuUsage = BigDecimal.ZERO;
      // Simple moving average calculation
      averageCpuUsage = averageCpuUsage.add(cpuUsage).divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP);
      
      if (maxCpuUsage == null || cpuUsage.compareTo(maxCpuUsage) > 0) {
        maxCpuUsage = cpuUsage;
      }
    }
    
    // Recalculate derived metrics
    this.successRate = calculateSuccessRate();
    this.failureRate = calculateFailureRate();
    this.averageExecutionTime = calculateAverageExecutionTime();
    this.averageProcessingRate = calculateAverageProcessingRate();
  }
  
  /**
   * Gets the metrics summary.
   *
   * @return metrics summary string
   */
  public String getSummary() {
    return String.format("Metrics[%s] %s - %d executions, %.2f%% success rate", 
        jobName, metricDate, totalExecutions, successRate);
  }
}
