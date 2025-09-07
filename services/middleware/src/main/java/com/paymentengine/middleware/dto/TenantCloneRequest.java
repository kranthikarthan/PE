package com.paymentengine.middleware.dto;

import com.paymentengine.middleware.entity.TenantConfiguration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * DTO for tenant cloning requests
 */
public class TenantCloneRequest {

    @NotBlank(message = "Source tenant ID is required")
    private String sourceTenantId;

    private String sourceVersion; // If null, uses latest version

    @NotBlank(message = "Target tenant ID is required")
    private String targetTenantId;

    @NotNull(message = "Target environment is required")
    private TenantConfiguration.Environment targetEnvironment;

    private String targetVersion; // If null, auto-generates version

    private String name;

    private String description;

    private String changeLog;

    private String clonedBy;

    private Boolean activateAfterClone = true;

    private Boolean copyMetadata = true;

    private Boolean copyConfigurationData = true;

    private Map<String, String> configurationOverrides;

    private Map<String, String> metadataOverrides;

    private Map<String, String> additionalMetadata;

    // Constructors
    public TenantCloneRequest() {}

    public TenantCloneRequest(String sourceTenantId, String targetTenantId, TenantConfiguration.Environment targetEnvironment) {
        this.sourceTenantId = sourceTenantId;
        this.targetTenantId = targetTenantId;
        this.targetEnvironment = targetEnvironment;
    }

    // Getters and Setters
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

    public String getTargetTenantId() {
        return targetTenantId;
    }

    public void setTargetTenantId(String targetTenantId) {
        this.targetTenantId = targetTenantId;
    }

    public TenantConfiguration.Environment getTargetEnvironment() {
        return targetEnvironment;
    }

    public void setTargetEnvironment(TenantConfiguration.Environment targetEnvironment) {
        this.targetEnvironment = targetEnvironment;
    }

    public String getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;
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

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public String getClonedBy() {
        return clonedBy;
    }

    public void setClonedBy(String clonedBy) {
        this.clonedBy = clonedBy;
    }

    public Boolean getActivateAfterClone() {
        return activateAfterClone;
    }

    public void setActivateAfterClone(Boolean activateAfterClone) {
        this.activateAfterClone = activateAfterClone;
    }

    public Boolean getCopyMetadata() {
        return copyMetadata;
    }

    public void setCopyMetadata(Boolean copyMetadata) {
        this.copyMetadata = copyMetadata;
    }

    public Boolean getCopyConfigurationData() {
        return copyConfigurationData;
    }

    public void setCopyConfigurationData(Boolean copyConfigurationData) {
        this.copyConfigurationData = copyConfigurationData;
    }

    public Map<String, String> getConfigurationOverrides() {
        return configurationOverrides;
    }

    public void setConfigurationOverrides(Map<String, String> configurationOverrides) {
        this.configurationOverrides = configurationOverrides;
    }

    public Map<String, String> getMetadataOverrides() {
        return metadataOverrides;
    }

    public void setMetadataOverrides(Map<String, String> metadataOverrides) {
        this.metadataOverrides = metadataOverrides;
    }

    public Map<String, String> getAdditionalMetadata() {
        return additionalMetadata;
    }

    public void setAdditionalMetadata(Map<String, String> additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
    }
}