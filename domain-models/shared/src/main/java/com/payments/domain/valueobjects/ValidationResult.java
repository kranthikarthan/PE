package com.payments.domain.valueobjects;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Value object for validation results
 */
@Data
@Builder
public class ValidationResult {
    
    private boolean isValid;
    private List<String> errors;
    private List<String> warnings;
    
    public ValidationResult() {
        this.isValid = true;
        this.errors = List.of();
        this.warnings = List.of();
    }
    
    public ValidationResult(boolean isValid, List<String> errors) {
        this.isValid = isValid;
        this.errors = errors != null ? errors : List.of();
        this.warnings = List.of();
    }
    
    public ValidationResult(boolean isValid, List<String> errors, List<String> warnings) {
        this.isValid = isValid;
        this.errors = errors != null ? errors : List.of();
        this.warnings = warnings != null ? warnings : List.of();
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    public String getFirstError() {
        return errors.isEmpty() ? null : errors.get(0);
    }
    
    public String getFirstWarning() {
        return warnings.isEmpty() ? null : warnings.get(0);
    }
    
    // Builder method for compatibility
    public static ValidationResultBuilder builder() {
        return new ValidationResultBuilder();
    }
}
