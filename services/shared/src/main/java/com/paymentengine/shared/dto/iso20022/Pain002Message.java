package com.paymentengine.shared.dto.iso20022;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ISO 20022 pain.002.001.03 - Customer Payment Status Report
 * Used to report the status of payment instructions
 */
@JsonRootName("CstmrPmtStsRpt")
public class Pain002Message {
    
    @JsonProperty("GrpHdr")
    @Valid
    @NotNull
    private GroupHeader groupHeader;
    
    @JsonProperty("OrgnlGrpInfAndSts")
    @Valid
    private OriginalGroupInformationAndStatus originalGroupInformationAndStatus;
    
    @JsonProperty("TxInfAndSts")
    @Valid
    private List<TransactionInformationAndStatus> transactionInformationAndStatus;
    
    // Constructors
    public Pain002Message() {}
    
    public Pain002Message(GroupHeader groupHeader, 
                         OriginalGroupInformationAndStatus originalGroupInformationAndStatus,
                         List<TransactionInformationAndStatus> transactionInformationAndStatus) {
        this.groupHeader = groupHeader;
        this.originalGroupInformationAndStatus = originalGroupInformationAndStatus;
        this.transactionInformationAndStatus = transactionInformationAndStatus;
    }
    
    // Group Header for pain.002
    public static class GroupHeader {
        @JsonProperty("MsgId")
        @NotNull
        @Size(min = 1, max = 35)
        private String messageId;
        
        @JsonProperty("CreDtTm")
        @NotNull
        private LocalDateTime creationDateTime;
        
        @JsonProperty("InitgPty")
        @Valid
        private Party initiatingParty;
        
        @JsonProperty("FwdgAgt")
        @Valid
        private FinancialInstitution forwardingAgent;
        
        // Constructors
        public GroupHeader() {}
        
        public GroupHeader(String messageId, LocalDateTime creationDateTime, Party initiatingParty) {
            this.messageId = messageId;
            this.creationDateTime = creationDateTime;
            this.initiatingParty = initiatingParty;
        }
        
        // Getters and Setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public LocalDateTime getCreationDateTime() { return creationDateTime; }
        public void setCreationDateTime(LocalDateTime creationDateTime) { this.creationDateTime = creationDateTime; }
        
        public Party getInitiatingParty() { return initiatingParty; }
        public void setInitiatingParty(Party initiatingParty) { this.initiatingParty = initiatingParty; }
        
        public FinancialInstitution getForwardingAgent() { return forwardingAgent; }
        public void setForwardingAgent(FinancialInstitution forwardingAgent) { this.forwardingAgent = forwardingAgent; }
    }
    
    // Original Group Information and Status
    public static class OriginalGroupInformationAndStatus {
        @JsonProperty("OrgnlMsgId")
        @NotNull
        @Size(min = 1, max = 35)
        private String originalMessageId;
        
        @JsonProperty("OrgnlMsgNmId")
        @NotNull
        @Size(min = 1, max = 35)
        private String originalMessageNameId;
        
        @JsonProperty("OrgnlCreDtTm")
        private LocalDateTime originalCreationDateTime;
        
        @JsonProperty("OrgnlNbOfTxs")
        private String originalNumberOfTransactions;
        
        @JsonProperty("OrgnlCtrlSum")
        private BigDecimal originalControlSum;
        
        @JsonProperty("GrpSts")
        @NotNull
        private String groupStatus; // ACCP, RJCT, PART, etc.
        
        @JsonProperty("StsRsnInf")
        @Valid
        private List<StatusReasonInformation> statusReasonInformation;
        
        // Constructors
        public OriginalGroupInformationAndStatus() {}
        
        public OriginalGroupInformationAndStatus(String originalMessageId, String originalMessageNameId, String groupStatus) {
            this.originalMessageId = originalMessageId;
            this.originalMessageNameId = originalMessageNameId;
            this.groupStatus = groupStatus;
        }
        
        // Getters and Setters
        public String getOriginalMessageId() { return originalMessageId; }
        public void setOriginalMessageId(String originalMessageId) { this.originalMessageId = originalMessageId; }
        
