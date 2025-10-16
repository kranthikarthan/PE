package com.payments.contracts.events;

import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentValidatedEvent {
  private UUID eventId;
  private String eventType;
  private Instant timestamp;
  private String correlationId;
  private String source;
  private String version;
  private String tenantId;
  private String businessUnitId;
  private PaymentId paymentId;
  private TenantContext tenantContext;
  private ValidationResponseDto validationResult;
  private Instant validatedAt;
  private String riskLevel;
  private Integer fraudScore;
}
