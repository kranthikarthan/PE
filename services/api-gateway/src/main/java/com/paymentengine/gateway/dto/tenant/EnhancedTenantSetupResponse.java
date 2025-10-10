package com.paymentengine.gateway.dto.tenant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for enhanced tenant setup with multi-level authentication configuration
 */
public class EnhancedTenantSetupResponse {
    
    private Boolean success;
    private String message;
    private String tenantId;
    private String tenantName;
    private String environment;
    private LocalDateTime createdAt;
    private List<ConfigurationResult> configurations;
    private Map<String, Object> metadata;
    
    // Constructors
    public EnhancedTenantSetupResponse() {}
    
    public EnhancedTenantSetupResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private EnhancedTenantSetupResponse response = new EnhancedTenantSetupResponse();
        
        public Builder success(Boolean success) {
            response.success = success;
            return this;
        }
        
        public Builder message(String message) {
            response.message = message;
            return this;
        }
        
        public Builder tenantId(String tenantId) {
            response.tenantId = tenantId;
            return this;
        }
        
        public Builder tenantName(String tenantName) {
            response.tenantName = tenantName;
            return this;
        }
        
        public Builder environment(String environment) {
            response.environment = environment;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            response.createdAt = createdAt;
            return this;
        }
        
        public Builder configurations(List<ConfigurationResult> configurations) {
            response.configurations = configurations;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            response.metadata = metadata;
            return this;
        }
        
        public EnhancedTenantSetupResponse build() {
            return response;
        }
    }
    
    // Getters and Setters
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getTenantName() {
        return tenantName;
    }
    
    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<ConfigurationResult> getConfigurations() {
        return configurations;
    }
    
    public void setConfigurations(List<ConfigurationResult> configurations) {
        this.configurations = configurations;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    // Inner classes
    public static class ConfigurationResult {
        private String level; // clearing-system, payment-type, downstream-call
        private String name;
        private String authMethod;
        private Boolean success;
        private String message;
        private String configurationId;
        private LocalDateTime createdAt;
        
        // Constructors
        public ConfigurationResult() {}
        
        public ConfigurationResult(String level, String name, String authMethod, Boolean success, String message) {
            this.level = level;
            this.name = name;
            this.authMethod = authMethod;
            this.success = success;
            this.message = message;
        }
        
        // Getters and Setters
        public String getLevel() {
            return level;
        }
        
        public void setLevel(String level) {
            this.level = level;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getAuthMethod() {
            return authMethod;
        }
        
        public void setAuthMethod(String authMethod) {
            this.authMethod = authMethod;
        }
        
        public Boolean getSuccess() {
            return success;
        }
        
        public void setSuccess(Boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getConfigurationId() {
            return configurationId;
        }
        
        public void setConfigurationId(String configurationId) {
            this.configurationId = configurationId;
        }
        
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}