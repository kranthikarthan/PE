package com.paymentengine.shared.dto.iso20022;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * ISO 20022 pain.007 - Customer Payment Reversal
 * Used for client-initiated payment reversals and cancellations
 */
public class Pain007Message {
    
    @JsonProperty("CstmrPmtRvsl")
    @NotNull
    @Valid
    private CustomerPaymentReversal customerPaymentReversal;
    
    public Pain007Message() {}
    
    public Pain007Message(CustomerPaymentReversal customerPaymentReversal) {
        this.customerPaymentReversal = customerPaymentReversal;
    }
    
    public CustomerPaymentReversal getCustomerPaymentReversal() {
        return customerPaymentReversal;
    }
    
    public void setCustomerPaymentReversal(CustomerPaymentReversal customerPaymentReversal) {
        this.customerPaymentReversal = customerPaymentReversal;
    }
    
    /**
     * Customer Payment Reversal
     */
    public static class CustomerPaymentReversal {
        
        @JsonProperty("GrpHdr")
        @NotNull
        @Valid
        private GroupHeader groupHeader;
        
        @JsonProperty("OrgnlGrpInf")
        @NotNull
        @Valid
        private OriginalGroupInformation originalGroupInformation;
        
        @JsonProperty("OrgnlPmtInfAndRvsl")
        @Valid
        private List<OriginalPaymentInformationAndReversal> originalPaymentInformationAndReversal;
        
        @JsonProperty("SplmtryData")
        @Valid
        private List<CommonTypes.SupplementaryData> supplementaryData;
        
        public CustomerPaymentReversal() {}
        
        // Getters and Setters
        public GroupHeader getGroupHeader() {
            return groupHeader;
        }
        
        public void setGroupHeader(GroupHeader groupHeader) {
            this.groupHeader = groupHeader;
        }
        
        public OriginalGroupInformation getOriginalGroupInformation() {
            return originalGroupInformation;
        }
        
        public void setOriginalGroupInformation(OriginalGroupInformation originalGroupInformation) {
            this.originalGroupInformation = originalGroupInformation;
        }
        
        public List<OriginalPaymentInformationAndReversal> getOriginalPaymentInformationAndReversal() {
            return originalPaymentInformationAndReversal;
        }
        
        public void setOriginalPaymentInformationAndReversal(List<OriginalPaymentInformationAndReversal> originalPaymentInformationAndReversal) {
            this.originalPaymentInformationAndReversal = originalPaymentInformationAndReversal;
        }
    }
    
    /**
     * Group Header for pain.007
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
        private BigDecimal controlSum;
        
        @JsonProperty("InitgPty")
        @NotNull
        @Valid
        private Party initiatingParty;
        
        @JsonProperty("FwdgAgt")
        @Valid
        private FinancialInstitution forwardingAgent;
        
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
        
        public BigDecimal getControlSum() {
            return controlSum;
        }
        
        public void setControlSum(BigDecimal controlSum) {
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
     * Original Group Information
     */
    public static class OriginalGroupInformation {
        
        @JsonProperty("OrgnlMsgId")
        @NotNull
        @Size(min = 1, max = 35)
        private String originalMessageId;
        
        @JsonProperty("OrgnlMsgNmId")
        @NotNull
        @Size(min = 1, max = 35)
        private String originalMessageNameId; // pain.001.001.03
        
        @JsonProperty("OrgnlCreDtTm")
        private String originalCreationDateTime;
        
        @JsonProperty("OrgnlNbOfTxs")
        private String originalNumberOfTransactions;
        
        @JsonProperty("OrgnlCtrlSum")
        private BigDecimal originalControlSum;
        
        @JsonProperty("GrpRvslInd")
        private Boolean groupReversalIndicator;
        
        @JsonProperty("RvslRsnInf")
        @Valid
        private List<PaymentReversalReason> reversalReasonInformation;
        
        public OriginalGroupInformation() {}
        
        // Getters and Setters
        public String getOriginalMessageId() {
            return originalMessageId;
        }
        
        public void setOriginalMessageId(String originalMessageId) {
            this.originalMessageId = originalMessageId;
        }
        
        public String getOriginalMessageNameId() {
            return originalMessageNameId;
        }
        
        public void setOriginalMessageNameId(String originalMessageNameId) {
            this.originalMessageNameId = originalMessageNameId;
        }
        
