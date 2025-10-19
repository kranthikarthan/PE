package com.payments.reconciliation.performance;

import com.payments.domain.reconciliation.ReconciliationRun;
import com.payments.domain.reconciliation.ReconciliationException;
import com.payments.reconciliation.service.ReconciliationService;
import com.payments.reconciliation.service.ReconciliationExceptionService;
import com.payments.reconciliation.repository.ReconciliationRunRepository;
import com.payments.reconciliation.repository.ReconciliationExceptionRepository;
import com.payments.reconciliation.dto.ReconciliationRunRequest;
import com.payments.reconciliation.dto.ReconciliationRunResponse;
import com.payments.reconciliation.dto.ReconciliationExceptionRequest;
import com.payments.reconciliation.dto.ReconciliationExceptionResponse;
import com.payments.domain.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for Reconciliation Service.
 *
 * <p>This test class provides comprehensive performance tests for the
 * reconciliation service including load testing, stress testing, and
 * concurrency testing to ensure the service can handle high-volume
 * operations efficiently.
 *
 * @since PE-413
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReconciliationServicePerformanceTest {
  
  @Autowired
  private ReconciliationService reconciliationService;
  
  @Autowired
  private ReconciliationExceptionService exceptionService;
  
  @Autowired
  private ReconciliationRunRepository runRepository;
  
  @Autowired
  private ReconciliationExceptionRepository exceptionRepository;
  
  private UUID tenantId;
  private UUID businessUnitId;
  private String userId;
  private ExecutorService executorService;
  
  @BeforeEach
  void setUp() {
    tenantId = UUID.randomUUID();
    businessUnitId = UUID.randomUUID();
    userId = "test-user";
    
    TenantContext.setContext(TenantContext.builder()
        .tenantId(tenantId)
        .tenantName("TestTenant")
        .businessUnitId(businessUnitId)
        .businessUnitName("TestBusinessUnit")
        .build());
    
    executorService = Executors.newFixedThreadPool(10);
    
    // Clear data before each test
    exceptionRepository.deleteAll();
    runRepository.deleteAll();
  }
  
  @Test
  void testReconciliationRunCreationPerformance() {
    // Test creating 1000 reconciliation runs
    int numberOfRuns = 1000;
    List<ReconciliationRunResponse> runs = new ArrayList<>();
    
    long startTime = System.currentTimeMillis();
    
    for (int i = 0; i < numberOfRuns; i++) {
      ReconciliationRunRequest runRequest = ReconciliationRunRequest.builder()
          .runDate(LocalDate.now().plusDays(i))
          .description("Performance test run " + i)
          .build();
      
      ReconciliationRunResponse runResponse = reconciliationService.createReconciliationRun(runRequest);
      runs.add(runResponse);
    }
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    // Verify all runs were created
    assertEquals(numberOfRuns, runs.size());
    
    // Performance assertion: 1000 runs should be created in less than 10 seconds
    assertTrue(duration < 10000, "Creating 1000 reconciliation runs took too long: " + duration + "ms");
    
    // Calculate throughput
    double throughput = (double) numberOfRuns / (duration / 1000.0);
    System.out.println("Reconciliation run creation throughput: " + throughput + " runs/second");
    
    // Verify all runs are in PENDING status
    for (ReconciliationRunResponse run : runs) {
      assertEquals("PENDING", run.getStatus());
    }
  }
  
  @Test
  void testExceptionCreationPerformance() {
    // Create a reconciliation run first
    ReconciliationRunRequest runRequest = ReconciliationRunRequest.builder()
        .runDate(LocalDate.now())
        .description("Exception performance test")
        .build();
    
    ReconciliationRunResponse runResponse = reconciliationService.createReconciliationRun(runRequest);
    assertNotNull(runResponse);
    
    // Test creating 5000 exceptions
    int numberOfExceptions = 5000;
    List<ReconciliationExceptionResponse> exceptions = new ArrayList<>();
    
    long startTime = System.currentTimeMillis();
    
    for (int i = 0; i < numberOfExceptions; i++) {
      ReconciliationExceptionRequest exceptionRequest = ReconciliationExceptionRequest.builder()
          .runId(runResponse.getRunId())
          .exceptionType("AMOUNT_MISMATCH")
          .internalTransactionId("TXN" + String.format("%05d", i))
          .clearingTransactionId("CLR" + String.format("%05d", i))
          .amountDifference(BigDecimal.valueOf(Math.random() * 100))
          .description("Performance test exception " + i)
          .priority("HIGH")
          .build();
      
      ReconciliationExceptionResponse exceptionResponse = exceptionService.createException(exceptionRequest);
      exceptions.add(exceptionResponse);
    }
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    // Verify all exceptions were created
    assertEquals(numberOfExceptions, exceptions.size());
    
    // Performance assertion: 5000 exceptions should be created in less than 15 seconds
    assertTrue(duration < 15000, "Creating 5000 exceptions took too long: " + duration + "ms");
    
    // Calculate throughput
    double throughput = (double) numberOfExceptions / (duration / 1000.0);
    System.out.println("Exception creation throughput: " + throughput + " exceptions/second");
    
    // Verify all exceptions are in OPEN status
    for (ReconciliationExceptionResponse exception : exceptions) {
      assertEquals("OPEN", exception.getStatus());
    }
  }
  
  @Test
  void testConcurrentReconciliationRuns() throws InterruptedException {
    // Test creating reconciliation runs concurrently
    int numberOfThreads = 10;
    int runsPerThread = 100;
    List<CompletableFuture<List<ReconciliationRunResponse>>> futures = new ArrayList<>();
    
    long startTime = System.currentTimeMillis();
    
    for (int thread = 0; thread < numberOfThreads; thread++) {
      final int threadId = thread;
      CompletableFuture<List<ReconciliationRunResponse>> future = CompletableFuture.supplyAsync(() -> {
        List<ReconciliationRunResponse> runs = new ArrayList<>();
        for (int i = 0; i < runsPerThread; i++) {
          ReconciliationRunRequest runRequest = ReconciliationRunRequest.builder()
              .runDate(LocalDate.now().plusDays(threadId * runsPerThread + i))
              .description("Concurrent test run " + threadId + "-" + i)
              .build();
          
          ReconciliationRunResponse runResponse = reconciliationService.createReconciliationRun(runRequest);
          runs.add(runResponse);
        }
        return runs;
      }, executorService);
      
      futures.add(future);
    }
    
    // Wait for all threads to complete
    CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    allFutures.get(30, TimeUnit.SECONDS);
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    // Verify all runs were created
    int totalRuns = 0;
    for (CompletableFuture<List<ReconciliationRunResponse>> future : futures) {
      List<ReconciliationRunResponse> runs = future.join();
      totalRuns += runs.size();
      assertEquals(runsPerThread, runs.size());
    }
    
    assertEquals(numberOfThreads * runsPerThread, totalRuns);
    
    // Performance assertion: 1000 concurrent runs should be created in less than 30 seconds
    assertTrue(duration < 30000, "Creating 1000 concurrent reconciliation runs took too long: " + duration + "ms");
    
    // Calculate throughput
    double throughput = (double) totalRuns / (duration / 1000.0);
    System.out.println("Concurrent reconciliation run creation throughput: " + throughput + " runs/second");
  }
  
  @Test
  void testConcurrentExceptionOperations() throws InterruptedException {
    // Create a reconciliation run first
    ReconciliationRunRequest runRequest = ReconciliationRunRequest.builder()
        .runDate(LocalDate.now())
        .description("Concurrent exception test")
        .build();
    
    ReconciliationRunResponse runResponse = reconciliationService.createReconciliationRun(runRequest);
    assertNotNull(runResponse);
    
    // Test concurrent exception operations
    int numberOfThreads = 5;
    int exceptionsPerThread = 200;
    List<CompletableFuture<List<ReconciliationExceptionResponse>>> futures = new ArrayList<>();
    
    long startTime = System.currentTimeMillis();
    
    for (int thread = 0; thread < numberOfThreads; thread++) {
      final int threadId = thread;
      CompletableFuture<List<ReconciliationExceptionResponse>> future = CompletableFuture.supplyAsync(() -> {
        List<ReconciliationExceptionResponse> exceptions = new ArrayList<>();
        for (int i = 0; i < exceptionsPerThread; i++) {
          ReconciliationExceptionRequest exceptionRequest = ReconciliationExceptionRequest.builder()
              .runId(runResponse.getRunId())
              .exceptionType("AMOUNT_MISMATCH")
              .internalTransactionId("TXN" + threadId + "-" + String.format("%03d", i))
              .clearingTransactionId("CLR" + threadId + "-" + String.format("%03d", i))
              .amountDifference(BigDecimal.valueOf(Math.random() * 100))
              .description("Concurrent test exception " + threadId + "-" + i)
              .priority("HIGH")
              .build();
          
          ReconciliationExceptionResponse exceptionResponse = exceptionService.createException(exceptionRequest);
          exceptions.add(exceptionResponse);
        }
        return exceptions;
      }, executorService);
      
      futures.add(future);
    }
    
    // Wait for all threads to complete
    CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    allFutures.get(30, TimeUnit.SECONDS);
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    // Verify all exceptions were created
    int totalExceptions = 0;
    for (CompletableFuture<List<ReconciliationExceptionResponse>> future : futures) {
      List<ReconciliationExceptionResponse> exceptions = future.join();
      totalExceptions += exceptions.size();
      assertEquals(exceptionsPerThread, exceptions.size());
    }
    
    assertEquals(numberOfThreads * exceptionsPerThread, totalExceptions);
    
    // Performance assertion: 1000 concurrent exceptions should be created in less than 30 seconds
    assertTrue(duration < 30000, "Creating 1000 concurrent exceptions took too long: " + duration + "ms");
    
    // Calculate throughput
    double throughput = (double) totalExceptions / (duration / 1000.0);
    System.out.println("Concurrent exception creation throughput: " + throughput + " exceptions/second");
  }
  
  @Test
  void testExceptionResolutionPerformance() {
    // Create a reconciliation run first
    ReconciliationRunRequest runRequest = ReconciliationRunRequest.builder()
        .runDate(LocalDate.now())
        .description("Exception resolution performance test")
        .build();
    
    ReconciliationRunResponse runResponse = reconciliationService.createReconciliationRun(runRequest);
    assertNotNull(runResponse);
    
    // Create 1000 exceptions
    int numberOfExceptions = 1000;
    List<ReconciliationExceptionResponse> exceptions = new ArrayList<>();
    
    for (int i = 0; i < numberOfExceptions; i++) {
      ReconciliationExceptionRequest exceptionRequest = ReconciliationExceptionRequest.builder()
          .runId(runResponse.getRunId())
          .exceptionType("AMOUNT_MISMATCH")
          .internalTransactionId("TXN" + String.format("%04d", i))
          .clearingTransactionId("CLR" + String.format("%04d", i))
          .amountDifference(BigDecimal.valueOf(Math.random() * 100))
          .description("Resolution test exception " + i)
          .priority("HIGH")
          .build();
      
      ReconciliationExceptionResponse exceptionResponse = exceptionService.createException(exceptionRequest);
      exceptions.add(exceptionResponse);
    }
    
    // Test resolving all exceptions
    long startTime = System.currentTimeMillis();
    
    for (int i = 0; i < numberOfExceptions; i++) {
      ReconciliationExceptionResponse exception = exceptions.get(i);
      exceptionService.resolveException(exception.getExceptionId(),
          com.payments.reconciliation.dto.ReconciliationExceptionResolutionRequest.builder()
              .resolution("Resolved exception " + i)
              .resolutionNotes("Performance test resolution")
              .build());
    }
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    // Performance assertion: 1000 exception resolutions should complete in less than 10 seconds
    assertTrue(duration < 10000, "Resolving 1000 exceptions took too long: " + duration + "ms");
    
    // Calculate throughput
    double throughput = (double) numberOfExceptions / (duration / 1000.0);
    System.out.println("Exception resolution throughput: " + throughput + " resolutions/second");
    
    // Verify all exceptions are resolved
    List<ReconciliationExceptionResponse> resolvedExceptions = exceptionService.getExceptionsByStatus(
        ReconciliationException.ExceptionStatus.RESOLVED);
    assertEquals(numberOfExceptions, resolvedExceptions.size());
  }
  
  @Test
  void testReconciliationRunQueriesPerformance() {
    // Create 1000 reconciliation runs
    int numberOfRuns = 1000;
    List<ReconciliationRunResponse> runs = new ArrayList<>();
    
    for (int i = 0; i < numberOfRuns; i++) {
      ReconciliationRunRequest runRequest = ReconciliationRunRequest.builder()
          .runDate(LocalDate.now().plusDays(i))
          .description("Query performance test run " + i)
          .build();
      
      ReconciliationRunResponse runResponse = reconciliationService.createReconciliationRun(runRequest);
      runs.add(runResponse);
    }
    
    // Test query performance
    long startTime = System.currentTimeMillis();
    
    // Get all runs
    List<ReconciliationRunResponse> allRuns = reconciliationService.getAllReconciliationRuns();
    assertEquals(numberOfRuns, allRuns.size());
    
    // Get runs by status
    List<ReconciliationRunResponse> pendingRuns = reconciliationService.getReconciliationRunsByStatus(
        ReconciliationRun.ReconciliationStatus.PENDING);
    assertEquals(numberOfRuns, pendingRuns.size());
    
    // Get runs by date range
    List<ReconciliationRunResponse> todayRuns = reconciliationService.getReconciliationRunsByDateRange(
        LocalDate.now(), LocalDate.now());
    assertEquals(1, todayRuns.size());
    
    // Get runs by business unit
    List<ReconciliationRunResponse> businessUnitRuns = reconciliationService.getReconciliationRunsByBusinessUnit(
        businessUnitId.toString());
    assertEquals(numberOfRuns, businessUnitRuns.size());
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    // Performance assertion: All queries should complete in less than 5 seconds
    assertTrue(duration < 5000, "Reconciliation run queries took too long: " + duration + "ms");
    
    System.out.println("Reconciliation run queries completed in: " + duration + "ms");
  }
  
  @Test
  void testExceptionQueriesPerformance() {
    // Create a reconciliation run first
    ReconciliationRunRequest runRequest = ReconciliationRunRequest.builder()
        .runDate(LocalDate.now())
        .description("Exception query performance test")
        .build();
    
    ReconciliationRunResponse runResponse = reconciliationService.createReconciliationRun(runRequest);
    assertNotNull(runResponse);
    
    // Create 1000 exceptions
    int numberOfExceptions = 1000;
    List<ReconciliationExceptionResponse> exceptions = new ArrayList<>();
    
    for (int i = 0; i < numberOfExceptions; i++) {
      ReconciliationExceptionRequest exceptionRequest = ReconciliationExceptionRequest.builder()
          .runId(runResponse.getRunId())
          .exceptionType("AMOUNT_MISMATCH")
          .internalTransactionId("TXN" + String.format("%04d", i))
          .clearingTransactionId("CLR" + String.format("%04d", i))
          .amountDifference(BigDecimal.valueOf(Math.random() * 100))
          .description("Query test exception " + i)
          .priority("HIGH")
          .build();
      
      ReconciliationExceptionResponse exceptionResponse = exceptionService.createException(exceptionRequest);
      exceptions.add(exceptionResponse);
    }
    
    // Test query performance
    long startTime = System.currentTimeMillis();
    
    // Get all exceptions
    List<ReconciliationExceptionResponse> allExceptions = exceptionService.getAllExceptions();
    assertEquals(numberOfExceptions, allExceptions.size());
    
    // Get exceptions by run
    List<ReconciliationExceptionResponse> runExceptions = exceptionService.getExceptionsByRunId(runResponse.getRunId());
    assertEquals(numberOfExceptions, runExceptions.size());
    
    // Get exceptions by status
    List<ReconciliationExceptionResponse> openExceptions = exceptionService.getExceptionsByStatus(
        ReconciliationException.ExceptionStatus.OPEN);
    assertEquals(numberOfExceptions, openExceptions.size());
    
    // Get exceptions by type
    List<ReconciliationExceptionResponse> amountMismatchExceptions = exceptionService.getExceptionsByType("AMOUNT_MISMATCH");
    assertEquals(numberOfExceptions, amountMismatchExceptions.size());
    
    // Get exceptions by priority
    List<ReconciliationExceptionResponse> highPriorityExceptions = exceptionService.getExceptionsByPriority("HIGH");
    assertEquals(numberOfExceptions, highPriorityExceptions.size());
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    // Performance assertion: All queries should complete in less than 5 seconds
    assertTrue(duration < 5000, "Exception queries took too long: " + duration + "ms");
    
    System.out.println("Exception queries completed in: " + duration + "ms");
  }
  
  @Test
  void testReconciliationRunStatisticsPerformance() {
    // Create a reconciliation run first
    ReconciliationRunRequest runRequest = ReconciliationRunRequest.builder()
        .runDate(LocalDate.now())
        .description("Statistics performance test")
        .build();
    
    ReconciliationRunResponse runResponse = reconciliationService.createReconciliationRun(runRequest);
    assertNotNull(runResponse);
    
    // Create 1000 exceptions
    int numberOfExceptions = 1000;
    List<ReconciliationExceptionResponse> exceptions = new ArrayList<>();
    
    for (int i = 0; i < numberOfExceptions; i++) {
      ReconciliationExceptionRequest exceptionRequest = ReconciliationExceptionRequest.builder()
          .runId(runResponse.getRunId())
          .exceptionType("AMOUNT_MISMATCH")
          .internalTransactionId("TXN" + String.format("%04d", i))
          .clearingTransactionId("CLR" + String.format("%04d", i))
          .amountDifference(BigDecimal.valueOf(Math.random() * 100))
          .description("Statistics test exception " + i)
          .priority("HIGH")
          .build();
      
      ReconciliationExceptionResponse exceptionResponse = exceptionService.createException(exceptionRequest);
      exceptions.add(exceptionResponse);
    }
    
    // Resolve half of the exceptions
    for (int i = 0; i < numberOfExceptions / 2; i++) {
      ReconciliationExceptionResponse exception = exceptions.get(i);
      exceptionService.resolveException(exception.getExceptionId(),
          com.payments.reconciliation.dto.ReconciliationExceptionResolutionRequest.builder()
              .resolution("Resolved exception " + i)
              .resolutionNotes("Statistics test resolution")
              .build());
    }
    
    // Test statistics calculation performance
    long startTime = System.currentTimeMillis();
    
    ReconciliationExceptionService.ReconciliationExceptionStatistics statistics = 
        exceptionService.getExceptionStatistics(runResponse.getRunId());
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    // Verify statistics
    assertNotNull(statistics);
    assertEquals(runResponse.getRunId(), statistics.getRunId());
    assertEquals(numberOfExceptions, statistics.getTotalExceptions());
    assertEquals(numberOfExceptions / 2, statistics.getOpenExceptions());
    assertEquals(numberOfExceptions / 2, statistics.getResolvedExceptions());
    assertEquals(0L, statistics.getClosedExceptions());
    assertEquals(0.5, statistics.getResolutionRate(), 0.01);
    
    // Performance assertion: Statistics calculation should complete in less than 1 second
    assertTrue(duration < 1000, "Statistics calculation took too long: " + duration + "ms");
    
    System.out.println("Statistics calculation completed in: " + duration + "ms");
  }
}