        public String getOriginalMessageNameId() { return originalMessageNameId; }
        public void setOriginalMessageNameId(String originalMessageNameId) { this.originalMessageNameId = originalMessageNameId; }
        
        public LocalDateTime getOriginalCreationDateTime() { return originalCreationDateTime; }
        public void setOriginalCreationDateTime(LocalDateTime originalCreationDateTime) { this.originalCreationDateTime = originalCreationDateTime; }
        
        public String getOriginalNumberOfTransactions() { return originalNumberOfTransactions; }
        public void setOriginalNumberOfTransactions(String originalNumberOfTransactions) { this.originalNumberOfTransactions = originalNumberOfTransactions; }
        
        public BigDecimal getOriginalControlSum() { return originalControlSum; }
        public void setOriginalControlSum(BigDecimal originalControlSum) { this.originalControlSum = originalControlSum; }
        
        public String getGroupStatus() { return groupStatus; }
        public void setGroupStatus(String groupStatus) { this.groupStatus = groupStatus; }
        
        public List<StatusReasonInformation> getStatusReasonInformation() { return statusReasonInformation; }
        public void setStatusReasonInformation(List<StatusReasonInformation> statusReasonInformation) { this.statusReasonInformation = statusReasonInformation; }
    }
    
    // Transaction Information and Status
    public static class TransactionInformationAndStatus {
        @JsonProperty("StsId")
        @Size(min = 1, max = 35)
        private String statusId;
        
        @JsonProperty("OrgnlEndToEndId")
        @Size(min = 1, max = 35)
        private String originalEndToEndId;
        
        @JsonProperty("OrgnlTxId")
        @Size(min = 1, max = 35)
        private String originalTransactionId;
        
        @JsonProperty("TxSts")
        @NotNull
        private String transactionStatus; // ACCP, RJCT, PDNG, etc.
        
        @JsonProperty("StsRsnInf")
        @Valid
        private List<StatusReasonInformation> statusReasonInformation;
        
        @JsonProperty("OrgnlTxRef")
        @Valid
        private OriginalTransactionReference originalTransactionReference;
        
        // Constructors
        public TransactionInformationAndStatus() {}
        
        public TransactionInformationAndStatus(String originalEndToEndId, String transactionStatus) {
            this.originalEndToEndId = originalEndToEndId;
            this.transactionStatus = transactionStatus;
        }
        
        // Getters and Setters
        public String getStatusId() { return statusId; }
        public void setStatusId(String statusId) { this.statusId = statusId; }
        
        public String getOriginalEndToEndId() { return originalEndToEndId; }
        public void setOriginalEndToEndId(String originalEndToEndId) { this.originalEndToEndId = originalEndToEndId; }
        
        public String getOriginalTransactionId() { return originalTransactionId; }
        public void setOriginalTransactionId(String originalTransactionId) { this.originalTransactionId = originalTransactionId; }
        
        public String getTransactionStatus() { return transactionStatus; }
        public void setTransactionStatus(String transactionStatus) { this.transactionStatus = transactionStatus; }
        
        public List<StatusReasonInformation> getStatusReasonInformation() { return statusReasonInformation; }
        public void setStatusReasonInformation(List<StatusReasonInformation> statusReasonInformation) { this.statusReasonInformation = statusReasonInformation; }
        
        public OriginalTransactionReference getOriginalTransactionReference() { return originalTransactionReference; }
        public void setOriginalTransactionReference(OriginalTransactionReference originalTransactionReference) { this.originalTransactionReference = originalTransactionReference; }
    }
    
    // Status Reason Information
    public static class StatusReasonInformation {
        @JsonProperty("Orgtr")
        @Valid
        private Party originator;
        
        @JsonProperty("Rsn")
        @Valid
        private Reason reason;
        
        @JsonProperty("AddtlInf")
        private List<String> additionalInformation;
        
        // Constructors
        public StatusReasonInformation() {}
        
        public StatusReasonInformation(Reason reason) {
            this.reason = reason;
        }
        
        // Getters and Setters
        public Party getOriginator() { return originator; }
        public void setOriginator(Party originator) { this.originator = originator; }
        
        public Reason getReason() { return reason; }
        public void setReason(Reason reason) { this.reason = reason; }
        
