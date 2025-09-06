package com.paymentengine.shared.dto.iso20022;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * ISO 20022 camt.054 - Bank to Customer Debit Credit Notification
 * Used for notifying customers of account movements and transaction status
 */
public class Camt054Message {
    
    @JsonProperty("BkToCstmrDbtCdtNtfctn")
    @NotNull
    @Valid
    private BankToCustomerDebitCreditNotification bankToCustomerDebitCreditNotification;
    
    public Camt054Message() {}
    
    public Camt054Message(BankToCustomerDebitCreditNotification bankToCustomerDebitCreditNotification) {
        this.bankToCustomerDebitCreditNotification = bankToCustomerDebitCreditNotification;
    }
    
    public BankToCustomerDebitCreditNotification getBankToCustomerDebitCreditNotification() {
        return bankToCustomerDebitCreditNotification;
    }
    
    public void setBankToCustomerDebitCreditNotification(BankToCustomerDebitCreditNotification bankToCustomerDebitCreditNotification) {
        this.bankToCustomerDebitCreditNotification = bankToCustomerDebitCreditNotification;
    }
    
    /**
     * Bank to Customer Debit Credit Notification
     */
    public static class BankToCustomerDebitCreditNotification {
        
        @JsonProperty("GrpHdr")
        @NotNull
        @Valid
        private GroupHeader groupHeader;
        
        @JsonProperty("Ntfctn")
        @NotNull
        @Valid
        private List<AccountNotification> notification;
        
        public BankToCustomerDebitCreditNotification() {}
        
        public GroupHeader getGroupHeader() {
            return groupHeader;
        }
        
        public void setGroupHeader(GroupHeader groupHeader) {
            this.groupHeader = groupHeader;
        }
        
        public List<AccountNotification> getNotification() {
            return notification;
        }
        
        public void setNotification(List<AccountNotification> notification) {
            this.notification = notification;
        }
    }
    
    /**
     * Group Header for camt.054
     */
    public static class GroupHeader {
        
        @JsonProperty("MsgId")
        @NotNull
        @Size(min = 1, max = 35)
        private String messageId;
        
        @JsonProperty("CreDtTm")
        @NotNull
        private String creationDateTime;
        
        @JsonProperty("MsgRcpt")
        @Valid
        private Party messageRecipient;
        
        @JsonProperty("MsgPgntn")
        @Valid
        private Pagination messagePagination;
        
        @JsonProperty("OrgnlBizQry")
        @Valid
        private OriginalBusinessQuery originalBusinessQuery;
        
        @JsonProperty("AddtlInf")
        @Size(max = 500)
        private String additionalInformation;
        
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
        
        public Party getMessageRecipient() {
            return messageRecipient;
        }
        
        public void setMessageRecipient(Party messageRecipient) {
            this.messageRecipient = messageRecipient;
        }
        
        public Pagination getMessagePagination() {
            return messagePagination;
        }
        
        public void setMessagePagination(Pagination messagePagination) {
            this.messagePagination = messagePagination;
        }
        
        public String getAdditionalInformation() {
            return additionalInformation;
        }
        
        public void setAdditionalInformation(String additionalInformation) {
            this.additionalInformation = additionalInformation;
        }
    }
    
    /**
     * Account Notification
     */
    public static class AccountNotification {
        
        @JsonProperty("Id")
        @NotNull
        @Size(min = 1, max = 35)
        private String identification;
        
        @JsonProperty("NtfctnPgntn")
        @Valid
        private Pagination notificationPagination;
        
        @JsonProperty("ElctrncSeqNb")
        private String electronicSequenceNumber;
        
        @JsonProperty("RptgSeqNb")
        private String reportingSequenceNumber;
        
        @JsonProperty("LglSeqNb")
        private String legalSequenceNumber;
        
        @JsonProperty("CreDtTm")
        private String creationDateTime;
        
        @JsonProperty("FrToDt")
        @Valid
        private DateTimePeriod fromToDate;
        
        @JsonProperty("CpyDplctInd")
        private String copyDuplicateIndicator; // CODU, COPY, DUPL
        
        @JsonProperty("RptgSrc")
        @Valid
        private ReportingSource reportingSource;
        
        @JsonProperty("Acct")
        @NotNull
        @Valid
        private Account account;
        
        @JsonProperty("RltdAcct")
        @Valid
        private Account relatedAccount;
        
        @JsonProperty("Intrst")
        @Valid
        private List<AccountInterest> interest;
        
        @JsonProperty("TxsSummry")
        @Valid
        private TotalTransactions transactionsSummary;
        
        @JsonProperty("Ntry")
        @Valid
        private List<ReportEntry> entry;
        
        @JsonProperty("AddtlNtfctnInf")
        @Size(max = 500)
        private String additionalNotificationInformation;
        
