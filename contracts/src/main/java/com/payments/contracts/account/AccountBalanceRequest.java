package com.payments.contracts.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Account Balance Request DTO
 *
 * <p>Request for retrieving account balance: - Account details - Request metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account balance request")
public class AccountBalanceRequest {

  @NotBlank(message = "Account number is required")
  @Schema(description = "Account number", required = true, example = "1234567890")
  private String accountNumber;

  @NotBlank(message = "Tenant ID is required")
  @Schema(description = "Tenant identifier", required = true, example = "tenant-001")
  private String tenantId;

  @NotBlank(message = "Business unit ID is required")
  @Schema(description = "Business unit identifier", required = true, example = "bu-001")
  private String businessUnitId;

  @NotNull(message = "Request timestamp is required")
  @Schema(description = "Request timestamp", required = true, format = "date-time")
  private Long requestTimestamp;

  @Schema(description = "Correlation ID for tracing", example = "corr-123456")
  private String correlationId;

  @Schema(description = "Request ID", example = "req-123456")
  private String requestId;

  @Schema(description = "Client ID", example = "client-001")
  private String clientId;
}
