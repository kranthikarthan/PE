package com.payments.domain.validation;

/**
 * Validation Status Enum
 * 
 * Represents the outcome of payment validation:
 * - PASSED: All validation rules passed
 * - FAILED: One or more validation rules failed
 */
public enum ValidationStatus {
    PASSED("PASSED", "Validation passed"),
    FAILED("FAILED", "Validation failed");
    
    private final String code;
    private final String description;
    
    ValidationStatus(String code, String description) {
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