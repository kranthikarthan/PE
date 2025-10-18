package com.payments.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Status Change Request DTO.
 *
 * <p>Used for POST /tenants/{id}/activate, suspend, deactivate endpoints.
 *
 * <p>Currently a placeholder for future use (reason tracking, etc.)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusChangeRequest {

  @NotBlank(message = "reason is required")
  private String reason;
}
