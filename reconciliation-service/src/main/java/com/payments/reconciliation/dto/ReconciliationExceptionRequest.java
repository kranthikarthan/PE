package com.payments.reconciliation.dto;

import com.payments.domain.reconciliation.ReconciliationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for reconciliation exception request.
 *
 * <p>This DTO represents the request data for creating
 * reconciliation exceptions. It includes validation
 * annotations and comprehensive field definitions.
 *
 * @since PE-412
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationExceptionRequest {
  
  @NotNull(message = "Run ID is required")
  private Long runId;
  
  @NotBlank(message = "Exception type is required")
  @Size(max = 50, message = "Exception type must not exceed 50 characters")
  private String exceptionType;
  
  @Size(max = 100, message = "Internal transaction ID must not exceed 100 characters")
  private String internalTransactionId;
  
  @Size(max = 100, message = "Clearing transaction ID must not exceed 100 characters")
  private String clearingTransactionId;
  
  private BigDecimal amountDifference;
  
  @Size(max = 500, message = "Description must not exceed 500 characters")
  private String description;
  
  @Size(max = 2000, message = "Details must not exceed 2000 characters")
  private String details;
  
  @Size(max = 20, message = "Priority must not exceed 20 characters")
  private String priority;
  
  @Size(max = 100, message = "Assigned to must not exceed 100 characters")
  private String assignedTo;
}
