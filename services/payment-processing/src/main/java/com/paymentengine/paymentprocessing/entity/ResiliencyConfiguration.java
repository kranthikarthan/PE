package com.paymentengine.paymentprocessing.entity;

public class ResiliencyConfiguration {
    private String configId;
    // ... add more fields as needed

    public ResiliencyConfiguration() {}

    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }
}
