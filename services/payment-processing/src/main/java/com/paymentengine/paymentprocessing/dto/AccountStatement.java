package com.paymentengine.paymentprocessing.dto;

public class AccountStatement {
    private String accountId;
    private double balance;
    // ... add more fields as needed

    public AccountStatement() {}

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}
