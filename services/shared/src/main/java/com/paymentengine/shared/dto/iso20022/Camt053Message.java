package com.paymentengine.shared.dto.iso20022;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * ISO 20022 camt.053 - Bank to Customer Statement
 * Used for providing account statements and balance information
 */
public class Camt053Message {
    
    @JsonProperty("BkToCstmrStmt")
    @NotNull
    @Valid
    private BankToCustomerStatement bankToCustomerStatement;
    
    public Camt053Message() {}
    
    public Camt053Message(BankToCustomerStatement bankToCustomerStatement) {
        this.bankToCustomerStatement = bankToCustomerStatement;
    }
    
    public BankToCustomerStatement getBankToCustomerStatement() {
        return bankToCustomerStatement;
    }
    
    public void setBankToCustomerStatement(BankToCustomerStatement bankToCustomerStatement) {
        this.bankToCustomerStatement = bankToCustomerStatement;
    }
    
    /**
     * Bank to Customer Statement
     */
    public static class BankToCustomerStatement {
        
        @JsonProperty("GrpHdr")
        @NotNull
        @Valid
        private GroupHeader groupHeader;
        
        @JsonProperty("Stmt")
        @NotNull
        @Valid
        private List<AccountStatement> statement;
        
        public BankToCustomerStatement() {}
        
        public GroupHeader getGroupHeader() {
            return groupHeader;
        }
        
        public void setGroupHeader(GroupHeader groupHeader) {
            this.groupHeader = groupHeader;
        }
        
        public List<AccountStatement> getStatement() {
            return statement;
        }
        
        public void setStatement(List<AccountStatement> statement) {
            this.statement = statement;
        }
    }
    
    /**
     * Group Header for camt.053
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
    }
    
    /**
     * Account Statement
     */
    public static class AccountStatement {
        
        @JsonProperty("Id")
        @NotNull
        @Size(min = 1, max = 35)
        private String identification;
        
        @JsonProperty("StmtPgntn")
        @Valid
        private Pagination statementPagination;
        
        @JsonProperty("ElctrncSeqNb")
        private String electronicSequenceNumber;
        
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
        
        @JsonProperty("Bal")
        @NotNull
        @Valid
        private List<CashBalance> balance;
        
        @JsonProperty("TxsSummry")
        @Valid
        private TotalTransactions transactionsSummary;
        
        @JsonProperty("Ntry")
        @Valid
        private List<ReportEntry> entry;
        
        @JsonProperty("AddtlStmtInf")
        @Size(max = 500)
        private String additionalStatementInformation;
        
        public AccountStatement() {}
        
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
        
        public List<CashBalance> getBalance() {
            return balance;
        }
        
