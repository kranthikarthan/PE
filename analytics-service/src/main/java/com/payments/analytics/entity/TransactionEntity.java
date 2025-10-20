package com.payments.analytics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Transaction Entity
 * 
 * Represents a transaction in the payments system.
 * Used for transaction search and reporting operations.
 */
@Entity
@Table(name = "transaction_entity", indexes = {
    @Index(name = "idx_transaction_tenant_business", columnList = "tenant_id, business_unit_id"),
    @Index(name = "idx_transaction_payment_id", columnList = "payment_id"),
    @Index(name = "idx_transaction_status", columnList = "status"),
    @Index(name = "idx_transaction_created_at", columnList = "created_at"),
    @Index(name = "idx_transaction_clearing_system", columnList = "clearing_system"),
    @Index(name = "idx_transaction_channel", columnList = "channel"),
    @Index(name = "idx_transaction_currency", columnList = "currency")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

    @Id
    @Column(name = "transaction_id", length = 255, nullable = false)
    private String transactionId;

    @Column(name = "payment_id", length = 255, nullable = false)
    private String paymentId;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "clearing_system", length = 100)
    private String clearingSystem;

    @Column(name = "channel", length = 100)
    private String channel;

    @Column(name = "tenant_id", length = 255, nullable = false)
    private String tenantId;

    @Column(name = "business_unit_id", length = 255, nullable = false)
    private String businessUnitId;

    @Column(name = "correlation_id", length = 255)
    private String correlationId;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "processing_time_seconds")
    private Long processingTimeSeconds;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "external_reference", length = 255)
    private String externalReference;

    @Column(name = "internal_reference", length = 255)
    private String internalReference;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "initiated_by", length = 255)
    private String initiatedBy;

    @Column(name = "approved_by", length = 255)
    private String approvedBy;

    @Column(name = "rejected_by", length = 255)
    private String rejectedBy;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "subcategory", length = 100)
    private String subcategory;

    @Column(name = "tags", length = 1000)
    private String tags;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "is_urgent")
    private Boolean isUrgent;

    @Column(name = "is_high_value")
    private Boolean isHighValue;

    @Column(name = "is_suspicious")
    private Boolean isSuspicious;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "compliance_status", length = 50)
    private String complianceStatus;

    @Column(name = "regulatory_reporting_required")
    private Boolean regulatoryReportingRequired;

    @Column(name = "regulatory_reporting_status", length = 50)
    private String regulatoryReportingStatus;

    @Column(name = "regulatory_reporting_date")
    private LocalDateTime regulatoryReportingDate;

    @Column(name = "audit_trail", columnDefinition = "jsonb")
    private Map<String, Object> auditTrail;

    @Column(name = "version")
    @Version
    private Long version;

    // Constructors
    public TransactionEntity(String transactionId, String paymentId, String status, 
                           BigDecimal amount, String currency, String tenantId, String businessUnitId) {
        this.transactionId = transactionId;
        this.paymentId = paymentId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.tenantId = tenantId;
        this.businessUnitId = businessUnitId;
    }

    // Helper methods
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isProcessing() {
        return "PROCESSING".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean isHighValue() {
        return isHighValue != null && isHighValue;
    }

    public boolean isUrgent() {
        return isUrgent != null && isUrgent;
    }

    public boolean isSuspicious() {
        return isSuspicious != null && isSuspicious;
    }

    public boolean requiresRegulatoryReporting() {
        return regulatoryReportingRequired != null && regulatoryReportingRequired;
    }

    public void markAsCompleted() {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
        if (this.createdAt != null) {
            this.processingTimeSeconds = java.time.Duration.between(this.createdAt, this.completedAt).getSeconds();
        }
    }

    public void markAsFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsCancelled(String reason) {
        this.status = "CANCELLED";
        this.errorMessage = reason;
        this.completedAt = LocalDateTime.now();
    }

    public void incrementRetryCount() {
        this.retryCount = (this.retryCount != null ? this.retryCount : 0) + 1;
    }

    public void setPriority(String priority) {
        this.priority = priority;
        this.isUrgent = "HIGH".equals(priority) || "URGENT".equals(priority);
    }

    public void setHighValue(BigDecimal threshold) {
        this.isHighValue = this.amount != null && this.amount.compareTo(threshold) > 0;
    }

    public void setRiskScore(Integer score) {
        this.riskScore = score;
        this.isSuspicious = score != null && score > 70;
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

    public void updateAuditTrail(String action, String userId, String details) {
        if (this.auditTrail == null) {
            this.auditTrail = new java.util.HashMap<>();
        }
        
        Map<String, Object> auditEntry = new java.util.HashMap<>();
        auditEntry.put("action", action);
        auditEntry.put("userId", userId);
        auditEntry.put("timestamp", LocalDateTime.now().toString());
        auditEntry.put("details", details);
        
        this.auditTrail.put(action + "_" + System.currentTimeMillis(), auditEntry);
    }
}
