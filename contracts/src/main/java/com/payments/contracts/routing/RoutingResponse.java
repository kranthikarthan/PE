package com.payments.contracts.routing;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Routing Response DTO
 *
 * <p>Response from routing decision engine: - Routing decision - Clearing system - Processing
 * instructions - Routing metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Routing response")
public class RoutingResponse {

  @Schema(description = "Payment identifier", example = "pay-123456")
  private String paymentId;

  @Schema(description = "Routing decision", example = "ROUTE_TO_SAMOS")
  private String routingDecision;

  @Schema(description = "Clearing system", example = "SAMOS")
  private String clearingSystem;

  @Schema(description = "Processing instructions")
  private String processingInstructions;

  @Schema(description = "Routing reason", example = "Standard EFT routing")
  private String routingReason;

  @Schema(description = "Routing timestamp", format = "date-time")
  private Instant routedAt;

  @Schema(description = "Routing metadata")
  private Map<String, String> routingMetadata;

  @Schema(description = "Alternative routing options")
  private List<String> alternativeRoutes;

  @Schema(description = "Error code if routing failed", example = "NO_ROUTE_FOUND")
  private String errorCode;

  @Schema(description = "Error message if routing failed", example = "No suitable route found")
  private String errorMessage;
}
