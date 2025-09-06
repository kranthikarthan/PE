package com.paymentengine.shared.dto.iso20022;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * ISO 20022 pain.001 - Customer Credit Transfer Initiation Message
 * JSON representation of the standard banking payment initiation message
 */
public class Pain001Message {
    
    @JsonProperty("CstmrCdtTrfInitn")
    @NotNull
    @Valid
    private CustomerCreditTransferInitiation customerCreditTransferInitiation;
    
    public Pain001Message() {}
    
    public Pain001Message(CustomerCreditTransferInitiation customerCreditTransferInitiation) {
        this.customerCreditTransferInitiation = customerCreditTransferInitiation;
    }
    
    public CustomerCreditTransferInitiation getCustomerCreditTransferInitiation() {
        return customerCreditTransferInitiation;
    }
    
    public void setCustomerCreditTransferInitiation(CustomerCreditTransferInitiation customerCreditTransferInitiation) {
        this.customerCreditTransferInitiation = customerCreditTransferInitiation;
    }
    
    /**
     * Customer Credit Transfer Initiation
     */
    public static class CustomerCreditTransferInitiation {
        
        @JsonProperty("GrpHdr")
        @NotNull
        @Valid
        private GroupHeader groupHeader;
        
        @JsonProperty("PmtInf")
        @NotNull
        @Valid
        private PaymentInformation paymentInformation;
        
        public CustomerCreditTransferInitiation() {}
        
        public GroupHeader getGroupHeader() {
            return groupHeader;
        }
        
        public void setGroupHeader(GroupHeader groupHeader) {
            this.groupHeader = groupHeader;
        }
        
        public PaymentInformation getPaymentInformation() {
            return paymentInformation;
        }
        
        public void setPaymentInformation(PaymentInformation paymentInformation) {
            this.paymentInformation = paymentInformation;
        }
    }
    
    /**
     * Group Header - Message level information
     */
    public static class GroupHeader {
        
        @JsonProperty("MsgId")
        @NotNull
        @Size(min = 1, max = 35)
        private String messageId;
        
        @JsonProperty("CreDtTm")
        @NotNull
        private String creationDateTime;
        
        @JsonProperty("NbOfTxs")
        @NotNull
        private String numberOfTransactions;
        
        @JsonProperty("CtrlSum")
        private String controlSum;
        
        @JsonProperty("InitgPty")
        @NotNull
        @Valid
        private Party initiatingParty;
        
        public GroupHeader() {}
        
        // Getters and Setters
        public String getMessageId() {
            return messageId;
        }
        
        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }
        
        public String getCreationDateTime() {
            return creationDateTime;
        }
        
        public void setCreationDateTime(String creationDateTime) {
            this.creationDateTime = creationDateTime;
        }
        
        public String getNumberOfTransactions() {
            return numberOfTransactions;
        }
        
        public void setNumberOfTransactions(String numberOfTransactions) {
            this.numberOfTransactions = numberOfTransactions;
        }
        
        public String getControlSum() {
            return controlSum;
        }
        
        public void setControlSum(String controlSum) {
            this.controlSum = controlSum;
        }
        
        public Party getInitiatingParty() {
            return initiatingParty;
        }
        
        public void setInitiatingParty(Party initiatingParty) {
            this.initiatingParty = initiatingParty;
        }
    }
    
    /**
     * Payment Information - Payment instruction details
     */
    public static class PaymentInformation {
        
        @JsonProperty("PmtInfId")
        @NotNull
        @Size(min = 1, max = 35)
        private String paymentInformationId;
        
        @JsonProperty("PmtMtd")
        @NotNull
        private String paymentMethod; // TRF (Transfer)
        
        @JsonProperty("BtchBookg")
        private Boolean batchBooking;
        
        @JsonProperty("NbOfTxs")
        @NotNull
        private String numberOfTransactions;
        
        @JsonProperty("CtrlSum")
        private String controlSum;
        
        @JsonProperty("PmtTpInf")
        @Valid
        private PaymentTypeInformation paymentTypeInformation;
        
        @JsonProperty("ReqdExctnDt")
        @NotNull
        private String requestedExecutionDate;
        
        @JsonProperty("Dbtr")
        @NotNull
        @Valid
        private Party debtor;
        
        @JsonProperty("DbtrAcct")
        @NotNull
        @Valid
        private Account debtorAccount;
        
        @JsonProperty("DbtrAgt")
        @Valid
        private FinancialInstitution debtorAgent;
        
        @JsonProperty("CdtTrfTxInf")
        @NotNull
        @Valid
        private CreditTransferTransactionInformation creditTransferTransactionInformation;
        
        public PaymentInformation() {}
        
        // Getters and Setters
        public String getPaymentInformationId() {
            return paymentInformationId;
        }
        
        public void setPaymentInformationId(String paymentInformationId) {
            this.paymentInformationId = paymentInformationId;
        }
        
        public String getPaymentMethod() {
            return paymentMethod;
        }
        
        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
        
        public Boolean getBatchBooking() {
            return batchBooking;
        }
        
        public void setBatchBooking(Boolean batchBooking) {
            this.batchBooking = batchBooking;
        }
        
        public String getNumberOfTransactions() {
            return numberOfTransactions;
        }
        
        public void setNumberOfTransactions(String numberOfTransactions) {
            this.numberOfTransactions = numberOfTransactions;
        }
        
        public String getControlSum() {
            return controlSum;
        }
        
        public void setControlSum(String controlSum) {
            this.controlSum = controlSum;
        }
        
        public PaymentTypeInformation getPaymentTypeInformation() {
            return paymentTypeInformation;
        }
        
        public void setPaymentTypeInformation(PaymentTypeInformation paymentTypeInformation) {
            this.paymentTypeInformation = paymentTypeInformation;
        }
        
        public String getRequestedExecutionDate() {
            return requestedExecutionDate;
        }
        
        public void setRequestedExecutionDate(String requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
        }
        
        public Party getDebtor() {
            return debtor;
        }
        
        public void setDebtor(Party debtor) {
            this.debtor = debtor;
        }
        
        public Account getDebtorAccount() {
            return debtorAccount;
        }
        
        public void setDebtorAccount(Account debtorAccount) {
            this.debtorAccount = debtorAccount;
        }
        
        public FinancialInstitution getDebtorAgent() {
            return debtorAgent;
        }
        
        public void setDebtorAgent(FinancialInstitution debtorAgent) {
            this.debtorAgent = debtorAgent;
        }
        
        public CreditTransferTransactionInformation getCreditTransferTransactionInformation() {
            return creditTransferTransactionInformation;
        }
        
        public void setCreditTransferTransactionInformation(CreditTransferTransactionInformation creditTransferTransactionInformation) {
            this.creditTransferTransactionInformation = creditTransferTransactionInformation;
        }
    }
    
    @Override
    public String toString() {
        return "Pain001Message{" +
                "customerCreditTransferInitiation=" + customerCreditTransferInitiation +
                '}';
    }
}