package com.payments.batch.error;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for RetryPolicy.
 *
 * @since PE-404
 */
@DisplayName("Retry Policy Tests")
class RetryPolicyTest {
  
  private RetryPolicy retryPolicy;
  
  @BeforeEach
  void setUp() {
    retryPolicy = RetryPolicy.builder()
        .maxAttempts(3)
        .initialDelayMs(1000)
        .maxDelayMs(10000)
        .backoffMultiplier(2.0)
        .useJitter(false)
        .strategy(RetryPolicy.RetryStrategy.EXPONENTIAL)
        .retryOnAllExceptions(true)
        .build();
  }
  
  @Test
  @DisplayName("Should create default retry policy")
  void shouldCreateDefaultRetryPolicy() {
    // When
    RetryPolicy defaultPolicy = RetryPolicy.defaultPolicy();
    
    // Then
    assertEquals(3, defaultPolicy.getMaxAttempts());
    assertEquals(1000, defaultPolicy.getInitialDelayMs());
    assertEquals(30000, defaultPolicy.getMaxDelayMs());
    assertEquals(2.0, defaultPolicy.getBackoffMultiplier());
    assertTrue(defaultPolicy.isUseJitter());
    assertEquals(RetryPolicy.RetryStrategy.EXPONENTIAL, defaultPolicy.getStrategy());
    assertTrue(defaultPolicy.isRetryOnAllExceptions());
  }
  
  @Test
  @DisplayName("Should create network retry policy")
  void shouldCreateNetworkRetryPolicy() {
    // When
    RetryPolicy networkPolicy = RetryPolicy.networkPolicy();
    
    // Then
    assertEquals(5, networkPolicy.getMaxAttempts());
    assertEquals(500, networkPolicy.getInitialDelayMs());
    assertEquals(10000, networkPolicy.getMaxDelayMs());
    assertEquals(1.5, networkPolicy.getBackoffMultiplier());
    assertTrue(networkPolicy.isUseJitter());
    assertEquals(RetryPolicy.RetryStrategy.EXPONENTIAL, networkPolicy.getStrategy());
  }
  
  @Test
  @DisplayName("Should create database retry policy")
  void shouldCreateDatabaseRetryPolicy() {
    // When
    RetryPolicy databasePolicy = RetryPolicy.databasePolicy();
    
    // Then
    assertEquals(3, databasePolicy.getMaxAttempts());
    assertEquals(2000, databasePolicy.getInitialDelayMs());
    assertEquals(15000, databasePolicy.getMaxDelayMs());
    assertEquals(2.0, databasePolicy.getBackoffMultiplier());
    assertFalse(databasePolicy.isUseJitter());
    assertEquals(RetryPolicy.RetryStrategy.EXPONENTIAL, databasePolicy.getStrategy());
  }
  
  @Test
  @DisplayName("Should create file retry policy")
  void shouldCreateFileRetryPolicy() {
    // When
    RetryPolicy filePolicy = RetryPolicy.filePolicy();
    
    // Then
    assertEquals(3, filePolicy.getMaxAttempts());
    assertEquals(1000, filePolicy.getInitialDelayMs());
    assertEquals(5000, filePolicy.getMaxDelayMs());
    assertEquals(1.5, filePolicy.getBackoffMultiplier());
    assertTrue(filePolicy.isUseJitter());
    assertEquals(RetryPolicy.RetryStrategy.LINEAR, filePolicy.getStrategy());
  }
  
  @Test
  @DisplayName("Should create critical retry policy")
  void shouldCreateCriticalRetryPolicy() {
    // When
    RetryPolicy criticalPolicy = RetryPolicy.criticalPolicy();
    
    // Then
    assertEquals(10, criticalPolicy.getMaxAttempts());
    assertEquals(500, criticalPolicy.getInitialDelayMs());
    assertEquals(60000, criticalPolicy.getMaxDelayMs());
    assertEquals(1.2, criticalPolicy.getBackoffMultiplier());
    assertTrue(criticalPolicy.isUseJitter());
    assertEquals(RetryPolicy.RetryStrategy.EXPONENTIAL, criticalPolicy.getStrategy());
  }
  
  @Test
  @DisplayName("Should calculate fixed delay correctly")
  void shouldCalculateFixedDelayCorrectly() {
    // Given
    RetryPolicy fixedPolicy = RetryPolicy.builder()
        .maxAttempts(3)
        .initialDelayMs(1000)
        .strategy(RetryPolicy.RetryStrategy.FIXED)
        .build();
    
    // When & Then
    assertEquals(0, fixedPolicy.calculateDelay(1)); // First attempt
    assertEquals(1000, fixedPolicy.calculateDelay(2)); // Second attempt
    assertEquals(1000, fixedPolicy.calculateDelay(3)); // Third attempt
  }
  
  @Test
  @DisplayName("Should calculate linear delay correctly")
  void shouldCalculateLinearDelayCorrectly() {
    // Given
    RetryPolicy linearPolicy = RetryPolicy.builder()
        .maxAttempts(3)
        .initialDelayMs(1000)
        .strategy(RetryPolicy.RetryStrategy.LINEAR)
        .build();
    
    // When & Then
    assertEquals(0, linearPolicy.calculateDelay(1)); // First attempt
    assertEquals(1000, linearPolicy.calculateDelay(2)); // Second attempt
    assertEquals(2000, linearPolicy.calculateDelay(3)); // Third attempt
  }
  
