package com.paymentengine.paymentprocessing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for testing scheme configurations
 */
public class SchemeTestRequest {
    
    @NotBlank(message = "Configuration ID is required")
    private String configId;
    
    @Valid
    @NotNull(message = "Test message is required")
    private SchemeMessageRequest testMessage;
    
    private Boolean validateOnly;
    
    // Constructors
    public SchemeTestRequest() {}
    
    public SchemeTestRequest(String configId, SchemeMessageRequest testMessage, Boolean validateOnly) {
        this.configId = configId;
        this.testMessage = testMessage;
        this.validateOnly = validateOnly;
    }
    
    // Getters and Setters
    public String getConfigId() {
        return configId;
    }
    
    public void setConfigId(String configId) {
        this.configId = configId;
    }
    
    public SchemeMessageRequest getTestMessage() {
        return testMessage;
    }
    
    public void setTestMessage(SchemeMessageRequest testMessage) {
        this.testMessage = testMessage;
    }
    
    public Boolean getValidateOnly() {
        return validateOnly;
    }
    
    public void setValidateOnly(Boolean validateOnly) {
        this.validateOnly = validateOnly;
    }
}