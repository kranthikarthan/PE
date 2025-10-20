package com.payments.metricsaggregation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Alert Event Entity
 * 
 * Records alert events triggered by alert rules.
 * Tracks alert lifecycle from trigger to resolution.
 */
@Entity
@Table(name = "alert_events", indexes = {
    @Index(name = "idx_alert_timestamp", columnList = "triggered_at"),
    @Index(name = "idx_alert_rule", columnList = "rule_id"),
    @Index(name = "idx_alert_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_id", nullable = false, unique = true)
    private String alertId;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "metric_name", nullable = false)
    private String metricName;

    @Column(name = "metric_value", nullable = false)
    private Double metricValue;

    @Column(name = "threshold_value", nullable = false)
    private Double thresholdValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private AlertRuleEntity.Severity severity;

    @Column(name = "message")
    private String message;

    @Column(name = "acknowledged_by")
    private String acknowledgedBy;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "resolved_by")
    private String resolvedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    public enum Status {
        TRIGGERED, ACKNOWLEDGED, RESOLVED, SUPPRESSED
    }
}
