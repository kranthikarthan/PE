package com.paymentengine.paymentprocessing.dto;

public class AccountStatementRequest {
    private String accountId;
    // ... add more fields as needed

    public AccountStatementRequest() {}

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
}
