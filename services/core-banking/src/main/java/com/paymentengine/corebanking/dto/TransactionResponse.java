package com.paymentengine.corebanking.dto;

import com.paymentengine.corebanking.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for transaction information
 */
public class TransactionResponse {
    
    private UUID id;
    private String transactionReference;
    private String externalReference;
    private UUID fromAccountId;
    private UUID toAccountId;
    private UUID paymentTypeId;
    private BigDecimal amount;
    private String currencyCode;
    private BigDecimal feeAmount;
    private Transaction.TransactionStatus status;
    private Transaction.TransactionType transactionType;
    private String description;
    private Map<String, Object> metadata;
    private LocalDateTime initiatedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public TransactionResponse() {}
    
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
    
    public Transaction.TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(Transaction.TransactionStatus status) {
        this.status = status;
    }
    
    public Transaction.TransactionType getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(Transaction.TransactionType transactionType) {
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
    
    // Computed properties
    public BigDecimal getTotalAmount() {
        return amount != null && feeAmount != null ? amount.add(feeAmount) : amount;
    }
    
    public boolean isCompleted() {
        return status == Transaction.TransactionStatus.COMPLETED;
    }
    
    public boolean isPending() {
        return status == Transaction.TransactionStatus.PENDING;
    }
    
    public boolean isFailed() {
        return status == Transaction.TransactionStatus.FAILED;
    }
    
    @Override
    public String toString() {
        return "TransactionResponse{" +
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
}