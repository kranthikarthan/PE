package com.paymentengine.paymentprocessing.dto;

public class KafkaMessageResponse {
    private String messageId;
    private boolean success;
    // ... add more fields as needed

    public KafkaMessageResponse() {}

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
