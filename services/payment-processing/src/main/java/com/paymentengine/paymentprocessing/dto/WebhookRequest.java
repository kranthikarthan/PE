package com.paymentengine.paymentprocessing.dto;

public class WebhookRequest {
    private String webhookId;
    private String payload;
    // ... add more fields as needed

    public WebhookRequest() {}

    public String getWebhookId() { return webhookId; }
    public void setWebhookId(String webhookId) { this.webhookId = webhookId; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
