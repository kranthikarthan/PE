package com.payments.contracts.events;

import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationFailedEvent {
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
  private List<FailedRuleDto> failedRules;
  private Instant failedAt;
  private String riskLevel;
  private Integer fraudScore;
}
