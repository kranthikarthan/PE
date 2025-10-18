package com.payments.audit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Audit Batch Scheduler - Periodic flush of event batches.
 *
 * <p>Ensures that audit events don't remain in memory for too long.
 * Flushes batch buffer periodically (every 60 seconds by default).
 *
 * <p>Pattern: Durable Subscriber with periodic flush
 * - Kafka listener collects events in batch
 * - Scheduler triggers flush at intervals
 * - Prevents stale data in memory
 * - Maintains low-latency audit trail
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "app.audit.batch-flush-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class AuditBatchScheduler {

  private final AuditEventConsumer auditEventConsumer;

  /**
   * Periodic flush of audit event batch.
   *
   * <p>Triggered every 60 seconds (configurable).
   * Ensures events don't stay in memory indefinitely.
   *
   * <p>Safe to call multiple times (idempotent):
   * - Empty batch: no-op
   * - Filled batch: flushes to database
   */
  @Scheduled(fixedRateString = "${app.audit.batch-flush-interval-ms:60000}")
  public void flushBatchPeriodically() {
    try {
      log.debug("Scheduled batch flush started");
      auditEventConsumer.flushBatchIfNeeded();
      log.debug("Scheduled batch flush completed successfully");
    } catch (Exception e) {
      log.error("Error during scheduled batch flush", e);
      // Don't throw - let scheduler retry at next interval
    }
  }
}
