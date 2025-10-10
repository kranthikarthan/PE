package com.paymentengine.paymentprocessing.dto;

public class TransactionHistoryRequest {
    private String accountId;
    // ... add more fields as needed

    public TransactionHistoryRequest() {}

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
}
