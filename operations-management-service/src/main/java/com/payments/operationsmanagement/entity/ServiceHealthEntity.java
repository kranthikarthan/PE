package com.payments.operationsmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

/**
 * Service Health Entity
 * 
 * Represents the health status of a microservice in the payments engine.
 * Tracks service status, metrics, circuit breaker state, and pod information.
 */
@Entity
@Table(name = "service_health")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHealthEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name", nullable = false, unique = true)
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ServiceStatus status;

    @Column(name = "uptime_percentage")
    private Double uptimePercentage;

    @Column(name = "request_rate")
    private Double requestRate;

    @Column(name = "error_rate")
    private Double errorRate;

    @Column(name = "response_time_p50")
    private Long responseTimeP50;

    @Column(name = "response_time_p95")
    private Long responseTimeP95;

    @Column(name = "response_time_p99")
    private Long responseTimeP99;

    @Enumerated(EnumType.STRING)
    @Column(name = "circuit_breaker_state")
    private CircuitBreakerState circuitBreakerState;

    @Column(name = "circuit_breaker_failure_rate")
    private Double circuitBreakerFailureRate;

    @Column(name = "pod_count")
    private Integer podCount;

    @Column(name = "cpu_usage_percentage")
    private Double cpuUsagePercentage;

    @Column(name = "memory_usage_mb")
    private Long memoryUsageMb;

    @Column(name = "last_health_check")
    private Instant lastHealthCheck;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "serviceHealth", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PodInfoEntity> pods;

    public enum ServiceStatus {
        UP, DOWN, DEGRADED, UNKNOWN
    }

    public enum CircuitBreakerState {
        CLOSED, OPEN, HALF_OPEN, DISABLED
    }
}
