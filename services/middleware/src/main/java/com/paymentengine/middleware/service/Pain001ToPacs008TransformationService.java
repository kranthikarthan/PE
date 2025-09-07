package com.paymentengine.middleware.service;

import java.util.Map;

/**
 * Service for transforming PAIN.001 messages to PACS.008 messages for clearing system processing
 */
public interface Pain001ToPacs008TransformationService {
    
    /**
     * Transform PAIN.001 message to PACS.008 message for clearing system
     */
    Map<String, Object> transformPain001ToPacs008(Map<String, Object> pain001Message, 
                                                 String tenantId, 
                                                 String paymentType, 
                                                 String localInstrumentCode);
    
    /**
     * Validate PAIN.001 message before transformation
     */
    Map<String, Object> validatePain001Message(Map<String, Object> pain001Message);
    
    /**
     * Extract payment information from PAIN.001 message
     */
    PaymentInfo extractPaymentInfo(Map<String, Object> pain001Message);
    
    /**
     * Create PACS.008 message structure
     */
    Map<String, Object> createPacs008Message(PaymentInfo paymentInfo, 
                                           String clearingSystemCode, 
                                           String tenantId);
    
    /**
     * Payment information extracted from PAIN.001
     */
    class PaymentInfo {
        private String messageId;
        private String endToEndId;
        private String instructionId;
        private String uetr;
        private String debtorName;
        private String debtorAccountId;
        private String debtorBic;
        private String creditorName;
        private String creditorAccountId;
        private String creditorBic;
        private String amount;
        private String currency;
        private String executionDate;
        private String remittanceInfo;
        private String paymentType;
        private String localInstrumentCode;
        private String serviceLevel;
        private String chargeBearer;
        private String purposeCode;
        private String categoryPurpose;
        private String initiatingPartyName;
        private String initiatingPartyBic;
        
        // Constructors
        public PaymentInfo() {}
        
        public PaymentInfo(String messageId, String endToEndId, String instructionId,
                          String uetr, String debtorName, String debtorAccountId, String debtorBic,
                          String creditorName, String creditorAccountId, String creditorBic,
                          String amount, String currency, String executionDate,
                          String remittanceInfo, String paymentType, String localInstrumentCode,
                          String serviceLevel, String chargeBearer, String purposeCode,
                          String categoryPurpose, String initiatingPartyName, String initiatingPartyBic) {
            this.messageId = messageId;
            this.endToEndId = endToEndId;
            this.instructionId = instructionId;
            this.uetr = uetr;
            this.debtorName = debtorName;
            this.debtorAccountId = debtorAccountId;
            this.debtorBic = debtorBic;
            this.creditorName = creditorName;
            this.creditorAccountId = creditorAccountId;
            this.creditorBic = creditorBic;
            this.amount = amount;
            this.currency = currency;
            this.executionDate = executionDate;
            this.remittanceInfo = remittanceInfo;
            this.paymentType = paymentType;
            this.localInstrumentCode = localInstrumentCode;
            this.serviceLevel = serviceLevel;
            this.chargeBearer = chargeBearer;
            this.purposeCode = purposeCode;
            this.categoryPurpose = categoryPurpose;
            this.initiatingPartyName = initiatingPartyName;
            this.initiatingPartyBic = initiatingPartyBic;
        }
        
        // Getters and Setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public String getEndToEndId() { return endToEndId; }
        public void setEndToEndId(String endToEndId) { this.endToEndId = endToEndId; }
        
        public String getInstructionId() { return instructionId; }
        public void setInstructionId(String instructionId) { this.instructionId = instructionId; }
        
        public String getUetr() { return uetr; }
        public void setUetr(String uetr) { this.uetr = uetr; }
        
        public String getDebtorName() { return debtorName; }
        public void setDebtorName(String debtorName) { this.debtorName = debtorName; }
        
        public String getDebtorAccountId() { return debtorAccountId; }
        public void setDebtorAccountId(String debtorAccountId) { this.debtorAccountId = debtorAccountId; }
        
        public String getDebtorBic() { return debtorBic; }
        public void setDebtorBic(String debtorBic) { this.debtorBic = debtorBic; }
        
        public String getCreditorName() { return creditorName; }
        public void setCreditorName(String creditorName) { this.creditorName = creditorName; }
        
        public String getCreditorAccountId() { return creditorAccountId; }
        public void setCreditorAccountId(String creditorAccountId) { this.creditorAccountId = creditorAccountId; }
        
        public String getCreditorBic() { return creditorBic; }
        public void setCreditorBic(String creditorBic) { this.creditorBic = creditorBic; }
        
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getExecutionDate() { return executionDate; }
        public void setExecutionDate(String executionDate) { this.executionDate = executionDate; }
        
        public String getRemittanceInfo() { return remittanceInfo; }
        public void setRemittanceInfo(String remittanceInfo) { this.remittanceInfo = remittanceInfo; }
        
        public String getPaymentType() { return paymentType; }
        public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
        
        public String getLocalInstrumentCode() { return localInstrumentCode; }
        public void setLocalInstrumentCode(String localInstrumentCode) { this.localInstrumentCode = localInstrumentCode; }
        
        public String getServiceLevel() { return serviceLevel; }
        public void setServiceLevel(String serviceLevel) { this.serviceLevel = serviceLevel; }
        
        public String getChargeBearer() { return chargeBearer; }
        public void setChargeBearer(String chargeBearer) { this.chargeBearer = chargeBearer; }
        
        public String getPurposeCode() { return purposeCode; }
        public void setPurposeCode(String purposeCode) { this.purposeCode = purposeCode; }
        
        public String getCategoryPurpose() { return categoryPurpose; }
        public void setCategoryPurpose(String categoryPurpose) { this.categoryPurpose = categoryPurpose; }
        
        public String getInitiatingPartyName() { return initiatingPartyName; }
        public void setInitiatingPartyName(String initiatingPartyName) { this.initiatingPartyName = initiatingPartyName; }
        
        public String getInitiatingPartyBic() { return initiatingPartyBic; }
        public void setInitiatingPartyBic(String initiatingPartyBic) { this.initiatingPartyBic = initiatingPartyBic; }
    }
}