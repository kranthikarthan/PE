package com.payments.paymentinitiation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Payment Initiation Service Application
 *
 * <p>Provides REST API for payment initiation with: - OpenAPI documentation - JPA persistence -
 * Validation - Health checks
 */
@SpringBootApplication
@EnableJpaRepositories
@EnableAsync
public class PaymentInitiationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaymentInitiationServiceApplication.class, args);
  }
}
