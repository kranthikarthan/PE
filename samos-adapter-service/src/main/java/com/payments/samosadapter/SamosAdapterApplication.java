package com.payments.samosadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * SAMOS Adapter Service Application
 *
 * <p>High-value RTGS payment clearing adapter for South African Reserve Bank (SARB) integration.
 * Supports ISO 20022 messaging format and real-time settlement.
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
public class SamosAdapterApplication {

  public static void main(String[] args) {
    SpringApplication.run(SamosAdapterApplication.class, args);
  }
}
