package com.paymentengine.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@JsonTypeName("PAYMENT_NOTIFICATION")
public class PaymentNotificationEvent extends PaymentEvent {
    
    @NotNull
    private String notificationType; // EMAIL, SMS, PUSH
    
    @NotNull
    private String recipient;
    
    @NotNull
    private String subject;
    
    @NotNull
    private String message;
    
    private String templateId;
    
    private Map<String, Object> templateData;
    
    private UUID transactionId;
    
    private String transactionReference;
    
    private BigDecimal amount;
    
    private String currencyCode;
    
    private String priority; // HIGH, MEDIUM, LOW
    
    private Integer retryCount;
    
    private String language;

    public PaymentNotificationEvent() {
        super("PAYMENT_NOTIFICATION", "notification-service");
    }

    public PaymentNotificationEvent(String notificationType, String recipient, 
                                  String subject, String message) {
        this();
        this.notificationType = notificationType;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        this.priority = "MEDIUM";
        this.retryCount = 0;
        this.language = "en";
    }

    // Getters and Setters
    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Map<String, Object> getTemplateData() {
        return templateData;
    }

    public void setTemplateData(Map<String, Object> templateData) {
        this.templateData = templateData;
    }

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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}