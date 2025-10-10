package com.paymentengine.paymentprocessing.dto;

public class HoldFundsRequest {
    private String accountId;
    private double amount;
    // ... add more fields as needed

    public HoldFundsRequest() {}

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
