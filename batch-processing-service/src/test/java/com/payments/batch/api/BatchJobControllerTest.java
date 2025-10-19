package com.payments.batch.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.payments.batch.api.dto.BatchJobExecutionRequest;
import com.payments.batch.api.dto.BatchJobExecutionResponse;
import com.payments.batch.api.dto.BatchJobStatusResponse;
import com.payments.batch.api.dto.BatchJobHistoryResponse;
import com.payments.batch.api.dto.BatchJobMetricsResponse;
import com.payments.batch.api.dto.BatchJobConfigurationResponse;
import com.payments.batch.api.dto.BatchJobScheduleRequest;
import com.payments.batch.api.dto.BatchJobScheduleResponse;
import com.payments.batch.service.BatchJobManagementService;
import com.payments.batch.error.BatchProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for BatchJobController.
 *
 * @since PE-405
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Batch Job Controller Tests")
class BatchJobControllerTest {
  
  @Mock
  private BatchJobManagementService batchJobManagementService;
  
  private BatchJobController batchJobController;
  
  @BeforeEach
  void setUp() {
    batchJobController = new BatchJobController(batchJobManagementService);
  }
  
  @Test
  @DisplayName("Should start job execution successfully")
  void shouldStartJobExecutionSuccessfully() {
    // Given
    BatchJobExecutionRequest request = BatchJobExecutionRequest.builder()
        .jobName("paymentProcessingJob")
        .parameters(Map.of("filePath", "/data/payments.csv"))
        .build();
    
    JobExecution mockJobExecution = createMockJobExecution(1L, "paymentProcessingJob");
    when(batchJobManagementService.startJob(anyString(), any(JobParameters.class)))
        .thenReturn(mockJobExecution);
    
    // When
    ResponseEntity<BatchJobExecutionResponse> response = batchJobController.startJob(request);
    
    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1L, response.getBody().getJobExecutionId());
    assertEquals("paymentProcessingJob", response.getBody().getJobName());
    assertEquals("STARTED", response.getBody().getStatus());
    assertTrue(response.getBody().getMessage().contains("successfully"));
  }
  
  @Test
  @DisplayName("Should handle job start failure")
  void shouldHandleJobStartFailure() {
    // Given
    BatchJobExecutionRequest request = BatchJobExecutionRequest.builder()
        .jobName("paymentProcessingJob")
        .build();
    
    when(batchJobManagementService.startJob(anyString(), any(JobParameters.class)))
        .thenThrow(new RuntimeException("Job start failed"));
    
    // When
    ResponseEntity<BatchJobExecutionResponse> response = batchJobController.startJob(request);
    
    // Then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("paymentProcessingJob", response.getBody().getJobName());
    assertEquals("FAILED", response.getBody().getStatus());
    assertTrue(response.getBody().getMessage().contains("Failed to start job"));
  }
  
  @Test
  @DisplayName("Should stop job execution successfully")
  void shouldStopJobExecutionSuccessfully() {
    // Given
    Long jobExecutionId = 1L;
    JobExecution mockJobExecution = createMockJobExecution(jobExecutionId, "paymentProcessingJob");
    when(batchJobManagementService.stopJob(jobExecutionId)).thenReturn(mockJobExecution);
    
    // When
    ResponseEntity<BatchJobStatusResponse> response = batchJobController.stopJob(jobExecutionId);
    
    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(jobExecutionId, response.getBody().getJobExecutionId());
    assertEquals("paymentProcessingJob", response.getBody().getJobName());
    assertTrue(response.getBody().getMessage().contains("successfully"));
  }
  
  @Test
  @DisplayName("Should handle job stop failure")
  void shouldHandleJobStopFailure() {
    // Given
    Long jobExecutionId = 1L;
    when(batchJobManagementService.stopJob(jobExecutionId))
        .thenThrow(new RuntimeException("Job stop failed"));
    
    // When
    ResponseEntity<BatchJobStatusResponse> response = batchJobController.stopJob(jobExecutionId);
    
    // Then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(jobExecutionId, response.getBody().getJobExecutionId());
    assertEquals("ERROR", response.getBody().getStatus());
    assertTrue(response.getBody().getMessage().contains("Failed to stop job"));
  }
  
  @Test
  @DisplayName("Should get job status successfully")
  void shouldGetJobStatusSuccessfully() {
    // Given
    Long jobExecutionId = 1L;
    JobExecution mockJobExecution = createMockJobExecution(jobExecutionId, "paymentProcessingJob");
    when(batchJobManagementService.getJobExecution(jobExecutionId)).thenReturn(mockJobExecution);
    
    // When
    ResponseEntity<BatchJobStatusResponse> response = batchJobController.getJobStatus(jobExecutionId);
    
    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(jobExecutionId, response.getBody().getJobExecutionId());
    assertEquals("paymentProcessingJob", response.getBody().getJobName());
    assertTrue(response.getBody().getMessage().contains("successfully"));
  }
  
  @Test
  @DisplayName("Should handle job status get failure")
  void shouldHandleJobStatusGetFailure() {
    // Given
    Long jobExecutionId = 1L;
    when(batchJobManagementService.getJobExecution(jobExecutionId))
        .thenThrow(new RuntimeException("Job status get failed"));
    
    // When
    ResponseEntity<BatchJobStatusResponse> response = batchJobController.getJobStatus(jobExecutionId);
    
    // Then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(jobExecutionId, response.getBody().getJobExecutionId());
    assertEquals("ERROR", response.getBody().getStatus());
    assertTrue(response.getBody().getMessage().contains("Failed to get job status"));
  }
  
  @Test
  @DisplayName("Should get job history successfully")
  void shouldGetJobHistorySuccessfully() {
    // Given
    String jobName = "paymentProcessingJob";
    int limit = 10;
    int offset = 0;
    
    List<JobExecution> mockJobExecutions = Arrays.asList(
        createMockJobExecution(1L, jobName),
        createMockJobExecution(2L, jobName)
    );
    
    when(batchJobManagementService.getJobHistory(jobName, limit, offset))
        .thenReturn(mockJobExecutions);
    when(batchJobManagementService.getJobHistoryCount(jobName)).thenReturn(2L);
    
    // When
    ResponseEntity<BatchJobHistoryResponse> response = batchJobController.getJobHistory(jobName, limit, offset);
    
    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().getJobExecutions().size());
    assertEquals(2L, response.getBody().getTotalCount());
    assertEquals(limit, response.getBody().getLimit());
    assertEquals(offset, response.getBody().getOffset());
    assertTrue(response.getBody().getMessage().contains("successfully"));
  }
  
  @Test
  @DisplayName("Should handle job history get failure")
  void shouldHandleJobHistoryGetFailure() {
    // Given
    String jobName = "paymentProcessingJob";
    when(batchJobManagementService.getJobHistory(jobName, 50, 0))
        .thenThrow(new RuntimeException("Job history get failed"));
    
    // When
    ResponseEntity<BatchJobHistoryResponse> response = batchJobController.getJobHistory(jobName, 50, 0);
    
    // Then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getMessage().contains("Failed to get job history"));
  }
  
  @Test
  @DisplayName("Should get job metrics successfully")
  void shouldGetJobMetricsSuccessfully() {
    // Given
    String jobName = "paymentProcessingJob";
    String timeRange = "24h";
    
    BatchJobMetricsResponse mockMetrics = BatchJobMetricsResponse.builder()
        .timeRange(timeRange)
        .totalExecutions(10L)
        .successfulExecutions(8L)
        .failedExecutions(2L)
        .successRate(80.0)
        .failureRate(20.0)
        .averageExecutionTime(300.0)
        .message("Job metrics retrieved successfully")
        .build();
    
    when(batchJobManagementService.getJobMetrics(jobName, timeRange)).thenReturn(mockMetrics);
    
    // When
    ResponseEntity<BatchJobMetricsResponse> response = batchJobController.getJobMetrics(jobName, timeRange);
    
    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(timeRange, response.getBody().getTimeRange());
    assertEquals(10L, response.getBody().getTotalExecutions());
    assertEquals(8L, response.getBody().getSuccessfulExecutions());
    assertEquals(2L, response.getBody().getFailedExecutions());
    assertEquals(80.0, response.getBody().getSuccessRate());
    assertEquals(20.0, response.getBody().getFailureRate());
    assertEquals(300.0, response.getBody().getAverageExecutionTime());
  }
  
  @Test
  @DisplayName("Should handle job metrics get failure")
  void shouldHandleJobMetricsGetFailure() {
    // Given
    String jobName = "paymentProcessingJob";
    String timeRange = "24h";
    
    when(batchJobManagementService.getJobMetrics(jobName, timeRange))
        .thenThrow(new RuntimeException("Job metrics get failed"));
    
    // When
    ResponseEntity<BatchJobMetricsResponse> response = batchJobController.getJobMetrics(jobName, timeRange);
    
    // Then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getMessage().contains("Failed to get job metrics"));
  }
  
  @Test
  @DisplayName("Should get job configuration successfully")
  void shouldGetJobConfigurationSuccessfully() {
    // Given
    BatchJobConfigurationResponse mockConfig = BatchJobConfigurationResponse.builder()
        .availableJobs(Arrays.asList("paymentProcessingJob", "reconciliationJob"))
        .message("Job configuration retrieved successfully")
        .build();
    
    when(batchJobManagementService.getJobConfiguration()).thenReturn(mockConfig);
    
    // When
    ResponseEntity<BatchJobConfigurationResponse> response = batchJobController.getJobConfiguration();
    
    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().getAvailableJobs().size());
    assertTrue(response.getBody().getAvailableJobs().contains("paymentProcessingJob"));
    assertTrue(response.getBody().getAvailableJobs().contains("reconciliationJob"));
    assertTrue(response.getBody().getMessage().contains("successfully"));
  }
  
  @Test
  @DisplayName("Should handle job configuration get failure")
  void shouldHandleJobConfigurationGetFailure() {
    // Given
    when(batchJobManagementService.getJobConfiguration())
        .thenThrow(new RuntimeException("Job configuration get failed"));
    
    // When
    ResponseEntity<BatchJobConfigurationResponse> response = batchJobController.getJobConfiguration();
    
    // Then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().getMessage().contains("Failed to get job configuration"));
  }
  
  @Test
  @DisplayName("Should schedule job successfully")
  void shouldScheduleJobSuccessfully() {
    // Given
    BatchJobScheduleRequest request = BatchJobScheduleRequest.builder()
        .jobName("paymentProcessingJob")
        .cronExpression("0 0 2 * * ?")
        .description("Daily payment processing")
        .build();
    
    BatchJobScheduleResponse mockSchedule = BatchJobScheduleResponse.builder()
        .scheduleId("schedule_1234567890")
        .jobName("paymentProcessingJob")
        .cronExpression("0 0 2 * * ?")
        .status("SCHEDULED")
        .enabled(true)
        .message("Job scheduled successfully")
        .build();
    
    when(batchJobManagementService.scheduleJob(request)).thenReturn(mockSchedule);
    
    // When
    ResponseEntity<BatchJobScheduleResponse> response = batchJobController.scheduleJob(request);
    
    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("schedule_1234567890", response.getBody().getScheduleId());
    assertEquals("paymentProcessingJob", response.getBody().getJobName());
    assertEquals("0 0 2 * * ?", response.getBody().getCronExpression());
    assertEquals("SCHEDULED", response.getBody().getStatus());
    assertTrue(response.getBody().getEnabled());
    assertTrue(response.getBody().getMessage().contains("successfully"));
  }
  
  @Test
  @DisplayName("Should handle job schedule failure")
  void shouldHandleJobScheduleFailure() {
    // Given
    BatchJobScheduleRequest request = BatchJobScheduleRequest.builder()
        .jobName("paymentProcessingJob")
        .build();
    
    when(batchJobManagementService.scheduleJob(request))
        .thenThrow(new RuntimeException("Job schedule failed"));
    
    // When
    ResponseEntity<BatchJobScheduleResponse> response = batchJobController.scheduleJob(request);
    
    // Then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("paymentProcessingJob", response.getBody().getJobName());
    assertTrue(response.getBody().getMessage().contains("Failed to schedule job"));
  }
  
  @Test
  @DisplayName("Should get health status successfully")
  void shouldGetHealthStatusSuccessfully() {
    // Given
    Map<String, Object> mockHealthStatus = Map.of(
        "status", "UP",
        "jobLauncher", "UP",
        "jobExplorer", "UP",
        "jobRepository", "UP",
        "availableJobs", Arrays.asList("paymentProcessingJob"),
        "jobCount", 1
    );
    
    when(batchJobManagementService.getHealthStatus()).thenReturn(mockHealthStatus);
    
    // When
    ResponseEntity<Map<String, Object>> response = batchJobController.getHealthStatus();
    
    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("UP", response.getBody().get("status"));
    assertEquals("UP", response.getBody().get("jobLauncher"));
    assertEquals("UP", response.getBody().get("jobExplorer"));
    assertEquals("UP", response.getBody().get("jobRepository"));
    assertEquals(1, response.getBody().get("jobCount"));
  }
  
  @Test
  @DisplayName("Should handle health status get failure")
  void shouldHandleHealthStatusGetFailure() {
    // Given
    when(batchJobManagementService.getHealthStatus())
        .thenThrow(new RuntimeException("Health status get failed"));
    
    // When
    ResponseEntity<Map<String, Object>> response = batchJobController.getHealthStatus();
    
    // Then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("ERROR", response.getBody().get("status"));
    assertTrue(response.getBody().get("message").toString().contains("Failed to get health status"));
  }
  
  /**
   * Creates a mock JobExecution for testing.
   *
   * @param jobExecutionId the job execution ID
   * @param jobName the job name
   * @return mock JobExecution
   */
  private JobExecution createMockJobExecution(Long jobExecutionId, String jobName) {
    JobExecution jobExecution = mock(JobExecution.class);
    JobInstance jobInstance = mock(JobInstance.class);
    
    when(jobExecution.getId()).thenReturn(jobExecutionId);
    when(jobExecution.getJobInstance()).thenReturn(jobInstance);
    when(jobInstance.getJobName()).thenReturn(jobName);
    when(jobExecution.getStatus()).thenReturn(org.springframework.batch.core.BatchStatus.STARTED);
    when(jobExecution.getStartTime()).thenReturn(LocalDateTime.now());
    when(jobExecution.getEndTime()).thenReturn(null);
    when(jobExecution.getExitStatus()).thenReturn(org.springframework.batch.core.ExitStatus.UNKNOWN);
    
    return jobExecution;
  }
}
