package com.paymentengine.corebanking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Transaction entity representing payment transactions
 */
@Entity
@Table(name = "transactions", schema = "payment_engine")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "transaction_reference", unique = true, nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String transactionReference;
    
    @Column(name = "external_reference", length = 100)
    @Size(max = 100)
    private String externalReference;
    
    @Column(name = "from_account_id")
    private UUID fromAccountId;
    
    @Column(name = "to_account_id")
    private UUID toAccountId;
    
    @Column(name = "payment_type_id", nullable = false)
    @NotNull
    private UUID paymentTypeId;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    
    @Column(name = "currency_code", length = 3)
    @Size(max = 3)
    private String currencyCode = "USD";
    
    @Column(name = "fee_amount", precision = 15, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal feeAmount = BigDecimal.ZERO;
    
    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "transaction_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private TransactionType transactionType;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    @Convert(converter = MapToJsonConverter.class)
    private Map<String, Object> metadata = new HashMap<>();
    
    @Column(name = "initiated_at", nullable = false)
    private LocalDateTime initiatedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (initiatedAt == null) {
            initiatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Transaction() {}
    
    public Transaction(String transactionReference, UUID paymentTypeId, 
                      BigDecimal amount, TransactionType transactionType) {
        this.transactionReference = transactionReference;
        this.paymentTypeId = paymentTypeId;
        this.amount = amount;
        this.transactionType = transactionType;
    }
    
    // Business methods
    public void markAsProcessing() {
        if (status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction must be PENDING to mark as PROCESSING");
        }
        status = TransactionStatus.PROCESSING;
        processedAt = LocalDateTime.now();
    }
    
    public void markAsCompleted() {
        if (status != TransactionStatus.PROCESSING) {
            throw new IllegalStateException("Transaction must be PROCESSING to mark as COMPLETED");
        }
        status = TransactionStatus.COMPLETED;
        completedAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String reason) {
        if (status == TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot fail a completed transaction");
        }
        status = TransactionStatus.FAILED;
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put("failure_reason", reason);
        metadata.put("failed_at", LocalDateTime.now().toString());
    }
    
    public void cancel(String reason) {
        if (status == TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed transaction");
        }
        status = TransactionStatus.CANCELLED;
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put("cancellation_reason", reason);
        metadata.put("cancelled_at", LocalDateTime.now().toString());
    }
    
    public boolean isCompleted() {
        return status == TransactionStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }
    
    public boolean isCancelled() {
        return status == TransactionStatus.CANCELLED;
    }
    
    public boolean isPending() {
        return status == TransactionStatus.PENDING;
    }
    
    public boolean isProcessing() {
        return status == TransactionStatus.PROCESSING;
    }
    
    public boolean isDebitTransaction() {
        return transactionType == TransactionType.DEBIT || 
               transactionType == TransactionType.TRANSFER ||
               transactionType == TransactionType.PAYMENT;
    }
    
    public boolean isCreditTransaction() {
        return transactionType == TransactionType.CREDIT ||
               transactionType == TransactionType.REFUND;
    }
    
    public BigDecimal getTotalAmount() {
        return amount.add(feeAmount);
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
    
    public String getExternalReference() {
        return externalReference;
    }
    
    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }
    
    public UUID getFromAccountId() {
        return fromAccountId;
    }
    
    public void setFromAccountId(UUID fromAccountId) {
        this.fromAccountId = fromAccountId;
    }
    
    public UUID getToAccountId() {
        return toAccountId;
    }
    
    public void setToAccountId(UUID toAccountId) {
        this.toAccountId = toAccountId;
    }
    
    public UUID getPaymentTypeId() {
        return paymentTypeId;
    }
    
    public void setPaymentTypeId(UUID paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrencyCode() {
        return currencyCode;
    }
    
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
    
    public BigDecimal getFeeAmount() {
        return feeAmount;
    }
    
    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    public TransactionType getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public LocalDateTime getInitiatedAt() {
        return initiatedAt;
    }
    
    public void setInitiatedAt(LocalDateTime initiatedAt) {
        this.initiatedAt = initiatedAt;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", transactionReference='" + transactionReference + '\'' +
                ", externalReference='" + externalReference + '\'' +
                ", fromAccountId=" + fromAccountId +
                ", toAccountId=" + toAccountId +
                ", paymentTypeId=" + paymentTypeId +
                ", amount=" + amount +
                ", currencyCode='" + currencyCode + '\'' +
                ", feeAmount=" + feeAmount +
                ", status=" + status +
                ", transactionType=" + transactionType +
                ", description='" + description + '\'' +
                ", initiatedAt=" + initiatedAt +
                ", completedAt=" + completedAt +
                '}';
    }
    
    /**
     * Transaction status enumeration
     */
    public enum TransactionStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        REVERSED
    }
    
    /**
     * Transaction type enumeration
     */
    public enum TransactionType {
        DEBIT,
        CREDIT,
        TRANSFER,
        PAYMENT,
        REFUND
    }
}