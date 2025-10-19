package com.payments.reconciliation.service;

import com.payments.domain.reconciliation.ReconciliationException;
import com.payments.domain.reconciliation.ReconciliationRun;
import com.payments.reconciliation.repository.ReconciliationExceptionRepository;
import com.payments.reconciliation.repository.ReconciliationRunRepository;
import com.payments.reconciliation.dto.ReconciliationExceptionRequest;
import com.payments.reconciliation.dto.ReconciliationExceptionResponse;
import com.payments.reconciliation.dto.ReconciliationExceptionResolutionRequest;
import com.payments.reconciliation.exception.ReconciliationExceptionServiceException;
import com.payments.domain.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReconciliationExceptionService.
 *
 * <p>This test class provides comprehensive unit tests for the
 * reconciliation exception service including success scenarios,
 * error handling, and edge cases.
 *
 * @since PE-412
 */
@ExtendWith(MockitoExtension.class)
class ReconciliationExceptionServiceTest {
  
  @Mock
  private ReconciliationExceptionRepository exceptionRepository;
  
  @Mock
  private ReconciliationRunRepository runRepository;
  
  @InjectMocks
  private ReconciliationExceptionService exceptionService;
  
  private UUID tenantId;
  private UUID businessUnitId;
  private String userId;
  private Long runId;
  
  @BeforeEach
  void setUp() {
    tenantId = UUID.randomUUID();
    businessUnitId = UUID.randomUUID();
    userId = "test-user";
    runId = 1L;
    
    TenantContext.setContext(TenantContext.builder()
        .tenantId(tenantId)
        .tenantName("TestTenant")
        .businessUnitId(businessUnitId)
        .businessUnitName("TestBusinessUnit")
        .build());
  }
  
  @Test
  void testCreateException_Success() {
    // Given
    ReconciliationExceptionRequest request = ReconciliationExceptionRequest.builder()
        .runId(runId)
        .exceptionType("AMOUNT_MISMATCH")
        .internalTransactionId("TXN001")
        .clearingTransactionId("CLR001")
        .amountDifference(BigDecimal.valueOf(10.50))
        .description("Amount mismatch between internal and clearing")
        .details("Internal amount: 100.00, Clearing amount: 110.50")
        .priority("HIGH")
        .assignedTo("operator1")
        .build();
    
    ReconciliationRun reconciliationRun = ReconciliationRun.builder()
        .id(runId)
        .runId("run-123")
        .runDate(LocalDateTime.now().toLocalDate())
        .status(ReconciliationRun.ReconciliationStatus.IN_PROGRESS)
        .tenantId(tenantId)
        .build();
    
    ReconciliationException savedException = ReconciliationException.builder()
        .id(1L)
        .exceptionId("exception-123")
        .runId(runId)
        .exceptionType("AMOUNT_MISMATCH")
        .internalTransactionId("TXN001")
        .clearingTransactionId("CLR001")
        .amountDifference(BigDecimal.valueOf(10.50))
        .status(ReconciliationException.ExceptionStatus.OPEN)
        .description("Amount mismatch between internal and clearing")
        .details("Internal amount: 100.00, Clearing amount: 110.50")
        .priority("HIGH")
        .assignedTo("operator1")
        .tenantId(tenantId)
        .businessUnitId(businessUnitId.toString())
        .createdAt(LocalDateTime.now())
        .createdBy(userId)
        .build();
    
    when(runRepository.findByIdAndTenantId(runId, tenantId)).thenReturn(Optional.of(reconciliationRun));
    when(exceptionRepository.save(any(ReconciliationException.class))).thenReturn(savedException);
    
    // When
    ReconciliationExceptionResponse response = exceptionService.createException(request);
    
    // Then
    assertNotNull(response);
    assertEquals("exception-123", response.getExceptionId());
    assertEquals(runId, response.getRunId());
    assertEquals("AMOUNT_MISMATCH", response.getExceptionType());
    assertEquals("TXN001", response.getInternalTransactionId());
    assertEquals("CLR001", response.getClearingTransactionId());
    assertEquals(BigDecimal.valueOf(10.50), response.getAmountDifference());
    assertEquals(ReconciliationException.ExceptionStatus.OPEN, response.getStatus());
    assertEquals("Amount mismatch between internal and clearing", response.getDescription());
    assertEquals("Internal amount: 100.00, Clearing amount: 110.50", response.getDetails());
    assertEquals("HIGH", response.getPriority());
    assertEquals("operator1", response.getAssignedTo());
    
    verify(runRepository).findByIdAndTenantId(runId, tenantId);
    verify(exceptionRepository).save(any(ReconciliationException.class));
  }
  
