package com.paymentengine.paymentprocessing.dto;

public class BatchTransactionResult {
    private String batchId;
    private boolean success;
    // ... add more fields as needed

    public BatchTransactionResult() {}

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
