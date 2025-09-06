package com.paymentengine.shared.dto.iso20022;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * ISO 20022 camt.055 - Customer Payment Cancellation Request
 * Used by customers to request cancellation of previously initiated payments
 */
public class Camt055Message {
    
    @JsonProperty("CstmrPmtCxlReq")
    @NotNull
    @Valid
    private CustomerPaymentCancellationRequest customerPaymentCancellationRequest;
    
    public Camt055Message() {}
    
    public Camt055Message(CustomerPaymentCancellationRequest customerPaymentCancellationRequest) {
        this.customerPaymentCancellationRequest = customerPaymentCancellationRequest;
    }
    
    public CustomerPaymentCancellationRequest getCustomerPaymentCancellationRequest() {
        return customerPaymentCancellationRequest;
    }
    
    public void setCustomerPaymentCancellationRequest(CustomerPaymentCancellationRequest customerPaymentCancellationRequest) {
        this.customerPaymentCancellationRequest = customerPaymentCancellationRequest;
    }
    
    /**
     * Customer Payment Cancellation Request
     */
    public static class CustomerPaymentCancellationRequest {
        
        @JsonProperty("GrpHdr")
        @NotNull
        @Valid
        private GroupHeader groupHeader;
        
        @JsonProperty("Undrlyg")
        @NotNull
        @Valid
        private List<UnderlyingTransaction> underlying;
        
        @JsonProperty("SplmtryData")
        @Valid
        private List<SupplementaryData> supplementaryData;
        
        public CustomerPaymentCancellationRequest() {}
        
        public GroupHeader getGroupHeader() {
            return groupHeader;
        }
        
        public void setGroupHeader(GroupHeader groupHeader) {
            this.groupHeader = groupHeader;
        }
        
        public List<UnderlyingTransaction> getUnderlying() {
            return underlying;
        }
        
        public void setUnderlying(List<UnderlyingTransaction> underlying) {
            this.underlying = underlying;
        }
        
        public List<SupplementaryData> getSupplementaryData() {
            return supplementaryData;
        }
        
        public void setSupplementaryData(List<SupplementaryData> supplementaryData) {
            this.supplementaryData = supplementaryData;
        }
    }
    
    /**
     * Group Header for camt.055
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
        
        public FinancialInstitution getForwardingAgent() {
            return forwardingAgent;
        }
        
        public void setForwardingAgent(FinancialInstitution forwardingAgent) {
            this.forwardingAgent = forwardingAgent;
        }
    }
    
    /**
     * Underlying Transaction
     */
    public static class UnderlyingTransaction {
        
        @JsonProperty("OrgnlGrpInfAndCxl")
        @Valid
        private OriginalGroupInformationAndCancellation originalGroupInformationAndCancellation;
        
        @JsonProperty("OrgnlPmtInfAndCxl")
        @Valid
        private List<OriginalPaymentInformationAndCancellation> originalPaymentInformationAndCancellation;
        
        @JsonProperty("TxInf")
        @Valid
        private List<PaymentTransaction> transactionInformation;
        
        public UnderlyingTransaction() {}
        
        public OriginalGroupInformationAndCancellation getOriginalGroupInformationAndCancellation() {
            return originalGroupInformationAndCancellation;
        }
        
        public void setOriginalGroupInformationAndCancellation(OriginalGroupInformationAndCancellation originalGroupInformationAndCancellation) {
            this.originalGroupInformationAndCancellation = originalGroupInformationAndCancellation;
        }
        
        public List<OriginalPaymentInformationAndCancellation> getOriginalPaymentInformationAndCancellation() {
            return originalPaymentInformationAndCancellation;
        }
        
        public void setOriginalPaymentInformationAndCancellation(List<OriginalPaymentInformationAndCancellation> originalPaymentInformationAndCancellation) {
            this.originalPaymentInformationAndCancellation = originalPaymentInformationAndCancellation;
        }
        
        public List<PaymentTransaction> getTransactionInformation() {
            return transactionInformation;
        }
        
        public void setTransactionInformation(List<PaymentTransaction> transactionInformation) {
            this.transactionInformation = transactionInformation;
        }
    }
    
    /**
     * Original Group Information and Cancellation
     */
    public static class OriginalGroupInformationAndCancellation {
        
