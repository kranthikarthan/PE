package com.paymentengine.middleware.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for scheme configuration tests
 */
public class SchemeTestResponse {
    
    private Boolean success;
    private Long responseTimeMs;
    private SchemeMessageResponse response;
    private SchemeMessageResponse.ErrorDetails error;
    private List<ValidationResult> validationResults;
    private Instant timestamp;
    
    // Constructors
    public SchemeTestResponse() {}
    
    public SchemeTestResponse(Boolean success, Long responseTimeMs, SchemeMessageResponse response,
                             SchemeMessageResponse.ErrorDetails error, List<ValidationResult> validationResults,
                             Instant timestamp) {
        this.success = success;
        this.responseTimeMs = responseTimeMs;
        this.response = response;
        this.error = error;
        this.validationResults = validationResults;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public Long getResponseTimeMs() {
        return responseTimeMs;
    }
    
    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
    
    public SchemeMessageResponse getResponse() {
        return response;
    }
    
    public void setResponse(SchemeMessageResponse response) {
        this.response = response;
    }
    
    public SchemeMessageResponse.ErrorDetails getError() {
        return error;
    }
    
    public void setError(SchemeMessageResponse.ErrorDetails error) {
        this.error = error;
    }
    
    public List<ValidationResult> getValidationResults() {
        return validationResults;
    }
    
    public void setValidationResults(List<ValidationResult> validationResults) {
        this.validationResults = validationResults;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    // Nested classes
    public static class ValidationResult {
        private String field;
        private Boolean valid;
        private String errorMessage;
        private ValidationSeverity severity;
        
        // Constructors
        public ValidationResult() {}
        
        public ValidationResult(String field, Boolean valid, String errorMessage, ValidationSeverity severity) {
            this.field = field;
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.severity = severity;
        }
        
        // Getters and Setters
        public String getField() {
            return field;
        }
        
        public void setField(String field) {
            this.field = field;
        }
        
        public Boolean getValid() {
            return valid;
        }
        
        public void setValid(Boolean valid) {
            this.valid = valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        public ValidationSeverity getSeverity() {
            return severity;
        }
        
        public void setSeverity(ValidationSeverity severity) {
            this.severity = severity;
        }
        
        public enum ValidationSeverity {
            ERROR,
            WARNING,
            INFO
        }
    }
}