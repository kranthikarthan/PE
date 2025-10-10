package com.paymentengine.paymentprocessing.dto;

public class WebhookResponse {
    private String webhookId;
    private boolean success;
    // ... add more fields as needed

    public WebhookResponse() {}

    public String getWebhookId() { return webhookId; }
    public void setWebhookId(String webhookId) { this.webhookId = webhookId; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
