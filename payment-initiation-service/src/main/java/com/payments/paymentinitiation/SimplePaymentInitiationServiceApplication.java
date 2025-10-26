package com.payments.paymentinitiation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Simple Payment Initiation Service Application
 * 
 * A minimal version for testing basic functionality without complex dependencies
 */
@SpringBootApplication
@RestController
public class SimplePaymentInitiationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimplePaymentInitiationServiceApplication.class, args);
    }

    @GetMapping("/api/v1/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "service", "Payment Initiation Service",
            "version", "0.1.0-SNAPSHOT",
            "timestamp", Instant.now()
        );
    }

    @GetMapping("/api/v1/pain001/test")
    public Map<String, Object> testPain001() {
        return Map.of(
            "message", "pain.001 test endpoint is working",
            "timestamp", Instant.now(),
            "service", "Payment Initiation Service",
            "version", "0.1.0-SNAPSHOT"
        );
    }
}