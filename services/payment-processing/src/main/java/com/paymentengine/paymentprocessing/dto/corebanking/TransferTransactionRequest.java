package com.paymentengine.paymentprocessing.dto.corebanking;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Transfer Transaction Request DTO
 */
public class TransferTransactionRequest {
    
    private String transactionReference;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String tenantId;
    private String paymentType;
    private String externalReference;
    private Map<String, Object> additionalData;
    
    // Constructors
    public TransferTransactionRequest() {}
    
    public TransferTransactionRequest(String transactionReference, String fromAccountNumber, 
                                    String toAccountNumber, BigDecimal amount, String currency, String tenantId) {
        this.transactionReference = transactionReference;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.currency = currency;
        this.tenantId = tenantId;
    }
    
    // Getters and Setters
    public String getTransactionReference() {
        return transactionReference;
    }
    
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    public String getExternalReference() {
        return externalReference;
    }
    
    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }
    
    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }
    
    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }
    
    @Override
    public String toString() {
        return "TransferTransactionRequest{" +
                "transactionReference='" + transactionReference + '\'' +
                ", fromAccountNumber='" + fromAccountNumber + '\'' +
                ", toAccountNumber='" + toAccountNumber + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }
}