  @Test
  void testCreateException_RunNotFound() {
    // Given
    ReconciliationExceptionRequest request = ReconciliationExceptionRequest.builder()
        .runId(runId)
        .exceptionType("AMOUNT_MISMATCH")
        .build();
    
    when(runRepository.findByIdAndTenantId(runId, tenantId)).thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(ReconciliationExceptionServiceException.class, () -> {
      exceptionService.createException(request);
    });
    
    verify(runRepository).findByIdAndTenantId(runId, tenantId);
    verify(exceptionRepository, never()).save(any(ReconciliationException.class));
  }
  
  @Test
  void testCreateException_Exception() {
    // Given
    ReconciliationExceptionRequest request = ReconciliationExceptionRequest.builder()
        .runId(runId)
        .exceptionType("AMOUNT_MISMATCH")
        .build();
    
    when(runRepository.findByIdAndTenantId(runId, tenantId))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(ReconciliationExceptionServiceException.class, () -> {
      exceptionService.createException(request);
    });
    
    verify(runRepository).findByIdAndTenantId(runId, tenantId);
  }
  
  @Test
  void testAssignException_Success() {
    // Given
    String exceptionId = "exception-123";
    String assignedTo = "operator2";
    
    ReconciliationException exception = ReconciliationException.builder()
        .id(1L)
        .exceptionId(exceptionId)
        .runId(runId)
        .exceptionType("AMOUNT_MISMATCH")
        .status(ReconciliationException.ExceptionStatus.OPEN)
        .tenantId(tenantId)
        .build();
    
    ReconciliationException assignedException = ReconciliationException.builder()
        .id(1L)
        .exceptionId(exceptionId)
        .runId(runId)
        .exceptionType("AMOUNT_MISMATCH")
        .status(ReconciliationException.ExceptionStatus.OPEN)
        .assignedTo(assignedTo)
        .tenantId(tenantId)
        .build();
    
    when(exceptionRepository.findByExceptionIdAndTenantId(exceptionId, tenantId))
        .thenReturn(Optional.of(exception));
    when(exceptionRepository.save(any(ReconciliationException.class)))
        .thenReturn(assignedException);
    
    // When
    ReconciliationExceptionResponse response = exceptionService.assignException(exceptionId, assignedTo);
    
    // Then
    assertNotNull(response);
    assertEquals(exceptionId, response.getExceptionId());
    assertEquals(runId, response.getRunId());
    assertEquals("AMOUNT_MISMATCH", response.getExceptionType());
    assertEquals(ReconciliationException.ExceptionStatus.OPEN, response.getStatus());
    assertEquals(assignedTo, response.getAssignedTo());
    
    verify(exceptionRepository).findByExceptionIdAndTenantId(exceptionId, tenantId);
    verify(exceptionRepository).save(any(ReconciliationException.class));
  }
  
  @Test
  void testAssignException_NotFound() {
    // Given
    String exceptionId = "exception-123";
    String assignedTo = "operator2";
    
    when(exceptionRepository.findByExceptionIdAndTenantId(exceptionId, tenantId))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(ReconciliationExceptionServiceException.class, () -> {
      exceptionService.assignException(exceptionId, assignedTo);
    });
    
    verify(exceptionRepository).findByExceptionIdAndTenantId(exceptionId, tenantId);
    verify(exceptionRepository, never()).save(any(ReconciliationException.class));
  }
  