        public void setBalance(List<CashBalance> balance) {
            this.balance = balance;
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
     * Cash Balance
     */
    public static class CashBalance {
        
        @JsonProperty("Tp")
        @NotNull
        @Valid
        private BalanceType type;
        
        @JsonProperty("CdtLine")
        @Valid
        private List<CreditLine> creditLine;
        
        @JsonProperty("Amt")
        @NotNull
        @Valid
        private ActiveCurrencyAndAmount amount;
        
        @JsonProperty("CdtDbtInd")
        @NotNull
        private String creditDebitIndicator; // CRDT, DBIT
        
        @JsonProperty("Dt")
        @NotNull
        @Valid
        private DateAndDateTime date;
        
        @JsonProperty("Avlbty")
        @Valid
        private List<CashAvailability> availability;
        
        public CashBalance() {}
        
        // Getters and Setters
        public BalanceType getType() {
            return type;
        }
        
        public void setType(BalanceType type) {
            this.type = type;
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
        
        public DateAndDateTime getDate() {
            return date;
        }
        
        public void setDate(DateAndDateTime date) {
            this.date = date;
        }
        
        public List<CashAvailability> getAvailability() {
            return availability;
        }
        
        public void setAvailability(List<CashAvailability> availability) {
            this.availability = availability;
        }
    }
    
    /**
     * Balance Type
     */
    public static class BalanceType {
        
        @JsonProperty("CdOrPrtry")
        @NotNull
        @Valid
        private BalanceTypeChoice codeOrProprietary;
        
        @JsonProperty("SubTp")
        @Valid
        private BalanceSubType subType;
        
        public BalanceType() {}
        
        public BalanceTypeChoice getCodeOrProprietary() {
            return codeOrProprietary;
        }
        
        public void setCodeOrProprietary(BalanceTypeChoice codeOrProprietary) {
            this.codeOrProprietary = codeOrProprietary;
        }
        
        public BalanceSubType getSubType() {
            return subType;
        }
        
        public void setSubType(BalanceSubType subType) {
            this.subType = subType;
        }
    }
    
    /**
     * Balance Type Choice
     */
    public static class BalanceTypeChoice {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code; // OPBD, CLBD, BOBD, ITBD, OTHR
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public BalanceTypeChoice() {}
        
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
     * Balance Sub Type
     */
    public static class BalanceSubType {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code;
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public BalanceSubType() {}
        
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
     * Credit Line
     */
    public static class CreditLine {
        
        @JsonProperty("Incl")
        @NotNull
        private Boolean included;
        
        @JsonProperty("Tp")
        @Valid
        private CreditLineType type;
        
        @JsonProperty("Amt")
        @Valid
        private ActiveCurrencyAndAmount amount;
        
        public CreditLine() {}
        
        public Boolean getIncluded() {
            return included;
        }
        
        public void setIncluded(Boolean included) {
            this.included = included;
        }
        
        public CreditLineType getType() {
            return type;
        }
        
        public void setType(CreditLineType type) {
            this.type = type;
        }
        
        public ActiveCurrencyAndAmount getAmount() {
            return amount;
        }
        
        public void setAmount(ActiveCurrencyAndAmount amount) {
            this.amount = amount;
        }
    }
    
    /**
     * Credit Line Type
     */
    public static class CreditLineType {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code; // PRE, CRED
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public CreditLineType() {}
        
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
    
    // Supporting classes (reusing from camt.054 where applicable)
    
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
    
    public static class CurrencyAndAmountRange {
        @JsonProperty("Amt")
        private ImpliedCurrencyAmountRange amount;
        
        @JsonProperty("CdtDbtInd")
        private String creditDebitIndicator;
        
        @JsonProperty("Ccy")
        private String currency;
        
        public ImpliedCurrencyAmountRange getAmount() { return amount; }
        public void setAmount(ImpliedCurrencyAmountRange amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
    
    public static class ImpliedCurrencyAmountRange {
        @JsonProperty("FrAmt")
        private BigDecimal fromAmount;
        
        @JsonProperty("ToAmt")
        private BigDecimal toAmount;
        
        public BigDecimal getFromAmount() { return fromAmount; }
        public void setFromAmount(BigDecimal fromAmount) { this.fromAmount = fromAmount; }
        public BigDecimal getToAmount() { return toAmount; }
        public void setToAmount(BigDecimal toAmount) { this.toAmount = toAmount; }
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
        
        @JsonProperty("BkTxCd")
        private BankTransactionCode bankTransactionCode;
        
        @JsonProperty("Avlbty")
        private List<CashAvailability> availability;
        
        public String getNumberOfEntries() { return numberOfEntries; }
        public void setNumberOfEntries(String numberOfEntries) { this.numberOfEntries = numberOfEntries; }
        public BankTransactionCode getBankTransactionCode() { return bankTransactionCode; }
        public void setBankTransactionCode(BankTransactionCode bankTransactionCode) { this.bankTransactionCode = bankTransactionCode; }
    }
    
    public static class ReportEntry {
        @JsonProperty("NtryRef")
        private String entryReference;
        
        @JsonProperty("Amt")
        @NotNull
        private ActiveCurrencyAndAmount amount;
        
        @JsonProperty("CdtDbtInd")
        @NotNull
        private String creditDebitIndicator;
        
        @JsonProperty("RvslInd")
        private Boolean reversalIndicator;
        
        @JsonProperty("Sts")
        @NotNull
        private String status; // BOOK, PDNG, INFO
        
        @JsonProperty("BookgDt")
        private DateAndDateTime bookingDate;
        
        @JsonProperty("ValDt")
        private DateAndDateTime valueDate;
        
        @JsonProperty("AcctSvcrRef")
        private String accountServicerReference;
        
        @JsonProperty("BkTxCd")
        @NotNull
        private BankTransactionCode bankTransactionCode;
        
        @JsonProperty("NtryDtls")
        private List<EntryDetails> entryDetails;
        
        @JsonProperty("AddtlNtryInf")
        private String additionalEntryInformation;
        
        // Getters and setters
        public String getEntryReference() { return entryReference; }
        public void setEntryReference(String entryReference) { this.entryReference = entryReference; }
        public ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(ActiveCurrencyAndAmount amount) { this.amount = amount; }
        public String getCreditDebitIndicator() { return creditDebitIndicator; }
        public void setCreditDebitIndicator(String creditDebitIndicator) { this.creditDebitIndicator = creditDebitIndicator; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BankTransactionCode getBankTransactionCode() { return bankTransactionCode; }
        public void setBankTransactionCode(BankTransactionCode bankTransactionCode) { this.bankTransactionCode = bankTransactionCode; }
        public List<EntryDetails> getEntryDetails() { return entryDetails; }
        public void setEntryDetails(List<EntryDetails> entryDetails) { this.entryDetails = entryDetails; }
    }
    
    public static class BankTransactionCode {
        @JsonProperty("Domn")
        private BankTransactionCodeDomain domain;
        
        @JsonProperty("Prtry")
        private ProprietaryBankTransactionCode proprietary;
        
        public BankTransactionCodeDomain getDomain() { return domain; }
        public void setDomain(BankTransactionCodeDomain domain) { this.domain = domain; }
        public ProprietaryBankTransactionCode getProprietary() { return proprietary; }
        public void setProprietary(ProprietaryBankTransactionCode proprietary) { this.proprietary = proprietary; }
    }
    
    public static class BankTransactionCodeDomain {
        @JsonProperty("Cd")
        @NotNull
        private String code; // PMNT, ACMT, CASH, TRAD
        
        @JsonProperty("Fmly")
        @NotNull
        private BankTransactionCodeFamily family;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public BankTransactionCodeFamily getFamily() { return family; }
        public void setFamily(BankTransactionCodeFamily family) { this.family = family; }
    }
    
    public static class BankTransactionCodeFamily {
        @JsonProperty("Cd")
        @NotNull
        private String code; // RCDT, RDDT, ICDT, etc.
        
        @JsonProperty("SubFmlyCd")
        @NotNull
        private String subFamilyCode; // BOOK, RLTI, etc.
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getSubFamilyCode() { return subFamilyCode; }
        public void setSubFamilyCode(String subFamilyCode) { this.subFamilyCode = subFamilyCode; }
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
        
        @JsonProperty("BkTxCd")
        private BankTransactionCode bankTransactionCode;
        
        @JsonProperty("RltdPties")
        private TransactionParties relatedParties;
        
        @JsonProperty("RltdAgts")
        private TransactionAgents relatedAgents;
        
        @JsonProperty("Purp")
        private Purpose purpose;
        
        @JsonProperty("RmtInf")
        private RemittanceInformation remittanceInformation;
        
        @JsonProperty("AddtlTxInf")
        private String additionalTransactionInformation;
        
        // Getters and setters
        public TransactionReferences getReferences() { return references; }
        public void setReferences(TransactionReferences references) { this.references = references; }
        public ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(ActiveCurrencyAndAmount amount) { this.amount = amount; }
        public String getCreditDebitIndicator() { return creditDebitIndicator; }
        public void setCreditDebitIndicator(String creditDebitIndicator) { this.creditDebitIndicator = creditDebitIndicator; }
        public TransactionParties getRelatedParties() { return relatedParties; }
        public void setRelatedParties(TransactionParties relatedParties) { this.relatedParties = relatedParties; }
        public RemittanceInformation getRemittanceInformation() { return remittanceInformation; }
        public void setRemittanceInformation(RemittanceInformation remittanceInformation) { this.remittanceInformation = remittanceInformation; }
    }
    
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
        
        @JsonProperty("ClrSysRef")
        private String clearingSystemReference;
        
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
        
        @JsonProperty("UltmtCdtr")
        private Party ultimateCreditor;
        
        @JsonProperty("Cdtr")
        private Party creditor;
        
        @JsonProperty("CdtrAcct")
        private Account creditorAccount;
        
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
        
        public FinancialInstitution getDebtorAgent() { return debtorAgent; }
        public void setDebtorAgent(FinancialInstitution debtorAgent) { this.debtorAgent = debtorAgent; }
        public FinancialInstitution getCreditorAgent() { return creditorAgent; }
        public void setCreditorAgent(FinancialInstitution creditorAgent) { this.creditorAgent = creditorAgent; }
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
    }
    
    public static class ReferredDocumentTypeChoice {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
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
    
    public static class SupplementaryData {
        @JsonProperty("PlcAndNm")
        private String placeAndName;
        
        @JsonProperty("Envlp")
        @NotNull
        private Object envelope;
        
        public String getPlaceAndName() { return placeAndName; }
        public void setPlaceAndName(String placeAndName) { this.placeAndName = placeAndName; }
        public Object getEnvelope() { return envelope; }
        public void setEnvelope(Object envelope) { this.envelope = envelope; }
    }
}