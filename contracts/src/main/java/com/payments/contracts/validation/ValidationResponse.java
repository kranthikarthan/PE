package com.payments.contracts.validation;

import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for payment validation Aligns with PaymentValidatedEvent and ValidationFailedEvent
 * schemas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment validation response")
public class ValidationResponse {

  @NotNull(message = "Payment ID is required")
  @Valid
  @Schema(description = "Unique payment identifier", required = true)
  private PaymentId paymentId;

  @NotNull(message = "Validation status is required")
  @Schema(description = "Validation result status", required = true)
  private ValidationStatus status;

  @NotNull(message = "Tenant context is required")
  @Valid
  @Schema(description = "Tenant and business unit context", required = true)
  private TenantContext tenantContext;

  @NotNull(message = "Validated timestamp is required")
  @Schema(description = "Validation completion timestamp", required = true)
  private Instant validatedAt;

  @Schema(description = "Risk level assessment")
  private RiskLevel riskLevel;

  @Schema(description = "Fraud score (0-100)")
  private Integer fraudScore;

  @Schema(description = "List of failed validation rules")
  private List<FailedRule> failedRules;

  @Schema(description = "Optional error message if validation failed")
  private String errorMessage;
}
