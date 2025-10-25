package com.payments.metricsaggregation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Metrics Data Entity
 * 
 * Stores time-series metrics data for all services in the payments engine.
 * Optimized for time-series queries with TimescaleDB.
 */
@Entity
@Table(name = "metrics_data", indexes = {
    @Index(name = "idx_metrics_timestamp", columnList = "timestamp"),
    @Index(name = "idx_metrics_service", columnList = "service_name"),
    @Index(name = "idx_metrics_metric_type", columnList = "metric_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false)
    private MetricType metricType;

    @Column(name = "metric_name", nullable = false)
    private String metricName;

    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "unit")
    private String unit;

    @Column(name = "tags", columnDefinition = "jsonb")
    private String tags;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    public enum MetricType {
        COUNTER, GAUGE, HISTOGRAM, TIMER, CUSTOM
    }
}
