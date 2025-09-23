package com.paymentengine.shared.dto.iso20022;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/**
 * ISO 20022 Financial Institution Information
 */
public class FinancialInstitution {
    
    @JsonProperty("FinInstnId")
    @Valid
    private FinancialInstitutionIdentification financialInstitutionIdentification;
    
    @JsonProperty("BrnchId")
    @Valid
    private BranchData branchIdentification;
    
    public FinancialInstitution() {}
    
    public FinancialInstitution(FinancialInstitutionIdentification financialInstitutionIdentification) {
        this.financialInstitutionIdentification = financialInstitutionIdentification;
    }
    
    public FinancialInstitutionIdentification getFinancialInstitutionIdentification() {
        return financialInstitutionIdentification;
    }
    
    public void setFinancialInstitutionIdentification(FinancialInstitutionIdentification financialInstitutionIdentification) {
        this.financialInstitutionIdentification = financialInstitutionIdentification;
    }
    
    public BranchData getBranchIdentification() {
        return branchIdentification;
    }
    
    public void setBranchIdentification(BranchData branchIdentification) {
        this.branchIdentification = branchIdentification;
    }
    
    /**
     * Financial Institution Identification
     */
    public static class FinancialInstitutionIdentification {
        
        @JsonProperty("BICFI")
        @Size(min = 8, max = 11)
        private String bicfi; // BIC (Bank Identifier Code)
        
        @JsonProperty("ClrSysMmbId")
        @Valid
        private ClearingSystemMemberIdentification clearingSystemMemberIdentification;
        
        @JsonProperty("LEI")
        @Size(min = 20, max = 20)
        private String legalEntityIdentifier;
        
        @JsonProperty("Nm")
        @Size(max = 140)
        private String name;
        
        @JsonProperty("PstlAdr")
        @Valid
        private Party.PostalAddress postalAddress;
        
        @JsonProperty("Othr")
        @Valid
        private CommonTypes.GenericFinancialIdentification other;
        
        public FinancialInstitutionIdentification() {}
        
        // Getters and Setters
        public String getBicfi() {
            return bicfi;
        }
        
        public void setBicfi(String bicfi) {
            this.bicfi = bicfi;
        }
        
        public ClearingSystemMemberIdentification getClearingSystemMemberIdentification() {
            return clearingSystemMemberIdentification;
        }
        
        public void setClearingSystemMemberIdentification(ClearingSystemMemberIdentification clearingSystemMemberIdentification) {
            this.clearingSystemMemberIdentification = clearingSystemMemberIdentification;
        }
        
        public String getLegalEntityIdentifier() {
            return legalEntityIdentifier;
        }
        
        public void setLegalEntityIdentifier(String legalEntityIdentifier) {
            this.legalEntityIdentifier = legalEntityIdentifier;
        }
        
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
        
        public CommonTypes.GenericFinancialIdentification getOther() {
            return other;
        }
        
        public void setOther(CommonTypes.GenericFinancialIdentification other) {
            this.other = other;
        }
    }
    
    /**
     * Clearing System Member Identification
     */
    public static class ClearingSystemMemberIdentification {
        
        @JsonProperty("ClrSysId")
        @Valid
        private ClearingSystemIdentification clearingSystemIdentification;
        
        @JsonProperty("MmbId")
        @NotNull
        @Size(min = 1, max = 35)
        private String memberId;
        
        public ClearingSystemMemberIdentification() {}
        
        public ClearingSystemIdentification getClearingSystemIdentification() {
            return clearingSystemIdentification;
        }
        
        public void setClearingSystemIdentification(ClearingSystemIdentification clearingSystemIdentification) {
            this.clearingSystemIdentification = clearingSystemIdentification;
        }
        
        public String getMemberId() {
            return memberId;
        }
        
        public void setMemberId(String memberId) {
            this.memberId = memberId;
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
     * Branch Data
     */
    public static class BranchData {
        
        @JsonProperty("Id")
        @Size(max = 35)
        private String identification;
        
        @JsonProperty("LEI")
        @Size(min = 20, max = 20)
        private String legalEntityIdentifier;
        
        @JsonProperty("Nm")
        @Size(max = 140)
        private String name;
        
        @JsonProperty("PstlAdr")
        @Valid
        private Party.PostalAddress postalAddress;
        
        public BranchData() {}
        
        // Getters and Setters
        public String getIdentification() {
            return identification;
        }
        
        public void setIdentification(String identification) {
            this.identification = identification;
        }
        
        public String getLegalEntityIdentifier() {
            return legalEntityIdentifier;
        }
        
        public void setLegalEntityIdentifier(String legalEntityIdentifier) {
            this.legalEntityIdentifier = legalEntityIdentifier;
        }
        
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
    }
}