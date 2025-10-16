package com.payments.saga.service;

import com.payments.saga.domain.SagaId;
import com.payments.saga.domain.SagaStep;
import com.payments.saga.domain.SagaStepId;
import com.payments.saga.entity.SagaStepEntity;
import com.payments.saga.repository.SagaStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing saga step persistence
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaStepService {

    private final SagaStepRepository sagaStepRepository;

    @Transactional
    public SagaStep saveStep(SagaStep step) {
        log.debug("Saving saga step {}", step.getId().getValue());
        
        SagaStepEntity entity = SagaStepEntity.fromDomain(step);
        SagaStepEntity savedEntity = sagaStepRepository.save(entity);
        
        return savedEntity.toDomain();
    }

    @Transactional
    public List<SagaStep> saveSteps(List<SagaStep> steps) {
        log.debug("Saving {} saga steps", steps.size());
        
        List<SagaStepEntity> entities = steps.stream()
                .map(SagaStepEntity::fromDomain)
                .collect(Collectors.toList());
        
        List<SagaStepEntity> savedEntities = sagaStepRepository.saveAll(entities);
        
        return savedEntities.stream()
                .map(SagaStepEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<SagaStep> getStep(SagaStepId stepId) {
        log.debug("Getting saga step {}", stepId.getValue());
        
        return sagaStepRepository.findById(stepId.getValue())
                .map(SagaStepEntity::toDomain);
    }

    @Transactional(readOnly = true)
    public List<SagaStep> getStepsBySagaId(SagaId sagaId) {
        log.debug("Getting steps for saga {}", sagaId.getValue());
        
        return sagaStepRepository.findBySagaIdOrderBySequence(sagaId.getValue())
                .stream()
                .map(SagaStepEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SagaStep> getStepsBySagaIdAndStatus(SagaId sagaId, com.payments.saga.domain.SagaStepStatus status) {
        log.debug("Getting steps for saga {} with status {}", sagaId.getValue(), status);
        
        return sagaStepRepository.findBySagaIdAndStatus(sagaId.getValue(), status)
                .stream()
                .map(SagaStepEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SagaStep> getCompletedStepsBySagaId(SagaId sagaId) {
        log.debug("Getting completed steps for saga {}", sagaId.getValue());
        
        return sagaStepRepository.findCompletedStepsBySagaIdOrderBySequenceDesc(
                sagaId.getValue(), com.payments.saga.domain.SagaStepStatus.COMPLETED)
                .stream()
                .map(SagaStepEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteStepsBySagaId(SagaId sagaId) {
        log.debug("Deleting steps for saga {}", sagaId.getValue());
        
        sagaStepRepository.deleteBySagaId(sagaId.getValue());
    }
}






