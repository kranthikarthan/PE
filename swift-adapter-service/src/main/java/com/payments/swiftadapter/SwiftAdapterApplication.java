package com.payments.swiftadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * SWIFT Adapter Service Application
 *
 * <p>Handles international payments with SWIFT network integration. Features: - International
 * payments via SWIFT network - MT103/pacs.008 messaging support - Sanctions screening and
 * compliance - Foreign exchange (FX) conversion - Cross-border payment processing
 */
@SpringBootApplication
@EnableFeignClients
public class SwiftAdapterApplication {

  public static void main(String[] args) {
    SpringApplication.run(SwiftAdapterApplication.class, args);
  }
}
