package com.payments.batch.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for batch job metrics.
 *
 * <p>This DTO encapsulates performance metrics and statistics for batch job executions,
 * including execution times, success rates, and resource usage.
 *
 * @since PE-405
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobMetricsResponse {
  
  /** The time range for the metrics */
  private String timeRange;
  
  /** The start time of the metrics period */
  private LocalDateTime startTime;
  
  /** The end time of the metrics period */
  private LocalDateTime endTime;
  
  /** The total number of job executions */
  private Long totalExecutions;
  
  /** The number of successful executions */
  private Long successfulExecutions;
  
  /** The number of failed executions */
  private Long failedExecutions;
  
  /** The number of running executions */
  private Long runningExecutions;
  
  /** The success rate percentage */
  private Double successRate;
  
  /** The failure rate percentage */
  private Double failureRate;
  
  /** The average execution time in seconds */
  private Double averageExecutionTime;
  
  /** The minimum execution time in seconds */
  private Double minExecutionTime;
  
  /** The maximum execution time in seconds */
  private Double maxExecutionTime;
  
  /** The total execution time in seconds */
  private Double totalExecutionTime;
  
  /** The average processing rate (records per second) */
  private Double averageProcessingRate;
  
  /** The total number of records processed */
  private Long totalRecordsProcessed;
  
  /** The total number of records failed */
  private Long totalRecordsFailed;
  
  /** The total number of records skipped */
  private Long totalRecordsSkipped;
  
  /** The average memory usage in MB */
  private Double averageMemoryUsage;
  
  /** The maximum memory usage in MB */
  private Double maxMemoryUsage;
  
  /** The average CPU usage percentage */
  private Double averageCpuUsage;
  
  /** The maximum CPU usage percentage */
  private Double maxCpuUsage;
  
  /** The job execution trends over time */
  private List<ExecutionTrend> executionTrends;
  
  /** The performance metrics by job name */
  private Map<String, JobPerformanceMetrics> performanceByJob;
  
  /** The error distribution by type */
  private Map<String, Long> errorDistribution;
  
  /** The response message */
  private String message;
  
  /** The error message (if any) */
  private String errorMessage;
  
  /** Additional metadata */
  private Map<String, Object> metadata;
  
  /**
   * Execution trend data point.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ExecutionTrend {
    private LocalDateTime timestamp;
    private Long executionCount;
    private Long successCount;
    private Long failureCount;
    private Double averageExecutionTime;
    private Double averageProcessingRate;
  }
  
  /**
   * Job performance metrics.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class JobPerformanceMetrics {
    private String jobName;
    private Long executionCount;
    private Long successCount;
    private Long failureCount;
    private Double successRate;
    private Double averageExecutionTime;
    private Double averageProcessingRate;
    private Long totalRecordsProcessed;
    private Double averageMemoryUsage;
    private Double averageCpuUsage;
  }
}
