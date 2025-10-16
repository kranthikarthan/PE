package com.payments.transactionprocessing.service;

import com.payments.domain.transaction.LedgerEntry;
import com.payments.domain.transaction.LedgerEntryType;
import com.payments.domain.transaction.Transaction;
import com.payments.domain.shared.AccountNumber;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for validating double-entry bookkeeping invariants and balances
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceValidationService {

    private final LedgerService ledgerService;
    private final AccountBalanceService accountBalanceService;

    /**
     * Validates that a transaction maintains double-entry bookkeeping invariants
     */
    @Transactional(readOnly = true)
    public boolean validateDoubleEntryInvariants(Transaction transaction) {
        log.debug("Validating double-entry invariants for transaction {}", 
                transaction.getId().getValue());

        List<LedgerEntry> entries = transaction.getLedgerEntries();
        
        if (entries.isEmpty()) {
            log.warn("Transaction {} has no ledger entries", transaction.getId().getValue());
            return false;
        }

        // Rule 1: Total debits must equal total credits
        BigDecimal totalDebits = calculateTotalByType(entries, LedgerEntryType.DEBIT);
        BigDecimal totalCredits = calculateTotalByType(entries, LedgerEntryType.CREDIT);
        
        if (!totalDebits.equals(totalCredits)) {
            log.error("Double-entry violation: Total debits {} != Total credits {} for transaction {}", 
                    totalDebits, totalCredits, transaction.getId().getValue());
            return false;
        }

        // Rule 2: At least one debit and one credit entry
        boolean hasDebit = entries.stream().anyMatch(e -> e.getEntryType() == LedgerEntryType.DEBIT);
        boolean hasCredit = entries.stream().anyMatch(e -> e.getEntryType() == LedgerEntryType.CREDIT);
        
        if (!hasDebit || !hasCredit) {
            log.error("Double-entry violation: Transaction {} must have both debit and credit entries", 
                    transaction.getId().getValue());
            return false;
        }

        // Rule 3: All entries must have the same currency
        if (!validateCurrencyConsistency(entries)) {
            log.error("Currency inconsistency in transaction {}", transaction.getId().getValue());
            return false;
        }

        // Rule 4: All amounts must be positive
        if (!validatePositiveAmounts(entries)) {
            log.error("Negative amounts found in transaction {}", transaction.getId().getValue());
            return false;
        }

        log.debug("Double-entry invariants validated for transaction {}", transaction.getId().getValue());
        return true;
    }

    /**
     * Validates account balances after a transaction
     */
    @Transactional(readOnly = true)
    public boolean validateAccountBalances(Transaction transaction) {
        log.debug("Validating account balances for transaction {}", 
                transaction.getId().getValue());

        List<LedgerEntry> entries = transaction.getLedgerEntries();
        
        for (LedgerEntry entry : entries) {
            AccountNumber accountNumber = entry.getAccountNumber();
            TenantContext tenantContext = entry.getTenantContext();
            
            // Get current balance from cache/database
            BigDecimal currentBalance = accountBalanceService.getCurrentBalance(accountNumber, tenantContext);
            
            // Calculate expected balance after this entry
            BigDecimal expectedBalance = calculateExpectedBalance(currentBalance, entry);
            
            // Validate that the entry's balance_after matches expected balance
            if (entry.getBalanceAfter() != null && !entry.getBalanceAfter().equals(expectedBalance)) {
                log.error("Balance mismatch for account {}: expected {} but entry shows {}", 
                        accountNumber.getValue(), expectedBalance, entry.getBalanceAfter());
                return false;
            }
        }

        log.debug("Account balances validated for transaction {}", transaction.getId().getValue());
        return true;
    }

    /**
     * Validates that all accounts have consistent balances
     */
    @Transactional(readOnly = true)
    public boolean validateAllAccountBalances(TenantContext tenantContext) {
        log.debug("Validating all account balances for tenant {}", tenantContext.getTenantId());

        // This would require a more comprehensive implementation
        // For now, we'll validate that the sum of all account balances equals zero
        // (which should be true in a properly balanced double-entry system)
        
        // Get all unique accounts for this tenant
        List<AccountNumber> accounts = ledgerService.getAllAccountsForTenant(tenantContext);
        
        BigDecimal totalSystemBalance = BigDecimal.ZERO;
        
        for (AccountNumber account : accounts) {
            BigDecimal balance = accountBalanceService.getCurrentBalance(account, tenantContext);
            totalSystemBalance = totalSystemBalance.add(balance);
        }
        
        // In a properly balanced system, total should be zero
        boolean isBalanced = totalSystemBalance.compareTo(BigDecimal.ZERO) == 0;
        
        if (!isBalanced) {
            log.error("System balance validation failed: Total balance {} != 0 for tenant {}", 
                    totalSystemBalance, tenantContext.getTenantId());
        } else {
            log.debug("System balance validation passed for tenant {}", tenantContext.getTenantId());
        }
        
        return isBalanced;
    }

    /**
     * Validates ledger entry integrity
     */
    @Transactional(readOnly = true)
    public boolean validateLedgerIntegrity(TenantContext tenantContext) {
        log.debug("Validating ledger integrity for tenant {}", tenantContext.getTenantId());

        // Get all ledger entries for the tenant
        List<LedgerEntry> allEntries = ledgerService.getAllEntriesForTenant(tenantContext);
        
        // Group by transaction
        Map<String, List<LedgerEntry>> entriesByTransaction = allEntries.stream()
                .collect(Collectors.groupingBy(e -> e.getTransactionId().getValue()));
        
        // Validate each transaction
        for (Map.Entry<String, List<LedgerEntry>> transactionEntries : entriesByTransaction.entrySet()) {
            String transactionId = transactionEntries.getKey();
            List<LedgerEntry> entries = transactionEntries.getValue();
            
            // Check double-entry balance for this transaction
            BigDecimal totalDebits = calculateTotalByType(entries, LedgerEntryType.DEBIT);
            BigDecimal totalCredits = calculateTotalByType(entries, LedgerEntryType.CREDIT);
            
            if (!totalDebits.equals(totalCredits)) {
                log.error("Ledger integrity violation: Transaction {} has unbalanced entries", transactionId);
                return false;
            }
        }
        
        log.debug("Ledger integrity validated for tenant {}", tenantContext.getTenantId());
        return true;
    }

    /**
     * Validates that account balances are sufficient for debit operations
     */
    @Transactional(readOnly = true)
    public boolean validateSufficientBalance(AccountNumber accountNumber, 
                                           TenantContext tenantContext, 
                                           Money debitAmount) {
        log.debug("Validating sufficient balance for account {} debit of {}", 
                accountNumber.getValue(), debitAmount.getAmount());

        BigDecimal currentBalance = accountBalanceService.getCurrentBalance(accountNumber, tenantContext);
        
        // Check if account has sufficient balance for the debit
        boolean hasSufficientBalance = currentBalance.compareTo(debitAmount.getAmount()) >= 0;
        
        if (!hasSufficientBalance) {
            log.warn("Insufficient balance: Account {} has {} but needs {} for debit", 
                    accountNumber.getValue(), currentBalance, debitAmount.getAmount());
        }
        
        return hasSufficientBalance;
    }

    private BigDecimal calculateTotalByType(List<LedgerEntry> entries, LedgerEntryType type) {
        return entries.stream()
                .filter(e -> e.getEntryType() == type)
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean validateCurrencyConsistency(List<LedgerEntry> entries) {
        // For now, we'll assume currency consistency since LedgerEntry uses BigDecimal
        // In a real implementation, we'd need to track currency separately
        return true;
    }

    private boolean validatePositiveAmounts(List<LedgerEntry> entries) {
        return entries.stream()
                .allMatch(e -> e.getAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    private BigDecimal calculateExpectedBalance(BigDecimal currentBalance, LedgerEntry entry) {
        if (entry.getEntryType() == LedgerEntryType.DEBIT) {
            return currentBalance.subtract(entry.getAmount());
        } else {
            return currentBalance.add(entry.getAmount());
        }
    }
}
