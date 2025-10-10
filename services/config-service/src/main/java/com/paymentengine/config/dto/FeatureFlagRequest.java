package com.paymentengine.config.dto;

import com.paymentengine.config.entity.FeatureFlag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class FeatureFlagRequest {
    
    @NotBlank(message = "Flag name is required")
    @Size(max = 100, message = "Flag name must not exceed 100 characters")
    private String flagName;
    
    @Size(max = 500, message = "Flag description must not exceed 500 characters")
    private String flagDescription;
    
    private Boolean flagValue = false;
    
    private UUID tenantId;
    
    private FeatureFlag.Environment environment = FeatureFlag.Environment.PRODUCTION;
    
    private Integer rolloutPercentage = 0;
    
    private String targetUsers;
    
    // Constructors
    public FeatureFlagRequest() {}
    
    public FeatureFlagRequest(String flagName, String flagDescription, Boolean flagValue, 
                             UUID tenantId, FeatureFlag.Environment environment, 
                             Integer rolloutPercentage, String targetUsers) {
        this.flagName = flagName;
        this.flagDescription = flagDescription;
        this.flagValue = flagValue;
        this.tenantId = tenantId;
        this.environment = environment;
        this.rolloutPercentage = rolloutPercentage;
        this.targetUsers = targetUsers;
    }
    
    // Getters and Setters
    public String getFlagName() { return flagName; }
    public void setFlagName(String flagName) { this.flagName = flagName; }
    
    public String getFlagDescription() { return flagDescription; }
    public void setFlagDescription(String flagDescription) { this.flagDescription = flagDescription; }
    
    public Boolean getFlagValue() { return flagValue; }
    public void setFlagValue(Boolean flagValue) { this.flagValue = flagValue; }
    
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    
    public FeatureFlag.Environment getEnvironment() { return environment; }
    public void setEnvironment(FeatureFlag.Environment environment) { this.environment = environment; }
    
    public Integer getRolloutPercentage() { return rolloutPercentage; }
    public void setRolloutPercentage(Integer rolloutPercentage) { this.rolloutPercentage = rolloutPercentage; }
    
    public String getTargetUsers() { return targetUsers; }
    public void setTargetUsers(String targetUsers) { this.targetUsers = targetUsers; }
}