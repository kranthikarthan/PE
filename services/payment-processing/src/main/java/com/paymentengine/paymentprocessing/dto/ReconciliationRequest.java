package com.paymentengine.paymentprocessing.dto;

public class ReconciliationRequest {
    private String reconciliationId;
    // ... add more fields as needed

    public ReconciliationRequest() {}

    public String getReconciliationId() { return reconciliationId; }
    public void setReconciliationId(String reconciliationId) { this.reconciliationId = reconciliationId; }
}
