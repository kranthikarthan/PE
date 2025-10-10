package com.paymentengine.paymentprocessing.dto;

public class PaymentStatistics {
    private String accountId;
    private int transactionCount;
    private double totalAmount;
    // ... add more fields as needed

    public PaymentStatistics() {}

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
}
