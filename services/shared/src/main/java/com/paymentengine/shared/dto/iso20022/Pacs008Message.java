package com.paymentengine.shared.dto.iso20022;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * ISO 20022 pacs.008 - Financial Institution to Financial Institution Customer Credit Transfer
 * Used for processing payments received from payment schemes/networks
 */
public class Pacs008Message {
    
    @JsonProperty("FIToFICstmrCdtTrf")
    @NotNull
    @Valid
    private FIToFICustomerCreditTransfer fiToFICustomerCreditTransfer;
    
    public Pacs008Message() {}
    
    public Pacs008Message(FIToFICustomerCreditTransfer fiToFICustomerCreditTransfer) {
        this.fiToFICustomerCreditTransfer = fiToFICustomerCreditTransfer;
    }
    
    public FIToFICustomerCreditTransfer getFiToFICustomerCreditTransfer() {
        return fiToFICustomerCreditTransfer;
    }
    
    public void setFiToFICustomerCreditTransfer(FIToFICustomerCreditTransfer fiToFICustomerCreditTransfer) {
        this.fiToFICustomerCreditTransfer = fiToFICustomerCreditTransfer;
    }
    
    /**
     * FI to FI Customer Credit Transfer
     */
    public static class FIToFICustomerCreditTransfer {
        
        @JsonProperty("GrpHdr")
        @NotNull
        @Valid
        private GroupHeader groupHeader;
        
        @JsonProperty("CdtTrfTxInf")
        @NotNull
        @Valid
        private List<CreditTransferTransaction> creditTransferTransactionInformation;
        
        public FIToFICustomerCreditTransfer() {}
        
        public GroupHeader getGroupHeader() {
            return groupHeader;
        }
        
        public void setGroupHeader(GroupHeader groupHeader) {
            this.groupHeader = groupHeader;
        }
        
        public List<CreditTransferTransaction> getCreditTransferTransactionInformation() {
            return creditTransferTransactionInformation;
        }
        
        public void setCreditTransferTransactionInformation(List<CreditTransferTransaction> creditTransferTransactionInformation) {
            this.creditTransferTransactionInformation = creditTransferTransactionInformation;
        }
    }
    
    /**
     * Group Header for pacs.008
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
        
        @JsonProperty("TtlIntrBkSttlmAmt")
        @Valid
        private ActiveCurrencyAndAmount totalInterbankSettlementAmount;
        
        @JsonProperty("IntrBkSttlmDt")
        private String interbankSettlementDate;
        
        @JsonProperty("SttlmInf")
        @NotNull
        @Valid
        private SettlementInformation settlementInformation;
        
        @JsonProperty("PmtTpInf")
        @Valid
        private PaymentTypeInformation paymentTypeInformation;
        
        @JsonProperty("InstgAgt")
        @Valid
        private FinancialInstitution instructingAgent;
        
        @JsonProperty("InstdAgt")
        @Valid
        private FinancialInstitution instructedAgent;
        
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
        
        public ActiveCurrencyAndAmount getTotalInterbankSettlementAmount() {
            return totalInterbankSettlementAmount;
        }
        
        public void setTotalInterbankSettlementAmount(ActiveCurrencyAndAmount totalInterbankSettlementAmount) {
            this.totalInterbankSettlementAmount = totalInterbankSettlementAmount;
        }
        
        public String getInterbankSettlementDate() {
            return interbankSettlementDate;
        }
        
        public void setInterbankSettlementDate(String interbankSettlementDate) {
            this.interbankSettlementDate = interbankSettlementDate;
        }
        
        public SettlementInformation getSettlementInformation() {
            return settlementInformation;
        }
        
        public void setSettlementInformation(SettlementInformation settlementInformation) {
            this.settlementInformation = settlementInformation;
        }
        
        public PaymentTypeInformation getPaymentTypeInformation() {
            return paymentTypeInformation;
        }
        
        public void setPaymentTypeInformation(PaymentTypeInformation paymentTypeInformation) {
            this.paymentTypeInformation = paymentTypeInformation;
        }
        
        public FinancialInstitution getInstructingAgent() {
            return instructingAgent;
        }
        
        public void setInstructingAgent(FinancialInstitution instructingAgent) {
            this.instructingAgent = instructingAgent;
        }
        
        public FinancialInstitution getInstructedAgent() {
            return instructedAgent;
        }
        
        public void setInstructedAgent(FinancialInstitution instructedAgent) {
            this.instructedAgent = instructedAgent;
        }
    }
    
    /**
     * Credit Transfer Transaction for pacs.008
     */
    public static class CreditTransferTransaction {
        
