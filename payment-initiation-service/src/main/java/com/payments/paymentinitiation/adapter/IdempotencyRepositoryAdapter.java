package com.payments.paymentinitiation.adapter;

import com.payments.paymentinitiation.entity.IdempotencyRecordEntity;
import com.payments.paymentinitiation.port.IdempotencyRepositoryPort;
import com.payments.paymentinitiation.repository.IdempotencyRecordJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Idempotency Repository Adapter
 * 
 * Implements the IdempotencyRepositoryPort using JPA repository
 * following the Ports and Adapters pattern
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyRepositoryAdapter implements IdempotencyRepositoryPort {

    private final IdempotencyRecordJpaRepository jpaRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdempotencyKeyAndTenantId(String idempotencyKey, String tenantId) {
        log.debug("Checking idempotency key existence: {}, {}", idempotencyKey, tenantId);
        
        return jpaRepository.existsByIdempotencyKeyAndTenantId(idempotencyKey, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IdempotencyRecord> findByIdempotencyKeyAndTenantId(String idempotencyKey, String tenantId) {
        log.debug("Finding idempotency record by key and tenant: {}, {}", idempotencyKey, tenantId);
        
        return jpaRepository.findByIdempotencyKeyAndTenantId(idempotencyKey, tenantId)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public IdempotencyRecord save(IdempotencyRecord record) {
        log.debug("Saving idempotency record: {}", record.id());
        
        IdempotencyRecordEntity entity = toEntity(record);
        IdempotencyRecordEntity savedEntity = jpaRepository.save(entity);
        
        log.debug("Idempotency record saved successfully: {}", savedEntity.getId());
        return toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IdempotencyRecord> findByTenantId(String tenantId) {
        log.debug("Finding idempotency records by tenant: {}", tenantId);
        
        return jpaRepository.findByTenantId(tenantId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IdempotencyRecord> findByPaymentId(String paymentId) {
        log.debug("Finding idempotency records by payment ID: {}", paymentId);
        
        return jpaRepository.findByPaymentId(paymentId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IdempotencyRecord> findByCreatedAtBefore(Instant beforeTime) {
        log.debug("Finding idempotency records created before: {}", beforeTime);
        
        return jpaRepository.findByCreatedAtBefore(beforeTime)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByTenantId(String tenantId) {
        log.debug("Counting idempotency records by tenant: {}", tenantId);
        
        return jpaRepository.countByTenantId(tenantId);
    }

    @Override
    @Transactional
    public int deleteByCreatedAtBefore(Instant beforeTime) {
        log.debug("Deleting idempotency records created before: {}", beforeTime);
        
        int deletedCount = jpaRepository.deleteByCreatedAtBefore(beforeTime);
        log.debug("Deleted {} idempotency records", deletedCount);
        return deletedCount;
    }

    /**
     * Convert entity to domain object
     */
    private IdempotencyRecord toDomain(IdempotencyRecordEntity entity) {
        return new IdempotencyRecord(
                entity.getId(),
                entity.getIdempotencyKey(),
                entity.getTenantId(),
                entity.getPaymentId(),
                entity.getCreatedAt()
        );
    }

    /**
     * Convert domain object to entity
     */
    private IdempotencyRecordEntity toEntity(IdempotencyRecord record) {
        return IdempotencyRecordEntity.builder()
                .id(record.id())
                .idempotencyKey(record.idempotencyKey())
                .tenantId(record.tenantId())
                .paymentId(record.paymentId())
                .createdAt(record.createdAt())
                .build();
    }
}
