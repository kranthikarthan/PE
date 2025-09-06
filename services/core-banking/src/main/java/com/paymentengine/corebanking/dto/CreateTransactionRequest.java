package com.paymentengine.corebanking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating a new transaction
 */
public class CreateTransactionRequest {
    
    private String externalReference;
    
    private UUID fromAccountId;
    
    private UUID toAccountId;
    
    @NotNull(message = "Payment type ID is required")
    private UUID paymentTypeId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @Size(max = 3, message = "Currency code must be 3 characters")
    private String currencyCode = "USD";
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private Map<String, Object> metadata;
    
    // Additional context fields
    private String channel;
    private String ipAddress;
    private String deviceId;
    
    // Constructors
    public CreateTransactionRequest() {}
    
    public CreateTransactionRequest(UUID paymentTypeId, BigDecimal amount) {
        this.paymentTypeId = paymentTypeId;
        this.amount = amount;
    }
    
    // Getters and Setters
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
        return "CreateTransactionRequest{" +
                "externalReference='" + externalReference + '\'' +
                ", fromAccountId=" + fromAccountId +
                ", toAccountId=" + toAccountId +
                ", paymentTypeId=" + paymentTypeId +
                ", amount=" + amount +
                ", currencyCode='" + currencyCode + '\'' +
                ", description='" + description + '\'' +
                ", channel='" + channel + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }
}