        public AccountNotification() {}
        
        // Getters and Setters
        public String getIdentification() {
            return identification;
        }
        
        public void setIdentification(String identification) {
            this.identification = identification;
        }
        
        public Account getAccount() {
            return account;
        }
        
        public void setAccount(Account account) {
            this.account = account;
        }
        
        public List<ReportEntry> getEntry() {
            return entry;
        }
        
        public void setEntry(List<ReportEntry> entry) {
            this.entry = entry;
        }
        
        public TotalTransactions getTransactionsSummary() {
            return transactionsSummary;
        }
        
        public void setTransactionsSummary(TotalTransactions transactionsSummary) {
            this.transactionsSummary = transactionsSummary;
        }
    }
    
    /**
     * Report Entry
     */
    public static class ReportEntry {
        
        @JsonProperty("NtryRef")
        @Size(max = 35)
        private String entryReference;
        
        @JsonProperty("Amt")
        @NotNull
        @Valid
        private ActiveCurrencyAndAmount amount;
        
        @JsonProperty("CdtDbtInd")
        @NotNull
        private String creditDebitIndicator; // CRDT, DBIT
        
        @JsonProperty("RvslInd")
        private Boolean reversalIndicator;
        
        @JsonProperty("Sts")
        @NotNull
        private String status; // BOOK, PDNG, INFO
        
        @JsonProperty("BookgDt")
        @Valid
        private DateAndDateTime bookingDate;
        
        @JsonProperty("ValDt")
        @Valid
        private DateAndDateTime valueDate;
        
        @JsonProperty("AcctSvcrRef")
        @Size(max = 35)
        private String accountServicerReference;
        
        @JsonProperty("Avlbty")
        @Valid
        private List<CashAvailability> availability;
        
        @JsonProperty("BkTxCd")
        @NotNull
        @Valid
        private BankTransactionCode bankTransactionCode;
        
        @JsonProperty("ComssnWvrInd")
        private Boolean commissionWaiverIndicator;
        
        @JsonProperty("AddtlInfInd")
        @Valid
        private MessageIdentification additionalInformationIndicator;
        
        @JsonProperty("AmtDtls")
        @Valid
        private AmountAndCurrencyExchange amountDetails;
        
        @JsonProperty("Chrgs")
        @Valid
        private List<ChargesInformation> charges;
        
        @JsonProperty("TechInptChanl")
        @Valid
        private TechnicalInputChannel technicalInputChannel;
        
        @JsonProperty("Intrst")
        @Valid
        private TransactionInterest interest;
        
        @JsonProperty("NtryDtls")
        @Valid
        private List<EntryDetails> entryDetails;
        
        @JsonProperty("AddtlNtryInf")
        @Size(max = 500)
        private String additionalEntryInformation;
        
        public ReportEntry() {}
        
        // Getters and Setters (key fields)
        public String getEntryReference() {
            return entryReference;
        }
        
        public void setEntryReference(String entryReference) {
            this.entryReference = entryReference;
        }
        
        public ActiveCurrencyAndAmount getAmount() {
            return amount;
        }
        
        public void setAmount(ActiveCurrencyAndAmount amount) {
            this.amount = amount;
        }
        
        public String getCreditDebitIndicator() {
            return creditDebitIndicator;
        }
        
        public void setCreditDebitIndicator(String creditDebitIndicator) {
            this.creditDebitIndicator = creditDebitIndicator;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public BankTransactionCode getBankTransactionCode() {
            return bankTransactionCode;
        }
        
        public void setBankTransactionCode(BankTransactionCode bankTransactionCode) {
            this.bankTransactionCode = bankTransactionCode;
        }
        
        public List<EntryDetails> getEntryDetails() {
            return entryDetails;
        }
        
        public void setEntryDetails(List<EntryDetails> entryDetails) {
            this.entryDetails = entryDetails;
        }
    }
    
    /**
     * Bank Transaction Code
     */
    public static class BankTransactionCode {
        
        @JsonProperty("Domn")
        @Valid
        private BankTransactionCodeDomain domain;
        
        @JsonProperty("Prtry")
        @Valid
        private ProprietaryBankTransactionCode proprietary;
        
        public BankTransactionCode() {}
        
        public BankTransactionCodeDomain getDomain() {
            return domain;
        }
        
        public void setDomain(BankTransactionCodeDomain domain) {
            this.domain = domain;
        }
        
        public ProprietaryBankTransactionCode getProprietary() {
            return proprietary;
        }
        
        public void setProprietary(ProprietaryBankTransactionCode proprietary) {
            this.proprietary = proprietary;
        }
    }
    
    /**
     * Bank Transaction Code Domain
     */
    public static class BankTransactionCodeDomain {
        
