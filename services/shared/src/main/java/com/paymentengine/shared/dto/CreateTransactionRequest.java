package com.paymentengine.shared.dto;

import java.math.BigDecimal;

/**
 * Placeholder for CreateTransactionRequest
 * TODO: This will be moved to core-banking module when it's built
 */
public class CreateTransactionRequest {
    private String externalReference;
    private BigDecimal amount;
    private String currencyCode;
    private String description;
    private String fromAccount;
    private String toAccount;
    private java.util.UUID paymentTypeId;
    private java.util.Map<String, Object> metadata;
    private String channel;
    
    public CreateTransactionRequest() {}
    
    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getFromAccount() { return fromAccount; }
    public void setFromAccount(String fromAccount) { this.fromAccount = fromAccount; }
    
    public String getToAccount() { return toAccount; }
    public void setToAccount(String toAccount) { this.toAccount = toAccount; }
    
    public java.util.UUID getPaymentTypeId() { return paymentTypeId; }
    public void setPaymentTypeId(java.util.UUID paymentTypeId) { this.paymentTypeId = paymentTypeId; }
    
    public java.util.Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(java.util.Map<String, Object> metadata) { this.metadata = metadata; }
    
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
}