package com.paymentengine.middleware.dto;

import com.paymentengine.middleware.entity.TenantConfiguration;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

/**
 * DTO for tenant export requests
 */
public class TenantExportRequest {

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    private String version; // If null, exports latest version

    private List<String> includeVersions; // If specified, exports multiple versions

    private TenantConfiguration.Environment environment;

    private Boolean includeConfigurationData = true;

    private Boolean includeMetadata = true;

    private Boolean includeHistory = false;

    private Boolean includeRelatedConfigurations = false;

    private Map<String, String> exportOptions;

    private String exportFormat = "JSON"; // JSON, YAML, XML

    private String exportPath; // Custom export path

    private Boolean compress = true;

    private String exportedBy;

    private String exportReason;

    // Constructors
    public TenantExportRequest() {}

    public TenantExportRequest(String tenantId) {
        this.tenantId = tenantId;
    }

    public TenantExportRequest(String tenantId, String version) {
        this.tenantId = tenantId;
        this.version = version;
    }

    // Getters and Setters
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

    public List<String> getIncludeVersions() {
        return includeVersions;
    }

    public void setIncludeVersions(List<String> includeVersions) {
        this.includeVersions = includeVersions;
    }

    public TenantConfiguration.Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(TenantConfiguration.Environment environment) {
        this.environment = environment;
    }

    public Boolean getIncludeConfigurationData() {
        return includeConfigurationData;
    }

    public void setIncludeConfigurationData(Boolean includeConfigurationData) {
        this.includeConfigurationData = includeConfigurationData;
    }

    public Boolean getIncludeMetadata() {
        return includeMetadata;
    }

    public void setIncludeMetadata(Boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
    }

    public Boolean getIncludeHistory() {
        return includeHistory;
    }

    public void setIncludeHistory(Boolean includeHistory) {
        this.includeHistory = includeHistory;
    }

    public Boolean getIncludeRelatedConfigurations() {
        return includeRelatedConfigurations;
    }

    public void setIncludeRelatedConfigurations(Boolean includeRelatedConfigurations) {
        this.includeRelatedConfigurations = includeRelatedConfigurations;
    }

    public Map<String, String> getExportOptions() {
        return exportOptions;
    }

    public void setExportOptions(Map<String, String> exportOptions) {
        this.exportOptions = exportOptions;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public String getExportPath() {
        return exportPath;
    }

    public void setExportPath(String exportPath) {
        this.exportPath = exportPath;
    }

    public Boolean getCompress() {
        return compress;
    }

    public void setCompress(Boolean compress) {
        this.compress = compress;
    }

    public String getExportedBy() {
        return exportedBy;
    }

    public void setExportedBy(String exportedBy) {
        this.exportedBy = exportedBy;
    }

    public String getExportReason() {
        return exportReason;
    }

    public void setExportReason(String exportReason) {
        this.exportReason = exportReason;
    }
}