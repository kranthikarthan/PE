package com.payments.validation.repository;

import com.payments.validation.entity.ValidationResultEntity;
import com.payments.validation.entity.ValidationResultEntity.ValidationStatus;
import com.payments.validation.entity.ValidationResultEntity.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for ValidationResultEntity
 */
@DataJpaTest
@ActiveProfiles("test")
class ValidationResultRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ValidationResultRepository repository;

    private ValidationResultEntity validResult;
    private ValidationResultEntity failedResult;
    private ValidationResultEntity highRiskResult;

    @BeforeEach
    void setUp() {
        // Create test data
        validResult = ValidationResultEntity.builder()
                .validationId("validation-123")
                .paymentId("payment-123")
                .tenantId("tenant-1")
                .businessUnitId("business-unit-1")
                .status(ValidationStatus.PASSED)
                .riskLevel(RiskLevel.LOW)
                .fraudScore(0)
                .riskScore(0)
                .appliedRules(List.of("RULE_001", "RULE_002"))
                .validatedAt(Instant.now())
                .correlationId("correlation-123")
                .createdBy("validation-service")
                .build();

        failedResult = ValidationResultEntity.builder()
                .validationId("validation-456")
                .paymentId("payment-456")
                .tenantId("tenant-1")
                .businessUnitId("business-unit-1")
                .status(ValidationStatus.FAILED)
                .riskLevel(RiskLevel.HIGH)
                .fraudScore(25)
                .riskScore(30)
                .appliedRules(List.of("RULE_001"))
                .validatedAt(Instant.now())
                .correlationId("correlation-456")
                .createdBy("validation-service")
                .build();

        highRiskResult = ValidationResultEntity.builder()
                .validationId("validation-789")
                .paymentId("payment-789")
                .tenantId("tenant-2")
                .businessUnitId("business-unit-2")
                .status(ValidationStatus.PASSED)
                .riskLevel(RiskLevel.CRITICAL)
                .fraudScore(75)
                .riskScore(80)
                .appliedRules(List.of("RULE_001", "RULE_002", "RULE_003"))
                .validatedAt(Instant.now())
                .correlationId("correlation-789")
                .createdBy("validation-service")
                .build();

        entityManager.persistAndFlush(validResult);
        entityManager.persistAndFlush(failedResult);
        entityManager.persistAndFlush(highRiskResult);
    }

    @Test
    void findByValidationId_WithValidId_ShouldReturnResult() {
        // When
        Optional<ValidationResultEntity> result = repository.findByValidationId("validation-123");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPaymentId()).isEqualTo("payment-123");
        assertThat(result.get().getStatus()).isEqualTo(ValidationStatus.PASSED);
    }

    @Test
    void findByValidationId_WithInvalidId_ShouldReturnEmpty() {
        // When
        Optional<ValidationResultEntity> result = repository.findByValidationId("invalid-id");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByPaymentId_WithValidId_ShouldReturnResults() {
        // When
        List<ValidationResultEntity> results = repository.findByPaymentId("payment-123");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getValidationId()).isEqualTo("validation-123");
    }

    @Test
    void findByTenantId_WithValidId_ShouldReturnResults() {
        // When
        Page<ValidationResultEntity> results = repository.findByTenantId("tenant-1", PageRequest.of(0, 10));

        // Then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).extracting(ValidationResultEntity::getTenantId)
                .containsOnly("tenant-1");
    }

    @Test
    void findByTenantIdAndBusinessUnitId_WithValidIds_ShouldReturnResults() {
        // When
        Page<ValidationResultEntity> results = repository.findByTenantIdAndBusinessUnitId(
                "tenant-1", "business-unit-1", PageRequest.of(0, 10));

        // Then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).extracting(ValidationResultEntity::getTenantId)
                .containsOnly("tenant-1");
        assertThat(results.getContent()).extracting(ValidationResultEntity::getBusinessUnitId)
                .containsOnly("business-unit-1");
    }

    @Test
    void findByCorrelationId_WithValidId_ShouldReturnResults() {
        // When
        List<ValidationResultEntity> results = repository.findByCorrelationId("correlation-123");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getValidationId()).isEqualTo("validation-123");
    }

    @Test
    void findByStatus_WithValidStatus_ShouldReturnResults() {
        // When
        Page<ValidationResultEntity> results = repository.findByStatus(ValidationStatus.PASSED, PageRequest.of(0, 10));

        // Then
        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).extracting(ValidationResultEntity::getStatus)
                .containsOnly(ValidationStatus.PASSED);
    }

    @Test
    void findByRiskLevel_WithValidLevel_ShouldReturnResults() {
        // When
        Page<ValidationResultEntity> results = repository.findByRiskLevel(RiskLevel.CRITICAL, PageRequest.of(0, 10));

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.get(0).getRiskLevel()).isEqualTo(RiskLevel.CRITICAL);
    }

    @Test
    void findByFraudScoreGreaterThanEqual_WithThreshold_ShouldReturnResults() {
        // When
        Page<ValidationResultEntity> results = repository.findByFraudScoreGreaterThanEqual(50, PageRequest.of(0, 10));

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.get(0).getFraudScore()).isGreaterThanOrEqualTo(50);
    }

    @Test
    void findByRiskScoreGreaterThanEqual_WithThreshold_ShouldReturnResults() {
        // When
        Page<ValidationResultEntity> results = repository.findByRiskScoreGreaterThanEqual(50, PageRequest.of(0, 10));

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.get(0).getRiskScore()).isGreaterThanOrEqualTo(50);
    }

    @Test
    void getValidationStatistics_WithValidTenant_ShouldReturnStatistics() {
        // When
        Object[] stats = repository.getValidationStatistics("tenant-1");

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats).hasSize(5);
        assertThat(stats[0]).isEqualTo(2L); // totalValidations
        assertThat(stats[1]).isEqualTo(1L); // passedValidations
        assertThat(stats[2]).isEqualTo(1L); // failedValidations
    }

    @Test
    void countByTenantIdAndStatus_WithValidParams_ShouldReturnCount() {
        // When
        long count = repository.countByTenantIdAndStatus("tenant-1", ValidationStatus.PASSED);

        // Then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void countByTenantIdAndRiskLevel_WithValidParams_ShouldReturnCount() {
        // When
        long count = repository.countByTenantIdAndRiskLevel("tenant-1", RiskLevel.HIGH);

        // Then
        assertThat(count).isEqualTo(1L);
    }
}
