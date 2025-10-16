package com.payments.paymentinitiation.port;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository Port for Idempotency Tracking
 *
 * <p>Defines the contract for idempotency record persistence operations following the Ports and
 * Adapters pattern
 */
public interface IdempotencyRepositoryPort {

  /**
   * Check if idempotency key exists for tenant
   *
   * @param idempotencyKey Idempotency key
   * @param tenantId Tenant ID
   * @return true if exists, false otherwise
   */
  boolean existsByIdempotencyKeyAndTenantId(String idempotencyKey, String tenantId);

  /**
   * Find idempotency record by key and tenant
   *
   * @param idempotencyKey Idempotency key
   * @param tenantId Tenant ID
   * @return Optional idempotency record
   */
  Optional<IdempotencyRecord> findByIdempotencyKeyAndTenantId(
      String idempotencyKey, String tenantId);

  /**
   * Save idempotency record
   *
   * @param record Idempotency record
   * @return Saved idempotency record
   */
  IdempotencyRecord save(IdempotencyRecord record);

  /**
   * Find idempotency records by tenant
   *
   * @param tenantId Tenant ID
   * @return List of idempotency records
   */
  List<IdempotencyRecord> findByTenantId(String tenantId);

  /**
   * Find idempotency records by payment ID
   *
   * @param paymentId Payment ID
   * @return List of idempotency records
   */
  List<IdempotencyRecord> findByPaymentId(String paymentId);

  /**
   * Find idempotency records created before specified time
   *
   * @param beforeTime Time threshold
   * @return List of idempotency records
   */
  List<IdempotencyRecord> findByCreatedAtBefore(Instant beforeTime);

  /**
   * Count idempotency records by tenant
   *
   * @param tenantId Tenant ID
   * @return Count of idempotency records
   */
  long countByTenantId(String tenantId);

  /**
   * Delete idempotency records created before specified time
   *
   * @param beforeTime Time threshold
   * @return Number of deleted records
   */
  int deleteByCreatedAtBefore(Instant beforeTime);

  /** Idempotency record domain object */
  record IdempotencyRecord(
      String id, String idempotencyKey, String tenantId, String paymentId, Instant createdAt) {}
}