  @Test
  void testAssignException_Exception() {
    // Given
    String exceptionId = "exception-123";
    String assignedTo = "operator2";
    
    when(exceptionRepository.findByExceptionIdAndTenantId(exceptionId, tenantId))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(ReconciliationExceptionServiceException.class, () -> {
      exceptionService.assignException(exceptionId, assignedTo);
    });
    
    verify(exceptionRepository).findByExceptionIdAndTenantId(exceptionId, tenantId);
  }
  
  @Test
  void testResolveException_Success() {
    // Given
    String exceptionId = "exception-123";
    ReconciliationExceptionResolutionRequest request = ReconciliationExceptionResolutionRequest.builder()
        .resolution("Manual adjustment applied")
        .resolutionNotes("Adjusted amount to match clearing system")
        .build();
    
    ReconciliationException exception = ReconciliationException.builder()
        .id(1L)
        .exceptionId(exceptionId)
        .runId(runId)
        .exceptionType("AMOUNT_MISMATCH")
        .status(ReconciliationException.ExceptionStatus.OPEN)
        .tenantId(tenantId)
        .build();
    
    ReconciliationException resolvedException = ReconciliationException.builder()
        .id(1L)
        .exceptionId(exceptionId)
        .runId(runId)
        .exceptionType("AMOUNT_MISMATCH")
        .status(ReconciliationException.ExceptionStatus.RESOLVED)
        .resolution("Manual adjustment applied")
        .resolutionNotes("Adjusted amount to match clearing system")
        .resolvedAt(LocalDateTime.now())
        .resolvedBy(userId)
        .tenantId(tenantId)
        .build();
    
    when(exceptionRepository.findByExceptionIdAndTenantId(exceptionId, tenantId))
        .thenReturn(Optional.of(exception));
    when(exceptionRepository.save(any(ReconciliationException.class)))
        .thenReturn(resolvedException);
    
    // When
    ReconciliationExceptionResponse response = exceptionService.resolveException(exceptionId, request);
    
    // Then
    assertNotNull(response);
    assertEquals(exceptionId, response.getExceptionId());
    assertEquals(runId, response.getRunId());
    assertEquals("AMOUNT_MISMATCH", response.getExceptionType());
    assertEquals(ReconciliationException.ExceptionStatus.RESOLVED, response.getStatus());
    assertEquals("Manual adjustment applied", response.getResolution());
    assertNotNull(response.getResolvedAt());
    assertEquals(userId, response.getResolvedBy());
    
    verify(exceptionRepository).findByExceptionIdAndTenantId(exceptionId, tenantId);
    verify(exceptionRepository).save(any(ReconciliationException.class));
  }
  
  @Test
  void testResolveException_NotFound() {
    // Given
    String exceptionId = "exception-123";
    ReconciliationExceptionResolutionRequest request = ReconciliationExceptionResolutionRequest.builder()
        .resolution("Manual adjustment applied")
        .build();
    
    when(exceptionRepository.findByExceptionIdAndTenantId(exceptionId, tenantId))
        .thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(ReconciliationExceptionServiceException.class, () -> {
      exceptionService.resolveException(exceptionId, request);
    });
    
    verify(exceptionRepository).findByExceptionIdAndTenantId(exceptionId, tenantId);
    verify(exceptionRepository, never()).save(any(ReconciliationException.class));
  }
  
