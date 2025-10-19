package com.payments.batch.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for batch job scheduling.
 *
 * <p>This DTO encapsulates the response information for a batch job scheduling request,
 * including schedule details, execution information, and status.
 *
 * @since PE-405
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobScheduleResponse {
  
  /** The schedule ID */
  private String scheduleId;
  
  /** The name of the job */
  private String jobName;
  
  /** The cron expression for scheduling */
  private String cronExpression;
  
  /** The scheduled execution time */
  private LocalDateTime scheduledTime;
  
  /** The time zone for scheduling */
  private String timeZone;
  
  /** The status of the schedule */
  private String status;
  
  /** Whether the schedule is enabled */
  private Boolean enabled;
  
  /** The start date for the schedule */
  private LocalDateTime startDate;
  
  /** The end date for the schedule */
  private LocalDateTime endDate;
  
  /** The maximum number of executions */
  private Integer maxExecutions;
  
  /** The number of executions so far */
  private Integer executionCount;
  
  /** The next execution time */
  private LocalDateTime nextExecutionTime;
  
  /** The last execution time */
  private LocalDateTime lastExecutionTime;
  
  /** The last execution status */
  private String lastExecutionStatus;
  
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
  
  /** The description of the scheduled job */
  private String description;
  
  /** The response message */
  private String message;
  
  /** The error message (if any) */
  private String errorMessage;
  
  /** Additional metadata */
  private Map<String, Object> metadata;
}
