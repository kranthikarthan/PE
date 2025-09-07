package com.paymentengine.paymentprocessing.dto;

import com.paymentengine.paymentprocessing.entity.TenantConfiguration;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * DTO for tenant import requests
 */
public class TenantImportRequest {

    @NotBlank(message = "Import data is required")
    private String importData; // JSON/YAML/XML string or file path

    private String importFormat = "JSON"; // JSON, YAML, XML

    @NotBlank(message = "Target tenant ID is required")
    private String targetTenantId;

    private TenantConfiguration.Environment targetEnvironment;

    private String targetVersion; // If null, auto-generates version

    private String name;

    private String description;

    private String changeLog;

    private String importedBy;

    private Boolean activateAfterImport = true;

    private Boolean validateBeforeImport = true;

    private Boolean overwriteExisting = false;

    private Boolean preserveSourceMetadata = true;

    private Map<String, String> configurationOverrides;

    private Map<String, String> metadataOverrides;

    private Map<String, String> additionalMetadata;

    private Map<String, String> importOptions;

    // Constructors
    public TenantImportRequest() {}

    public TenantImportRequest(String importData, String targetTenantId) {
        this.importData = importData;
        this.targetTenantId = targetTenantId;
    }

    public TenantImportRequest(String importData, String targetTenantId, TenantConfiguration.Environment targetEnvironment) {
        this.importData = importData;
        this.targetTenantId = targetTenantId;
        this.targetEnvironment = targetEnvironment;
    }

    // Getters and Setters
    public String getImportData() {
        return importData;
    }

    public void setImportData(String importData) {
        this.importData = importData;
    }

    public String getImportFormat() {
        return importFormat;
    }

    public void setImportFormat(String importFormat) {
        this.importFormat = importFormat;
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

    public String getImportedBy() {
        return importedBy;
    }

    public void setImportedBy(String importedBy) {
        this.importedBy = importedBy;
    }

    public Boolean getActivateAfterImport() {
        return activateAfterImport;
    }

    public void setActivateAfterImport(Boolean activateAfterImport) {
        this.activateAfterImport = activateAfterImport;
    }

    public Boolean getValidateBeforeImport() {
        return validateBeforeImport;
    }

    public void setValidateBeforeImport(Boolean validateBeforeImport) {
        this.validateBeforeImport = validateBeforeImport;
    }

    public Boolean getOverwriteExisting() {
        return overwriteExisting;
    }

    public void setOverwriteExisting(Boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }

    public Boolean getPreserveSourceMetadata() {
        return preserveSourceMetadata;
    }

    public void setPreserveSourceMetadata(Boolean preserveSourceMetadata) {
        this.preserveSourceMetadata = preserveSourceMetadata;
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

    public Map<String, String> getImportOptions() {
        return importOptions;
    }

    public void setImportOptions(Map<String, String> importOptions) {
        this.importOptions = importOptions;
    }
}