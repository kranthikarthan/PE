package com.paymentengine.middleware.exception;

import java.util.List;

/**
 * Exception for validation-related errors in middleware
 */
public class ValidationException extends CoreBankingException {
    
    private final List<String> validationErrors;
    
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.validationErrors = List.of(message);
    }
    
    public ValidationException(String message, List<String> validationErrors) {
        super(message, "VALIDATION_ERROR");
        this.validationErrors = validationErrors;
    }
    
    public ValidationException(List<String> validationErrors) {
        super("Validation failed: " + String.join(", ", validationErrors), "VALIDATION_ERROR");
        this.validationErrors = validationErrors;
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}