        @JsonProperty("Cd")
        @NotNull
        @Size(max = 4)
        private String code; // PMNT, ACMT, CASH, TRAD
        
        @JsonProperty("Fmly")
        @NotNull
        @Valid
        private BankTransactionCodeFamily family;
        
        public BankTransactionCodeDomain() {}
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public BankTransactionCodeFamily getFamily() {
            return family;
        }
        
        public void setFamily(BankTransactionCodeFamily family) {
            this.family = family;
        }
    }
    
    /**
     * Bank Transaction Code Family
     */
    public static class BankTransactionCodeFamily {
        
        @JsonProperty("Cd")
        @NotNull
        @Size(max = 4)
        private String code; // RCDT, RDDT, ICDT, etc.
        
        @JsonProperty("SubFmlyCd")
        @NotNull
        @Size(max = 4)
        private String subFamilyCode; // BOOK, RLTI, etc.
        
        public BankTransactionCodeFamily() {}
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getSubFamilyCode() {
            return subFamilyCode;
        }
        
        public void setSubFamilyCode(String subFamilyCode) {
            this.subFamilyCode = subFamilyCode;
        }
    }
    
    // Additional supporting classes (abbreviated for space)
    
    public static class Pagination {
        @JsonProperty("PgNb")
        private String pageNumber;
        
        @JsonProperty("LastPgInd")
        private Boolean lastPageIndicator;
        
        public String getPageNumber() { return pageNumber; }
        public void setPageNumber(String pageNumber) { this.pageNumber = pageNumber; }
        public Boolean getLastPageIndicator() { return lastPageIndicator; }
        public void setLastPageIndicator(Boolean lastPageIndicator) { this.lastPageIndicator = lastPageIndicator; }
    }
    
    public static class OriginalBusinessQuery {
        @JsonProperty("MsgId")
        private String messageId;
        
        @JsonProperty("MsgNmId")
        private String messageNameId;
        
        @JsonProperty("CreDtTm")
        private String creationDateTime;
        
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
    }
    
    public static class DateTimePeriod {
        @JsonProperty("FrDtTm")
        private String fromDateTime;
        
        @JsonProperty("ToDtTm")
        private String toDateTime;
        
        public String getFromDateTime() { return fromDateTime; }
        public void setFromDateTime(String fromDateTime) { this.fromDateTime = fromDateTime; }
        public String getToDateTime() { return toDateTime; }
        public void setToDateTime(String toDateTime) { this.toDateTime = toDateTime; }
    }
    
    public static class ReportingSource {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    public static class AccountInterest {
        @JsonProperty("Tp")
        private InterestType type;
        
        @JsonProperty("Rate")
        private List<Rate> rate;
        
        @JsonProperty("FrToDt")
        private DateTimePeriod fromToDate;
        
        @JsonProperty("Rsn")
        private String reason;
        
        @JsonProperty("Tax")
        private TaxCharges tax;
        
        public InterestType getType() { return type; }
        public void setType(InterestType type) { this.type = type; }
    }
    
    public static class InterestType {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    public static class Rate {
        @JsonProperty("Tp")
        private RateType type;
        
        @JsonProperty("VldtyRg")
        private CurrencyAndAmountRange validityRange;
        
        public RateType getType() { return type; }
        public void setType(RateType type) { this.type = type; }
    }
    
    public static class RateType {
        @JsonProperty("Pctg")
        private BigDecimal percentage;
        
        @JsonProperty("Othr")
        private String other;
        
        public BigDecimal getPercentage() { return percentage; }
        public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
    }
    
    public static class TaxCharges {
        @JsonProperty("Id")
        private String identification;
        
        @JsonProperty("Rate")
        private BigDecimal rate;
        
        @JsonProperty("Amt")
        private ActiveCurrencyAndAmount amount;
        
        public String getIdentification() { return identification; }
        public void setIdentification(String identification) { this.identification = identification; }
    }
    
    public static class TotalTransactions {
        @JsonProperty("TtlNtries")
        private NumberAndSumOfTransactions totalEntries;
        
        @JsonProperty("TtlCdtNtries")
        private NumberAndSumOfTransactions totalCreditEntries;
        
        @JsonProperty("TtlDbtNtries") 
        private NumberAndSumOfTransactions totalDebitEntries;
        
        @JsonProperty("TtlNtriesPerBkTxCd")
        private List<TotalsPerBankTransactionCode> totalEntriesPerBankTransactionCode;
        
