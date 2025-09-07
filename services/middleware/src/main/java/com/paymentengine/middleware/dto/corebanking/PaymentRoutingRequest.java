package com.paymentengine.middleware.dto.corebanking;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Payment Routing Request DTO
 */
public class PaymentRoutingRequest {
    
    private String fromAccountNumber;
    private String toAccountNumber;
    private String paymentType;
    private String localInstrumentationCode;
    private BigDecimal amount;
    private String currency;
    private String tenantId;
    private String transactionReference;
    private Map<String, Object> additionalData;
    
    // Constructors
    public PaymentRoutingRequest() {}
    
    public PaymentRoutingRequest(String fromAccountNumber, String toAccountNumber, 
                               String paymentType, String localInstrumentationCode,
                               BigDecimal amount, String currency, String tenantId) {
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.paymentType = paymentType;
        this.localInstrumentationCode = localInstrumentationCode;
        this.amount = amount;
        this.currency = currency;
        this.tenantId = tenantId;
    }
    
    // Getters and Setters
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
    
    public String getLocalInstrumentationCode() {
        return localInstrumentationCode;
    }
    
    public void setLocalInstrumentationCode(String localInstrumentationCode) {
        this.localInstrumentationCode = localInstrumentationCode;
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
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getTransactionReference() {
        return transactionReference;
    }
    
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
    
    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }
    
    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }
    
    @Override
    public String toString() {
        return "PaymentRoutingRequest{" +
                "fromAccountNumber='" + fromAccountNumber + '\'' +
                ", toAccountNumber='" + toAccountNumber + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", localInstrumentationCode='" + localInstrumentationCode + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }
}