  @Test
  void testResolveException_Exception() {
    // Given
    String exceptionId = "exception-123";
    ReconciliationExceptionResolutionRequest request = ReconciliationExceptionResolutionRequest.builder()
        .resolution("Manual adjustment applied")
        .build();
    
    when(exceptionRepository.findByExceptionIdAndTenantId(exceptionId, tenantId))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(ReconciliationExceptionServiceException.class, () -> {
      exceptionService.resolveException(exceptionId, request);
    });
    
    verify(exceptionRepository).findByExceptionIdAndTenantId(exceptionId, tenantId);
  }
  
  @Test
  void testGetExceptionsByRunId_Success() {
    // Given
    ReconciliationException exception1 = ReconciliationException.builder()
        .id(1L)
        .exceptionId("exception-1")
        .runId(runId)
        .exceptionType("AMOUNT_MISMATCH")
        .status(ReconciliationException.ExceptionStatus.OPEN)
        .tenantId(tenantId)
        .build();
    
    ReconciliationException exception2 = ReconciliationException.builder()
        .id(2L)
        .exceptionId("exception-2")
        .runId(runId)
        .exceptionType("MISSING_TRANSACTION")
        .status(ReconciliationException.ExceptionStatus.RESOLVED)
        .tenantId(tenantId)
        .build();
    
    List<ReconciliationException> exceptions = List.of(exception1, exception2);
    
    when(exceptionRepository.findByRunIdAndTenantId(runId, tenantId)).thenReturn(exceptions);
    
    // When
    List<ReconciliationExceptionResponse> responses = exceptionService.getExceptionsByRunId(runId);
    
    // Then
    assertNotNull(responses);
    assertEquals(2, responses.size());
    
    ReconciliationExceptionResponse response1 = responses.get(0);
    assertEquals("exception-1", response1.getExceptionId());
    assertEquals(runId, response1.getRunId());
    assertEquals("AMOUNT_MISMATCH", response1.getExceptionType());
    assertEquals(ReconciliationException.ExceptionStatus.OPEN, response1.getStatus());
    
    ReconciliationExceptionResponse response2 = responses.get(1);
    assertEquals("exception-2", response2.getExceptionId());
    assertEquals(runId, response2.getRunId());
    assertEquals("MISSING_TRANSACTION", response2.getExceptionType());
    assertEquals(ReconciliationException.ExceptionStatus.RESOLVED, response2.getStatus());
    
    verify(exceptionRepository).findByRunIdAndTenantId(runId, tenantId);
  }
  
  @Test
  void testGetExceptionsByRunId_Exception() {
    // Given
    when(exceptionRepository.findByRunIdAndTenantId(runId, tenantId))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(ReconciliationExceptionServiceException.class, () -> {
      exceptionService.getExceptionsByRunId(runId);
    });
    
    verify(exceptionRepository).findByRunIdAndTenantId(runId, tenantId);
  }
  
  @Test
  void testGetAllExceptions_Success() {
    // Given
    ReconciliationException exception1 = ReconciliationException.builder()
        .id(1L)
        .exceptionId("exception-1")
        .runId(runId)
        .exceptionType("AMOUNT_MISMATCH")
        .status(ReconciliationException.ExceptionStatus.OPEN)
        .tenantId(tenantId)
        .build();
    
    ReconciliationException exception2 = ReconciliationException.builder()
        .id(2L)
        .exceptionId("exception-2")
        .runId(runId + 1)
        .exceptionType("MISSING_TRANSACTION")
        .status(ReconciliationException.ExceptionStatus.RESOLVED)
        .tenantId(tenantId)
        .build();
    
    List<ReconciliationException> exceptions = List.of(exception1, exception2);
    
    when(exceptionRepository.findByTenantId(tenantId)).thenReturn(exceptions);
    
    // When
    List<ReconciliationExceptionResponse> responses = exceptionService.getAllExceptions();
    
    // Then
    assertNotNull(responses);
    assertEquals(2, responses.size());
    
    ReconciliationExceptionResponse response1 = responses.get(0);
    assertEquals("exception-1", response1.getExceptionId());
    assertEquals("AMOUNT_MISMATCH", response1.getExceptionType());
    assertEquals(ReconciliationException.ExceptionStatus.OPEN, response1.getStatus());
    
    ReconciliationExceptionResponse response2 = responses.get(1);
    assertEquals("exception-2", response2.getExceptionId());
    assertEquals("MISSING_TRANSACTION", response2.getExceptionType());
    assertEquals(ReconciliationException.ExceptionStatus.RESOLVED, response2.getStatus());
    
    verify(exceptionRepository).findByTenantId(tenantId);
  }
  
