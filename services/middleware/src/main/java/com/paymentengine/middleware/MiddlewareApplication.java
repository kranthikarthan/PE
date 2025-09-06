package com.paymentengine.middleware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Middleware Service Application
 * 
 * Provides business orchestration and external integrations:
 * - Authentication and authorization services
 * - Business workflow orchestration
 * - External system integrations
 * - Notification services
 * - Dashboard and reporting APIs
 * - Webhook management
 */
@SpringBootApplication(scanBasePackages = {
    "com.paymentengine.middleware",
    "com.paymentengine.shared"
})
@EnableFeignClients
@EnableKafka
@EnableAsync
@EnableScheduling
public class MiddlewareApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiddlewareApplication.class, args);
    }
}