        @JsonProperty("OrgnlMsgId")
        @NotNull
        @Size(min = 1, max = 35)
        private String originalMessageId;
        
        @JsonProperty("OrgnlMsgNmId")
        @NotNull
        @Size(min = 1, max = 35)
        private String originalMessageNameId; // pain.001.001.03, pacs.008.001.03, etc.
        
        @JsonProperty("OrgnlCreDtTm")
        private String originalCreationDateTime;
        
        @JsonProperty("OrgnlNbOfTxs")
        private String originalNumberOfTransactions;
        
        @JsonProperty("OrgnlCtrlSum")
        private BigDecimal originalControlSum;
        
        @JsonProperty("GrpCxlInd")
        private Boolean groupCancellationIndicator;
        
        @JsonProperty("CxlRsnInf")
        @Valid
        private List<PaymentCancellationReason> cancellationReasonInformation;
        
        public OriginalGroupInformationAndCancellation() {}
        
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
        
        public Boolean getGroupCancellationIndicator() {
            return groupCancellationIndicator;
        }
        
        public void setGroupCancellationIndicator(Boolean groupCancellationIndicator) {
            this.groupCancellationIndicator = groupCancellationIndicator;
        }
        
        public List<PaymentCancellationReason> getCancellationReasonInformation() {
            return cancellationReasonInformation;
        }
        
        public void setCancellationReasonInformation(List<PaymentCancellationReason> cancellationReasonInformation) {
            this.cancellationReasonInformation = cancellationReasonInformation;
        }
    }
    
    /**
     * Original Payment Information and Cancellation
     */
    public static class OriginalPaymentInformationAndCancellation {
        
        @JsonProperty("OrgnlPmtInfId")
        @NotNull
        @Size(min = 1, max = 35)
        private String originalPaymentInformationId;
        
        @JsonProperty("OrgnlNbOfTxs")
        private String originalNumberOfTransactions;
        
        @JsonProperty("OrgnlCtrlSum")
        private BigDecimal originalControlSum;
        
        @JsonProperty("PmtInfCxlInd")
        private Boolean paymentInformationCancellationIndicator;
        
        @JsonProperty("CxlRsnInf")
        @Valid
        private List<PaymentCancellationReason> cancellationReasonInformation;
        
        @JsonProperty("TxInf")
        @Valid
        private List<PaymentTransaction> transactionInformation;
        
        public OriginalPaymentInformationAndCancellation() {}
        
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
        
        public Boolean getPaymentInformationCancellationIndicator() {
            return paymentInformationCancellationIndicator;
        }
        
        public void setPaymentInformationCancellationIndicator(Boolean paymentInformationCancellationIndicator) {
            this.paymentInformationCancellationIndicator = paymentInformationCancellationIndicator;
        }
        
        public List<PaymentCancellationReason> getCancellationReasonInformation() {
            return cancellationReasonInformation;
        }
        
        public void setCancellationReasonInformation(List<PaymentCancellationReason> cancellationReasonInformation) {
            this.cancellationReasonInformation = cancellationReasonInformation;
        }
        
        public List<PaymentTransaction> getTransactionInformation() {
            return transactionInformation;
        }
        
        public void setTransactionInformation(List<PaymentTransaction> transactionInformation) {
            this.transactionInformation = transactionInformation;
        }
    }
    
    /**
     * Payment Transaction for Cancellation
     */
    public static class PaymentTransaction {
        
        @JsonProperty("CxlId")
        @Size(max = 35)
        private String cancellationId;
        
        @JsonProperty("OrgnlGrpInf")
        @Valid
        private OriginalGroupInformation originalGroupInformation;
        
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
        private ActiveCurrencyAndAmount originalInstructedAmount;
        
        @JsonProperty("OrgnlReqdExctnDt")
        private String originalRequestedExecutionDate;
        
        @JsonProperty("OrgnlReqdColltnDt")
        private String originalRequestedCollectionDate;
        
        @JsonProperty("CxlRsnInf")
        @Valid
        private List<PaymentCancellationReason> cancellationReasonInformation;
        
        @JsonProperty("OrgnlTxRef")
        @Valid
        private OriginalTransactionReference originalTransactionReference;
        
