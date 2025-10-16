package com.payments.transactionprocessing.service;

import com.payments.domain.shared.AccountNumber;
import com.payments.domain.shared.TenantContext;
import com.payments.transactionprocessing.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountBalanceService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final LedgerEntryRepository ledgerEntryRepository;

    private static final String BALANCE_KEY_PREFIX = "balance:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    @Cacheable(value = "account-balances", key = "#accountNumber.value + '_' + #tenantContext.tenantId")
    public BigDecimal getCurrentBalance(AccountNumber accountNumber, TenantContext tenantContext) {
        log.debug("Getting current balance for account {} and tenant {}", 
                accountNumber.getValue(), tenantContext.getTenantId());

        String cacheKey = buildBalanceKey(accountNumber, tenantContext);
        
        // Try cache first
        Object cachedBalance = redisTemplate.opsForValue().get(cacheKey);
        if (cachedBalance != null) {
            log.debug("Balance found in cache for account {}", accountNumber.getValue());
            return (BigDecimal) cachedBalance;
        }

        // Calculate from ledger entries
        BigDecimal balance = calculateBalanceFromLedger(accountNumber, tenantContext);
        
        // Cache the result
        redisTemplate.opsForValue().set(cacheKey, balance, CACHE_TTL);
        
        log.debug("Calculated balance {} for account {}", balance, accountNumber.getValue());
        return balance;
    }

    @CacheEvict(value = "account-balances", key = "#accountNumber.value + '_' + #tenantContext.tenantId")
    public void updateBalance(AccountNumber accountNumber, TenantContext tenantContext, BigDecimal newBalance) {
        log.debug("Updating balance to {} for account {} and tenant {}", 
                newBalance, accountNumber.getValue(), tenantContext.getTenantId());

        String cacheKey = buildBalanceKey(accountNumber, tenantContext);
        redisTemplate.opsForValue().set(cacheKey, newBalance, CACHE_TTL);
        
        log.info("Balance updated to {} for account {}", newBalance, accountNumber.getValue());
    }

    @CacheEvict(value = "account-balances", key = "#accountNumber.value + '_' + #tenantContext.tenantId")
    public void invalidateBalance(AccountNumber accountNumber, TenantContext tenantContext) {
        log.debug("Invalidating balance cache for account {} and tenant {}", 
                accountNumber.getValue(), tenantContext.getTenantId());

        String cacheKey = buildBalanceKey(accountNumber, tenantContext);
        redisTemplate.delete(cacheKey);
    }

    public void invalidateAllBalancesForTenant(TenantContext tenantContext) {
        log.debug("Invalidating all balance caches for tenant {}", tenantContext.getTenantId());
        
        // This would require a more sophisticated cache invalidation strategy
        // For now, we'll rely on TTL expiration
        log.info("Balance cache invalidation requested for tenant {}", tenantContext.getTenantId());
    }

    private BigDecimal calculateBalanceFromLedger(AccountNumber accountNumber, TenantContext tenantContext) {
        // Get all ledger entries for the account
        var entries = ledgerEntryRepository.findByTenantContextAndAccountNumber(tenantContext, accountNumber);
        
        BigDecimal balance = BigDecimal.ZERO;
        
        for (var entry : entries) {
            if (entry.getEntryType() == com.payments.domain.transaction.LedgerEntryType.DEBIT) {
                balance = balance.subtract(entry.getAmount());
            } else {
                balance = balance.add(entry.getAmount());
            }
        }
        
        return balance;
    }

    private String buildBalanceKey(AccountNumber accountNumber, TenantContext tenantContext) {
        return BALANCE_KEY_PREFIX + tenantContext.getTenantId() + ":" + accountNumber.getValue();
    }
}
