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
 * <p>Configures repository adapters and ports following the Ports and Adapters pattern
 */
@Configuration
public class RepositoryConfig {

  // Adapters are annotated with @Component and picked up via component scanning.
  // Explicit @Bean definitions removed to avoid duplicate beans of the same port type.
}