        public String getOriginalCreationDateTime() {
            return originalCreationDateTime;
        }
        
        public void setOriginalCreationDateTime(String originalCreationDateTime) {
            this.originalCreationDateTime = originalCreationDateTime;
        }
        
        public Boolean getGroupReversalIndicator() {
            return groupReversalIndicator;
        }
        
        public void setGroupReversalIndicator(Boolean groupReversalIndicator) {
            this.groupReversalIndicator = groupReversalIndicator;
        }
        
        public List<PaymentReversalReason> getReversalReasonInformation() {
            return reversalReasonInformation;
        }
        
        public void setReversalReasonInformation(List<PaymentReversalReason> reversalReasonInformation) {
            this.reversalReasonInformation = reversalReasonInformation;
        }
    }
    
    /**
     * Original Payment Information and Reversal
     */
    public static class OriginalPaymentInformationAndReversal {
        
        @JsonProperty("OrgnlPmtInfId")
        @NotNull
        @Size(min = 1, max = 35)
        private String originalPaymentInformationId;
        
        @JsonProperty("OrgnlNbOfTxs")
        private String originalNumberOfTransactions;
        
        @JsonProperty("OrgnlCtrlSum")
        private BigDecimal originalControlSum;
        
        @JsonProperty("PmtInfRvslInd")
        private Boolean paymentInformationReversalIndicator;
        
        @JsonProperty("RvslRsnInf")
        @Valid
        private List<PaymentReversalReason> reversalReasonInformation;
        
        @JsonProperty("TxInf")
        @Valid
        private List<PaymentTransactionInformation> transactionInformation;
        
        public OriginalPaymentInformationAndReversal() {}
        
        // Getters and Setters
        public String getOriginalPaymentInformationId() {
            return originalPaymentInformationId;
        }
        
        public void setOriginalPaymentInformationId(String originalPaymentInformationId) {
            this.originalPaymentInformationId = originalPaymentInformationId;
        }
        
        public String getOriginalNumberOfTransactions() {
            return originalNumberOfTransactions;
        }
        
        public void setOriginalNumberOfTransactions(String originalNumberOfTransactions) {
            this.originalNumberOfTransactions = originalNumberOfTransactions;
        }
        
        public BigDecimal getOriginalControlSum() {
            return originalControlSum;
        }
        
        public void setOriginalControlSum(BigDecimal originalControlSum) {
            this.originalControlSum = originalControlSum;
        }
        
        public Boolean getPaymentInformationReversalIndicator() {
            return paymentInformationReversalIndicator;
        }
        
        public void setPaymentInformationReversalIndicator(Boolean paymentInformationReversalIndicator) {
            this.paymentInformationReversalIndicator = paymentInformationReversalIndicator;
        }
        
        public List<PaymentReversalReason> getReversalReasonInformation() {
            return reversalReasonInformation;
        }
        
        public void setReversalReasonInformation(List<PaymentReversalReason> reversalReasonInformation) {
            this.reversalReasonInformation = reversalReasonInformation;
        }
        
        public List<PaymentTransactionInformation> getTransactionInformation() {
            return transactionInformation;
        }
        
        public void setTransactionInformation(List<PaymentTransactionInformation> transactionInformation) {
            this.transactionInformation = transactionInformation;
        }
    }
    
    /**
     * Payment Transaction Information
     */
    public static class PaymentTransactionInformation {
        
        @JsonProperty("RvslId")
        @Size(max = 35)
        private String reversalId;
        
        @JsonProperty("OrgnlInstrId")
        @Size(max = 35)
        private String originalInstructionId;
        
        @JsonProperty("OrgnlEndToEndId")
        @Size(max = 35)
        private String originalEndToEndId;
        
        @JsonProperty("OrgnlTxId")
        @Size(max = 35)
        private String originalTransactionId;
        
        @JsonProperty("OrgnlUETR")
        @Size(max = 36)
        private String originalUniqueEndToEndTransactionReference;
        
        @JsonProperty("OrgnlInstdAmt")
        @Valid
        private CommonTypes.ActiveCurrencyAndAmount originalInstructedAmount;
        
        @JsonProperty("RvsdInstdAmt")
        @Valid
        private CommonTypes.ActiveCurrencyAndAmount reversedInstructedAmount;
        
        @JsonProperty("ChrgBr")
        private String chargeBearer; // DEBT, CRED, SHAR, SLEV
        
