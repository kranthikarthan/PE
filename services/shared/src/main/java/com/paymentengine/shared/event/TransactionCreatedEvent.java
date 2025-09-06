package com.paymentengine.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Event published when a new transaction is created in the system.
 * Contains all necessary information for downstream processing.
 */
@JsonTypeName("TRANSACTION_CREATED")
public class TransactionCreatedEvent extends PaymentEvent {
    
    @NotNull
    private UUID transactionId;
    
    @NotNull
    private String transactionReference;
    
    private String externalReference;
    
    private UUID fromAccountId;
    
    private UUID toAccountId;
    
    @NotNull
    private UUID paymentTypeId;
    
    @NotNull
    @Positive
    private BigDecimal amount;
    
    @NotNull
    private String currencyCode;
    
    private BigDecimal feeAmount;
    
    @NotNull
    private String transactionType;
    
    private String description;
    
    private Map<String, Object> metadata;
    
    private String channel;
    
    private String ipAddress;
    
    private String deviceId;

    public TransactionCreatedEvent() {
        super("TRANSACTION_CREATED", "core-banking");
    }

    public TransactionCreatedEvent(UUID transactionId, String transactionReference, 
                                 UUID fromAccountId, UUID toAccountId, UUID paymentTypeId,
                                 BigDecimal amount, String currencyCode, String transactionType) {
        this();
        this.transactionId = transactionId;
        this.transactionReference = transactionReference;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.paymentTypeId = paymentTypeId;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.transactionType = transactionType;
        this.feeAmount = BigDecimal.ZERO;
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

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
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

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "TransactionCreatedEvent{" +
                "transactionId=" + transactionId +
                ", transactionReference='" + transactionReference + '\'' +
                ", externalReference='" + externalReference + '\'' +
                ", fromAccountId=" + fromAccountId +
                ", toAccountId=" + toAccountId +
                ", paymentTypeId=" + paymentTypeId +
                ", amount=" + amount +
                ", currencyCode='" + currencyCode + '\'' +
                ", feeAmount=" + feeAmount +
                ", transactionType='" + transactionType + '\'' +
                ", description='" + description + '\'' +
                ", metadata=" + metadata +
                ", channel='" + channel + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", deviceId='" + deviceId + '\'' +
                "} " + super.toString();
    }
}