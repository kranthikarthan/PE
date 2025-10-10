package com.paymentengine.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonTypeName("TRANSACTION_COMPLETED")
public class TransactionCompletedEvent extends PaymentEvent {
    
    @NotNull
    private UUID transactionId;
    
    @NotNull
    private String transactionReference;
    
    @NotNull
    private BigDecimal amount;
    
    @NotNull
    private String currencyCode;
    
    private UUID fromAccountId;
    
    private UUID toAccountId;
    
    @NotNull
    private Instant completedAt;
    
    private String confirmationCode;
    
    private String processorReference;
    
    private Long processingTimeMs;

    public TransactionCompletedEvent() {
        super("TRANSACTION_COMPLETED", "core-banking");
    }

    public TransactionCompletedEvent(UUID transactionId, String transactionReference, 
                                   BigDecimal amount, String currencyCode, Instant completedAt) {
        this();
        this.transactionId = transactionId;
        this.transactionReference = transactionReference;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.completedAt = completedAt;
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

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public String getProcessorReference() {
        return processorReference;
    }

    public void setProcessorReference(String processorReference) {
        this.processorReference = processorReference;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}