package com.paymentengine.paymentprocessing.dto.corebanking;

import java.math.BigDecimal;

/**
 * ISO 20022 Payment Request DTO
 */
public class Iso20022PaymentRequest {
    
    private String transactionReference;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String paymentType;
    private String messageType;
    private String iso20022Message;
    private String clearingSystemCode;
    private String tenantId;
    
    // Constructors
    public Iso20022PaymentRequest() {}
    
    public Iso20022PaymentRequest(String transactionReference, String fromAccountNumber, 
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
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public String getIso20022Message() {
        return iso20022Message;
    }
    
    public void setIso20022Message(String iso20022Message) {
        this.iso20022Message = iso20022Message;
    }
    
    public String getClearingSystemCode() {
        return clearingSystemCode;
    }
    
    public void setClearingSystemCode(String clearingSystemCode) {
        this.clearingSystemCode = clearingSystemCode;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    @Override
    public String toString() {
        return "Iso20022PaymentRequest{" +
                "transactionReference='" + transactionReference + '\'' +
                ", fromAccountNumber='" + fromAccountNumber + '\'' +
                ", toAccountNumber='" + toAccountNumber + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", messageType='" + messageType + '\'' +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }
}