package com.payments.saga.service;

import com.payments.domain.shared.TenantContext;
import com.payments.saga.domain.Saga;
import com.payments.saga.domain.SagaId;
import com.payments.saga.domain.SagaStatus;
import com.payments.saga.entity.SagaEntity;
import com.payments.saga.repository.SagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Saga Management Service
 * 
 * Provides business logic for saga management operations.
 * Enables operations teams to monitor, manage, and control sagas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaManagementService {

    private final SagaRepository sagaRepository;
    private final SagaOrchestrator sagaOrchestrator;

    /**
     * Get all sagas with filtering and pagination
     */
    @Transactional(readOnly = true)
    public SagaListResult getAllSagas(
            String tenantId,
            String businessUnitId,
            int page,
            int size,
            String status,
            String template,
            String startDate,
            String endDate) {
        
        log.info("Getting all sagas for tenant: {}, business unit: {}, page: {}, size: {}", 
                tenantId, businessUnitId, page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            
            // Build query based on filters
            Page<SagaEntity> sagaPage = sagaRepository.findByTenantIdAndBusinessUnitId(
                tenantId, businessUnitId, pageable);
            
            List<Saga> sagas = sagaPage.getContent().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
            
            return SagaListResult.builder()
                .sagas(sagas)
                .totalCount((int) sagaPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to retrieve sagas for tenant: {}, business unit: {}", 
                    tenantId, businessUnitId, e);
            throw new RuntimeException("Failed to retrieve sagas", e);
        }
    }

    /**
     * Get saga details with full information
     */
    @Transactional(readOnly = true)
    public SagaDetails getSagaDetails(String sagaId, String tenantId, String businessUnitId) {
        log.info("Getting saga details for: {}, tenant: {}, business unit: {}", 
                sagaId, tenantId, businessUnitId);
        
        try {
            Optional<SagaEntity> sagaEntity = sagaRepository.findBySagaIdAndTenantIdAndBusinessUnitId(
                sagaId, tenantId, businessUnitId);
            
            if (sagaEntity.isEmpty()) {
                throw new IllegalArgumentException("Saga not found: " + sagaId);
            }
            
            Saga saga = mapToDomain(sagaEntity.get());
            
            return SagaDetails.builder()
                .sagaId(saga.getSagaId().getValue())
                .status(saga.getStatus().name())
                .templateName(saga.getTemplateName())
                .paymentId(saga.getPaymentId())
                .correlationId(saga.getCorrelationId())
                .startedAt(saga.getCreatedAt().toString())
                .lastUpdatedAt(saga.getUpdatedAt().toString())
                .steps(saga.getSteps())
                .events(saga.getEvents())
                .build();
                
        } catch (IllegalArgumentException e) {
            log.warn("Saga not found: {}", sagaId);
            throw e;
        } catch (Exception e) {
            log.error("Failed to retrieve saga details for: {}", sagaId, e);
            throw new RuntimeException("Failed to retrieve saga details", e);
        }
    }

    /**
     * Resume a paused saga
     */
    @Transactional
    public Saga resumeSaga(String sagaId, String reason, boolean forceResume, 
                          String userId, String tenantId, String businessUnitId) {
        log.info("Resuming saga: {} by user: {}, tenant: {}, business unit: {}", 
                sagaId, userId, tenantId, businessUnitId);
        
        try {
            Optional<SagaEntity> sagaEntity = sagaRepository.findBySagaIdAndTenantIdAndBusinessUnitId(
                sagaId, tenantId, businessUnitId);
            
            if (sagaEntity.isEmpty()) {
                throw new IllegalArgumentException("Saga not found: " + sagaId);
            }
            
            Saga saga = mapToDomain(sagaEntity.get());
            
            // Check if saga can be resumed
            if (!canResumeSaga(saga) && !forceResume) {
                throw new IllegalArgumentException("Saga cannot be resumed in current state: " + saga.getStatus());
            }
            
            // Resume the saga
            Saga resumedSaga = sagaOrchestrator.resumeSaga(SagaId.of(sagaId), reason);
            
            log.info("Saga resumed successfully: {}", sagaId);
            return resumedSaga;
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot resume saga: {} - {}", sagaId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to resume saga: {}", sagaId, e);
            throw new RuntimeException("Failed to resume saga", e);
        }
    }

    /**
     * Force complete a saga
     */
    @Transactional
    public Saga forceCompleteSaga(String sagaId, String reason, boolean skipCompensation,
                                 String userId, String tenantId, String businessUnitId) {
        log.info("Force completing saga: {} by user: {}, tenant: {}, business unit: {}", 
                sagaId, userId, tenantId, businessUnitId);
        
        try {
            Optional<SagaEntity> sagaEntity = sagaRepository.findBySagaIdAndTenantIdAndBusinessUnitId(
                sagaId, tenantId, businessUnitId);
            
            if (sagaEntity.isEmpty()) {
                throw new IllegalArgumentException("Saga not found: " + sagaId);
            }
            
            Saga saga = mapToDomain(sagaEntity.get());
            
            // Check if saga can be force completed
            if (!canForceCompleteSaga(saga)) {
                throw new IllegalArgumentException("Saga cannot be force completed in current state: " + saga.getStatus());
            }
            
            // Force complete the saga
            Saga completedSaga = sagaOrchestrator.forceCompleteSaga(SagaId.of(sagaId), reason, skipCompensation);
            
            log.info("Saga force completed successfully: {}", sagaId);
            return completedSaga;
            
        } catch (IllegalArgumentException e) {
            log.warn("Cannot force complete saga: {} - {}", sagaId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to force complete saga: {}", sagaId, e);
            throw new RuntimeException("Failed to force complete saga", e);
        }
    }

    /**
     * Get saga statistics
     */
    @Transactional(readOnly = true)
    public SagaStatistics getSagaStatistics(String tenantId, String businessUnitId) {
        log.info("Getting saga statistics for tenant: {}, business unit: {}", tenantId, businessUnitId);
        
        try {
            List<SagaEntity> allSagas = sagaRepository.findByTenantIdAndBusinessUnitId(tenantId, businessUnitId);
            
            int totalSagas = allSagas.size();
            int runningSagas = (int) allSagas.stream()
                .filter(saga -> saga.getStatus().equals("RUNNING"))
                .count();
            int completedSagas = (int) allSagas.stream()
                .filter(saga -> saga.getStatus().equals("COMPLETED"))
                .count();
            int failedSagas = (int) allSagas.stream()
                .filter(saga -> saga.getStatus().equals("FAILED"))
                .count();
            
            double successRate = totalSagas > 0 ? (double) completedSagas / totalSagas * 100 : 0.0;
            
            // Calculate average execution time
            double averageExecutionTime = allSagas.stream()
                .filter(saga -> saga.getStatus().equals("COMPLETED"))
                .mapToLong(saga -> {
                    if (saga.getCompletedAt() != null && saga.getCreatedAt() != null) {
                        return saga.getCompletedAt().toEpochSecond(ZoneOffset.UTC) - 
                               saga.getCreatedAt().toEpochSecond(ZoneOffset.UTC);
                    }
                    return 0;
                })
                .average()
                .orElse(0.0);
            
            return SagaStatistics.builder()
                .totalSagas(totalSagas)
                .runningSagas(runningSagas)
                .completedSagas(completedSagas)
                .failedSagas(failedSagas)
                .averageExecutionTime(averageExecutionTime)
                .successRate(successRate)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to retrieve saga statistics", e);
            throw new RuntimeException("Failed to retrieve saga statistics", e);
        }
    }

    /**
     * Get failed sagas for repair
     */
    @Transactional(readOnly = true)
    public FailedSagasResult getFailedSagas(String tenantId, String businessUnitId, int page, int size) {
        log.info("Getting failed sagas for tenant: {}, business unit: {}, page: {}, size: {}", 
                tenantId, businessUnitId, page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
            
            Page<SagaEntity> failedSagasPage = sagaRepository.findByTenantIdAndBusinessUnitIdAndStatus(
                tenantId, businessUnitId, "FAILED", pageable);
            
            List<Saga> failedSagas = failedSagasPage.getContent().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
            
            return FailedSagasResult.builder()
                .failedSagas(failedSagas)
                .totalCount((int) failedSagasPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to retrieve failed sagas", e);
            throw new RuntimeException("Failed to retrieve failed sagas", e);
        }
    }

    /**
     * Check if saga can be resumed
     */
    private boolean canResumeSaga(Saga saga) {
        return saga.getStatus() == SagaStatus.PAUSED || 
               saga.getStatus() == SagaStatus.FAILED ||
               saga.getStatus() == SagaStatus.RETRYING;
    }

    /**
     * Check if saga can be force completed
     */
    private boolean canForceCompleteSaga(Saga saga) {
        return saga.getStatus() == SagaStatus.RUNNING || 
               saga.getStatus() == SagaStatus.PAUSED ||
               saga.getStatus() == SagaStatus.FAILED;
    }

    /**
     * Map entity to domain object
     */
    private Saga mapToDomain(SagaEntity entity) {
        // This would be implemented based on the actual mapping logic
        return Saga.builder()
            .sagaId(SagaId.of(entity.getSagaId()))
            .templateName(entity.getTemplateName())
            .status(SagaStatus.valueOf(entity.getStatus()))
            .paymentId(entity.getPaymentId())
            .correlationId(entity.getCorrelationId())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    // Result DTOs

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SagaListResult {
        private List<Saga> sagas;
        private Integer totalCount;
        private Integer page;
        private Integer size;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SagaDetails {
        private String sagaId;
        private String status;
        private String templateName;
        private String paymentId;
        private String correlationId;
        private String startedAt;
        private String lastUpdatedAt;
        private List<com.payments.saga.domain.SagaStep> steps;
        private List<com.payments.saga.domain.SagaEvent> events;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SagaStatistics {
        private Integer totalSagas;
        private Integer runningSagas;
        private Integer completedSagas;
        private Integer failedSagas;
        private Double averageExecutionTime;
        private Double successRate;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FailedSagasResult {
        private List<Saga> failedSagas;
        private Integer totalCount;
        private Integer page;
        private Integer size;
    }
}