        public NumberAndSumOfTransactions getTotalEntries() { return totalEntries; }
        public void setTotalEntries(NumberAndSumOfTransactions totalEntries) { this.totalEntries = totalEntries; }
        public NumberAndSumOfTransactions getTotalCreditEntries() { return totalCreditEntries; }
        public void setTotalCreditEntries(NumberAndSumOfTransactions totalCreditEntries) { this.totalCreditEntries = totalCreditEntries; }
        public NumberAndSumOfTransactions getTotalDebitEntries() { return totalDebitEntries; }
        public void setTotalDebitEntries(NumberAndSumOfTransactions totalDebitEntries) { this.totalDebitEntries = totalDebitEntries; }
    }
    
    public static class NumberAndSumOfTransactions {
        @JsonProperty("NbOfNtries")
        private String numberOfEntries;
        
        @JsonProperty("Sum")
        private BigDecimal sum;
        
        @JsonProperty("TtlNetNtry")
        private AmountAndDirection totalNetEntry;
        
        public String getNumberOfEntries() { return numberOfEntries; }
        public void setNumberOfEntries(String numberOfEntries) { this.numberOfEntries = numberOfEntries; }
        public BigDecimal getSum() { return sum; }
        public void setSum(BigDecimal sum) { this.sum = sum; }
    }
    
    public static class AmountAndDirection {
        @JsonProperty("Amt")
        private ActiveCurrencyAndAmount amount;
        
        @JsonProperty("CdtDbtInd")
        private String creditDebitIndicator;
        
        public ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(ActiveCurrencyAndAmount amount) { this.amount = amount; }
        public String getCreditDebitIndicator() { return creditDebitIndicator; }
        public void setCreditDebitIndicator(String creditDebitIndicator) { this.creditDebitIndicator = creditDebitIndicator; }
    }
    
    public static class TotalsPerBankTransactionCode {
        @JsonProperty("NbOfNtries")
        private String numberOfEntries;
        
        @JsonProperty("Sum")
        private BigDecimal sum;
        
        @JsonProperty("TtlNetNtry")
        private AmountAndDirection totalNetEntry;
        
        @JsonProperty("CdtDbtInd")
        private String creditDebitIndicator;
        
        @JsonProperty("FcstInd")
        private Boolean forecastIndicator;
        
        @JsonProperty("BkTxCd")
        private BankTransactionCode bankTransactionCode;
        
        @JsonProperty("Avlbty")
        private List<CashAvailability> availability;
        
        @JsonProperty("Dt")
        private DateAndDateTime date;
        
        public String getNumberOfEntries() { return numberOfEntries; }
        public void setNumberOfEntries(String numberOfEntries) { this.numberOfEntries = numberOfEntries; }
        public BankTransactionCode getBankTransactionCode() { return bankTransactionCode; }
        public void setBankTransactionCode(BankTransactionCode bankTransactionCode) { this.bankTransactionCode = bankTransactionCode; }
    }
    
    public static class EntryDetails {
        @JsonProperty("BtchId")
        private String batchId;
        
        @JsonProperty("TxDtls")
        private List<EntryTransaction> transactionDetails;
        
        public String getBatchId() { return batchId; }
        public void setBatchId(String batchId) { this.batchId = batchId; }
        public List<EntryTransaction> getTransactionDetails() { return transactionDetails; }
        public void setTransactionDetails(List<EntryTransaction> transactionDetails) { this.transactionDetails = transactionDetails; }
    }
    
    public static class EntryTransaction {
        @JsonProperty("Refs")
        private TransactionReferences references;
        
        @JsonProperty("Amt")
        private ActiveCurrencyAndAmount amount;
        
        @JsonProperty("CdtDbtInd")
        private String creditDebitIndicator;
        
        @JsonProperty("AmtDtls")
        private AmountAndCurrencyExchange amountDetails;
        
        @JsonProperty("Avlbty")
        private List<CashAvailability> availability;
        
        @JsonProperty("BkTxCd")
        private BankTransactionCode bankTransactionCode;
        
        @JsonProperty("Chrgs")
        private List<ChargesInformation> charges;
        
        @JsonProperty("Intrst")
        private TransactionInterest interest;
        
        @JsonProperty("RltdPties")
        private TransactionParties relatedParties;
        
        @JsonProperty("RltdAgts")
        private TransactionAgents relatedAgents;
        
        @JsonProperty("LclInstrm")
        private LocalInstrument localInstrument;
        
        @JsonProperty("Purp")
        private Purpose purpose;
        
        @JsonProperty("RltdRmtInf")
        private List<RemittanceLocation> relatedRemittanceInformation;
        
        @JsonProperty("RmtInf")
        private RemittanceInformation remittanceInformation;
        
        @JsonProperty("RltdDts")
        private TransactionDates relatedDates;
        
        @JsonProperty("RltdPric")
        private TransactionPrice relatedPrice;
        
        @JsonProperty("RltdQties")
        private List<TransactionQuantities> relatedQuantities;
        
        @JsonProperty("FinInstrmId")
        private SecurityIdentification financialInstrumentIdentification;
        
