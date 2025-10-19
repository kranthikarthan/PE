package com.payments.analytics.domain;

import com.payments.domain.shared.TenantContext;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Analytics Event entity for tracking business events.
 *
 * <p>This entity represents a business event that occurred in the Payment Engine
 * and needs to be tracked for analytics and reporting purposes.
 *
 * @since PE-415
 */
@Entity
@Table(name = "analytics_events", indexes = {
    @Index(name = "idx_analytics_events_event_type", columnList = "eventType"),
    @Index(name = "idx_analytics_events_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_analytics_events_business_unit_id", columnList = "businessUnitId"),
    @Index(name = "idx_analytics_events_timestamp", columnList = "timestamp"),
    @Index(name = "idx_analytics_events_entity_id", columnList = "entityId"),
    @Index(name = "idx_analytics_events_entity_type", columnList = "entityType")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "event_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "entity_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Column(name = "description")
    private String description;

    @ElementCollection
    @CollectionTable(name = "analytics_event_attributes", joinColumns = @JoinColumn(name = "event_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    private Map<String, String> attributes;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "business_unit_id")
    private UUID businessUnitId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "correlation_id")
    private String correlationId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void setTenantAndBusinessUnit() {
        if (TenantContext.getCurrentTenantId() != null) {
            this.tenantId = TenantContext.getCurrentTenantId();
        }
        if (TenantContext.getCurrentBusinessUnitId() != null) {
            this.businessUnitId = TenantContext.getCurrentBusinessUnitId();
        }
    }

    /**
     * Event types for analytics tracking.
     */
    public enum EventType {
        PAYMENT_CREATED,
        PAYMENT_VALIDATED,
        PAYMENT_SUBMITTED_TO_CLEARING,
        PAYMENT_CLEARED,
        PAYMENT_COMPLETED,
        PAYMENT_FAILED,
        PAYMENT_CANCELLED,
        SETTLEMENT_WORKFLOW_STARTED,
        SETTLEMENT_WORKFLOW_COMPLETED,
        SETTLEMENT_WORKFLOW_FAILED,
        RECONCILIATION_RUN_STARTED,
        RECONCILIATION_RUN_COMPLETED,
        RECONCILIATION_EXCEPTION_CREATED,
        RECONCILIATION_EXCEPTION_RESOLVED,
        BATCH_JOB_STARTED,
        BATCH_JOB_COMPLETED,
        BATCH_JOB_FAILED,
        ALERT_CREATED,
        ALERT_ACKNOWLEDGED,
        ALERT_RESOLVED,
        USER_LOGIN,
        USER_LOGOUT,
        API_CALL,
        ERROR_OCCURRED,
        PERFORMANCE_METRIC
    }

    /**
     * Entity types for analytics tracking.
     */
    public enum EntityType {
        PAYMENT,
        SETTLEMENT_WORKFLOW,
        RECONCILIATION_RUN,
        RECONCILIATION_EXCEPTION,
        BATCH_JOB,
        ALERT,
        USER,
        API,
        SYSTEM
    }

    /**
     * Event status for analytics tracking.
     */
    public enum EventStatus {
        SUCCESS,
        FAILURE,
        PENDING,
        CANCELLED
    }
}
