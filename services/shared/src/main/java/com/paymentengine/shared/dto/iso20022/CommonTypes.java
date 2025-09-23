package com.paymentengine.shared.dto.iso20022;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Common types used across ISO 20022 messages
 */
public class CommonTypes {
    
    /**
     * Generic Organisation Identification
     */
    public static class GenericOrganisationIdentification {
        
        @JsonProperty("Id")
        @NotNull
        @Size(min = 1, max = 35)
        private String identification;
        
        @JsonProperty("SchmeNm")
        private SchemeName schemeName;
        
        @JsonProperty("Issr")
        @Size(max = 35)
        private String issuer;
        
        public GenericOrganisationIdentification() {}
        
        // Getters and Setters
        public String getIdentification() {
            return identification;
        }
        
        public void setIdentification(String identification) {
            this.identification = identification;
        }
        
        public SchemeName getSchemeName() {
            return schemeName;
        }
        
        public void setSchemeName(SchemeName schemeName) {
            this.schemeName = schemeName;
        }
        
        public String getIssuer() {
            return issuer;
        }
        
        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }
    
    /**
     * Generic Person Identification
     */
    public static class GenericPersonIdentification {
        
        @JsonProperty("Id")
        @NotNull
        @Size(min = 1, max = 35)
        private String identification;
        
        @JsonProperty("SchmeNm")
        private SchemeName schemeName;
        
        @JsonProperty("Issr")
        @Size(max = 35)
        private String issuer;
        
        public GenericPersonIdentification() {}
        
        // Getters and Setters
        public String getIdentification() {
            return identification;
        }
        
        public void setIdentification(String identification) {
            this.identification = identification;
        }
        
        public SchemeName getSchemeName() {
            return schemeName;
        }
        
        public void setSchemeName(SchemeName schemeName) {
            this.schemeName = schemeName;
        }
        
        public String getIssuer() {
            return issuer;
        }
        
        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }
    
    /**
     * Generic Financial Identification
     */
    public static class GenericFinancialIdentification {
        
        @JsonProperty("Id")
        @NotNull
        @Size(min = 1, max = 35)
        private String identification;
        
        @JsonProperty("SchmeNm")
        private SchemeName schemeName;
        
        @JsonProperty("Issr")
        @Size(max = 35)
        private String issuer;
        
        public GenericFinancialIdentification() {}
        
        // Getters and Setters
        public String getIdentification() {
            return identification;
        }
        
        public void setIdentification(String identification) {
            this.identification = identification;
        }
        
        public SchemeName getSchemeName() {
            return schemeName;
        }
        
        public void setSchemeName(SchemeName schemeName) {
            this.schemeName = schemeName;
        }
        
        public String getIssuer() {
            return issuer;
        }
        
        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }
    
    /**
     * Scheme Name
     */
    public static class SchemeName {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code;
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public SchemeName() {}
        
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
     * Date and Place of Birth
     */
    public static class DateAndPlaceOfBirth {
        
        @JsonProperty("BirthDt")
        @NotNull
        private String birthDate; // YYYY-MM-DD
        
        @JsonProperty("PrvcOfBirth")
        @Size(max = 35)
        private String provinceOfBirth;
        
        @JsonProperty("CityOfBirth")
        @NotNull
        @Size(min = 1, max = 35)
        private String cityOfBirth;
        
        @JsonProperty("CtryOfBirth")
        @NotNull
        @Size(min = 2, max = 2)
        private String countryOfBirth;
        
        public DateAndPlaceOfBirth() {}
        
        // Getters and Setters
        public String getBirthDate() {
            return birthDate;
        }
        
        public void setBirthDate(String birthDate) {
            this.birthDate = birthDate;
        }
        
        public String getProvinceOfBirth() {
            return provinceOfBirth;
        }
        
        public void setProvinceOfBirth(String provinceOfBirth) {
            this.provinceOfBirth = provinceOfBirth;
        }
        
