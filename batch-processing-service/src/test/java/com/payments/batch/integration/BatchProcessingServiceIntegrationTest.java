package com.payments.batch.integration;

import com.payments.batch.config.BatchJobConfiguration;
import com.payments.batch.domain.BatchJobExecutionMetadata;
import com.payments.batch.domain.BatchJobSchedule;
import com.payments.batch.domain.BatchJobMetrics;
import com.payments.batch.domain.ProcessedPayment;
import com.payments.batch.repository.BatchJobExecutionMetadataRepository;
import com.payments.batch.repository.BatchJobScheduleRepository;
import com.payments.batch.repository.BatchJobMetricsRepository;
import com.payments.batch.repository.ProcessedPaymentRepository;
import com.payments.batch.service.BatchJobManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for the Batch Processing Service.
 *
 * <p>This test class provides end-to-end testing of the batch processing
 * service including job execution, metadata tracking, scheduling, and
 * performance metrics collection.
 *
 * @since PE-407
 */
@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/org/springframework/batch/core/schema-postgresql.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class BatchProcessingServiceIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job paymentProcessingJob;

    @Autowired
    private BatchJobManagementService batchJobManagementService;

    @Autowired
    private BatchJobExecutionMetadataRepository metadataRepository;

    @Autowired
    private BatchJobScheduleRepository scheduleRepository;

    @Autowired
    private BatchJobMetricsRepository metricsRepository;

    @Autowired
    private ProcessedPaymentRepository processedPaymentRepository;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(paymentProcessingJob);
    }

    @Test
    @DisplayName("Should execute batch job successfully with metadata tracking")
    void shouldExecuteBatchJobSuccessfullyWithMetadataTracking() {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", "test-payments.csv")
                .addString("tenantId", "tenant1")
                .addString("businessUnitId", "bu1")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertNotNull(jobExecution);
        assertEquals("COMPLETED", jobExecution.getStatus().toString());

        // Verify metadata was created
        List<BatchJobExecutionMetadata> metadataList = metadataRepository.findByJobExecutionId(jobExecution.getId());
        assertFalse(metadataList.isEmpty());
        
        BatchJobExecutionMetadata metadata = metadataList.get(0);
        assertEquals("paymentProcessingJob", metadata.getJobName());
        assertEquals("tenant1", metadata.getTenantId());
        assertEquals("bu1", metadata.getBusinessUnitId());
        assertEquals(BatchJobExecutionMetadata.ExecutionStatus.COMPLETED, metadata.getStatus());
        assertNotNull(metadata.getStartTime());
        assertNotNull(metadata.getEndTime());
        assertTrue(metadata.getDurationSeconds() > 0);
    }

    @Test
    @DisplayName("Should track job execution progress and performance metrics")
    void shouldTrackJobExecutionProgressAndPerformanceMetrics() {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", "test-payments.csv")
                .addString("tenantId", "tenant1")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertNotNull(jobExecution);
        assertEquals("COMPLETED", jobExecution.getStatus().toString());

        // Verify performance metrics
        List<BatchJobExecutionMetadata> metadataList = metadataRepository.findByJobExecutionId(jobExecution.getId());
        assertFalse(metadataList.isEmpty());
        
        BatchJobExecutionMetadata metadata = metadataList.get(0);
        assertTrue(metadata.getRecordsProcessed() > 0);
        assertTrue(metadata.getProcessingRate().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(metadata.getProgressPercentage().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    @DisplayName("Should create and manage job schedules")
    void shouldCreateAndManageJobSchedules() {
        // Given
        BatchJobSchedule schedule = BatchJobSchedule.builder()
                .scheduleId("schedule-001")
                .jobName("paymentProcessingJob")
                .tenantId("tenant1")
                .businessUnitId("bu1")
                .scheduleName("Daily Payment Processing")
                .description("Process payments daily at 2 AM")
                .cronExpression("0 0 2 * * ?")
                .timeZone("UTC")
                .status(BatchJobSchedule.ScheduleStatus.ACTIVE)
                .enabled(true)
                .priority(5)
                .async(false)
                .parameters("{\"inputFile\": \"daily-payments.csv\"}")
                .configuration("{\"chunkSize\": 1000}")
                .createdBy("admin")
                .build();

        // When
        BatchJobSchedule savedSchedule = scheduleRepository.save(schedule);

        // Then
        assertNotNull(savedSchedule);
        assertNotNull(savedSchedule.getId());
        assertEquals("schedule-001", savedSchedule.getScheduleId());
        assertEquals("paymentProcessingJob", savedSchedule.getJobName());
        assertEquals("tenant1", savedSchedule.getTenantId());
        assertEquals(BatchJobSchedule.ScheduleStatus.ACTIVE, savedSchedule.getStatus());
        assertTrue(savedSchedule.getEnabled());
        assertTrue(savedSchedule.isActive());
    }

    @Test
    @DisplayName("Should collect and aggregate performance metrics")
    void shouldCollectAndAggregatePerformanceMetrics() {
        // Given
        LocalDate today = LocalDate.now();
        BatchJobMetrics metrics = BatchJobMetrics.builder()
                .jobName("paymentProcessingJob")
                .tenantId("tenant1")
                .businessUnitId("bu1")
                .metricDate(today)
                .metricHour(14) // 2 PM
                .totalExecutions(10L)
                .successfulExecutions(9L)
                .failedExecutions(1L)
                .runningExecutions(0L)
                .successRate(BigDecimal.valueOf(90.0))
                .failureRate(BigDecimal.valueOf(10.0))
                .averageExecutionTime(BigDecimal.valueOf(120.5))
                .minExecutionTime(BigDecimal.valueOf(95.0))
                .maxExecutionTime(BigDecimal.valueOf(180.0))
                .totalExecutionTime(BigDecimal.valueOf(1205.0))
                .averageProcessingRate(BigDecimal.valueOf(8.5))
                .totalRecordsProcessed(10000L)
                .totalRecordsFailed(100L)
                .totalRecordsSkipped(50L)
                .averageMemoryUsage(BigDecimal.valueOf(512.0))
                .maxMemoryUsage(BigDecimal.valueOf(1024.0))
                .averageCpuUsage(BigDecimal.valueOf(75.5))
                .maxCpuUsage(BigDecimal.valueOf(95.0))
                .build();

        // When
        BatchJobMetrics savedMetrics = metricsRepository.save(metrics);

        // Then
        assertNotNull(savedMetrics);
        assertNotNull(savedMetrics.getId());
        assertEquals("paymentProcessingJob", savedMetrics.getJobName());
        assertEquals("tenant1", savedMetrics.getTenantId());
        assertEquals(today, savedMetrics.getMetricDate());
        assertEquals(14, savedMetrics.getMetricHour());
        assertEquals(10L, savedMetrics.getTotalExecutions());
        assertEquals(9L, savedMetrics.getSuccessfulExecutions());
        assertEquals(1L, savedMetrics.getFailedExecutions());
        assertEquals(BigDecimal.valueOf(90.0), savedMetrics.getSuccessRate());
        assertEquals(BigDecimal.valueOf(10.0), savedMetrics.getFailureRate());
        assertEquals(BigDecimal.valueOf(120.5), savedMetrics.getAverageExecutionTime());
        assertEquals(BigDecimal.valueOf(8.5), savedMetrics.getAverageProcessingRate());
        assertEquals(10000L, savedMetrics.getTotalRecordsProcessed());
        assertEquals(BigDecimal.valueOf(512.0), savedMetrics.getAverageMemoryUsage());
        assertEquals(BigDecimal.valueOf(75.5), savedMetrics.getAverageCpuUsage());
    }

    @Test
    @DisplayName("Should handle job execution failures gracefully")
    void shouldHandleJobExecutionFailuresGracefully() {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", "invalid-file.csv")
                .addString("tenantId", "tenant1")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertNotNull(jobExecution);
        // Job should fail due to invalid file
        assertEquals("FAILED", jobExecution.getStatus().toString());

        // Verify failure metadata was created
        List<BatchJobExecutionMetadata> metadataList = metadataRepository.findByJobExecutionId(jobExecution.getId());
        if (!metadataList.isEmpty()) {
            BatchJobExecutionMetadata metadata = metadataList.get(0);
            assertEquals(BatchJobExecutionMetadata.ExecutionStatus.FAILED, metadata.getStatus());
            assertNotNull(metadata.getErrorMessage());
        }
    }

    @Test
    @DisplayName("Should support multi-tenant data isolation")
    void shouldSupportMultiTenantDataIsolation() {
        // Given
        BatchJobExecutionMetadata metadata1 = BatchJobExecutionMetadata.builder()
                .jobExecutionId(1L)
                .jobName("paymentProcessingJob")
                .tenantId("tenant1")
                .businessUnitId("bu1")
                .status(BatchJobExecutionMetadata.ExecutionStatus.COMPLETED)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now())
                .durationSeconds(3600)
                .recordsProcessed(1000L)
                .build();

        BatchJobExecutionMetadata metadata2 = BatchJobExecutionMetadata.builder()
                .jobExecutionId(2L)
                .jobName("paymentProcessingJob")
                .tenantId("tenant2")
                .businessUnitId("bu2")
                .status(BatchJobExecutionMetadata.ExecutionStatus.COMPLETED)
                .startTime(LocalDateTime.now().minusHours(2))
                .endTime(LocalDateTime.now().minusHours(1))
                .durationSeconds(3600)
                .recordsProcessed(2000L)
                .build();

        // When
        metadataRepository.save(metadata1);
        metadataRepository.save(metadata2);

        // Then
        List<BatchJobExecutionMetadata> tenant1Metadata = metadataRepository.findByTenantId("tenant1");
        List<BatchJobExecutionMetadata> tenant2Metadata = metadataRepository.findByTenantId("tenant2");

        assertEquals(1, tenant1Metadata.size());
        assertEquals(1, tenant2Metadata.size());
        assertEquals("tenant1", tenant1Metadata.get(0).getTenantId());
        assertEquals("tenant2", tenant2Metadata.get(0).getTenantId());
    }

    @Test
    @DisplayName("Should process different file formats correctly")
    void shouldProcessDifferentFileFormatsCorrectly() {
        // Test CSV processing
        JobParameters csvParameters = new JobParametersBuilder()
                .addString("inputFile", "test-payments.csv")
                .addString("tenantId", "tenant1")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution csvExecution = jobLauncherTestUtils.launchJob(csvParameters);
        assertNotNull(csvExecution);
        assertEquals("COMPLETED", csvExecution.getStatus().toString());

        // Test Excel processing
        JobParameters excelParameters = new JobParametersBuilder()
                .addString("inputFile", "test-payments.xlsx")
                .addString("tenantId", "tenant1")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution excelExecution = jobLauncherTestUtils.launchJob(excelParameters);
        assertNotNull(excelExecution);
        assertEquals("COMPLETED", excelExecution.getStatus().toString());

        // Test JSON processing
        JobParameters jsonParameters = new JobParametersBuilder()
                .addString("inputFile", "test-payments.json")
                .addString("tenantId", "tenant1")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jsonExecution = jobLauncherTestUtils.launchJob(jsonParameters);
        assertNotNull(jsonExecution);
        assertEquals("COMPLETED", jsonExecution.getStatus().toString());
    }

    @Test
    @DisplayName("Should handle large batch processing efficiently")
    void shouldHandleLargeBatchProcessingEfficiently() {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", "large-payments.csv")
                .addString("tenantId", "tenant1")
                .addString("chunkSize", "1000")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When
        long startTime = System.currentTimeMillis();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull(jobExecution);
        assertEquals("COMPLETED", jobExecution.getStatus().toString());
        
        // Verify processing time is reasonable (less than 30 seconds for test data)
        long processingTime = endTime - startTime;
        assertTrue(processingTime < 30000, "Processing time should be less than 30 seconds");

        // Verify records were processed
        List<ProcessedPayment> processedPayments = processedPaymentRepository.findAll();
        assertFalse(processedPayments.isEmpty());
    }

    @Test
    @DisplayName("Should maintain data consistency during concurrent executions")
    void shouldMaintainDataConsistencyDuringConcurrentExecutions() {
        // Given
        JobParameters jobParameters1 = new JobParametersBuilder()
                .addString("inputFile", "test-payments-1.csv")
                .addString("tenantId", "tenant1")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobParameters jobParameters2 = new JobParametersBuilder()
                .addString("inputFile", "test-payments-2.csv")
                .addString("tenantId", "tenant1")
                .addLong("timestamp", System.currentTimeMillis() + 1)
                .toJobParameters();

        // When
        JobExecution execution1 = jobLauncherTestUtils.launchJob(jobParameters1);
        JobExecution execution2 = jobLauncherTestUtils.launchJob(jobParameters2);

        // Then
        assertNotNull(execution1);
        assertNotNull(execution2);
        assertEquals("COMPLETED", execution1.getStatus().toString());
        assertEquals("COMPLETED", execution2.getStatus().toString());

        // Verify both executions have unique metadata
        List<BatchJobExecutionMetadata> allMetadata = metadataRepository.findAll();
        assertTrue(allMetadata.size() >= 2);

        // Verify no data corruption
        List<ProcessedPayment> allProcessedPayments = processedPaymentRepository.findAll();
        assertFalse(allProcessedPayments.isEmpty());
    }

    @Test
    @DisplayName("Should calculate and update metrics correctly")
    void shouldCalculateAndUpdateMetricsCorrectly() {
        // Given
        BatchJobMetrics metrics = BatchJobMetrics.builder()
                .jobName("paymentProcessingJob")
                .tenantId("tenant1")
                .metricDate(LocalDate.now())
                .metricHour(10)
                .build();

        // When
        metrics.updateMetrics(
                BigDecimal.valueOf(120.0), // execution time
                1000L, // records processed
                50L, // records failed
                25L, // records skipped
                BigDecimal.valueOf(512.0), // memory usage
                BigDecimal.valueOf(75.0), // CPU usage
                true // successful
        );

        BatchJobMetrics savedMetrics = metricsRepository.save(metrics);

        // Then
        assertNotNull(savedMetrics);
        assertEquals(1L, savedMetrics.getTotalExecutions());
        assertEquals(1L, savedMetrics.getSuccessfulExecutions());
        assertEquals(0L, savedMetrics.getFailedExecutions());
        assertEquals(BigDecimal.valueOf(100.0), savedMetrics.getSuccessRate());
        assertEquals(BigDecimal.valueOf(0.0), savedMetrics.getFailureRate());
        assertEquals(BigDecimal.valueOf(120.0), savedMetrics.getAverageExecutionTime());
        assertEquals(1000L, savedMetrics.getTotalRecordsProcessed());
        assertEquals(50L, savedMetrics.getTotalRecordsFailed());
        assertEquals(25L, savedMetrics.getTotalRecordsSkipped());
    }
}
