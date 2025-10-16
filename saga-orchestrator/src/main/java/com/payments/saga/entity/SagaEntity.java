package com.payments.saga.entity;

import com.payments.domain.shared.TenantContext;
import com.payments.saga.domain.Saga;
import com.payments.saga.domain.SagaId;
import com.payments.saga.domain.SagaStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for Saga persistence
 */
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

    public static SagaEntity fromDomain(Saga saga) {
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
                .build();
    }

    public Saga toDomain() {
        return Saga.builder()
                .id(SagaId.of(this.id))
                .sagaName(this.sagaName)
                .status(this.status)
                .tenantContext(TenantContext.of(this.tenantId, "Tenant", this.businessUnitId, "Business Unit"))
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
                .build();
    }

    private static String convertSagaDataToJson(Map<String, Object> sagaData) {
        if (sagaData == null || sagaData.isEmpty()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(sagaData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert saga data to JSON", e);
        }
    }

    private static Map<String, Object> convertJsonToSagaData(String sagaDataJson) {
        if (sagaDataJson == null || sagaDataJson.trim().isEmpty()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(sagaDataJson, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to saga data", e);
        }
    }
}






