package com.paymentengine.corebanking.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for configuration hierarchy in Core Banking Service
 */
public class ConfigurationHierarchyDTO {
    
    private String tenantId;
    private String tenantName;
    private String environment;
    private List<HierarchyLevelDTO> hierarchyLevels;
    private Map<String, Object> metadata;
    private LocalDateTime lastUpdated;
    
    // Constructors
    public ConfigurationHierarchyDTO() {}
    
    public ConfigurationHierarchyDTO(String tenantId, String tenantName) {
        this.tenantId = tenantId;
        this.tenantName = tenantName;
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
    
    public List<HierarchyLevelDTO> getHierarchyLevels() {
        return hierarchyLevels;
    }
    
    public void setHierarchyLevels(List<HierarchyLevelDTO> hierarchyLevels) {
        this.hierarchyLevels = hierarchyLevels;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    // Inner classes
    public static class HierarchyLevelDTO {
        private String level; // downstream-call, payment-type, tenant, clearing-system
        private Integer priority; // 1 = highest priority, 4 = lowest priority
        private String name;
        private String authMethod;
        private Boolean isConfigured;
        private Boolean isActive;
        private String configurationId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Map<String, Object> configuration;
        
        // Constructors
        public HierarchyLevelDTO() {}
        
        public HierarchyLevelDTO(String level, Integer priority, String name, String authMethod) {
            this.level = level;
            this.priority = priority;
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
        
        public Integer getPriority() {
            return priority;
        }
        
        public void setPriority(Integer priority) {
            this.priority = priority;
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
        
        public Boolean getIsConfigured() {
            return isConfigured;
        }
        
        public void setIsConfigured(Boolean isConfigured) {
            this.isConfigured = isConfigured;
        }
        
        public Boolean getIsActive() {
            return isActive;
        }
        
        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
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
        
        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }
        
        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
        
        public Map<String, Object> getConfiguration() {
            return configuration;
        }
        
        public void setConfiguration(Map<String, Object> configuration) {
            this.configuration = configuration;
        }
    }
}