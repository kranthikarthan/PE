package com.payments.validation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Validation Service Application
 * 
 * Provides payment validation with business rules engine:
 * - Event-driven validation processing
 * - Business rules engine integration
 * - Multi-tenant validation rules
 * - Fraud detection and risk assessment
 */
@SpringBootApplication
@EnableKafka
public class ValidationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValidationServiceApplication.class, args);
    }
}