        @JsonProperty("Tax")
        private TaxInformation tax;
        
        @JsonProperty("RtrInf")
        private PaymentReturnInformation returnInformation;
        
        @JsonProperty("CorpActn")
        private CorporateAction corporateAction;
        
        @JsonProperty("SfkpgAcct")
        private SecuritiesAccount safekeepingAccount;
        
        @JsonProperty("CshDpst")
        private List<CashDeposit> cashDeposit;
        
        @JsonProperty("CardTx")
        private CardTransaction cardTransaction;
        
        @JsonProperty("AddtlTxInf")
        @Size(max = 500)
        private String additionalTransactionInformation;
        
        @JsonProperty("SplmtryData")
        private List<SupplementaryData> supplementaryData;
        
        public EntryTransaction() {}
        
        // Key getters and setters
        public TransactionReferences getReferences() {
            return references;
        }
        
        public void setReferences(TransactionReferences references) {
            this.references = references;
        }
        
        public ActiveCurrencyAndAmount getAmount() {
            return amount;
        }
        
        public void setAmount(ActiveCurrencyAndAmount amount) {
            this.amount = amount;
        }
        
        public String getCreditDebitIndicator() {
            return creditDebitIndicator;
        }
        
        public void setCreditDebitIndicator(String creditDebitIndicator) {
            this.creditDebitIndicator = creditDebitIndicator;
        }
        
        public TransactionParties getRelatedParties() {
            return relatedParties;
        }
        
        public void setRelatedParties(TransactionParties relatedParties) {
            this.relatedParties = relatedParties;
        }
        
        public RemittanceInformation getRemittanceInformation() {
            return remittanceInformation;
        }
        
        public void setRemittanceInformation(RemittanceInformation remittanceInformation) {
            this.remittanceInformation = remittanceInformation;
        }
    }
    
    // Supporting classes (abbreviated)
    
    public static class TransactionReferences {
        @JsonProperty("MsgId")
        private String messageId;
        
        @JsonProperty("AcctSvcrRef")
        private String accountServicerReference;
        
        @JsonProperty("PmtInfId")
        private String paymentInformationId;
        
        @JsonProperty("InstrId")
        private String instructionId;
        
        @JsonProperty("EndToEndId")
        private String endToEndId;
        
        @JsonProperty("TxId")
        private String transactionId;
        
        @JsonProperty("UETR")
        private String uniqueEndToEndTransactionReference;
        
        @JsonProperty("MndtId")
        private String mandateId;
        
        @JsonProperty("ChqNb")
        private String chequeNumber;
        
        @JsonProperty("ClrSysRef")
        private String clearingSystemReference;
        
        @JsonProperty("AcctOwnrTxId")
        private String accountOwnerTransactionId;
        
        @JsonProperty("AcctSvcrTxId")
        private String accountServicerTransactionId;
        
        @JsonProperty("MktInfrstrctrTxId")
        private String marketInfrastructureTransactionId;
        
        @JsonProperty("PrcgId")
        private String processingId;
        
        @JsonProperty("Prtry")
        private List<ProprietaryReference> proprietary;
        
        // Getters and setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        public String getEndToEndId() { return endToEndId; }
        public void setEndToEndId(String endToEndId) { this.endToEndId = endToEndId; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    }
    
    public static class TransactionParties {
        @JsonProperty("UltmtDbtr")
        private Party ultimateDebtor;
        
        @JsonProperty("Dbtr")
        private Party debtor;
        
        @JsonProperty("DbtrAcct")
        private Account debtorAccount;
        
        @JsonProperty("InitgPty")
        private Party initiatingParty;
        
        @JsonProperty("UltmtCdtr")
        private Party ultimateCreditor;
        
        @JsonProperty("Cdtr")
        private Party creditor;
        
        @JsonProperty("CdtrAcct")
        private Account creditorAccount;
        
        @JsonProperty("TradgPty")
        private Party tradingParty;
        
        @JsonProperty("Prtry")
        private List<ProprietaryParty> proprietary;
        
        // Getters and setters
        public Party getDebtor() { return debtor; }
        public void setDebtor(Party debtor) { this.debtor = debtor; }
        public Account getDebtorAccount() { return debtorAccount; }
        public void setDebtorAccount(Account debtorAccount) { this.debtorAccount = debtorAccount; }
        public Party getCreditor() { return creditor; }
        public void setCreditor(Party creditor) { this.creditor = creditor; }
        public Account getCreditorAccount() { return creditorAccount; }
        public void setCreditorAccount(Account creditorAccount) { this.creditorAccount = creditorAccount; }
    }
    
    public static class TransactionAgents {
        @JsonProperty("InstgAgt")
        private FinancialInstitution instructingAgent;
        
