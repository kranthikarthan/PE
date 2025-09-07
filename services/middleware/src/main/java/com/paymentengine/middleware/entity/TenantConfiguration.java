package com.paymentengine.middleware.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity for storing tenant configuration versions
 * Supports versioning and migration of tenant configurations
 */
@Entity
@Table(name = "tenant_configuration_versions", indexes = {
    @Index(name = "idx_tenant_config_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_tenant_config_version", columnList = "version"),
    @Index(name = "idx_tenant_config_active", columnList = "isActive"),
    @Index(name = "idx_tenant_config_created_at", columnList = "createdAt")
})
public class TenantConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String tenantId;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean isActive = false;

    @Column(nullable = false)
    private Boolean isDefault = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Environment environment;

    @Column(length = 50)
    private String sourceTenantId; // For cloned configurations

    @Column(length = 50)
    private String sourceVersion; // Source version for cloned configurations

    @Column(length = 100)
    private String clonedBy;

    @Column
    private LocalDateTime clonedAt;

    @ElementCollection
    @CollectionTable(name = "tenant_configuration_data", joinColumns = @JoinColumn(name = "tenant_configuration_id"))
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value", columnDefinition = "TEXT")
    private Map<String, String> configurationData;

    @ElementCollection
    @CollectionTable(name = "tenant_configuration_metadata", joinColumns = @JoinColumn(name = "tenant_configuration_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value", columnDefinition = "TEXT")
    private Map<String, String> metadata;

    @Column(columnDefinition = "TEXT")
    private String changeLog;

    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public TenantConfiguration() {}

    public TenantConfiguration(String tenantId, String version, Environment environment) {
        this.tenantId = tenantId;
        this.version = version;
        this.environment = environment;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getSourceTenantId() {
        return sourceTenantId;
    }

    public void setSourceTenantId(String sourceTenantId) {
        this.sourceTenantId = sourceTenantId;
    }

    public String getSourceVersion() {
        return sourceVersion;
    }

    public void setSourceVersion(String sourceVersion) {
        this.sourceVersion = sourceVersion;
    }

    public String getClonedBy() {
        return clonedBy;
    }

    public void setClonedBy(String clonedBy) {
        this.clonedBy = clonedBy;
    }

    public LocalDateTime getClonedAt() {
        return clonedAt;
    }

    public void setClonedAt(LocalDateTime clonedAt) {
        this.clonedAt = clonedAt;
    }

    public Map<String, String> getConfigurationData() {
        return configurationData;
    }

    public void setConfigurationData(Map<String, String> configurationData) {
        this.configurationData = configurationData;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
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

    // Environment enum
    public enum Environment {
        DEVELOPMENT("Development"),
        INTEGRATION("Integration Testing"),
        USER_ACCEPTANCE("User Acceptance Testing"),
        PRODUCTION("Production");

        private final String displayName;

        Environment(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}