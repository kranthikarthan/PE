package com.paymentengine.paymentprocessing.dto;

public class TransactionRecord {
    private String transactionId;
    private double amount;
    // ... add more fields as needed

    public TransactionRecord() {}

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
