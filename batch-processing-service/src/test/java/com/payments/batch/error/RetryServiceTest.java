package com.payments.batch.error;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for RetryService.
 *
 * @since PE-404
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Retry Service Tests")
class RetryServiceTest {
  
  @Mock
  private RetryService.RetryableOperation<String> mockOperation;
  
  private RetryService retryService;
  
  @BeforeEach
  void setUp() {
    retryService = new RetryService();
  }
  
  @Test
  @DisplayName("Should execute operation successfully on first attempt")
  void shouldExecuteOperationSuccessfullyOnFirstAttempt() throws Exception {
    // Given
    String operationName = "test-operation";
    String expectedResult = "success";
    when(mockOperation.execute()).thenReturn(expectedResult);
    
    // When
    String result = retryService.executeWithRetry(operationName, mockOperation);
    
    // Then
    assertEquals(expectedResult, result);
    verify(mockOperation, times(1)).execute();
  }
  
  @Test
  @DisplayName("Should retry operation on failure and succeed")
  void shouldRetryOperationOnFailureAndSucceed() throws Exception {
    // Given
    String operationName = "test-operation";
    String expectedResult = "success";
    when(mockOperation.execute())
        .thenThrow(new RuntimeException("First attempt failed"))
        .thenReturn(expectedResult);
    
    // When
    String result = retryService.executeWithRetry(operationName, mockOperation);
    
    // Then
    assertEquals(expectedResult, result);
    verify(mockOperation, times(2)).execute();
  }
  
  @Test
  @DisplayName("Should fail after maximum retry attempts")
  void shouldFailAfterMaximumRetryAttempts() throws Exception {
    // Given
    String operationName = "test-operation";
    RuntimeException exception = new RuntimeException("Operation failed");
    when(mockOperation.execute()).thenThrow(exception);
    
    // When & Then
    BatchProcessingException result = assertThrows(BatchProcessingException.class, () -> {
      retryService.executeWithRetry(operationName, mockOperation);
    });
    
    assertEquals("OPERATION_FAILED", result.getErrorCode());
    assertTrue(result.getMessage().contains(operationName));
    verify(mockOperation, times(3)).execute(); // Default max attempts is 3
  }
  
  @Test
  @DisplayName("Should use custom retry policy")
  void shouldUseCustomRetryPolicy() throws Exception {
    // Given
    String operationName = "test-operation";
    RetryPolicy customPolicy = RetryPolicy.builder()
        .maxAttempts(2)
        .initialDelayMs(100)
        .strategy(RetryPolicy.RetryStrategy.FIXED)
        .retryOnAllExceptions(true)
        .build();
    
    when(mockOperation.execute()).thenThrow(new RuntimeException("Operation failed"));
    
    // When & Then
    assertThrows(BatchProcessingException.class, () -> {
      retryService.executeWithRetry(operationName, mockOperation, customPolicy);
    });
    
    verify(mockOperation, times(2)).execute(); // Custom max attempts is 2
  }
  
  @Test
  @DisplayName("Should handle non-retryable exceptions")
  void shouldHandleNonRetryableExceptions() throws Exception {
    // Given
    String operationName = "test-operation";
    IllegalArgumentException exception = new IllegalArgumentException("Validation failed");
    when(mockOperation.execute()).thenThrow(exception);
    
    // When & Then
    BatchProcessingException result = assertThrows(BatchProcessingException.class, () -> {
      retryService.executeWithRetry(operationName, mockOperation);
    });
    
    assertEquals("OPERATION_FAILED", result.getErrorCode());
    assertFalse(result.isRetryable());
    verify(mockOperation, times(1)).execute(); // Should not retry validation errors
  }
  
  @Test
  @DisplayName("Should execute network operation with network retry policy")
  void shouldExecuteNetworkOperationWithNetworkRetryPolicy() throws Exception {
    // Given
    String operationName = "network-operation";
    String expectedResult = "success";
    when(mockOperation.execute()).thenReturn(expectedResult);
    
    // When
    String result = retryService.executeNetworkOperation(operationName, mockOperation);
    
    // Then
    assertEquals(expectedResult, result);
    verify(mockOperation, times(1)).execute();
  }
  
  @Test
  @DisplayName("Should execute database operation with database retry policy")
  void shouldExecuteDatabaseOperationWithDatabaseRetryPolicy() throws Exception {
    // Given
    String operationName = "database-operation";
    String expectedResult = "success";
    when(mockOperation.execute()).thenReturn(expectedResult);
    
    // When
    String result = retryService.executeDatabaseOperation(operationName, mockOperation);
    
    // Then
    assertEquals(expectedResult, result);
    verify(mockOperation, times(1)).execute();
  }
  