        public String getCityOfBirth() {
            return cityOfBirth;
        }
        
        public void setCityOfBirth(String cityOfBirth) {
            this.cityOfBirth = cityOfBirth;
        }
        
        public String getCountryOfBirth() {
            return countryOfBirth;
        }
        
        public void setCountryOfBirth(String countryOfBirth) {
            this.countryOfBirth = countryOfBirth;
        }
    }
    
    /**
     * Purpose
     */
    public static class Purpose {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code; // CBFF, CHAR, CORT, INTC, etc.
        
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
     * Regulatory Reporting
     */
    public static class RegulatoryReporting {
        
        @JsonProperty("DbtCdtRptgInd")
        private String debitCreditReportingIndicator; // DEBT, CRED, BOTH
        
        @JsonProperty("Authrty")
        private RegulatoryAuthority authority;
        
        @JsonProperty("Dtls")
        private StructuredRegulatoryReporting[] details;
        
        public RegulatoryReporting() {}
        
        // Getters and Setters
        public String getDebitCreditReportingIndicator() {
            return debitCreditReportingIndicator;
        }
        
        public void setDebitCreditReportingIndicator(String debitCreditReportingIndicator) {
            this.debitCreditReportingIndicator = debitCreditReportingIndicator;
        }
        
        public RegulatoryAuthority getAuthority() {
            return authority;
        }
        
        public void setAuthority(RegulatoryAuthority authority) {
            this.authority = authority;
        }
        
        public StructuredRegulatoryReporting[] getDetails() {
            return details;
        }
        
        public void setDetails(StructuredRegulatoryReporting[] details) {
            this.details = details;
        }
    }
    
    /**
     * Tax Information
     */
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
        private Amount totalTaxableBaseAmount;
        
        @JsonProperty("TtlTaxAmt")
        private Amount totalTaxAmount;
        
        @JsonProperty("Dt")
        private String date;
        
        @JsonProperty("SeqNb")
        private String sequenceNumber;
        
        @JsonProperty("Rcrd")
        private TaxRecord[] record;
        
        public TaxInformation() {}
        
        // Getters and Setters (abbreviated for brevity)
        public TaxParty getCreditor() {
            return creditor;
        }
        
        public void setCreditor(TaxParty creditor) {
            this.creditor = creditor;
        }
        
        public TaxParty getDebtor() {
            return debtor;
        }
        
        public void setDebtor(TaxParty debtor) {
            this.debtor = debtor;
        }
        
        public String getReferenceNumber() {
            return referenceNumber;
        }
        
        public void setReferenceNumber(String referenceNumber) {
            this.referenceNumber = referenceNumber;
        }
    }
    
    /**
     * Remittance Information
     */
    public static class RemittanceInformation {
        
        @JsonProperty("Ustrd")
        @Size(max = 140)
        private String[] unstructured;
        
        @JsonProperty("Strd")
        private StructuredRemittanceInformation[] structured;
        
        public RemittanceInformation() {}
        
        public String[] getUnstructured() {
            return unstructured;
        }
        
        public void setUnstructured(String[] unstructured) {
            this.unstructured = unstructured;
        }
        
        public StructuredRemittanceInformation[] getStructured() {
            return structured;
        }
        
        public void setStructured(StructuredRemittanceInformation[] structured) {
            this.structured = structured;
        }
    }
    
    // Additional supporting classes would be defined here...
    // For brevity, showing the main structure
    
    public static class RegulatoryAuthority {
        @JsonProperty("Nm")
        private String name;
        
        @JsonProperty("Ctry")
        private String country;
        
        // Getters and setters...
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }
    
    public static class StructuredRegulatoryReporting {
        @JsonProperty("Tp")
        private String type;
        
        @JsonProperty("Dt")
        private String date;
        
        @JsonProperty("Ctry")
        private String country;
        
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Amt")
        private Amount amount;
        
        @JsonProperty("Inf")
        private String[] information;
        
        // Getters and setters...
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
    
