package com.payments.transactionprocessing.repository;

import com.payments.domain.shared.TenantContext;
import com.payments.domain.transaction.TransactionEventId;
import com.payments.domain.transaction.TransactionId;
import com.payments.transactionprocessing.entity.TransactionEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionEventRepository extends JpaRepository<TransactionEventEntity, TransactionEventId> {

    List<TransactionEventEntity> findByTransactionId(TransactionId transactionId);

    List<TransactionEventEntity> findByTenantContext(TenantContext tenantContext);

    List<TransactionEventEntity> findByTenantContextAndEventType(
        TenantContext tenantContext, 
        String eventType
    );

    @Query("SELECT te FROM TransactionEventEntity te WHERE te.tenantContext = :tenantContext " +
           "AND te.occurredAt BETWEEN :startTime AND :endTime " +
           "ORDER BY te.occurredAt DESC")
    List<TransactionEventEntity> findByTenantContextAndTimeRange(
        @Param("tenantContext") TenantContext tenantContext,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    @Query("SELECT te FROM TransactionEventEntity te WHERE te.correlationId = :correlationId " +
           "ORDER BY te.occurredAt ASC")
    List<TransactionEventEntity> findByCorrelationId(@Param("correlationId") String correlationId);

    @Query("SELECT te FROM TransactionEventEntity te WHERE te.tenantContext = :tenantContext " +
           "AND te.eventType = :eventType " +
           "AND te.occurredAt BETWEEN :startTime AND :endTime " +
           "ORDER BY te.occurredAt DESC")
    List<TransactionEventEntity> findByTenantContextAndEventTypeAndTimeRange(
        @Param("tenantContext") TenantContext tenantContext,
        @Param("eventType") String eventType,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    @Query("SELECT MAX(te.eventSequence) FROM TransactionEventEntity te WHERE te.transaction.id = :transactionId")
    Long findMaxEventSequenceByTransactionId(@Param("transactionId") TransactionId transactionId);
}






