package com.paymentengine.shared.dto.iso20022;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * ISO 20022 Credit Transfer Transaction Information
 */
public class CreditTransferTransactionInformation {
    
    @JsonProperty("PmtId")
    @NotNull
    @Valid
    private CommonTypes.PaymentIdentification paymentIdentification;
    
    @JsonProperty("PmtTpInf")
    @Valid
    private PaymentTypeInformation paymentTypeInformation;
    
    @JsonProperty("Amt")
    @NotNull
    @Valid
    private Amount amount;
    
    @JsonProperty("ChrgBr")
    private String chargeBearer; // DEBT, CRED, SHAR, SLEV
    
    @JsonProperty("UltmtDbtr")
    @Valid
    private Party ultimateDebtor;
    
    @JsonProperty("CdtrAgt")
    @Valid
    private FinancialInstitution creditorAgent;
    
    @JsonProperty("Cdtr")
    @NotNull
    @Valid
    private Party creditor;
    
    @JsonProperty("CdtrAcct")
    @NotNull
    @Valid
    private Account creditorAccount;
    
    @JsonProperty("UltmtCdtr")
    @Valid
    private Party ultimateCreditor;
    
    @JsonProperty("InstrForCdtrAgt")
    @Size(max = 420)
    private String instructionForCreditorAgent;
    
    @JsonProperty("InstrForDbtrAgt")
    @Size(max = 420)
    private String instructionForDebtorAgent;
    
    @JsonProperty("Purp")
    @Valid
    private CommonTypes.Purpose purpose;
    
    @JsonProperty("RgltryRptg")
    @Valid
    private CommonTypes.RegulatoryReporting regulatoryReporting;
    
    @JsonProperty("Tax")
    @Valid
    private CommonTypes.TaxInformation taxInformation;
    
    @JsonProperty("RltdRmtInf")
    @Valid
    private CommonTypes.RemittanceInformation relatedRemittanceInformation;
    
    @JsonProperty("RmtInf")
    @Valid
    private CommonTypes.RemittanceInformation remittanceInformation;
    
    public CreditTransferTransactionInformation() {}
    
    // Getters and Setters
    public CommonTypes.PaymentIdentification getPaymentIdentification() {
        return paymentIdentification;
    }
    
    public void setPaymentIdentification(CommonTypes.PaymentIdentification paymentIdentification) {
        this.paymentIdentification = paymentIdentification;
    }
    
    public PaymentTypeInformation getPaymentTypeInformation() {
        return paymentTypeInformation;
    }
    
    public void setPaymentTypeInformation(PaymentTypeInformation paymentTypeInformation) {
        this.paymentTypeInformation = paymentTypeInformation;
    }
    
    public Amount getAmount() {
        return amount;
    }
    
    public void setAmount(Amount amount) {
        this.amount = amount;
    }
    
    public String getChargeBearer() {
        return chargeBearer;
    }
    
    public void setChargeBearer(String chargeBearer) {
        this.chargeBearer = chargeBearer;
    }
    
    public Party getUltimateDebtor() {
        return ultimateDebtor;
    }
    
    public void setUltimateDebtor(Party ultimateDebtor) {
        this.ultimateDebtor = ultimateDebtor;
    }
    
    public FinancialInstitution getCreditorAgent() {
        return creditorAgent;
    }
    
