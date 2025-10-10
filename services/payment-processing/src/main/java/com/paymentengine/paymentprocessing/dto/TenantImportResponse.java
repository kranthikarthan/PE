package com.paymentengine.paymentprocessing.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for tenant import responses
 */
public class TenantImportResponse {

    private boolean success;
    private String message;
    private UUID configurationId;
    private String tenantId;
    private String version;
    private LocalDateTime importedAt;
    private String importedBy;
    private Map<String, Object> importSummary;
    private List<String> importedVersions;
    private Map<String, Object> validationResults;
    private Map<String, Object> warnings;
    private Map<String, Object> errors;

    // Constructors
    public TenantImportResponse() {}

    public TenantImportResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public TenantImportResponse(boolean success, String message, UUID configurationId, String tenantId, String version) {
        this.success = success;
        this.message = message;
        this.configurationId = configurationId;
        this.tenantId = tenantId;
        this.version = version;
    }

    // Static factory methods
    public static TenantImportResponse success(String message, UUID configurationId, String tenantId, String version) {
        return new TenantImportResponse(true, message, configurationId, tenantId, version);
    }

    public static TenantImportResponse error(String message) {
        return new TenantImportResponse(false, message);
    }

    public static TenantImportResponse error(String message, Map<String, Object> errors) {
        TenantImportResponse response = new TenantImportResponse(false, message);
        response.setErrors(errors);
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(UUID configurationId) {
        this.configurationId = configurationId;
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

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }

    public String getImportedBy() {
        return importedBy;
    }

    public void setImportedBy(String importedBy) {
        this.importedBy = importedBy;
    }

    public Map<String, Object> getImportSummary() {
        return importSummary;
    }

    public void setImportSummary(Map<String, Object> importSummary) {
        this.importSummary = importSummary;
    }

    public List<String> getImportedVersions() {
        return importedVersions;
    }

    public void setImportedVersions(List<String> importedVersions) {
        this.importedVersions = importedVersions;
    }

    public Map<String, Object> getValidationResults() {
        return validationResults;
    }

    public void setValidationResults(Map<String, Object> validationResults) {
        this.validationResults = validationResults;
    }

    public Map<String, Object> getWarnings() {
        return warnings;
    }

    public void setWarnings(Map<String, Object> warnings) {
        this.warnings = warnings;
    }

    public Map<String, Object> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, Object> errors) {
        this.errors = errors;
    }
}