package com.payments.bankservafricaadapter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for BankservAfrica Adapter Service */
@Configuration
@EnableCaching
@ConfigurationProperties(prefix = "bankservafrica.adapter")
@Data
public class BankservAfricaAdapterConfig {

  /** BankservAfrica endpoint configuration */
  private String endpoint;

  private String apiVersion;
  private Integer timeoutSeconds;
  private Integer retryAttempts;
  private Boolean encryptionEnabled;
  private Integer batchSize;
  private String processingWindowStart;
  private String processingWindowEnd;

  /** Cache manager for BankservAfrica adapter caching */
  @Bean
  public CacheManager cacheManager() {
    return new ConcurrentMapCacheManager(
        "bankservafrica-adapters",
        "bankservafrica-eft-messages",
        "bankservafrica-iso8583-messages",
        "bankservafrica-ach-transactions",
        "bankservafrica-transaction-logs",
        "bankservafrica-settlement-records");
  }
}