    public static class TaxParty {
        @JsonProperty("TaxId")
        private String taxId;
        
        @JsonProperty("RegnId")
        private String registrationId;
        
        @JsonProperty("TaxTp")
        private String taxType;
        
        // Getters and setters...
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
        
        // Getters and setters...
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
        
        // Getters and setters...
        public String getYear() { return year; }
        public void setYear(String year) { this.year = year; }
    }
    
    public static class TaxAmount {
        @JsonProperty("Rate")
        private String rate;
        
        @JsonProperty("TaxblBaseAmt")
        private Amount taxableBaseAmount;
        
        @JsonProperty("TtlAmt")
        private Amount totalAmount;
        
        // Getters and setters...
        public String getRate() { return rate; }
        public void setRate(String rate) { this.rate = rate; }
    }
    
    public static class DatePeriod {
        @JsonProperty("FrDt")
        private String fromDate;
        
        @JsonProperty("ToDt")
        private String toDate;
        
        // Getters and setters...
        public String getFromDate() { return fromDate; }
        public void setFromDate(String fromDate) { this.fromDate = fromDate; }
        public String getToDate() { return toDate; }
        public void setToDate(String toDate) { this.toDate = toDate; }
    }
    
    public static class StructuredRemittanceInformation {
        @JsonProperty("RfrdDocInf")
        private ReferredDocumentInformation[] referredDocumentInformation;
        
        @JsonProperty("RfrdDocAmt")
        private RemittanceAmount referredDocumentAmount;
        
        @JsonProperty("CdtrRefInf")
        private CreditorReferenceInformation creditorReferenceInformation;
        
        @JsonProperty("Invcr")
        private PartyIdentification invoicer;
        
        @JsonProperty("Invcee")
        private PartyIdentification invoicee;
        
        @JsonProperty("TaxRmt")
        private TaxInformation taxRemittance;
        
        @JsonProperty("AddtlRmtInf")
        @Size(max = 140)
        private String[] additionalRemittanceInformation;
        
        // Getters and setters...
        public ReferredDocumentInformation[] getReferredDocumentInformation() { return referredDocumentInformation; }
        public void setReferredDocumentInformation(ReferredDocumentInformation[] referredDocumentInformation) { this.referredDocumentInformation = referredDocumentInformation; }
    }
    
    public static class ReferredDocumentInformation {
        @JsonProperty("Tp")
        private ReferredDocumentType type;
        
        @JsonProperty("Nb")
        @Size(max = 35)
        private String number;
        
        @JsonProperty("RltdDt")
        private String relatedDate;
        
        @JsonProperty("LineDtls")
        private DocumentLineInformation[] lineDetails;
        
        // Getters and setters...
        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }
    }
    
    public static class ReferredDocumentType {
        @JsonProperty("CdOrPrtry")
        private ReferredDocumentTypeChoice codeOrProprietary;
        
        @JsonProperty("Issr")
        @Size(max = 35)
        private String issuer;
        
        // Getters and setters...
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
        
        // Getters and setters...
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getProprietary() { return proprietary; }
        public void setProprietary(String proprietary) { this.proprietary = proprietary; }
    }
    
    public static class DocumentLineInformation {
        @JsonProperty("Id")
        private DocumentLineIdentification[] identification;
        
        @JsonProperty("Desc")
        @Size(max = 2048)
        private String description;
        
        @JsonProperty("Amt")
        private RemittanceAmount amount;
        
        // Getters and setters...
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
        
        // Getters and setters...
        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }
    }
    
    public static class RemittanceAmount {
        @JsonProperty("DuePyblAmt")
        private Amount duePayableAmount;
        
        @JsonProperty("DscntApldAmt")
        private DiscountAmountAndType[] discountAppliedAmount;
        
        @JsonProperty("CdtNoteAmt")
        private Amount creditNoteAmount;
        
        @JsonProperty("TaxAmt")
        private TaxAmountAndType[] taxAmount;
        
