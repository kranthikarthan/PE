package com.payments.contracts.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API Contract for Payment Initiation Service
 *
 * <p>This contract defines the external API interface for the Payment Initiation Service, enabling
 * loose coupling between services through well-defined contracts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment initiation request via API")
public class PaymentInitiationApiRequest {

  @NotBlank(message = "Payment ID is required")
  @Schema(description = "Unique payment identifier", example = "PAY-123456789")
  private String paymentId;

  @NotBlank(message = "Idempotency key is required")
  @Schema(description = "Idempotency key for duplicate prevention", example = "IDEMP-2023-001")
  private String idempotencyKey;

  @NotBlank(message = "Source account is required")
  @Schema(description = "Source account number", example = "1234567890")
  private String sourceAccount;

  @NotBlank(message = "Destination account is required")
  @Schema(description = "Destination account number", example = "9876543210")
  private String destinationAccount;

  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be positive")
  @Schema(description = "Payment amount")
  private BigDecimal amount;

  @NotBlank(message = "Currency is required")
  @Schema(description = "Currency code", example = "ZAR")
  private String currency;

  @NotBlank(message = "Reference is required")
  @Schema(description = "Payment reference", example = "INV-2023-001")
  private String reference;

  @NotBlank(message = "Payment type is required")
  @Schema(description = "Type of payment", example = "EFT")
  private String paymentType;

  @NotBlank(message = "Priority is required")
  @Schema(description = "Payment priority", example = "NORMAL")
  private String priority;

  @NotBlank(message = "Tenant ID is required")
  @Schema(description = "Tenant identifier", example = "TENANT-001")
  private String tenantId;

  @NotBlank(message = "Business unit ID is required")
  @Schema(description = "Business unit identifier", example = "BU-001")
  private String businessUnitId;

  @NotBlank(message = "Initiated by is required")
  @Schema(description = "User who initiated the payment", example = "user@company.com")
  private String initiatedBy;

  @Schema(description = "Additional metadata")
  private Map<String, String> metadata;

  @Schema(description = "Request timestamp")
  private Instant timestamp;
}
