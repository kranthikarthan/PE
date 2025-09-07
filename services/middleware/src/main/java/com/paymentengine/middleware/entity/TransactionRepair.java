package com.paymentengine.middleware.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Transaction Repair Entity
 * 
 * Tracks transactions that require repair due to failures, timeouts,
 * or other issues during debit/credit API orchestration.
 */
@Entity
@Table(name = "transaction_repairs", schema = "payment_engine")
public class TransactionRepair {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "transaction_reference", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String transactionReference;
    
    @Column(name = "parent_transaction_id", length = 100)
    @Size(max = 100)
    private String parentTransactionId;
    
    @Column(name = "tenant_id", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String tenantId;
    
    @Column(name = "repair_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull
    private RepairType repairType;
    
    @Column(name = "repair_status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull
    private RepairStatus repairStatus = RepairStatus.PENDING;
    
    @Column(name = "failure_reason", length = 500)
    @Size(max = 500)
    private String failureReason;
    
    @Column(name = "error_code", length = 50)
    @Size(max = 50)
    private String errorCode;
    
    @Column(name = "error_message", length = 1000)
    @Size(max = 1000)
    private String errorMessage;
    
    @Column(name = "from_account_number", length = 50)
    @Size(max = 50)
    private String fromAccountNumber;
    
    @Column(name = "to_account_number", length = 50)
    @Size(max = 50)
    private String toAccountNumber;
    
    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", length = 3)
    @Size(max = 3)
    private String currency;
    
    @Column(name = "payment_type", length = 50)
    @Size(max = 50)
    private String paymentType;
    
    @Column(name = "debit_status", length = 50)
    @Enumerated(EnumType.STRING)
    private DebitCreditStatus debitStatus;
    
    @Column(name = "credit_status", length = 50)
    @Enumerated(EnumType.STRING)
    private DebitCreditStatus creditStatus;
    
    @Column(name = "debit_reference", length = 100)
    @Size(max = 100)
    private String debitReference;
    
    @Column(name = "credit_reference", length = 100)
    @Size(max = 100)
    private String creditReference;
    
    @Column(name = "debit_response", columnDefinition = "jsonb")
    private Map<String, Object> debitResponse;
    
    @Column(name = "credit_response", columnDefinition = "jsonb")
    private Map<String, Object> creditResponse;
    
    @Column(name = "original_request", columnDefinition = "jsonb")
    private Map<String, Object> originalRequest;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "max_retries")
    private Integer maxRetries = 3;
    
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;
    
    @Column(name = "timeout_at")
    private LocalDateTime timeoutAt;
    
    @Column(name = "priority")
    private Integer priority = 1;
    
    @Column(name = "assigned_to", length = 100)
    @Size(max = 100)
    private String assignedTo;
    
    @Column(name = "corrective_action", length = 50)
    @Enumerated(EnumType.STRING)
    private CorrectiveAction correctiveAction;
    
    @Column(name = "corrective_action_details", columnDefinition = "jsonb")
    private Map<String, Object> correctiveActionDetails;
    
    @Column(name = "resolution_notes", length = 2000)
    @Size(max = 2000)
    private String resolutionNotes;
    
    @Column(name = "resolved_by", length = 100)
    @Size(max = 100)
    private String resolvedBy;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    @Size(max = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    @Size(max = 100)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public TransactionRepair() {}
    
    public TransactionRepair(String transactionReference, String tenantId, RepairType repairType) {
        this.transactionReference = transactionReference;
        this.tenantId = tenantId;
        this.repairType = repairType;
    }
    
    // Business methods
    public void markForRetry(int delayMinutes) {
        this.retryCount++;
        this.nextRetryAt = LocalDateTime.now().plusMinutes(delayMinutes);
        this.repairStatus = RepairStatus.PENDING;
    }
    
    public void markAsResolved(String resolvedBy, String resolutionNotes) {
        this.repairStatus = RepairStatus.RESOLVED;
        this.resolvedBy = resolvedBy;
        this.resolutionNotes = resolutionNotes;
        this.resolvedAt = LocalDateTime.now();
    }
    
    public void assignTo(String assignedTo) {
        this.assignedTo = assignedTo;
        this.repairStatus = RepairStatus.ASSIGNED;
    }
    
    public boolean canRetry() {
        return retryCount < maxRetries && 
               (nextRetryAt == null || LocalDateTime.now().isAfter(nextRetryAt));
    }
    
    public boolean isTimedOut() {
        return timeoutAt != null && LocalDateTime.now().isAfter(timeoutAt);
    }
    
    public void setDebitSuccess(String debitReference, Map<String, Object> debitResponse) {
        this.debitStatus = DebitCreditStatus.SUCCESS;
        this.debitReference = debitReference;
        this.debitResponse = debitResponse;
    }
    
    public void setDebitFailure(String errorCode, String errorMessage, Map<String, Object> debitResponse) {
        this.debitStatus = DebitCreditStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.debitResponse = debitResponse;
    }
    
    public void setCreditSuccess(String creditReference, Map<String, Object> creditResponse) {
        this.creditStatus = DebitCreditStatus.SUCCESS;
        this.creditReference = creditReference;
        this.creditResponse = creditResponse;
    }
    
    public void setCreditFailure(String errorCode, String errorMessage, Map<String, Object> creditResponse) {
        this.creditStatus = DebitCreditStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.creditResponse = creditResponse;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getTransactionReference() {
        return transactionReference;
    }
    
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
    
    public String getParentTransactionId() {
        return parentTransactionId;
    }
    
    public void setParentTransactionId(String parentTransactionId) {
        this.parentTransactionId = parentTransactionId;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public RepairType getRepairType() {
        return repairType;
    }
    
    public void setRepairType(RepairType repairType) {
        this.repairType = repairType;
    }
    
    public RepairStatus getRepairStatus() {
        return repairStatus;
    }
    
    public void setRepairStatus(RepairStatus repairStatus) {
        this.repairStatus = repairStatus;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getFromAccountNumber() {
        return fromAccountNumber;
    }
    
    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }
    
    public String getToAccountNumber() {
        return toAccountNumber;
    }
    
    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    public DebitCreditStatus getDebitStatus() {
        return debitStatus;
    }
    
    public void setDebitStatus(DebitCreditStatus debitStatus) {
        this.debitStatus = debitStatus;
    }
    
    public DebitCreditStatus getCreditStatus() {
        return creditStatus;
    }
    
    public void setCreditStatus(DebitCreditStatus creditStatus) {
        this.creditStatus = creditStatus;
    }
    
    public String getDebitReference() {
        return debitReference;
    }
    
    public void setDebitReference(String debitReference) {
        this.debitReference = debitReference;
    }
    
    public String getCreditReference() {
        return creditReference;
    }
    
    public void setCreditReference(String creditReference) {
        this.creditReference = creditReference;
    }
    
    public Map<String, Object> getDebitResponse() {
        return debitResponse;
    }
    
    public void setDebitResponse(Map<String, Object> debitResponse) {
        this.debitResponse = debitResponse;
    }
    
    public Map<String, Object> getCreditResponse() {
        return creditResponse;
    }
    
    public void setCreditResponse(Map<String, Object> creditResponse) {
        this.creditResponse = creditResponse;
    }
    
    public Map<String, Object> getOriginalRequest() {
        return originalRequest;
    }
    
    public void setOriginalRequest(Map<String, Object> originalRequest) {
        this.originalRequest = originalRequest;
    }
    
    public Integer getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
    
    public Integer getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }
    
    public void setNextRetryAt(LocalDateTime nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }
    
    public LocalDateTime getTimeoutAt() {
        return timeoutAt;
    }
    
    public void setTimeoutAt(LocalDateTime timeoutAt) {
        this.timeoutAt = timeoutAt;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public String getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public CorrectiveAction getCorrectiveAction() {
        return correctiveAction;
    }
    
    public void setCorrectiveAction(CorrectiveAction correctiveAction) {
        this.correctiveAction = correctiveAction;
    }
    
    public Map<String, Object> getCorrectiveActionDetails() {
        return correctiveActionDetails;
    }
    
    public void setCorrectiveActionDetails(Map<String, Object> correctiveActionDetails) {
        this.correctiveActionDetails = correctiveActionDetails;
    }
    
    public String getResolutionNotes() {
        return resolutionNotes;
    }
    
    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
    
    public String getResolvedBy() {
        return resolvedBy;
    }
    
    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    @Override
    public String toString() {
        return "TransactionRepair{" +
                "id=" + id +
                ", transactionReference='" + transactionReference + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", repairType=" + repairType +
                ", repairStatus=" + repairStatus +
                ", failureReason='" + failureReason + '\'' +
                ", retryCount=" + retryCount +
                ", priority=" + priority +
                '}';
    }
    
    /**
     * Repair Type Enumeration
     */
    public enum RepairType {
        DEBIT_FAILED,
        CREDIT_FAILED,
        DEBIT_TIMEOUT,
        CREDIT_TIMEOUT,
        DEBIT_CREDIT_MISMATCH,
        PARTIAL_SUCCESS,
        SYSTEM_ERROR,
        MANUAL_REVIEW
    }
    
    /**
     * Repair Status Enumeration
     */
    public enum RepairStatus {
        PENDING,
        ASSIGNED,
        IN_PROGRESS,
        RESOLVED,
        FAILED,
        CANCELLED
    }
    
    /**
     * Debit/Credit Status Enumeration
     */
    public enum DebitCreditStatus {
        PENDING,
        SUCCESS,
        FAILED,
        TIMEOUT,
        CANCELLED
    }
    
    /**
     * Corrective Action Enumeration
     */
    public enum CorrectiveAction {
        RETRY_DEBIT,
        RETRY_CREDIT,
        RETRY_BOTH,
        REVERSE_DEBIT,
        REVERSE_CREDIT,
        REVERSE_BOTH,
        MANUAL_CREDIT,
        MANUAL_DEBIT,
        MANUAL_BOTH,
        CANCEL_TRANSACTION,
        ESCALATE,
        NO_ACTION
    }
}