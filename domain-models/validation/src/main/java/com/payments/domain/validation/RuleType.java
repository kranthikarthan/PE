package com.payments.domain.validation;

/**
 * Rule Type Enum
 * 
 * Represents the type of validation rule:
 * - BUSINESS: Business logic rules
 * - COMPLIANCE: Regulatory compliance rules
 * - FRAUD: Fraud detection rules
 * - RISK: Risk assessment rules
 */
public enum RuleType {
    BUSINESS("BUSINESS", "Business logic rules"),
    COMPLIANCE("COMPLIANCE", "Regulatory compliance rules"),
    FRAUD("FRAUD", "Fraud detection rules"),
    RISK("RISK", "Risk assessment rules");
    
    private final String code;
    private final String description;
    
    RuleType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return code;
    }
}