        @JsonProperty("InstdAgt")
        private FinancialInstitution instructedAgent;
        
        @JsonProperty("DbtrAgt")
        private FinancialInstitution debtorAgent;
        
        @JsonProperty("CdtrAgt")
        private FinancialInstitution creditorAgent;
        
        @JsonProperty("IntrmyAgt1")
        private FinancialInstitution intermediaryAgent1;
        
        @JsonProperty("IntrmyAgt2")
        private FinancialInstitution intermediaryAgent2;
        
        @JsonProperty("IntrmyAgt3")
        private FinancialInstitution intermediaryAgent3;
        
        @JsonProperty("RcvgAgt")
        private FinancialInstitution receivingAgent;
        
        @JsonProperty("DlvrgAgt")
        private FinancialInstitution deliveringAgent;
        
        @JsonProperty("IssgAgt")
        private FinancialInstitution issuingAgent;
        
        @JsonProperty("SttlmPlc")
        private FinancialInstitution settlementPlace;
        
        @JsonProperty("Prtry")
        private List<ProprietaryAgent> proprietary;
        
        // Getters and setters
        public FinancialInstitution getDebtorAgent() { return debtorAgent; }
        public void setDebtorAgent(FinancialInstitution debtorAgent) { this.debtorAgent = debtorAgent; }
        public FinancialInstitution getCreditorAgent() { return creditorAgent; }
        public void setCreditorAgent(FinancialInstitution creditorAgent) { this.creditorAgent = creditorAgent; }
    }
    
    // Additional supporting classes would continue here...
    // Abbreviated for space but following the same pattern
    
    public static class ProprietaryReference {
        @JsonProperty("Tp")
        private String type;
        
        @JsonProperty("Ref")
        private String reference;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
    }
    
    public static class ProprietaryParty {
        @JsonProperty("Tp")
        private String type;
        
        @JsonProperty("Pty")
        private Party party;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Party getParty() { return party; }
        public void setParty(Party party) { this.party = party; }
    }
    
    public static class ProprietaryAgent {
        @JsonProperty("Tp")
        private String type;
        
        @JsonProperty("Agt")
        private FinancialInstitution agent;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public FinancialInstitution getAgent() { return agent; }
        public void setAgent(FinancialInstitution agent) { this.agent = agent; }
    }
    
    public static class ProprietaryBankTransactionCode {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Issr")
        private String issuer;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
    }
    
    public static class DateAndDateTime {
        @JsonProperty("Dt")
        private String date;
        
        @JsonProperty("DtTm")
        private String dateTime;
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getDateTime() { return dateTime; }
        public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    }
    
    public static class CashAvailability {
        @JsonProperty("Dt")
        private CashAvailabilityDate date;
        
        @JsonProperty("Amt")
        private ActiveCurrencyAndAmount amount;
        
        @JsonProperty("CdtDbtInd")
        private String creditDebitIndicator;
        
        public CashAvailabilityDate getDate() { return date; }
        public void setDate(CashAvailabilityDate date) { this.date = date; }
        public ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(ActiveCurrencyAndAmount amount) { this.amount = amount; }
    }
    
    public static class CashAvailabilityDate {
        @JsonProperty("NbOfDays")
        private String numberOfDays;
        
        @JsonProperty("ActlDt")
        private String actualDate;
        
        public String getNumberOfDays() { return numberOfDays; }
        public void setNumberOfDays(String numberOfDays) { this.numberOfDays = numberOfDays; }
        public String getActualDate() { return actualDate; }
        public void setActualDate(String actualDate) { this.actualDate = actualDate; }
    }
    
    public static class AmountAndCurrencyExchange {
        @JsonProperty("InstdAmt")
        private ActiveCurrencyAndAmount instructedAmount;
        
        @JsonProperty("TxAmt")
        private ActiveCurrencyAndAmount transactionAmount;
        
        @JsonProperty("CntrValAmt")
        private ActiveCurrencyAndAmount counterValueAmount;
        
        @JsonProperty("AnncdPstngAmt")
        private ActiveCurrencyAndAmount announcedPostingAmount;
        
        @JsonProperty("PrtryAmt")
        private List<ProprietaryAmount> proprietaryAmount;
        
        public ActiveCurrencyAndAmount getInstructedAmount() { return instructedAmount; }
        public void setInstructedAmount(ActiveCurrencyAndAmount instructedAmount) { this.instructedAmount = instructedAmount; }
        public ActiveCurrencyAndAmount getTransactionAmount() { return transactionAmount; }
        public void setTransactionAmount(ActiveCurrencyAndAmount transactionAmount) { this.transactionAmount = transactionAmount; }
    }
    
    public static class ProprietaryAmount {
        @JsonProperty("Tp")
        private String type;
        
