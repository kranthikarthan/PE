package com.payments.contracts.events;

import com.payments.contracts.validation.FailedRule;
import com.payments.contracts.validation.RiskLevel;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Validation failed event Aligns with AsyncAPI ValidationFailed message schema */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Validation failed event")
public class ValidationFailedEvent extends BaseEvent {

  @NotNull(message = "Payment ID is required")
  @Valid
  @Schema(description = "Unique payment identifier", required = true)
  private PaymentId paymentId;

  @NotNull(message = "Tenant context is required")
  @Valid
  @Schema(description = "Tenant and business unit context", required = true)
  private TenantContext tenantContext;

  @NotNull(message = "Failed timestamp is required")
  @Schema(description = "Validation failure timestamp", required = true)
  private Instant failedAt;

  @Schema(description = "Risk level assessment")
  private RiskLevel riskLevel;

  @Schema(description = "Fraud score (0-100)")
  private Integer fraudScore;

  @Schema(description = "List of failed validation rules")
  private List<FailedRule> failedRules;

  @Schema(description = "Failure reason")
  private String failureReason;
}
