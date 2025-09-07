package com.paymentengine.middleware.dto.corebanking;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Debit Transaction Request DTO
 */
public class DebitTransactionRequest {
    
    private String transactionReference;
    private String accountNumber;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String tenantId;
    private String paymentType;
    private String externalReference;
    private Map<String, Object> additionalData;
    
    // Constructors
    public DebitTransactionRequest() {}
    
    public DebitTransactionRequest(String transactionReference, String accountNumber, 
                                 BigDecimal amount, String currency, String tenantId) {
        this.transactionReference = transactionReference;
        this.accountNumber = accountNumber;
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
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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
        return "DebitTransactionRequest{" +
                "transactionReference='" + transactionReference + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }
}