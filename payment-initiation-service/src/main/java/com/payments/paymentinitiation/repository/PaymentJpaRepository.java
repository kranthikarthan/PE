package com.payments.paymentinitiation.repository;

import com.payments.domain.shared.PaymentId;
import com.payments.paymentinitiation.entity.PaymentEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for Payment Entity
 *
 * <p>Provides data access methods for Payment aggregate with multi-tenancy support
 */
@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, PaymentId> {

  /**
   * Find payment by ID and tenant context
   *
   * @param paymentId Payment ID
   * @param tenantId Tenant ID
   * @return Optional payment entity
   */
  @Query(
      "SELECT p FROM PaymentEntity p WHERE p.paymentId = :paymentId AND p.tenantContext.tenantId = :tenantId")
  Optional<PaymentEntity> findByIdAndTenantId(
      @Param("paymentId") PaymentId paymentId, @Param("tenantId") String tenantId);

  /**
   * Find payments by tenant ID
   *
   * @param tenantId Tenant ID
   * @param pageable Pagination parameters
   * @return Page of payment entities
   */
  @Query(
      "SELECT p FROM PaymentEntity p WHERE p.tenantContext.tenantId = :tenantId ORDER BY p.initiatedAt DESC")
  Page<PaymentEntity> findByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

  /**
   * Find payments by tenant and business unit
   *
   * @param tenantId Tenant ID
   * @param businessUnitId Business unit ID
   * @param pageable Pagination parameters
   * @return Page of payment entities
   */
  @Query(
      "SELECT p FROM PaymentEntity p WHERE p.tenantContext.tenantId = :tenantId AND p.tenantContext.businessUnitId = :businessUnitId ORDER BY p.initiatedAt DESC")
  Page<PaymentEntity> findByTenantIdAndBusinessUnitId(
      @Param("tenantId") String tenantId,
      @Param("businessUnitId") String businessUnitId,
      Pageable pageable);

  /**
   * Find payments by status and tenant
   *
   * @param status Payment status
   * @param tenantId Tenant ID
   * @return List of payment entities
   */
  @Query(
      "SELECT p FROM PaymentEntity p WHERE p.status = :status AND p.tenantContext.tenantId = :tenantId ORDER BY p.initiatedAt ASC")
  List<PaymentEntity> findByStatusAndTenantId(
      @Param("status") com.payments.domain.payment.PaymentStatus status,
      @Param("tenantId") String tenantId);

  /**
   * Find payments by date range and tenant
   *
   * @param tenantId Tenant ID
   * @param startDate Start date
   * @param endDate End date
   * @param pageable Pagination parameters
   * @return Page of payment entities
   */
  @Query(
      "SELECT p FROM PaymentEntity p WHERE p.tenantContext.tenantId = :tenantId AND p.initiatedAt BETWEEN :startDate AND :endDate ORDER BY p.initiatedAt DESC")
  Page<PaymentEntity> findByTenantIdAndDateRange(
      @Param("tenantId") String tenantId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      Pageable pageable);

  /**
   * Count payments by status and tenant
   *
   * @param status Payment status
   * @param tenantId Tenant ID
   * @return Count of payments
   */
  @Query(
      "SELECT COUNT(p) FROM PaymentEntity p WHERE p.status = :status AND p.tenantContext.tenantId = :tenantId")
  long countByStatusAndTenantId(
      @Param("status") com.payments.domain.payment.PaymentStatus status,
      @Param("tenantId") String tenantId);

  /**
   * Find payments by source account and tenant
   *
   * @param sourceAccount Source account number
   * @param tenantId Tenant ID
   * @param pageable Pagination parameters
   * @return Page of payment entities
   */
  @Query(
      "SELECT p FROM PaymentEntity p WHERE p.sourceAccount.accountNumber = :sourceAccount AND p.tenantContext.tenantId = :tenantId ORDER BY p.initiatedAt DESC")
  Page<PaymentEntity> findBySourceAccountAndTenantId(
      @Param("sourceAccount") String sourceAccount,
      @Param("tenantId") String tenantId,
      Pageable pageable);

  /**
   * Find payments by destination account and tenant
   *
   * @param destinationAccount Destination account number
   * @param tenantId Tenant ID
   * @param pageable Pagination parameters
   * @return Page of payment entities
   */
  @Query(
      "SELECT p FROM PaymentEntity p WHERE p.destinationAccount.accountNumber = :destinationAccount AND p.tenantContext.tenantId = :tenantId ORDER BY p.initiatedAt DESC")
  Page<PaymentEntity> findByDestinationAccountAndTenantId(
      @Param("destinationAccount") String destinationAccount,
      @Param("tenantId") String tenantId,
      Pageable pageable);

  /**
   * Check if payment exists by ID and tenant
   *
   * @param paymentId Payment ID
   * @param tenantId Tenant ID
   * @return true if exists, false otherwise
   */
  @Query(
      "SELECT COUNT(p) > 0 FROM PaymentEntity p WHERE p.paymentId = :paymentId AND p.tenantContext.tenantId = :tenantId")
  boolean existsByIdAndTenantId(
      @Param("paymentId") PaymentId paymentId, @Param("tenantId") String tenantId);
}
