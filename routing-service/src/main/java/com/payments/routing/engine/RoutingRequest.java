package com.payments.routing.engine;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Routing Request DTO
 *
 * <p>Input for routing decision engine: - Payment attributes (amount, currency, type) - Tenant
 * context (tenant ID, business unit ID) - Account information (source, destination) - Processing
 * preferences (priority, mode) - Custom metadata
 *
 * <p>Validation: Jakarta validation annotations Performance: Immutable DTO with builder pattern
 */
@Data
@Builder
public class RoutingRequest {

  @NotBlank(message = "Payment ID is required")
  @Size(max = 50, message = "Payment ID must not exceed 50 characters")
  private String paymentId;

  @NotBlank(message = "Tenant ID is required")
  @Size(max = 50, message = "Tenant ID must not exceed 50 characters")
  private String tenantId;

  @NotBlank(message = "Business Unit ID is required")
  @Size(max = 50, message = "Business Unit ID must not exceed 50 characters")
  private String businessUnitId;

  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be positive")
  private BigDecimal amount;

  @NotBlank(message = "Currency is required")
  @Size(min = 3, max = 3, message = "Currency must be 3 characters")
  private String currency;

  @NotBlank(message = "Payment Type is required")
  @Size(max = 20, message = "Payment Type must not exceed 20 characters")
  private String paymentType;

  @NotBlank(message = "Source Account is required")
  @Size(max = 20, message = "Source Account must not exceed 20 characters")
  private String sourceAccount;

  @NotBlank(message = "Destination Account is required")
  @Size(max = 20, message = "Destination Account must not exceed 20 characters")
  private String destinationAccount;

  @NotBlank(message = "Priority is required")
  @Size(max = 20, message = "Priority must not exceed 20 characters")
  private String priority;

  @NotNull(message = "Created At is required")
  private Instant createdAt;

  private Map<String, String> metadata;
}
