package com.payments.contracts.validation;

import com.payments.contracts.payment.PaymentInitiationRequest;
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

/** Request DTO for payment validation Aligns with PaymentInitiatedEvent schema */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment validation request")
public class ValidationRequest {

  @NotNull(message = "Payment ID is required")
  @Valid
  @Schema(description = "Unique payment identifier", required = true)
  private PaymentId paymentId;

  @NotNull(message = "Payment details are required")
  @Valid
  @Schema(description = "Payment initiation details", required = true)
  private PaymentInitiationRequest paymentDetails;

  @NotNull(message = "Tenant context is required")
  @Valid
  @Schema(description = "Tenant and business unit context", required = true)
  private TenantContext tenantContext;

  @NotNull(message = "Validation timestamp is required")
  @Schema(description = "Validation request timestamp", required = true)
  private Instant requestedAt;
}