        @JsonProperty("PmtId")
        @NotNull
        @Valid
        private PaymentIdentification paymentIdentification;
        
        @JsonProperty("PmtTpInf")
        @Valid
        private PaymentTypeInformation paymentTypeInformation;
        
        @JsonProperty("IntrBkSttlmAmt")
        @NotNull
        @Valid
        private ActiveCurrencyAndAmount interbankSettlementAmount;
        
        @JsonProperty("IntrBkSttlmDt")
        private String interbankSettlementDate;
        
        @JsonProperty("SttlmPrty")
        private String settlementPriority; // HIGH, NORM
        
        @JsonProperty("SttlmTmIndctn")
        @Valid
        private SettlementTimeIndication settlementTimeIndication;
        
        @JsonProperty("SttlmTmReq")
        @Valid
        private SettlementTimeRequest settlementTimeRequest;
        
        @JsonProperty("AccptncDtTm")
        private String acceptanceDateTime;
        
        @JsonProperty("PoolgAdjstmntDt")
        private String poolingAdjustmentDate;
        
        @JsonProperty("InstgAgt")
        @Valid
        private FinancialInstitution instructingAgent;
        
        @JsonProperty("InstdAgt")
        @Valid
        private FinancialInstitution instructedAgent;
        
        @JsonProperty("IntrmyAgt1")
        @Valid
        private FinancialInstitution intermediaryAgent1;
        
        @JsonProperty("IntrmyAgt1Acct")
        @Valid
        private Account intermediaryAgent1Account;
        
        @JsonProperty("IntrmyAgt2")
        @Valid
        private FinancialInstitution intermediaryAgent2;
        
        @JsonProperty("IntrmyAgt2Acct")
        @Valid
        private Account intermediaryAgent2Account;
        
        @JsonProperty("IntrmyAgt3")
        @Valid
        private FinancialInstitution intermediaryAgent3;
        
        @JsonProperty("IntrmyAgt3Acct")
        @Valid
        private Account intermediaryAgent3Account;
        
        @JsonProperty("PrvsInstgAgt1")
        @Valid
        private FinancialInstitution previousInstructingAgent1;
        
        @JsonProperty("PrvsInstgAgt1Acct")
        @Valid
        private Account previousInstructingAgent1Account;
        
        @JsonProperty("PrvsInstgAgt2")
        @Valid
        private FinancialInstitution previousInstructingAgent2;
        
        @JsonProperty("PrvsInstgAgt2Acct")
        @Valid
        private Account previousInstructingAgent2Account;
        
        @JsonProperty("PrvsInstgAgt3")
        @Valid
        private FinancialInstitution previousInstructingAgent3;
        
        @JsonProperty("PrvsInstgAgt3Acct")
        @Valid
        private Account previousInstructingAgent3Account;
        
        @JsonProperty("ChrgsInf")
        @Valid
        private List<ChargesInformation> chargesInformation;
        
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
        
        @JsonProperty("UltmtDbtr")
        @Valid
        private Party ultimateDebtor;
        
        @JsonProperty("UltmtCdtr")
        @Valid
        private Party ultimateCreditor;
        
        @JsonProperty("InstrForCdtrAgt")
        @Valid
        private List<InstructionForCreditorAgent> instructionForCreditorAgent;
        
        @JsonProperty("InstrForNxtAgt")
        @Valid
        private List<InstructionForNextAgent> instructionForNextAgent;
        
