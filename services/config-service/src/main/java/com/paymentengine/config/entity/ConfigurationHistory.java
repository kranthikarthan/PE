package com.paymentengine.config.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "configuration_history")
public class ConfigurationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "config_type", nullable = false)
    private ConfigType configType;
    
    @Column(name = "config_id", nullable = false)
    private UUID configId;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "config_key", nullable = false)
    private String configKey;
    
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;
    
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
    
    @Size(max = 500)
    @Column(name = "change_reason")
    private String changeReason;
    
    @Size(max = 100)
    @Column(name = "changed_by", nullable = false)
    private String changedBy;
    
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public ConfigurationHistory() {}
    
    public ConfigurationHistory(ConfigType configType, UUID configId, String configKey, 
                               String oldValue, String newValue, String changeReason, String changedBy) {
        this.configType = configType;
        this.configId = configId;
        this.configKey = configKey;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeReason = changeReason;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public ConfigType getConfigType() { return configType; }
    public void setConfigType(ConfigType configType) { this.configType = configType; }
    
    public UUID getConfigId() { return configId; }
    public void setConfigId(UUID configId) { this.configId = configId; }
    
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public enum ConfigType {
        TENANT, TENANT_CONFIG, FEATURE_FLAG, SERVICE_CONFIG, ENVIRONMENT_CONFIG
    }
}