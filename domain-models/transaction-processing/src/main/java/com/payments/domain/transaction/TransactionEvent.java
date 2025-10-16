package com.payments.domain.transaction;

import com.payments.domain.shared.TenantContext;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Transaction Event (Entity within Transaction Aggregate) */
@Entity
@Table(name = "transaction_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@AllArgsConstructor
public class TransactionEvent {

  @EmbeddedId
  @AttributeOverride(name = "value", column = @Column(name = "event_id"))
  private TransactionEventId id;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "transaction_id"))
  private TransactionId transactionId;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id")),
    @AttributeOverride(name = "businessUnitId", column = @Column(name = "business_unit_id"))
  })
  private TenantContext tenantContext;

  @Column(name = "event_type")
  private String eventType;

  @Column(name = "event_data", columnDefinition = "jsonb")
  private String eventData;

  @Column(name = "occurred_at")
  private Instant occurredAt;

  @Column(name = "event_sequence")
  private Long eventSequence;

  @Column(name = "correlation_id")
  private String correlationId;

  @Column(name = "causation_id")
  private String causationId;

  public TransactionEvent(
      TransactionEventId id,
      TransactionId transactionId,
      TenantContext tenantContext,
      Long eventSequence,
      String eventType,
      String eventData,
      String correlationId,
      String causationId,
      Instant occurredAt) {
    this.id = id;
    this.transactionId = transactionId;
    this.tenantContext = tenantContext;
    this.eventSequence = eventSequence;
    this.eventType = eventType;
    this.eventData = eventData;
    this.correlationId = correlationId;
    this.causationId = causationId;
    this.occurredAt = occurredAt;
  }
}
