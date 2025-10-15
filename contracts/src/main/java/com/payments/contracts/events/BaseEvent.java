package com.payments.contracts.events;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Base event structure for all domain events Aligns with AsyncAPI EventMetadata schema */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Base event metadata")
public class BaseEvent {

  @NotNull(message = "Event ID is required")
  @Schema(description = "Unique event identifier", required = true, format = "uuid")
  private UUID eventId;

  @NotBlank(message = "Event type is required")
  @Schema(description = "Type of event", required = true)
  private String eventType;

  @NotNull(message = "Timestamp is required")
  @Schema(description = "Event occurrence timestamp", required = true, format = "date-time")
  private Instant timestamp;

  @NotNull(message = "Correlation ID is required")
  @Schema(
      description = "Correlation ID for tracing across services",
      required = true,
      format = "uuid")
  private UUID correlationId;

  @Schema(description = "ID of event that caused this event", format = "uuid")
  private UUID causationId;

  @NotBlank(message = "Source is required")
  @Schema(description = "Source service that published event", required = true)
  private String source;

  @NotBlank(message = "Version is required")
  @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "Version must follow semantic versioning")
  @Schema(
      description = "Semantic version of the event schema",
      required = true,
      pattern = "^\\d+\\.\\d+\\.\\d+$")
  private String version;

  @NotBlank(message = "Tenant ID is required")
  @Schema(description = "Tenant identifier for multi-tenancy", required = true)
  private String tenantId;

  @NotBlank(message = "Business unit ID is required")
  @Schema(description = "Business unit identifier within tenant", required = true)
  private String businessUnitId;
}
