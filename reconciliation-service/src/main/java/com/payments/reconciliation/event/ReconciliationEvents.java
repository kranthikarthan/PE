package com.payments.reconciliation.event;

import java.time.Instant;

public final class ReconciliationEvents {

  private ReconciliationEvents() {}

  public record ReconciliationRunStartedEvent(
      String reconciliationId, String clearingSystem, Instant startedAt) {}

  public record ReconciliationRunCompletedEvent(
      String reconciliationId,
      String status,
      Integer matchedCount,
      Integer unmatchedCount,
      Instant completedAt) {}
}
