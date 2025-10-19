package com.payments.samosadapter.domain;

import com.payments.domain.shared.TenantContext;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

/**
 * SAMOS Settlement Account Entity
 *
 * <p>Represents a settlement account with SARB (South African Reserve Bank) for RTGS (Real-Time
 * Gross Settlement) payments through SAMOS.
 *
 * <p>Each participating bank must have a settlement account with SARB to participate in SAMOS.
 * These accounts are used for real-time settlement of high-value payments.
 *
 * <p>Key characteristics:
 *
 * <ul>
 *   <li>Unique SARB account number (9-digit)
 *   <li>Real-time balance tracking
 *   <li>Daily liquidity limits
 *   <li>Collateral requirements
 *   <li>Operating hours compliance (07:00-16:30 SAST)
 *   <li>Multi-tenancy support
 * </ul>
 */
@Entity
@Table(
    name = "samos_settlement_accounts",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"tenant_id", "account_number"}),
      @UniqueConstraint(columnNames = {"tenant_id", "bank_code"})
    },
    indexes = {
      @Index(name = "idx_settlement_tenant", columnList = "tenant_id"),
      @Index(name = "idx_settlement_account_number", columnList = "account_number"),
      @Index(name = "idx_settlement_bank_code", columnList = "bank_code"),
      @Index(name = "idx_settlement_status", columnList = "status")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class SamosSettlementAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Embedded
  @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id"))
  private TenantContext tenantContext;

  /** SARB settlement account number (9-digit) */
  @Column(name = "account_number", nullable = false, length = 9)
  private String accountNumber;

  /** Participating bank code (SARB member code) */
  @Column(name = "bank_code", nullable = false, length = 6)
  private String bankCode;

  /** Bank name */
  @Column(name = "bank_name", nullable = false)
  private String bankName;

  /** Current available balance in ZAR */
  @Column(name = "current_balance", nullable = false, precision = 18, scale = 2)
  private BigDecimal currentBalance = BigDecimal.ZERO;

  /** Reserved balance (pending settlements) in ZAR */
  @Column(name = "reserved_balance", nullable = false, precision = 18, scale = 2)
  private BigDecimal reservedBalance = BigDecimal.ZERO;

  /** Daily debit limit in ZAR */
  @Column(name = "daily_debit_limit", nullable = false, precision = 18, scale = 2)
  private BigDecimal dailyDebitLimit;

  /** Minimum collateral requirement in ZAR */
  @Column(name = "collateral_requirement", nullable = false, precision = 18, scale = 2)
  private BigDecimal collateralRequirement;

  /** Current collateral pledged in ZAR */
  @Column(name = "collateral_pledged", nullable = false, precision = 18, scale = 2)
  private BigDecimal collateralPledged = BigDecimal.ZERO;

  /** Account status */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccountStatus status = AccountStatus.ACTIVE;

  /** Account type */
  @Enumerated(EnumType.STRING)
  @Column(name = "account_type", nullable = false)
  private AccountType accountType = AccountType.SETTLEMENT;

  /** Currency (always ZAR for SAMOS) */
  @Column(nullable = false, length = 3)
  private String currency = "ZAR";

  /** Last balance update timestamp */
  @Column(name = "last_balance_update")
  private Instant lastBalanceUpdate;

  /** Last settlement timestamp */
  @Column(name = "last_settlement_time")
  private Instant lastSettlementTime;

  /** Number of settlements today */
  @Column(name = "settlements_today")
  private Integer settlementsToday = 0;

  /** Total amount settled today in ZAR */
  @Column(name = "amount_settled_today", precision = 18, scale = 2)
  private BigDecimal amountSettledToday = BigDecimal.ZERO;

  /** SARB contact information */
  @Column(name = "sarb_contact_email")
  private String sarbContactEmail;

  @Column(name = "sarb_contact_phone")
  private String sarbContactPhone;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_by")
  private String updatedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "samos_adapter_id")
  private SamosAdapter samosAdapter;

  /**
   * Create a new SAMOS settlement account
   *
   * @param tenantContext The tenant context
   * @param accountNumber SARB account number (9-digit)
   * @param bankCode SARB member bank code
   * @param bankName Bank name
   * @param dailyDebitLimit Daily debit limit in ZAR
   * @param collateralRequirement Minimum collateral requirement
   * @param createdBy User creating the account
   * @return New settlement account
   */
  public static SamosSettlementAccount create(
      TenantContext tenantContext,
      String accountNumber,
      String bankCode,
      String bankName,
      BigDecimal dailyDebitLimit,
      BigDecimal collateralRequirement,
      String createdBy) {

    validateAccountNumber(accountNumber);
    validateBankCode(bankCode);

    if (bankName == null || bankName.isBlank()) {
      throw new IllegalArgumentException("Bank name cannot be null or blank");
    }

    if (dailyDebitLimit == null || dailyDebitLimit.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Daily debit limit must be positive");
    }

    if (collateralRequirement == null || collateralRequirement.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Collateral requirement cannot be negative");
    }

    SamosSettlementAccount account = new SamosSettlementAccount();
    account.tenantContext = tenantContext;
    account.accountNumber = accountNumber;
    account.bankCode = bankCode;
    account.bankName = bankName;
    account.dailyDebitLimit = dailyDebitLimit;
    account.collateralRequirement = collateralRequirement;
    account.createdBy = createdBy;
    account.updatedBy = createdBy;
    account.createdAt = Instant.now();
    account.updatedAt = Instant.now();

    return account;
  }

  /**
   * Update account balance (credit)
   *
   * @param amount Amount to credit
   * @param updatedBy User performing the update
   */
  public void credit(BigDecimal amount, String updatedBy) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Credit amount must be positive");
    }

    if (!isActive()) {
      throw new IllegalStateException(
          "Cannot credit inactive account: " + accountNumber + " (status: " + status + ")");
    }

    this.currentBalance = this.currentBalance.add(amount);
    this.lastBalanceUpdate = Instant.now();
    this.updatedBy = updatedBy;
    this.updatedAt = Instant.now();
  }

  /**
   * Update account balance (debit)
   *
   * @param amount Amount to debit
   * @param updatedBy User performing the update
   */
  public void debit(BigDecimal amount, String updatedBy) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Debit amount must be positive");
    }

    if (!isActive()) {
      throw new IllegalStateException(
          "Cannot debit inactive account: " + accountNumber + " (status: " + status + ")");
    }

    BigDecimal availableBalance = getAvailableBalance();
    if (availableBalance.compareTo(amount) < 0) {
      throw new IllegalStateException(
          "Insufficient balance. Available: "
              + availableBalance
              + " ZAR, Required: "
              + amount
              + " ZAR");
    }

    this.currentBalance = this.currentBalance.subtract(amount);
    this.lastBalanceUpdate = Instant.now();
    this.updatedBy = updatedBy;
    this.updatedAt = Instant.now();
  }

  /**
   * Reserve funds for pending settlement
   *
   * @param amount Amount to reserve
   * @param updatedBy User performing the operation
   */
  public void reserve(BigDecimal amount, String updatedBy) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Reserve amount must be positive");
    }

    if (!isActive()) {
      throw new IllegalStateException("Cannot reserve funds on inactive account: " + accountNumber);
    }

    BigDecimal availableBalance = getAvailableBalance();
    if (availableBalance.compareTo(amount) < 0) {
      throw new IllegalStateException(
          "Insufficient balance to reserve. Available: "
              + availableBalance
              + " ZAR, Required: "
              + amount
              + " ZAR");
    }

    this.reservedBalance = this.reservedBalance.add(amount);
    this.updatedBy = updatedBy;
    this.updatedAt = Instant.now();
  }

  /**
   * Release reserved funds
   *
   * @param amount Amount to release
   * @param updatedBy User performing the operation
   */
  public void releaseReservation(BigDecimal amount, String updatedBy) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Release amount must be positive");
    }

    if (this.reservedBalance.compareTo(amount) < 0) {
      throw new IllegalStateException(
          "Cannot release more than reserved. Reserved: "
              + this.reservedBalance
              + " ZAR, Requested: "
              + amount
              + " ZAR");
    }

    this.reservedBalance = this.reservedBalance.subtract(amount);
    this.updatedBy = updatedBy;
    this.updatedAt = Instant.now();
  }

  /**
   * Settle reserved funds (deduct from balance and reservation)
   *
   * @param amount Amount to settle
   * @param updatedBy User performing the settlement
   */
  public void settleReservedFunds(BigDecimal amount, String updatedBy) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Settlement amount must be positive");
    }

    if (this.reservedBalance.compareTo(amount) < 0) {
      throw new IllegalStateException(
          "Insufficient reserved balance. Reserved: "
              + this.reservedBalance
              + " ZAR, Required: "
              + amount
              + " ZAR");
    }

    // Release reservation and debit current balance
    this.reservedBalance = this.reservedBalance.subtract(amount);
    this.currentBalance = this.currentBalance.subtract(amount);

    // Update settlement tracking
    this.settlementsToday = (this.settlementsToday == null ? 0 : this.settlementsToday) + 1;
    this.amountSettledToday =
        (this.amountSettledToday == null ? BigDecimal.ZERO : this.amountSettledToday).add(amount);
    this.lastSettlementTime = Instant.now();

    this.lastBalanceUpdate = Instant.now();
    this.updatedBy = updatedBy;
    this.updatedAt = Instant.now();
  }

  /**
   * Pledge collateral
   *
   * @param amount Collateral amount to pledge
   * @param updatedBy User performing the operation
   */
  public void pledgeCollateral(BigDecimal amount, String updatedBy) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Collateral pledge amount must be positive");
    }

    this.collateralPledged = this.collateralPledged.add(amount);
    this.updatedBy = updatedBy;
    this.updatedAt = Instant.now();
  }

  /**
   * Release collateral
   *
   * @param amount Collateral amount to release
   * @param updatedBy User performing the operation
   */
  public void releaseCollateral(BigDecimal amount, String updatedBy) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Collateral release amount must be positive");
    }

    if (this.collateralPledged.compareTo(amount) < 0) {
      throw new IllegalStateException(
          "Cannot release more than pledged. Pledged: "
              + this.collateralPledged
              + " ZAR, Requested: "
              + amount
              + " ZAR");
    }

    BigDecimal remainingCollateral = this.collateralPledged.subtract(amount);
    if (remainingCollateral.compareTo(this.collateralRequirement) < 0) {
      throw new IllegalStateException(
          "Cannot release collateral below requirement. Required: "
              + this.collateralRequirement
              + " ZAR, After release: "
              + remainingCollateral
              + " ZAR");
    }

    this.collateralPledged = remainingCollateral;
    this.updatedBy = updatedBy;
    this.updatedAt = Instant.now();
  }

  /** Reset daily counters (called at start of business day) */
  public void resetDailyCounters() {
    this.settlementsToday = 0;
    this.amountSettledToday = BigDecimal.ZERO;
    this.updatedAt = Instant.now();
  }

  /** Activate account */
  public void activate(String updatedBy) {
    if (isActive()) {
      throw new IllegalStateException("Account is already active: " + accountNumber);
    }

    this.status = AccountStatus.ACTIVE;
    this.updatedBy = updatedBy;
    this.updatedAt = Instant.now();
  }

  /** Suspend account */
  public void suspend(String updatedBy) {
    if (isSuspended()) {
      throw new IllegalStateException("Account is already suspended: " + accountNumber);
    }

    this.status = AccountStatus.SUSPENDED;
    this.updatedBy = updatedBy;
    this.updatedAt = Instant.now();
  }

  /** Close account */
  public void close(String updatedBy) {
    if (isClosed()) {
      throw new IllegalStateException("Account is already closed: " + accountNumber);
    }

    if (this.currentBalance.compareTo(BigDecimal.ZERO) != 0) {
      throw new IllegalStateException(
          "Cannot close account with non-zero balance: " + this.currentBalance + " ZAR");
    }

    if (this.reservedBalance.compareTo(BigDecimal.ZERO) != 0) {
      throw new IllegalStateException(
          "Cannot close account with reserved funds: " + this.reservedBalance + " ZAR");
    }

    this.status = AccountStatus.CLOSED;
    this.updatedBy = updatedBy;
    this.updatedAt = Instant.now();
  }

  /** Get available balance (current - reserved) */
  public BigDecimal getAvailableBalance() {
    return this.currentBalance.subtract(this.reservedBalance);
  }

  /** Check if account is active */
  public boolean isActive() {
    return this.status == AccountStatus.ACTIVE;
  }

  /** Check if account is suspended */
  public boolean isSuspended() {
    return this.status == AccountStatus.SUSPENDED;
  }

  /** Check if account is closed */
  public boolean isClosed() {
    return this.status == AccountStatus.CLOSED;
  }

  /** Check if collateral is sufficient */
  public boolean hasAdequateCollateral() {
    return this.collateralPledged.compareTo(this.collateralRequirement) >= 0;
  }

  /** Check if within daily debit limit */
  public boolean isWithinDailyLimit(BigDecimal additionalAmount) {
    BigDecimal totalToday =
        (this.amountSettledToday == null ? BigDecimal.ZERO : this.amountSettledToday)
            .add(additionalAmount);
    return totalToday.compareTo(this.dailyDebitLimit) <= 0;
  }

  /** Validate SARB account number format (9 digits) */
  private static void validateAccountNumber(String accountNumber) {
    if (accountNumber == null || !accountNumber.matches("\\d{9}")) {
      throw new IllegalArgumentException(
          "Invalid SARB account number. Must be 9 digits. Got: " + accountNumber);
    }
  }

  /** Validate bank code format */
  private static void validateBankCode(String bankCode) {
    if (bankCode == null || bankCode.isBlank() || bankCode.length() > 6) {
      throw new IllegalArgumentException(
          "Invalid bank code. Must be non-blank and max 6 characters. Got: " + bankCode);
    }
  }

  /** Account Status */
  public enum AccountStatus {
    ACTIVE,
    SUSPENDED,
    CLOSED
  }

  /** Account Type */
  public enum AccountType {
    SETTLEMENT, // Primary settlement account
    COLLATERAL, // Collateral account
    BACKUP // Backup settlement account
  }
}