        @JsonProperty("RvslRsnInf")
        @Valid
        private List<PaymentReversalReason> reversalReasonInformation;
        
        @JsonProperty("OrgnlTxRef")
        @Valid
        private OriginalTransactionReference originalTransactionReference;
        
        @JsonProperty("SplmtryData")
        @Valid
        private List<CommonTypes.SupplementaryData> supplementaryData;
        
        public PaymentTransactionInformation() {}
        
        // Getters and Setters
        public String getReversalId() {
            return reversalId;
        }
        
        public void setReversalId(String reversalId) {
            this.reversalId = reversalId;
        }
        
        public String getOriginalInstructionId() {
            return originalInstructionId;
        }
        
        public void setOriginalInstructionId(String originalInstructionId) {
            this.originalInstructionId = originalInstructionId;
        }
        
        public String getOriginalEndToEndId() {
            return originalEndToEndId;
        }
        
        public void setOriginalEndToEndId(String originalEndToEndId) {
            this.originalEndToEndId = originalEndToEndId;
        }
        
        public String getOriginalTransactionId() {
            return originalTransactionId;
        }
        
        public void setOriginalTransactionId(String originalTransactionId) {
            this.originalTransactionId = originalTransactionId;
        }
        
        public CommonTypes.ActiveCurrencyAndAmount getOriginalInstructedAmount() {
            return originalInstructedAmount;
        }
        
        public void setOriginalInstructedAmount(CommonTypes.ActiveCurrencyAndAmount originalInstructedAmount) {
            this.originalInstructedAmount = originalInstructedAmount;
        }
        
        public CommonTypes.ActiveCurrencyAndAmount getReversedInstructedAmount() {
            return reversedInstructedAmount;
        }
        
        public void setReversedInstructedAmount(CommonTypes.ActiveCurrencyAndAmount reversedInstructedAmount) {
            this.reversedInstructedAmount = reversedInstructedAmount;
        }
        
        public List<PaymentReversalReason> getReversalReasonInformation() {
            return reversalReasonInformation;
        }
        
        public void setReversalReasonInformation(List<PaymentReversalReason> reversalReasonInformation) {
            this.reversalReasonInformation = reversalReasonInformation;
        }
        
        public OriginalTransactionReference getOriginalTransactionReference() {
            return originalTransactionReference;
        }
        
        public void setOriginalTransactionReference(OriginalTransactionReference originalTransactionReference) {
            this.originalTransactionReference = originalTransactionReference;
        }
    }
    
    /**
     * Payment Reversal Reason
     */
    public static class PaymentReversalReason {
        
        @JsonProperty("Orgtr")
        @Valid
        private PartyIdentification originator;
        
        @JsonProperty("Rsn")
        @Valid
        private ReversalReason reason;
        
        @JsonProperty("AddtlInf")
        @Size(max = 105)
        private List<String> additionalInformation;
        
        public PaymentReversalReason() {}
        
        public PartyIdentification getOriginator() {
            return originator;
        }
        
        public void setOriginator(PartyIdentification originator) {
            this.originator = originator;
        }
        
        public ReversalReason getReason() {
            return reason;
        }
        
        public void setReason(ReversalReason reason) {
            this.reason = reason;
        }
        
        public List<String> getAdditionalInformation() {
            return additionalInformation;
        }
        
        public void setAdditionalInformation(List<String> additionalInformation) {
            this.additionalInformation = additionalInformation;
        }
    }
    
    /**
     * Reversal Reason
     */
    public static class ReversalReason {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code; // AC01, AC04, AC06, AG01, AG02, AM04, AM05, etc.
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public ReversalReason() {}
        
        public ReversalReason(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getProprietary() {
            return proprietary;
        }
        
        public void setProprietary(String proprietary) {
            this.proprietary = proprietary;
        }
    }
    
    /**
     * Original Transaction Reference
     */
    public static class OriginalTransactionReference {
        
        @JsonProperty("IntrBkSttlmAmt")
        @Valid
        private CommonTypes.ActiveCurrencyAndAmount interbankSettlementAmount;
        
        @JsonProperty("Amt")
        @Valid
        private AmountType amount;
        
        @JsonProperty("IntrBkSttlmDt")
        private String interbankSettlementDate;
        
        @JsonProperty("ReqdColltnDt")
        private String requestedCollectionDate;
        
