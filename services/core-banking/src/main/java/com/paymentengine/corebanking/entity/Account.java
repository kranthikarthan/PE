package com.paymentengine.corebanking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Account entity representing customer bank accounts
 */
@Entity
@Table(name = "accounts", schema = "payment_engine")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "account_number", unique = true, nullable = false, length = 50)
    @NotNull
    @Size(max = 50)
    private String accountNumber;
    
    @Column(name = "customer_id", nullable = false)
    @NotNull
    private UUID customerId;
    
    @Column(name = "account_type_id", nullable = false)
    @NotNull
    private UUID accountTypeId;
    
    @Column(name = "currency_code", length = 3)
    @Size(max = 3)
    private String currencyCode = "USD";
    
    @Column(name = "balance", precision = 15, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "available_balance", precision = 15, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal availableBalance = BigDecimal.ZERO;
    
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.ACTIVE;
    
    @Column(name = "opened_date")
    private LocalDate openedDate;
    
    @Column(name = "closed_date")
    private LocalDate closedDate;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (openedDate == null) {
            openedDate = LocalDate.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Account() {}
    
    public Account(String accountNumber, UUID customerId, UUID accountTypeId) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.accountTypeId = accountTypeId;
    }
    
    // Business methods
    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (availableBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        
        balance = balance.subtract(amount);
        availableBalance = availableBalance.subtract(amount);
    }
    
    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        
        balance = balance.add(amount);
        availableBalance = availableBalance.add(amount);
    }
    
    public void holdFunds(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Hold amount must be positive");
        }
        if (availableBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available balance for hold");
        }
        
        availableBalance = availableBalance.subtract(amount);
    }
    
    public void releaseFunds(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Release amount must be positive");
        }
        
        BigDecimal heldAmount = balance.subtract(availableBalance);
        if (heldAmount.compareTo(amount) < 0) {
            throw new IllegalStateException("Cannot release more than held amount");
        }
        
        availableBalance = availableBalance.add(amount);
    }
    
    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }
    
    public boolean hasSufficientFunds(BigDecimal amount) {
        return availableBalance.compareTo(amount) >= 0;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
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
    
    public AccountStatus getStatus() {
        return status;
    }
    
    public void setStatus(AccountStatus status) {
        this.status = status;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", customerId=" + customerId +
                ", accountTypeId=" + accountTypeId +
                ", currencyCode='" + currencyCode + '\'' +
                ", balance=" + balance +
                ", availableBalance=" + availableBalance +
                ", status=" + status +
                ", openedDate=" + openedDate +
                ", closedDate=" + closedDate +
                '}';
    }
    
    /**
     * Account status enumeration
     */
    public enum AccountStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        CLOSED
    }
}