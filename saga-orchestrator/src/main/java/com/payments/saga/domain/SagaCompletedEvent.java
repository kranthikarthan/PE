package com.payments.saga.domain;

import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Domain event for saga completed */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SagaCompletedEvent extends SagaEvent {
  private String sagaName;
  private Instant completedAt;
  private int totalSteps;
  private int completedSteps;
  private Map<String, Object> sagaResult;

  public SagaCompletedEvent(
      SagaId sagaId,
      TenantContext tenantContext,
      String correlationId,
      String sagaName,
      Instant completedAt,
      int totalSteps,
      int completedSteps,
      Map<String, Object> sagaResult) {
    super(sagaId, tenantContext, correlationId, "SagaCompleted");
    this.sagaName = sagaName;
    this.completedAt = completedAt;
    this.totalSteps = totalSteps;
    this.completedSteps = completedSteps;
    this.sagaResult = sagaResult;
  }

  @Override
  public Map<String, Object> getEventData() {
    return Map.of(
        "sagaName", sagaName,
        "completedAt", completedAt.toString(),
        "totalSteps", totalSteps,
        "completedSteps", completedSteps,
        "sagaResult", sagaResult != null ? sagaResult : Map.of());
  }
}
