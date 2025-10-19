package com.payments.batch.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.payments.batch.api.dto.BatchJobConfigurationResponse;
import com.payments.batch.api.dto.BatchJobMetricsResponse;
import com.payments.batch.api.dto.BatchJobScheduleRequest;
import com.payments.batch.api.dto.BatchJobScheduleResponse;
import com.payments.batch.error.BatchProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for BatchJobManagementService.
 *
 * @since PE-405
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Batch Job Management Service Tests")
class BatchJobManagementServiceTest {
  
  @Mock
  private JobLauncher jobLauncher;
  
  @Mock
  private JobExplorer jobExplorer;
  
  @Mock
  private JobRepository jobRepository;
  
  @Mock
  private ApplicationContext applicationContext;
  
  @Mock
  private Job mockJob;
  
  private BatchJobManagementService batchJobManagementService;
  
  @BeforeEach
  void setUp() {
    batchJobManagementService = new BatchJobManagementService(
        jobLauncher, jobExplorer, jobRepository, applicationContext);
  }
  
  @Test
  @DisplayName("Should start job successfully")
  void shouldStartJobSuccessfully() throws Exception {
    // Given
    String jobName = "paymentProcessingJob";
    JobParameters jobParameters = new JobParameters();
    JobExecution mockJobExecution = createMockJobExecution(1L, jobName);
    
    when(applicationContext.getBeanNamesForType(Job.class))
        .thenReturn(new String[]{"paymentProcessingJob"});
    when(applicationContext.getBean("paymentProcessingJob", Job.class)).thenReturn(mockJob);
    when(mockJob.getName()).thenReturn(jobName);
    when(jobLauncher.run(mockJob, jobParameters)).thenReturn(mockJobExecution);
    
    // When
    JobExecution result = batchJobManagementService.startJob(jobName, jobParameters);
    
    // Then
    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(jobLauncher).run(mockJob, jobParameters);
  }
  
  @Test
  @DisplayName("Should handle job not found")
  void shouldHandleJobNotFound() {
    // Given
    String jobName = "nonExistentJob";
    JobParameters jobParameters = new JobParameters();
    
    when(applicationContext.getBeanNamesForType(Job.class))
        .thenReturn(new String[]{"paymentProcessingJob"});
    when(applicationContext.getBean("paymentProcessingJob", Job.class)).thenReturn(mockJob);
    when(mockJob.getName()).thenReturn("paymentProcessingJob");
    
    // When & Then
    BatchProcessingException exception = assertThrows(BatchProcessingException.class, () -> {
      batchJobManagementService.startJob(jobName, jobParameters);
    });
    
    assertEquals("JOB_NOT_FOUND", exception.getErrorCode());
    assertTrue(exception.getMessage().contains("Job not found"));
  }
  
  @Test
  @DisplayName("Should handle job already running")
  void shouldHandleJobAlreadyRunning() throws Exception {
    // Given
    String jobName = "paymentProcessingJob";
    JobParameters jobParameters = new JobParameters();
    
    when(applicationContext.getBeanNamesForType(Job.class))
        .thenReturn(new String[]{"paymentProcessingJob"});
    when(applicationContext.getBean("paymentProcessingJob", Job.class)).thenReturn(mockJob);
    when(mockJob.getName()).thenReturn(jobName);
    when(jobLauncher.run(mockJob, jobParameters))
        .thenThrow(new JobExecutionAlreadyRunningException("Job already running"));
    
    // When & Then
    BatchProcessingException exception = assertThrows(BatchProcessingException.class, () -> {
      batchJobManagementService.startJob(jobName, jobParameters);
    });
    
    assertEquals("JOB_ALREADY_RUNNING", exception.getErrorCode());
    assertTrue(exception.getMessage().contains("Job is already running"));
  }
  
  @Test
  @DisplayName("Should handle job instance already complete")
  void shouldHandleJobInstanceAlreadyComplete() throws Exception {
    // Given
    String jobName = "paymentProcessingJob";
    JobParameters jobParameters = new JobParameters();
    
    when(applicationContext.getBeanNamesForType(Job.class))
        .thenReturn(new String[]{"paymentProcessingJob"});
    when(applicationContext.getBean("paymentProcessingJob", Job.class)).thenReturn(mockJob);
    when(mockJob.getName()).thenReturn(jobName);
    when(jobLauncher.run(mockJob, jobParameters))
        .thenThrow(new JobInstanceAlreadyCompleteException("Job instance already complete"));
    
    // When & Then
    BatchProcessingException exception = assertThrows(BatchProcessingException.class, () -> {
      batchJobManagementService.startJob(jobName, jobParameters);
    });
    
    assertEquals("JOB_INSTANCE_COMPLETE", exception.getErrorCode());
    assertTrue(exception.getMessage().contains("Job instance already complete"));
  }
  