        @JsonProperty("Amt")
        private ActiveCurrencyAndAmount amount;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(ActiveCurrencyAndAmount amount) { this.amount = amount; }
    }
    
    public static class TechnicalInputChannel {
        @JsonProperty("Cd")
        private String code; // FAXE, FILE, ONLI, POST
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getProprietary() { return proprietary; }
        public void setProprietary(String proprietary) { this.proprietary = proprietary; }
    }
    
    public static class TransactionInterest {
        @JsonProperty("TtlIntrstAndTaxAmt")
        private ActiveCurrencyAndAmount totalInterestAndTaxAmount;
        
        @JsonProperty("Rcrd")
        private List<InterestRecord> record;
        
        public ActiveCurrencyAndAmount getTotalInterestAndTaxAmount() { return totalInterestAndTaxAmount; }
        public void setTotalInterestAndTaxAmount(ActiveCurrencyAndAmount totalInterestAndTaxAmount) { this.totalInterestAndTaxAmount = totalInterestAndTaxAmount; }
        public List<InterestRecord> getRecord() { return record; }
        public void setRecord(List<InterestRecord> record) { this.record = record; }
    }
    
    public static class InterestRecord {
        @JsonProperty("Amt")
        private ActiveCurrencyAndAmount amount;
        
        @JsonProperty("CdtDbtInd")
        private String creditDebitIndicator;
        
        @JsonProperty("Tp")
        private InterestType type;
        
        @JsonProperty("Rate")
        private Rate rate;
        
        @JsonProperty("FrToDt")
        private DateTimePeriod fromToDate;
        
        @JsonProperty("Rsn")
        private String reason;
        
        @JsonProperty("Tax")
        private TaxCharges tax;
        
        public ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(ActiveCurrencyAndAmount amount) { this.amount = amount; }
        public String getCreditDebitIndicator() { return creditDebitIndicator; }
        public void setCreditDebitIndicator(String creditDebitIndicator) { this.creditDebitIndicator = creditDebitIndicator; }
    }
    
    public static class TransactionDates {
        @JsonProperty("AccptncDtTm")
        private String acceptanceDateTime;
        
        @JsonProperty("TradActvtyCtrctlSttlmDt")
        private String tradeActivityContractualSettlementDate;
        
        @JsonProperty("TradDt")
        private String tradeDate;
        
        @JsonProperty("IntrBkSttlmDt")
        private String interbankSettlementDate;
        
        @JsonProperty("StartDt")
        private String startDate;
        
        @JsonProperty("EndDt")
        private String endDate;
        
        @JsonProperty("TxDtTm")
        private String transactionDateTime;
        
        @JsonProperty("Prtry")
        private List<ProprietaryDate> proprietary;
        
        public String getAcceptanceDateTime() { return acceptanceDateTime; }
        public void setAcceptanceDateTime(String acceptanceDateTime) { this.acceptanceDateTime = acceptanceDateTime; }
        public String getTransactionDateTime() { return transactionDateTime; }
        public void setTransactionDateTime(String transactionDateTime) { this.transactionDateTime = transactionDateTime; }
    }
    
    public static class ProprietaryDate {
        @JsonProperty("Tp")
        private String type;
        
        @JsonProperty("Dt")
        private DateAndDateTime date;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public DateAndDateTime getDate() { return date; }
        public void setDate(DateAndDateTime date) { this.date = date; }
    }
    
    public static class TransactionPrice {
        @JsonProperty("DealPric")
        private Price dealPrice;
        
        @JsonProperty("Prtry")
        private List<ProprietaryPrice> proprietary;
        
        public Price getDealPrice() { return dealPrice; }
        public void setDealPrice(Price dealPrice) { this.dealPrice = dealPrice; }
    }
    
    public static class Price {
        @JsonProperty("Tp")
        private PriceType type;
        
        @JsonProperty("Val")
        private PriceRateOrAmount value;
        
        public PriceType getType() { return type; }
        public void setType(PriceType type) { this.type = type; }
        public PriceRateOrAmount getValue() { return value; }
        public void setValue(PriceRateOrAmount value) { this.value = value; }
    }
    
    public static class PriceType {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private GenericIdentification proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    public static class PriceRateOrAmount {
        @JsonProperty("Rate")
        private BigDecimal rate;
        
        @JsonProperty("Amt")
        private ActiveCurrencyAndAmount amount;
        
        public BigDecimal getRate() { return rate; }
        public void setRate(BigDecimal rate) { this.rate = rate; }
        public ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(ActiveCurrencyAndAmount amount) { this.amount = amount; }
    }
    
    public static class ProprietaryPrice {
        @JsonProperty("Tp")
        private String type;
        
        @JsonProperty("Pric")
        private Price price;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Price getPrice() { return price; }
        public void setPrice(Price price) { this.price = price; }
    }
    
