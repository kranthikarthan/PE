package com.payments.batch.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for batch job execution.
 *
 * <p>This DTO encapsulates the response information for a batch job execution request,
 * including execution details, status, and timing information.
 *
 * @since PE-405
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobExecutionResponse {
  
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
  
  /** The parameters used for the job execution */
  private Map<String, Object> parameters;
  
  /** The configuration used for the job execution */
  private Map<String, Object> configuration;
  
  /** Whether the job is running asynchronously */
  private Boolean async;
  
  /** The priority level of the job execution */
  private Integer priority;
  
  /** The tenant ID for the job execution */
  private String tenantId;
  
  /** The business unit ID for the job execution */
  private String businessUnitId;
  
  /** The description of the job execution */
  private String description;
  
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
  
  /** The response message */
  private String message;
  
  /** The error message (if any) */
  private String errorMessage;
  
  /** The stack trace (if any) */
  private String stackTrace;
  
  /** Additional metadata */
  private Map<String, Object> metadata;
}
