package com.payments.contracts.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API Contract for Routing Service Response
 *
 * <p>This contract defines the response structure for routing operations, providing routing
 * decisions and clearing system information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Routing response via API")
public class RoutingApiResponse {

  @Schema(description = "Unique payment identifier", example = "PAY-123456789")
  private String paymentId;

  @Schema(description = "Selected clearing system", example = "SAMOS")
  private String clearingSystem;

  @Schema(description = "Routing decision reason", example = "Amount within SAMOS limits")
  private String routingReason;

  @Schema(description = "Routing confidence score", example = "0.95")
  private Double confidenceScore;

  @Schema(description = "Alternative clearing systems")
  private String[] alternativeClearingSystems;

  @Schema(description = "Response timestamp")
  private Instant timestamp;

  @Schema(description = "Error message if applicable")
  private String errorMessage;

  @Schema(description = "Error code if applicable")
  private String errorCode;

  @Schema(description = "Additional routing metadata")
  private Map<String, String> metadata;

  @Schema(description = "Processing time in milliseconds")
  private Long processingTimeMs;

  @Schema(description = "Correlation ID for tracing")
  private String correlationId;
}
