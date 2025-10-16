package com.payments.saga.repository;

import com.payments.saga.domain.SagaStepStatus;
import com.payments.saga.domain.SagaStepType;
import com.payments.saga.entity.SagaStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for SagaStep entities
 */
@Repository
public interface SagaStepRepository extends JpaRepository<SagaStepEntity, String> {
    
    List<SagaStepEntity> findBySagaId(String sagaId);
    
    List<SagaStepEntity> findBySagaIdOrderBySequence(String sagaId);
    
    List<SagaStepEntity> findBySagaIdAndStatus(String sagaId, SagaStepStatus status);
    
    List<SagaStepEntity> findBySagaIdAndStepType(String sagaId, SagaStepType stepType);
    
    @Query("SELECT s FROM SagaStepEntity s WHERE s.sagaId = :sagaId AND s.sequence = :sequence")
    SagaStepEntity findBySagaIdAndSequence(@Param("sagaId") String sagaId, @Param("sequence") Integer sequence);
    
    @Query("SELECT s FROM SagaStepEntity s WHERE s.sagaId = :sagaId AND s.status IN :statuses ORDER BY s.sequence")
    List<SagaStepEntity> findBySagaIdAndStatusIn(@Param("sagaId") String sagaId, @Param("statuses") List<SagaStepStatus> statuses);
    
    @Query("SELECT COUNT(s) FROM SagaStepEntity s WHERE s.sagaId = :sagaId AND s.status = :status")
    long countBySagaIdAndStatus(@Param("sagaId") String sagaId, @Param("status") SagaStepStatus status);
    
    @Query("SELECT s FROM SagaStepEntity s WHERE s.sagaId = :sagaId AND s.status = :status ORDER BY s.sequence DESC")
    List<SagaStepEntity> findCompletedStepsBySagaIdOrderBySequenceDesc(@Param("sagaId") String sagaId, @Param("status") SagaStepStatus status);
    
    @Query("SELECT s FROM SagaStepEntity s WHERE s.tenantId = :tenantId AND s.businessUnitId = :businessUnitId AND s.status = :status")
    List<SagaStepEntity> findByTenantAndBusinessUnitAndStatus(
            @Param("tenantId") String tenantId,
            @Param("businessUnitId") String businessUnitId,
            @Param("status") SagaStepStatus status
    );
    
    void deleteBySagaId(String sagaId);
}
