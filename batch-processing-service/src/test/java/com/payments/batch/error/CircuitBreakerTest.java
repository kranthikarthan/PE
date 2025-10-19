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
 * Unit tests for CircuitBreaker.
 *
 * @since PE-404
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Circuit Breaker Tests")
class CircuitBreakerTest {
  
  @Mock
  private CircuitBreaker.CircuitBreakerOperation<String> mockOperation;
  
  private CircuitBreaker circuitBreaker;
  private CircuitBreakerConfig config;
  
  @BeforeEach
  void setUp() {
    config = CircuitBreakerConfig.builder()
        .failureThreshold(0.5) // 50% failure rate
        .successThreshold(0.8) // 80% success rate
        .minimumNumberOfCalls(5)
        .waitDurationInOpenState(Duration.ofSeconds(1))
        .halfOpenMaxCalls(2)
        .recordCallsInHalfOpenState(true)
        .build();
    
    circuitBreaker = new CircuitBreaker("test-circuit", config);
  }
  
  @Test
  @DisplayName("Should execute operation successfully when circuit is closed")
  void shouldExecuteOperationSuccessfullyWhenCircuitIsClosed() throws Exception {
    // Given
    String expectedResult = "success";
    when(mockOperation.execute()).thenReturn(expectedResult);
    
    // When
    String result = circuitBreaker.execute(mockOperation);
    
    // Then
    assertEquals(expectedResult, result);
    verify(mockOperation, times(1)).execute();
    assertEquals(CircuitBreaker.CircuitState.CLOSED, circuitBreaker.getState());
  }
  
  @Test
  @DisplayName("Should open circuit when failure threshold is exceeded")
  void shouldOpenCircuitWhenFailureThresholdIsExceeded() throws Exception {
    // Given
    when(mockOperation.execute()).thenThrow(new RuntimeException("Operation failed"));
    
    // When - Execute enough operations to exceed failure threshold
    for (int i = 0; i < 6; i++) {
      try {
        circuitBreaker.execute(mockOperation);
      } catch (Exception e) {
        // Expected to fail
      }
    }
    
    // Then
    assertEquals(CircuitBreaker.CircuitState.OPEN, circuitBreaker.getState());
    assertTrue(circuitBreaker.isOpen());
    assertFalse(circuitBreaker.isClosed());
  }
  
  @Test
  @DisplayName("Should reject requests when circuit is open")
  void shouldRejectRequestsWhenCircuitIsOpen() throws Exception {
    // Given
    when(mockOperation.execute()).thenThrow(new RuntimeException("Operation failed"));
    
    // Open the circuit
    for (int i = 0; i < 6; i++) {
      try {
        circuitBreaker.execute(mockOperation);
      } catch (Exception e) {
        // Expected to fail
      }
    }
    
    // When & Then
    assertThrows(CircuitBreakerOpenException.class, () -> {
      circuitBreaker.execute(mockOperation);
    });
    
    verify(mockOperation, times(6)).execute(); // Only the initial attempts
  }
  
  @Test
  @DisplayName("Should transition to half-open state after wait duration")
  void shouldTransitionToHalfOpenStateAfterWaitDuration() throws Exception {
    // Given
    when(mockOperation.execute()).thenThrow(new RuntimeException("Operation failed"));
    
    // Open the circuit
    for (int i = 0; i < 6; i++) {
      try {
        circuitBreaker.execute(mockOperation);
      } catch (Exception e) {
        // Expected to fail
      }
    }
    
    // Wait for the circuit to attempt reset
    Thread.sleep(1100); // Wait longer than waitDurationInOpenState
    
    // When
    when(mockOperation.execute()).thenReturn("success");
    String result = circuitBreaker.execute(mockOperation);
    
    // Then
    assertEquals("success", result);
    assertEquals(CircuitBreaker.CircuitState.HALF_OPEN, circuitBreaker.getState());
  }
  
  @Test
  @DisplayName("Should close circuit when success threshold is met in half-open state")
  void shouldCloseCircuitWhenSuccessThresholdIsMetInHalfOpenState() throws Exception {
    // Given
    when(mockOperation.execute()).thenThrow(new RuntimeException("Operation failed"));
    
    // Open the circuit
    for (int i = 0; i < 6; i++) {
      try {
        circuitBreaker.execute(mockOperation);
      } catch (Exception e) {
        // Expected to fail
      }
    }
    
    // Wait for the circuit to attempt reset
    Thread.sleep(1100);
    
    // When - Execute successful operations in half-open state
    when(mockOperation.execute()).thenReturn("success");
    for (int i = 0; i < 2; i++) {
      circuitBreaker.execute(mockOperation);
    }
    
    // Then
    assertEquals(CircuitBreaker.CircuitState.CLOSED, circuitBreaker.getState());
    assertTrue(circuitBreaker.isClosed());
  }
  
  @Test
  @DisplayName("Should open circuit again if failures occur in half-open state")
  void shouldOpenCircuitAgainIfFailuresOccurInHalfOpenState() throws Exception {
    // Given
    when(mockOperation.execute()).thenThrow(new RuntimeException("Operation failed"));
    
    // Open the circuit
    for (int i = 0; i < 6; i++) {
      try {
        circuitBreaker.execute(mockOperation);
      } catch (Exception e) {
        // Expected to fail
      }
    }
    
    // Wait for the circuit to attempt reset
    Thread.sleep(1100);
    
    // When - Execute failing operations in half-open state
    for (int i = 0; i < 2; i++) {
      try {
        circuitBreaker.execute(mockOperation);
      } catch (Exception e) {
        // Expected to fail
      }
    }
    
    // Then
    assertEquals(CircuitBreaker.CircuitState.OPEN, circuitBreaker.getState());
  }
  
