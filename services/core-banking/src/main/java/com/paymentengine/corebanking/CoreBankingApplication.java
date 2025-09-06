package com.paymentengine.corebanking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Core Banking Service Application
 * 
 * Provides transaction processing, account management, and payment processing
 * capabilities for the Payment Engine system.
 */
@SpringBootApplication(scanBasePackages = {
    "com.paymentengine.corebanking",
    "com.paymentengine.shared"
})
@EnableKafka
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class CoreBankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreBankingApplication.class, args);
    }
}