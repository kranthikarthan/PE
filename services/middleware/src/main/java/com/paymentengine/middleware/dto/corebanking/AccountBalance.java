package com.paymentengine.middleware.dto.corebanking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account Balance DTO
 */
public class AccountBalance {
    
    private String accountNumber;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal pendingBalance;
    private String currency;
    private LocalDateTime lastUpdated;
    private String status;
    
    // Constructors
    public AccountBalance() {}
    
    public AccountBalance(String accountNumber, BigDecimal balance, BigDecimal availableBalance, String currency) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.availableBalance = availableBalance;
        this.currency = currency;
    }
    
    // Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }
    
    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }
    
    public BigDecimal getPendingBalance() {
        return pendingBalance;
    }
    
    public void setPendingBalance(BigDecimal pendingBalance) {
        this.pendingBalance = pendingBalance;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "AccountBalance{" +
                "accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", availableBalance=" + availableBalance +
                ", currency='" + currency + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}