        @JsonProperty("AdjstmntAmtAndRsn")
        private DocumentAdjustment[] adjustmentAmountAndReason;
        
        @JsonProperty("RmtdAmt")
        private Amount remittedAmount;
        
        // Getters and setters...
        public Amount getDuePayableAmount() { return duePayableAmount; }
        public void setDuePayableAmount(Amount duePayableAmount) { this.duePayableAmount = duePayableAmount; }
    }
    
    public static class DiscountAmountAndType {
        @JsonProperty("Tp")
        private DiscountAmountType type;
        
        @JsonProperty("Amt")
        private Amount amount;
        
        // Getters and setters...
        public Amount getAmount() { return amount; }
        public void setAmount(Amount amount) { this.amount = amount; }
    }
    
    public static class DiscountAmountType {
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code;
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        // Getters and setters...
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    public static class TaxAmountAndType {
        @JsonProperty("Tp")
        private TaxAmountType type;
        
        @JsonProperty("Amt")
        private Amount amount;
        
        // Getters and setters...
        public Amount getAmount() { return amount; }
        public void setAmount(Amount amount) { this.amount = amount; }
    }
    
    public static class TaxAmountType {
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code;
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        // Getters and setters...
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    public static class DocumentAdjustment {
        @JsonProperty("Amt")
        private Amount amount;
        
        @JsonProperty("CdtDbtInd")
        private String creditDebitIndicator; // CRDT, DBIT
        
        @JsonProperty("Rsn")
        private String reason;
        
        @JsonProperty("AddtlInf")
        @Size(max = 140)
        private String additionalInformation;
        
        // Getters and setters...
        public Amount getAmount() { return amount; }
        public void setAmount(Amount amount) { this.amount = amount; }
        public String getCreditDebitIndicator() { return creditDebitIndicator; }
        public void setCreditDebitIndicator(String creditDebitIndicator) { this.creditDebitIndicator = creditDebitIndicator; }
    }
    
    public static class CreditorReferenceInformation {
        @JsonProperty("Tp")
        private CreditorReferenceType type;
        
        @JsonProperty("Ref")
        @Size(max = 35)
        private String reference;
        
        // Getters and setters...
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
    }
    
    public static class CreditorReferenceType {
        @JsonProperty("CdOrPrtry")
        private CreditorReferenceTypeChoice codeOrProprietary;
        
        @JsonProperty("Issr")
        @Size(max = 35)
        private String issuer;
        
        // Getters and setters...
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
        
        // Getters and setters...
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
    
    public static class PartyIdentification {
        @JsonProperty("Nm")
        @Size(max = 140)
        private String name;
        
        @JsonProperty("PstlAdr")
        private Party.PostalAddress postalAddress;
        
        @JsonProperty("Id")
        private Party.PartyIdentification identification;
        
        @JsonProperty("CtryOfRes")
        @Size(min = 2, max = 2)
        private String countryOfResidence;
        
        @JsonProperty("CtctDtls")
        private Party.ContactDetails contactDetails;
        
        // Getters and setters...
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    /**
     * Payment Identification
     */
    public static class PaymentIdentification {
        @JsonProperty("InstrId")
        private String instructionId;
        
        @JsonProperty("EndToEndId")
        private String endToEndId;
        
        @JsonProperty("TxId")
        private String transactionId;
        
        @JsonProperty("ClrSysRef")
        private String clearingSystemReference;
        
        public PaymentIdentification() {}
        
        public String getInstructionId() { return instructionId; }
        public void setInstructionId(String instructionId) { this.instructionId = instructionId; }
        
        public String getEndToEndId() { return endToEndId; }
        public void setEndToEndId(String endToEndId) { this.endToEndId = endToEndId; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public String getClearingSystemReference() { return clearingSystemReference; }
        public void setClearingSystemReference(String clearingSystemReference) { this.clearingSystemReference = clearingSystemReference; }
    }

