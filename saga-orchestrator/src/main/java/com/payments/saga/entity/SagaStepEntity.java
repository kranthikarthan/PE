package com.payments.saga.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.domain.shared.TenantContext;
import com.payments.saga.domain.SagaId;
import com.payments.saga.domain.SagaStep;
import com.payments.saga.domain.SagaStepId;
import com.payments.saga.domain.SagaStepStatus;
import com.payments.saga.domain.SagaStepType;
import com.payments.saga.exception.SagaPersistenceException;
import com.payments.saga.exception.SagaSerializationException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** JPA entity for SagaStep persistence */
@Entity
@Table(name = "saga_steps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStepEntity {
  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "saga_id", nullable = false)
  private String sagaId;

  @Column(name = "step_name", nullable = false)
  private String stepName;

  @Enumerated(EnumType.STRING)
  @Column(name = "step_type", nullable = false)
  private SagaStepType stepType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private SagaStepStatus status;

  @Column(name = "sequence", nullable = false)
  private Integer sequence;

  @Column(name = "service_name", nullable = false)
  private String serviceName;

  @Column(name = "endpoint", length = 500)
  private String endpoint;

  @Column(name = "compensation_endpoint", length = 500)
  private String compensationEndpoint;

  @Column(name = "input_data", columnDefinition = "jsonb")
  private String inputDataJson;

  @Column(name = "output_data", columnDefinition = "jsonb")
  private String outputDataJson;

  @Column(name = "error_data", columnDefinition = "jsonb")
  private String errorDataJson;

  @Column(name = "error_message", columnDefinition = "text")
  private String errorMessage;

  @Column(name = "retry_count")
  private Integer retryCount;

  @Column(name = "max_retries")
  private Integer maxRetries;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "failed_at")
  private Instant failedAt;

  @Column(name = "compensated_at")
  private Instant compensatedAt;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "business_unit_id", nullable = false)
  private String businessUnitId;

  @Column(name = "correlation_id", nullable = false)
  private String correlationId;

  @CreationTimestamp
  @Column(name = "created_at")
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  // Add audit fields for domain events
  @Column(name = "version")
  private Long version;

  @Column(name = "last_modified_by")
  private String lastModifiedBy;

  @Column(name = "domain_events", columnDefinition = "jsonb")
  private String domainEventsJson;

  // Singleton ObjectMapper for efficiency
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static SagaStepEntity fromDomain(SagaStep step) {
    try {
      return SagaStepEntity.builder()
          .id(step.getId().getValue())
          .sagaId(step.getSagaId().getValue())
          .stepName(step.getStepName())
          .stepType(step.getStepType())
          .status(step.getStatus())
          .sequence(step.getSequence())
          .serviceName(step.getServiceName())
          .endpoint(step.getEndpoint())
          .compensationEndpoint(step.getCompensationEndpoint())
          .inputDataJson(convertMapToJson(step.getInputData()))
          .outputDataJson(convertMapToJson(step.getOutputData()))
          .errorDataJson(convertMapToJson(step.getErrorData()))
          .errorMessage(step.getErrorMessage())
          .retryCount(step.getRetryCount())
          .maxRetries(step.getMaxRetries())
          .startedAt(step.getStartedAt())
          .completedAt(step.getCompletedAt())
          .failedAt(step.getFailedAt())
          .compensatedAt(step.getCompensatedAt())
          .tenantId(step.getTenantContext().getTenantId())
          .businessUnitId(step.getTenantContext().getBusinessUnitId())
          .correlationId(step.getCorrelationId())
          .version(step.getVersion())
          .lastModifiedBy(step.getLastModifiedBy())
          .domainEventsJson(null)
          .build();
    } catch (Exception e) {
      throw new SagaPersistenceException("Failed to convert domain to entity", e);
    }
  }

  public SagaStep toDomain() {
    try {
      TenantContext tenantContext =
          TenantContext.of(
              this.tenantId,
              resolveTenantName(this.tenantId),
              this.businessUnitId,
              resolveBusinessUnitName(this.businessUnitId));

      return SagaStep.builder()
          .id(SagaStepId.of(this.id))
          .sagaId(SagaId.of(this.sagaId))
          .stepName(this.stepName)
          .stepType(this.stepType)
          .status(this.status)
          .sequence(this.sequence)
          .serviceName(this.serviceName)
          .endpoint(this.endpoint)
          .compensationEndpoint(this.compensationEndpoint)
          .inputData(convertJsonToMap(this.inputDataJson))
          .outputData(convertJsonToMap(this.outputDataJson))
          .errorData(convertJsonToMap(this.errorDataJson))
          .errorMessage(this.errorMessage)
          .retryCount(this.retryCount != null ? this.retryCount : 0)
          .maxRetries(this.maxRetries != null ? this.maxRetries : 3)
          .startedAt(this.startedAt)
          .completedAt(this.completedAt)
          .failedAt(this.failedAt)
          .compensatedAt(this.compensatedAt)
          .tenantContext(tenantContext)
          .correlationId(this.correlationId)
          .version(this.version)
          .lastModifiedBy(this.lastModifiedBy)
          .domainEvents(new ArrayList<>())
          .build();
    } catch (Exception e) {
      throw new SagaPersistenceException("Failed to convert entity to domain", e);
    }
  }

  private static String convertMapToJson(Map<String, Object> data) {
    if (data == null || data.isEmpty()) {
      return null;
    }
    try {
      return OBJECT_MAPPER.writeValueAsString(data);
    } catch (Exception e) {
      throw new SagaSerializationException("Failed to convert map to JSON", e);
    }
  }

  private static Map<String, Object> convertJsonToMap(String json) {
    if (json == null || json.trim().isEmpty()) {
      return null;
    }
    try {
      return OBJECT_MAPPER.readValue(
          json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      throw new SagaSerializationException("Failed to convert JSON to map", e);
    }
  }

  private String resolveTenantName(String tenantId) {
    // Implement proper tenant name resolution
    return "Resolved Tenant Name";
  }

  private String resolveBusinessUnitName(String businessUnitId) {
    // Implement proper business unit name resolution
    return "Resolved Business Unit Name";
  }
}