    public void setCreditorAgent(FinancialInstitution creditorAgent) {
        this.creditorAgent = creditorAgent;
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
    
    public Party getUltimateCreditor() {
        return ultimateCreditor;
    }
    
    public void setUltimateCreditor(Party ultimateCreditor) {
        this.ultimateCreditor = ultimateCreditor;
    }
    
    public String getInstructionForCreditorAgent() {
        return instructionForCreditorAgent;
    }
    
    public void setInstructionForCreditorAgent(String instructionForCreditorAgent) {
        this.instructionForCreditorAgent = instructionForCreditorAgent;
    }
    
    public String getInstructionForDebtorAgent() {
        return instructionForDebtorAgent;
    }
    
    public void setInstructionForDebtorAgent(String instructionForDebtorAgent) {
        this.instructionForDebtorAgent = instructionForDebtorAgent;
    }
    
    public CommonTypes.Purpose getPurpose() {
        return purpose;
    }
    
    public void setPurpose(CommonTypes.Purpose purpose) {
        this.purpose = purpose;
    }
    
    public CommonTypes.RegulatoryReporting getRegulatoryReporting() {
        return regulatoryReporting;
    }
    
    public void setRegulatoryReporting(CommonTypes.RegulatoryReporting regulatoryReporting) {
        this.regulatoryReporting = regulatoryReporting;
    }
    
    public CommonTypes.TaxInformation getTaxInformation() {
        return taxInformation;
    }
    
    public void setTaxInformation(CommonTypes.TaxInformation taxInformation) {
        this.taxInformation = taxInformation;
    }
    
    public CommonTypes.RemittanceInformation getRelatedRemittanceInformation() {
        return relatedRemittanceInformation;
    }
    
    public void setRelatedRemittanceInformation(CommonTypes.RemittanceInformation relatedRemittanceInformation) {
        this.relatedRemittanceInformation = relatedRemittanceInformation;
    }
    
    public CommonTypes.RemittanceInformation getRemittanceInformation() {
        return remittanceInformation;
    }
    
    public void setRemittanceInformation(CommonTypes.RemittanceInformation remittanceInformation) {
        this.remittanceInformation = remittanceInformation;
    }
    
    /**
     * Payment Identification
     */
    public static class PaymentIdentification {
        
        @JsonProperty("InstrId")
        @Size(max = 35)
        private String instructionId;
        
        @JsonProperty("EndToEndId")
        @NotNull
        @Size(min = 1, max = 35)
        private String endToEndId;
        
        @JsonProperty("TxId")
        @Size(max = 35)
        private String transactionId;
        
        @JsonProperty("UETR")
        @Size(max = 36)
        private String uniqueEndToEndTransactionReference;
        
        public PaymentIdentification() {}
        
        public PaymentIdentification(String endToEndId) {
            this.endToEndId = endToEndId;
        }
        
        // Getters and Setters
        public String getInstructionId() {
            return instructionId;
        }
        
        public void setInstructionId(String instructionId) {
            this.instructionId = instructionId;
        }
        
        public String getEndToEndId() {
            return endToEndId;
        }
        
        public void setEndToEndId(String endToEndId) {
            this.endToEndId = endToEndId;
        }
        
        public String getTransactionId() {
            return transactionId;
        }
        
        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }
        
        public String getUniqueEndToEndTransactionReference() {
            return uniqueEndToEndTransactionReference;
        }
        
        public void setUniqueEndToEndTransactionReference(String uniqueEndToEndTransactionReference) {
            this.uniqueEndToEndTransactionReference = uniqueEndToEndTransactionReference;
        }
    }
    
    /**
     * Amount with currency
     */
    public static class Amount {
        
        @JsonProperty("InstdAmt")
        @Valid
        private InstructedAmount instructedAmount;
        
        @JsonProperty("EqvtAmt")
        @Valid
        private EquivalentAmount equivalentAmount;
        
        public Amount() {}
        
        public Amount(InstructedAmount instructedAmount) {
            this.instructedAmount = instructedAmount;
        }
        
        public InstructedAmount getInstructedAmount() {
            return instructedAmount;
        }
        
        public void setInstructedAmount(InstructedAmount instructedAmount) {
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
     * Instructed Amount
     */
    public static class InstructedAmount {
        
        @JsonProperty("Ccy")
        @NotNull
        @Size(min = 3, max = 3)
        private String currency;
        
        @JsonProperty("value")
        @NotNull
        private BigDecimal value;
        
        public InstructedAmount() {}
        
        public InstructedAmount(String currency, BigDecimal value) {
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
     * Equivalent Amount (for currency conversion)
     */
    public static class EquivalentAmount {
        
        @JsonProperty("Amt")
        @Valid
        private InstructedAmount amount;
        
        @JsonProperty("CcyOfTrf")
        @Size(min = 3, max = 3)
        private String currencyOfTransfer;
        
        public EquivalentAmount() {}
        
        public InstructedAmount getAmount() {
            return amount;
        }
        
        public void setAmount(InstructedAmount amount) {
            this.amount = amount;
        }
        
        public String getCurrencyOfTransfer() {
            return currencyOfTransfer;
        }
        
        public void setCurrencyOfTransfer(String currencyOfTransfer) {
            this.currencyOfTransfer = currencyOfTransfer;
        }
    }
}