package com.payments.contracts.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Account Validation Request DTO
 *
 * <p>Request for validating account information: - Account details - Validation criteria - Request
 * metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account validation request")
public class AccountValidationRequest {

  @NotBlank(message = "Account number is required")
  @Schema(description = "Account number to validate", required = true, example = "1234567890")
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

  @Schema(description = "Account holder name", example = "John Doe")
  private String accountHolderName;

  @Schema(description = "Account type", example = "CHECKING")
  private String accountType;

  @Schema(description = "Validation type", example = "STANDARD")
  private String validationType;

  @Schema(description = "Correlation ID for tracing", example = "corr-123456")
  private String correlationId;

  @Schema(description = "Request ID", example = "req-123456")
  private String requestId;

  @Schema(description = "Client ID", example = "client-001")
  private String clientId;
}
