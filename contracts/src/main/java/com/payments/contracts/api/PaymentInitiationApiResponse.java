package com.payments.contracts.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API Contract for Payment Initiation Response
 *
 * <p>This contract defines the response structure for payment initiation operations, providing a
 * standardized response format across all services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment initiation response via API")
public class PaymentInitiationApiResponse {

  @Schema(description = "Unique payment identifier", example = "PAY-123456789")
  private String paymentId;

  @Schema(description = "Payment status", example = "INITIATED")
  private String status;

  @Schema(description = "Status reason", example = "Payment successfully initiated")
  private String statusReason;

  @Schema(description = "Response timestamp")
  private Instant timestamp;

  @Schema(description = "Error message if applicable")
  private String errorMessage;

  @Schema(description = "Error code if applicable")
  private String errorCode;

  @Schema(description = "Additional response metadata")
  private Map<String, String> metadata;

  @Schema(description = "Processing time in milliseconds")
  private Long processingTimeMs;

  @Schema(description = "Correlation ID for tracing")
  private String correlationId;
}
