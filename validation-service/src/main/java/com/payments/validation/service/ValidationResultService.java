package com.payments.validation.service;

import com.payments.domain.payment.PaymentId;
import com.payments.domain.shared.TenantContext;
import com.payments.domain.validation.ValidationResult;
import com.payments.domain.validation.ValidationStatus;
import com.payments.domain.validation.RiskLevel;
import com.payments.domain.validation.FailedRule;
import com.payments.validation.entity.ValidationResultEntity;
import com.payments.validation.mapper.ValidationResultMapper;
import com.payments.validation.repository.ValidationResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for ValidationResult persistence and retrieval
 * 
 * Provides business logic for:
 * - Saving validation results
 * - Retrieving validation results
 * - Analytics and reporting
 * - Data cleanup operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationResultService {

    private final ValidationResultRepository repository;
    private final ValidationResultMapper mapper;

    /**
     * Save validation result
     * 
     * @param validationResult Domain validation result
     * @return Saved validation result
     */
    @Transactional
    public ValidationResult saveValidationResult(ValidationResult validationResult) {
        log.debug("Saving validation result: {} for payment: {}", 
                validationResult.getValidationId(), validationResult.getPaymentId().getValue());
        
        try {
            ValidationResultEntity entity = mapper.toEntity(validationResult);
            ValidationResultEntity savedEntity = repository.save(entity);
            
            log.info("Successfully saved validation result: {} for payment: {}", 
                    savedEntity.getValidationId(), savedEntity.getPaymentId());
            
            return mapper.toDomain(savedEntity);
            
        } catch (Exception e) {
            log.error("Failed to save validation result: {}", validationResult.getValidationId(), e);
            throw new RuntimeException("Failed to save validation result", e);
        }
    }

    /**
     * Find validation result by validation ID
     * 
     * @param validationId Validation ID
     * @return Optional validation result
     */
    @Transactional(readOnly = true)
    public Optional<ValidationResult> findByValidationId(String validationId) {
        log.debug("Finding validation result by validation ID: {}", validationId);
        
        return repository.findByValidationId(validationId)
                .map(mapper::toDomain);
    }

    /**
     * Find validation results by payment ID
     * 
     * @param paymentId Payment ID
     * @return List of validation results
     */
    @Transactional(readOnly = true)
    public List<ValidationResult> findByPaymentId(String paymentId) {
        log.debug("Finding validation results by payment ID: {}", paymentId);
        
        return repository.findByPaymentIdOrderByValidatedAtDesc(paymentId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * Find validation results by tenant ID
     * 
     * @param tenantId Tenant ID
     * @param pageable Pageable parameters
     * @return Page of validation results
     */
    @Transactional(readOnly = true)
    public Page<ValidationResult> findByTenantId(String tenantId, Pageable pageable) {
        log.debug("Finding validation results by tenant ID: {}", tenantId);
        
        return repository.findByTenantIdOrderByValidatedAtDesc(tenantId, pageable)
                .map(mapper::toDomain);
    }

    /**
     * Find validation results by tenant and business unit
     * 
     * @param tenantId Tenant ID
     * @param businessUnitId Business unit ID
     * @param pageable Pageable parameters
     * @return Page of validation results
     */
    @Transactional(readOnly = true)
    public Page<ValidationResult> findByTenantIdAndBusinessUnitId(
            String tenantId, String businessUnitId, Pageable pageable) {
        log.debug("Finding validation results by tenant: {} and business unit: {}", 
                tenantId, businessUnitId);
        
        return repository.findByTenantIdAndBusinessUnitIdOrderByValidatedAtDesc(
                tenantId, businessUnitId, pageable)
                .map(mapper::toDomain);
    }

    /**
     * Find validation results by correlation ID
     * 
     * @param correlationId Correlation ID
     * @return List of validation results
     */
    @Transactional(readOnly = true)
    public List<ValidationResult> findByCorrelationId(String correlationId) {
        log.debug("Finding validation results by correlation ID: {}", correlationId);
        
        return repository.findByCorrelationIdOrderByValidatedAtDesc(correlationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * Find validation results by status
     * 
     * @param status Validation status
     * @param pageable Pageable parameters
     * @return Page of validation results
     */
    @Transactional(readOnly = true)
    public Page<ValidationResult> findByStatus(ValidationStatus status, Pageable pageable) {
        log.debug("Finding validation results by status: {}", status);
        
        ValidationResultEntity.ValidationStatus entityStatus = mapValidationStatus(status);
        return repository.findByStatusOrderByValidatedAtDesc(entityStatus, pageable)
                .map(mapper::toDomain);
    }

    /**
     * Find validation results by risk level
     * 
     * @param riskLevel Risk level
     * @param pageable Pageable parameters
     * @return Page of validation results
     */
    @Transactional(readOnly = true)
    public Page<ValidationResult> findByRiskLevel(RiskLevel riskLevel, Pageable pageable) {
        log.debug("Finding validation results by risk level: {}", riskLevel);
        
        ValidationResultEntity.RiskLevel entityRiskLevel = mapRiskLevel(riskLevel);
        return repository.findByRiskLevelOrderByValidatedAtDesc(entityRiskLevel, pageable)
                .map(mapper::toDomain);
    }

    /**
     * Find validation results within date range
     * 
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pageable parameters
     * @return Page of validation results
     */
    @Transactional(readOnly = true)
    public Page<ValidationResult> findByValidatedAtBetween(
            Instant startDate, Instant endDate, Pageable pageable) {
        log.debug("Finding validation results between: {} and {}", startDate, endDate);
        
        return repository.findByValidatedAtBetween(startDate, endDate, pageable)
                .map(mapper::toDomain);
    }

    /**
     * Get validation statistics for tenant
     * 
     * @param tenantId Tenant ID
     * @return Validation statistics
     */
    @Transactional(readOnly = true)
    public ValidationStatistics getValidationStatistics(String tenantId) {
        log.debug("Getting validation statistics for tenant: {}", tenantId);
        
        Object[] stats = repository.getValidationStatistics(tenantId);
        if (stats == null || stats.length == 0) {
            return ValidationStatistics.empty();
        }
        
        return ValidationStatistics.builder()
                .totalValidations(((Number) stats[0]).longValue())
                .passedValidations(((Number) stats[1]).longValue())
                .failedValidations(((Number) stats[2]).longValue())
                .averageFraudScore(stats[3] != null ? ((Number) stats[3]).doubleValue() : 0.0)
                .averageRiskScore(stats[4] != null ? ((Number) stats[4]).doubleValue() : 0.0)
                .build();
    }

    /**
     * Clean up old validation results
     * 
     * @param cutoffDate Cutoff date
     * @return Number of deleted records
     */
    @Transactional
    public int cleanupOldValidationResults(Instant cutoffDate) {
        log.info("Cleaning up validation results older than: {}", cutoffDate);
        
        int deletedCount = repository.deleteByValidatedAtBefore(cutoffDate);
        log.info("Cleaned up {} old validation results", deletedCount);
        
        return deletedCount;
    }

    /**
     * Map domain ValidationStatus to entity enum
     */
    private ValidationResultEntity.ValidationStatus mapValidationStatus(ValidationStatus domain) {
        return switch (domain) {
            case PASSED -> ValidationResultEntity.ValidationStatus.PASSED;
            case FAILED -> ValidationResultEntity.ValidationStatus.FAILED;
        };
    }

    /**
     * Map domain RiskLevel to entity enum
     */
    private ValidationResultEntity.RiskLevel mapRiskLevel(RiskLevel domain) {
        return switch (domain) {
            case LOW -> ValidationResultEntity.RiskLevel.LOW;
            case MEDIUM -> ValidationResultEntity.RiskLevel.MEDIUM;
            case HIGH -> ValidationResultEntity.RiskLevel.HIGH;
            case CRITICAL -> ValidationResultEntity.RiskLevel.CRITICAL;
        };
    }

    /**
     * Validation Statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ValidationStatistics {
        private Long totalValidations;
        private Long passedValidations;
        private Long failedValidations;
        private Double averageFraudScore;
        private Double averageRiskScore;

        public static ValidationStatistics empty() {
            return ValidationStatistics.builder()
                    .totalValidations(0L)
                    .passedValidations(0L)
                    .failedValidations(0L)
                    .averageFraudScore(0.0)
                    .averageRiskScore(0.0)
                    .build();
        }
    }
}
