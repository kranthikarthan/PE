package com.payments.reconciliation.dto;

import com.payments.domain.reconciliation.ReconciliationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for reconciliation exception response.
 *
 * <p>This DTO represents the response data for reconciliation
 * exception operations. It includes comprehensive field
 * definitions and response formatting.
 *
 * @since PE-412
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationExceptionResponse {
  
  private String exceptionId;
  
  private Long runId;
  
  private String exceptionType;
  
  private String internalTransactionId;
  
  private String clearingTransactionId;
  
  private BigDecimal amountDifference;
  
  private ReconciliationException.ExceptionStatus status;
  
  private String description;
  
  private String details;
  
  private String priority;
  
  private String assignedTo;
  
  private String resolution;
  
  private LocalDateTime resolvedAt;
  
  private String resolvedBy;
  
  private LocalDateTime createdAt;
  
  private String createdBy;
  
  private LocalDateTime updatedAt;
  
  private String updatedBy;
}
