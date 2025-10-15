package com.payments.paymentinitiation.config;

import com.payments.paymentinitiation.adapter.IdempotencyRepositoryAdapter;
import com.payments.paymentinitiation.adapter.PaymentRepositoryAdapter;
import com.payments.paymentinitiation.port.IdempotencyRepositoryPort;
import com.payments.paymentinitiation.port.PaymentRepositoryPort;
import com.payments.paymentinitiation.repository.IdempotencyRecordJpaRepository;
import com.payments.paymentinitiation.repository.PaymentJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Repository Configuration
 * 
 * Configures repository adapters and ports
 * following the Ports and Adapters pattern
 */
@Configuration
public class RepositoryConfig {

    /**
     * Payment Repository Port Bean
     * 
     * @param jpaRepository JPA repository
     * @param paymentMapper Payment mapper
     * @return Payment repository port implementation
     */
    @Bean
    public PaymentRepositoryPort paymentRepositoryPort(
            PaymentJpaRepository jpaRepository,
            com.payments.paymentinitiation.mapper.PaymentMapper paymentMapper) {
        return new PaymentRepositoryAdapter(jpaRepository, paymentMapper);
    }

    /**
     * Idempotency Repository Port Bean
     * 
     * @param jpaRepository JPA repository
     * @return Idempotency repository port implementation
     */
    @Bean
    public IdempotencyRepositoryPort idempotencyRepositoryPort(
            IdempotencyRecordJpaRepository jpaRepository) {
        return new IdempotencyRepositoryAdapter(jpaRepository);
    }
}
