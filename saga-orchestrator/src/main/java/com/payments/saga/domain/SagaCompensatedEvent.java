package com.payments.saga.domain;

import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Domain event for saga compensated */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SagaCompensatedEvent extends SagaEvent {
  private String sagaName;
  private Instant compensatedAt;
  private int compensatedSteps;
  private Map<String, Object> compensationResult;

  public SagaCompensatedEvent(
      SagaId sagaId,
      TenantContext tenantContext,
      String correlationId,
      String sagaName,
      Instant compensatedAt,
      int compensatedSteps,
      Map<String, Object> compensationResult) {
    super(sagaId, tenantContext, correlationId, "SagaCompensated");
    this.sagaName = sagaName;
    this.compensatedAt = compensatedAt;
    this.compensatedSteps = compensatedSteps;
    this.compensationResult = compensationResult;
  }

  @Override
  public Map<String, Object> getEventData() {
    return Map.of(
        "sagaName",
        sagaName,
        "compensatedAt",
        compensatedAt.toString(),
        "compensatedSteps",
        compensatedSteps,
        "compensationResult",
        compensationResult != null ? compensationResult : Map.of());
  }
}
