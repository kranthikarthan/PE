package com.payments.validation.service;

import com.payments.domain.payment.PaymentId;
import com.payments.domain.shared.TenantContext;
import com.payments.domain.validation.ValidationResult;
import com.payments.domain.validation.ValidationStatus;
import com.payments.domain.validation.RiskLevel;
import com.payments.domain.validation.FailedRule;
import com.payments.domain.validation.RuleType;
import com.payments.validation.entity.ValidationResultEntity;
import com.payments.validation.mapper.ValidationResultMapper;
import com.payments.validation.repository.ValidationResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ValidationResultService
 */
@ExtendWith(MockitoExtension.class)
class ValidationResultServiceTest {

    @Mock
    private ValidationResultRepository repository;

    @Mock
    private ValidationResultMapper mapper;

    @InjectMocks
    private ValidationResultService service;

    private ValidationResult validResult;
    private ValidationResultEntity validEntity;

    @BeforeEach
    void setUp() {
        validResult = ValidationResult.builder()
                .validationId("validation-123")
                .paymentId(PaymentId.builder().value("payment-123").build())
                .tenantContext(TenantContext.builder()
                        .tenantId("tenant-1")
                        .businessUnitId("business-unit-1")
                        .build())
                .status(ValidationStatus.PASSED)
                .riskLevel(RiskLevel.LOW)
                .fraudScore(0)
                .riskScore(0)
                .appliedRules(List.of("RULE_001", "RULE_002"))
                .failedRules(List.of())
                .validationMetadata("{\"test\": \"data\"}")
                .validatedAt(Instant.now())
                .correlationId("correlation-123")
                .createdBy("validation-service")
                .build();

        validEntity = ValidationResultEntity.builder()
                .validationId("validation-123")
                .paymentId("payment-123")
                .tenantId("tenant-1")
                .businessUnitId("business-unit-1")
                .status(ValidationResultEntity.ValidationStatus.PASSED)
                .riskLevel(ValidationResultEntity.RiskLevel.LOW)
                .fraudScore(0)
                .riskScore(0)
                .appliedRules(List.of("RULE_001", "RULE_002"))
                .validatedAt(Instant.now())
                .correlationId("correlation-123")
                .createdBy("validation-service")
                .build();
    }

