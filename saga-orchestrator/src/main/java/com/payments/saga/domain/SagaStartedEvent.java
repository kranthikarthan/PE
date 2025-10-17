package com.payments.saga.domain;

import com.payments.domain.shared.TenantContext;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Domain event for saga started */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SagaStartedEvent extends SagaEvent {
  private String sagaName;
  private String sagaType;
  private Map<String, Object> sagaData;

  public SagaStartedEvent(
      SagaId sagaId,
      TenantContext tenantContext,
      String correlationId,
      String sagaName,
      String sagaType,
      Map<String, Object> sagaData) {
    super(sagaId, tenantContext, correlationId, "SagaStarted");
    this.sagaName = sagaName;
    this.sagaType = sagaType;
    this.sagaData = sagaData;
  }

  @Override
  public Map<String, Object> getEventData() {
    return Map.of(
        "sagaName", sagaName,
        "sagaType", sagaType,
        "sagaData", sagaData != null ? sagaData : Map.of());
  }
}
