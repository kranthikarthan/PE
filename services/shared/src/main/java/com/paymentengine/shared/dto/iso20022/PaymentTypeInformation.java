package com.paymentengine.shared.dto.iso20022;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/**
 * ISO 20022 Payment Type Information
 */
public class PaymentTypeInformation {
    
    @JsonProperty("InstrPrty")
    private String instructionPriority; // HIGH, NORM, URGP
    
    @JsonProperty("SvcLvl")
    @Valid
    private ServiceLevel serviceLevel;
    
    @JsonProperty("LclInstrm")
    @Valid
    private LocalInstrument localInstrument;
    
    @JsonProperty("CtgyPurp")
    @Valid
    private CategoryPurpose categoryPurpose;
    
    public PaymentTypeInformation() {}
    
    // Getters and Setters
    public String getInstructionPriority() {
        return instructionPriority;
    }
    
    public void setInstructionPriority(String instructionPriority) {
        this.instructionPriority = instructionPriority;
    }
    
    public ServiceLevel getServiceLevel() {
        return serviceLevel;
    }
    
    public void setServiceLevel(ServiceLevel serviceLevel) {
        this.serviceLevel = serviceLevel;
    }
    
    public LocalInstrument getLocalInstrument() {
        return localInstrument;
    }
    
    public void setLocalInstrument(LocalInstrument localInstrument) {
        this.localInstrument = localInstrument;
    }
    
    public CategoryPurpose getCategoryPurpose() {
        return categoryPurpose;
    }
    
    public void setCategoryPurpose(CategoryPurpose categoryPurpose) {
        this.categoryPurpose = categoryPurpose;
    }
    
    /**
     * Service Level
     */
    public static class ServiceLevel {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code; // SEPA, URGP, NURG, etc.
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public ServiceLevel() {}
        
        public ServiceLevel(String code) {
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
     * Local Instrument
     */
    public static class LocalInstrument {
        
        @JsonProperty("Cd")
        @Size(max = 35)
        private String code; // ACH, WIRE, RTP, etc.
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public LocalInstrument() {}
        
        public LocalInstrument(String code) {
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
     * Category Purpose
     */
    public static class CategoryPurpose {
        
        @JsonProperty("Cd")
        @Size(max = 4)
        private String code; // CBFF, CHAR, CORT, etc.
        
        @JsonProperty("Prtry")
        @Size(max = 35)
        private String proprietary;
        
        public CategoryPurpose() {}
        
        public CategoryPurpose(String code) {
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
}