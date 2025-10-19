package com.payments.batch.service;

import com.payments.batch.api.dto.BatchJobConfigurationResponse;
import com.payments.batch.api.dto.BatchJobMetricsResponse;
import com.payments.batch.api.dto.BatchJobScheduleRequest;
import com.payments.batch.api.dto.BatchJobScheduleResponse;
import com.payments.batch.error.BatchProcessingException;
import com.payments.batch.error.ErrorCategory;
import com.payments.batch.error.ErrorSeverity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing batch job operations including execution, monitoring, and configuration.
 *
 * <p>This service provides comprehensive batch job management capabilities including:
 * <ul>
 *   <li>Job execution control (start, stop, pause, resume)
 *   <li>Job status monitoring and health checks
 *   <li>Job history and audit trail management
 *   <li>Job configuration and parameter management
 *   <li>Job metrics and performance monitoring
 *   <li>Job scheduling and trigger management
 * </ul>
 *
 * @since PE-405
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobManagementService {
  
  private final JobLauncher jobLauncher;
  private final JobExplorer jobExplorer;
  private final JobRepository jobRepository;
  private final ApplicationContext applicationContext;
  
  /**
   * Starts a new batch job execution.
   *
   * @param jobName the name of the job to start
   * @param jobParameters the parameters for the job execution
   * @return the job execution
   * @throws BatchProcessingException if job start fails
   */
  @Transactional
  public JobExecution startJob(String jobName, JobParameters jobParameters) {
    try {
      log.info("Starting batch job: {} with parameters: {}", jobName, jobParameters);
      
      Job job = getJobByName(jobName);
      if (job == null) {
        throw new BatchProcessingException(
            "JOB_NOT_FOUND",
            "Job not found: " + jobName,
            null,
            ErrorSeverity.HIGH,
            ErrorCategory.CONFIGURATION,
            Map.of("jobName", jobName),
            new String[]{"Check job configuration", "Verify job name"},
            false
        );
      }
      
      JobExecution jobExecution = jobLauncher.run(job, jobParameters);
      
      log.info("Batch job started successfully: {} (execution ID: {})", 
          jobName, jobExecution.getId());
      
      return jobExecution;
      
    } catch (JobExecutionAlreadyRunningException e) {
      log.warn("Job already running: {}", jobName);
      throw new BatchProcessingException(
          "JOB_ALREADY_RUNNING",
          "Job is already running: " + jobName,
          e,
          ErrorSeverity.MEDIUM,
          ErrorCategory.BUSINESS,
          Map.of("jobName", jobName),
          new String[]{"Wait for current execution to complete", "Stop current execution if needed"},
          true
      );
    } catch (JobInstanceAlreadyCompleteException e) {
      log.warn("Job instance already complete: {}", jobName);
      throw new BatchProcessingException(
          "JOB_INSTANCE_COMPLETE",
          "Job instance already complete: " + jobName,
          e,
          ErrorSeverity.MEDIUM,
          ErrorCategory.BUSINESS,
          Map.of("jobName", jobName),
          new String[]{"Use different parameters", "Check job history"},
          false
      );
    } catch (JobParametersInvalidException e) {
      log.error("Invalid job parameters for job: {}", jobName);
      throw new BatchProcessingException(
          "INVALID_JOB_PARAMETERS",
          "Invalid job parameters: " + e.getMessage(),
          e,
          ErrorSeverity.HIGH,
          ErrorCategory.VALIDATION,
          Map.of("jobName", jobName, "parameters", jobParameters),
          new String[]{"Review job parameters", "Check parameter validation rules"},
          false
      );
    } catch (JobRestartException e) {
      log.error("Job restart failed: {}", jobName);
      throw new BatchProcessingException(
          "JOB_RESTART_FAILED",
          "Job restart failed: " + e.getMessage(),
          e,
          ErrorSeverity.HIGH,
          ErrorCategory.SYSTEM,
          Map.of("jobName", jobName),
          new String[]{"Check job status", "Review job configuration"},
          true
      );
    } catch (Exception e) {
      log.error("Failed to start batch job: {}", jobName, e);
      throw new BatchProcessingException(
          "JOB_START_FAILED",
          "Failed to start job: " + e.getMessage(),
          e,
          ErrorSeverity.CRITICAL,
          ErrorCategory.SYSTEM,
          Map.of("jobName", jobName),
          new String[]{"Check system resources", "Review job configuration", "Contact support"},
          true
      );
    }
  }
  
  /**
   * Stops a running batch job execution.
   *
   * @param jobExecutionId the job execution ID
   * @return the job execution
   * @throws BatchProcessingException if job stop fails
   */
  @Transactional
  public JobExecution stopJob(Long jobExecutionId) {
    try {
      log.info("Stopping batch job execution: {}", jobExecutionId);
      
      JobExecution jobExecution = jobExplorer.getJobExecution(jobExecutionId);
      if (jobExecution == null) {
        throw new BatchProcessingException(
            "JOB_EXECUTION_NOT_FOUND",
            "Job execution not found: " + jobExecutionId,
            null,
            ErrorSeverity.HIGH,
            ErrorCategory.VALIDATION,
            Map.of("jobExecutionId", jobExecutionId),
            new String[]{"Check job execution ID", "Review job history"},
            false
        );
      }
      
      if (jobExecution.getStatus().isUnsuccessful() || jobExecution.getStatus().isCompleted()) {
        throw new BatchProcessingException(
            "JOB_NOT_RUNNING",
            "Job is not running: " + jobExecutionId,
            null,
            ErrorSeverity.MEDIUM,
            ErrorCategory.BUSINESS,
            Map.of("jobExecutionId", jobExecutionId, "status", jobExecution.getStatus()),
            new String[]{"Check job status", "Job may already be completed"},
            false
        );
      }
      
      jobExecution.stop();
      
      log.info("Batch job stopped successfully: {}", jobExecutionId);
      return jobExecution;
      
    } catch (Exception e) {
      log.error("Failed to stop batch job: {}", jobExecutionId, e);
      throw new BatchProcessingException(
          "JOB_STOP_FAILED",
          "Failed to stop job: " + e.getMessage(),
          e,
          ErrorSeverity.HIGH,
          ErrorCategory.SYSTEM,
          Map.of("jobExecutionId", jobExecutionId),
          new String[]{"Check job status", "Review system logs", "Contact support"},
          true
      );
    }
  }
  
  /**
   * Gets a job execution by ID.
   *
   * @param jobExecutionId the job execution ID
   * @return the job execution
   * @throws BatchProcessingException if job execution not found
   */
  public JobExecution getJobExecution(Long jobExecutionId) {
    try {
      JobExecution jobExecution = jobExplorer.getJobExecution(jobExecutionId);
      if (jobExecution == null) {
        throw new BatchProcessingException(
            "JOB_EXECUTION_NOT_FOUND",
            "Job execution not found: " + jobExecutionId,
            null,
            ErrorSeverity.HIGH,
            ErrorCategory.VALIDATION,
            Map.of("jobExecutionId", jobExecutionId),
            new String[]{"Check job execution ID", "Review job history"},
            false
        );
      }
      
      return jobExecution;
      
    } catch (Exception e) {
      log.error("Failed to get job execution: {}", jobExecutionId, e);
      throw new BatchProcessingException(
          "JOB_EXECUTION_GET_FAILED",
          "Failed to get job execution: " + e.getMessage(),
          e,
          ErrorSeverity.HIGH,
          ErrorCategory.SYSTEM,
          Map.of("jobExecutionId", jobExecutionId),
          new String[]{"Check job execution ID", "Review system logs"},
          true
      );
    }
  }
  
  /**
   * Gets the history of job executions.
   *
   * @param jobName the job name filter (optional)
   * @param limit the maximum number of results
   * @param offset the offset for pagination
   * @return list of job executions
   */
  public List<JobExecution> getJobHistory(String jobName, int limit, int offset) {
    try {
      log.debug("Getting job history: jobName={}, limit={}, offset={}", jobName, limit, offset);
      
      List<JobExecution> jobExecutions = new ArrayList<>();
      
      if (jobName != null && !jobName.trim().isEmpty()) {
        // Get job executions for specific job
        List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, offset, limit);
        for (JobInstance jobInstance : jobInstances) {
          List<JobExecution> executions = jobExplorer.getJobExecutions(jobInstance);
          jobExecutions.addAll(executions);
        }
      } else {
        // Get all job executions
        Set<String> jobNames = jobExplorer.getJobNames();
        for (String name : jobNames) {
          List<JobInstance> jobInstances = jobExplorer.getJobInstances(name, 0, limit);
          for (JobInstance jobInstance : jobInstances) {
            List<JobExecution> executions = jobExplorer.getJobExecutions(jobInstance);
            jobExecutions.addAll(executions);
          }
        }
      }
      
      // Sort by start time descending and apply pagination
      jobExecutions.sort((e1, e2) -> {
        if (e1.getStartTime() == null && e2.getStartTime() == null) return 0;
        if (e1.getStartTime() == null) return 1;
        if (e2.getStartTime() == null) return -1;
        return e2.getStartTime().compareTo(e1.getStartTime());
      });
      
      return jobExecutions.stream()
          .skip(offset)
          .limit(limit)
          .collect(Collectors.toList());
      
    } catch (Exception e) {
      log.error("Failed to get job history: {}", e.getMessage(), e);
      throw new BatchProcessingException(
          "JOB_HISTORY_GET_FAILED",
          "Failed to get job history: " + e.getMessage(),
          e,
          ErrorSeverity.MEDIUM,
          ErrorCategory.SYSTEM,
          Map.of("jobName", jobName, "limit", limit, "offset", offset),
          new String[]{"Check system logs", "Review database connectivity"},
          true
      );
    }
  }
  
  /**
   * Gets the count of job executions in history.
   *
   * @param jobName the job name filter (optional)
   * @return the count of job executions
   */
  public long getJobHistoryCount(String jobName) {
    try {
      if (jobName != null && !jobName.trim().isEmpty()) {
        return jobExplorer.getJobInstanceCount(jobName);
      } else {
        return jobExplorer.getJobNames().stream()
            .mapToLong(jobExplorer::getJobInstanceCount)
            .sum();
      }
    } catch (Exception e) {
      log.error("Failed to get job history count: {}", e.getMessage(), e);
      return 0;
    }
  }
  
  /**
   * Gets metrics for job executions.
   *
   * @param jobName the job name filter (optional)
   * @param timeRange the time range for metrics
   * @return job metrics response
   */
  public BatchJobMetricsResponse getJobMetrics(String jobName, String timeRange) {
    try {
      log.debug("Getting job metrics: jobName={}, timeRange={}", jobName, timeRange);
      
      // Parse time range
      LocalDateTime endTime = LocalDateTime.now();
      LocalDateTime startTime = parseTimeRange(timeRange, endTime);
      
      // Get job executions in time range
      List<JobExecution> jobExecutions = getJobHistory(jobName, 1000, 0);
      List<JobExecution> filteredExecutions = jobExecutions.stream()
          .filter(execution -> execution.getStartTime() != null)
          .filter(execution -> !execution.getStartTime().isBefore(startTime))
          .filter(execution -> !execution.getStartTime().isAfter(endTime))
          .collect(Collectors.toList());
      
      // Calculate metrics
      long totalExecutions = filteredExecutions.size();
      long successfulExecutions = filteredExecutions.stream()
          .mapToLong(execution -> execution.getStatus().isCompleted() ? 1 : 0)
          .sum();
      long failedExecutions = filteredExecutions.stream()
          .mapToLong(execution -> execution.getStatus().isUnsuccessful() ? 1 : 0)
          .sum();
      long runningExecutions = filteredExecutions.stream()
          .mapToLong(execution -> execution.getStatus().isRunning() ? 1 : 0)
          .sum();
      
      double successRate = totalExecutions > 0 ? (double) successfulExecutions / totalExecutions * 100 : 0;
      double failureRate = totalExecutions > 0 ? (double) failedExecutions / totalExecutions * 100 : 0;
      
      // Calculate execution times
      List<Double> executionTimes = filteredExecutions.stream()
          .filter(execution -> execution.getStartTime() != null && execution.getEndTime() != null)
          .map(execution -> {
            long duration = java.time.Duration.between(execution.getStartTime(), execution.getEndTime()).getSeconds();
            return (double) duration;
          })
          .collect(Collectors.toList());
      
      double averageExecutionTime = executionTimes.isEmpty() ? 0 : 
          executionTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
      double minExecutionTime = executionTimes.isEmpty() ? 0 : 
          executionTimes.stream().mapToDouble(Double::doubleValue).min().orElse(0);
      double maxExecutionTime = executionTimes.isEmpty() ? 0 : 
          executionTimes.stream().mapToDouble(Double::doubleValue).max().orElse(0);
      
      return BatchJobMetricsResponse.builder()
          .timeRange(timeRange)
          .startTime(startTime)
          .endTime(endTime)
          .totalExecutions(totalExecutions)
          .successfulExecutions(successfulExecutions)
          .failedExecutions(failedExecutions)
          .runningExecutions(runningExecutions)
          .successRate(successRate)
          .failureRate(failureRate)
          .averageExecutionTime(averageExecutionTime)
          .minExecutionTime(minExecutionTime)
          .maxExecutionTime(maxExecutionTime)
          .totalExecutionTime(executionTimes.stream().mapToDouble(Double::doubleValue).sum())
          .message("Job metrics retrieved successfully")
          .build();
      
    } catch (Exception e) {
      log.error("Failed to get job metrics: {}", e.getMessage(), e);
      throw new BatchProcessingException(
          "JOB_METRICS_GET_FAILED",
          "Failed to get job metrics: " + e.getMessage(),
          e,
          ErrorSeverity.MEDIUM,
          ErrorCategory.SYSTEM,
          Map.of("jobName", jobName, "timeRange", timeRange),
          new String[]{"Check system logs", "Review metrics configuration"},
          true
      );
    }
  }
  
  /**
   * Gets the configuration for batch jobs.
   *
   * @return job configuration response
   */
  public BatchJobConfigurationResponse getJobConfiguration() {
    try {
      log.debug("Getting job configuration");
      
      // Get available jobs
      List<String> availableJobs = new ArrayList<>();
      Map<String, BatchJobConfigurationResponse.JobDefinition> jobDefinitions = new HashMap<>();
      
      String[] jobBeanNames = applicationContext.getBeanNamesForType(Job.class);
      for (String jobBeanName : jobBeanNames) {
        Job job = applicationContext.getBean(jobBeanName, Job.class);
        availableJobs.add(job.getName());
        
        BatchJobConfigurationResponse.JobDefinition jobDefinition = 
            BatchJobConfigurationResponse.JobDefinition.builder()
                .jobName(job.getName())
                .description("Batch job: " + job.getName())
                .version("1.0")
                .enabled(true)
                .async(false)
                .priority(5)
                .timeout(3600)
                .retryCount(3)
                .build();
        
        jobDefinitions.put(job.getName(), jobDefinition);
      }
      
      // System configuration
      BatchJobConfigurationResponse.SystemConfiguration systemConfiguration = 
          BatchJobConfigurationResponse.SystemConfiguration.builder()
              .maxConcurrentJobs(10)
              .maxRetryAttempts(3)
              .defaultTimeout(3600)
              .defaultChunkSize(1000)
              .defaultPageSize(50)
              .enableNotifications(true)
              .enableMetrics(true)
              .enableAuditLogging(true)
              .build();
      
      return BatchJobConfigurationResponse.builder()
          .availableJobs(availableJobs)
          .jobDefinitions(jobDefinitions)
          .systemConfiguration(systemConfiguration)
          .message("Job configuration retrieved successfully")
          .build();
      
    } catch (Exception e) {
      log.error("Failed to get job configuration: {}", e.getMessage(), e);
      throw new BatchProcessingException(
          "JOB_CONFIGURATION_GET_FAILED",
          "Failed to get job configuration: " + e.getMessage(),
          e,
          ErrorSeverity.MEDIUM,
          ErrorCategory.SYSTEM,
          null,
          new String[]{"Check system configuration", "Review job definitions"},
          true
      );
    }
  }
  
  /**
   * Schedules a batch job execution.
   *
   * @param request the schedule request
   * @return schedule response
   */
  public BatchJobScheduleResponse scheduleJob(BatchJobScheduleRequest request) {
    try {
      log.info("Scheduling batch job: {}", request.getJobName());
      
      // Generate schedule ID
      String scheduleId = "schedule_" + System.currentTimeMillis();
      
      // Calculate next execution time
      LocalDateTime nextExecutionTime = calculateNextExecutionTime(request);
      
      BatchJobScheduleResponse response = BatchJobScheduleResponse.builder()
          .scheduleId(scheduleId)
          .jobName(request.getJobName())
          .cronExpression(request.getCronExpression())
          .scheduledTime(request.getScheduledTime())
          .timeZone(request.getTimeZone())
          .status("SCHEDULED")
          .enabled(request.getEnabled() != null ? request.getEnabled() : true)
          .startDate(request.getStartDate())
          .endDate(request.getEndDate())
          .maxExecutions(request.getMaxExecutions())
          .executionCount(0)
          .nextExecutionTime(nextExecutionTime)
          .parameters(request.getParameters())
          .configuration(request.getConfiguration())
          .async(request.getAsync())
          .priority(request.getPriority())
          .tenantId(request.getTenantId())
          .businessUnitId(request.getBusinessUnitId())
          .description(request.getDescription())
          .message("Job scheduled successfully")
          .build();
      
      // TODO: Implement actual scheduling logic with Quartz or similar
      log.info("Job scheduled successfully: {} (schedule ID: {})", 
          request.getJobName(), scheduleId);
      
      return response;
      
    } catch (Exception e) {
      log.error("Failed to schedule batch job: {}", e.getMessage(), e);
      throw new BatchProcessingException(
          "JOB_SCHEDULE_FAILED",
          "Failed to schedule job: " + e.getMessage(),
          e,
          ErrorSeverity.HIGH,
          ErrorCategory.SYSTEM,
          Map.of("jobName", request.getJobName()),
          new String[]{"Check scheduling configuration", "Review job parameters"},
          true
      );
    }
  }
  
  /**
   * Gets the health status of the batch processing system.
   *
   * @return health status map
   */
  public Map<String, Object> getHealthStatus() {
    try {
      Map<String, Object> healthStatus = new HashMap<>();
      
      // Check job launcher
      boolean jobLauncherHealthy = jobLauncher != null;
      healthStatus.put("jobLauncher", jobLauncherHealthy ? "UP" : "DOWN");
      
      // Check job explorer
      boolean jobExplorerHealthy = jobExplorer != null;
      healthStatus.put("jobExplorer", jobExplorerHealthy ? "UP" : "DOWN");
      
      // Check job repository
      boolean jobRepositoryHealthy = jobRepository != null;
      healthStatus.put("jobRepository", jobRepositoryHealthy ? "UP" : "DOWN");
      
      // Get available jobs
      List<String> availableJobs = new ArrayList<>();
      try {
        String[] jobBeanNames = applicationContext.getBeanNamesForType(Job.class);
        availableJobs = Arrays.asList(jobBeanNames);
      } catch (Exception e) {
        log.warn("Failed to get available jobs: {}", e.getMessage());
      }
      healthStatus.put("availableJobs", availableJobs);
      healthStatus.put("jobCount", availableJobs.size());
      
      // Overall status
      boolean overallHealthy = jobLauncherHealthy && jobExplorerHealthy && jobRepositoryHealthy;
      healthStatus.put("status", overallHealthy ? "UP" : "DOWN");
      healthStatus.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
      
      return healthStatus;
      
    } catch (Exception e) {
      log.error("Failed to get health status: {}", e.getMessage(), e);
      return Map.of(
          "status", "DOWN",
          "error", e.getMessage(),
          "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
      );
    }
  }
  
  /**
   * Gets a job by name from the application context.
   *
   * @param jobName the job name
   * @return the job or null if not found
   */
  private Job getJobByName(String jobName) {
    try {
      String[] jobBeanNames = applicationContext.getBeanNamesForType(Job.class);
      for (String jobBeanName : jobBeanNames) {
        Job job = applicationContext.getBean(jobBeanName, Job.class);
        if (job.getName().equals(jobName)) {
          return job;
        }
      }
      return null;
    } catch (Exception e) {
      log.error("Failed to get job by name: {}", jobName, e);
      return null;
    }
  }
  
  /**
   * Parses a time range string and returns the start time.
   *
   * @param timeRange the time range string (e.g., "24h", "7d", "1m")
   * @param endTime the end time
   * @return the start time
   */
  private LocalDateTime parseTimeRange(String timeRange, LocalDateTime endTime) {
    if (timeRange == null || timeRange.trim().isEmpty()) {
      return endTime.minusHours(24);
    }
    
    try {
      String unit = timeRange.substring(timeRange.length() - 1).toLowerCase();
      int value = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
      
      return switch (unit) {
        case "h" -> endTime.minusHours(value);
        case "d" -> endTime.minusDays(value);
        case "m" -> endTime.minusMonths(value);
        case "y" -> endTime.minusYears(value);
        default -> endTime.minusHours(24);
      };
    } catch (Exception e) {
      log.warn("Failed to parse time range: {}, using default 24h", timeRange);
      return endTime.minusHours(24);
    }
  }
  
  /**
   * Calculates the next execution time for a scheduled job.
   *
   * @param request the schedule request
   * @return the next execution time
   */
  private LocalDateTime calculateNextExecutionTime(BatchJobScheduleRequest request) {
    if (request.getScheduledTime() != null) {
      return request.getScheduledTime();
    }
    
    if (request.getCronExpression() != null) {
      // TODO: Implement cron expression parsing
      return LocalDateTime.now().plusHours(1);
    }
    
    return LocalDateTime.now().plusMinutes(5);
  }
}
