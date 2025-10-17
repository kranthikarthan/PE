package com.payments.saga.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payments.domain.shared.TenantContext;
import com.payments.saga.domain.Saga;
import com.payments.saga.domain.SagaId;
import com.payments.saga.domain.SagaStatus;
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

/** JPA entity for Saga persistence */
@Entity
@Table(name = "sagas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaEntity {
  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "saga_name", nullable = false)
  private String sagaName;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private SagaStatus status;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "business_unit_id", nullable = false)
  private String businessUnitId;

  @Column(name = "correlation_id", nullable = false)
  private String correlationId;

  @Column(name = "payment_id")
  private String paymentId;

  @Column(name = "saga_data", columnDefinition = "jsonb")
  private String sagaDataJson;

  @Column(name = "error_message", columnDefinition = "text")
  private String errorMessage;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "failed_at")
  private Instant failedAt;

  @Column(name = "compensated_at")
  private Instant compensatedAt;

  @Column(name = "current_step_index")
  private Integer currentStepIndex;

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

  public static SagaEntity fromDomain(Saga saga) {
    try {
      return SagaEntity.builder()
          .id(saga.getId().getValue())
          .sagaName(saga.getSagaName())
          .status(saga.getStatus())
          .tenantId(saga.getTenantContext().getTenantId())
          .businessUnitId(saga.getTenantContext().getBusinessUnitId())
          .correlationId(saga.getCorrelationId())
          .paymentId(saga.getPaymentId())
          .sagaDataJson(convertSagaDataToJson(saga.getSagaData()))
          .errorMessage(saga.getErrorMessage())
          .startedAt(saga.getStartedAt())
          .completedAt(saga.getCompletedAt())
          .failedAt(saga.getFailedAt())
          .compensatedAt(saga.getCompensatedAt())
          .currentStepIndex(saga.getCurrentStepIndex())
          .version(saga.getVersion())
          .lastModifiedBy(saga.getLastModifiedBy())
          .domainEventsJson(null)
          .build();
    } catch (Exception e) {
      throw new SagaPersistenceException("Failed to convert domain to entity", e);
    }
  }

  public Saga toDomain() {
    try {
      TenantContext tenantContext =
          TenantContext.of(
              this.tenantId,
              resolveTenantName(this.tenantId),
              this.businessUnitId,
              resolveBusinessUnitName(this.businessUnitId));

      return Saga.builder()
          .id(SagaId.of(this.id))
          .sagaName(this.sagaName)
          .status(this.status)
          .tenantContext(tenantContext)
          .correlationId(this.correlationId)
          .paymentId(this.paymentId)
          .sagaData(convertJsonToSagaData(this.sagaDataJson))
          .errorMessage(this.errorMessage)
          .startedAt(this.startedAt)
          .completedAt(this.completedAt)
          .failedAt(this.failedAt)
          .compensatedAt(this.compensatedAt)
          .currentStepIndex(this.currentStepIndex != null ? this.currentStepIndex : 0)
          .steps(null) // Will be loaded separately
          .version(this.version)
          .lastModifiedBy(this.lastModifiedBy)
          .domainEvents(new ArrayList<>())
          .build();
    } catch (Exception e) {
      throw new SagaPersistenceException("Failed to convert entity to domain", e);
    }
  }

  private static String convertSagaDataToJson(Map<String, Object> sagaData) {
    if (sagaData == null || sagaData.isEmpty()) {
      return null;
    }
    try {
      return OBJECT_MAPPER.writeValueAsString(sagaData);
    } catch (Exception e) {
      throw new SagaSerializationException("Failed to convert saga data to JSON", e);
    }
  }

  private static Map<String, Object> convertJsonToSagaData(String sagaDataJson) {
    if (sagaDataJson == null || sagaDataJson.trim().isEmpty()) {
      return null;
    }
    try {
      return OBJECT_MAPPER.readValue(sagaDataJson, Map.class);
    } catch (Exception e) {
      throw new SagaSerializationException("Failed to convert JSON to saga data", e);
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
