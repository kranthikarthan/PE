package com.paymentengine.paymentprocessing.dto.corebanking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Account Information DTO
 */
public class AccountInfo {
    
    private String accountNumber;
    private String accountName;
    private String accountType;
    private String currency;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private String status;
    private String customerId;
    private String customerName;
    private LocalDate openedDate;
    private LocalDate closedDate;
    private String branchCode;
    private String branchName;
    private String bankCode;
    private String bankName;
    private Map<String, Object> additionalInfo;
    private LocalDateTime lastUpdated;
    
    // Constructors
    public AccountInfo() {}
    
    public AccountInfo(String accountNumber, String accountName, String accountType, 
                      String currency, BigDecimal balance, String status) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.accountType = accountType;
        this.currency = currency;
        this.balance = balance;
        this.status = status;
    }
    
    // Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getAccountName() {
        return accountName;
    }
    
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public LocalDate getOpenedDate() {
        return openedDate;
    }
    
    public void setOpenedDate(LocalDate openedDate) {
        this.openedDate = openedDate;
    }
    
    public LocalDate getClosedDate() {
        return closedDate;
    }
    
    public void setClosedDate(LocalDate closedDate) {
        this.closedDate = closedDate;
    }
    
    public String getBranchCode() {
        return branchCode;
    }
    
    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }
    
    public String getBranchName() {
        return branchName;
    }
    
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }
    
    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    @Override
    public String toString() {
        return "AccountInfo{" +
                "accountNumber='" + accountNumber + '\'' +
                ", accountName='" + accountName + '\'' +
                ", accountType='" + accountType + '\'' +
                ", currency='" + currency + '\'' +
                ", balance=" + balance +
                ", availableBalance=" + availableBalance +
                ", status='" + status + '\'' +
                ", customerId='" + customerId + '\'' +
                ", bankCode='" + bankCode + '\'' +
                '}';
    }
}