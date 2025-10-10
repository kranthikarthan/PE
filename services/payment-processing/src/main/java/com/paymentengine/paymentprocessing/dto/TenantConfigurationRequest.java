package com.paymentengine.paymentprocessing.dto;

public class TenantConfigurationRequest {
    private String tenantId;
    // ... add more fields as needed

    public TenantConfigurationRequest() {}

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
