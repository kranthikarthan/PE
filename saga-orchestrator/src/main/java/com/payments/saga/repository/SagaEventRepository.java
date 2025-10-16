package com.payments.saga.repository;

import com.payments.saga.entity.SagaEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for SagaEvent entities
 */
@Repository
public interface SagaEventRepository extends JpaRepository<SagaEventEntity, String> {
    
    List<SagaEventEntity> findBySagaId(String sagaId);
    
    List<SagaEventEntity> findBySagaIdOrderByOccurredAt(String sagaId);
    
    List<SagaEventEntity> findBySagaIdAndEventType(String sagaId, String eventType);
    
    List<SagaEventEntity> findByCorrelationId(String correlationId);
    
    List<SagaEventEntity> findByTenantIdAndBusinessUnitId(String tenantId, String businessUnitId);
    
    @Query("SELECT e FROM SagaEventEntity e WHERE e.sagaId = :sagaId AND e.occurredAt >= :fromDate AND e.occurredAt <= :toDate ORDER BY e.occurredAt")
    List<SagaEventEntity> findBySagaIdAndOccurredAtBetween(
            @Param("sagaId") String sagaId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate
    );
    
    @Query("SELECT e FROM SagaEventEntity e WHERE e.tenantId = :tenantId AND e.businessUnitId = :businessUnitId AND e.eventType = :eventType ORDER BY e.occurredAt DESC")
    List<SagaEventEntity> findByTenantAndBusinessUnitAndEventTypeOrderByOccurredAtDesc(
            @Param("tenantId") String tenantId,
            @Param("businessUnitId") String businessUnitId,
            @Param("eventType") String eventType
    );
    
    @Query("SELECT COUNT(e) FROM SagaEventEntity e WHERE e.sagaId = :sagaId AND e.eventType = :eventType")
    long countBySagaIdAndEventType(@Param("sagaId") String sagaId, @Param("eventType") String eventType);
}






