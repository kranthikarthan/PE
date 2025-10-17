package com.payments.contracts.routing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Routing Request DTO
 *
 * <p>Input for routing decision engine: - Payment attributes (amount, currency, type) - Tenant
 * context (tenant ID, business unit ID) - Account information (source, destination) - Processing
 * preferences (priority, mode) - Custom metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Routing request")
public class RoutingRequest {

  @NotBlank(message = "Payment ID is required")
  @Size(max = 50, message = "Payment ID must not exceed 50 characters")
  @Schema(description = "Payment identifier", required = true, example = "pay-123456")
  private String paymentId;

  @NotBlank(message = "Tenant ID is required")
  @Size(max = 50, message = "Tenant ID must not exceed 50 characters")
  @Schema(description = "Tenant identifier", required = true, example = "tenant-001")
  private String tenantId;

  @NotBlank(message = "Business Unit ID is required")
  @Size(max = 50, message = "Business Unit ID must not exceed 50 characters")
  @Schema(description = "Business unit identifier", required = true, example = "bu-001")
  private String businessUnitId;

  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be positive")
  @Schema(description = "Payment amount", required = true, example = "1000.00")
  private BigDecimal amount;

  @NotBlank(message = "Currency is required")
  @Size(min = 3, max = 3, message = "Currency must be 3 characters")
  @Schema(description = "Currency code", required = true, example = "ZAR")
  private String currency;

  @NotBlank(message = "Payment Type is required")
  @Size(max = 20, message = "Payment Type must not exceed 20 characters")
  @Schema(description = "Payment type", required = true, example = "EFT")
  private String paymentType;

  @NotBlank(message = "Source Account is required")
  @Size(max = 20, message = "Source Account must not exceed 20 characters")
  @Schema(description = "Source account number", required = true, example = "1234567890")
  private String sourceAccount;

  @NotBlank(message = "Destination Account is required")
  @Size(max = 20, message = "Destination Account must not exceed 20 characters")
  @Schema(description = "Destination account number", required = true, example = "0987654321")
  private String destinationAccount;

  @NotBlank(message = "Priority is required")
  @Size(max = 20, message = "Priority must not exceed 20 characters")
  @Schema(description = "Payment priority", required = true, example = "HIGH")
  private String priority;

  @NotNull(message = "Created At is required")
  @Schema(description = "Payment creation timestamp", required = true, format = "date-time")
  private Instant createdAt;

  @Schema(description = "Additional metadata")
  private Map<String, String> metadata;
}
