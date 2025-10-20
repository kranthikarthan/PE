package com.payments.reconciliation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Reconciliation Run Entity
 * 
 * Represents a reconciliation run in the payments system.
 * Used for reconciliation management and monitoring operations.
 */
@Entity
@Table(name = "reconciliation_run_entity", indexes = {
    @Index(name = "idx_reconciliation_run_tenant_business", columnList = "tenant_id, business_unit_id"),
    @Index(name = "idx_reconciliation_run_status", columnList = "status"),
    @Index(name = "idx_reconciliation_run_clearing_system", columnList = "clearing_system"),
    @Index(name = "idx_reconciliation_run_started_at", columnList = "started_at"),
    @Index(name = "idx_reconciliation_run_completed_at", columnList = "completed_at"),
    @Index(name = "idx_reconciliation_run_started_by", columnList = "started_by")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationRunEntity {

    @Id
    @Column(name = "run_id", length = 255, nullable = false)
    private String runId;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "clearing_system", length = 100, nullable = false)
    private String clearingSystem;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "tenant_id", length = 255, nullable = false)
    private String tenantId;

    @Column(name = "business_unit_id", length = 255, nullable = false)
    private String businessUnitId;

    @Column(name = "correlation_id", length = 255)
    private String correlationId;

    @Column(name = "started_by", length = 255)
    private String startedBy;

    @Column(name = "stopped_by", length = 255)
    private String stoppedBy;

    @Column(name = "stop_reason", length = 500)
    private String stopReason;

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "matched_records")
    private Integer matchedRecords;

    @Column(name = "unmatched_records")
    private Integer unmatchedRecords;

    @Column(name = "processing_time_seconds")
    private Long processingTimeSeconds;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "version")
    @Version
    private Long version;

    // Constructors
    public ReconciliationRunEntity(String runId, String status, String clearingSystem, 
                                 String tenantId, String businessUnitId) {
        this.runId = runId;
        this.status = status;
        this.clearingSystem = clearingSystem;
        this.tenantId = tenantId;
        this.businessUnitId = businessUnitId;
    }

    // Helper methods
    public boolean isRunning() {
        return "RUNNING".equals(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    public boolean isStopped() {
        return "STOPPED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public void markAsCompleted() {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.processingTimeSeconds = java.time.Duration.between(this.startedAt, this.completedAt).getSeconds();
        }
    }

    public void markAsFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.processingTimeSeconds = java.time.Duration.between(this.startedAt, this.completedAt).getSeconds();
        }
    }

    public void markAsStopped(String reason, String stoppedBy) {
        this.status = "STOPPED";
        this.stopReason = reason;
        this.stoppedBy = stoppedBy;
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.processingTimeSeconds = java.time.Duration.between(this.startedAt, this.completedAt).getSeconds();
        }
    }

    public void markAsCancelled(String reason, String cancelledBy) {
        this.status = "CANCELLED";
        this.stopReason = reason;
        this.stoppedBy = cancelledBy;
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.processingTimeSeconds = java.time.Duration.between(this.startedAt, this.completedAt).getSeconds();
        }
    }

    public void updateProgress(int totalRecords, int matchedRecords, int unmatchedRecords) {
        this.totalRecords = totalRecords;
        this.matchedRecords = matchedRecords;
        this.unmatchedRecords = unmatchedRecords;
    }

    public double getMatchRate() {
        if (totalRecords == null || totalRecords == 0) {
            return 0.0;
        }
        return (double) (matchedRecords != null ? matchedRecords : 0) / totalRecords * 100.0;
    }

    public double getUnmatchRate() {
        if (totalRecords == null || totalRecords == 0) {
            return 0.0;
        }
        return (double) (unmatchedRecords != null ? unmatchedRecords : 0) / totalRecords * 100.0;
    }

    public boolean hasErrors() {
        return errorMessage != null && !errorMessage.trim().isEmpty();
    }

    public boolean isLongRunning(int thresholdMinutes) {
        if (startedAt == null) {
            return false;
        }
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(thresholdMinutes);
        return startedAt.isBefore(threshold);
    }

    public boolean isHighExceptionRate(double threshold) {
        if (totalRecords == null || totalRecords == 0) {
            return false;
        }
        double exceptionRate = (double) (unmatchedRecords != null ? unmatchedRecords : 0) / totalRecords;
        return exceptionRate > threshold;
    }

    public void addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = "{}";
        }
        // Simple JSON manipulation - in production, use a proper JSON library
        this.metadata = this.metadata.replace("}", ",\"" + key + "\":\"" + value + "\"}");
    }

    public void setClearingSystem(String clearingSystem) {
        this.clearingSystem = clearingSystem;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartedBy(String startedBy) {
        this.startedBy = startedBy;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
