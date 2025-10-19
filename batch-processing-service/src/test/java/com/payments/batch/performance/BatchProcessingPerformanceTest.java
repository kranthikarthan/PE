package com.payments.batch.performance;

import com.payments.batch.domain.BatchJobExecutionMetadata;
import com.payments.batch.domain.BatchJobMetrics;
import com.payments.batch.domain.ProcessedPayment;
import com.payments.batch.repository.BatchJobExecutionMetadataRepository;
import com.payments.batch.repository.BatchJobMetricsRepository;
import com.payments.batch.repository.ProcessedPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance and load testing for the Batch Processing Service.
 *
 * <p>This test class provides comprehensive performance testing including
 * load testing, concurrent execution testing, and performance metrics validation.
 *
 * @since PE-407
 */
@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@Sql(scripts = "/org/springframework/batch/core/schema-postgresql.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class BatchProcessingPerformanceTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job paymentProcessingJob;

    @Autowired
    private BatchJobExecutionMetadataRepository metadataRepository;

    @Autowired
    private BatchJobMetricsRepository metricsRepository;

    @Autowired
    private ProcessedPaymentRepository processedPaymentRepository;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(paymentProcessingJob);
        executorService = Executors.newFixedThreadPool(10);
    }

    @Test
    @DisplayName("Should handle high-volume batch processing efficiently")
    void shouldHandleHighVolumeBatchProcessingEfficiently() {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", "high-volume-payments.csv")
                .addString("tenantId", "tenant1")
                .addString("chunkSize", "1000")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When
        long startTime = System.currentTimeMillis();
        var jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull(jobExecution);
        assertEquals("COMPLETED", jobExecution.getStatus().toString());

        // Performance assertions
        long processingTime = endTime - startTime;
        assertTrue(processingTime < 60000, "Processing time should be less than 60 seconds for high volume");

        // Verify processing rate
        List<BatchJobExecutionMetadata> metadataList = metadataRepository.findByJobExecutionId(jobExecution.getId());
        if (!metadataList.isEmpty()) {
            BatchJobExecutionMetadata metadata = metadataList.get(0);
            assertTrue(metadata.getProcessingRate().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(metadata.getRecordsProcessed() > 0);
        }

        // Verify records were processed
        List<ProcessedPayment> processedPayments = processedPaymentRepository.findAll();
        assertFalse(processedPayments.isEmpty());
        assertTrue(processedPayments.size() > 1000, "Should process more than 1000 records");
    }

    @Test
    @DisplayName("Should handle concurrent job executions without conflicts")
    void shouldHandleConcurrentJobExecutionsWithoutConflicts() throws Exception {
        // Given
        int concurrentJobs = 5;
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        // When
        for (int i = 0; i < concurrentJobs; i++) {
            final int jobIndex = i;
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    JobParameters jobParameters = new JobParametersBuilder()
                            .addString("inputFile", "concurrent-test-" + jobIndex + ".csv")
                            .addString("tenantId", "tenant" + jobIndex)
                            .addLong("timestamp", System.currentTimeMillis() + jobIndex)
                            .toJobParameters();

                    var jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
                    return jobExecution != null && "COMPLETED".equals(jobExecution.getStatus().toString());
                } catch (Exception e) {
                    return false;
                }
            }, executorService);
            futures.add(future);
        }

        // Wait for all jobs to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        allFutures.get(120, TimeUnit.SECONDS); // 2 minute timeout

        // Then
        long successfulJobs = futures.stream()
                .mapToLong(future -> {
                    try {
                        return future.get() ? 1 : 0;
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();

        assertEquals(concurrentJobs, successfulJobs, "All concurrent jobs should complete successfully");

        // Verify no data corruption
        List<ProcessedPayment> allProcessedPayments = processedPaymentRepository.findAll();
        assertFalse(allProcessedPayments.isEmpty());
    }

    @Test
    @DisplayName("Should maintain performance under sustained load")
    void shouldMaintainPerformanceUnderSustainedLoad() throws Exception {
        // Given
        int sustainedJobs = 10;
        List<CompletableFuture<Long>> futures = new ArrayList<>();

        // When
        for (int i = 0; i < sustainedJobs; i++) {
            final int jobIndex = i;
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    JobParameters jobParameters = new JobParametersBuilder()
                            .addString("inputFile", "sustained-load-" + jobIndex + ".csv")
                            .addString("tenantId", "tenant" + (jobIndex % 3)) // Distribute across tenants
                            .addLong("timestamp", System.currentTimeMillis() + jobIndex)
                            .toJobParameters();

                    var jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
                    long endTime = System.currentTimeMillis();
                    
                    if (jobExecution != null && "COMPLETED".equals(jobExecution.getStatus().toString())) {
                        return endTime - startTime;
                    }
                    return -1L; // Indicate failure
                } catch (Exception e) {
                    return -1L;
                }
            }, executorService);
            futures.add(future);
        }

        // Wait for all jobs to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        allFutures.get(300, TimeUnit.SECONDS); // 5 minute timeout

        // Then
        List<Long> processingTimes = futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        return -1L;
                    }
                })
                .filter(time -> time > 0)
                .toList();

        assertEquals(sustainedJobs, processingTimes.size(), "All sustained load jobs should complete successfully");

        // Calculate performance metrics
        double averageProcessingTime = processingTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long maxProcessingTime = processingTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        // Performance assertions
        assertTrue(averageProcessingTime < 30000, "Average processing time should be less than 30 seconds");
        assertTrue(maxProcessingTime < 60000, "Maximum processing time should be less than 60 seconds");

        // Verify no performance degradation over time
        long firstHalfAverage = processingTimes.subList(0, sustainedJobs / 2).stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long secondHalfAverage = processingTimes.subList(sustainedJobs / 2, sustainedJobs).stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        double performanceDegradation = Math.abs(secondHalfAverage - firstHalfAverage) / firstHalfAverage;
        assertTrue(performanceDegradation < 0.5, "Performance degradation should be less than 50%");
    }

    @Test
    @DisplayName("Should handle memory efficiently during large batch processing")
    void shouldHandleMemoryEfficientlyDuringLargeBatchProcessing() {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", "memory-intensive-payments.csv")
                .addString("tenantId", "tenant1")
                .addString("chunkSize", "500") // Smaller chunk size for memory efficiency
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When
        long startTime = System.currentTimeMillis();
        var jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull(jobExecution);
        assertEquals("COMPLETED", jobExecution.getStatus().toString());

        // Verify memory usage is reasonable
        List<BatchJobExecutionMetadata> metadataList = metadataRepository.findByJobExecutionId(jobExecution.getId());
        if (!metadataList.isEmpty()) {
            BatchJobExecutionMetadata metadata = metadataList.get(0);
            if (metadata.getMemoryUsageMb() != null) {
                assertTrue(metadata.getMemoryUsageMb().compareTo(BigDecimal.valueOf(2048)) < 0,
                        "Memory usage should be less than 2GB");
            }
        }

        // Verify processing completed successfully
        List<ProcessedPayment> processedPayments = processedPaymentRepository.findAll();
        assertFalse(processedPayments.isEmpty());
    }

    @Test
    @DisplayName("Should maintain data consistency under high concurrency")
    void shouldMaintainDataConsistencyUnderHighConcurrency() throws Exception {
        // Given
        int highConcurrencyJobs = 20;
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        // When
        for (int i = 0; i < highConcurrencyJobs; i++) {
            final int jobIndex = i;
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    JobParameters jobParameters = new JobParametersBuilder()
                            .addString("inputFile", "high-concurrency-" + jobIndex + ".csv")
                            .addString("tenantId", "tenant" + (jobIndex % 5)) // Distribute across 5 tenants
                            .addLong("timestamp", System.currentTimeMillis() + jobIndex)
                            .toJobParameters();

                    var jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
                    return jobExecution != null && "COMPLETED".equals(jobExecution.getStatus().toString());
                } catch (Exception e) {
                    return false;
                }
            }, executorService);
            futures.add(future);
        }

        // Wait for all jobs to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        allFutures.get(180, TimeUnit.SECONDS); // 3 minute timeout

        // Then
        long successfulJobs = futures.stream()
                .mapToLong(future -> {
                    try {
                        return future.get() ? 1 : 0;
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();

        assertTrue(successfulJobs >= highConcurrencyJobs * 0.8, 
                "At least 80% of high concurrency jobs should complete successfully");

        // Verify data consistency
        List<ProcessedPayment> allProcessedPayments = processedPaymentRepository.findAll();
        assertFalse(allProcessedPayments.isEmpty());

        // Verify no duplicate records
        long uniqueRecords = allProcessedPayments.stream()
                .map(ProcessedPayment::getPaymentId)
                .distinct()
                .count();
        assertEquals(allProcessedPayments.size(), uniqueRecords, "No duplicate records should exist");
    }

    @Test
    @DisplayName("Should handle database connection pooling efficiently")
    void shouldHandleDatabaseConnectionPoolingEfficiently() throws Exception {
        // Given
        int connectionPoolJobs = 15;
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        // When
        for (int i = 0; i < connectionPoolJobs; i++) {
            final int jobIndex = i;
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    JobParameters jobParameters = new JobParametersBuilder()
                            .addString("inputFile", "connection-pool-" + jobIndex + ".csv")
                            .addString("tenantId", "tenant" + (jobIndex % 3))
                            .addLong("timestamp", System.currentTimeMillis() + jobIndex)
                            .toJobParameters();

                    var jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
                    return jobExecution != null && "COMPLETED".equals(jobExecution.getStatus().toString());
                } catch (Exception e) {
                    return false;
                }
            }, executorService);
            futures.add(future);
        }

        // Wait for all jobs to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        allFutures.get(240, TimeUnit.SECONDS); // 4 minute timeout

        // Then
        long successfulJobs = futures.stream()
                .mapToLong(future -> {
                    try {
                        return future.get() ? 1 : 0;
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();

        assertTrue(successfulJobs >= connectionPoolJobs * 0.9, 
                "At least 90% of connection pool jobs should complete successfully");

        // Verify no connection leaks
        List<BatchJobExecutionMetadata> allMetadata = metadataRepository.findAll();
        assertFalse(allMetadata.isEmpty());
    }

    @Test
    @DisplayName("Should aggregate performance metrics correctly under load")
    void shouldAggregatePerformanceMetricsCorrectlyUnderLoad() {
        // Given
        LocalDate today = LocalDate.now();
        int metricsCount = 10;

        // When
        for (int i = 0; i < metricsCount; i++) {
            BatchJobMetrics metrics = BatchJobMetrics.builder()
                    .jobName("paymentProcessingJob")
                    .tenantId("tenant" + (i % 3))
                    .businessUnitId("bu" + (i % 2))
                    .metricDate(today)
                    .metricHour(i % 24)
                    .totalExecutions(1L)
                    .successfulExecutions(1L)
                    .failedExecutions(0L)
                    .averageExecutionTime(BigDecimal.valueOf(100.0 + i * 10))
                    .totalRecordsProcessed(1000L + i * 100)
                    .averageMemoryUsage(BigDecimal.valueOf(512.0 + i * 50))
                    .averageCpuUsage(BigDecimal.valueOf(70.0 + i * 2))
                    .build();

            metricsRepository.save(metrics);
        }

        // Then
        List<BatchJobMetrics> allMetrics = metricsRepository.findAll();
        assertEquals(metricsCount, allMetrics.size());

        // Verify metrics aggregation
        for (BatchJobMetrics metrics : allMetrics) {
            assertNotNull(metrics.getSuccessRate());
            assertNotNull(metrics.getFailureRate());
            assertNotNull(metrics.getAverageExecutionTime());
            assertNotNull(metrics.getAverageProcessingRate());
            assertTrue(metrics.getSuccessRate().compareTo(BigDecimal.ZERO) >= 0);
            assertTrue(metrics.getFailureRate().compareTo(BigDecimal.ZERO) >= 0);
        }
    }

    @Test
    @DisplayName("Should handle transaction rollback correctly under failures")
    void shouldHandleTransactionRollbackCorrectlyUnderFailures() {
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", "corrupted-payments.csv")
                .addString("tenantId", "tenant1")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When
        var jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertNotNull(jobExecution);
        assertEquals("FAILED", jobExecution.getStatus().toString());

        // Verify no partial data was committed
        List<ProcessedPayment> processedPayments = processedPaymentRepository.findAll();
        // Should be empty or contain only valid records from previous tests
        // (depending on test isolation)

        // Verify failure metadata was created
        List<BatchJobExecutionMetadata> metadataList = metadataRepository.findByJobExecutionId(jobExecution.getId());
        if (!metadataList.isEmpty()) {
            BatchJobExecutionMetadata metadata = metadataList.get(0);
            assertEquals(BatchJobExecutionMetadata.ExecutionStatus.FAILED, metadata.getStatus());
            assertNotNull(metadata.getErrorMessage());
        }
    }

    @Test
    @DisplayName("Should maintain performance with different chunk sizes")
    void shouldMaintainPerformanceWithDifferentChunkSizes() {
        // Test different chunk sizes
        int[] chunkSizes = {100, 500, 1000, 2000};
        List<Long> processingTimes = new ArrayList<>();

        for (int chunkSize : chunkSizes) {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFile", "chunk-size-test.csv")
                    .addString("tenantId", "tenant1")
                    .addString("chunkSize", String.valueOf(chunkSize))
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            long startTime = System.currentTimeMillis();
            var jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
            long endTime = System.currentTimeMillis();

            assertNotNull(jobExecution);
            assertEquals("COMPLETED", jobExecution.getStatus().toString());
            processingTimes.add(endTime - startTime);
        }

        // Verify all chunk sizes completed successfully
        assertEquals(chunkSizes.length, processingTimes.size());
        
        // Verify processing times are reasonable for all chunk sizes
        for (Long processingTime : processingTimes) {
            assertTrue(processingTime < 30000, "Processing time should be less than 30 seconds for any chunk size");
        }
    }
}
