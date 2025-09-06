package com.paymentengine.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JsonTypeName("TRANSACTION_UPDATED")
public class TransactionUpdatedEvent extends PaymentEvent {
    
    @NotNull
    private UUID transactionId;
    
    @NotNull
    private String transactionReference;
    
    @NotNull
    private String previousStatus;
    
    @NotNull
    private String newStatus;
    
    private String reason;
    
    private Instant processedAt;
    
    private Map<String, Object> additionalData;

    public TransactionUpdatedEvent() {
        super("TRANSACTION_UPDATED", "core-banking");
    }

    public TransactionUpdatedEvent(UUID transactionId, String transactionReference, 
                                 String previousStatus, String newStatus) {
        this();
        this.transactionId = transactionId;
        this.transactionReference = transactionReference;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
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

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }
}