        @JsonProperty("ReqdExctnDt")
        private String requestedExecutionDate;
        
        @JsonProperty("CdtrSchmeId")
        @Valid
        private PartyIdentification creditorSchemeIdentification;
        
        @JsonProperty("SttlmInf")
        @Valid
        private SettlementInformation settlementInformation;
        
        @JsonProperty("PmtTpInf")
        @Valid
        private PaymentTypeInformation paymentTypeInformation;
        
        @JsonProperty("PmtMtd")
        private String paymentMethod; // CHK, TRF, DD, TRA
        
        @JsonProperty("MndtRltdInf")
        @Valid
        private MandateRelatedInformation mandateRelatedInformation;
        
        @JsonProperty("RmtInf")
        @Valid
        private RemittanceInformation remittanceInformation;
        
        @JsonProperty("UltmtDbtr")
        @Valid
        private Party ultimateDebtor;
        
        @JsonProperty("Dbtr")
        @Valid
        private Party debtor;
        
        @JsonProperty("DbtrAcct")
        @Valid
        private Account debtorAccount;
        
        @JsonProperty("DbtrAgt")
        @Valid
        private FinancialInstitution debtorAgent;
        
        @JsonProperty("DbtrAgtAcct")
        @Valid
        private Account debtorAgentAccount;
        
        @JsonProperty("CdtrAgt")
        @Valid
        private FinancialInstitution creditorAgent;
        
        @JsonProperty("CdtrAgtAcct")
        @Valid
        private Account creditorAgentAccount;
        
        @JsonProperty("Cdtr")
        @Valid
        private Party creditor;
        
        @JsonProperty("CdtrAcct")
        @Valid
        private Account creditorAccount;
        
        @JsonProperty("UltmtCdtr")
        @Valid
        private Party ultimateCreditor;
        
        @JsonProperty("Purp")
        @Valid
        private Purpose purpose;
        
        public OriginalTransactionReference() {}
        
        // Key getters and setters
        public CommonTypes.ActiveCurrencyAndAmount getInterbankSettlementAmount() {
            return interbankSettlementAmount;
        }
        
        public void setInterbankSettlementAmount(CommonTypes.ActiveCurrencyAndAmount interbankSettlementAmount) {
            this.interbankSettlementAmount = interbankSettlementAmount;
        }
        
        public AmountType getAmount() {
            return amount;
        }
        
        public void setAmount(AmountType amount) {
            this.amount = amount;
        }
        
        public String getPaymentMethod() {
            return paymentMethod;
        }
        
        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
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
        
        public Party getCreditor() {
            return creditor;
        }
        
        public void setCreditor(Party creditor) {
            this.creditor = creditor;
        }
        
        public Account getCreditorAccount() {
            return creditorAccount;
        }
        
        public void setCreditorAccount(Account creditorAccount) {
            this.creditorAccount = creditorAccount;
        }
    }
    
    /**
     * Amount Type (for original transaction reference)
     */
    public static class AmountType {
        
        @JsonProperty("InstdAmt")
        @Valid
        private CommonTypes.ActiveCurrencyAndAmount instructedAmount;
        
        @JsonProperty("EqvtAmt")
        @Valid
        private EquivalentAmount equivalentAmount;
        
        public AmountType() {}
        
        public CommonTypes.ActiveCurrencyAndAmount getInstructedAmount() {
            return instructedAmount;
        }
        
        public void setInstructedAmount(CommonTypes.ActiveCurrencyAndAmount instructedAmount) {
            this.instructedAmount = instructedAmount;
        }
        
        public EquivalentAmount getEquivalentAmount() {
            return equivalentAmount;
        }
        
        public void setEquivalentAmount(EquivalentAmount equivalentAmount) {
            this.equivalentAmount = equivalentAmount;
        }
    }
    
    /**
     * Equivalent Amount
     */
    public static class EquivalentAmount {
        
        @JsonProperty("Amt")
        @NotNull
        @Valid
        private CommonTypes.ActiveCurrencyAndAmount amount;
        
        @JsonProperty("CcyOfTrf")
        @NotNull
        @Size(min = 3, max = 3)
        private String currencyOfTransfer;
        
        public EquivalentAmount() {}
        
        public CommonTypes.ActiveCurrencyAndAmount getAmount() {
            return amount;
        }
        
