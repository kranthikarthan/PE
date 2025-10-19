package com.payments.batch.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Unit tests for BatchJobExecutionMetadata.
 *
 * @since PE-406
 */
@DisplayName("Batch Job Execution Metadata Tests")
class BatchJobExecutionMetadataTest {
  
  private BatchJobExecutionMetadata metadata;
  
  @BeforeEach
  void setUp() {
    metadata = BatchJobExecutionMetadata.builder()
        .jobExecutionId(1L)
        .jobName("paymentProcessingJob")
        .tenantId("tenant1")
        .businessUnitId("bu1")
        .executionType(BatchJobExecutionMetadata.ExecutionType.MANUAL)
        .priority(5)
        .status(BatchJobExecutionMetadata.ExecutionStatus.RUNNING)
        .startTime(LocalDateTime.now())
        .recordsProcessed(1000L)
        .recordsFailed(50L)
        .recordsSkipped(25L)
        .totalRecords(1075L)
        .build();
  }
  
  @Test
  @DisplayName("Should create metadata with default values")
  void shouldCreateMetadataWithDefaultValues() {
    // Given
    BatchJobExecutionMetadata newMetadata = new BatchJobExecutionMetadata();
    
    // Then
    assertEquals(BatchJobExecutionMetadata.ExecutionType.MANUAL, newMetadata.getExecutionType());
    assertEquals(5, newMetadata.getPriority());
    assertEquals(BigDecimal.ZERO, newMetadata.getProgressPercentage());
    assertEquals(0, newMetadata.getStepsCompleted());
    assertEquals(0, newMetadata.getTotalSteps());
    assertEquals(0L, newMetadata.getRecordsProcessed());
    assertEquals(0L, newMetadata.getRecordsFailed());
    assertEquals(0L, newMetadata.getRecordsSkipped());
    assertEquals(0L, newMetadata.getTotalRecords());
  }
  
  @Test
  @DisplayName("Should calculate duration correctly")
  void shouldCalculateDurationCorrectly() {
    // Given
    metadata.setStartTime(LocalDateTime.now().minusHours(2));
    metadata.setEndTime(LocalDateTime.now());
    
    // When
    Integer duration = metadata.calculateDuration();
    
    // Then
    assertNotNull(duration);
    assertTrue(duration >= 7200); // At least 2 hours in seconds
  }
  
  @Test
  @DisplayName("Should return null duration when start time is null")
  void shouldReturnNullDurationWhenStartTimeIsNull() {
    // Given
    metadata.setStartTime(null);
    metadata.setEndTime(LocalDateTime.now());
    
    // When
    Integer duration = metadata.calculateDuration();
    
    // Then
    assertNull(duration);
  }
  
  @Test
  @DisplayName("Should return null duration when end time is null")
  void shouldReturnNullDurationWhenEndTimeIsNull() {
    // Given
    metadata.setStartTime(LocalDateTime.now());
    metadata.setEndTime(null);
    
    // When
    Integer duration = metadata.calculateDuration();
    
    // Then
    assertNull(duration);
  }
  
  @Test
  @DisplayName("Should calculate processing rate correctly")
  void shouldCalculateProcessingRateCorrectly() {
    // Given
    metadata.setDurationSeconds(100);
    metadata.setRecordsProcessed(1000L);
    
    // When
    BigDecimal processingRate = metadata.calculateProcessingRate();
    
    // Then
    assertNotNull(processingRate);
    assertEquals(BigDecimal.valueOf(10.0), processingRate);
  }
  
  @Test
  @DisplayName("Should return null processing rate when duration is zero")
  void shouldReturnNullProcessingRateWhenDurationIsZero() {
    // Given
    metadata.setDurationSeconds(0);
    metadata.setRecordsProcessed(1000L);
    
    // When
    BigDecimal processingRate = metadata.calculateProcessingRate();
    
    // Then
    assertNull(processingRate);
  }
  
  @Test
  @DisplayName("Should return null processing rate when duration is null")
  void shouldReturnNullProcessingRateWhenDurationIsNull() {
    // Given
    metadata.setDurationSeconds(null);
    metadata.setRecordsProcessed(1000L);
    
    // When
    BigDecimal processingRate = metadata.calculateProcessingRate();
    
    // Then
    assertNull(processingRate);
  }
  
