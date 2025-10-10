package com.paymentengine.paymentprocessing.dto;

public class ReconciliationResult {
    private String reconciliationId;
    private boolean success;
    // ... add more fields as needed

    public ReconciliationResult() {}

    public String getReconciliationId() { return reconciliationId; }
    public void setReconciliationId(String reconciliationId) { this.reconciliationId = reconciliationId; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
