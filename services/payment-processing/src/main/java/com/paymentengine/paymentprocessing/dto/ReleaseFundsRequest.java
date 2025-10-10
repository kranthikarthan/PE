package com.paymentengine.paymentprocessing.dto;

public class ReleaseFundsRequest {
    private String accountId;
    private double amount;
    // ... add more fields as needed

    public ReleaseFundsRequest() {}

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
