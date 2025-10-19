package com.payments.analytics.domain;

import com.payments.domain.shared.TenantContext;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Report Execution entity for tracking report generation.
 *
 * <p>This entity represents an execution of a report template,
 * including the parameters, status, and results.
 *
 * @since PE-415
 */
@Entity
@Table(name = "report_executions", indexes = {
    @Index(name = "idx_report_executions_template_id", columnList = "templateId"),
    @Index(name = "idx_report_executions_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_report_executions_business_unit_id", columnList = "businessUnitId"),
    @Index(name = "idx_report_executions_status", columnList = "status"),
    @Index(name = "idx_report_executions_executed_at", columnList = "executedAt"),
    @Index(name = "idx_report_executions_executed_by", columnList = "executedBy")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "execution_id", nullable = false, unique = true)
    private String executionId;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters; // JSON string for execution parameters

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    @Column(name = "progress")
    private Integer progress; // 0-100

    @Column(name = "executed_at", nullable = false)
    private OffsetDateTime executedAt;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_format")
    @Enumerated(EnumType.STRING)
    private OutputFormat fileFormat;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @Column(name = "executed_by", nullable = false)
    private String executedBy;

    @Column(name = "execution_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExecutionType executionType;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "max_retries")
    private Integer maxRetries;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "business_unit_id")
    private UUID businessUnitId;

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
     * Execution status for report generation.
     */
    public enum ExecutionStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED,
        TIMEOUT
    }

    /**
     * Execution types for report generation.
     */
    public enum ExecutionType {
        MANUAL,
        SCHEDULED,
        API,
        WEBHOOK,
        SYSTEM
    }

    /**
     * Output formats for reports.
     */
    public enum OutputFormat {
        PDF,
        EXCEL,
        CSV,
        JSON,
        XML,
        HTML,
        PNG,
        JPEG
    }
}
