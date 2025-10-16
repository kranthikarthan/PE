package com.payments.paymentinitiation.repository;

import com.payments.paymentinitiation.entity.IdempotencyRecordEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for Idempotency Record Entity
 *
 * <p>Provides data access methods for idempotency tracking
 */
@Repository
public interface IdempotencyRecordJpaRepository
    extends JpaRepository<IdempotencyRecordEntity, String> {

  /**
   * Check if idempotency key exists for tenant
   *
   * @param idempotencyKey Idempotency key
   * @param tenantId Tenant ID
   * @return true if exists, false otherwise
   */
  @Query(
      "SELECT COUNT(i) > 0 FROM IdempotencyRecordEntity i WHERE i.idempotencyKey = :idempotencyKey AND i.tenantId = :tenantId")
  boolean existsByIdempotencyKeyAndTenantId(
      @Param("idempotencyKey") String idempotencyKey, @Param("tenantId") String tenantId);

  /**
   * Find idempotency record by key and tenant
   *
   * @param idempotencyKey Idempotency key
   * @param tenantId Tenant ID
   * @return Optional idempotency record
   */
  @Query(
      "SELECT i FROM IdempotencyRecordEntity i WHERE i.idempotencyKey = :idempotencyKey AND i.tenantId = :tenantId")
  Optional<IdempotencyRecordEntity> findByIdempotencyKeyAndTenantId(
      @Param("idempotencyKey") String idempotencyKey, @Param("tenantId") String tenantId);

  /**
   * Find idempotency records by tenant
   *
   * @param tenantId Tenant ID
   * @return List of idempotency records
   */
  @Query(
      "SELECT i FROM IdempotencyRecordEntity i WHERE i.tenantId = :tenantId ORDER BY i.createdAt DESC")
  List<IdempotencyRecordEntity> findByTenantId(@Param("tenantId") String tenantId);

  /**
   * Find idempotency records by payment ID
   *
   * @param paymentId Payment ID
   * @return List of idempotency records
   */
  @Query("SELECT i FROM IdempotencyRecordEntity i WHERE i.paymentId = :paymentId")
  List<IdempotencyRecordEntity> findByPaymentId(@Param("paymentId") String paymentId);

  /**
   * Find idempotency records created before specified time
   *
   * @param beforeTime Time threshold
   * @return List of idempotency records
   */
  @Query("SELECT i FROM IdempotencyRecordEntity i WHERE i.createdAt < :beforeTime")
  List<IdempotencyRecordEntity> findByCreatedAtBefore(@Param("beforeTime") Instant beforeTime);

  /**
   * Count idempotency records by tenant
   *
   * @param tenantId Tenant ID
   * @return Count of idempotency records
   */
  @Query("SELECT COUNT(i) FROM IdempotencyRecordEntity i WHERE i.tenantId = :tenantId")
  long countByTenantId(@Param("tenantId") String tenantId);

  /**
   * Delete idempotency records created before specified time
   *
   * @param beforeTime Time threshold
   * @return Number of deleted records
   */
  @Query("DELETE FROM IdempotencyRecordEntity i WHERE i.createdAt < :beforeTime")
  int deleteByCreatedAtBefore(@Param("beforeTime") Instant beforeTime);
}
