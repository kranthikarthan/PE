package com.payments.settlement.event;

import java.math.BigDecimal;
import java.time.Instant;

public final class SettlementEvents {

  private SettlementEvents() {}

  public record SettlementBatchCreatedEvent(
      String batchId,
      String clearingSystem,
      Instant createdAt,
      Integer transactionCount,
      BigDecimal totalAmount) {}

  public record SettlementCompletedEvent(
      String batchId,
      String settlementStatus,
      Integer settledCount,
      Integer failedCount,
      BigDecimal totalSettledAmount,
      Instant completedAt) {}
}
