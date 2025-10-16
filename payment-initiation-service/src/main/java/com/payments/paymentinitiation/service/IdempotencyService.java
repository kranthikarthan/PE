package com.payments.paymentinitiation.service;

import com.payments.domain.shared.PaymentId;
import com.payments.paymentinitiation.port.IdempotencyRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Idempotency Service
 * 
 * Manages idempotency keys to prevent duplicate payment processing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRepositoryPort idempotencyRepository;

    /**
     * Check if request is duplicate based on idempotency key
     * 
     * @param idempotencyKey Idempotency key
     * @param tenantId Tenant ID
     * @return true if duplicate, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isDuplicate(String idempotencyKey, String tenantId) {
        return idempotencyRepository.existsByIdempotencyKeyAndTenantId(idempotencyKey, tenantId);
    }

    /**
     * Record idempotency key for future duplicate detection
     * 
     * @param idempotencyKey Idempotency key
     * @param tenantId Tenant ID
     * @param paymentId Payment ID
     */
    @Transactional
    public void recordIdempotency(String idempotencyKey, String tenantId, PaymentId paymentId) {
        IdempotencyRepositoryPort.IdempotencyRecord record = new IdempotencyRepositoryPort.IdempotencyRecord(
                UUID.randomUUID().toString(),
                idempotencyKey,
                tenantId,
                paymentId.getValue(),
                Instant.now()
        );
        
        idempotencyRepository.save(record);
        log.debug("Recorded idempotency for key: {} and tenant: {}", idempotencyKey, tenantId);
    }

}
