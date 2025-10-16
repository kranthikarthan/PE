package com.payments.saga.service;

import com.payments.saga.domain.Saga;
import com.payments.saga.domain.SagaId;
import com.payments.saga.entity.SagaEntity;
import com.payments.saga.repository.SagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing saga persistence
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaService {

    private final SagaRepository sagaRepository;
    private final SagaStepService sagaStepService;

    @Transactional
    public Saga saveSaga(Saga saga) {
        log.debug("Saving saga {}", saga.getId().getValue());
        
        SagaEntity entity = SagaEntity.fromDomain(saga);
        SagaEntity savedEntity = sagaRepository.save(entity);
        
        // Save steps if they exist
        if (saga.getSteps() != null && !saga.getSteps().isEmpty()) {
            sagaStepService.saveSteps(saga.getSteps());
        }
        
        return savedEntity.toDomain();
    }

    @Transactional(readOnly = true)
    public Optional<Saga> getSaga(SagaId sagaId) {
        log.debug("Getting saga {}", sagaId.getValue());
        
        return sagaRepository.findById(sagaId.getValue())
                .map(entity -> {
                    Saga saga = entity.toDomain();
                    // Load steps
                    saga.setSteps(sagaStepService.getStepsBySagaId(sagaId));
                    return saga;
                });
    }

    @Transactional(readOnly = true)
    public List<Saga> getSagasByCorrelationId(String correlationId) {
        log.debug("Getting sagas by correlation ID {}", correlationId);
        
        return sagaRepository.findByCorrelationId(correlationId)
                .stream()
                .map(SagaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Saga> getSagasByPaymentId(String paymentId) {
        log.debug("Getting sagas by payment ID {}", paymentId);
        
        return sagaRepository.findByPaymentId(paymentId)
                .stream()
                .map(SagaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Saga> getSagasByTenantAndBusinessUnit(String tenantId, String businessUnitId) {
        log.debug("Getting sagas by tenant {} and business unit {}", tenantId, businessUnitId);
        
        return sagaRepository.findByTenantIdAndBusinessUnitId(tenantId, businessUnitId)
                .stream()
                .map(SagaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Saga> getActiveSagas() {
        log.debug("Getting active sagas");
        
        return sagaRepository.findByStatus(com.payments.saga.domain.SagaStatus.RUNNING)
                .stream()
                .map(SagaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSaga(SagaId sagaId) {
        log.debug("Deleting saga {}", sagaId.getValue());
        
        sagaRepository.deleteById(sagaId.getValue());
    }
}






