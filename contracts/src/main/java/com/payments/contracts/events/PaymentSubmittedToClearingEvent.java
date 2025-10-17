package com.payments.contracts.events;

import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Payment Submitted to Clearing Event
 *
 * <p>Event published when a payment is submitted to clearing: - Payment identification - Clearing
 * system reference - Submission timestamp - Clearing metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Payment submitted to clearing event")
public class PaymentSubmittedToClearingEvent extends BaseEvent {

  @Schema(description = "Payment identifier", required = true)
  private PaymentId paymentId;

  @Schema(description = "Tenant context", required = true)
  private TenantContext tenantContext;

  @Schema(description = "Clearing system reference", example = "SAMOS-123456")
  private String clearingSystemReference;

  @Schema(description = "Clearing system", example = "SAMOS")
  private String clearingSystem;

  @Schema(description = "Submission timestamp", format = "date-time")
  private Instant submittedAt;

  @Schema(description = "Clearing instructions")
  private String clearingInstructions;

  @Schema(description = "Clearing metadata")
  private java.util.Map<String, String> clearingMetadata;
}
