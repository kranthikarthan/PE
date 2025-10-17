package com.payments.bankservafricaadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * BankservAfrica Adapter Service Application
 *
 * <p>Main Spring Boot application for BankservAfrica clearing adapter: - EFT batch processing - ISO
 * 8583 message handling - ACH integration - Resilience patterns with circuit breakers -
 * Multi-tenant support
 */
@SpringBootApplication
@EnableFeignClients
@EnableCaching
public class BankservAfricaAdapterApplication {

  public static void main(String[] args) {
    SpringApplication.run(BankservAfricaAdapterApplication.class, args);
  }
}
