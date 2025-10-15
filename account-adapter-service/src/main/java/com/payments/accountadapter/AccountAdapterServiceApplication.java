package com.payments.accountadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Account Adapter Service Application
 * 
 * Provides account system integration:
 * - External account system integration
 * - Account balance validation
 * - Account status checking
 * - Account holder verification
 * - OAuth2 authentication
 * - Circuit breaker patterns
 */
@SpringBootApplication
@EnableFeignClients
public class AccountAdapterServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountAdapterServiceApplication.class, args);
    }
}
