package com.payments.batch.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for batch job execution.
 *
 * <p>This DTO encapsulates the parameters required to start a new batch job execution,
 * including the job name, execution parameters, and optional configuration overrides.
 *
 * @since PE-405
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobExecutionRequest {
  
  /** The name of the job to execute */
  @NotBlank(message = "Job name is required")
  private String jobName;
  
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
  
  /** Optional description for the job execution */
  private String description;
  
  /** Whether to validate parameters before execution */
  private Boolean validateParameters;
  
  /** Maximum execution time in minutes */
  private Integer maxExecutionTime;
  
  /** Whether to send notifications on completion */
  private Boolean sendNotifications;
  
  /** Notification recipients */
  private String[] notificationRecipients;
}