        public void setAmount(CommonTypes.ActiveCurrencyAndAmount amount) {
            this.amount = amount;
        }
        
        public String getCurrencyOfTransfer() {
            return currencyOfTransfer;
        }
        
        public void setCurrencyOfTransfer(String currencyOfTransfer) {
            this.currencyOfTransfer = currencyOfTransfer;
        }
    }
    
    /**
     * Mandate Related Information
     */
    public static class MandateRelatedInformation {
        
        @JsonProperty("MndtId")
        @Size(max = 35)
        private String mandateId;
        
        @JsonProperty("DtOfSgntr")
        private String dateOfSignature;
        
        @JsonProperty("AmdmntInd")
        private Boolean amendmentIndicator;
        
        @JsonProperty("AmdmntInfDtls")
        @Valid
        private AmendmentInformationDetails amendmentInformationDetails;
        
        @JsonProperty("ElctrncSgntr")
        @Size(max = 1025)
        private String electronicSignature;
        
        @JsonProperty("FrstColltnDt")
        private String firstCollectionDate;
        
        @JsonProperty("FnlColltnDt")
        private String finalCollectionDate;
        
        @JsonProperty("Frqcy")
        private String frequency; // DAIL, WEEK, TOWK, MNTH, etc.
        
        @JsonProperty("Rsn")
        @Valid
        private MandateSetupReason reason;
        
        @JsonProperty("TrckgDays")
        private String trackingDays;
        
        public MandateRelatedInformation() {}
        
        // Getters and Setters
        public String getMandateId() {
            return mandateId;
        }
        
        public void setMandateId(String mandateId) {
            this.mandateId = mandateId;
        }
        
        public String getDateOfSignature() {
            return dateOfSignature;
        }
        
        public void setDateOfSignature(String dateOfSignature) {
            this.dateOfSignature = dateOfSignature;
        }
        
        public String getFrequency() {
            return frequency;
        }
        
        public void setFrequency(String frequency) {
            this.frequency = frequency;
        }
    }
    
    /**
     * Amendment Information Details
     */
    public static class AmendmentInformationDetails {
        
        @JsonProperty("OrgnlMndtId")
        @Size(max = 35)
        private String originalMandateId;
        
        @JsonProperty("OrgnlCdtrSchmeId")
        @Valid
        private PartyIdentification originalCreditorSchemeIdentification;
        
        @JsonProperty("OrgnlCdtrAgt")
        @Valid
        private FinancialInstitution originalCreditorAgent;
        
        @JsonProperty("OrgnlCdtrAgtAcct")
        @Valid
        private Account originalCreditorAgentAccount;
        
        @JsonProperty("OrgnlDbtr")
        @Valid
        private Party originalDebtor;
        
        @JsonProperty("OrgnlDbtrAcct")
        @Valid
        private Account originalDebtorAccount;
        
        @JsonProperty("OrgnlDbtrAgt")
        @Valid
        private FinancialInstitution originalDebtorAgent;
        
        @JsonProperty("OrgnlDbtrAgtAcct")
        @Valid
        private Account originalDebtorAgentAccount;
        
        @JsonProperty("OrgnlFnlColltnDt")
        private String originalFinalCollectionDate;
        
        @JsonProperty("OrgnlFrqcy")
        private String originalFrequency;
        
        @JsonProperty("OrgnlRsn")
        @Valid
        private MandateSetupReason originalReason;
        
        @JsonProperty("OrgnlTrckgDays")
        private String originalTrackingDays;
        
        public AmendmentInformationDetails() {}
        
        // Getters and Setters
        public String getOriginalMandateId() {
            return originalMandateId;
        }
        
        public void setOriginalMandateId(String originalMandateId) {
            this.originalMandateId = originalMandateId;
        }
        
        public Party getOriginalDebtor() {
            return originalDebtor;
        }
        
        public void setOriginalDebtor(Party originalDebtor) {
            this.originalDebtor = originalDebtor;
        }
        
        public Account getOriginalDebtorAccount() {
            return originalDebtorAccount;
        }
        
        public void setOriginalDebtorAccount(Account originalDebtorAccount) {
            this.originalDebtorAccount = originalDebtorAccount;
        }
    }
    
    /**
     * Mandate Setup Reason
     */
    public static class MandateSetupReason {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code; // BKTR, CDCB, CDCD, etc.
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public MandateSetupReason() {}
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getProprietary() {
            return proprietary;
        }
        
