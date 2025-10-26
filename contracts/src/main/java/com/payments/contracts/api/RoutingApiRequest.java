package com.payments.contracts.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API Contract for Routing Service
 *
 * <p>This contract defines the external API interface for the Routing Service, enabling payment
 * routing decisions through well-defined contracts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Routing request via API")
public class RoutingApiRequest {

  @NotBlank(message = "Payment ID is required")
  @Schema(description = "Unique payment identifier", example = "PAY-123456789")
  private String paymentId;

  @NotBlank(message = "Tenant ID is required")
  @Schema(description = "Tenant identifier", example = "TENANT-001")
  private String tenantId;

  @NotBlank(message = "Business unit ID is required")
  @Schema(description = "Business unit identifier", example = "BU-001")
  private String businessUnitId;

  @NotNull(message = "Amount is required")
  @Schema(description = "Payment amount")
  private BigDecimal amount;

  @NotBlank(message = "Currency is required")
  @Schema(description = "Currency code", example = "ZAR")
  private String currency;

  @NotBlank(message = "Payment type is required")
  @Schema(description = "Type of payment", example = "EFT")
  private String paymentType;

  @NotBlank(message = "Source account is required")
  @Schema(description = "Source account number", example = "1234567890")
  private String sourceAccount;

  @NotBlank(message = "Destination account is required")
  @Schema(description = "Destination account number", example = "9876543210")
  private String destinationAccount;

  @NotBlank(message = "Priority is required")
  @Schema(description = "Payment priority", example = "NORMAL")
  private String priority;

  @Schema(description = "Request timestamp")
  private Instant createdAt;

  @Schema(description = "Additional routing metadata")
  private Map<String, String> metadata;
}