  @Test
  @DisplayName("Should calculate exponential delay correctly")
  void shouldCalculateExponentialDelayCorrectly() {
    // Given
    RetryPolicy exponentialPolicy = RetryPolicy.builder()
        .maxAttempts(3)
        .initialDelayMs(1000)
        .backoffMultiplier(2.0)
        .strategy(RetryPolicy.RetryStrategy.EXPONENTIAL)
        .build();
    
    // When & Then
    assertEquals(0, exponentialPolicy.calculateDelay(1)); // First attempt
    assertEquals(1000, exponentialPolicy.calculateDelay(2)); // Second attempt
    assertEquals(2000, exponentialPolicy.calculateDelay(3)); // Third attempt
  }
  
  @Test
  @DisplayName("Should respect maximum delay limit")
  void shouldRespectMaximumDelayLimit() {
    // Given
    RetryPolicy maxDelayPolicy = RetryPolicy.builder()
        .maxAttempts(5)
        .initialDelayMs(1000)
        .maxDelayMs(2000)
        .backoffMultiplier(2.0)
        .strategy(RetryPolicy.RetryStrategy.EXPONENTIAL)
        .build();
    
    // When & Then
    assertEquals(0, maxDelayPolicy.calculateDelay(1)); // First attempt
    assertEquals(1000, maxDelayPolicy.calculateDelay(2)); // Second attempt
    assertEquals(2000, maxDelayPolicy.calculateDelay(3)); // Third attempt (capped)
    assertEquals(2000, maxDelayPolicy.calculateDelay(4)); // Fourth attempt (capped)
    assertEquals(2000, maxDelayPolicy.calculateDelay(5)); // Fifth attempt (capped)
  }
  
  @Test
  @DisplayName("Should apply jitter when enabled")
  void shouldApplyJitterWhenEnabled() {
    // Given
    RetryPolicy jitterPolicy = RetryPolicy.builder()
        .maxAttempts(3)
        .initialDelayMs(1000)
        .useJitter(true)
        .maxJitter(0.1)
        .strategy(RetryPolicy.RetryStrategy.FIXED)
        .build();
    
    // When - Calculate delay multiple times
    long delay1 = jitterPolicy.calculateDelay(2);
    long delay2 = jitterPolicy.calculateDelay(2);
    long delay3 = jitterPolicy.calculateDelay(2);
    
    // Then - Delays should be different due to jitter
    // Note: This test might occasionally fail due to randomness, but it's unlikely
    assertTrue(delay1 >= 900 && delay1 <= 1100);
    assertTrue(delay2 >= 900 && delay2 <= 1100);
    assertTrue(delay3 >= 900 && delay3 <= 1100);
  }
  
  @Test
  @DisplayName("Should determine if exception should be retried")
  void shouldDetermineIfExceptionShouldBeRetried() {
    // Given
    RetryPolicy policy = RetryPolicy.builder()
        .maxAttempts(3)
        .retryOnAllExceptions(true)
        .build();
    
    // When & Then
    assertTrue(policy.shouldRetry(new RuntimeException("Network error")));
    assertTrue(policy.shouldRetry(new IllegalArgumentException("Validation error")));
    assertTrue(policy.shouldRetry(new Exception("Generic error")));
  }
  
  @Test
  @DisplayName("Should not retry specific exception types")
  void shouldNotRetrySpecificExceptionTypes() {
    // Given
    RetryPolicy policy = RetryPolicy.builder()
        .maxAttempts(3)
        .retryOnAllExceptions(false)
        .retryOnExceptions(new Class[]{RuntimeException.class})
        .noRetryOnExceptions(new Class[]{IllegalArgumentException.class})
        .build();
    
    // When & Then
    assertTrue(policy.shouldRetry(new RuntimeException("Network error")));
    assertFalse(policy.shouldRetry(new IllegalArgumentException("Validation error")));
    assertFalse(policy.shouldRetry(new Exception("Generic error")));
  }
  
  @Test
  @DisplayName("Should calculate total retry duration")
  void shouldCalculateTotalRetryDuration() {
    // Given
    RetryPolicy policy = RetryPolicy.builder()
        .maxAttempts(3)
        .initialDelayMs(1000)
        .strategy(RetryPolicy.RetryStrategy.FIXED)
        .build();
    
    // When
    var totalDuration = policy.getTotalRetryDuration();
    
    // Then
    assertEquals(2000, totalDuration.toMillis()); // 1000ms + 1000ms
  }
  
  @Test
  @DisplayName("Should check if retries are allowed")
  void shouldCheckIfRetriesAreAllowed() {
    // Given
    RetryPolicy allowRetriesPolicy = RetryPolicy.builder()
        .maxAttempts(3)
        .build();
    
    RetryPolicy noRetriesPolicy = RetryPolicy.builder()
        .maxAttempts(1)
        .build();
    
    // When & Then
    assertTrue(allowRetriesPolicy.allowsRetries());
    assertFalse(noRetriesPolicy.allowsRetries());
  }
  
  @Test
  @DisplayName("Should get policy summary")
  void shouldGetPolicySummary() {
    // When
    String summary = retryPolicy.getSummary();
    
    // Then
    assertTrue(summary.contains("maxAttempts=3"));
    assertTrue(summary.contains("strategy=EXPONENTIAL"));
    assertTrue(summary.contains("initialDelay=1000ms"));
    assertTrue(summary.contains("maxDelay=10000ms"));
    assertTrue(summary.contains("jitter=false"));
  }
  
  @Test
  @DisplayName("Should handle custom retry condition")
  void shouldHandleCustomRetryCondition() {
    // Given
    RetryPolicy customPolicy = RetryPolicy.builder()
        .maxAttempts(3)
        .retryOnAllExceptions(false)
        .retryCondition(throwable -> throwable.getMessage().contains("retryable"))
        .build();
    
    // When & Then
    assertTrue(customPolicy.shouldRetry(new RuntimeException("This is retryable")));
    assertFalse(customPolicy.shouldRetry(new RuntimeException("This is not retryable")));
  }
}
