package com.payments.samosadapter.service;

import com.payments.domain.shared.TenantContext;
import com.payments.samosadapter.domain.SamosSettlementAccount;
import com.payments.samosadapter.domain.SamosSettlementAccount.AccountStatus;
import com.payments.samosadapter.dto.SamosSettlementAccountCreateRequest;
import com.payments.samosadapter.dto.SamosSettlementAccountResponse;
import com.payments.samosadapter.dto.SamosSettlementAccountBalanceResponse;
import com.payments.samosadapter.exception.SamosSettlementAccountException;
import com.payments.samosadapter.repository.SamosSettlementAccountRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SAMOS Settlement Account Service
 *
 * <p>Manages SARB settlement accounts for RTGS payments. Handles account lifecycle, balance
 * management, fund reservations, and settlement operations.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li>Account creation and management
 *   <li>Real-time balance tracking
 *   <li>Fund reservation for pending settlements
 *   <li>Collateral management
 *   <li>Daily limit enforcement
 *   <li>Operating hours validation
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SamosSettlementAccountService {

  private final SamosSettlementAccountRepository accountRepository;

  // SAMOS operating hours (SAST - South African Standard Time)
  private static final LocalTime SAMOS_START_TIME = LocalTime.of(7, 0); // 07:00
  private static final LocalTime SAMOS_END_TIME = LocalTime.of(16, 30); // 16:30

  /**
   * Create a new SAMOS settlement account
   *
   * @param request Account creation request
   * @return Created account
   */
  @Transactional
  @CircuitBreaker(name = "samos-settlement", fallbackMethod = "createAccountFallback")
  @Retry(name = "samos-settlement")
  public SamosSettlementAccountResponse createAccount(
      SamosSettlementAccountCreateRequest request) {

    log.info(
        "Creating SAMOS settlement account for tenant: {}, account: {}",
        request.getTenantId(),
        request.getAccountNumber());

    // Validate tenant context
    TenantContext tenantContext =
        TenantContext.builder().tenantId(request.getTenantId()).build();

    // Check for duplicate account number
    if (accountRepository.existsByTenantIdAndAccountNumber(
        request.getTenantId(), request.getAccountNumber())) {
      throw new SamosSettlementAccountException(
          "Settlement account already exists with account number: " + request.getAccountNumber());
    }

    // Check for duplicate bank code
    if (accountRepository.existsByTenantIdAndBankCode(
        request.getTenantId(), request.getBankCode())) {
      throw new SamosSettlementAccountException(
          "Settlement account already exists with bank code: " + request.getBankCode());
    }

    // Create account
    SamosSettlementAccount account =
        SamosSettlementAccount.create(
            tenantContext,
            request.getAccountNumber(),
            request.getBankCode(),
            request.getBankName(),
            request.getDailyDebitLimit(),
            request.getCollateralRequirement(),
            request.getCreatedBy());

    // Save account
    account = accountRepository.save(account);

    log.info(
        "Created SAMOS settlement account: {} for tenant: {}",
        account.getAccountNumber(),
        request.getTenantId());

    return mapToResponse(account);
  }

  /**
   * Get settlement account by ID
   *
   * @param tenantId Tenant ID
   * @param accountId Account ID
   * @return Settlement account
   */
  public SamosSettlementAccountResponse getAccount(String tenantId, String accountId) {
    log.debug("Retrieving settlement account: {} for tenant: {}", accountId, tenantId);

    SamosSettlementAccount account =
        accountRepository
            .findById(accountId)
            .filter(a -> a.getTenantContext().getTenantId().equals(tenantId))
            .orElseThrow(
                () ->
                    new SamosSettlementAccountException(
                        "Settlement account not found: " + accountId));

    return mapToResponse(account);
  }

  /**
   * Get settlement account by account number
   *
   * @param tenantId Tenant ID
   * @param accountNumber SARB account number
   * @return Settlement account
   */
  public SamosSettlementAccountResponse getAccountByNumber(String tenantId, String accountNumber) {
    log.debug(
        "Retrieving settlement account by number: {} for tenant: {}", accountNumber, tenantId);

    SamosSettlementAccount account =
        accountRepository
            .findByTenantIdAndAccountNumber(tenantId, accountNumber)
            .orElseThrow(
                () ->
                    new SamosSettlementAccountException(
                        "Settlement account not found with number: " + accountNumber));

    return mapToResponse(account);
  }

  /**
   * Get all settlement accounts for a tenant
   *
   * @param tenantId Tenant ID
   * @return List of settlement accounts
   */
  public List<SamosSettlementAccountResponse> getAllAccounts(String tenantId) {
    log.debug("Retrieving all settlement accounts for tenant: {}", tenantId);

    List<SamosSettlementAccount> accounts = accountRepository.findAllByTenantId(tenantId);

    return accounts.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  /**
   * Get active settlement accounts for a tenant
   *
   * @param tenantId Tenant ID
   * @return List of active settlement accounts
   */
  public List<SamosSettlementAccountResponse> getActiveAccounts(String tenantId) {
    log.debug("Retrieving active settlement accounts for tenant: {}", tenantId);

    List<SamosSettlementAccount> accounts =
        accountRepository.findActiveAccountsByTenantId(tenantId);

    return accounts.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  /**
   * Get account balance information
   *
   * @param tenantId Tenant ID
   * @param accountId Account ID
   * @return Balance information
   */
  public SamosSettlementAccountBalanceResponse getAccountBalance(
      String tenantId, String accountId) {
    log.debug("Retrieving balance for account: {} tenant: {}", accountId, tenantId);

    SamosSettlementAccount account =
        accountRepository
            .findById(accountId)
            .filter(a -> a.getTenantContext().getTenantId().equals(tenantId))
            .orElseThrow(
                () ->
                    new SamosSettlementAccountException(
                        "Settlement account not found: " + accountId));

    return SamosSettlementAccountBalanceResponse.builder()
        .accountId(account.getId())
        .accountNumber(account.getAccountNumber())
        .bankCode(account.getBankCode())
        .currentBalance(account.getCurrentBalance())
        .reservedBalance(account.getReservedBalance())
        .availableBalance(account.getAvailableBalance())
        .collateralPledged(account.getCollateralPledged())
        .collateralRequirement(account.getCollateralRequirement())
        .hasAdequateCollateral(account.hasAdequateCollateral())
        .dailyDebitLimit(account.getDailyDebitLimit())
        .amountSettledToday(account.getAmountSettledToday())
        .settlementsToday(account.getSettlementsToday())
        .remainingDailyLimit(
            account.getDailyDebitLimit().subtract(account.getAmountSettledToday()))
        .lastBalanceUpdate(account.getLastBalanceUpdate())
        .lastSettlementTime(account.getLastSettlementTime())
        .status(account.getStatus().name())
        .currency(account.getCurrency())
        .build();
  }

  /**
   * Credit settlement account
   *
   * @param tenantId Tenant ID
   * @param accountId Account ID
   * @param amount Amount to credit
   * @param updatedBy User performing the operation
   * @return Updated account
   */
  @Transactional
  @CircuitBreaker(name = "samos-settlement")
  @Retry(name = "samos-settlement")
  public SamosSettlementAccountResponse creditAccount(
      String tenantId, String accountId, BigDecimal amount, String updatedBy) {

    log.info("Crediting account: {} with amount: {} ZAR", accountId, amount);

    SamosSettlementAccount account = getAccountEntity(tenantId, accountId);

    account.credit(amount, updatedBy);
    account = accountRepository.save(account);

    log.info(
        "Credited account: {} with {} ZAR. New balance: {} ZAR",
        accountId,
        amount,
        account.getCurrentBalance());

    return mapToResponse(account);
  }

  /**
   * Debit settlement account
   *
   * @param tenantId Tenant ID
   * @param accountId Account ID
   * @param amount Amount to debit
   * @param updatedBy User performing the operation
   * @return Updated account
   */
  @Transactional
  @CircuitBreaker(name = "samos-settlement")
  @Retry(name = "samos-settlement")
  public SamosSettlementAccountResponse debitAccount(
      String tenantId, String accountId, BigDecimal amount, String updatedBy) {

    log.info("Debiting account: {} with amount: {} ZAR", accountId, amount);

    SamosSettlementAccount account = getAccountEntity(tenantId, accountId);

    account.debit(amount, updatedBy);
    account = accountRepository.save(account);

    log.info(
        "Debited account: {} with {} ZAR. New balance: {} ZAR",
        accountId,
        amount,
        account.getCurrentBalance());

    return mapToResponse(account);
  }

  /**
   * Reserve funds for pending settlement
   *
   * @param tenantId Tenant ID
   * @param accountId Account ID
   * @param amount Amount to reserve
   * @param updatedBy User performing the operation
   * @return Updated account
   */
  @Transactional
  @CircuitBreaker(name = "samos-settlement")
  @Retry(name = "samos-settlement")
  public SamosSettlementAccountResponse reserveFunds(
      String tenantId, String accountId, BigDecimal amount, String updatedBy) {

    log.info("Reserving funds on account: {} amount: {} ZAR", accountId, amount);

    // Validate operating hours
    validateOperatingHours();

    SamosSettlementAccount account = getAccountEntity(tenantId, accountId);

    // Check daily limit
    if (!account.isWithinDailyLimit(amount)) {
      throw new SamosSettlementAccountException(
          "Settlement would exceed daily debit limit. Limit: "
              + account.getDailyDebitLimit()
              + " ZAR, Already settled: "
              + account.getAmountSettledToday()
              + " ZAR, Requested: "
              + amount
              + " ZAR");
    }

    account.reserve(amount, updatedBy);
    account = accountRepository.save(account);

    log.info(
        "Reserved {} ZAR on account: {}. Reserved balance: {} ZAR",
        amount,
        accountId,
        account.getReservedBalance());

    return mapToResponse(account);
  }

  /**
   * Release reserved funds
   *
   * @param tenantId Tenant ID
   * @param accountId Account ID
   * @param amount Amount to release
   * @param updatedBy User performing the operation
   * @return Updated account
   */
  @Transactional
  @CircuitBreaker(name = "samos-settlement")
  @Retry(name = "samos-settlement")
  public SamosSettlementAccountResponse releaseReservation(
      String tenantId, String accountId, BigDecimal amount, String updatedBy) {

    log.info("Releasing reservation on account: {} amount: {} ZAR", accountId, amount);

    SamosSettlementAccount account = getAccountEntity(tenantId, accountId);

    account.releaseReservation(amount, updatedBy);
    account = accountRepository.save(account);

    log.info(
        "Released {} ZAR on account: {}. Reserved balance: {} ZAR",
        amount,
        accountId,
        account.getReservedBalance());

    return mapToResponse(account);
  }

  /**
   * Settle reserved funds (complete payment)
   *
   * @param tenantId Tenant ID
   * @param accountId Account ID
   * @param amount Amount to settle
   * @param updatedBy User performing the operation
   * @return Updated account
   */
  @Transactional
  @CircuitBreaker(name = "samos-settlement")
  @Retry(name = "samos-settlement")
  public SamosSettlementAccountResponse settleReservedFunds(
      String tenantId, String accountId, BigDecimal amount, String updatedBy) {

    log.info("Settling reserved funds on account: {} amount: {} ZAR", accountId, amount);

    // Validate operating hours
    validateOperatingHours();

    SamosSettlementAccount account = getAccountEntity(tenantId, accountId);

    account.settleReservedFunds(amount, updatedBy);
    account = accountRepository.save(account);

    log.info(
        "Settled {} ZAR on account: {}. Balance: {} ZAR, Settlements today: {}",
        amount,
        accountId,
        account.getCurrentBalance(),
        account.getSettlementsToday());

    return mapToResponse(account);
  }

  /**
   * Pledge collateral
   *
   * @param tenantId Tenant ID
   * @param accountId Account ID
   * @param amount Collateral amount
   * @param updatedBy User performing the operation
   * @return Updated account
   */
  @Transactional
  public SamosSettlementAccountResponse pledgeCollateral(
      String tenantId, String accountId, BigDecimal amount, String updatedBy) {

    log.info("Pledging collateral on account: {} amount: {} ZAR", accountId, amount);

    SamosSettlementAccount account = getAccountEntity(tenantId, accountId);

    account.pledgeCollateral(amount, updatedBy);
    account = accountRepository.save(account);

    log.info(
        "Pledged {} ZAR collateral on account: {}. Total pledged: {} ZAR",
        amount,
        accountId,
        account.getCollateralPledged());

    return mapToResponse(account);
  }

  /**
   * Release collateral
   *
   * @param tenantId Tenant ID
   * @param accountId Account ID
   * @param amount Collateral amount
   * @param updatedBy User performing the operation
   * @return Updated account
   */
  @Transactional
  public SamosSettlementAccountResponse releaseCollateral(
      String tenantId, String accountId, BigDecimal amount, String updatedBy) {

    log.info("Releasing collateral on account: {} amount: {} ZAR", accountId, amount);

    SamosSettlementAccount account = getAccountEntity(tenantId, accountId);

    account.releaseCollateral(amount, updatedBy);
    account = accountRepository.save(account);

    log.info(
        "Released {} ZAR collateral on account: {}. Remaining pledged: {} ZAR",
        amount,
        accountId,
        account.getCollateralPledged());

    return mapToResponse(account);
  }

  /**
   * Activate settlement account
   *
   * @param tenantId Tenant ID
   * @param accountId Account ID
   * @param updatedBy User performing the operation
   * @return Updated account
   */
  @Transactional
  public SamosSettlementAccountResponse activateAccount(
      String tenantId, String accountId, String updatedBy) {

    log.info("Activating settlement account: {}", accountId);

    SamosSettlementAccount account = getAccountEntity(tenantId, accountId);

    account.activate(updatedBy);
    account = accountRepository.save(account);

    log.info("Activated settlement account: {}", accountId);

    return mapToResponse(account);
  }

  /**
   * Suspend settlement account
   *
   * @param tenantId Tenant ID
   * @param accountId Account ID
   * @param updatedBy User performing the operation
   * @return Updated account
   */
  @Transactional
  public SamosSettlementAccountResponse suspendAccount(
      String tenantId, String accountId, String updatedBy) {

    log.info("Suspending settlement account: {}", accountId);

    SamosSettlementAccount account = getAccountEntity(tenantId, accountId);

    account.suspend(updatedBy);
    account = accountRepository.save(account);

    log.info("Suspended settlement account: {}", accountId);

    return mapToResponse(account);
  }

  /**
   * Close settlement account
   *
   * @param tenantId Tenant ID
   * @param accountId Account ID
   * @param updatedBy User performing the operation
   * @return Updated account
   */
  @Transactional
  public SamosSettlementAccountResponse closeAccount(
      String tenantId, String accountId, String updatedBy) {

    log.info("Closing settlement account: {}", accountId);

    SamosSettlementAccount account = getAccountEntity(tenantId, accountId);

    account.close(updatedBy);
    account = accountRepository.save(account);

    log.info("Closed settlement account: {}", accountId);

    return mapToResponse(account);
  }

  /**
   * Reset daily counters for all accounts (start of business day)
   *
   * @param tenantId Tenant ID
   */
  @Transactional
  public void resetDailyCounters(String tenantId) {
    log.info("Resetting daily counters for tenant: {}", tenantId);

    List<SamosSettlementAccount> accounts =
        accountRepository.findActiveAccountsByTenantId(tenantId);

    accounts.forEach(
        account -> {
          account.resetDailyCounters();
          accountRepository.save(account);
        });

    log.info("Reset daily counters for {} accounts", accounts.size());
  }

  /**
   * Get accounts with insufficient collateral
   *
   * @param tenantId Tenant ID
   * @return List of accounts
   */
  public List<SamosSettlementAccountResponse> getAccountsWithInsufficientCollateral(
      String tenantId) {
    log.debug("Retrieving accounts with insufficient collateral for tenant: {}", tenantId);

    List<SamosSettlementAccount> accounts =
        accountRepository.findAccountsWithInsufficientCollateral(tenantId);

    return accounts.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  /**
   * Get accounts with low balance
   *
   * @param tenantId Tenant ID
   * @param threshold Balance threshold
   * @return List of accounts
   */
  public List<SamosSettlementAccountResponse> getAccountsWithLowBalance(
      String tenantId, BigDecimal threshold) {
    log.debug(
        "Retrieving accounts with low balance (< {} ZAR) for tenant: {}", threshold, tenantId);

    List<SamosSettlementAccount> accounts =
        accountRepository.findAccountsWithLowBalance(tenantId, threshold);

    return accounts.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  // Helper methods

  private SamosSettlementAccount getAccountEntity(String tenantId, String accountId) {
    return accountRepository
        .findById(accountId)
        .filter(a -> a.getTenantContext().getTenantId().equals(tenantId))
        .orElseThrow(
            () ->
                new SamosSettlementAccountException(
                    "Settlement account not found: " + accountId));
  }

  private SamosSettlementAccountResponse mapToResponse(SamosSettlementAccount account) {
    return SamosSettlementAccountResponse.builder()
        .id(account.getId())
        .tenantId(account.getTenantContext().getTenantId())
        .accountNumber(account.getAccountNumber())
        .bankCode(account.getBankCode())
        .bankName(account.getBankName())
        .currentBalance(account.getCurrentBalance())
        .reservedBalance(account.getReservedBalance())
        .availableBalance(account.getAvailableBalance())
        .dailyDebitLimit(account.getDailyDebitLimit())
        .collateralRequirement(account.getCollateralRequirement())
        .collateralPledged(account.getCollateralPledged())
        .hasAdequateCollateral(account.hasAdequateCollateral())
        .status(account.getStatus().name())
        .accountType(account.getAccountType().name())
        .currency(account.getCurrency())
        .lastBalanceUpdate(account.getLastBalanceUpdate())
        .lastSettlementTime(account.getLastSettlementTime())
        .settlementsToday(account.getSettlementsToday())
        .amountSettledToday(account.getAmountSettledToday())
        .sarbContactEmail(account.getSarbContactEmail())
        .sarbContactPhone(account.getSarbContactPhone())
        .createdAt(account.getCreatedAt())
        .updatedAt(account.getUpdatedAt())
        .createdBy(account.getCreatedBy())
        .updatedBy(account.getUpdatedBy())
        .build();
  }

  private void validateOperatingHours() {
    LocalTime now = LocalTime.now();
    if (now.isBefore(SAMOS_START_TIME) || now.isAfter(SAMOS_END_TIME)) {
      throw new SamosSettlementAccountException(
          "SAMOS settlement operations are only allowed between "
              + SAMOS_START_TIME
              + " and "
              + SAMOS_END_TIME
              + " SAST. Current time: "
              + now);
    }
  }

  // Fallback methods for circuit breaker

  private SamosSettlementAccountResponse createAccountFallback(
      SamosSettlementAccountCreateRequest request, Exception ex) {
    log.error(
        "Circuit breaker: Failed to create settlement account for tenant: {} - {}",
        request.getTenantId(),
        ex.getMessage());
    throw new SamosSettlementAccountException(
        "Settlement account service temporarily unavailable. Please try again later.", ex);
  }
}

