package com.payments.routing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Routing Service Application
 * 
 * Provides payment routing decisions:
 * - Routing rule management
 * - Payment routing decisions
 * - Clearing system selection
 * - Routing optimization
 */
@SpringBootApplication
@EnableCaching
public class RoutingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutingServiceApplication.class, args);
    }
}
