package com.paymentengine.paymentprocessing.dto;

public class KafkaMessageRequest {
    private String messageId;
    private String payload;
    // ... add more fields as needed

    public KafkaMessageRequest() {}

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
