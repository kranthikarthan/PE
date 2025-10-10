package com.paymentengine.paymentprocessing.dto;

public class BatchTransactionRequest {
    private String batchId;
    // ... add more fields as needed

    public BatchTransactionRequest() {}

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
}
