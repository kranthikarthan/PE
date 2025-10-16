package com.payments.saga.domain;

import com.payments.domain.shared.TenantContext;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Event published when saga compensation starts */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SagaCompensationStartedEvent extends SagaEvent {
  private String sagaName;
  private String paymentId;
  private String reason;
  private int stepsToCompensate;

  public SagaCompensationStartedEvent(
      SagaId sagaId,
      TenantContext tenantContext,
      String correlationId,
      String sagaName,
      String paymentId,
      String reason,
      int stepsToCompensate) {
    super(sagaId, tenantContext, correlationId, "SagaCompensationStarted");
    this.sagaName = sagaName;
    this.paymentId = paymentId;
    this.reason = reason;
    this.stepsToCompensate = stepsToCompensate;
  }

  @Override
  public Map<String, Object> getEventData() {
    return Map.of(
        "sagaName", sagaName,
        "paymentId", paymentId,
        "reason", reason,
        "stepsToCompensate", stepsToCompensate);
  }
}
