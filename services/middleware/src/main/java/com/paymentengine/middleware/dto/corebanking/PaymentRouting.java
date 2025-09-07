package com.paymentengine.middleware.dto.corebanking;

/**
 * Payment Routing Result DTO
 */
public class PaymentRouting {
    
    public enum RoutingType {
        SAME_BANK,           // Same bank to same bank
        OTHER_BANK,          // Same bank to other bank via clearing system
        INCOMING_CLEARING,   // Other bank to same bank via clearing system
        EXTERNAL_SYSTEM      // External system integration
    }
    
    private RoutingType routingType;
    private String clearingSystemCode;
    private String clearingSystemName;
    private String localInstrumentationCode;
    private String paymentType;
    private boolean requiresClearingSystem;
    private String routingInstructions;
    private String processingMode; // SYNC, ASYNC, BATCH
    private String messageFormat;  // JSON, XML
    private String endpointUrl;
    private String authenticationMethod;
    private int priority;
    private String description;
    
    // Constructors
    public PaymentRouting() {}
    
    public PaymentRouting(RoutingType routingType, String clearingSystemCode, 
                         String localInstrumentationCode, String paymentType) {
        this.routingType = routingType;
        this.clearingSystemCode = clearingSystemCode;
        this.localInstrumentationCode = localInstrumentationCode;
        this.paymentType = paymentType;
        this.requiresClearingSystem = routingType != RoutingType.SAME_BANK;
    }
    
    // Getters and Setters
    public RoutingType getRoutingType() {
        return routingType;
    }
    
    public void setRoutingType(RoutingType routingType) {
        this.routingType = routingType;
    }
    
    public String getClearingSystemCode() {
        return clearingSystemCode;
    }
    
    public void setClearingSystemCode(String clearingSystemCode) {
        this.clearingSystemCode = clearingSystemCode;
    }
    
    public String getClearingSystemName() {
        return clearingSystemName;
    }
    
    public void setClearingSystemName(String clearingSystemName) {
        this.clearingSystemName = clearingSystemName;
    }
    
    public String getLocalInstrumentationCode() {
        return localInstrumentationCode;
    }
    
    public void setLocalInstrumentationCode(String localInstrumentationCode) {
        this.localInstrumentationCode = localInstrumentationCode;
    }
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    public boolean isRequiresClearingSystem() {
        return requiresClearingSystem;
    }
    
    public void setRequiresClearingSystem(boolean requiresClearingSystem) {
        this.requiresClearingSystem = requiresClearingSystem;
    }
    
    public String getRoutingInstructions() {
        return routingInstructions;
    }
    
    public void setRoutingInstructions(String routingInstructions) {
        this.routingInstructions = routingInstructions;
    }
    
    public String getProcessingMode() {
        return processingMode;
    }
    
    public void setProcessingMode(String processingMode) {
        this.processingMode = processingMode;
    }
    
    public String getMessageFormat() {
        return messageFormat;
    }
    
    public void setMessageFormat(String messageFormat) {
        this.messageFormat = messageFormat;
    }
    
    public String getEndpointUrl() {
        return endpointUrl;
    }
    
    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }
    
    public String getAuthenticationMethod() {
        return authenticationMethod;
    }
    
    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "PaymentRouting{" +
                "routingType=" + routingType +
                ", clearingSystemCode='" + clearingSystemCode + '\'' +
                ", clearingSystemName='" + clearingSystemName + '\'' +
                ", localInstrumentationCode='" + localInstrumentationCode + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", requiresClearingSystem=" + requiresClearingSystem +
                ", processingMode='" + processingMode + '\'' +
                ", messageFormat='" + messageFormat + '\'' +
                '}';
    }
}