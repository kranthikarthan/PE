package com.payments.contracts.events.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event Contract for Payment Processing Events
 *
 * <p>This contract defines standardized events for payment processing, enabling event-driven
 * communication between microservices.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Payment processing event")
public class PaymentProcessingEvent extends BaseApiEvent {

  @Schema(description = "Unique payment identifier", example = "PAY-123456789")
  private String paymentId;

  @Schema(description = "Payment status", example = "PROCESSING")
  private String status;

  @Schema(description = "Payment amount")
  private BigDecimal amount;

  @Schema(description = "Currency code", example = "ZAR")
  private String currency;

  @Schema(description = "Source account", example = "1234567890")
  private String sourceAccount;

  @Schema(description = "Destination account", example = "9876543210")
  private String destinationAccount;

  @Schema(description = "Clearing system", example = "SAMOS")
  private String clearingSystem;

  @Schema(description = "Tenant ID", example = "TENANT-001")
  private String tenantId;

  @Schema(description = "Business unit ID", example = "BU-001")
  private String businessUnitId;

  @Schema(description = "Processing stage", example = "ROUTING")
  private String processingStage;

  @Schema(description = "Event-specific metadata")
  private Map<String, String> eventMetadata;
}
