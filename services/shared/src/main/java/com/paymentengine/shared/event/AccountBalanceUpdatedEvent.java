package com.paymentengine.shared.event;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@JsonTypeName("ACCOUNT_BALANCE_UPDATED")
public class AccountBalanceUpdatedEvent extends PaymentEvent {
    
    @NotNull
    private UUID accountId;
    
    @NotNull
    private String accountNumber;
    
    @NotNull
    private BigDecimal previousBalance;
    
    @NotNull
    private BigDecimal newBalance;
    
    @NotNull
    private BigDecimal previousAvailableBalance;
    
    @NotNull
    private BigDecimal newAvailableBalance;
    
    @NotNull
    private String currencyCode;
    
    private UUID transactionId;
    
    private String transactionReference;
    
    private String updateReason;

    public AccountBalanceUpdatedEvent() {
        super("ACCOUNT_BALANCE_UPDATED", "core-banking");
    }

    public AccountBalanceUpdatedEvent(UUID accountId, String accountNumber, 
                                    BigDecimal previousBalance, BigDecimal newBalance,
                                    BigDecimal previousAvailableBalance, BigDecimal newAvailableBalance,
                                    String currencyCode) {
        this();
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.previousBalance = previousBalance;
        this.newBalance = newBalance;
        this.previousAvailableBalance = previousAvailableBalance;
        this.newAvailableBalance = newAvailableBalance;
        this.currencyCode = currencyCode;
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

    public BigDecimal getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(BigDecimal previousBalance) {
        this.previousBalance = previousBalance;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

    public BigDecimal getPreviousAvailableBalance() {
        return previousAvailableBalance;
    }

    public void setPreviousAvailableBalance(BigDecimal previousAvailableBalance) {
        this.previousAvailableBalance = previousAvailableBalance;
    }

    public BigDecimal getNewAvailableBalance() {
        return newAvailableBalance;
    }

    public void setNewAvailableBalance(BigDecimal newAvailableBalance) {
        this.newAvailableBalance = newAvailableBalance;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getUpdateReason() {
        return updateReason;
    }

    public void setUpdateReason(String updateReason) {
        this.updateReason = updateReason;
    }
}