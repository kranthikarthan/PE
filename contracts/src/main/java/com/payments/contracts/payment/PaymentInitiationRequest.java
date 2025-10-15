package com.payments.contracts.payment;

import com.payments.domain.shared.Money;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for payment initiation Aligns with PaymentInitiatedEvent schema */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment initiation request")
public class PaymentInitiationRequest {

  @NotNull(message = "Payment ID is required")
  @Valid
  @Schema(description = "Unique payment identifier", required = true)
  private PaymentId paymentId;

  @NotBlank(message = "Idempotency key is required")
  @Size(max = 255, message = "Idempotency key must not exceed 255 characters")
  @Schema(
      description = "Idempotency key for duplicate prevention",
      required = true,
      maxLength = 255)
  private String idempotencyKey;

  @NotBlank(message = "Source account is required")
  @Size(min = 11, max = 11, message = "Source account must be exactly 11 digits")
  @Schema(description = "Source account number (11 digits)", required = true, pattern = "^\\d{11}$")
  private String sourceAccount;

  @NotBlank(message = "Destination account is required")
  @Size(min = 11, max = 11, message = "Destination account must be exactly 11 digits")
  @Schema(
      description = "Destination account number (11 digits)",
      required = true,
      pattern = "^\\d{11}$")
  private String destinationAccount;

  @NotNull(message = "Amount is required")
  @Valid
  @Schema(description = "Payment amount and currency", required = true)
  private Money amount;

  @NotBlank(message = "Payment reference is required")
  @Size(max = 35, message = "Payment reference must not exceed 35 characters")
  @Schema(description = "Payment reference", required = true, maxLength = 35)
  private String reference;

  @NotNull(message = "Payment type is required")
  @Schema(description = "Type of payment", required = true)
  private PaymentType paymentType;

  @NotNull(message = "Priority is required")
  @Schema(description = "Payment priority", required = true)
  private Priority priority;

  @NotNull(message = "Tenant context is required")
  @Valid
  @Schema(description = "Tenant and business unit context", required = true)
  private TenantContext tenantContext;

  @NotBlank(message = "Initiator is required")
  @Schema(description = "User who initiated the payment", required = true)
  private String initiatedBy;
}
