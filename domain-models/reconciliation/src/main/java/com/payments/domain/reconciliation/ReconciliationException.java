package com.payments.domain.reconciliation;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reconciliation Exception Domain Model
 *
 * <p>Represents a reconciliation exception in the domain layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationException {

  private String exceptionId;
  private String runId;
  private String type;
  private String severity;
  private String status;
  private String description;
  private String details;
  private String tenantId;
  private String businessUnitId;
  private String correlationId;
  private String transactionId;
  private String paymentId;
  private String clearingSystem;
  private String resolution;
  private String resolutionNotes;
  private String resolvedBy;
  private LocalDateTime resolvedAt;
  private String priority;
  private String category;
  private String subcategory;
  private String tags;
  private String notes;
  private Boolean isUrgent;
  private Boolean isHighPriority;
  private Boolean isEscalated;
  private Integer escalationLevel;
  private String escalatedBy;
  private LocalDateTime escalatedAt;
  private String escalationReason;
  private String assignedTo;
  private LocalDateTime assignedAt;
  private LocalDateTime dueDate;
  private Boolean slaBreach;
  private String slaBreachReason;
  private Integer retryCount;
  private Integer maxRetries;
  private LocalDateTime lastRetryAt;
  private String metadata;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Long version;
}
