package com.payments.contracts.events;

import com.payments.contracts.validation.RiskLevel;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Payment validated event Aligns with AsyncAPI PaymentValidated message schema */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Payment validated event")
public class PaymentValidatedEvent extends BaseEvent {

  @NotNull(message = "Payment ID is required")
  @Valid
  @Schema(description = "Unique payment identifier", required = true)
  private PaymentId paymentId;

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
}
