package com.payments.payshapadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * PayShap Adapter Service Application
 *
 * <p>Handles instant P2P payments with proxy registry integration. Features: - Instant P2P payments
 * up to R3,000 - Proxy registry integration for mobile/email lookup - ISO 20022 messaging support -
 * Real-time processing and settlement
 */
@SpringBootApplication
@EnableFeignClients
public class PayShapAdapterApplication {

  public static void main(String[] args) {
    SpringApplication.run(PayShapAdapterApplication.class, args);
  }
}
