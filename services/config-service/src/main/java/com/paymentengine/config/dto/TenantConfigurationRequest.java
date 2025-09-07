package com.paymentengine.config.dto;

import com.paymentengine.config.entity.TenantConfiguration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TenantConfigurationRequest {
    
    @NotBlank(message = "Config key is required")
    @Size(max = 100, message = "Config key must not exceed 100 characters")
    private String configKey;
    
    private String configValue;
    
    private TenantConfiguration.ConfigType configType = TenantConfiguration.ConfigType.STRING;
    
    private Boolean isEncrypted = false;
    
    // Constructors
    public TenantConfigurationRequest() {}
    
    public TenantConfigurationRequest(String configKey, String configValue, 
                                    TenantConfiguration.ConfigType configType, Boolean isEncrypted) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.configType = configType;
        this.isEncrypted = isEncrypted;
    }
    
    // Getters and Setters
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    
    public TenantConfiguration.ConfigType getConfigType() { return configType; }
    public void setConfigType(TenantConfiguration.ConfigType configType) { this.configType = configType; }
    
    public Boolean getIsEncrypted() { return isEncrypted; }
    public void setIsEncrypted(Boolean isEncrypted) { this.isEncrypted = isEncrypted; }
}