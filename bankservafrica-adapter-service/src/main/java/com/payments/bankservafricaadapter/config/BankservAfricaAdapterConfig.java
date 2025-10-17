package com.payments.bankservafricaadapter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Configuration for BankservAfrica Adapter Service
 */
@Configuration
@EnableCaching
public class BankservAfricaAdapterConfig {
    
    /**
     * Cache manager for BankservAfrica adapter caching
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "bankservafrica-adapters",
                "bankservafrica-eft-messages",
                "bankservafrica-iso8583-messages",
                "bankservafrica-ach-transactions",
                "bankservafrica-transaction-logs",
                "bankservafrica-settlement-records"
        );
    }
}
