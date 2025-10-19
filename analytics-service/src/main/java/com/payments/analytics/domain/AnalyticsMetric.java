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
import java.util.UUID;

/**
 * Analytics Metric entity for tracking performance and business metrics.
 *
 * <p>This entity represents a metric that was calculated or collected
 * for analytics and reporting purposes.
 *
 * @since PE-415
 */
@Entity
@Table(name = "analytics_metrics", indexes = {
    @Index(name = "idx_analytics_metrics_metric_name", columnList = "metricName"),
    @Index(name = "idx_analytics_metrics_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_analytics_metrics_business_unit_id", columnList = "businessUnitId"),
    @Index(name = "idx_analytics_metrics_timestamp", columnList = "timestamp"),
    @Index(name = "idx_analytics_metrics_category", columnList = "category"),
    @Index(name = "idx_analytics_metrics_entity_id", columnList = "entityId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "metric_name", nullable = false)
    private String metricName;

    @Column(name = "metric_value", nullable = false)
    private BigDecimal metricValue;

    @Column(name = "metric_unit")
    private String metricUnit;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private MetricCategory category;

    @Column(name = "subcategory")
    private String subcategory;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "entity_type")
    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    @Column(name = "aggregation_period")
    @Enumerated(EnumType.STRING)
    private AggregationPeriod aggregationPeriod;

    @Column(name = "dimensions")
    private String dimensions; // JSON string for additional dimensions

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
     * Metric categories for analytics tracking.
     */
    public enum MetricCategory {
        PERFORMANCE,
        BUSINESS,
        TECHNICAL,
        SECURITY,
        COMPLIANCE,
        USER_EXPERIENCE,
        FINANCIAL,
        OPERATIONAL
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
        SYSTEM,
        TENANT,
        BUSINESS_UNIT
    }

    /**
     * Aggregation periods for metrics.
     */
    public enum AggregationPeriod {
        REAL_TIME,
        MINUTE,
        HOUR,
        DAY,
        WEEK,
        MONTH,
        QUARTER,
        YEAR
    }
}
