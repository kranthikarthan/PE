package com.paymentengine.config.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feature_flags")
public class FeatureFlag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "flag_name", unique = true, nullable = false)
    private String flagName;
    
    @Size(max = 500)
    @Column(name = "flag_description")
    private String flagDescription;
    
    @Column(name = "flag_value", nullable = false)
    private Boolean flagValue = false;
    
    @Column(name = "tenant_id")
    private UUID tenantId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Environment environment = Environment.PRODUCTION;
    
    @Column(name = "rollout_percentage")
    private Integer rolloutPercentage = 0;
    
    @Column(name = "target_users", columnDefinition = "TEXT")
    private String targetUsers;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Size(max = 100)
    @Column(name = "created_by")
    private String createdBy;
    
    @Size(max = 100)
    @Column(name = "updated_by")
    private String updatedBy;
    
    // Constructors
    public FeatureFlag() {}
    
    public FeatureFlag(String flagName, String flagDescription, Boolean flagValue) {
        this.flagName = flagName;
        this.flagDescription = flagDescription;
        this.flagValue = flagValue;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getFlagName() { return flagName; }
    public void setFlagName(String flagName) { this.flagName = flagName; }
    
    public String getFlagDescription() { return flagDescription; }
    public void setFlagDescription(String flagDescription) { this.flagDescription = flagDescription; }
    
    public Boolean getFlagValue() { return flagValue; }
    public void setFlagValue(Boolean flagValue) { this.flagValue = flagValue; }
    
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    
    public Environment getEnvironment() { return environment; }
    public void setEnvironment(Environment environment) { this.environment = environment; }
    
    public Integer getRolloutPercentage() { return rolloutPercentage; }
    public void setRolloutPercentage(Integer rolloutPercentage) { this.rolloutPercentage = rolloutPercentage; }
    
    public String getTargetUsers() { return targetUsers; }
    public void setTargetUsers(String targetUsers) { this.targetUsers = targetUsers; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    public enum Environment {
        DEVELOPMENT, TESTING, STAGING, PRODUCTION
    }
}