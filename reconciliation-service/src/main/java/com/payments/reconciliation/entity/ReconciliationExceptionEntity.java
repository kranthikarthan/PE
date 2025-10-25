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
 * Reconciliation Exception Entity
 * 
 * Represents a reconciliation exception in the payments system.
 * Used for tracking and managing reconciliation exceptions.
 */
@Entity
@Table(name = "reconciliation_exception_entity", indexes = {
    @Index(name = "idx_reconciliation_exception_tenant_business", columnList = "tenant_id, business_unit_id"),
    @Index(name = "idx_reconciliation_exception_run_id", columnList = "run_id"),
    @Index(name = "idx_reconciliation_exception_status", columnList = "status"),
    @Index(name = "idx_reconciliation_exception_severity", columnList = "severity"),
    @Index(name = "idx_reconciliation_exception_type", columnList = "type"),
    @Index(name = "idx_reconciliation_exception_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationExceptionEntity {

    @Id
    @Column(name = "exception_id", length = 255, nullable = false)
    private String exceptionId;

    @Column(name = "run_id", length = 255, nullable = false)
    private String runId;

    @Column(name = "type", length = 100, nullable = false)
    private String type;

    @Column(name = "severity", length = 20, nullable = false)
    private String severity;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "description", length = 1000, nullable = false)
    private String description;

    @Column(name = "details", columnDefinition = "text")
    private String details;

    @Column(name = "tenant_id", length = 255, nullable = false)
    private String tenantId;

    @Column(name = "business_unit_id", length = 255, nullable = false)
    private String businessUnitId;

    @Column(name = "correlation_id", length = 255)
    private String correlationId;

    @Column(name = "transaction_id", length = 255)
    private String transactionId;

    @Column(name = "payment_id", length = 255)
    private String paymentId;

    @Column(name = "clearing_system", length = 100)
    private String clearingSystem;

    @Column(name = "resolution", length = 1000)
    private String resolution;

    @Column(name = "resolution_notes", length = 2000)
    private String resolutionNotes;

    @Column(name = "resolved_by", length = 255)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "subcategory", length = 100)
    private String subcategory;

    @Column(name = "tags", length = 1000)
    private String tags;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "is_urgent")
    private Boolean isUrgent;

    @Column(name = "is_high_priority")
    private Boolean isHighPriority;

    @Column(name = "is_escalated")
    private Boolean isEscalated;

    @Column(name = "escalation_level")
    private Integer escalationLevel;

    @Column(name = "escalated_by", length = 255)
    private String escalatedBy;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "escalation_reason", length = 500)
    private String escalationReason;

    @Column(name = "assigned_to", length = 255)
    private String assignedTo;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "sla_breach")
    private Boolean slaBreach;

    @Column(name = "sla_breach_reason", length = 500)
    private String slaBreachReason;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "max_retries")
    private Integer maxRetries;

    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "version")
    @Version
    private Long version;

    // Constructors
    public ReconciliationExceptionEntity(String exceptionId, String runId, String type, 
                                       String severity, String status, String description,
                                       String tenantId, String businessUnitId) {
        this.exceptionId = exceptionId;
        this.runId = runId;
        this.type = type;
        this.severity = severity;
        this.status = status;
        this.description = description;
        this.tenantId = tenantId;
        this.businessUnitId = businessUnitId;
    }

    // Helper methods
    public boolean isOpen() {
        return "OPEN".equals(status);
    }

    public boolean isResolved() {
        return "RESOLVED".equals(status);
    }

    public boolean isClosed() {
        return "CLOSED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean isHighSeverity() {
        return "HIGH".equals(severity) || "CRITICAL".equals(severity);
    }

    public boolean isMediumSeverity() {
        return "MEDIUM".equals(severity);
    }

    public boolean isLowSeverity() {
        return "LOW".equals(severity);
    }

    public boolean isUrgent() {
        return isUrgent != null && isUrgent;
    }

    public boolean isHighPriority() {
        return isHighPriority != null && isHighPriority;
    }

    public boolean isEscalated() {
        return isEscalated != null && isEscalated;
    }

    public boolean isAssigned() {
        return assignedTo != null && !assignedTo.trim().isEmpty();
    }

    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate);
    }

    public boolean isSlaBreached() {
        return slaBreach != null && slaBreach;
    }

    public boolean canRetry() {
        return retryCount == null || maxRetries == null || retryCount < maxRetries;
    }

    public void markAsResolved(String resolution, String resolutionNotes, String resolvedBy) {
        this.status = "RESOLVED";
        this.resolution = resolution;
        this.resolutionNotes = resolutionNotes;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = LocalDateTime.now();
    }

    public void markAsClosed(String closedBy) {
        this.status = "CLOSED";
        this.resolvedBy = closedBy;
        this.resolvedAt = LocalDateTime.now();
    }

    public void markAsCancelled(String reason, String cancelledBy) {
        this.status = "CANCELLED";
        this.resolution = reason;
        this.resolvedBy = cancelledBy;
        this.resolvedAt = LocalDateTime.now();
    }

    public void escalate(String reason, String escalatedBy, Integer escalationLevel) {
        this.isEscalated = true;
        this.escalationLevel = escalationLevel;
        this.escalatedBy = escalatedBy;
        this.escalatedAt = LocalDateTime.now();
        this.escalationReason = reason;
    }

    public void assign(String assignedTo) {
        this.assignedTo = assignedTo;
        this.assignedAt = LocalDateTime.now();
    }

    public void setPriority(String priority) {
        this.priority = priority;
        this.isUrgent = "URGENT".equals(priority) || "CRITICAL".equals(priority);
        this.isHighPriority = "HIGH".equals(priority) || "URGENT".equals(priority) || "CRITICAL".equals(priority);
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
        // Check if already overdue
        if (dueDate != null && LocalDateTime.now().isAfter(dueDate)) {
            this.slaBreach = true;
            this.slaBreachReason = "Due date has passed";
        }
    }

    public void incrementRetryCount() {
        this.retryCount = (this.retryCount != null ? this.retryCount : 0) + 1;
        this.lastRetryAt = LocalDateTime.now();
    }

    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = tag;
        } else {
            this.tags = this.tags + "," + tag;
        }
    }

    public void addNote(String note) {
        if (this.notes == null) {
            this.notes = note;
        } else {
            this.notes = this.notes + "\n" + note;
        }
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

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
