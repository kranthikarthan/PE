package com.payments.saga.domain;

import com.payments.domain.shared.TenantContext;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/** Event published when a saga step fails */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SagaStepFailedEvent extends SagaEvent {
  private String stepName;
  private SagaStepType stepType;
  private int sequence;
  private String serviceName;
  private String errorMessage;
  private Map<String, Object> errorData;
  private int retryCount;
  private int maxRetries;

  public SagaStepFailedEvent(
      SagaId sagaId,
      TenantContext tenantContext,
      String correlationId,
      String stepName,
      SagaStepType stepType,
      int sequence,
      String serviceName,
      String errorMessage,
      Map<String, Object> errorData,
      int retryCount,
      int maxRetries) {
    super(sagaId, tenantContext, correlationId, "SagaStepFailed");
    this.stepName = stepName;
    this.stepType = stepType;
    this.sequence = sequence;
    this.serviceName = serviceName;
    this.errorMessage = errorMessage;
    this.errorData = errorData;
    this.retryCount = retryCount;
    this.maxRetries = maxRetries;
  }

  @Override
  public Map<String, Object> getEventData() {
    return Map.of(
        "stepName", stepName,
        "stepType", stepType.name(),
        "sequence", sequence,
        "serviceName", serviceName,
        "errorMessage", errorMessage,
        "errorData", errorData != null ? errorData : Map.of(),
        "retryCount", retryCount,
        "maxRetries", maxRetries);
  }
}
