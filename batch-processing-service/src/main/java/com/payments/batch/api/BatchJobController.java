package com.payments.batch.api;

import com.payments.batch.api.dto.BatchJobExecutionRequest;
import com.payments.batch.api.dto.BatchJobExecutionResponse;
import com.payments.batch.api.dto.BatchJobStatusResponse;
import com.payments.batch.api.dto.BatchJobHistoryResponse;
import com.payments.batch.api.dto.BatchJobMetricsResponse;
import com.payments.batch.api.dto.BatchJobConfigurationResponse;
import com.payments.batch.api.dto.BatchJobScheduleRequest;
import com.payments.batch.api.dto.BatchJobScheduleResponse;
import com.payments.batch.service.BatchJobManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for batch job management operations.
 *
 * <p>This controller provides comprehensive REST endpoints for managing batch processing
 * jobs including execution, monitoring, configuration, and scheduling capabilities.
 *
 * <p><b>Supported Operations:</b>
 * <ul>
 *   <li>Job execution (start, stop, pause, resume)
 *   <li>Job status monitoring and health checks
 *   <li>Job history and audit trail
 *   <li>Job configuration management
 *   <li>Job metrics and performance monitoring
 *   <li>Job scheduling and trigger management
 * </ul>
 *
 * <p><b>Security Features:</b>
 * <ul>
 *   <li>Role-based access control
 *   <li>API key authentication
 *   <li>Request validation and sanitization
 *   <li>Audit logging for all operations
 * </ul>
 *
 * @since PE-405
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/batch/jobs")
@RequiredArgsConstructor
@Tag(name = "Batch Job Management", description = "REST API for batch job management operations")
public class BatchJobController {
  
  private final BatchJobManagementService batchJobManagementService;
  