  @Test
  void testGetAllExceptions_Exception() {
    // Given
    when(exceptionRepository.findByTenantId(tenantId))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(ReconciliationExceptionServiceException.class, () -> {
      exceptionService.getAllExceptions();
    });
    
    verify(exceptionRepository).findByTenantId(tenantId);
  }
  
  @Test
  void testGetExceptionsByStatus_Success() {
    // Given
    ReconciliationException.ExceptionStatus status = ReconciliationException.ExceptionStatus.OPEN;
    
    ReconciliationException exception1 = ReconciliationException.builder()
        .id(1L)
        .exceptionId("exception-1")
        .runId(runId)
        .exceptionType("AMOUNT_MISMATCH")
        .status(status)
        .tenantId(tenantId)
        .build();
    
    ReconciliationException exception2 = ReconciliationException.builder()
        .id(2L)
        .exceptionId("exception-2")
        .runId(runId + 1)
        .exceptionType("MISSING_TRANSACTION")
        .status(status)
        .tenantId(tenantId)
        .build();
    
    List<ReconciliationException> exceptions = List.of(exception1, exception2);
    
    when(exceptionRepository.findByStatusAndTenantId(status, tenantId)).thenReturn(exceptions);
    
    // When
    List<ReconciliationExceptionResponse> responses = exceptionService.getExceptionsByStatus(status);
    
    // Then
    assertNotNull(responses);
    assertEquals(2, responses.size());
    
    ReconciliationExceptionResponse response1 = responses.get(0);
    assertEquals("exception-1", response1.getExceptionId());
    assertEquals("AMOUNT_MISMATCH", response1.getExceptionType());
    assertEquals(status, response1.getStatus());
    
    ReconciliationExceptionResponse response2 = responses.get(1);
    assertEquals("exception-2", response2.getExceptionId());
    assertEquals("MISSING_TRANSACTION", response2.getExceptionType());
    assertEquals(status, response2.getStatus());
    
    verify(exceptionRepository).findByStatusAndTenantId(status, tenantId);
  }
  
  @Test
  void testGetExceptionsByStatus_Exception() {
    // Given
    ReconciliationException.ExceptionStatus status = ReconciliationException.ExceptionStatus.OPEN;
    
    when(exceptionRepository.findByStatusAndTenantId(status, tenantId))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(ReconciliationExceptionServiceException.class, () -> {
      exceptionService.getExceptionsByStatus(status);
    });
    
    verify(exceptionRepository).findByStatusAndTenantId(status, tenantId);
  }
  
