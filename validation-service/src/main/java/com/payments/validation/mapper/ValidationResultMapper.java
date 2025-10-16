package com.payments.validation.mapper;

import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import com.payments.domain.validation.ValidationResult;
import com.payments.domain.validation.ValidationStatus;
import com.payments.domain.validation.RiskLevel;
import com.payments.domain.validation.FailedRule;
import com.payments.domain.validation.RuleType;
import com.payments.validation.entity.ValidationResultEntity;
import com.payments.validation.entity.ValidationFailedRuleEntity;
import com.payments.validation.entity.FraudDetectionResultEntity;
import com.payments.validation.entity.RiskAssessmentResultEntity;
import com.payments.validation.entity.ValidationAuditTrailEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for ValidationResult domain and entity conversion
 * 
 * Handles mapping between:
 * - Domain ValidationResult and ValidationResultEntity
 * - Domain FailedRule and ValidationFailedRuleEntity
 * - Domain objects and JPA entities
 */
@Component
public class ValidationResultMapper {

    /**
     * Convert domain ValidationResult to entity
     */
    public ValidationResultEntity toEntity(ValidationResult domain) {
        if (domain == null) {
            return null;
        }

        return ValidationResultEntity.builder()
                .validationId(domain.getValidationId())
                .paymentId(domain.getPaymentId().getValue())
                .tenantId(domain.getTenantContext().getTenantId())
                .businessUnitId(domain.getTenantContext().getBusinessUnitId())
                .status(mapValidationStatus(domain.getStatus()))
                .riskLevel(mapRiskLevel(domain.getRiskLevel()))
                .fraudScore(domain.getFraudScore() != null ? domain.getFraudScore().intValue() : 0)
                .riskScore(domain.getRiskScore() != null ? domain.getRiskScore().intValue() : 0)
                .appliedRules(domain.getAppliedRules())
                .validationMetadata(domain.getValidationMetadata())
                .validatedAt(domain.getValidatedAt())
                .correlationId(domain.getCorrelationId())
                .createdBy(domain.getCreatedBy())
                .build();
    }

    /**
     * Convert entity to domain ValidationResult
     */
    public ValidationResult toDomain(ValidationResultEntity entity) {
        if (entity == null) {
            return null;
        }

        return ValidationResult.builder()
                .validationId(entity.getValidationId())
                .paymentId(new PaymentId(entity.getPaymentId()))
                .tenantContext(TenantContext.builder()
                        .tenantId(entity.getTenantId())
                        .businessUnitId(entity.getBusinessUnitId())
                        .build())
                .status(mapValidationStatus(entity.getStatus()))
                .riskLevel(mapRiskLevel(entity.getRiskLevel()))
                .fraudScore(BigDecimal.valueOf(entity.getFraudScore()))
                .riskScore(BigDecimal.valueOf(entity.getRiskScore()))
                .appliedRules(entity.getAppliedRules())
                .validationMetadata(entity.getValidationMetadata())
                .validatedAt(entity.getValidatedAt())
                .correlationId(entity.getCorrelationId())
                .createdBy(entity.getCreatedBy())
                .build();
    }

    /**
     * Convert domain FailedRule to entity
     */
    public ValidationFailedRuleEntity toEntity(FailedRule domain) {
        if (domain == null) {
            return null;
        }

        return ValidationFailedRuleEntity.builder()
                .ruleId(domain.getRuleId())
                .ruleName(domain.getRuleName())
                .ruleType(mapRuleType(RuleType.valueOf(domain.getRuleType())))
                .failureReason(domain.getFailureReason())
                .ruleMetadata(domain.getRuleMetadata())
                .failedAt(domain.getFailedAt())
                .build();
    }

    /**
     * Convert entity to domain FailedRule
     */
    public FailedRule toDomain(ValidationFailedRuleEntity entity) {
        if (entity == null) {
            return null;
        }

        return FailedRule.builder()
                .ruleId(entity.getRuleId())
                .ruleName(entity.getRuleName())
                .ruleType(mapRuleType(entity.getRuleType()).toString())
                .failureReason(entity.getFailureReason())
                .ruleMetadata(entity.getRuleMetadata())
                .failedAt(entity.getFailedAt())
                .build();
    }

    /**
     * Convert list of domain FailedRules to entities
     */
    public List<ValidationFailedRuleEntity> toEntities(List<FailedRule> domainList) {
        if (domainList == null) {
            return null;
        }

        return domainList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of entities to domain FailedRules
     */
    public List<FailedRule> toDomainList(List<ValidationFailedRuleEntity> entityList) {
        if (entityList == null) {
            return null;
        }

        return entityList.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Map domain ValidationStatus to entity enum
     */
    private ValidationResultEntity.ValidationStatus mapValidationStatus(ValidationStatus domain) {
        if (domain == null) {
            return null;
        }

        return switch (domain) {
            case PASSED -> ValidationResultEntity.ValidationStatus.PASSED;
            case FAILED -> ValidationResultEntity.ValidationStatus.FAILED;
        };
    }

    /**
     * Map entity enum to domain ValidationStatus
     */
    private ValidationStatus mapValidationStatus(ValidationResultEntity.ValidationStatus entity) {
        if (entity == null) {
            return null;
        }

        return switch (entity) {
            case PASSED -> ValidationStatus.PASSED;
            case FAILED -> ValidationStatus.FAILED;
        };
    }

    /**
     * Map domain RiskLevel to entity enum
     */
    private ValidationResultEntity.RiskLevel mapRiskLevel(RiskLevel domain) {
        if (domain == null) {
            return null;
        }

        return switch (domain) {
            case LOW -> ValidationResultEntity.RiskLevel.LOW;
            case MEDIUM -> ValidationResultEntity.RiskLevel.MEDIUM;
            case HIGH -> ValidationResultEntity.RiskLevel.HIGH;
            case CRITICAL -> ValidationResultEntity.RiskLevel.CRITICAL;
        };
    }

    /**
     * Map entity enum to domain RiskLevel
     */
    private RiskLevel mapRiskLevel(ValidationResultEntity.RiskLevel entity) {
        if (entity == null) {
            return null;
        }

        return switch (entity) {
            case LOW -> RiskLevel.LOW;
            case MEDIUM -> RiskLevel.MEDIUM;
            case HIGH -> RiskLevel.HIGH;
            case CRITICAL -> RiskLevel.CRITICAL;
        };
    }

    /**
     * Map domain RuleType to entity enum
     */
    private ValidationFailedRuleEntity.RuleType mapRuleType(RuleType domain) {
        if (domain == null) {
            return null;
        }

        return switch (domain) {
            case BUSINESS -> ValidationFailedRuleEntity.RuleType.BUSINESS;
            case COMPLIANCE -> ValidationFailedRuleEntity.RuleType.COMPLIANCE;
            case FRAUD -> ValidationFailedRuleEntity.RuleType.FRAUD;
            case RISK -> ValidationFailedRuleEntity.RuleType.RISK;
        };
    }

    /**
     * Map entity enum to domain RuleType
     */
    private RuleType mapRuleType(ValidationFailedRuleEntity.RuleType entity) {
        if (entity == null) {
            return null;
        }

        return switch (entity) {
            case BUSINESS -> RuleType.BUSINESS;
            case COMPLIANCE -> RuleType.COMPLIANCE;
            case FRAUD -> RuleType.FRAUD;
            case RISK -> RuleType.RISK;
        };
    }
}
