package com.payments.paymentinitiation.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * Simple configuration for testing purposes
 */
@Configuration
@ComponentScan(
    basePackages = "com.payments.paymentinitiation",
    includeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.payments\\.paymentinitiation\\.api\\.(HealthController|Pain001TestController)"
    ),
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.payments\\.paymentinitiation\\.api\\.(PaymentInitiationController|PaymentRepairController|Pain001Pain002Controller).*"
    )
)
public class SimpleTestConfiguration {
}