  @Test
  void testGetExceptionsByAssignedTo_Success() {
    // Given
    String assignedTo = "operator1";
    
    ReconciliationException exception1 = ReconciliationException.builder()
        .id(1L)
        .exceptionId("exception-1")
        .runId(runId)
        .exceptionType("AMOUNT_MISMATCH")
        .status(ReconciliationException.ExceptionStatus.OPEN)
        .assignedTo(assignedTo)
        .tenantId(tenantId)
        .build();
    
    ReconciliationException exception2 = ReconciliationException.builder()
        .id(2L)
        .exceptionId("exception-2")
        .runId(runId + 1)
        .exceptionType("MISSING_TRANSACTION")
        .status(ReconciliationException.ExceptionStatus.OPEN)
        .assignedTo(assignedTo)
        .tenantId(tenantId)
        .build();
    
    List<ReconciliationException> exceptions = List.of(exception1, exception2);
    
    when(exceptionRepository.findByAssignedToAndTenantId(assignedTo, tenantId)).thenReturn(exceptions);
    
    // When
    List<ReconciliationExceptionResponse> responses = exceptionService.getExceptionsByAssignedTo(assignedTo);
    
    // Then
    assertNotNull(responses);
    assertEquals(2, responses.size());
    
    ReconciliationExceptionResponse response1 = responses.get(0);
    assertEquals("exception-1", response1.getExceptionId());
    assertEquals("AMOUNT_MISMATCH", response1.getExceptionType());
    assertEquals(assignedTo, response1.getAssignedTo());
    
    ReconciliationExceptionResponse response2 = responses.get(1);
    assertEquals("exception-2", response2.getExceptionId());
    assertEquals("MISSING_TRANSACTION", response2.getExceptionType());
    assertEquals(assignedTo, response2.getAssignedTo());
    
    verify(exceptionRepository).findByAssignedToAndTenantId(assignedTo, tenantId);
  }
  
  @Test
  void testGetExceptionsByAssignedTo_Exception() {
    // Given
    String assignedTo = "operator1";
    
    when(exceptionRepository.findByAssignedToAndTenantId(assignedTo, tenantId))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(ReconciliationExceptionServiceException.class, () -> {
      exceptionService.getExceptionsByAssignedTo(assignedTo);
    });
    
    verify(exceptionRepository).findByAssignedToAndTenantId(assignedTo, tenantId);
  }
  
  @Test
  void testGetExceptionStatistics_Success() {
    // Given
    when(exceptionRepository.countByRunIdAndTenantId(runId, tenantId)).thenReturn(10L);
    when(exceptionRepository.countByRunIdAndStatusAndTenantId(runId, ReconciliationException.ExceptionStatus.OPEN, tenantId)).thenReturn(3L);
    when(exceptionRepository.countByRunIdAndStatusAndTenantId(runId, ReconciliationException.ExceptionStatus.RESOLVED, tenantId)).thenReturn(5L);
    when(exceptionRepository.countByRunIdAndStatusAndTenantId(runId, ReconciliationException.ExceptionStatus.CLOSED, tenantId)).thenReturn(2L);
    
    // When
    ReconciliationExceptionService.ReconciliationExceptionStatistics statistics = exceptionService.getExceptionStatistics(runId);
    
    // Then
    assertNotNull(statistics);
    assertEquals(runId, statistics.getRunId());
    assertEquals(10L, statistics.getTotalExceptions());
    assertEquals(3L, statistics.getOpenExceptions());
    assertEquals(5L, statistics.getResolvedExceptions());
    assertEquals(2L, statistics.getClosedExceptions());
    assertEquals(0.7, statistics.getResolutionRate(), 0.01); // (5 + 2) / 10 = 0.7
    
    verify(exceptionRepository).countByRunIdAndTenantId(runId, tenantId);
    verify(exceptionRepository).countByRunIdAndStatusAndTenantId(runId, ReconciliationException.ExceptionStatus.OPEN, tenantId);
    verify(exceptionRepository).countByRunIdAndStatusAndTenantId(runId, ReconciliationException.ExceptionStatus.RESOLVED, tenantId);
    verify(exceptionRepository).countByRunIdAndStatusAndTenantId(runId, ReconciliationException.ExceptionStatus.CLOSED, tenantId);
  }
  
  @Test
  void testGetExceptionStatistics_Exception() {
    // Given
    when(exceptionRepository.countByRunIdAndTenantId(runId, tenantId))
        .thenThrow(new RuntimeException("Database error"));
    
    // When & Then
    assertThrows(ReconciliationExceptionServiceException.class, () -> {
      exceptionService.getExceptionStatistics(runId);
    });
    
    verify(exceptionRepository).countByRunIdAndTenantId(runId, tenantId);
  }
}