    /**
     * Currency and Amount
     */
    public static class CurrencyAndAmount {
        @JsonProperty("Ccy")
        private String currency;
        
        @JsonProperty("Value")
        private String value;
        
        public CurrencyAndAmount() {}
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    /**
     * Active Currency and Amount
     */
    public static class ActiveCurrencyAndAmount {
        @JsonProperty("Ccy")
        private String currency;
        
        @JsonProperty("Value")
        private String value;
        
        public ActiveCurrencyAndAmount() {}
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    /**
     * Message Identification
     */
    public static class MessageIdentification {
        @JsonProperty("MsgId")
        private String messageId;
        
        @JsonProperty("CreDtTm")
        private String creationDateTime;
        
        public MessageIdentification() {}
        
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        
        public String getCreationDateTime() { return creationDateTime; }
        public void setCreationDateTime(String creationDateTime) { this.creationDateTime = creationDateTime; }
    }

    /**
     * Charges Information
     */
    public static class ChargesInformation {
        @JsonProperty("TtlChrgsAndTaxAmt")
        private ActiveCurrencyAndAmount totalChargesAndTaxAmount;
        
        @JsonProperty("Rcrd")
        private ChargeRecord record;
        
        public ChargesInformation() {}
        
        public ActiveCurrencyAndAmount getTotalChargesAndTaxAmount() { return totalChargesAndTaxAmount; }
        public void setTotalChargesAndTaxAmount(ActiveCurrencyAndAmount totalChargesAndTaxAmount) { this.totalChargesAndTaxAmount = totalChargesAndTaxAmount; }
        
        public ChargeRecord getRecord() { return record; }
        public void setRecord(ChargeRecord record) { this.record = record; }
    }

    /**
     * Charge Record
     */
    public static class ChargeRecord {
        @JsonProperty("Amt")
        private ActiveCurrencyAndAmount amount;
        
        @JsonProperty("CdtDbtInd")
        private String creditDebitIndicator;
        
        @JsonProperty("Tp")
        private ChargeType type;
        
        public ChargeRecord() {}
        
        public ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(ActiveCurrencyAndAmount amount) { this.amount = amount; }
        
        public String getCreditDebitIndicator() { return creditDebitIndicator; }
        public void setCreditDebitIndicator(String creditDebitIndicator) { this.creditDebitIndicator = creditDebitIndicator; }
        
        public ChargeType getType() { return type; }
        public void setType(ChargeType type) { this.type = type; }
    }

    /**
     * Charge Type
     */
    public static class ChargeType {
        @JsonProperty("CdOrPrtry")
        private CodeOrProprietary codeOrProprietary;
        
        public ChargeType() {}
        
        public CodeOrProprietary getCodeOrProprietary() { return codeOrProprietary; }
        public void setCodeOrProprietary(CodeOrProprietary codeOrProprietary) { this.codeOrProprietary = codeOrProprietary; }
    }

    /**
     * Code or Proprietary
     */
    public static class CodeOrProprietary {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public CodeOrProprietary() {}
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getProprietary() { return proprietary; }
        public void setProprietary(String proprietary) { this.proprietary = proprietary; }
    }

    /**
     * Local Instrument
     */
    public static class LocalInstrument {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public LocalInstrument() {}
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getProprietary() { return proprietary; }
        public void setProprietary(String proprietary) { this.proprietary = proprietary; }
    }

    /**
     * Remittance Location
     */
    public static class RemittanceLocation {
        @JsonProperty("RmtId")
        private String remittanceId;
        
        @JsonProperty("RmtLctnMtd")
        private String remittanceLocationMethod;
        
        @JsonProperty("RmtLctnElctrncAdr")
        private String remittanceLocationElectronicAddress;
        
        @JsonProperty("RmtLctnPstlAdr")
        private PostalAddress remittanceLocationPostalAddress;
        
        public RemittanceLocation() {}
        
        public String getRemittanceId() { return remittanceId; }
        public void setRemittanceId(String remittanceId) { this.remittanceId = remittanceId; }
        
