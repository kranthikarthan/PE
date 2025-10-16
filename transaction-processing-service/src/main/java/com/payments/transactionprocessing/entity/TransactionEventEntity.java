package com.payments.transactionprocessing.entity;

import com.payments.domain.transaction.TransactionEvent;
import com.payments.domain.transaction.TransactionEventId;
import com.payments.domain.transaction.TransactionId;
import com.payments.domain.shared.TenantContext;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "transaction_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEventEntity {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "event_id"))
    private TransactionEventId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id")
    private TransactionEntity transaction;

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

    // Convert from domain model
    public static TransactionEventEntity fromDomain(TransactionEvent event) {
        TransactionEventEntity entity = new TransactionEventEntity();
        entity.setId(event.getId());
        entity.setTenantContext(event.getTenantContext());
        entity.setEventType(event.getEventType());
        entity.setEventData(event.getEventData());
        entity.setOccurredAt(event.getOccurredAt());
        entity.setEventSequence(event.getEventSequence());
        entity.setCorrelationId(event.getCorrelationId());
        entity.setCausationId(event.getCausationId());
        return entity;
    }

    // Convert to domain model
    public TransactionEvent toDomain() {
        return new TransactionEvent(
            this.id,
            this.transaction.getId(),
            this.tenantContext,
            this.eventSequence,
            this.eventType,
            this.eventData,
            this.correlationId,
            this.causationId,
            this.occurredAt
        );
    }
}






