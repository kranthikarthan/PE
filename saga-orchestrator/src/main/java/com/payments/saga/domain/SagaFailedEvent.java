package com.payments.saga.domain;

import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Domain event for saga failed */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SagaFailedEvent extends SagaEvent {
  private String sagaName;
  private Instant failedAt;
  private String failureReason;
  private Map<String, Object> failureData;

  public SagaFailedEvent(
      SagaId sagaId,
      TenantContext tenantContext,
      String correlationId,
      String sagaName,
      Instant failedAt,
      String failureReason,
      Map<String, Object> failureData) {
    super(sagaId, tenantContext, correlationId, "SagaFailed");
    this.sagaName = sagaName;
    this.failedAt = failedAt;
    this.failureReason = failureReason;
    this.failureData = failureData;
  }

  @Override
  public Map<String, Object> getEventData() {
    return Map.of(
        "sagaName",
        sagaName,
        "failedAt",
        failedAt.toString(),
        "failureReason",
        failureReason,
        "failureData",
        failureData != null ? failureData : Map.of());
  }
}
