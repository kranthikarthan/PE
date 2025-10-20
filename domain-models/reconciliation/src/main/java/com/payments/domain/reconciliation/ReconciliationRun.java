package com.payments.domain.reconciliation;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reconciliation Run Domain Model
 *
 * <p>Represents a reconciliation run in the domain layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationRun {

  private String runId;
  private String status;
  private String clearingSystem;
  private String description;
  private String tenantId;
  private String businessUnitId;
  private String correlationId;
  private String startedBy;
  private String stoppedBy;
  private String stopReason;
  private Integer totalRecords;
  private Integer matchedRecords;
  private Integer unmatchedRecords;
  private Long processingTimeSeconds;
  private String errorMessage;
  private String metadata;
  private LocalDateTime startedAt;
  private LocalDateTime updatedAt;
  private LocalDateTime completedAt;
  private Long version;
}
