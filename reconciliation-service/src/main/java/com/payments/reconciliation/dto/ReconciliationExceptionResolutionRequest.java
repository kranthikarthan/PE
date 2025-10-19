package com.payments.reconciliation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for reconciliation exception resolution request.
 *
 * <p>This DTO represents the request data for resolving
 * reconciliation exceptions. It includes validation
 * annotations and comprehensive field definitions.
 *
 * @since PE-412
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationExceptionResolutionRequest {
  
  @NotBlank(message = "Resolution is required")
  @Size(max = 500, message = "Resolution must not exceed 500 characters")
  private String resolution;
  
  @Size(max = 2000, message = "Resolution notes must not exceed 2000 characters")
  private String resolutionNotes;
}
