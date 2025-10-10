package com.paymentengine.paymentprocessing.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for tenant export responses
 */
public class TenantExportResponse {

    private boolean success;
    private String message;
    private String exportId;
    private String tenantId;
    private String version;
    private String exportFormat;
    private String filePath;
    private String downloadUrl;
    private long fileSize;
    private LocalDateTime exportedAt;
    private String exportedBy;
    private Map<String, Object> exportSummary;
    private List<String> includedVersions;
    private Map<String, Object> metadata;
    private Map<String, Object> warnings;
    private Map<String, Object> errors;

    // Constructors
    public TenantExportResponse() {}

    public TenantExportResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public TenantExportResponse(boolean success, String message, String exportId, String tenantId) {
        this.success = success;
        this.message = message;
        this.exportId = exportId;
        this.tenantId = tenantId;
    }

    // Static factory methods
    public static TenantExportResponse success(String message, String exportId, String tenantId) {
        return new TenantExportResponse(true, message, exportId, tenantId);
    }

    public static TenantExportResponse error(String message) {
        return new TenantExportResponse(false, message);
    }

    public static TenantExportResponse error(String message, Map<String, Object> errors) {
        TenantExportResponse response = new TenantExportResponse(false, message);
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

    public String getExportId() {
        return exportId;
    }

    public void setExportId(String exportId) {
        this.exportId = exportId;
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

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getExportedAt() {
        return exportedAt;
    }

    public void setExportedAt(LocalDateTime exportedAt) {
        this.exportedAt = exportedAt;
    }

    public String getExportedBy() {
        return exportedBy;
    }

    public void setExportedBy(String exportedBy) {
        this.exportedBy = exportedBy;
    }

    public Map<String, Object> getExportSummary() {
        return exportSummary;
    }

    public void setExportSummary(Map<String, Object> exportSummary) {
        this.exportSummary = exportSummary;
    }

    public List<String> getIncludedVersions() {
        return includedVersions;
    }

    public void setIncludedVersions(List<String> includedVersions) {
        this.includedVersions = includedVersions;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
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