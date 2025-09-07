package com.paymentengine.middleware.dto.corebanking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Transaction Result DTO
 */
public class TransactionResult {
    
    public enum Status {
        SUCCESS,
        FAILED,
        PENDING,
        CANCELLED,
        REJECTED
    }
    
    private String transactionReference;
    private String externalReference;
    private Status status;
    private String statusMessage;
    private BigDecimal amount;
    private String currency;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String paymentType;
    private LocalDateTime processedAt;
    private String errorCode;
    private String errorMessage;
    private Map<String, Object> additionalData;
    private String coreBankingReference;
    
    // Constructors
    public TransactionResult() {}
    
    public TransactionResult(String transactionReference, Status status, String statusMessage) {
        this.transactionReference = transactionReference;
        this.status = status;
        this.statusMessage = statusMessage;
    }
    
    // Getters and Setters
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
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
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
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
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
    
    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }
    
    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }
    
    public String getCoreBankingReference() {
        return coreBankingReference;
    }
    
    public void setCoreBankingReference(String coreBankingReference) {
        this.coreBankingReference = coreBankingReference;
    }
    
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
    
    public boolean isFailed() {
        return status == Status.FAILED || status == Status.REJECTED;
    }
    
    @Override
    public String toString() {
        return "TransactionResult{" +
                "transactionReference='" + transactionReference + '\'' +
                ", status=" + status +
                ", statusMessage='" + statusMessage + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", processedAt=" + processedAt +
                '}';
    }
}