package com.payments.batch.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for batch job configuration.
 *
 * <p>This DTO encapsulates the current configuration settings for batch job processing,
 * including job definitions, parameters, and system settings.
 *
 * @since PE-405
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobConfigurationResponse {
  
  /** The available job names */
  private java.util.List<String> availableJobs;
  
  /** The job definitions */
  private Map<String, JobDefinition> jobDefinitions;
  
  /** The system configuration */
  private SystemConfiguration systemConfiguration;
  
  /** The default parameters for jobs */
  private Map<String, Object> defaultParameters;
  
  /** The response message */
  private String message;
  
  /** The error message (if any) */
  private String errorMessage;
  
  /** Additional metadata */
  private Map<String, Object> metadata;
  
  /**
   * Job definition information.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class JobDefinition {
    private String jobName;
    private String description;
    private String version;
    private java.util.List<String> steps;
    private Map<String, Object> parameters;
    private Map<String, Object> configuration;
    private Boolean enabled;
    private String cronExpression;
    private Integer priority;
    private Integer timeout;
    private Integer retryCount;
    private Boolean async;
    private String tenantId;
    private String businessUnitId;
  }
  
  /**
   * System configuration information.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SystemConfiguration {
    private Integer maxConcurrentJobs;
    private Integer maxRetryAttempts;
    private Integer defaultTimeout;
    private Integer defaultChunkSize;
    private Integer defaultPageSize;
    private Boolean enableNotifications;
    private Boolean enableMetrics;
    private Boolean enableAuditLogging;
    private String defaultTenantId;
    private String defaultBusinessUnitId;
    private Map<String, Object> globalParameters;
    private Map<String, Object> systemSettings;
  }
}
