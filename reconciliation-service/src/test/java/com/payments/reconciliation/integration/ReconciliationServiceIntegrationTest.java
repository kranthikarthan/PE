package com.payments.reconciliation.integration;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Reconciliation Service.
 *
 * <p>This test class provides comprehensive integration tests for the
 * reconciliation service including end-to-end workflows, database
 * operations, and service interactions.
 *
 * @since PE-413
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReconciliationServiceIntegrationTest {
  
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
    
    // Clear data before each test
    exceptionRepository.deleteAll();
    runRepository.deleteAll();
  }
  
  @Test
  void testEndToEndReconciliationWorkflow() {
    // 1. Create reconciliation run
    ReconciliationRunRequest runRequest = ReconciliationRunRequest.builder()
        .runDate(LocalDate.now())
        .description("Daily reconciliation run")
        .build();
    
    ReconciliationRunResponse runResponse = reconciliationService.createReconciliationRun(runRequest);
    assertNotNull(runResponse);
    assertNotNull(runResponse.getRunId());
    assertEquals("PENDING", runResponse.getStatus());
    assertEquals(LocalDate.now(), runResponse.getRunDate());
    assertEquals("Daily reconciliation run", runResponse.getDescription());
    
    // 2. Start reconciliation run
    ReconciliationRunResponse startedRun = reconciliationService.startReconciliationRun(runResponse.getRunId());
    assertNotNull(startedRun);
    assertEquals("IN_PROGRESS", startedRun.getStatus());
    assertNotNull(startedRun.getStartedAt());
    
    // 3. Create exceptions during reconciliation
    ReconciliationExceptionRequest exceptionRequest1 = ReconciliationExceptionRequest.builder()
        .runId(runResponse.getRunId())
        .exceptionType("AMOUNT_MISMATCH")
        .internalTransactionId("TXN001")
        .clearingTransactionId("CLR001")
        .amountDifference(java.math.BigDecimal.valueOf(10.50))
        .description("Amount mismatch between internal and clearing")
        .details("Internal amount: 100.00, Clearing amount: 110.50")
        .priority("HIGH")
        .assignedTo("operator1")
        .build();
    
    ReconciliationExceptionResponse exceptionResponse1 = exceptionService.createException(exceptionRequest1);
    assertNotNull(exceptionResponse1);
    assertEquals("AMOUNT_MISMATCH", exceptionResponse1.getExceptionType());
    assertEquals("TXN001", exceptionResponse1.getInternalTransactionId());
    assertEquals("CLR001", exceptionResponse1.getClearingTransactionId());
    assertEquals(java.math.BigDecimal.valueOf(10.50), exceptionResponse1.getAmountDifference());
    assertEquals("OPEN", exceptionResponse1.getStatus());
    
    ReconciliationExceptionRequest exceptionRequest2 = ReconciliationExceptionRequest.builder()
        .runId(runResponse.getRunId())
        .exceptionType("MISSING_TRANSACTION")
        .internalTransactionId("TXN002")
        .description("Transaction not found in clearing system")
        .priority("MEDIUM")
        .assignedTo("operator2")
        .build();
    
    ReconciliationExceptionResponse exceptionResponse2 = exceptionService.createException(exceptionRequest2);
    assertNotNull(exceptionResponse2);
    assertEquals("MISSING_TRANSACTION", exceptionResponse2.getExceptionType());
    assertEquals("TXN002", exceptionResponse2.getInternalTransactionId());
    assertEquals("OPEN", exceptionResponse2.getStatus());
    
    // 4. Assign and resolve exceptions
    ReconciliationExceptionResponse assignedException1 = exceptionService.assignException(
        exceptionResponse1.getExceptionId(), "operator1");
    assertNotNull(assignedException1);
    assertEquals("operator1", assignedException1.getAssignedTo());
    
    ReconciliationExceptionResponse assignedException2 = exceptionService.assignException(
        exceptionResponse2.getExceptionId(), "operator2");
    assertNotNull(assignedException2);
    assertEquals("operator2", assignedException2.getAssignedTo());
    
    // 5. Resolve exceptions
    ReconciliationExceptionResponse resolvedException1 = exceptionService.resolveException(
        exceptionResponse1.getExceptionId(),
        com.payments.reconciliation.dto.ReconciliationExceptionResolutionRequest.builder()
            .resolution("Manual adjustment applied")
            .resolutionNotes("Adjusted amount to match clearing system")
            .build());
    assertNotNull(resolvedException1);
    assertEquals("RESOLVED", resolvedException1.getStatus());
    assertEquals("Manual adjustment applied", resolvedException1.getResolution());
    assertNotNull(resolvedException1.getResolvedAt());
    
    ReconciliationExceptionResponse resolvedException2 = exceptionService.resolveException(
        exceptionResponse2.getExceptionId(),
        com.payments.reconciliation.dto.ReconciliationExceptionResolutionRequest.builder()
            .resolution("Transaction found and matched")
            .resolutionNotes("Transaction was in different clearing batch")
            .build());
    assertNotNull(resolvedException2);
    assertEquals("RESOLVED", resolvedException2.getStatus());
    assertEquals("Transaction found and matched", resolvedException2.getResolution());
    assertNotNull(resolvedException2.getResolvedAt());
    
    // 6. Complete reconciliation run
    ReconciliationRunResponse completedRun = reconciliationService.completeReconciliationRun(runResponse.getRunId());
    assertNotNull(completedRun);
    assertEquals("COMPLETED", completedRun.getStatus());
    assertNotNull(completedRun.getCompletedAt());
    
    // 7. Verify final state
    List<ReconciliationExceptionResponse> allExceptions = exceptionService.getExceptionsByRunId(runResponse.getRunId());
    assertNotNull(allExceptions);
    assertEquals(2, allExceptions.size());
    
    // Verify exception statistics
    ReconciliationExceptionService.ReconciliationExceptionStatistics statistics = 
        exceptionService.getExceptionStatistics(runResponse.getRunId());
    assertNotNull(statistics);
    assertEquals(runResponse.getRunId(), statistics.getRunId());
    assertEquals(2L, statistics.getTotalExceptions());
    assertEquals(0L, statistics.getOpenExceptions());
    assertEquals(2L, statistics.getResolvedExceptions());
    assertEquals(0L, statistics.getClosedExceptions());
    assertEquals(1.0, statistics.getResolutionRate(), 0.01);
  }
  
  @Test
  void testReconciliationRunLifecycle() {
    // 1. Create reconciliation run
    ReconciliationRunRequest runRequest = ReconciliationRunRequest.builder()
        .runDate(LocalDate.now())
        .description("Test reconciliation run")
        .build();
    
    ReconciliationRunResponse runResponse = reconciliationService.createReconciliationRun(runRequest);
    assertNotNull(runResponse);
    assertEquals("PENDING", runResponse.getStatus());
    
    // 2. Start reconciliation run
    ReconciliationRunResponse startedRun = reconciliationService.startReconciliationRun(runResponse.getRunId());
    assertNotNull(startedRun);
    assertEquals("IN_PROGRESS", startedRun.getStatus());
    assertNotNull(startedRun.getStartedAt());
    
    // 3. Update reconciliation run with progress
    ReconciliationRunResponse updatedRun = reconciliationService.updateReconciliationRun(
        runResponse.getRunId(),
        com.payments.reconciliation.dto.ReconciliationRunUpdateRequest.builder()
            .totalInternal(100)
            .totalClearing(95)
            .matchedCount(90)
            .exceptionCount(5)
            .build());
    assertNotNull(updatedRun);
    assertEquals(100, updatedRun.getTotalInternal());
    assertEquals(95, updatedRun.getTotalClearing());
    assertEquals(90, updatedRun.getMatchedCount());
    assertEquals(5, updatedRun.getExceptionCount());
    
    // 4. Complete reconciliation run
    ReconciliationRunResponse completedRun = reconciliationService.completeReconciliationRun(runResponse.getRunId());
    assertNotNull(completedRun);
    assertEquals("COMPLETED", completedRun.getStatus());
    assertNotNull(completedRun.getCompletedAt());
    
    // 5. Verify final state
    ReconciliationRunResponse finalRun = reconciliationService.getReconciliationRun(runResponse.getRunId());
    assertNotNull(finalRun);
    assertEquals("COMPLETED", finalRun.getStatus());
    assertEquals(100, finalRun.getTotalInternal());
    assertEquals(95, finalRun.getTotalClearing());
    assertEquals(90, finalRun.getMatchedCount());
    assertEquals(5, finalRun.getExceptionCount());
    assertNotNull(finalRun.getStartedAt());
    assertNotNull(finalRun.getCompletedAt());
  }
  
  @Test
  void testExceptionWorkflow() {
    // 1. Create reconciliation run
    ReconciliationRunRequest runRequest = ReconciliationRunRequest.builder()
        .runDate(LocalDate.now())
        .description("Exception workflow test")
        .build();
    
    ReconciliationRunResponse runResponse = reconciliationService.createReconciliationRun(runRequest);
    assertNotNull(runResponse);
    
    // 2. Create multiple exceptions
    ReconciliationExceptionRequest exceptionRequest1 = ReconciliationExceptionRequest.builder()
        .runId(runResponse.getRunId())
        .exceptionType("AMOUNT_MISMATCH")
        .internalTransactionId("TXN001")
        .clearingTransactionId("CLR001")
        .amountDifference(java.math.BigDecimal.valueOf(10.50))
        .description("Amount mismatch")
        .priority("HIGH")
        .build();
    
    ReconciliationExceptionResponse exception1 = exceptionService.createException(exceptionRequest1);
    assertNotNull(exception1);
    assertEquals("OPEN", exception1.getStatus());
    
    ReconciliationExceptionRequest exceptionRequest2 = ReconciliationExceptionRequest.builder()
        .runId(runResponse.getRunId())
        .exceptionType("MISSING_TRANSACTION")
        .internalTransactionId("TXN002")
        .description("Missing transaction")
        .priority("MEDIUM")
        .build();
    
    ReconciliationExceptionResponse exception2 = exceptionService.createException(exceptionRequest2);
    assertNotNull(exception2);
    assertEquals("OPEN", exception2.getStatus());
    
    ReconciliationExceptionRequest exceptionRequest3 = ReconciliationExceptionRequest.builder()
        .runId(runResponse.getRunId())
        .exceptionType("DUPLICATE_TRANSACTION")
        .internalTransactionId("TXN003")
        .clearingTransactionId("CLR003")
        .description("Duplicate transaction")
        .priority("LOW")
        .build();
    
    ReconciliationExceptionResponse exception3 = exceptionService.createException(exceptionRequest3);
    assertNotNull(exception3);
    assertEquals("OPEN", exception3.getStatus());
    
    // 3. Assign exceptions to different users
    ReconciliationExceptionResponse assignedException1 = exceptionService.assignException(
        exception1.getExceptionId(), "operator1");
    assertEquals("operator1", assignedException1.getAssignedTo());
    
    ReconciliationExceptionResponse assignedException2 = exceptionService.assignException(
        exception2.getExceptionId(), "operator2");
    assertEquals("operator2", assignedException2.getAssignedTo());
    
    ReconciliationExceptionResponse assignedException3 = exceptionService.assignException(
        exception3.getExceptionId(), "operator1");
    assertEquals("operator1", assignedException3.getAssignedTo());
    
    // 4. Resolve exceptions
    ReconciliationExceptionResponse resolvedException1 = exceptionService.resolveException(
        exception1.getExceptionId(),
        com.payments.reconciliation.dto.ReconciliationExceptionResolutionRequest.builder()
            .resolution("Manual adjustment applied")
            .resolutionNotes("Adjusted amount to match clearing system")
            .build());
    assertEquals("RESOLVED", resolvedException1.getStatus());
    
    ReconciliationExceptionResponse resolvedException2 = exceptionService.resolveException(
        exception2.getExceptionId(),
        com.payments.reconciliation.dto.ReconciliationExceptionResolutionRequest.builder()
            .resolution("Transaction found and matched")
            .resolutionNotes("Transaction was in different clearing batch")
            .build());
    assertEquals("RESOLVED", resolvedException2.getStatus());
    
    // 5. Verify exception statistics
    ReconciliationExceptionService.ReconciliationExceptionStatistics statistics = 
        exceptionService.getExceptionStatistics(runResponse.getRunId());
    assertNotNull(statistics);
    assertEquals(3L, statistics.getTotalExceptions());
    assertEquals(1L, statistics.getOpenExceptions());
    assertEquals(2L, statistics.getResolvedExceptions());
    assertEquals(0L, statistics.getClosedExceptions());
    assertEquals(0.67, statistics.getResolutionRate(), 0.01);
    
    // 6. Get exceptions by status
    List<ReconciliationExceptionResponse> openExceptions = exceptionService.getExceptionsByStatus(
        ReconciliationException.ExceptionStatus.OPEN);
    assertNotNull(openExceptions);
    assertEquals(1, openExceptions.size());
    assertEquals("DUPLICATE_TRANSACTION", openExceptions.get(0).getExceptionType());
    
    List<ReconciliationExceptionResponse> resolvedExceptions = exceptionService.getExceptionsByStatus(
        ReconciliationException.ExceptionStatus.RESOLVED);
    assertNotNull(resolvedExceptions);
    assertEquals(2, resolvedExceptions.size());
    
    // 7. Get exceptions by assigned user
    List<ReconciliationExceptionResponse> operator1Exceptions = exceptionService.getExceptionsByAssignedTo("operator1");
    assertNotNull(operator1Exceptions);
    assertEquals(2, operator1Exceptions.size());
    
    List<ReconciliationExceptionResponse> operator2Exceptions = exceptionService.getExceptionsByAssignedTo("operator2");
    assertNotNull(operator2Exceptions);
    assertEquals(1, operator2Exceptions.size());
  }
  
  @Test
  void testReconciliationRunQueries() {
    // 1. Create multiple reconciliation runs
    ReconciliationRunRequest runRequest1 = ReconciliationRunRequest.builder()
        .runDate(LocalDate.now().minusDays(1))
        .description("Yesterday's reconciliation")
        .build();
    
    ReconciliationRunResponse run1 = reconciliationService.createReconciliationRun(runRequest1);
    assertNotNull(run1);
    
    ReconciliationRunRequest runRequest2 = ReconciliationRunRequest.builder()
        .runDate(LocalDate.now())
        .description("Today's reconciliation")
        .build();
    
    ReconciliationRunResponse run2 = reconciliationService.createReconciliationRun(runRequest2);
    assertNotNull(run2);
    
    ReconciliationRunRequest runRequest3 = ReconciliationRunRequest.builder()
        .runDate(LocalDate.now().plusDays(1))
        .description("Tomorrow's reconciliation")
        .build();
    
    ReconciliationRunResponse run3 = reconciliationService.createReconciliationRun(runRequest3);
    assertNotNull(run3);
    
    // 2. Start and complete some runs
    reconciliationService.startReconciliationRun(run1.getRunId());
    reconciliationService.completeReconciliationRun(run1.getRunId());
    
    reconciliationService.startReconciliationRun(run2.getRunId());
    
    // 3. Get all reconciliation runs
    List<ReconciliationRunResponse> allRuns = reconciliationService.getAllReconciliationRuns();
    assertNotNull(allRuns);
    assertEquals(3, allRuns.size());
    
    // 4. Get runs by status
    List<ReconciliationRunResponse> completedRuns = reconciliationService.getReconciliationRunsByStatus(
        ReconciliationRun.ReconciliationStatus.COMPLETED);
    assertNotNull(completedRuns);
    assertEquals(1, completedRuns.size());
    assertEquals(run1.getRunId(), completedRuns.get(0).getRunId());
    
    List<ReconciliationRunResponse> inProgressRuns = reconciliationService.getReconciliationRunsByStatus(
        ReconciliationRun.ReconciliationStatus.IN_PROGRESS);
    assertNotNull(inProgressRuns);
    assertEquals(1, inProgressRuns.size());
    assertEquals(run2.getRunId(), inProgressRuns.get(0).getRunId());
    
    List<ReconciliationRunResponse> pendingRuns = reconciliationService.getReconciliationRunsByStatus(
        ReconciliationRun.ReconciliationStatus.PENDING);
    assertNotNull(pendingRuns);
    assertEquals(1, pendingRuns.size());
    assertEquals(run3.getRunId(), pendingRuns.get(0).getRunId());
    
    // 5. Get runs by date range
    List<ReconciliationRunResponse> todayRuns = reconciliationService.getReconciliationRunsByDateRange(
        LocalDate.now(), LocalDate.now());
    assertNotNull(todayRuns);
    assertEquals(1, todayRuns.size());
    assertEquals(run2.getRunId(), todayRuns.get(0).getRunId());
    
    // 6. Get runs by business unit
    List<ReconciliationRunResponse> businessUnitRuns = reconciliationService.getReconciliationRunsByBusinessUnit(
        businessUnitId.toString());
    assertNotNull(businessUnitRuns);
    assertEquals(3, businessUnitRuns.size());
  }
  
  @Test
  void testExceptionQueries() {
    // 1. Create reconciliation run
    ReconciliationRunRequest runRequest = ReconciliationRunRequest.builder()
        .runDate(LocalDate.now())
        .description("Exception query test")
        .build();
    
    ReconciliationRunResponse runResponse = reconciliationService.createReconciliationRun(runRequest);
    assertNotNull(runResponse);
    
    // 2. Create exceptions with different types and priorities
    ReconciliationExceptionRequest exceptionRequest1 = ReconciliationExceptionRequest.builder()
        .runId(runResponse.getRunId())
        .exceptionType("AMOUNT_MISMATCH")
        .internalTransactionId("TXN001")
        .description("Amount mismatch")
        .priority("HIGH")
        .assignedTo("operator1")
        .build();
    
    ReconciliationExceptionResponse exception1 = exceptionService.createException(exceptionRequest1);
    assertNotNull(exception1);
    
    ReconciliationExceptionRequest exceptionRequest2 = ReconciliationExceptionRequest.builder()
        .runId(runResponse.getRunId())
        .exceptionType("MISSING_TRANSACTION")
        .internalTransactionId("TXN002")
        .description("Missing transaction")
        .priority("MEDIUM")
        .assignedTo("operator2")
        .build();
    
    ReconciliationExceptionResponse exception2 = exceptionService.createException(exceptionRequest2);
    assertNotNull(exception2);
    
    ReconciliationExceptionRequest exceptionRequest3 = ReconciliationExceptionRequest.builder()
        .runId(runResponse.getRunId())
        .exceptionType("DUPLICATE_TRANSACTION")
        .internalTransactionId("TXN003")
        .description("Duplicate transaction")
        .priority("LOW")
        .build();
    
    ReconciliationExceptionResponse exception3 = exceptionService.createException(exceptionRequest3);
    assertNotNull(exception3);
    
    // 3. Get all exceptions
    List<ReconciliationExceptionResponse> allExceptions = exceptionService.getAllExceptions();
    assertNotNull(allExceptions);
    assertEquals(3, allExceptions.size());
    
    // 4. Get exceptions by run
    List<ReconciliationExceptionResponse> runExceptions = exceptionService.getExceptionsByRunId(runResponse.getRunId());
    assertNotNull(runExceptions);
    assertEquals(3, runExceptions.size());
    
    // 5. Get exceptions by status
    List<ReconciliationExceptionResponse> openExceptions = exceptionService.getExceptionsByStatus(
        ReconciliationException.ExceptionStatus.OPEN);
    assertNotNull(openExceptions);
    assertEquals(3, openExceptions.size());
    
    // 6. Get exceptions by assigned user
    List<ReconciliationExceptionResponse> operator1Exceptions = exceptionService.getExceptionsByAssignedTo("operator1");
    assertNotNull(operator1Exceptions);
    assertEquals(1, operator1Exceptions.size());
    assertEquals("AMOUNT_MISMATCH", operator1Exceptions.get(0).getExceptionType());
    
    List<ReconciliationExceptionResponse> operator2Exceptions = exceptionService.getExceptionsByAssignedTo("operator2");
    assertNotNull(operator2Exceptions);
    assertEquals(1, operator2Exceptions.size());
    assertEquals("MISSING_TRANSACTION", operator2Exceptions.get(0).getExceptionType());
    
    // 7. Get exceptions by type
    List<ReconciliationExceptionResponse> amountMismatchExceptions = exceptionService.getExceptionsByType("AMOUNT_MISMATCH");
    assertNotNull(amountMismatchExceptions);
    assertEquals(1, amountMismatchExceptions.size());
    assertEquals("TXN001", amountMismatchExceptions.get(0).getInternalTransactionId());
    
    // 8. Get exceptions by priority
    List<ReconciliationExceptionResponse> highPriorityExceptions = exceptionService.getExceptionsByPriority("HIGH");
    assertNotNull(highPriorityExceptions);
    assertEquals(1, highPriorityExceptions.size());
    assertEquals("AMOUNT_MISMATCH", highPriorityExceptions.get(0).getExceptionType());
    
    List<ReconciliationExceptionResponse> mediumPriorityExceptions = exceptionService.getExceptionsByPriority("MEDIUM");
    assertNotNull(mediumPriorityExceptions);
    assertEquals(1, mediumPriorityExceptions.size());
    assertEquals("MISSING_TRANSACTION", mediumPriorityExceptions.get(0).getExceptionType());
    
    List<ReconciliationExceptionResponse> lowPriorityExceptions = exceptionService.getExceptionsByPriority("LOW");
    assertNotNull(lowPriorityExceptions);
    assertEquals(1, lowPriorityExceptions.size());
    assertEquals("DUPLICATE_TRANSACTION", lowPriorityExceptions.get(0).getExceptionType());
  }
  
  @Test
  void testReconciliationRunStatistics() {
    // 1. Create reconciliation run
    ReconciliationRunRequest runRequest = ReconciliationRunRequest.builder()
        .runDate(LocalDate.now())
        .description("Statistics test")
        .build();
    
    ReconciliationRunResponse runResponse = reconciliationService.createReconciliationRun(runRequest);
    assertNotNull(runResponse);
    
    // 2. Start reconciliation run
    reconciliationService.startReconciliationRun(runResponse.getRunId());
    
    // 3. Update with statistics
    reconciliationService.updateReconciliationRun(runResponse.getRunId(),
        com.payments.reconciliation.dto.ReconciliationRunUpdateRequest.builder()
            .totalInternal(1000)
            .totalClearing(950)
            .matchedCount(900)
            .exceptionCount(50)
            .build());
    
    // 4. Create exceptions
    for (int i = 1; i <= 50; i++) {
      ReconciliationExceptionRequest exceptionRequest = ReconciliationExceptionRequest.builder()
          .runId(runResponse.getRunId())
          .exceptionType("AMOUNT_MISMATCH")
          .internalTransactionId("TXN" + String.format("%03d", i))
          .description("Amount mismatch " + i)
          .priority("HIGH")
          .build();
      
      exceptionService.createException(exceptionRequest);
    }
    
    // 5. Resolve some exceptions
    List<ReconciliationExceptionResponse> allExceptions = exceptionService.getExceptionsByRunId(runResponse.getRunId());
    for (int i = 0; i < 30; i++) {
      exceptionService.resolveException(allExceptions.get(i).getExceptionId(),
          com.payments.reconciliation.dto.ReconciliationExceptionResolutionRequest.builder()
              .resolution("Resolved exception " + (i + 1))
              .resolutionNotes("Manual resolution applied")
              .build());
    }
    
    // 6. Complete reconciliation run
    reconciliationService.completeReconciliationRun(runResponse.getRunId());
    
    // 7. Verify statistics
    ReconciliationExceptionService.ReconciliationExceptionStatistics statistics = 
        exceptionService.getExceptionStatistics(runResponse.getRunId());
    assertNotNull(statistics);
    assertEquals(runResponse.getRunId(), statistics.getRunId());
    assertEquals(50L, statistics.getTotalExceptions());
    assertEquals(20L, statistics.getOpenExceptions());
    assertEquals(30L, statistics.getResolvedExceptions());
    assertEquals(0L, statistics.getClosedExceptions());
    assertEquals(0.6, statistics.getResolutionRate(), 0.01);
    
    // 8. Get final reconciliation run
    ReconciliationRunResponse finalRun = reconciliationService.getReconciliationRun(runResponse.getRunId());
    assertNotNull(finalRun);
    assertEquals("COMPLETED", finalRun.getStatus());
    assertEquals(1000, finalRun.getTotalInternal());
    assertEquals(950, finalRun.getTotalClearing());
    assertEquals(900, finalRun.getMatchedCount());
    assertEquals(50, finalRun.getExceptionCount());
  }
}
