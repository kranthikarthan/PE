package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for Idempotency Key management
 */
@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    /**
     * Find by idempotency key
     */
    Optional<IdempotencyKey> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find by idempotency key and tenant
     */
    Optional<IdempotencyKey> findByIdempotencyKeyAndTenantId(String idempotencyKey, String tenantId);

    /**
     * Delete expired keys
     */
    @Modifying
    @Query("DELETE FROM IdempotencyKey i WHERE i.expiresAt < :now")
    int deleteExpiredKeys(LocalDateTime now);

    /**
     * Count active keys for tenant
     */
    @Query("SELECT COUNT(i) FROM IdempotencyKey i WHERE i.tenantId = :tenantId AND i.expiresAt > :now")
    long countActiveKeysByTenant(String tenantId, LocalDateTime now);
}
