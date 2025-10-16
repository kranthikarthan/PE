package com.payments.saga.domain;

import com.payments.domain.shared.TenantContext;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Event published when a saga step starts */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SagaStepStartedEvent extends SagaEvent {
  private String stepName;
  private SagaStepType stepType;
  private int sequence;
  private String serviceName;
  private String endpoint;

  public SagaStepStartedEvent(
      SagaId sagaId,
      TenantContext tenantContext,
      String correlationId,
      String stepName,
      SagaStepType stepType,
      int sequence,
      String serviceName,
      String endpoint) {
    super(sagaId, tenantContext, correlationId, "SagaStepStarted");
    this.stepName = stepName;
    this.stepType = stepType;
    this.sequence = sequence;
    this.serviceName = serviceName;
    this.endpoint = endpoint;
  }

  @Override
  public Map<String, Object> getEventData() {
    return Map.of(
        "stepName", stepName,
        "stepType", stepType.name(),
        "sequence", sequence,
        "serviceName", serviceName,
        "endpoint", endpoint);
  }
}
