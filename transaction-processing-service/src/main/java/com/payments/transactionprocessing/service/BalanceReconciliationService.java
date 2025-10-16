package com.payments.transactionprocessing.service;

import com.payments.domain.shared.AccountNumber;
import com.payments.domain.shared.TenantContext;
import com.payments.domain.transaction.LedgerEntry;
import com.payments.domain.transaction.LedgerEntryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for reconciling account balances and ensuring data integrity
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceReconciliationService {

    private final LedgerService ledgerService;
    private final AccountBalanceService accountBalanceService;
    private final BalanceValidationService balanceValidationService;

    /**
     * Performs a comprehensive balance reconciliation for a tenant
     */
    @Transactional(readOnly = true)
    public ReconciliationResult reconcileAllBalances(TenantContext tenantContext) {
        log.info("Starting balance reconciliation for tenant {}", tenantContext.getTenantId());

        ReconciliationResult result = new ReconciliationResult();
        result.setTenantId(tenantContext.getTenantId());
        result.setReconciliationDate(LocalDate.now());

        // Get all accounts for the tenant
        List<AccountNumber> accounts = ledgerService.getAllAccountsForTenant(tenantContext);
        result.setTotalAccounts(accounts.size());

        BigDecimal totalSystemBalance = BigDecimal.ZERO;
        int discrepanciesFound = 0;

        for (AccountNumber account : accounts) {
            AccountReconciliation accountReconciliation = reconcileAccountBalance(account, tenantContext);
            result.addAccountReconciliation(accountReconciliation);

            if (!accountReconciliation.isBalanced()) {
                discrepanciesFound++;
                log.warn("Balance discrepancy found for account {}: expected {} but calculated {}", 
                        account.getValue(), accountReconciliation.getCachedBalance(), 
                        accountReconciliation.getCalculatedBalance());
            }

            totalSystemBalance = totalSystemBalance.add(accountReconciliation.getCalculatedBalance());
        }

        result.setTotalDiscrepancies(discrepanciesFound);
        result.setSystemBalance(totalSystemBalance);
        result.setSystemBalanced(totalSystemBalance.compareTo(BigDecimal.ZERO) == 0);

        // Validate ledger integrity
        boolean ledgerIntegrity = balanceValidationService.validateLedgerIntegrity(tenantContext);
        result.setLedgerIntegrityValid(ledgerIntegrity);

        log.info("Balance reconciliation completed for tenant {}: {} discrepancies found, system balanced: {}", 
                tenantContext.getTenantId(), discrepanciesFound, result.isSystemBalanced());

        return result;
    }

    /**
     * Reconciles balance for a specific account
     */
    @Transactional(readOnly = true)
    public AccountReconciliation reconcileAccountBalance(AccountNumber accountNumber, TenantContext tenantContext) {
        log.debug("Reconciling balance for account {} and tenant {}", 
                accountNumber.getValue(), tenantContext.getTenantId());

        AccountReconciliation reconciliation = new AccountReconciliation();
        reconciliation.setAccountNumber(accountNumber.getValue());
        reconciliation.setTenantId(tenantContext.getTenantId());

        // Get cached balance
        BigDecimal cachedBalance = accountBalanceService.getCurrentBalance(accountNumber, tenantContext);
        reconciliation.setCachedBalance(cachedBalance);

        // Calculate balance from ledger entries
        BigDecimal calculatedBalance = calculateBalanceFromLedger(accountNumber, tenantContext);
        reconciliation.setCalculatedBalance(calculatedBalance);

        // Check if balances match
        boolean isBalanced = cachedBalance.equals(calculatedBalance);
        reconciliation.setBalanced(isBalanced);

        if (!isBalanced) {
            BigDecimal difference = calculatedBalance.subtract(cachedBalance);
            reconciliation.setDifference(difference);
            log.warn("Balance mismatch for account {}: cached={}, calculated={}, difference={}", 
                    accountNumber.getValue(), cachedBalance, calculatedBalance, difference);
        }

        return reconciliation;
    }

    /**
     * Reconciles balances for a specific date range
     */
    @Transactional(readOnly = true)
    public ReconciliationResult reconcileBalancesForDateRange(TenantContext tenantContext, 
                                                           LocalDate startDate, 
                                                           LocalDate endDate) {
        log.info("Reconciling balances for tenant {} from {} to {}", 
                tenantContext.getTenantId(), startDate, endDate);

        ReconciliationResult result = new ReconciliationResult();
        result.setTenantId(tenantContext.getTenantId());
        result.setReconciliationDate(LocalDate.now());
        result.setStartDate(startDate);
        result.setEndDate(endDate);

        List<AccountNumber> accounts = ledgerService.getAllAccountsForTenant(tenantContext);
        result.setTotalAccounts(accounts.size());

        int discrepanciesFound = 0;
        BigDecimal totalSystemBalance = BigDecimal.ZERO;

        for (AccountNumber account : accounts) {
            // Get entries for the date range
            List<LedgerEntry> entries = ledgerService.getAccountEntriesByDateRange(
                    account, tenantContext, startDate, endDate);

            if (entries.isEmpty()) {
                continue;
            }

            // Calculate balance for this period
            BigDecimal periodBalance = calculateBalanceFromEntries(entries);
            totalSystemBalance = totalSystemBalance.add(periodBalance);

            // Get cached balance
            BigDecimal cachedBalance = accountBalanceService.getCurrentBalance(account, tenantContext);

            if (!cachedBalance.equals(periodBalance)) {
                discrepanciesFound++;
                log.warn("Period balance discrepancy for account {}: cached={}, period={}", 
                        account.getValue(), cachedBalance, periodBalance);
            }
        }

        result.setTotalDiscrepancies(discrepanciesFound);
        result.setSystemBalance(totalSystemBalance);
        result.setSystemBalanced(totalSystemBalance.compareTo(BigDecimal.ZERO) == 0);

        log.info("Period reconciliation completed: {} discrepancies found", discrepanciesFound);
        return result;
    }

    /**
     * Fixes balance discrepancies by updating cached balances
     */
    @Transactional
    public void fixBalanceDiscrepancies(TenantContext tenantContext) {
        log.info("Fixing balance discrepancies for tenant {}", tenantContext.getTenantId());

        List<AccountNumber> accounts = ledgerService.getAllAccountsForTenant(tenantContext);
        int fixedCount = 0;

        for (AccountNumber account : accounts) {
            AccountReconciliation reconciliation = reconcileAccountBalance(account, tenantContext);
            
            if (!reconciliation.isBalanced()) {
                // Update the cached balance with the calculated balance
                accountBalanceService.updateBalance(account, tenantContext, reconciliation.getCalculatedBalance());
                fixedCount++;
                
                log.info("Fixed balance for account {}: updated from {} to {}", 
                        account.getValue(), reconciliation.getCachedBalance(), 
                        reconciliation.getCalculatedBalance());
            }
        }

        log.info("Fixed {} balance discrepancies for tenant {}", fixedCount, tenantContext.getTenantId());
    }

    /**
     * Validates that all account balances are consistent
     */
    @Transactional(readOnly = true)
    public boolean validateSystemBalance(TenantContext tenantContext) {
        log.debug("Validating system balance for tenant {}", tenantContext.getTenantId());

        return balanceValidationService.validateAllAccountBalances(tenantContext);
    }

    private BigDecimal calculateBalanceFromLedger(AccountNumber accountNumber, TenantContext tenantContext) {
        List<LedgerEntry> entries = ledgerService.getAccountEntries(accountNumber, tenantContext);
        return calculateBalanceFromEntries(entries);
    }

    private BigDecimal calculateBalanceFromEntries(List<LedgerEntry> entries) {
        BigDecimal balance = BigDecimal.ZERO;
        
        for (LedgerEntry entry : entries) {
            if (entry.getEntryType() == LedgerEntryType.DEBIT) {
                balance = balance.subtract(entry.getAmount());
            } else {
                balance = balance.add(entry.getAmount());
            }
        }
        
        return balance;
    }

    /**
     * Result of balance reconciliation
     */
    public static class ReconciliationResult {
        private String tenantId;
        private LocalDate reconciliationDate;
        private LocalDate startDate;
        private LocalDate endDate;
        private int totalAccounts;
        private int totalDiscrepancies;
        private BigDecimal systemBalance;
        private boolean systemBalanced;
        private boolean ledgerIntegrityValid;
        private List<AccountReconciliation> accountReconciliations;

        // Getters and setters
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }

        public LocalDate getReconciliationDate() { return reconciliationDate; }
        public void setReconciliationDate(LocalDate reconciliationDate) { this.reconciliationDate = reconciliationDate; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        public int getTotalAccounts() { return totalAccounts; }
        public void setTotalAccounts(int totalAccounts) { this.totalAccounts = totalAccounts; }

        public int getTotalDiscrepancies() { return totalDiscrepancies; }
        public void setTotalDiscrepancies(int totalDiscrepancies) { this.totalDiscrepancies = totalDiscrepancies; }

        public BigDecimal getSystemBalance() { return systemBalance; }
        public void setSystemBalance(BigDecimal systemBalance) { this.systemBalance = systemBalance; }

        public boolean isSystemBalanced() { return systemBalanced; }
        public void setSystemBalanced(boolean systemBalanced) { this.systemBalanced = systemBalanced; }

        public boolean isLedgerIntegrityValid() { return ledgerIntegrityValid; }
        public void setLedgerIntegrityValid(boolean ledgerIntegrityValid) { this.ledgerIntegrityValid = ledgerIntegrityValid; }

        public List<AccountReconciliation> getAccountReconciliations() { return accountReconciliations; }
        public void setAccountReconciliations(List<AccountReconciliation> accountReconciliations) { this.accountReconciliations = accountReconciliations; }

        public void addAccountReconciliation(AccountReconciliation reconciliation) {
            if (this.accountReconciliations == null) {
                this.accountReconciliations = new java.util.ArrayList<>();
            }
            this.accountReconciliations.add(reconciliation);
        }
    }

    /**
     * Result of account balance reconciliation
     */
    public static class AccountReconciliation {
        private String accountNumber;
        private String tenantId;
        private BigDecimal cachedBalance;
        private BigDecimal calculatedBalance;
        private BigDecimal difference;
        private boolean balanced;

        // Getters and setters
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }

        public BigDecimal getCachedBalance() { return cachedBalance; }
        public void setCachedBalance(BigDecimal cachedBalance) { this.cachedBalance = cachedBalance; }

        public BigDecimal getCalculatedBalance() { return calculatedBalance; }
        public void setCalculatedBalance(BigDecimal calculatedBalance) { this.calculatedBalance = calculatedBalance; }

        public BigDecimal getDifference() { return difference; }
        public void setDifference(BigDecimal difference) { this.difference = difference; }

        public boolean isBalanced() { return balanced; }
        public void setBalanced(boolean balanced) { this.balanced = balanced; }
    }
}






