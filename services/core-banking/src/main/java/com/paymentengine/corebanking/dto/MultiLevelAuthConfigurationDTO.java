package com.paymentengine.corebanking.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for multi-level authentication configuration in Core Banking Service
 */
public class MultiLevelAuthConfigurationDTO {
    
    private String tenantId;
    private String tenantName;
    private String environment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ConfigurationLevelDTO> configurationLevels;
    private Map<String, Object> metadata;
    
    // Constructors
    public MultiLevelAuthConfigurationDTO() {}
    
    public MultiLevelAuthConfigurationDTO(String tenantId, String tenantName, String environment) {
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.environment = environment;
    }
    
    // Getters and Setters
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
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<ConfigurationLevelDTO> getConfigurationLevels() {
        return configurationLevels;
    }
    
    public void setConfigurationLevels(List<ConfigurationLevelDTO> configurationLevels) {
        this.configurationLevels = configurationLevels;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    // Inner classes
    public static class ConfigurationLevelDTO {
        private String level; // clearing-system, payment-type, downstream-call
        private String name;
        private String authMethod;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private Map<String, Object> configuration;
        
        // Constructors
        public ConfigurationLevelDTO() {}
        
        public ConfigurationLevelDTO(String level, String name, String authMethod) {
            this.level = level;
            this.name = name;
            this.authMethod = authMethod;
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
        
        public Boolean getIsActive() {
            return isActive;
        }
        
        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }
        
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
        
        public Map<String, Object> getConfiguration() {
            return configuration;
        }
        
        public void setConfiguration(Map<String, Object> configuration) {
            this.configuration = configuration;
        }
    }
}