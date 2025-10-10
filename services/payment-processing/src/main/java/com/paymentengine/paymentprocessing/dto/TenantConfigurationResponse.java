package com.paymentengine.paymentprocessing.dto;

public class TenantConfigurationResponse {
    private String tenantId;
    private boolean success;
    // ... add more fields as needed

    public TenantConfigurationResponse() {}

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
