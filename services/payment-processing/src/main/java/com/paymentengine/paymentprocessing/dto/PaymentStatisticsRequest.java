package com.paymentengine.paymentprocessing.dto;

public class PaymentStatisticsRequest {
    private String accountId;
    // ... add more fields as needed

    public PaymentStatisticsRequest() {}

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
}
