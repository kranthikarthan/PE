package com.paymentengine.paymentprocessing.dto;

import com.paymentengine.paymentprocessing.entity.TenantConfiguration;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for tenant cloning responses
 */
public class TenantCloneResponse {

    private boolean success;
    private String message;
    private UUID configurationId;
    private String tenantId;
    private String version;
    private TenantConfiguration.Environment environment;
    private String sourceTenantId;
    private String sourceVersion;
    private LocalDateTime clonedAt;
    private String clonedBy;
    private Map<String, Object> summary;
    private Map<String, Object> warnings;
    private Map<String, Object> errors;

    // Constructors
    public TenantCloneResponse() {}

    public TenantCloneResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public TenantCloneResponse(boolean success, String message, UUID configurationId, String tenantId, String version) {
        this.success = success;
        this.message = message;
        this.configurationId = configurationId;
        this.tenantId = tenantId;
        this.version = version;
    }

    // Static factory methods
    public static TenantCloneResponse success(String message, UUID configurationId, String tenantId, String version) {
        return new TenantCloneResponse(true, message, configurationId, tenantId, version);
    }

    public static TenantCloneResponse error(String message) {
        return new TenantCloneResponse(false, message);
    }

    public static TenantCloneResponse error(String message, Map<String, Object> errors) {
        TenantCloneResponse response = new TenantCloneResponse(false, message);
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

    public TenantConfiguration.Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(TenantConfiguration.Environment environment) {
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

    public LocalDateTime getClonedAt() {
        return clonedAt;
    }

    public void setClonedAt(LocalDateTime clonedAt) {
        this.clonedAt = clonedAt;
    }

    public String getClonedBy() {
        return clonedBy;
    }

    public void setClonedBy(String clonedBy) {
        this.clonedBy = clonedBy;
    }

    public Map<String, Object> getSummary() {
        return summary;
    }

    public void setSummary(Map<String, Object> summary) {
        this.summary = summary;
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