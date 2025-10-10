package com.paymentengine.paymentprocessing.dto.corebanking;

import java.time.LocalDateTime;

/**
 * ISO 20022 Payment Result DTO
 */
public class Iso20022PaymentResult {
    
    private String transactionReference;
    private String status;
    private String message;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime processedAt;
    private String clearingSystemReference;
    
    // Constructors
    public Iso20022PaymentResult() {}
    
    public Iso20022PaymentResult(String transactionReference, String status, String message) {
        this.transactionReference = transactionReference;
        this.status = status;
        this.message = message;
        this.processedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getTransactionReference() {
        return transactionReference;
    }
    
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
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
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public String getClearingSystemReference() {
        return clearingSystemReference;
    }
    
    public void setClearingSystemReference(String clearingSystemReference) {
        this.clearingSystemReference = clearingSystemReference;
    }
    
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }
    
    @Override
    public String toString() {
        return "Iso20022PaymentResult{" +
                "transactionReference='" + transactionReference + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", processedAt=" + processedAt +
                '}';
    }
}