  @Test
  @DisplayName("Should execute file operation with file retry policy")
  void shouldExecuteFileOperationWithFileRetryPolicy() throws Exception {
    // Given
    String operationName = "file-operation";
    String expectedResult = "success";
    when(mockOperation.execute()).thenReturn(expectedResult);
    
    // When
    String result = retryService.executeFileOperation(operationName, mockOperation);
    
    // Then
    assertEquals(expectedResult, result);
    verify(mockOperation, times(1)).execute();
  }
  
  @Test
  @DisplayName("Should execute critical operation with critical retry policy")
  void shouldExecuteCriticalOperationWithCriticalRetryPolicy() throws Exception {
    // Given
    String operationName = "critical-operation";
    String expectedResult = "success";
    when(mockOperation.execute()).thenReturn(expectedResult);
    
    // When
    String result = retryService.executeCriticalOperation(operationName, mockOperation);
    
    // Then
    assertEquals(expectedResult, result);
    verify(mockOperation, times(1)).execute();
  }
  
  @Test
  @DisplayName("Should handle custom error handler")
  void shouldHandleCustomErrorHandler() throws Exception {
    // Given
    String operationName = "test-operation";
    RuntimeException exception = new RuntimeException("Custom error");
    when(mockOperation.execute()).thenThrow(exception);
    
    RetryService.RetryableOperation<String> operation = () -> {
      throw exception;
    };
    
    RetryService.RetryableOperation<String> customErrorHandler = (ex) -> {
      return new BatchProcessingException("CUSTOM_ERROR", "Custom error handling", ex);
    };
    
    // When & Then
    BatchProcessingException result = assertThrows(BatchProcessingException.class, () -> {
      retryService.executeWithRetry(operationName, operation, RetryPolicy.defaultPolicy(), customErrorHandler);
    });
    
    assertEquals("CUSTOM_ERROR", result.getErrorCode());
  }
  
  @Test
  @DisplayName("Should track circuit breaker statistics")
  void shouldTrackCircuitBreakerStatistics() throws Exception {
    // Given
    String operationName = "test-operation";
    when(mockOperation.execute()).thenReturn("success");
    
    // When
    retryService.executeWithRetry(operationName, mockOperation);
    
    // Then
    var stats = retryService.getAllCircuitBreakerStats();
    assertTrue(stats.containsKey(operationName));
    
    var operationStats = stats.get(operationName);
    assertEquals(operationName, operationStats.getName());
    assertEquals(1, operationStats.getTotalCalls());
    assertEquals(1, operationStats.getSuccessfulCalls());
    assertEquals(0, operationStats.getFailedCalls());
  }
  
  @Test
  @DisplayName("Should reset circuit breaker")
  void shouldResetCircuitBreaker() throws Exception {
    // Given
    String operationName = "test-operation";
    when(mockOperation.execute()).thenReturn("success");
    
    // When
    retryService.executeWithRetry(operationName, mockOperation);
    retryService.resetCircuitBreaker(operationName);
    
    // Then
    var stats = retryService.getAllCircuitBreakerStats();
    var operationStats = stats.get(operationName);
    assertEquals(0, operationStats.getTotalCalls());
  }
  
  @Test
  @DisplayName("Should handle interrupted retry")
  void shouldHandleInterruptedRetry() throws Exception {
    // Given
    String operationName = "test-operation";
    AtomicInteger attemptCount = new AtomicInteger(0);
    
    when(mockOperation.execute()).thenAnswer(invocation -> {
      int attempt = attemptCount.incrementAndGet();
      if (attempt == 1) {
        throw new RuntimeException("First attempt failed");
      } else {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Second attempt failed");
      }
    });
    
    // When & Then
    BatchProcessingException result = assertThrows(BatchProcessingException.class, () -> {
      retryService.executeWithRetry(operationName, mockOperation);
    });
    
    assertEquals("RETRY_INTERRUPTED", result.getErrorCode());
    assertTrue(Thread.currentThread().isInterrupted());
  }
  
  @Test
  @DisplayName("Should classify exceptions correctly")
  void shouldClassifyExceptionsCorrectly() throws Exception {
    // Given
    String operationName = "test-operation";
    
    // Test network exception classification
    when(mockOperation.execute()).thenThrow(new RuntimeException("Network timeout"));
    BatchProcessingException result = assertThrows(BatchProcessingException.class, () -> {
      retryService.executeWithRetry(operationName, mockOperation);
    });
    assertEquals(ErrorCategory.INTEGRATION, result.getCategory());
    
    // Test validation exception classification
    when(mockOperation.execute()).thenThrow(new IllegalArgumentException("Validation failed"));
    result = assertThrows(BatchProcessingException.class, () -> {
      retryService.executeWithRetry(operationName, mockOperation);
    });
    assertEquals(ErrorCategory.VALIDATION, result.getCategory());
  }
}
