package com.payments.saga.entity;

import com.payments.saga.domain.SagaEvent;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/** JPA entity for SagaEvent persistence */
@Entity
@Table(name = "saga_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaEventEntity {
  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "saga_id", nullable = false)
  private String sagaId;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(name = "event_data", columnDefinition = "jsonb")
  private String eventDataJson;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "business_unit_id", nullable = false)
  private String businessUnitId;

  @Column(name = "correlation_id", nullable = false)
  private String correlationId;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @CreationTimestamp
  @Column(name = "created_at")
  private Instant createdAt;

  public static SagaEventEntity fromDomain(SagaEvent event) {
    return SagaEventEntity.builder()
        .id(event.getEventId())
        .sagaId(event.getSagaId().getValue())
        .eventType(event.getEventType())
        .eventDataJson(convertEventDataToJson(event.getEventData()))
        .tenantId(event.getTenantContext().getTenantId())
        .businessUnitId(event.getTenantContext().getBusinessUnitId())
        .correlationId(event.getCorrelationId())
        .occurredAt(event.getOccurredAt())
        .build();
  }

  private static String convertEventDataToJson(Map<String, Object> eventData) {
    if (eventData == null || eventData.isEmpty()) {
      return null;
    }
    try {
      com.fasterxml.jackson.databind.ObjectMapper mapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      return mapper.writeValueAsString(eventData);
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert event data to JSON", e);
    }
  }
}