        public List<String> getAdditionalInformation() { return additionalInformation; }
        public void setAdditionalInformation(List<String> additionalInformation) { this.additionalInformation = additionalInformation; }
    }
    
    // Reason
    public static class Reason {
        @JsonProperty("Cd")
        @Size(min = 1, max = 4)
        private String code;
        
        @JsonProperty("Prtry")
        @Size(min = 1, max = 35)
        private String proprietary;
        
        // Constructors
        public Reason() {}
        
        public Reason(String code) {
            this.code = code;
        }
        
        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getProprietary() { return proprietary; }
        public void setProprietary(String proprietary) { this.proprietary = proprietary; }
    }
    
    // Original Transaction Reference
    public static class OriginalTransactionReference {
        @JsonProperty("Amt")
        @Valid
        private Amount amount;
        
        @JsonProperty("ReqdExctnDt")
        private LocalDateTime requestedExecutionDate;
        
        @JsonProperty("ReqdColltnDt")
        private LocalDateTime requestedCollectionDate;
        
        @JsonProperty("CdtrSchmeId")
        @Valid
        private Party creditorSchemeId;
        
        // Constructors
        public OriginalTransactionReference() {}
        
        // Getters and Setters
        public Amount getAmount() { return amount; }
        public void setAmount(Amount amount) { this.amount = amount; }
        
        public LocalDateTime getRequestedExecutionDate() { return requestedExecutionDate; }
        public void setRequestedExecutionDate(LocalDateTime requestedExecutionDate) { this.requestedExecutionDate = requestedExecutionDate; }
        
        public LocalDateTime getRequestedCollectionDate() { return requestedCollectionDate; }
        public void setRequestedCollectionDate(LocalDateTime requestedCollectionDate) { this.requestedCollectionDate = requestedCollectionDate; }
        
        public Party getCreditorSchemeId() { return creditorSchemeId; }
        public void setCreditorSchemeId(Party creditorSchemeId) { this.creditorSchemeId = creditorSchemeId; }
    }
    
    // Amount
    public static class Amount {
        @JsonProperty("InstdAmt")
        @Valid
        private CommonTypes.CurrencyAndAmount instructedAmount;
        
        @JsonProperty("EqvtAmt")
        @Valid
        private CommonTypes.CurrencyAndAmount equivalentAmount;
        
        // Constructors
        public Amount() {}
        
        public Amount(CommonTypes.CurrencyAndAmount instructedAmount) {
            this.instructedAmount = instructedAmount;
        }
        
        // Getters and Setters
        public CommonTypes.CurrencyAndAmount getInstructedAmount() { return instructedAmount; }
        public void setInstructedAmount(CommonTypes.CurrencyAndAmount instructedAmount) { this.instructedAmount = instructedAmount; }
        
        public CommonTypes.CurrencyAndAmount getEquivalentAmount() { return equivalentAmount; }
        public void setEquivalentAmount(CommonTypes.CurrencyAndAmount equivalentAmount) { this.equivalentAmount = equivalentAmount; }
    }
    
    // Main class getters and setters
    public GroupHeader getGroupHeader() { return groupHeader; }
    public void setGroupHeader(GroupHeader groupHeader) { this.groupHeader = groupHeader; }
    
    public OriginalGroupInformationAndStatus getOriginalGroupInformationAndStatus() { return originalGroupInformationAndStatus; }
    public void setOriginalGroupInformationAndStatus(OriginalGroupInformationAndStatus originalGroupInformationAndStatus) { this.originalGroupInformationAndStatus = originalGroupInformationAndStatus; }
    
    public List<TransactionInformationAndStatus> getTransactionInformationAndStatus() { return transactionInformationAndStatus; }
    public void setTransactionInformationAndStatus(List<TransactionInformationAndStatus> transactionInformationAndStatus) { this.transactionInformationAndStatus = transactionInformationAndStatus; }
    
    @Override
    public String toString() {
        return "Pain002Message{" +
                "groupHeader=" + groupHeader +
                ", originalGroupInformationAndStatus=" + originalGroupInformationAndStatus +
                ", transactionInformationAndStatus=" + transactionInformationAndStatus +
                '}';
    }
}