  @Test
  @DisplayName("Should calculate success rate correctly")
  void shouldCalculateSuccessRateCorrectly() {
    // Given
    metadata.setTotalRecords(1000L);
    metadata.setRecordsProcessed(800L);
    
    // When
    BigDecimal successRate = metadata.calculateSuccessRate();
    
    // Then
    assertNotNull(successRate);
    assertEquals(BigDecimal.valueOf(80.0), successRate);
  }
  
  @Test
  @DisplayName("Should return null success rate when total records is zero")
  void shouldReturnNullSuccessRateWhenTotalRecordsIsZero() {
    // Given
    metadata.setTotalRecords(0L);
    metadata.setRecordsProcessed(800L);
    
    // When
    BigDecimal successRate = metadata.calculateSuccessRate();
    
    // Then
    assertNull(successRate);
  }
  
  @Test
  @DisplayName("Should return null success rate when total records is null")
  void shouldReturnNullSuccessRateWhenTotalRecordsIsNull() {
    // Given
    metadata.setTotalRecords(null);
    metadata.setRecordsProcessed(800L);
    
    // When
    BigDecimal successRate = metadata.calculateSuccessRate();
    
    // Then
    assertNull(successRate);
  }
  
  @Test
  @DisplayName("Should check if running correctly")
  void shouldCheckIfRunningCorrectly() {
    // Given
    metadata.setStatus(BatchJobExecutionMetadata.ExecutionStatus.RUNNING);
    
    // When & Then
    assertTrue(metadata.isRunning());
    
    // Given
    metadata.setStatus(BatchJobExecutionMetadata.ExecutionStatus.COMPLETED);
    
    // When & Then
    assertFalse(metadata.isRunning());
  }
  
  @Test
  @DisplayName("Should check if completed correctly")
  void shouldCheckIfCompletedCorrectly() {
    // Given
    metadata.setStatus(BatchJobExecutionMetadata.ExecutionStatus.COMPLETED);
    
    // When & Then
    assertTrue(metadata.isCompleted());
    
    // Given
    metadata.setStatus(BatchJobExecutionMetadata.ExecutionStatus.FAILED);
    
    // When & Then
    assertTrue(metadata.isCompleted());
    
    // Given
    metadata.setStatus(BatchJobExecutionMetadata.ExecutionStatus.RUNNING);
    
    // When & Then
    assertFalse(metadata.isCompleted());
  }
  
  @Test
  @DisplayName("Should check if successful correctly")
  void shouldCheckIfSuccessfulCorrectly() {
    // Given
    metadata.setStatus(BatchJobExecutionMetadata.ExecutionStatus.COMPLETED);
    
    // When & Then
    assertTrue(metadata.isSuccessful());
    
    // Given
    metadata.setStatus(BatchJobExecutionMetadata.ExecutionStatus.FAILED);
    
    // When & Then
    assertFalse(metadata.isSuccessful());
  }
  
  @Test
  @DisplayName("Should check if failed correctly")
  void shouldCheckIfFailedCorrectly() {
    // Given
    metadata.setStatus(BatchJobExecutionMetadata.ExecutionStatus.FAILED);
    
    // When & Then
    assertTrue(metadata.isFailed());
    
    // Given
    metadata.setStatus(BatchJobExecutionMetadata.ExecutionStatus.COMPLETED);
    
    // When & Then
    assertFalse(metadata.isFailed());
  }
  
  @Test
  @DisplayName("Should get summary correctly")
  void shouldGetSummaryCorrectly() {
    // When
    String summary = metadata.getSummary();
    
    // Then
    assertNotNull(summary);
    assertTrue(summary.contains("JobExecution[1]"));
    assertTrue(summary.contains("paymentProcessingJob"));
    assertTrue(summary.contains("RUNNING"));
  }
  
  @Test
  @DisplayName("Should handle null start time in summary")
  void shouldHandleNullStartTimeInSummary() {
    // Given
    metadata.setStartTime(null);
    
    // When
    String summary = metadata.getSummary();
    
    // Then
    assertNotNull(summary);
    assertTrue(summary.contains("N/A"));
  }
  
  @Test
  @DisplayName("Should handle all execution types")
  void shouldHandleAllExecutionTypes() {
    // Test all execution types
    for (BatchJobExecutionMetadata.ExecutionType type : BatchJobExecutionMetadata.ExecutionType.values()) {
      metadata.setExecutionType(type);
      assertEquals(type, metadata.getExecutionType());
    }
  }
  
