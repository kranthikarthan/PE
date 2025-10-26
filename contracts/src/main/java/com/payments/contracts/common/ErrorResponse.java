package com.payments.contracts.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Common Error Response DTO
 *
 * <p>Standardized error response structure used across all services for consistent error handling
 * and API documentation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response")
public class ErrorResponse {

  @Schema(description = "Error message", example = "Payment not found")
  private String message;

  @Schema(description = "Error code", example = "PAYMENT_NOT_FOUND")
  private String code;

  @Schema(description = "Timestamp when error occurred", example = "2023-10-26T09:46:07Z")
  private String timestamp;

  @Schema(
      description = "Additional error details",
      example = "{\"field\": \"paymentId\", \"value\": \"PAY-123\"}")
  private Map<String, String> details;
}