        public String getRemittanceLocationMethod() { return remittanceLocationMethod; }
        public void setRemittanceLocationMethod(String remittanceLocationMethod) { this.remittanceLocationMethod = remittanceLocationMethod; }
        
        public String getRemittanceLocationElectronicAddress() { return remittanceLocationElectronicAddress; }
        public void setRemittanceLocationElectronicAddress(String remittanceLocationElectronicAddress) { this.remittanceLocationElectronicAddress = remittanceLocationElectronicAddress; }
        
        public PostalAddress getRemittanceLocationPostalAddress() { return remittanceLocationPostalAddress; }
        public void setRemittanceLocationPostalAddress(PostalAddress remittanceLocationPostalAddress) { this.remittanceLocationPostalAddress = remittanceLocationPostalAddress; }
    }

    /**
     * Postal Address
     */
    public static class PostalAddress {
        @JsonProperty("AdrTp")
        private String addressType;
        
        @JsonProperty("Dept")
        private String department;
        
        @JsonProperty("SubDept")
        private String subDepartment;
        
        @JsonProperty("StrtNm")
        private String streetName;
        
        @JsonProperty("BldgNb")
        private String buildingNumber;
        
        @JsonProperty("BldgNm")
        private String buildingName;
        
        @JsonProperty("Flr")
        private String floor;
        
        @JsonProperty("PstBx")
        private String postBox;
        
        @JsonProperty("Room")
        private String room;
        
        @JsonProperty("PstCd")
        private String postCode;
        
        @JsonProperty("TwnNm")
        private String townName;
        
        @JsonProperty("TwnLctnNm")
        private String townLocationName;
        
        @JsonProperty("DstrctNm")
        private String districtName;
        
        @JsonProperty("CtrySubDvsn")
        private String countrySubDivision;
        
        @JsonProperty("Ctry")
        private String country;
        
        @JsonProperty("AdrLine")
        private String addressLine;
        
        public PostalAddress() {}
        
        // Getters and setters
        public String getAddressType() { return addressType; }
        public void setAddressType(String addressType) { this.addressType = addressType; }
        
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        
        public String getSubDepartment() { return subDepartment; }
        public void setSubDepartment(String subDepartment) { this.subDepartment = subDepartment; }
        
        public String getStreetName() { return streetName; }
        public void setStreetName(String streetName) { this.streetName = streetName; }
        
        public String getBuildingNumber() { return buildingNumber; }
        public void setBuildingNumber(String buildingNumber) { this.buildingNumber = buildingNumber; }
        
        public String getBuildingName() { return buildingName; }
        public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
        
        public String getFloor() { return floor; }
        public void setFloor(String floor) { this.floor = floor; }
        
        public String getPostBox() { return postBox; }
        public void setPostBox(String postBox) { this.postBox = postBox; }
        
        public String getRoom() { return room; }
        public void setRoom(String room) { this.room = room; }
        
        public String getPostCode() { return postCode; }
        public void setPostCode(String postCode) { this.postCode = postCode; }
        
        public String getTownName() { return townName; }
        public void setTownName(String townName) { this.townName = townName; }
        
        public String getTownLocationName() { return townLocationName; }
        public void setTownLocationName(String townLocationName) { this.townLocationName = townLocationName; }
        
        public String getDistrictName() { return districtName; }
        public void setDistrictName(String districtName) { this.districtName = districtName; }
        
        public String getCountrySubDivision() { return countrySubDivision; }
        public void setCountrySubDivision(String countrySubDivision) { this.countrySubDivision = countrySubDivision; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getAddressLine() { return addressLine; }
        public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    }

    /**
     * Supplementary Data
     */
    public static class SupplementaryData {
        @JsonProperty("PlcAndNm")
        private String placeAndName;
        
        @JsonProperty("Envlp")
        private String envelope;
        
        public SupplementaryData() {}
        