        @JsonProperty("SplmtryData")
        @Valid
        private List<SupplementaryData> supplementaryData;
        
        public PaymentTransaction() {}
        
        // Getters and Setters
        public String getCancellationId() {
            return cancellationId;
        }
        
        public void setCancellationId(String cancellationId) {
            this.cancellationId = cancellationId;
        }
        
        public OriginalGroupInformation getOriginalGroupInformation() {
            return originalGroupInformation;
        }
        
        public void setOriginalGroupInformation(OriginalGroupInformation originalGroupInformation) {
            this.originalGroupInformation = originalGroupInformation;
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
        
        public String getOriginalUniqueEndToEndTransactionReference() {
            return originalUniqueEndToEndTransactionReference;
        }
        
        public void setOriginalUniqueEndToEndTransactionReference(String originalUniqueEndToEndTransactionReference) {
            this.originalUniqueEndToEndTransactionReference = originalUniqueEndToEndTransactionReference;
        }
        
        public ActiveCurrencyAndAmount getOriginalInstructedAmount() {
            return originalInstructedAmount;
        }
        
        public void setOriginalInstructedAmount(ActiveCurrencyAndAmount originalInstructedAmount) {
            this.originalInstructedAmount = originalInstructedAmount;
        }
        
        public String getOriginalRequestedExecutionDate() {
            return originalRequestedExecutionDate;
        }
        
        public void setOriginalRequestedExecutionDate(String originalRequestedExecutionDate) {
            this.originalRequestedExecutionDate = originalRequestedExecutionDate;
        }
        
        public String getOriginalRequestedCollectionDate() {
            return originalRequestedCollectionDate;
        }
        
        public void setOriginalRequestedCollectionDate(String originalRequestedCollectionDate) {
            this.originalRequestedCollectionDate = originalRequestedCollectionDate;
        }
        
        public List<PaymentCancellationReason> getCancellationReasonInformation() {
            return cancellationReasonInformation;
        }
        
        public void setCancellationReasonInformation(List<PaymentCancellationReason> cancellationReasonInformation) {
            this.cancellationReasonInformation = cancellationReasonInformation;
        }
        
        public OriginalTransactionReference getOriginalTransactionReference() {
            return originalTransactionReference;
        }
        
        public void setOriginalTransactionReference(OriginalTransactionReference originalTransactionReference) {
            this.originalTransactionReference = originalTransactionReference;
        }
        
        public List<SupplementaryData> getSupplementaryData() {
            return supplementaryData;
        }
        
        public void setSupplementaryData(List<SupplementaryData> supplementaryData) {
            this.supplementaryData = supplementaryData;
        }
    }
    
    /**
     * Payment Cancellation Reason
     */
    public static class PaymentCancellationReason {
        
        @JsonProperty("Orgtr")
        @Valid
        private PartyIdentification originator;
        
        @JsonProperty("Rsn")
        @Valid
        private CancellationReason reason;
        
        @JsonProperty("AddtlInf")
        @Size(max = 105)
        private List<String> additionalInformation;
        
        public PaymentCancellationReason() {}
        
        public PaymentCancellationReason(CancellationReason reason) {
            this.reason = reason;
        }
        
        public PartyIdentification getOriginator() {
            return originator;
        }
        
        public void setOriginator(PartyIdentification originator) {
            this.originator = originator;
        }
        
        public CancellationReason getReason() {
            return reason;
        }
        