  @Test
  @DisplayName("Should handle invalid job parameters")
  void shouldHandleInvalidJobParameters() throws Exception {
    // Given
    String jobName = "paymentProcessingJob";
    JobParameters jobParameters = new JobParameters();
    
    when(applicationContext.getBeanNamesForType(Job.class))
        .thenReturn(new String[]{"paymentProcessingJob"});
    when(applicationContext.getBean("paymentProcessingJob", Job.class)).thenReturn(mockJob);
    when(mockJob.getName()).thenReturn(jobName);
    when(jobLauncher.run(mockJob, jobParameters))
        .thenThrow(new JobParametersInvalidException("Invalid parameters"));
    
    // When & Then
    BatchProcessingException exception = assertThrows(BatchProcessingException.class, () -> {
      batchJobManagementService.startJob(jobName, jobParameters);
    });
    
    assertEquals("INVALID_JOB_PARAMETERS", exception.getErrorCode());
    assertTrue(exception.getMessage().contains("Invalid job parameters"));
  }
  
  @Test
  @DisplayName("Should stop job successfully")
  void shouldStopJobSuccessfully() {
    // Given
    Long jobExecutionId = 1L;
    JobExecution mockJobExecution = createMockJobExecution(jobExecutionId, "paymentProcessingJob");
    
    when(jobExplorer.getJobExecution(jobExecutionId)).thenReturn(mockJobExecution);
    when(mockJobExecution.getStatus()).thenReturn(org.springframework.batch.core.BatchStatus.STARTED);
    
    // When
    JobExecution result = batchJobManagementService.stopJob(jobExecutionId);
    
    // Then
    assertNotNull(result);
    assertEquals(jobExecutionId, result.getId());
    verify(mockJobExecution).stop();
  }
  
  @Test
  @DisplayName("Should handle job execution not found")
  void shouldHandleJobExecutionNotFound() {
    // Given
    Long jobExecutionId = 1L;
    
    when(jobExplorer.getJobExecution(jobExecutionId)).thenReturn(null);
    
    // When & Then
    BatchProcessingException exception = assertThrows(BatchProcessingException.class, () -> {
      batchJobManagementService.stopJob(jobExecutionId);
    });
    
    assertEquals("JOB_EXECUTION_NOT_FOUND", exception.getErrorCode());
    assertTrue(exception.getMessage().contains("Job execution not found"));
  }
  
  @Test
  @DisplayName("Should handle job not running")
  void shouldHandleJobNotRunning() {
    // Given
    Long jobExecutionId = 1L;
    JobExecution mockJobExecution = createMockJobExecution(jobExecutionId, "paymentProcessingJob");
    
    when(jobExplorer.getJobExecution(jobExecutionId)).thenReturn(mockJobExecution);
    when(mockJobExecution.getStatus()).thenReturn(org.springframework.batch.core.BatchStatus.COMPLETED);
    
    // When & Then
    BatchProcessingException exception = assertThrows(BatchProcessingException.class, () -> {
      batchJobManagementService.stopJob(jobExecutionId);
    });
    
    assertEquals("JOB_NOT_RUNNING", exception.getErrorCode());
    assertTrue(exception.getMessage().contains("Job is not running"));
  }
  
  @Test
  @DisplayName("Should get job execution successfully")
  void shouldGetJobExecutionSuccessfully() {
    // Given
    Long jobExecutionId = 1L;
    JobExecution mockJobExecution = createMockJobExecution(jobExecutionId, "paymentProcessingJob");
    
    when(jobExplorer.getJobExecution(jobExecutionId)).thenReturn(mockJobExecution);
    
    // When
    JobExecution result = batchJobManagementService.getJobExecution(jobExecutionId);
    
    // Then
    assertNotNull(result);
    assertEquals(jobExecutionId, result.getId());
  }
  
  @Test
  @DisplayName("Should handle job execution get failure")
  void shouldHandleJobExecutionGetFailure() {
    // Given
    Long jobExecutionId = 1L;
    
    when(jobExplorer.getJobExecution(jobExecutionId)).thenReturn(null);
    
    // When & Then
    BatchProcessingException exception = assertThrows(BatchProcessingException.class, () -> {
      batchJobManagementService.getJobExecution(jobExecutionId);
    });
    
    assertEquals("JOB_EXECUTION_NOT_FOUND", exception.getErrorCode());
    assertTrue(exception.getMessage().contains("Job execution not found"));
  }
  
