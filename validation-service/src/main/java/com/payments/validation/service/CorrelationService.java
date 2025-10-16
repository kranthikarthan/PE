package com.payments.validation.service;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Correlation Service
 *
 * <p>Manages correlation IDs for request tracing: - Thread-local correlation ID storage -
 * Correlation ID generation - Context propagation - Request tracing
 */
@Slf4j
@Service
public class CorrelationService {

  private static final ThreadLocal<String> correlationIdContext = new ThreadLocal<>();

  /**
   * Set correlation ID for current thread
   *
   * @param correlationId Correlation ID
   */
  public void setCorrelationId(String correlationId) {
    if (correlationId == null || correlationId.trim().isEmpty()) {
      correlationId = generateCorrelationId();
    }
    correlationIdContext.set(correlationId);
    log.debug("Set correlation ID: {}", correlationId);
  }

  /**
   * Get current correlation ID
   *
   * @return Current correlation ID or null if not set
   */
  public String getCurrentCorrelationId() {
    return correlationIdContext.get();
  }

  /**
   * Get current correlation ID or generate new one
   *
   * @return Current correlation ID or new one if not set
   */
  public String getCurrentCorrelationIdOrGenerate() {
    String correlationId = correlationIdContext.get();
    if (correlationId == null) {
      correlationId = generateCorrelationId();
      correlationIdContext.set(correlationId);
    }
    return correlationId;
  }

  /** Clear correlation ID for current thread */
  public void clearCorrelationId() {
    String correlationId = correlationIdContext.get();
    correlationIdContext.remove();
    log.debug("Cleared correlation ID: {}", correlationId);
  }

  /**
   * Generate new correlation ID
   *
   * @return New correlation ID
   */
  public String generateCorrelationId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Check if correlation ID is set
   *
   * @return true if correlation ID is set
   */
  public boolean hasCorrelationId() {
    return correlationIdContext.get() != null;
  }

  /**
   * Execute with correlation ID
   *
   * @param correlationId Correlation ID
   * @param runnable Runnable to execute
   */
  public void executeWithCorrelationId(String correlationId, Runnable runnable) {
    String previousCorrelationId = getCurrentCorrelationId();
    try {
      setCorrelationId(correlationId);
      runnable.run();
    } finally {
      if (previousCorrelationId != null) {
        setCorrelationId(previousCorrelationId);
      } else {
        clearCorrelationId();
      }
    }
  }
}