        public void setReason(CancellationReason reason) {
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
     * Cancellation Reason
     */
    public static class CancellationReason {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code; // CUST, DUPL, FRAD, TECH, UPAY, etc.
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public CancellationReason() {}
        
        public CancellationReason(String code) {
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
        private String originalMessageNameId;
        
        @JsonProperty("OrgnlCreDtTm")
        private String originalCreationDateTime;
        
        public OriginalGroupInformation() {}
        
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
    }
    
    /**
     * Original Transaction Reference
     */
    public static class OriginalTransactionReference {
        
        @JsonProperty("IntrBkSttlmAmt")
        @Valid
        private ActiveCurrencyAndAmount interbankSettlementAmount;
        
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
        public ActiveCurrencyAndAmount getInterbankSettlementAmount() {
            return interbankSettlementAmount;
        }
        
        public void setInterbankSettlementAmount(ActiveCurrencyAndAmount interbankSettlementAmount) {
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
        
        public RemittanceInformation getRemittanceInformation() {
            return remittanceInformation;
        }
        
        public void setRemittanceInformation(RemittanceInformation remittanceInformation) {
            this.remittanceInformation = remittanceInformation;
        }
    }
    
    /**
     * Amount Type
     */
    public static class AmountType {
        
        @JsonProperty("InstdAmt")
        @Valid
        private ActiveCurrencyAndAmount instructedAmount;
        
        @JsonProperty("EqvtAmt")
        @Valid
        private EquivalentAmount equivalentAmount;
        
        public AmountType() {}
        
        public ActiveCurrencyAndAmount getInstructedAmount() {
            return instructedAmount;
        }
        
        public void setInstructedAmount(ActiveCurrencyAndAmount instructedAmount) {
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
     * Active Currency and Amount
     */
    public static class ActiveCurrencyAndAmount {
        
        @JsonProperty("Ccy")
        @NotNull
        @Size(min = 3, max = 3)
        private String currency;
        
        @JsonProperty("value")
        @NotNull
        private BigDecimal value;
        
        public ActiveCurrencyAndAmount() {}
        
        public ActiveCurrencyAndAmount(String currency, BigDecimal value) {
            this.currency = currency;
            this.value = value;
        }
        
        public String getCurrency() {
            return currency;
        }
        
        public void setCurrency(String currency) {
            this.currency = currency;
        }
        
        public BigDecimal getValue() {
            return value;
        }
        
        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }
    
    /**
     * Equivalent Amount
     */
    public static class EquivalentAmount {
        
        @JsonProperty("Amt")
        @NotNull
        @Valid
        private ActiveCurrencyAndAmount amount;
        
        @JsonProperty("CcyOfTrf")
        @NotNull
        @Size(min = 3, max = 3)
        private String currencyOfTransfer;
        
        public EquivalentAmount() {}
        
        public ActiveCurrencyAndAmount getAmount() {
            return amount;
        }
        
        public void setAmount(ActiveCurrencyAndAmount amount) {
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
     * Party Identification
     */
    public static class PartyIdentification {
        
        @JsonProperty("Nm")
        @Size(max = 140)
        private String name;
        
        @JsonProperty("PstlAdr")
        @Valid
        private Party.PostalAddress postalAddress;
        
        @JsonProperty("Id")
        @Valid
        private Party.PartyIdentification identification;
        
        @JsonProperty("CtryOfRes")
        @Size(min = 2, max = 2)
        private String countryOfResidence;
        
        @JsonProperty("CtctDtls")
        @Valid
        private Party.ContactDetails contactDetails;
        
        public PartyIdentification() {}
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Party.PostalAddress getPostalAddress() {
            return postalAddress;
        }
        
        public void setPostalAddress(Party.PostalAddress postalAddress) {
            this.postalAddress = postalAddress;
        }
        
        public Party.PartyIdentification getIdentification() {
            return identification;
        }
        
        public void setIdentification(Party.PartyIdentification identification) {
            this.identification = identification;
        }
        
        public String getCountryOfResidence() {
            return countryOfResidence;
        }
        
        public void setCountryOfResidence(String countryOfResidence) {
            this.countryOfResidence = countryOfResidence;
        }
        
        public Party.ContactDetails getContactDetails() {
            return contactDetails;
        }
        
        public void setContactDetails(Party.ContactDetails contactDetails) {
            this.contactDetails = contactDetails;
        }
    }
    
    /**
     * Settlement Information
     */
    public static class SettlementInformation {
        
        @JsonProperty("SttlmMtd")
        @NotNull
        private String settlementMethod; // INDA, INGA, COVE, CLRG
        
        @JsonProperty("SttlmAcct")
        @Valid
        private Account settlementAccount;
        
        @JsonProperty("ClrSys")
        @Valid
        private ClearingSystemIdentification clearingSystem;
        
        @JsonProperty("InstgRmbrsmntAgt")
        @Valid
        private FinancialInstitution instructingReimbursementAgent;
        
        @JsonProperty("InstgRmbrsmntAgtAcct")
        @Valid
        private Account instructingReimbursementAgentAccount;
        
        @JsonProperty("InstdRmbrsmntAgt")
        @Valid
        private FinancialInstitution instructedReimbursementAgent;
        
        @JsonProperty("InstdRmbrsmntAgtAcct")
        @Valid
        private Account instructedReimbursementAgentAccount;
        
        @JsonProperty("ThrdRmbrsmntAgt")
        @Valid
        private FinancialInstitution thirdReimbursementAgent;
        
        @JsonProperty("ThrdRmbrsmntAgtAcct")
        @Valid
        private Account thirdReimbursementAgentAccount;
        
        public SettlementInformation() {}
        
        public String getSettlementMethod() {
            return settlementMethod;
        }
        
        public void setSettlementMethod(String settlementMethod) {
            this.settlementMethod = settlementMethod;
        }
        
        public Account getSettlementAccount() {
            return settlementAccount;
        }
        
        public void setSettlementAccount(Account settlementAccount) {
            this.settlementAccount = settlementAccount;
        }
        
        public ClearingSystemIdentification getClearingSystem() {
            return clearingSystem;
        }
        
        public void setClearingSystem(ClearingSystemIdentification clearingSystem) {
            this.clearingSystem = clearingSystem;
        }
    }
    
    /**
     * Clearing System Identification
     */
    public static class ClearingSystemIdentification {
        
        @JsonProperty("Cd")
        @Size(max = 5)
        private String code; // USABA, CHAPS, TARGET2, etc.
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public ClearingSystemIdentification() {}
        
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
    
    /**
     * Purpose
     */
    public static class Purpose {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code; // CBFF, CHAR, CORT, etc.
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public Purpose() {}
        
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
     * Remittance Information
     */
    public static class RemittanceInformation {
        
        @JsonProperty("Ustrd")
        @Size(max = 140)
        private List<String> unstructured;
        
        @JsonProperty("Strd")
        @Valid
        private List<StructuredRemittanceInformation> structured;
        
        public RemittanceInformation() {}
        
        public List<String> getUnstructured() {
            return unstructured;
        }
        
        public void setUnstructured(List<String> unstructured) {
            this.unstructured = unstructured;
        }
        
        public List<StructuredRemittanceInformation> getStructured() {
            return structured;
        }
        
        public void setStructured(List<StructuredRemittanceInformation> structured) {
            this.structured = structured;
        }
    }
    
    /**
     * Structured Remittance Information
     */
    public static class StructuredRemittanceInformation {
        
        @JsonProperty("RfrdDocInf")
        @Valid
        private List<ReferredDocumentInformation> referredDocumentInformation;
        
        @JsonProperty("RfrdDocAmt")
        @Valid
        private RemittanceAmount referredDocumentAmount;
        
        @JsonProperty("CdtrRefInf")
        @Valid
        private CreditorReferenceInformation creditorReferenceInformation;
        
        @JsonProperty("Invcr")
        @Valid
        private PartyIdentification invoicer;
        
        @JsonProperty("Invcee")
        @Valid
        private PartyIdentification invoicee;
        
        @JsonProperty("TaxRmt")
        @Valid
        private TaxInformation taxRemittance;
        
        @JsonProperty("AddtlRmtInf")
        @Size(max = 140)
        private List<String> additionalRemittanceInformation;
        
        public StructuredRemittanceInformation() {}
        
        public List<ReferredDocumentInformation> getReferredDocumentInformation() {
            return referredDocumentInformation;
        }
        
        public void setReferredDocumentInformation(List<ReferredDocumentInformation> referredDocumentInformation) {
            this.referredDocumentInformation = referredDocumentInformation;
        }
        
        public RemittanceAmount getReferredDocumentAmount() {
            return referredDocumentAmount;
        }
        
        public void setReferredDocumentAmount(RemittanceAmount referredDocumentAmount) {
            this.referredDocumentAmount = referredDocumentAmount;
        }
        
        public CreditorReferenceInformation getCreditorReferenceInformation() {
            return creditorReferenceInformation;
        }
        
        public void setCreditorReferenceInformation(CreditorReferenceInformation creditorReferenceInformation) {
            this.creditorReferenceInformation = creditorReferenceInformation;
        }
    }
    
    // Supporting classes (reusing existing types where applicable)
    
    public static class ReferredDocumentInformation {
        @JsonProperty("Tp")
        private ReferredDocumentType type;
        
        @JsonProperty("Nb")
        @Size(max = 35)
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
        @Size(max = 35)
        private String issuer;
        
        public ReferredDocumentTypeChoice getCodeOrProprietary() { return codeOrProprietary; }
        public void setCodeOrProprietary(ReferredDocumentTypeChoice codeOrProprietary) { this.codeOrProprietary = codeOrProprietary; }
    }
    
    public static class ReferredDocumentTypeChoice {
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code; // MSIN, CNFA, DNFA, CINV, etc.
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    public static class DocumentLineInformation {
        @JsonProperty("Id")
        private List<DocumentLineIdentification> identification;
        
        @JsonProperty("Desc")
        @Size(max = 2048)
        private String description;
        
        @JsonProperty("Amt")
        private RemittanceAmount amount;
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    public static class DocumentLineIdentification {
        @JsonProperty("Tp")
        private String type;
        
        @JsonProperty("Nb")
        @Size(max = 35)
        private String number;
        
        @JsonProperty("RltdDt")
        private String relatedDate;
        
        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }
    }
    
    public static class RemittanceAmount {
        @JsonProperty("DuePyblAmt")
        private ActiveCurrencyAndAmount duePayableAmount;
        
        @JsonProperty("DscntApldAmt")
        private List<DiscountAmountAndType> discountAppliedAmount;
        
        @JsonProperty("CdtNoteAmt")
        private ActiveCurrencyAndAmount creditNoteAmount;
        
        @JsonProperty("TaxAmt")
        private List<TaxAmountAndType> taxAmount;
        
        @JsonProperty("AdjstmntAmtAndRsn")
        private List<DocumentAdjustment> adjustmentAmountAndReason;
        
        @JsonProperty("RmtdAmt")
        private ActiveCurrencyAndAmount remittedAmount;
        
        public ActiveCurrencyAndAmount getDuePayableAmount() { return duePayableAmount; }
        public void setDuePayableAmount(ActiveCurrencyAndAmount duePayableAmount) { this.duePayableAmount = duePayableAmount; }
    }
    
    public static class CreditorReferenceInformation {
        @JsonProperty("Tp")
        private CreditorReferenceType type;
        
        @JsonProperty("Ref")
        @Size(max = 35)
        private String reference;
        
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
    }
    
    public static class CreditorReferenceType {
        @JsonProperty("CdOrPrtry")
        private CreditorReferenceTypeChoice codeOrProprietary;
        
        @JsonProperty("Issr")
        @Size(max = 35)
        private String issuer;
        
        public CreditorReferenceTypeChoice getCodeOrProprietary() { return codeOrProprietary; }
        public void setCodeOrProprietary(CreditorReferenceTypeChoice codeOrProprietary) { this.codeOrProprietary = codeOrProprietary; }
    }
    
    public static class CreditorReferenceTypeChoice {
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code; // SCOR, RADM, RPIN, etc.
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    public static class TaxInformation {
        @JsonProperty("Cdtr")
        private TaxParty creditor;
        
        @JsonProperty("Dbtr")
        private TaxParty debtor;
        
        @JsonProperty("UltmtDbtr")
        private TaxParty ultimateDebtor;
        
        @JsonProperty("AdmstnZone")
        @Size(max = 35)
        private String administrationZone;
        
        @JsonProperty("RefNb")
        @Size(max = 140)
        private String referenceNumber;
        
        @JsonProperty("Mtd")
        @Size(max = 35)
        private String method;
        
        @JsonProperty("TtlTaxblBaseAmt")
        private ActiveCurrencyAndAmount totalTaxableBaseAmount;
        
        @JsonProperty("TtlTaxAmt")
        private ActiveCurrencyAndAmount totalTaxAmount;
        
        @JsonProperty("Dt")
        private String date;
        
        @JsonProperty("SeqNb")
        private String sequenceNumber;
        
        @JsonProperty("Rcrd")
        private List<TaxRecord> record;
        
        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    }
    
    public static class TaxParty {
        @JsonProperty("TaxId")
        private String taxId;
        
        @JsonProperty("RegnId")
        private String registrationId;
        
        @JsonProperty("TaxTp")
        private String taxType;
        
        public String getTaxId() { return taxId; }
        public void setTaxId(String taxId) { this.taxId = taxId; }
    }
    
    public static class TaxRecord {
        @JsonProperty("Tp")
        private String type;
        
        @JsonProperty("Ctgy")
        private String category;
        
        @JsonProperty("CtgyDtls")
        private String categoryDetails;
        
        @JsonProperty("DbtrSts")
        private String debtorStatus;
        
        @JsonProperty("CertId")
        private String certificateId;
        
        @JsonProperty("FrmsCd")
        private String formsCode;
        
        @JsonProperty("Prd")
        private TaxPeriod period;
        
        @JsonProperty("TaxAmt")
        private TaxAmount taxAmount;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
    
    public static class TaxPeriod {
        @JsonProperty("Yr")
        private String year;
        
        @JsonProperty("Tp")
        private String type;
        
        @JsonProperty("FrToDt")
        private DatePeriod fromToDate;
        
        public String getYear() { return year; }
        public void setYear(String year) { this.year = year; }
    }
    
    public static class DatePeriod {
        @JsonProperty("FrDt")
        private String fromDate;
        
        @JsonProperty("ToDt")
        private String toDate;
        
        public String getFromDate() { return fromDate; }
        public void setFromDate(String fromDate) { this.fromDate = fromDate; }
        public String getToDate() { return toDate; }
        public void setToDate(String toDate) { this.toDate = toDate; }
    }
    
    public static class TaxAmount {
        @JsonProperty("Rate")
        private String rate;
        
        @JsonProperty("TaxblBaseAmt")
        private ActiveCurrencyAndAmount taxableBaseAmount;
        
        @JsonProperty("TtlAmt")
        private ActiveCurrencyAndAmount totalAmount;
        
        public String getRate() { return rate; }
        public void setRate(String rate) { this.rate = rate; }
    }
    
    public static class DiscountAmountAndType {
        @JsonProperty("Tp")
        private DiscountAmountType type;
        
        @JsonProperty("Amt")
        private ActiveCurrencyAndAmount amount;
        
        public ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(ActiveCurrencyAndAmount amount) { this.amount = amount; }
    }
    
    public static class DiscountAmountType {
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code;
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    public static class TaxAmountAndType {
        @JsonProperty("Tp")
        private TaxAmountType type;
        
        @JsonProperty("Amt")
        private ActiveCurrencyAndAmount amount;
        
        public ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(ActiveCurrencyAndAmount amount) { this.amount = amount; }
    }
    
    public static class TaxAmountType {
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code;
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    public static class DocumentAdjustment {
        @JsonProperty("Amt")
        private ActiveCurrencyAndAmount amount;
        
        @JsonProperty("CdtDbtInd")
        private String creditDebitIndicator; // CRDT, DBIT
        
        @JsonProperty("Rsn")
        private String reason;
        
        @JsonProperty("AddtlInf")
        @Size(max = 140)
        private String additionalInformation;
        
        public ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(ActiveCurrencyAndAmount amount) { this.amount = amount; }
        public String getCreditDebitIndicator() { return creditDebitIndicator; }
        public void setCreditDebitIndicator(String creditDebitIndicator) { this.creditDebitIndicator = creditDebitIndicator; }
    }
    
    /**
     * Supplementary Data
     */
    public static class SupplementaryData {
        
        @JsonProperty("PlcAndNm")
        @Size(max = 350)
        private String placeAndName;
        
        @JsonProperty("Envlp")
        @NotNull
        private Object envelope; // Any additional data
        
        public SupplementaryData() {}
        
        public String getPlaceAndName() {
            return placeAndName;
        }
        
        public void setPlaceAndName(String placeAndName) {
            this.placeAndName = placeAndName;
        }
        
        public Object getEnvelope() {
            return envelope;
        }
        
        public void setEnvelope(Object envelope) {
            this.envelope = envelope;
        }
    }
    
    @Override
    public String toString() {
        return "Camt055Message{" +
                "customerPaymentCancellationRequest=" + customerPaymentCancellationRequest +
                '}';
    }
}