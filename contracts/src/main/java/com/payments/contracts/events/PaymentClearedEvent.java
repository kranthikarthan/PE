package com.payments.contracts.events;

import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Payment Cleared Event
 *
 * <p>Event published when a payment is cleared: - Payment identification - Clearing
 * confirmation - Clearing timestamp - Clearing metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Payment cleared event")
public class PaymentClearedEvent extends BaseEvent {

  @Schema(description = "Payment identifier", required = true)
  private PaymentId paymentId;

  @Schema(description = "Tenant context", required = true)
  private TenantContext tenantContext;

  @Schema(description = "Clearing confirmation number", example = "CONF-123456")
  private String clearingConfirmationNumber;

  @Schema(description = "Clearing system", example = "SAMOS")
  private String clearingSystem;

  @Schema(description = "Clearing timestamp", format = "date-time")
  private Instant clearedAt;

  @Schema(description = "Clearing reference", example = "SAMOS-123456")
  private String clearingReference;

  @Schema(description = "Clearing metadata")
  private java.util.Map<String, String> clearingMetadata;
}
