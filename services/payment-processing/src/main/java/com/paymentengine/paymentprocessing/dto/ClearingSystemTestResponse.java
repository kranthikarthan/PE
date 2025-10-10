package com.paymentengine.paymentprocessing.dto;

public class ClearingSystemTestResponse {
    private String testId;
    private boolean success;
    // ... add more fields as needed

    public ClearingSystemTestResponse() {}

    public String getTestId() { return testId; }
    public void setTestId(String testId) { this.testId = testId; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
