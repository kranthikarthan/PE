package com.payments.transactionprocessing.service;

import com.payments.domain.transaction.*;
import com.payments.domain.shared.*;
import com.payments.transactionprocessing.entity.LedgerEntryEntity;
import com.payments.transactionprocessing.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountBalanceService accountBalanceService;

    @Transactional
    public void createLedgerEntries(Transaction transaction) {
        log.info("Creating ledger entries for transaction {}", transaction.getId().getValue());

        for (LedgerEntry ledgerEntry : transaction.getLedgerEntries()) {
            LedgerEntryEntity entity = LedgerEntryEntity.fromDomain(ledgerEntry);
            
            // Calculate balances
            BigDecimal balanceBefore = accountBalanceService.getCurrentBalance(
                ledgerEntry.getAccountNumber(), transaction.getTenantContext());
            BigDecimal balanceAfter = calculateNewBalance(balanceBefore, ledgerEntry);
            
            entity.setBalanceBefore(balanceBefore);
            entity.setBalanceAfter(balanceAfter);
            
            ledgerEntryRepository.save(entity);
            
            // Update account balance
            accountBalanceService.updateBalance(
                ledgerEntry.getAccountNumber(), 
                transaction.getTenantContext(), 
                balanceAfter
            );
        }

        log.info("Ledger entries created for transaction {}", transaction.getId().getValue());
    }

    @Cacheable(value = "ledger-entries", key = "#accountNumber.value + '_' + #tenantContext.tenantId")
    public List<LedgerEntry> getAccountEntries(AccountNumber accountNumber, TenantContext tenantContext) {
        log.debug("Getting ledger entries for account {} and tenant {}", 
                accountNumber.getValue(), tenantContext.getTenantId());
        
        return ledgerEntryRepository.findByTenantContextAndAccountNumber(tenantContext, accountNumber)
                .stream()
                .map(LedgerEntryEntity::toDomain)
                .collect(Collectors.toList());
    }

    public List<LedgerEntry> getAccountEntriesByType(AccountNumber accountNumber, 
                                                   TenantContext tenantContext, 
                                                   LedgerEntryType entryType) {
        log.debug("Getting {} entries for account {} and tenant {}", 
                entryType, accountNumber.getValue(), tenantContext.getTenantId());
        
        return ledgerEntryRepository.findByTenantContextAndAccountNumberAndEntryType(
                tenantContext, accountNumber, entryType)
                .stream()
                .map(LedgerEntryEntity::toDomain)
                .collect(Collectors.toList());
    }

    public List<LedgerEntry> getAccountEntriesByDateRange(AccountNumber accountNumber, 
                                                         TenantContext tenantContext,
                                                         LocalDate startDate, 
                                                         LocalDate endDate) {
        log.debug("Getting ledger entries for account {} between {} and {}", 
                accountNumber.getValue(), startDate, endDate);
        
        return ledgerEntryRepository.findByTenantContextAndAccountNumberAndDateRange(
                tenantContext, accountNumber, startDate, endDate)
                .stream()
                .map(LedgerEntryEntity::toDomain)
                .collect(Collectors.toList());
    }

    public BigDecimal getAccountBalance(AccountNumber accountNumber, TenantContext tenantContext) {
        log.debug("Getting current balance for account {} and tenant {}", 
                accountNumber.getValue(), tenantContext.getTenantId());
        
        return accountBalanceService.getCurrentBalance(accountNumber, tenantContext);
    }

    public BigDecimal getAccountBalanceByType(AccountNumber accountNumber, 
                                            TenantContext tenantContext, 
                                            LedgerEntryType entryType) {
        log.debug("Getting {} balance for account {} and tenant {}", 
                entryType, accountNumber.getValue(), tenantContext.getTenantId());
        
        BigDecimal balance = ledgerEntryRepository.sumAmountByTenantContextAndAccountNumberAndEntryType(
                tenantContext, accountNumber, entryType);
        
        return balance != null ? balance : BigDecimal.ZERO;
    }

    public List<LedgerEntry> getTransactionEntries(TransactionId transactionId) {
        log.debug("Getting ledger entries for transaction {}", transactionId.getValue());
        
        return ledgerEntryRepository.findByTransactionId(transactionId)
                .stream()
                .map(LedgerEntryEntity::toDomain)
                .collect(Collectors.toList());
    }

    public List<LedgerEntry> getLatestAccountEntries(AccountNumber accountNumber, TenantContext tenantContext) {
        log.debug("Getting latest entries for account {} and tenant {}", 
                accountNumber.getValue(), tenantContext.getTenantId());
        
        return ledgerEntryRepository.findLatestByTenantContextAndAccountNumber(tenantContext, accountNumber)
                .stream()
                .map(LedgerEntryEntity::toDomain)
                .collect(Collectors.toList());
    }

    public List<AccountNumber> getAllAccountsForTenant(TenantContext tenantContext) {
        log.debug("Getting all accounts for tenant {}", tenantContext.getTenantId());
        
        return ledgerEntryRepository.findDistinctAccountNumbersByTenantContext(tenantContext)
                .stream()
                .map(AccountNumber::of)
                .collect(Collectors.toList());
    }

    public List<LedgerEntry> getAllEntriesForTenant(TenantContext tenantContext) {
        log.debug("Getting all ledger entries for tenant {}", tenantContext.getTenantId());
        
        return ledgerEntryRepository.findByTenantContext(tenantContext)
                .stream()
                .map(LedgerEntryEntity::toDomain)
                .collect(Collectors.toList());
    }

    public BigDecimal getTotalDebitsForAccount(AccountNumber accountNumber, TenantContext tenantContext) {
        log.debug("Getting total debits for account {} and tenant {}", 
                accountNumber.getValue(), tenantContext.getTenantId());
        
        BigDecimal total = ledgerEntryRepository.sumAmountByTenantContextAndAccountNumberAndEntryType(
                tenantContext, accountNumber, LedgerEntryType.DEBIT);
        
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalCreditsForAccount(AccountNumber accountNumber, TenantContext tenantContext) {
        log.debug("Getting total credits for account {} and tenant {}", 
                accountNumber.getValue(), tenantContext.getTenantId());
        
        BigDecimal total = ledgerEntryRepository.sumAmountByTenantContextAndAccountNumberAndEntryType(
                tenantContext, accountNumber, LedgerEntryType.CREDIT);
        
        return total != null ? total : BigDecimal.ZERO;
    }

    public boolean hasAccountActivity(AccountNumber accountNumber, TenantContext tenantContext) {
        log.debug("Checking if account {} has activity for tenant {}", 
                accountNumber.getValue(), tenantContext.getTenantId());
        
        return ledgerEntryRepository.countByTenantContextAndAccountNumber(tenantContext, accountNumber) > 0;
    }

    private BigDecimal calculateNewBalance(BigDecimal currentBalance, LedgerEntry ledgerEntry) {
        if (ledgerEntry.getEntryType() == LedgerEntryType.DEBIT) {
            return currentBalance.subtract(ledgerEntry.getAmount());
        } else {
            return currentBalance.add(ledgerEntry.getAmount());
        }
    }
}