    @Test
    void saveValidationResult_WithValidResult_ShouldSaveAndReturn() {
        // Given
        when(mapper.toEntity(validResult)).thenReturn(validEntity);
        when(repository.save(validEntity)).thenReturn(validEntity);
        when(mapper.toDomain(validEntity)).thenReturn(validResult);

        // When
        ValidationResult result = service.saveValidationResult(validResult);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getValidationId()).isEqualTo("validation-123");
        verify(mapper).toEntity(validResult);
        verify(repository).save(validEntity);
        verify(mapper).toDomain(validEntity);
    }

    @Test
    void findByValidationId_WithValidId_ShouldReturnResult() {
        // Given
        when(repository.findByValidationId("validation-123")).thenReturn(Optional.of(validEntity));
        when(mapper.toDomain(validEntity)).thenReturn(validResult);

        // When
        Optional<ValidationResult> result = service.findByValidationId("validation-123");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getValidationId()).isEqualTo("validation-123");
        verify(repository).findByValidationId("validation-123");
        verify(mapper).toDomain(validEntity);
    }

    @Test
    void findByValidationId_WithInvalidId_ShouldReturnEmpty() {
        // Given
        when(repository.findByValidationId("invalid-id")).thenReturn(Optional.empty());

        // When
        Optional<ValidationResult> result = service.findByValidationId("invalid-id");

        // Then
        assertThat(result).isEmpty();
        verify(repository).findByValidationId("invalid-id");
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void findByPaymentId_WithValidId_ShouldReturnResults() {
        // Given
        List<ValidationResultEntity> entities = List.of(validEntity);
        when(repository.findByPaymentIdOrderByValidatedAtDesc("payment-123")).thenReturn(entities);
        when(mapper.toDomain(validEntity)).thenReturn(validResult);

        // When
        List<ValidationResult> results = service.findByPaymentId("payment-123");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getValidationId()).isEqualTo("validation-123");
        verify(repository).findByPaymentIdOrderByValidatedAtDesc("payment-123");
        verify(mapper).toDomain(validEntity);
    }

    @Test
    void findByTenantId_WithValidId_ShouldReturnPage() {
        // Given
        Page<ValidationResultEntity> entityPage = new PageImpl<>(List.of(validEntity));
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findByTenantIdOrderByValidatedAtDesc("tenant-1", pageable)).thenReturn(entityPage);
        when(mapper.toDomain(validEntity)).thenReturn(validResult);

        // When
        Page<ValidationResult> results = service.findByTenantId("tenant-1", pageable);

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getValidationId()).isEqualTo("validation-123");
        verify(repository).findByTenantIdOrderByValidatedAtDesc("tenant-1", pageable);
        verify(mapper).toDomain(validEntity);
    }

    @Test
    void findByStatus_WithValidStatus_ShouldReturnPage() {
        // Given
        Page<ValidationResultEntity> entityPage = new PageImpl<>(List.of(validEntity));
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findByStatusOrderByValidatedAtDesc(any(), any(Pageable.class))).thenReturn(entityPage);
        when(mapper.toDomain(validEntity)).thenReturn(validResult);

        // When
        Page<ValidationResult> results = service.findByStatus(ValidationStatus.PASSED, pageable);

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getStatus()).isEqualTo(ValidationStatus.PASSED);
        verify(repository).findByStatusOrderByValidatedAtDesc(any(), any(Pageable.class));
        verify(mapper).toDomain(validEntity);
    }

    @Test
    void findByRiskLevel_WithValidLevel_ShouldReturnPage() {
        // Given
        Page<ValidationResultEntity> entityPage = new PageImpl<>(List.of(validEntity));
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findByRiskLevelOrderByValidatedAtDesc(any(), any(Pageable.class))).thenReturn(entityPage);
        when(mapper.toDomain(validEntity)).thenReturn(validResult);

        // When
        Page<ValidationResult> results = service.findByRiskLevel(RiskLevel.LOW, pageable);

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getRiskLevel()).isEqualTo(RiskLevel.LOW);
        verify(repository).findByRiskLevelOrderByValidatedAtDesc(any(), any(Pageable.class));
        verify(mapper).toDomain(validEntity);
    }

    @Test
    void getValidationStatistics_WithValidTenant_ShouldReturnStatistics() {
        // Given
        Object[] stats = {2L, 1L, 1L, 25.0, 30.0};
        when(repository.getValidationStatistics("tenant-1")).thenReturn(stats);

        // When
        ValidationResultService.ValidationStatistics result = service.getValidationStatistics("tenant-1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalValidations()).isEqualTo(2L);
        assertThat(result.getPassedValidations()).isEqualTo(1L);
        assertThat(result.getFailedValidations()).isEqualTo(1L);
        assertThat(result.getAverageFraudScore()).isEqualTo(25.0);
        assertThat(result.getAverageRiskScore()).isEqualTo(30.0);
        verify(repository).getValidationStatistics("tenant-1");
    }

    @Test
    void getValidationStatistics_WithNoData_ShouldReturnEmptyStatistics() {
        // Given
        when(repository.getValidationStatistics("tenant-1")).thenReturn(null);

        // When
        ValidationResultService.ValidationStatistics result = service.getValidationStatistics("tenant-1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalValidations()).isEqualTo(0L);
        assertThat(result.getPassedValidations()).isEqualTo(0L);
        assertThat(result.getFailedValidations()).isEqualTo(0L);
        assertThat(result.getAverageFraudScore()).isEqualTo(0.0);
        assertThat(result.getAverageRiskScore()).isEqualTo(0.0);
    }

    @Test
    void cleanupOldValidationResults_WithValidDate_ShouldReturnDeletedCount() {
        // Given
        Instant cutoffDate = Instant.now().minusSeconds(86400); // 24 hours ago
        when(repository.deleteByValidatedAtBefore(cutoffDate)).thenReturn(5);

        // When
        int deletedCount = service.cleanupOldValidationResults(cutoffDate);

        // Then
        assertThat(deletedCount).isEqualTo(5);
        verify(repository).deleteByValidatedAtBefore(cutoffDate);
    }

    @Test
    void saveValidationResult_WithException_ShouldThrowRuntimeException() {
        // Given
        when(mapper.toEntity(validResult)).thenReturn(validEntity);
        when(repository.save(validEntity)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        try {
            service.saveValidationResult(validResult);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Failed to save validation result");
            assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
            assertThat(e.getCause().getMessage()).isEqualTo("Database error");
        }
    }
}
