package com.payments.saga.domain;

import com.payments.domain.shared.TenantContext;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Event published when a saga step completes */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SagaStepCompletedEvent extends SagaEvent {
  private String stepName;
  private SagaStepType stepType;
  private int sequence;
  private String serviceName;
  private Map<String, Object> outputData;

  public SagaStepCompletedEvent(
      SagaId sagaId,
      TenantContext tenantContext,
      String correlationId,
      String stepName,
      SagaStepType stepType,
      int sequence,
      String serviceName,
      Map<String, Object> outputData) {
    super(sagaId, tenantContext, correlationId, "SagaStepCompleted");
    this.stepName = stepName;
    this.stepType = stepType;
    this.sequence = sequence;
    this.serviceName = serviceName;
    this.outputData = outputData;
  }

  @Override
  public Map<String, Object> getEventData() {
    return Map.of(
        "stepName", stepName,
        "stepType", stepType.name(),
        "sequence", sequence,
        "serviceName", serviceName,
        "outputData", outputData != null ? outputData : Map.of());
  }
}
