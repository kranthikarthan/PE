package com.paymentengine.gateway.dto.tenant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for configuration deployment result
 */
public class ConfigurationDeploymentResult {
    
    private Boolean success;
    private String message;
    private String tenantId;
    private LocalDateTime deployedAt;
    private List<DeploymentStep> deploymentSteps;
    private Map<String, Object> testResults;
    private Map<String, Object> metadata;
    
    // Constructors
    public ConfigurationDeploymentResult() {}
    
    public ConfigurationDeploymentResult(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private ConfigurationDeploymentResult result = new ConfigurationDeploymentResult();
        
        public Builder success(Boolean success) {
            result.success = success;
            return this;
        }
        
        public Builder message(String message) {
            result.message = message;
            return this;
        }
        
        public Builder tenantId(String tenantId) {
            result.tenantId = tenantId;
            return this;
        }
        
        public Builder deployedAt(LocalDateTime deployedAt) {
            result.deployedAt = deployedAt;
            return this;
        }
        
        public Builder deploymentSteps(List<DeploymentStep> deploymentSteps) {
            result.deploymentSteps = deploymentSteps;
            return this;
        }
        
        public Builder testResults(Map<String, Object> testResults) {
            result.testResults = testResults;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            result.metadata = metadata;
            return this;
        }
        
        public ConfigurationDeploymentResult build() {
            return result;
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
    
    public LocalDateTime getDeployedAt() {
        return deployedAt;
    }
    
    public void setDeployedAt(LocalDateTime deployedAt) {
        this.deployedAt = deployedAt;
    }
    
    public List<DeploymentStep> getDeploymentSteps() {
        return deploymentSteps;
    }
    
    public void setDeploymentSteps(List<DeploymentStep> deploymentSteps) {
        this.deploymentSteps = deploymentSteps;
    }
    
    public Map<String, Object> getTestResults() {
        return testResults;
    }
    
    public void setTestResults(Map<String, Object> testResults) {
        this.testResults = testResults;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    // Inner classes
    public static class DeploymentStep {
        private String stepName;
        private String description;
        private Boolean success;
        private String message;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long durationMs;
        private Map<String, Object> details;
        
        // Constructors
        public DeploymentStep() {}
        
        public DeploymentStep(String stepName, String description, Boolean success, String message) {
            this.stepName = stepName;
            this.description = description;
            this.success = success;
            this.message = message;
        }
        
        // Getters and Setters
        public String getStepName() {
            return stepName;
        }
        
        public void setStepName(String stepName) {
            this.stepName = stepName;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
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
        
        public LocalDateTime getStartedAt() {
            return startedAt;
        }
        
        public void setStartedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
        }
        
        public LocalDateTime getCompletedAt() {
            return completedAt;
        }
        
        public void setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
        }
        
        public Long getDurationMs() {
            return durationMs;
        }
        
        public void setDurationMs(Long durationMs) {
            this.durationMs = durationMs;
        }
        
        public Map<String, Object> getDetails() {
            return details;
        }
        
        public void setDetails(Map<String, Object> details) {
            this.details = details;
        }
    }
}