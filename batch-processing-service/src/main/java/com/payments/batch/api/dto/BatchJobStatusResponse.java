package com.payments.batch.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for batch job status.
 *
 * <p>This DTO encapsulates the current status information for a batch job execution,
 * including execution details, progress, and performance metrics.
 *
 * @since PE-405
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobStatusResponse {
  
  /** The job execution ID */
  private Long jobExecutionId;
  
  /** The name of the job */
  private String jobName;
  
  /** The current status of the job execution */
  private String status;
  
  /** The start time of the job execution */
  private LocalDateTime startTime;
  
  /** The end time of the job execution (if completed) */
  private LocalDateTime endTime;
  
  /** The last updated time */
  private LocalDateTime lastUpdated;
  
  /** The exit code of the job execution */
  private String exitCode;
  
  /** The exit description of the job execution */
  private String exitDescription;
  
  /** The progress percentage of the job execution */
  private Double progress;
  
  /** The estimated completion time */
  private LocalDateTime estimatedCompletionTime;
  
  /** The number of records processed */
  private Long recordsProcessed;
  
  /** The number of records failed */
  private Long recordsFailed;
  
  /** The number of records skipped */
  private Long recordsSkipped;
  
  /** The total number of records */
  private Long totalRecords;
  
  /** The processing rate (records per second) */
  private Double processingRate;
  
  /** The memory usage in MB */
  private Double memoryUsage;
  
  /** The CPU usage percentage */
  private Double cpuUsage;
  
  /** The current step being executed */
  private String currentStep;
  
  /** The step progress percentage */
  private Double stepProgress;
  
  /** The number of steps completed */
  private Integer stepsCompleted;
  
  /** The total number of steps */
  private Integer totalSteps;
  
  /** The execution duration in seconds */
  private Long executionDuration;
  
  /** The remaining time in seconds */
  private Long remainingTime;
  
  /** The response message */
  private String message;
  
  /** The error message (if any) */
  private String errorMessage;
  
  /** The stack trace (if any) */
  private String stackTrace;
  
  /** Additional status metadata */
  private Map<String, Object> metadata;
  
  /** Whether the job is running */
  private Boolean isRunning;
  
  /** Whether the job is completed */
  private Boolean isCompleted;
  
  /** Whether the job failed */
  private Boolean isFailed;
  
  /** Whether the job was stopped */
  private Boolean isStopped;
}
