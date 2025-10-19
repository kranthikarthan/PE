package com.payments.batch.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Request DTO for batch job scheduling.
 *
 * <p>This DTO encapsulates the parameters required to schedule a batch job execution,
 * including scheduling details, execution parameters, and notification settings.
 *
 * @since PE-405
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobScheduleRequest {
  
  /** The name of the job to schedule */
  @NotBlank(message = "Job name is required")
  private String jobName;
  
  /** The cron expression for scheduling */
  private String cronExpression;
  
  /** The scheduled execution time */
  private LocalDateTime scheduledTime;
  
  /** The time zone for scheduling */
  private String timeZone;
  
  /** Optional parameters for the job execution */
  private Map<String, Object> parameters;
  
  /** Optional configuration overrides */
  private Map<String, Object> configuration;
  
  /** Whether to run the job asynchronously */
  private Boolean async;
  
  /** Priority level for the job execution */
  private Integer priority;
  
  /** Tenant ID for multi-tenant execution */
  private String tenantId;
  
  /** Business unit ID for execution context */
  private String businessUnitId;
  
  /** Optional description for the scheduled job */
  private String description;
  
  /** Whether to validate parameters before execution */
  private Boolean validateParameters;
  
  /** Maximum execution time in minutes */
  private Integer maxExecutionTime;
  
  /** Whether to send notifications on completion */
  private Boolean sendNotifications;
  
  /** Notification recipients */
  private String[] notificationRecipients;
  
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
  
  /** Whether to run the job immediately after scheduling */
  private Boolean runImmediately;
}
