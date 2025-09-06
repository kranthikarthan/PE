package com.paymentengine.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@JsonTypeName("TRANSACTION_FAILED")
public class TransactionFailedEvent extends PaymentEvent {
    
    @NotNull
    private UUID transactionId;
    
    @NotNull
    private String transactionReference;
    
    @NotNull
    private String failureReason;
    
    private String errorCode;
    
    private String errorMessage;
    
    @NotNull
    private Instant failedAt;
    
    private boolean isRetryable;
    
    private Integer retryCount;
    
    private String processorResponse;

    public TransactionFailedEvent() {
        super("TRANSACTION_FAILED", "core-banking");
    }

    public TransactionFailedEvent(UUID transactionId, String transactionReference, 
                                String failureReason, Instant failedAt) {
        this();
        this.transactionId = transactionId;
        this.transactionReference = transactionReference;
        this.failureReason = failureReason;
        this.failedAt = failedAt;
        this.isRetryable = false;
        this.retryCount = 0;
    }

    // Getters and Setters
    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
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

    public Instant getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }

    public boolean isRetryable() {
        return isRetryable;
    }

    public void setRetryable(boolean retryable) {
        isRetryable = retryable;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getProcessorResponse() {
        return processorResponse;
    }

    public void setProcessorResponse(String processorResponse) {
        this.processorResponse = processorResponse;
    }
}