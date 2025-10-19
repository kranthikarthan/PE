package com.payments.batch.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity for batch job execution metadata.
 *
 * <p>This entity represents enhanced metadata for batch job executions including
 * performance tracking, progress monitoring, and detailed execution information.
 * It provides comprehensive tracking capabilities for batch processing operations.
 *
 * @since PE-406
 */
@Entity
@Table(name = "batch_job_execution_metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobExecutionMetadata {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "job_execution_id", nullable = false)
  private Long jobExecutionId;
  
  @Column(name = "job_name", nullable = false, length = 255)
  private String jobName;
  
  @Column(name = "tenant_id", nullable = false, length = 50)
  private String tenantId;
  
  @Column(name = "business_unit_id", length = 50)
  private String businessUnitId;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "execution_type", nullable = false, length = 50)
  private ExecutionType executionType = ExecutionType.MANUAL;
  
  @Column(name = "priority", nullable = false)
  private Integer priority = 5;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  private ExecutionStatus status;
  
  @Column(name = "start_time")
  private LocalDateTime startTime;
  
  @Column(name = "end_time")
  private LocalDateTime endTime;
  
  @Column(name = "duration_seconds")
  private Integer durationSeconds;
  
  @Column(name = "exit_code", length = 50)
  private String exitCode;
  
  @Column(name = "exit_description", columnDefinition = "TEXT")
  private String exitDescription;
  
  @Column(name = "progress_percentage", precision = 5, scale = 2)
  private BigDecimal progressPercentage = BigDecimal.ZERO;
  
  @Column(name = "estimated_completion_time")
  private LocalDateTime estimatedCompletionTime;
  
  @Column(name = "current_step", length = 255)
  private String currentStep;
  
  @Column(name = "step_progress", precision = 5, scale = 2)
  private BigDecimal stepProgress = BigDecimal.ZERO;
  
  @Column(name = "steps_completed")
  private Integer stepsCompleted = 0;
  
  @Column(name = "total_steps")
  private Integer totalSteps = 0;
  
  @Column(name = "records_processed")
  private Long recordsProcessed = 0L;
  
  @Column(name = "records_failed")
  private Long recordsFailed = 0L;
  
  @Column(name = "records_skipped")
  private Long recordsSkipped = 0L;
  
  @Column(name = "total_records")
  private Long totalRecords = 0L;
  
  @Column(name = "processing_rate", precision = 10, scale = 2)
  private BigDecimal processingRate;
  
  @Column(name = "memory_usage_mb", precision = 10, scale = 2)
  private BigDecimal memoryUsageMb;
  
  @Column(name = "cpu_usage_percentage", precision = 5, scale = 2)
  private BigDecimal cpuUsagePercentage;
  
  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;
  
  @Column(name = "stack_trace", columnDefinition = "TEXT")
  private String stackTrace;
  
  @Column(name = "parameters", columnDefinition = "JSONB")
  private String parameters;
  
  @Column(name = "configuration", columnDefinition = "JSONB")
  private String configuration;
  
  @Column(name = "metadata", columnDefinition = "JSONB")
  private String metadata;
  
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
  
  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
  
  @Column(name = "created_by", length = 100)
  private String createdBy;
  
  @Column(name = "updated_by", length = 100)
  private String updatedBy;
  
  /**
   * Enumeration of execution types.
   */
  public enum ExecutionType {
    MANUAL, SCHEDULED, API, WEBHOOK
  }
  
  /**
   * Enumeration of execution statuses.
   */
  public enum ExecutionStatus {
    PENDING, RUNNING, COMPLETED, FAILED, STOPPED, PAUSED
  }
  
  /**
   * Calculates the duration in seconds between start and end time.
   *
   * @return duration in seconds, or null if not calculable
   */
  public Integer calculateDuration() {
    if (startTime != null && endTime != null) {
      return (int) java.time.Duration.between(startTime, endTime).getSeconds();
    }
    return null;
  }
  
  /**
   * Calculates the processing rate (records per second).
   *
   * @return processing rate, or null if not calculable
   */
  public BigDecimal calculateProcessingRate() {
    if (durationSeconds != null && durationSeconds > 0 && recordsProcessed != null) {
      return BigDecimal.valueOf(recordsProcessed).divide(BigDecimal.valueOf(durationSeconds), 2, BigDecimal.ROUND_HALF_UP);
    }
    return null;
  }
  
  /**
   * Calculates the success rate percentage.
   *
   * @return success rate percentage, or null if not calculable
   */
  public BigDecimal calculateSuccessRate() {
    if (totalRecords != null && totalRecords > 0) {
      long successfulRecords = recordsProcessed != null ? recordsProcessed : 0L;
      return BigDecimal.valueOf(successfulRecords)
          .divide(BigDecimal.valueOf(totalRecords), 4, BigDecimal.ROUND_HALF_UP)
          .multiply(BigDecimal.valueOf(100));
    }
    return null;
  }
  
  /**
   * Checks if the execution is currently running.
   *
   * @return true if running, false otherwise
   */
  public boolean isRunning() {
    return status == ExecutionStatus.RUNNING;
  }
  
  /**
   * Checks if the execution is completed (successfully or with failure).
   *
   * @return true if completed, false otherwise
   */
  public boolean isCompleted() {
    return status == ExecutionStatus.COMPLETED || status == ExecutionStatus.FAILED;
  }
  
  /**
   * Checks if the execution was successful.
   *
   * @return true if successful, false otherwise
   */
  public boolean isSuccessful() {
    return status == ExecutionStatus.COMPLETED;
  }
  
  /**
   * Checks if the execution failed.
   *
   * @return true if failed, false otherwise
   */
  public boolean isFailed() {
    return status == ExecutionStatus.FAILED;
  }
  
  /**
   * Gets the execution summary.
   *
   * @return execution summary string
   */
  public String getSummary() {
    return String.format("JobExecution[%d] %s - %s (%s)", 
        jobExecutionId, jobName, status, 
        startTime != null ? startTime.toString() : "N/A");
  }
}
