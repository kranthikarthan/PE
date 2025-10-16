package com.payments.transactionprocessing.repository;

import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import com.payments.domain.transaction.TransactionId;
import com.payments.domain.transaction.TransactionStatus;
import com.payments.transactionprocessing.entity.TransactionEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, TransactionId> {

  Optional<TransactionEntity> findByIdAndTenantContext(
      TransactionId id, TenantContext tenantContext);

  List<TransactionEntity> findByTenantContext(TenantContext tenantContext);

  List<TransactionEntity> findByPaymentIdAndTenantContext(
      PaymentId paymentId, TenantContext tenantContext);

  List<TransactionEntity> findByStatusAndTenantContext(
      TransactionStatus status, TenantContext tenantContext);

  @Query(
      "SELECT t FROM TransactionEntity t WHERE t.tenantContext = :tenantContext "
          + "AND t.createdAt BETWEEN :startDate AND :endDate "
          + "ORDER BY t.createdAt DESC")
  List<TransactionEntity> findByTenantContextAndDateRange(
      @Param("tenantContext") TenantContext tenantContext,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      "SELECT COUNT(t) FROM TransactionEntity t WHERE t.tenantContext = :tenantContext "
          + "AND t.status = :status")
  long countByTenantContextAndStatus(
      @Param("tenantContext") TenantContext tenantContext,
      @Param("status") TransactionStatus status);

  @Query(
      "SELECT t FROM TransactionEntity t WHERE t.tenantContext = :tenantContext "
          + "AND t.status IN :statuses "
          + "ORDER BY t.createdAt ASC")
  List<TransactionEntity> findByTenantContextAndStatusIn(
      @Param("tenantContext") TenantContext tenantContext,
      @Param("statuses") List<TransactionStatus> statuses);

  @Query(
      "SELECT t FROM TransactionEntity t WHERE t.clearingSystem = :clearingSystem "
          + "AND t.clearingReference = :clearingReference")
  Optional<TransactionEntity> findByClearingSystemAndReference(
      @Param("clearingSystem") String clearingSystem,
      @Param("clearingReference") String clearingReference);
}