        public String getPlaceAndName() { return placeAndName; }
        public void setPlaceAndName(String placeAndName) { this.placeAndName = placeAndName; }
        
        public String getEnvelope() { return envelope; }
        public void setEnvelope(String envelope) { this.envelope = envelope; }
    }

    /**
     * Amount
     */
    public static class Amount {
        @JsonProperty("Ccy")
        private String currency;
        
        @JsonProperty("Value")
        private String value;
        
        public Amount() {}
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    /**
     * Payment Card
     */
    public static class PaymentCard {
        @JsonProperty("PlainCardData")
        private String plainCardData;
        
        @JsonProperty("CardData")
        private String cardData;
        
        public PaymentCard() {}
        
        public String getPlainCardData() { return plainCardData; }
        public void setPlainCardData(String plainCardData) { this.plainCardData = plainCardData; }
        
        public String getCardData() { return cardData; }
        public void setCardData(String cardData) { this.cardData = cardData; }
    }

    /**
     * Point of Interaction
     */
    public static class PointOfInteraction {
        @JsonProperty("Id")
        private String id;
        
        @JsonProperty("SysNm")
        private String systemName;
        
        public PointOfInteraction() {}
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getSystemName() { return systemName; }
        public void setSystemName(String systemName) { this.systemName = systemName; }
    }

    /**
     * Card Transaction Detail
     */
    public static class CardTransactionDetail {
        @JsonProperty("TxId")
        private String transactionId;
        
        @JsonProperty("Amt")
        private ActiveCurrencyAndAmount amount;
        
        public CardTransactionDetail() {}
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public ActiveCurrencyAndAmount getAmount() { return amount; }
        public void setAmount(ActiveCurrencyAndAmount amount) { this.amount = amount; }
    }

    /**
     * Cash Account
     */
    public static class CashAccount {
        @JsonProperty("Id")
        private String id;
        
        @JsonProperty("Tp")
        private String type;
        
        public CashAccount() {}
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    /**
     * Securities Account
     */
    public static class SecuritiesAccount {
        @JsonProperty("Id")
        private String id;
        
        @JsonProperty("Tp")
        private String type;
        
        public SecuritiesAccount() {}
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    /**
     * Payment Return Information
     */
    public static class PaymentReturnInformation {
        @JsonProperty("OrgnlInstrId")
        private String originalInstructionId;
        
        @JsonProperty("OrgnlEndToEndId")
        private String originalEndToEndId;
        
        public PaymentReturnInformation() {}
        
        public String getOriginalInstructionId() { return originalInstructionId; }
        public void setOriginalInstructionId(String originalInstructionId) { this.originalInstructionId = originalInstructionId; }
        
        public String getOriginalEndToEndId() { return originalEndToEndId; }
        public void setOriginalEndToEndId(String originalEndToEndId) { this.originalEndToEndId = originalEndToEndId; }
    }

    /**
     * Price Type
     */
    public static class PriceType {
        @JsonProperty("Cd")
        private String code;
        
        @JsonProperty("Prtry")
        private String proprietary;
        
        public PriceType() {}
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getProprietary() { return proprietary; }
        public void setProprietary(String proprietary) { this.proprietary = proprietary; }
    }

    /**
     * Transaction Interest
     */
    public static class TransactionInterest {
        @JsonProperty("TtlIntrstAndTaxAmt")
        private ActiveCurrencyAndAmount totalInterestAndTaxAmount;
        
        @JsonProperty("Rate")
        private String rate;
        
        public TransactionInterest() {}
        
        public ActiveCurrencyAndAmount getTotalInterestAndTaxAmount() { return totalInterestAndTaxAmount; }
        public void setTotalInterestAndTaxAmount(ActiveCurrencyAndAmount totalInterestAndTaxAmount) { this.totalInterestAndTaxAmount = totalInterestAndTaxAmount; }
        
        public String getRate() { return rate; }
        public void setRate(String rate) { this.rate = rate; }
    }
}