        public void setProprietary(String proprietary) {
            this.proprietary = proprietary;
        }
    }
    
    // Additional supporting classes
    
    public static class PartyIdentification {
        @JsonProperty("Nm")
        private String name;
        
        @JsonProperty("PstlAdr")
        private Party.PostalAddress postalAddress;
        
        @JsonProperty("Id")
        private Party.PartyIdentification identification;
        
        @JsonProperty("CtryOfRes")
        private String countryOfResidence;
        
        @JsonProperty("CtctDtls")
        private Party.ContactDetails contactDetails;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Party.PartyIdentification getIdentification() { return identification; }
        public void setIdentification(Party.PartyIdentification identification) { this.identification = identification; }
    }
    
    public static class SettlementInformation {
        @JsonProperty("SttlmMtd")
        private String settlementMethod;
        
        @JsonProperty("SttlmAcct")
        private Account settlementAccount;
        
        @JsonProperty("ClrSys")
        private ClearingSystemIdentification clearingSystem;
        
        public String getSettlementMethod() { return settlementMethod; }
        public void setSettlementMethod(String settlementMethod) { this.settlementMethod = settlementMethod; }
        public Account getSettlementAccount() { return settlementAccount; }
        public void setSettlementAccount(Account settlementAccount) { this.settlementAccount = settlementAccount; }
    }
    
    public static class ClearingSystemIdentification {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getProprietary() { return proprietary; }
        public void setProprietary(String proprietary) { this.proprietary = proprietary; }
    }
    
    public static class LocalInstrument {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getProprietary() { return proprietary; }
        public void setProprietary(String proprietary) { this.proprietary = proprietary; }
    }
    
    public static class Purpose {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getProprietary() { return proprietary; }
        public void setProprietary(String proprietary) { this.proprietary = proprietary; }
    }
    
    public static class RemittanceInformation {
        @JsonProperty("Ustrd")
        private List<String> unstructured;
        
        @JsonProperty("Strd")
        private List<StructuredRemittanceInformation> structured;
        
        public List<String> getUnstructured() { return unstructured; }
        public void setUnstructured(List<String> unstructured) { this.unstructured = unstructured; }
        public List<StructuredRemittanceInformation> getStructured() { return structured; }
        public void setStructured(List<StructuredRemittanceInformation> structured) { this.structured = structured; }
    }
    
    public static class StructuredRemittanceInformation {
        @JsonProperty("RfrdDocInf")
        private List<ReferredDocumentInformation> referredDocumentInformation;
        
        @JsonProperty("RfrdDocAmt")
        private RemittanceAmount referredDocumentAmount;
        
        @JsonProperty("CdtrRefInf")
        private CreditorReferenceInformation creditorReferenceInformation;
        
        @JsonProperty("AddtlRmtInf")
        private List<String> additionalRemittanceInformation;
        
        public List<ReferredDocumentInformation> getReferredDocumentInformation() { return referredDocumentInformation; }
        public void setReferredDocumentInformation(List<ReferredDocumentInformation> referredDocumentInformation) { this.referredDocumentInformation = referredDocumentInformation; }
    }
    
    public static class ReferredDocumentInformation {
        @JsonProperty("Tp")
        private ReferredDocumentType type;
        
        @JsonProperty("Nb")
        private String number;
        
        @JsonProperty("RltdDt")
        private String relatedDate;
        
        @JsonProperty("LineDtls")
        private List<DocumentLineInformation> lineDetails;
        
        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }
        public String getRelatedDate() { return relatedDate; }
        public void setRelatedDate(String relatedDate) { this.relatedDate = relatedDate; }
    }
    
    public static class ReferredDocumentType {
        @JsonProperty("CdOrPrtry")
        private ReferredDocumentTypeChoice codeOrProprietary;
        
        @JsonProperty("Issr")
        private String issuer;
        
        public ReferredDocumentTypeChoice getCodeOrProprietary() { return codeOrProprietary; }
        public void setCodeOrProprietary(ReferredDocumentTypeChoice codeOrProprietary) { this.codeOrProprietary = codeOrProprietary; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
    }
    
    public static class ReferredDocumentTypeChoice {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getProprietary() { return proprietary; }
        public void setProprietary(String proprietary) { this.proprietary = proprietary; }
    }
    
