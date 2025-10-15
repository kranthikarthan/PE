package com.payments.paymentinitiation.config;

import com.payments.paymentinitiation.service.PaymentBusinessRulesService;
import com.payments.paymentinitiation.service.PaymentDomainService;
import com.payments.paymentinitiation.service.PaymentEventPublisher;
import com.payments.paymentinitiation.service.PaymentInitiationService;
import com.payments.paymentinitiation.service.IdempotencyService;
import com.payments.paymentinitiation.port.PaymentRepositoryPort;
import com.payments.paymentinitiation.port.IdempotencyRepositoryPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

/**
 * Test Configuration
 * 
 * Provides mock beans for testing
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public PaymentRepositoryPort mockPaymentRepository() {
        return mock(PaymentRepositoryPort.class);
    }

    @Bean
    @Primary
    public IdempotencyRepositoryPort mockIdempotencyRepository() {
        return mock(IdempotencyRepositoryPort.class);
    }

    @Bean
    @Primary
    public IdempotencyService mockIdempotencyService() {
        return mock(IdempotencyService.class);
    }

    @Bean
    @Primary
    public PaymentDomainService mockPaymentDomainService() {
        return mock(PaymentDomainService.class);
    }

    @Bean
    @Primary
    public PaymentEventPublisher mockPaymentEventPublisher() {
        return mock(PaymentEventPublisher.class);
    }

    @Bean
    @Primary
    public PaymentBusinessRulesService mockPaymentBusinessRulesService() {
        return mock(PaymentBusinessRulesService.class);
    }
}