  @Test
  @DisplayName("Should track circuit breaker statistics")
  void shouldTrackCircuitBreakerStatistics() throws Exception {
    // Given
    when(mockOperation.execute()).thenReturn("success");
    
    // When
    circuitBreaker.execute(mockOperation);
    
    // Then
    CircuitBreakerStats stats = circuitBreaker.getStats();
    assertEquals("test-circuit", stats.getName());
    assertEquals(CircuitBreaker.CircuitState.CLOSED, stats.getState());
    assertEquals(1, stats.getTotalCalls());
    assertEquals(1, stats.getSuccessfulCalls());
    assertEquals(0, stats.getFailedCalls());
    assertEquals(1.0, stats.getSuccessRate(), 0.01);
    assertEquals(0.0, stats.getFailureRate(), 0.01);
  }
  
  @Test
  @DisplayName("Should reset circuit breaker to initial state")
  void shouldResetCircuitBreakerToInitialState() throws Exception {
    // Given
    when(mockOperation.execute()).thenReturn("success");
    circuitBreaker.execute(mockOperation);
    
    // When
    circuitBreaker.reset();
    
    // Then
    CircuitBreakerStats stats = circuitBreaker.getStats();
    assertEquals(0, stats.getTotalCalls());
    assertEquals(0, stats.getSuccessfulCalls());
    assertEquals(0, stats.getFailedCalls());
    assertEquals(CircuitBreaker.CircuitState.CLOSED, stats.getState());
  }
  
  @Test
  @DisplayName("Should force circuit breaker to open state")
  void shouldForceCircuitBreakerToOpenState() {
    // When
    circuitBreaker.forceOpen();
    
    // Then
    assertEquals(CircuitBreaker.CircuitState.OPEN, circuitBreaker.getState());
    assertTrue(circuitBreaker.isOpen());
  }
  
  @Test
  @DisplayName("Should force circuit breaker to closed state")
  void shouldForceCircuitBreakerToClosedState() {
    // When
    circuitBreaker.forceClosed();
    
    // Then
    assertEquals(CircuitBreaker.CircuitState.CLOSED, circuitBreaker.getState());
    assertTrue(circuitBreaker.isClosed());
  }
  
  @Test
  @DisplayName("Should calculate failure rate correctly")
  void shouldCalculateFailureRateCorrectly() throws Exception {
    // Given
    when(mockOperation.execute())
        .thenReturn("success")
        .thenThrow(new RuntimeException("Failed"))
        .thenReturn("success")
        .thenThrow(new RuntimeException("Failed"));
    
    // When
    try {
      circuitBreaker.execute(mockOperation);
    } catch (Exception e) {
      // Expected to fail
    }
    
    try {
      circuitBreaker.execute(mockOperation);
    } catch (Exception e) {
      // Expected to fail
    }
    
    try {
      circuitBreaker.execute(mockOperation);
    } catch (Exception e) {
      // Expected to fail
    }
    
    try {
      circuitBreaker.execute(mockOperation);
    } catch (Exception e) {
      // Expected to fail
    }
    
    // Then
    assertEquals(0.5, circuitBreaker.calculateFailureRate(), 0.01);
    assertEquals(0.5, circuitBreaker.calculateSuccessRate(), 0.01);
  }
  
  @Test
  @DisplayName("Should handle concurrent operations")
  void shouldHandleConcurrentOperations() throws Exception {
    // Given
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);
    
    when(mockOperation.execute()).thenAnswer(invocation -> {
      if (Math.random() < 0.5) {
        successCount.incrementAndGet();
        return "success";
      } else {
        failureCount.incrementAndGet();
        throw new RuntimeException("Random failure");
      }
    });
    
    // When - Execute multiple operations concurrently
    Thread[] threads = new Thread[10];
    for (int i = 0; i < 10; i++) {
      threads[i] = new Thread(() -> {
        try {
          circuitBreaker.execute(mockOperation);
        } catch (Exception e) {
          // Expected to fail sometimes
        }
      });
      threads[i].start();
    }
    
    // Wait for all threads to complete
    for (Thread thread : threads) {
      thread.join();
    }
    
    // Then
    assertTrue(successCount.get() > 0);
    assertTrue(failureCount.get() > 0);
  }
  
  @Test
  @DisplayName("Should create circuit breaker with default config")
  void shouldCreateCircuitBreakerWithDefaultConfig() {
    // When
    CircuitBreaker defaultBreaker = CircuitBreaker.withDefaultConfig("default-test");
    
    // Then
    assertEquals("default-test", defaultBreaker.getName());
    assertEquals(CircuitBreaker.CircuitState.CLOSED, defaultBreaker.getState());
  }
  
  @Test
  @DisplayName("Should create circuit breaker for batch processing")
  void shouldCreateCircuitBreakerForBatchProcessing() {
    // When
    CircuitBreaker batchBreaker = CircuitBreaker.forBatchProcessing("batch-test");
    
    // Then
    assertEquals("batch-test", batchBreaker.getName());
    assertEquals(CircuitBreaker.CircuitState.CLOSED, batchBreaker.getState());
  }
  
  @Test
  @DisplayName("Should create circuit breaker for external service")
  void shouldCreateCircuitBreakerForExternalService() {
    // When
    CircuitBreaker externalBreaker = CircuitBreaker.forExternalService("external-test");
    
    // Then
    assertEquals("external-test", externalBreaker.getName());
    assertEquals(CircuitBreaker.CircuitState.CLOSED, externalBreaker.getState());
  }
}