    public static class DocumentLineInformation {
        @JsonProperty("Id")
        private List<DocumentLineIdentification> identification;
        
        @JsonProperty("Desc")
        private String description;
        
        @JsonProperty("Amt")
        private RemittanceAmount amount;
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public RemittanceAmount getAmount() { return amount; }
        public void setAmount(RemittanceAmount amount) { this.amount = amount; }
    }
    
    public static class DocumentLineIdentification {
        @JsonProperty("Tp")
        private String type;
        
        @JsonProperty("Nb")
        private String number;
        
        @JsonProperty("RltdDt")
        private String relatedDate;
        
        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }
        public String getRelatedDate() { return relatedDate; }
        public void setRelatedDate(String relatedDate) { this.relatedDate = relatedDate; }
    }
    
    public static class RemittanceAmount {
        @JsonProperty("DuePyblAmt")
        private CommonTypes.ActiveCurrencyAndAmount duePayableAmount;
        
        @JsonProperty("DscntApldAmt")
        private List<DiscountAmountAndType> discountAppliedAmount;
        
        @JsonProperty("CdtNoteAmt")
        private CommonTypes.ActiveCurrencyAndAmount creditNoteAmount;
        
        @JsonProperty("TaxAmt")
        private List<TaxAmountAndType> taxAmount;
        
        @JsonProperty("AdjstmntAmtAndRsn")
        private List<DocumentAdjustment> adjustmentAmountAndReason;
        
        @JsonProperty("RmtdAmt")
        private CommonTypes.ActiveCurrencyAndAmount remittedAmount;
        
        public CommonTypes.ActiveCurrencyAndAmount getDuePayableAmount() { return duePayableAmount; }
        public void setDuePayableAmount(CommonTypes.ActiveCurrencyAndAmount duePayableAmount) { this.duePayableAmount = duePayableAmount; }
        public CommonTypes.ActiveCurrencyAndAmount getRemittedAmount() { return remittedAmount; }
        public void setRemittedAmount(CommonTypes.ActiveCurrencyAndAmount remittedAmount) { this.remittedAmount = remittedAmount; }
    }
    
    public static class DiscountAmountAndType {
        @JsonProperty("Tp")
        private DiscountAmountType type;
        
        @JsonProperty("Amt")
        private CommonTypes.ActiveCurrencyAndAmount amount;
        
        public CommonTypes.ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(CommonTypes.ActiveCurrencyAndAmount amount) { this.amount = amount; }
    }
    
    public static class DiscountAmountType {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    public static class TaxAmountAndType {
        @JsonProperty("Tp")
        private TaxAmountType type;
        
        @JsonProperty("Amt")
        private CommonTypes.ActiveCurrencyAndAmount amount;
        
        public CommonTypes.ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(CommonTypes.ActiveCurrencyAndAmount amount) { this.amount = amount; }
    }
    
    public static class TaxAmountType {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    public static class DocumentAdjustment {
        @JsonProperty("Amt")
        private CommonTypes.ActiveCurrencyAndAmount amount;
        
        @JsonProperty("CdtDbtInd")
        private String creditDebitIndicator;
        
        @JsonProperty("Rsn")
        private String reason;
        
        @JsonProperty("AddtlInf")
        private String additionalInformation;
        
        public CommonTypes.ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(CommonTypes.ActiveCurrencyAndAmount amount) { this.amount = amount; }
        public String getCreditDebitIndicator() { return creditDebitIndicator; }
        public void setCreditDebitIndicator(String creditDebitIndicator) { this.creditDebitIndicator = creditDebitIndicator; }
    }
    
    public static class CreditorReferenceInformation {
        @JsonProperty("Tp")
        private CreditorReferenceType type;
        
        @JsonProperty("Ref")
        private String reference;
        
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
    }
    
    public static class CreditorReferenceType {
        @JsonProperty("CdOrPrtry")
        private CreditorReferenceTypeChoice codeOrProprietary;
        
        @JsonProperty("Issr")
        private String issuer;
        
        public CreditorReferenceTypeChoice getCodeOrProprietary() { return codeOrProprietary; }
        public void setCodeOrProprietary(CreditorReferenceTypeChoice codeOrProprietary) { this.codeOrProprietary = codeOrProprietary; }
    }
    
    public static class CreditorReferenceTypeChoice {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    // Additional classes following the same pattern...
    // This covers the essential structure for pain.007 reversal messages
}