  @Test
  @DisplayName("Should handle all execution statuses")
  void shouldHandleAllExecutionStatuses() {
    // Test all execution statuses
    for (BatchJobExecutionMetadata.ExecutionStatus status : BatchJobExecutionMetadata.ExecutionStatus.values()) {
      metadata.setStatus(status);
      assertEquals(status, metadata.getStatus());
    }
  }
  
  @Test
  @DisplayName("Should handle builder pattern correctly")
  void shouldHandleBuilderPatternCorrectly() {
    // Given
    LocalDateTime now = LocalDateTime.now();
    
    // When
    BatchJobExecutionMetadata builtMetadata = BatchJobExecutionMetadata.builder()
        .jobExecutionId(2L)
        .jobName("testJob")
        .tenantId("tenant2")
        .businessUnitId("bu2")
        .executionType(BatchJobExecutionMetadata.ExecutionType.SCHEDULED)
        .priority(10)
        .status(BatchJobExecutionMetadata.ExecutionStatus.COMPLETED)
        .startTime(now)
        .endTime(now.plusHours(1))
        .durationSeconds(3600)
        .exitCode("COMPLETED")
        .exitDescription("Job completed successfully")
        .progressPercentage(BigDecimal.valueOf(100.0))
        .currentStep("finalStep")
        .stepProgress(BigDecimal.valueOf(100.0))
        .stepsCompleted(5)
        .totalSteps(5)
        .recordsProcessed(5000L)
        .recordsFailed(0L)
        .recordsSkipped(0L)
        .totalRecords(5000L)
        .processingRate(BigDecimal.valueOf(1.39))
        .memoryUsageMb(BigDecimal.valueOf(512.0))
        .cpuUsagePercentage(BigDecimal.valueOf(75.5))
        .errorMessage(null)
        .stackTrace(null)
        .parameters("{\"param1\": \"value1\"}")
        .configuration("{\"config1\": \"value1\"}")
        .metadata("{\"meta1\": \"value1\"}")
        .createdBy("user1")
        .updatedBy("user1")
        .build();
    
    // Then
    assertNotNull(builtMetadata);
    assertEquals(2L, builtMetadata.getJobExecutionId());
    assertEquals("testJob", builtMetadata.getJobName());
    assertEquals("tenant2", builtMetadata.getTenantId());
    assertEquals("bu2", builtMetadata.getBusinessUnitId());
    assertEquals(BatchJobExecutionMetadata.ExecutionType.SCHEDULED, builtMetadata.getExecutionType());
    assertEquals(10, builtMetadata.getPriority());
    assertEquals(BatchJobExecutionMetadata.ExecutionStatus.COMPLETED, builtMetadata.getStatus());
    assertEquals(now, builtMetadata.getStartTime());
    assertEquals(now.plusHours(1), builtMetadata.getEndTime());
    assertEquals(3600, builtMetadata.getDurationSeconds());
    assertEquals("COMPLETED", builtMetadata.getExitCode());
    assertEquals("Job completed successfully", builtMetadata.getExitDescription());
    assertEquals(BigDecimal.valueOf(100.0), builtMetadata.getProgressPercentage());
    assertEquals("finalStep", builtMetadata.getCurrentStep());
    assertEquals(BigDecimal.valueOf(100.0), builtMetadata.getStepProgress());
    assertEquals(5, builtMetadata.getStepsCompleted());
    assertEquals(5, builtMetadata.getTotalSteps());
    assertEquals(5000L, builtMetadata.getRecordsProcessed());
    assertEquals(0L, builtMetadata.getRecordsFailed());
    assertEquals(0L, builtMetadata.getRecordsSkipped());
    assertEquals(5000L, builtMetadata.getTotalRecords());
    assertEquals(BigDecimal.valueOf(1.39), builtMetadata.getProcessingRate());
    assertEquals(BigDecimal.valueOf(512.0), builtMetadata.getMemoryUsageMb());
    assertEquals(BigDecimal.valueOf(75.5), builtMetadata.getCpuUsagePercentage());
    assertNull(builtMetadata.getErrorMessage());
    assertNull(builtMetadata.getStackTrace());
    assertEquals("{\"param1\": \"value1\"}", builtMetadata.getParameters());
    assertEquals("{\"config1\": \"value1\"}", builtMetadata.getConfiguration());
    assertEquals("{\"meta1\": \"value1\"}", builtMetadata.getMetadata());
    assertEquals("user1", builtMetadata.getCreatedBy());
    assertEquals("user1", builtMetadata.getUpdatedBy());
  }
}
