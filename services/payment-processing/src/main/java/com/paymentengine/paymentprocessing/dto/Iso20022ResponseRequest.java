package com.paymentengine.paymentprocessing.dto;

public class Iso20022ResponseRequest {
    private String transactionId;
    private String message;
    // ... add more fields as needed

    public Iso20022ResponseRequest() {}

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