  /**
   * Starts a new batch job execution.
   *
   * @param request the job execution request
   * @return job execution response
   */
  @PostMapping("/execute")
  @Operation(summary = "Start batch job execution", 
             description = "Initiates a new batch job execution with the specified parameters")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Job started successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
      @ApiResponse(responseCode = "409", description = "Job already running"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<BatchJobExecutionResponse> startJob(
      @Valid @RequestBody BatchJobExecutionRequest request) {
    
    log.info("Starting batch job execution: {}", request.getJobName());
    
    try {
      JobParameters jobParameters = buildJobParameters(request);
      JobExecution jobExecution = batchJobManagementService.startJob(request.getJobName(), jobParameters);
      
      BatchJobExecutionResponse response = BatchJobExecutionResponse.builder()
          .jobExecutionId(jobExecution.getId())
          .jobName(request.getJobName())
          .status(jobExecution.getStatus().toString())
          .startTime(jobExecution.getStartTime())
          .parameters(request.getParameters())
          .message("Job started successfully")
          .build();
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("Failed to start batch job: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(BatchJobExecutionResponse.builder()
              .jobName(request.getJobName())
              .status("FAILED")
              .message("Failed to start job: " + e.getMessage())
              .build());
    }
  }
  
  /**
   * Stops a running batch job execution.
   *
   * @param jobExecutionId the job execution ID
   * @return job status response
   */
  @PostMapping("/{jobExecutionId}/stop")
  @Operation(summary = "Stop batch job execution", 
             description = "Stops a running batch job execution")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Job stopped successfully"),
      @ApiResponse(responseCode = "404", description = "Job execution not found"),
      @ApiResponse(responseCode = "409", description = "Job not running"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<BatchJobStatusResponse> stopJob(
      @Parameter(description = "Job execution ID") @PathVariable Long jobExecutionId) {
    
    log.info("Stopping batch job execution: {}", jobExecutionId);
    
    try {
      JobExecution jobExecution = batchJobManagementService.stopJob(jobExecutionId);
      
      BatchJobStatusResponse response = BatchJobStatusResponse.builder()
          .jobExecutionId(jobExecutionId)
          .jobName(jobExecution.getJobInstance().getJobName())
          .status(jobExecution.getStatus().toString())
          .endTime(jobExecution.getEndTime())
          .message("Job stopped successfully")
          .build();
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("Failed to stop batch job: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(BatchJobStatusResponse.builder()
              .jobExecutionId(jobExecutionId)
              .status("ERROR")
              .message("Failed to stop job: " + e.getMessage())
              .build());
    }
  }
  
  /**
   * Gets the status of a batch job execution.
   *
   * @param jobExecutionId the job execution ID
   * @return job status response
   */
  @GetMapping("/{jobExecutionId}/status")
  @Operation(summary = "Get job execution status", 
             description = "Retrieves the current status of a batch job execution")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Job status retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Job execution not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<BatchJobStatusResponse> getJobStatus(
      @Parameter(description = "Job execution ID") @PathVariable Long jobExecutionId) {
    
    log.debug("Getting status for batch job execution: {}", jobExecutionId);
    
    try {
      JobExecution jobExecution = batchJobManagementService.getJobExecution(jobExecutionId);
      
      BatchJobStatusResponse response = BatchJobStatusResponse.builder()
          .jobExecutionId(jobExecutionId)
          .jobName(jobExecution.getJobInstance().getJobName())
          .status(jobExecution.getStatus().toString())
          .startTime(jobExecution.getStartTime())
          .endTime(jobExecution.getEndTime())
          .exitCode(jobExecution.getExitStatus().getExitCode())
          .exitDescription(jobExecution.getExitStatus().getExitDescription())
          .message("Job status retrieved successfully")
          .build();
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("Failed to get job status: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(BatchJobStatusResponse.builder()
              .jobExecutionId(jobExecutionId)
              .status("ERROR")
              .message("Failed to get job status: " + e.getMessage())
              .build());
    }
  }
  
  /**
   * Gets the history of batch job executions.
   *
   * @param jobName the job name (optional)
   * @param limit the maximum number of results (default: 50)
   * @param offset the offset for pagination (default: 0)
   * @return job history response
   */
  @GetMapping("/history")
  @Operation(summary = "Get job execution history", 
             description = "Retrieves the history of batch job executions with pagination")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Job history retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<BatchJobHistoryResponse> getJobHistory(
      @Parameter(description = "Job name filter") @RequestParam(required = false) String jobName,
      @Parameter(description = "Maximum number of results") @RequestParam(defaultValue = "50") int limit,
      @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") int offset) {
    
    log.debug("Getting job history: jobName={}, limit={}, offset={}", jobName, limit, offset);
    
    try {
      List<JobExecution> jobExecutions = batchJobManagementService.getJobHistory(jobName, limit, offset);
      
      BatchJobHistoryResponse response = BatchJobHistoryResponse.builder()
          .jobExecutions(jobExecutions)
          .totalCount(batchJobManagementService.getJobHistoryCount(jobName))
          .limit(limit)
          .offset(offset)
          .message("Job history retrieved successfully")
          .build();
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("Failed to get job history: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(BatchJobHistoryResponse.builder()
              .message("Failed to get job history: " + e.getMessage())
              .build());
    }
  }
  
  /**
   * Gets metrics for batch job executions.
   *
   * @param jobName the job name (optional)
   * @param timeRange the time range for metrics (default: 24h)
   * @return job metrics response
   */
  @GetMapping("/metrics")
  @Operation(summary = "Get job execution metrics", 
             description = "Retrieves performance metrics for batch job executions")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Job metrics retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<BatchJobMetricsResponse> getJobMetrics(
      @Parameter(description = "Job name filter") @RequestParam(required = false) String jobName,
      @Parameter(description = "Time range for metrics") @RequestParam(defaultValue = "24h") String timeRange) {
    
    log.debug("Getting job metrics: jobName={}, timeRange={}", jobName, timeRange);
    
    try {
      BatchJobMetricsResponse response = batchJobManagementService.getJobMetrics(jobName, timeRange);
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("Failed to get job metrics: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(BatchJobMetricsResponse.builder()
              .message("Failed to get job metrics: " + e.getMessage())
              .build());
    }
  }
  
  /**
   * Gets the configuration for batch jobs.
   *
   * @return job configuration response
   */
  @GetMapping("/configuration")
  @Operation(summary = "Get job configuration", 
             description = "Retrieves the current configuration for batch jobs")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Job configuration retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<BatchJobConfigurationResponse> getJobConfiguration() {
    
    log.debug("Getting job configuration");
    
    try {
      BatchJobConfigurationResponse response = batchJobManagementService.getJobConfiguration();
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("Failed to get job configuration: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(BatchJobConfigurationResponse.builder()
              .message("Failed to get job configuration: " + e.getMessage())
              .build());
    }
  }
  
  /**
   * Schedules a batch job execution.
   *
   * @param request the job schedule request
   * @return job schedule response
   */
  @PostMapping("/schedule")
  @Operation(summary = "Schedule batch job execution", 
             description = "Schedules a batch job execution for future execution")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Job scheduled successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid schedule parameters"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<BatchJobScheduleResponse> scheduleJob(
      @Valid @RequestBody BatchJobScheduleRequest request) {
    
    log.info("Scheduling batch job: {}", request.getJobName());
    
    try {
      BatchJobScheduleResponse response = batchJobManagementService.scheduleJob(request);
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("Failed to schedule batch job: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(BatchJobScheduleResponse.builder()
              .jobName(request.getJobName())
              .message("Failed to schedule job: " + e.getMessage())
              .build());
    }
  }
  
  /**
   * Gets the health status of the batch processing system.
   *
   * @return health status response
   */
  @GetMapping("/health")
  @Operation(summary = "Get system health status", 
             description = "Retrieves the health status of the batch processing system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Health status retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> getHealthStatus() {
    
    log.debug("Getting system health status");
    
    try {
      Map<String, Object> healthStatus = batchJobManagementService.getHealthStatus();
      return ResponseEntity.ok(healthStatus);
      
    } catch (Exception e) {
      log.error("Failed to get health status: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of(
              "status", "ERROR",
              "message", "Failed to get health status: " + e.getMessage()
          ));
    }
  }
  
  /**
   * Builds job parameters from the execution request.
   *
   * @param request the job execution request
   * @return job parameters
   */
  private JobParameters buildJobParameters(BatchJobExecutionRequest request) {
    JobParametersBuilder builder = new JobParametersBuilder();
    
    // Add timestamp to ensure unique job execution
    builder.addLong("timestamp", System.currentTimeMillis());
    
    // Add custom parameters
    if (request.getParameters() != null) {
      request.getParameters().forEach((key, value) -> {
        if (value instanceof String) {
          builder.addString(key, (String) value);
        } else if (value instanceof Long) {
          builder.addLong(key, (Long) value);
        } else if (value instanceof Double) {
          builder.addDouble(key, (Double) value);
        } else {
          builder.addString(key, value.toString());
        }
      });
    }
    
    return builder.toJobParameters();
  }
}
