package com.payments.paymentinitiation.repository;

import com.payments.paymentinitiation.port.IdempotencyRepositoryPort;
import org.springframework.stereotype.Repository;

/**
 * Idempotency Repository
 * 
 * Repository interface for idempotency record persistence
 * Uses the IdempotencyRepositoryPort for abstraction
 */
@Repository
public interface IdempotencyRepository extends IdempotencyRepositoryPort {
    // This interface extends IdempotencyRepositoryPort
    // The actual implementation is provided by IdempotencyRepositoryAdapter
}