package com.payments.saga.domain;

import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Domain event for saga compensation started */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SagaCompensationStartedEvent extends SagaEvent {
  private String sagaName;
  private Instant compensationStartedAt;
  private String compensationReason;
  private Map<String, Object> compensationData;

  public SagaCompensationStartedEvent(
      SagaId sagaId,
      TenantContext tenantContext,
      String correlationId,
      String sagaName,
      Instant compensationStartedAt,
      String compensationReason,
      Map<String, Object> compensationData) {
    super(sagaId, tenantContext, correlationId, "SagaCompensationStarted");
    this.sagaName = sagaName;
    this.compensationStartedAt = compensationStartedAt;
    this.compensationReason = compensationReason;
    this.compensationData = compensationData;
  }

  @Override
  public Map<String, Object> getEventData() {
    return Map.of(
        "sagaName",
        sagaName,
        "compensationStartedAt",
        compensationStartedAt.toString(),
        "compensationReason",
        compensationReason,
        "compensationData",
        compensationData != null ? compensationData : Map.of());
  }
}
