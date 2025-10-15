package com.payments.contracts.payment;

import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for payment initiation Aligns with PaymentInitiatedEvent schema */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment initiation response")
public class PaymentInitiationResponse {

  @NotNull(message = "Payment ID is required")
  @Valid
  @Schema(description = "Unique payment identifier", required = true)
  private PaymentId paymentId;

  @NotNull(message = "Status is required")
  @Schema(description = "Current payment status", required = true)
  private PaymentStatus status;

  @NotNull(message = "Tenant context is required")
  @Valid
  @Schema(description = "Tenant and business unit context", required = true)
  private TenantContext tenantContext;

  @NotNull(message = "Initiated timestamp is required")
  @Schema(description = "Payment initiation timestamp", required = true)
  private Instant initiatedAt;

  @Schema(description = "Optional error message if initiation failed")
  private String errorMessage;
}