  @Test
  @DisplayName("Should get job history successfully")
  void shouldGetJobHistorySuccessfully() {
    // Given
    String jobName = "paymentProcessingJob";
    int limit = 10;
    int offset = 0;
    
    JobExecution mockJobExecution1 = createMockJobExecution(1L, jobName);
    JobExecution mockJobExecution2 = createMockJobExecution(2L, jobName);
    List<JobExecution> mockJobExecutions = Arrays.asList(mockJobExecution1, mockJobExecution2);
    
    when(jobExplorer.getJobNames()).thenReturn(Arrays.asList(jobName));
    when(jobExplorer.getJobInstances(jobName, 0, limit)).thenReturn(Arrays.asList(createMockJobInstance(1L, jobName)));
    when(jobExplorer.getJobExecutions(any())).thenReturn(mockJobExecutions);
    
    // When
    List<JobExecution> result = batchJobManagementService.getJobHistory(jobName, limit, offset);
    
    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
  }
  
  @Test
  @DisplayName("Should get job metrics successfully")
  void shouldGetJobMetricsSuccessfully() {
    // Given
    String jobName = "paymentProcessingJob";
    String timeRange = "24h";
    
    JobExecution mockJobExecution1 = createMockJobExecution(1L, jobName);
    JobExecution mockJobExecution2 = createMockJobExecution(2L, jobName);
    List<JobExecution> mockJobExecutions = Arrays.asList(mockJobExecution1, mockJobExecution2);
    
    when(jobExplorer.getJobNames()).thenReturn(Arrays.asList(jobName));
    when(jobExplorer.getJobInstances(jobName, 0, 1000)).thenReturn(Arrays.asList(createMockJobInstance(1L, jobName)));
    when(jobExplorer.getJobExecutions(any())).thenReturn(mockJobExecutions);
    
    // When
    BatchJobMetricsResponse result = batchJobManagementService.getJobMetrics(jobName, timeRange);
    
    // Then
    assertNotNull(result);
    assertEquals(timeRange, result.getTimeRange());
    assertTrue(result.getTotalExecutions() >= 0);
  }
  
  @Test
  @DisplayName("Should get job configuration successfully")
  void shouldGetJobConfigurationSuccessfully() {
    // Given
    when(applicationContext.getBeanNamesForType(Job.class))
        .thenReturn(new String[]{"paymentProcessingJob"});
    when(applicationContext.getBean("paymentProcessingJob", Job.class)).thenReturn(mockJob);
    when(mockJob.getName()).thenReturn("paymentProcessingJob");
    
    // When
    BatchJobConfigurationResponse result = batchJobManagementService.getJobConfiguration();
    
    // Then
    assertNotNull(result);
    assertNotNull(result.getAvailableJobs());
    assertTrue(result.getAvailableJobs().contains("paymentProcessingJob"));
    assertNotNull(result.getSystemConfiguration());
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
    
    // When
    BatchJobScheduleResponse result = batchJobManagementService.scheduleJob(request);
    
    // Then
    assertNotNull(result);
    assertEquals("paymentProcessingJob", result.getJobName());
    assertEquals("0 0 2 * * ?", result.getCronExpression());
    assertEquals("SCHEDULED", result.getStatus());
    assertTrue(result.getEnabled());
    assertTrue(result.getMessage().contains("successfully"));
  }
  
  @Test
  @DisplayName("Should get health status successfully")
  void shouldGetHealthStatusSuccessfully() {
    // When
    Map<String, Object> result = batchJobManagementService.getHealthStatus();
    
    // Then
    assertNotNull(result);
    assertTrue(result.containsKey("status"));
    assertTrue(result.containsKey("jobLauncher"));
    assertTrue(result.containsKey("jobExplorer"));
    assertTrue(result.containsKey("jobRepository"));
    assertTrue(result.containsKey("availableJobs"));
    assertTrue(result.containsKey("jobCount"));
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
    JobInstance jobInstance = createMockJobInstance(1L, jobName);
    
    when(jobExecution.getId()).thenReturn(jobExecutionId);
    when(jobExecution.getJobInstance()).thenReturn(jobInstance);
    when(jobExecution.getStatus()).thenReturn(org.springframework.batch.core.BatchStatus.STARTED);
    when(jobExecution.getStartTime()).thenReturn(LocalDateTime.now());
    when(jobExecution.getEndTime()).thenReturn(null);
    when(jobExecution.getExitStatus()).thenReturn(org.springframework.batch.core.ExitStatus.UNKNOWN);
    
    return jobExecution;
  }
  
  /**
   * Creates a mock JobInstance for testing.
   *
   * @param jobInstanceId the job instance ID
   * @param jobName the job name
   * @return mock JobInstance
   */
  private JobInstance createMockJobInstance(Long jobInstanceId, String jobName) {
    JobInstance jobInstance = mock(JobInstance.class);
    when(jobInstance.getId()).thenReturn(jobInstanceId);
    when(jobInstance.getJobName()).thenReturn(jobName);
    return jobInstance;
  }
}
