package com.payments.operationsmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Pod Information Entity
 * 
 * Represents information about a Kubernetes pod for a specific service.
 * Tracks pod status, resource usage, and metadata.
 */
@Entity
@Table(name = "pod_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PodInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pod_name", nullable = false)
    private String podName;

    @Column(name = "namespace", nullable = false)
    private String namespace;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PodStatus status;

    @Column(name = "cpu_usage_percentage")
    private Double cpuUsagePercentage;

    @Column(name = "memory_usage_mb")
    private Long memoryUsageMb;

    @Column(name = "restart_count")
    private Integer restartCount;

    @Column(name = "age_seconds")
    private Long ageSeconds;

    @Column(name = "node_name")
    private String nodeName;

    @Column(name = "ip_address")
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_health_id")
    private ServiceHealthEntity serviceHealth;

    public enum PodStatus {
        RUNNING, PENDING, FAILED, SUCCEEDED, UNKNOWN
    }
}
