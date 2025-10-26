package com.payments.contracts.events.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base Event Contract for API Events
 *
 * <p>This contract defines the base structure for all API events, providing common fields for
 * event-driven communication.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Base API event structure")
public class BaseApiEvent {

  @Schema(description = "Event ID", example = "evt-123456789")
  private String eventId;

  @Schema(description = "Event type", example = "payment.processing.started")
  private String eventType;

  @Schema(description = "Event version", example = "1.0")
  private String eventVersion;

  @Schema(description = "Event timestamp")
  private Instant timestamp;

  @Schema(description = "Correlation ID for tracing", example = "corr-123456789")
  private String correlationId;

  @Schema(description = "Source service", example = "payment-initiation-service")
  private String sourceService;

  @Schema(description = "Event metadata")
  private Map<String, String> metadata;
}
