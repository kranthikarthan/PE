package com.paymentengine.paymentprocessing.dto;

public class Iso20022PaymentResponse {
    private String transactionId;
    private boolean success;
    private String details;
    // ... add more fields as needed

    public Iso20022PaymentResponse() {}

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