    public static class TransactionQuantities {
        @JsonProperty("Qty")
        private FinancialInstrumentQuantity quantity;
        
        @JsonProperty("Unit")
        private String unit;
        
        public FinancialInstrumentQuantity getQuantity() { return quantity; }
        public void setQuantity(FinancialInstrumentQuantity quantity) { this.quantity = quantity; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }
    
    public static class FinancialInstrumentQuantity {
        @JsonProperty("Unit")
        private BigDecimal unit;
        
        @JsonProperty("FaceAmt")
        private BigDecimal faceAmount;
        
        @JsonProperty("AmtsdVal")
        private BigDecimal amortisedValue;
        
        public BigDecimal getUnit() { return unit; }
        public void setUnit(BigDecimal unit) { this.unit = unit; }
    }
    
    public static class SecurityIdentification {
        @JsonProperty("ISIN")
        private String isin;
        
        @JsonProperty("Othr")
        private List<OtherIdentification> other;
        
        @JsonProperty("Desc")
        private String description;
        
        public String getIsin() { return isin; }
        public void setIsin(String isin) { this.isin = isin; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    public static class OtherIdentification {
        @JsonProperty("Id")
        private String identification;
        
        @JsonProperty("Sfx")
        private String suffix;
        
        @JsonProperty("Tp")
        private IdentificationType type;
        
        public String getIdentification() { return identification; }
        public void setIdentification(String identification) { this.identification = identification; }
    }
    
    public static class IdentificationType {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    public static class PaymentReturnInformation {
        @JsonProperty("OrgnlBkTxCd")
        private BankTransactionCode originalBankTransactionCode;
        
        @JsonProperty("Orgtr")
        private PartyIdentification originator;
        
        @JsonProperty("Rsn")
        private ReturnReason reason;
        
        @JsonProperty("AddtlInf")
        private List<String> additionalInformation;
        
        public BankTransactionCode getOriginalBankTransactionCode() { return originalBankTransactionCode; }
        public void setOriginalBankTransactionCode(BankTransactionCode originalBankTransactionCode) { this.originalBankTransactionCode = originalBankTransactionCode; }
        public ReturnReason getReason() { return reason; }
        public void setReason(ReturnReason reason) { this.reason = reason; }
    }
    
    public static class ReturnReason {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getProprietary() { return proprietary; }
        public void setProprietary(String proprietary) { this.proprietary = proprietary; }
    }
    
    public static class CorporateAction {
        @JsonProperty("EvtTp")
        private CorporateActionEventType eventType;
        
        @JsonProperty("EvtId")
        private String eventId;
        
        public CorporateActionEventType getEventType() { return eventType; }
        public void setEventType(CorporateActionEventType eventType) { this.eventType = eventType; }
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
    }
    
    public static class CorporateActionEventType {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    public static class SecuritiesAccount {
        @JsonProperty("Id")
        private String identification;
        
        @JsonProperty("Tp")
        private GenericIdentification type;
        
        @JsonProperty("Nm")
        private String name;
        
        public String getIdentification() { return identification; }
        public void setIdentification(String identification) { this.identification = identification; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    public static class CashDeposit {
        @JsonProperty("NoteDnmtn")
        private ActiveCurrencyAndAmount noteDenomination;
        
        @JsonProperty("NbOfNotes")
        private String numberOfNotes;
        
        @JsonProperty("Amt")
        private ActiveCurrencyAndAmount amount;
        
        public ActiveCurrencyAndAmount getNoteDenomination() { return noteDenomination; }
        public void setNoteDenomination(ActiveCurrencyAndAmount noteDenomination) { this.noteDenomination = noteDenomination; }
        public String getNumberOfNotes() { return numberOfNotes; }
        public void setNumberOfNotes(String numberOfNotes) { this.numberOfNotes = numberOfNotes; }
        public ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(ActiveCurrencyAndAmount amount) { this.amount = amount; }
    }
    
    public static class CardTransaction {
        @JsonProperty("Card")
        private PaymentCard card;
        
        @JsonProperty("POI")
        private PointOfInteraction pointOfInteraction;
        
        @JsonProperty("Tx")
        private CardTransactionDetail transaction;
        
        @JsonProperty("PrePdAcct")
        private CashAccount prepaidAccount;
        
        public PaymentCard getCard() { return card; }
        public void setCard(PaymentCard card) { this.card = card; }
        public PointOfInteraction getPointOfInteraction() { return pointOfInteraction; }
        public void setPointOfInteraction(PointOfInteraction pointOfInteraction) { this.pointOfInteraction = pointOfInteraction; }
    }
    
    // Additional classes would continue following the same pattern...
    // This represents the core structure of pacs.008 message
}