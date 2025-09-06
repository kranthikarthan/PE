package com.paymentengine.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@JsonTypeName("ACCOUNT_CREATED")
public class AccountCreatedEvent extends PaymentEvent {
    
    @NotNull
    private UUID accountId;
    
    @NotNull
    private String accountNumber;
    
    @NotNull
    private UUID customerId;
    
    @NotNull
    private UUID accountTypeId;
    
    @NotNull
    private String currencyCode;
    
    @NotNull
    private BigDecimal initialBalance;
    
    private String accountStatus;

    public AccountCreatedEvent() {
        super("ACCOUNT_CREATED", "core-banking");
    }

    public AccountCreatedEvent(UUID accountId, String accountNumber, UUID customerId, 
                             UUID accountTypeId, String currencyCode, BigDecimal initialBalance) {
        this();
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.accountTypeId = accountTypeId;
        this.currencyCode = currencyCode;
        this.initialBalance = initialBalance;
        this.accountStatus = "ACTIVE";
    }

    // Getters and Setters
    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getAccountTypeId() {
        return accountTypeId;
    }

    public void setAccountTypeId(UUID accountTypeId) {
        this.accountTypeId = accountTypeId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
}