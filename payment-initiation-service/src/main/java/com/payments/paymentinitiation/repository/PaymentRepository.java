package com.payments.paymentinitiation.repository;

import com.payments.domain.payment.Payment;
import com.payments.domain.payment.PaymentId;
import com.payments.paymentinitiation.port.PaymentRepositoryPort;
import org.springframework.stereotype.Repository;

/**
 * Payment Repository
 * 
 * Repository interface for Payment aggregate persistence
 * Uses the PaymentRepositoryPort for abstraction
 */
@Repository
public interface PaymentRepository extends PaymentRepositoryPort {
    // This interface extends PaymentRepositoryPort
    // The actual implementation is provided by PaymentRepositoryAdapter
}