        @JsonProperty("Purp")
        @Valid
        private Purpose purpose;
        
        @JsonProperty("RgltryRptg")
        @Valid
        private List<RegulatoryReporting> regulatoryReporting;
        
        @JsonProperty("Tax")
        @Valid
        private TaxInformation tax;
        
        @JsonProperty("RltdRmtInf")
        @Valid
        private List<RemittanceLocation> relatedRemittanceInformation;
        
        @JsonProperty("RmtInf")
        @Valid
        private RemittanceInformation remittanceInformation;
        
        @JsonProperty("SplmtryData")
        @Valid
        private List<SupplementaryData> supplementaryData;
        
        public CreditTransferTransaction() {}
        
        // Getters and Setters (abbreviated for key fields)
        public PaymentIdentification getPaymentIdentification() {
            return paymentIdentification;
        }
        
        public void setPaymentIdentification(PaymentIdentification paymentIdentification) {
            this.paymentIdentification = paymentIdentification;
        }
        
        public ActiveCurrencyAndAmount getInterbankSettlementAmount() {
            return interbankSettlementAmount;
        }
        
        public void setInterbankSettlementAmount(ActiveCurrencyAndAmount interbankSettlementAmount) {
            this.interbankSettlementAmount = interbankSettlementAmount;
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
        
        public SettlementInformation(String settlementMethod) {
            this.settlementMethod = settlementMethod;
        }
        
        // Getters and Setters
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
     * Settlement Time Indication
     */
    public static class SettlementTimeIndication {
        
        @JsonProperty("DbtDtTm")
        private String debitDateTime;
        
        @JsonProperty("CdtDtTm")
        private String creditDateTime;
        
        public SettlementTimeIndication() {}
        
        public String getDebitDateTime() {
            return debitDateTime;
        }
        
        public void setDebitDateTime(String debitDateTime) {
            this.debitDateTime = debitDateTime;
        }
        
        public String getCreditDateTime() {
            return creditDateTime;
        }
        
        public void setCreditDateTime(String creditDateTime) {
            this.creditDateTime = creditDateTime;
        }
    }
    
    /**
     * Settlement Time Request
     */
    public static class SettlementTimeRequest {
        
        @JsonProperty("CLSTm")
        private String clsTime;
        
        @JsonProperty("TillTm")
        private String tillTime;
        
        @JsonProperty("FrTm")
        private String fromTime;
        
        @JsonProperty("RjctTm")
        private String rejectTime;
        
        public SettlementTimeRequest() {}
        
        // Getters and Setters
        public String getClsTime() {
            return clsTime;
        }
        
        public void setClsTime(String clsTime) {
            this.clsTime = clsTime;
        }
        
        public String getTillTime() {
            return tillTime;
        }
        
        public void setTillTime(String tillTime) {
            this.tillTime = tillTime;
        }
    }
    
    /**
     * Charges Information
     */
    public static class ChargesInformation {
        
        @JsonProperty("Amt")
        @NotNull
        @Valid
        private ActiveCurrencyAndAmount amount;
        
        @JsonProperty("Agt")
        @NotNull
        @Valid
        private FinancialInstitution agent;
        
        @JsonProperty("Tp")
        @Valid
        private ChargeType type;
        
        public ChargesInformation() {}
        
        public ActiveCurrencyAndAmount getAmount() {
            return amount;
        }
        
        public void setAmount(ActiveCurrencyAndAmount amount) {
            this.amount = amount;
        }
        
        public FinancialInstitution getAgent() {
            return agent;
        }
        
        public void setAgent(FinancialInstitution agent) {
            this.agent = agent;
        }
        
        public ChargeType getType() {
            return type;
        }
        
        public void setType(ChargeType type) {
            this.type = type;
        }
    }
    
    /**
     * Charge Type
     */
    public static class ChargeType {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code; // DEBT, CRED, SHAR, SLEV
        
        @JsonProperty("Prtry")
        @Valid
        private ChargeBearerType proprietary;
        
        public ChargeType() {}
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public ChargeBearerType getProprietary() {
            return proprietary;
        }
        
        public void setProprietary(ChargeBearerType proprietary) {
            this.proprietary = proprietary;
        }
    }
    
    /**
     * Charge Bearer Type
     */
    public static class ChargeBearerType {
        
        @JsonProperty("Id")
        @Size(max = 35)
        private String identification;
        
        @JsonProperty("Issr")
        @Size(max = 35)
        private String issuer;
        
        public ChargeBearerType() {}
        
        public String getIdentification() {
            return identification;
        }
        
        public void setIdentification(String identification) {
            this.identification = identification;
        }
        
        public String getIssuer() {
            return issuer;
        }
        
        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }
    
    /**
     * Instruction for Creditor Agent
     */
    public static class InstructionForCreditorAgent {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code; // CHQB, HOLD, PHOB, TELB
        
        @JsonProperty("InstrInf")
        @Size(max = 140)
        private String instructionInformation;
        
        public InstructionForCreditorAgent() {}
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getInstructionInformation() {
            return instructionInformation;
        }
        
        public void setInstructionInformation(String instructionInformation) {
            this.instructionInformation = instructionInformation;
        }
    }
    
    /**
     * Instruction for Next Agent
     */
    public static class InstructionForNextAgent {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code;
        
        @JsonProperty("InstrInf")
        @Size(max = 140)
        private String instructionInformation;
        
        public InstructionForNextAgent() {}
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getInstructionInformation() {
            return instructionInformation;
        }
        
        public void setInstructionInformation(String instructionInformation) {
            this.instructionInformation = instructionInformation;
        }
    }
    
    /**
     * Remittance Location
     */
    public static class RemittanceLocation {
        
        @JsonProperty("RmtId")
        @Size(max = 35)
        private String remittanceIdentification;
        
        @JsonProperty("RmtLctnDtls")
        @Valid
        private List<RemittanceLocationDetails> remittanceLocationDetails;
        
        public RemittanceLocation() {}
        
        public String getRemittanceIdentification() {
            return remittanceIdentification;
        }
        
        public void setRemittanceIdentification(String remittanceIdentification) {
            this.remittanceIdentification = remittanceIdentification;
        }
        
        public List<RemittanceLocationDetails> getRemittanceLocationDetails() {
            return remittanceLocationDetails;
        }
        
        public void setRemittanceLocationDetails(List<RemittanceLocationDetails> remittanceLocationDetails) {
            this.remittanceLocationDetails = remittanceLocationDetails;
        }
    }
    
    /**
     * Remittance Location Details
     */
    public static class RemittanceLocationDetails {
        
        @JsonProperty("Mtd")
        @NotNull
        private String method; // FAXI, EDIC, URID, EMAL, POST, SMSM
        
        @JsonProperty("ElctrncAdr")
        @Size(max = 2048)
        private String electronicAddress;
        
        @JsonProperty("PstlAdr")
        @Valid
        private NameAndAddress postalAddress;
        
        public RemittanceLocationDetails() {}
        
        public String getMethod() {
            return method;
        }
        
        public void setMethod(String method) {
            this.method = method;
        }
        
        public String getElectronicAddress() {
            return electronicAddress;
        }
        
        public void setElectronicAddress(String electronicAddress) {
            this.electronicAddress = electronicAddress;
        }
        
        public NameAndAddress getPostalAddress() {
            return postalAddress;
        }
        
        public void setPostalAddress(NameAndAddress postalAddress) {
            this.postalAddress = postalAddress;
        }
    }
    
    /**
     * Name and Address
     */
    public static class NameAndAddress {
        
        @JsonProperty("Nm")
        @NotNull
        @Size(min = 1, max = 140)
        private String name;
        
        @JsonProperty("Adr")
        @Valid
        private Party.PostalAddress address;
        
        public NameAndAddress() {}
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Party.PostalAddress getAddress() {
            return address;
        }
        
        public void setAddress(Party.PostalAddress address) {
            this.address = address;
        }
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
}