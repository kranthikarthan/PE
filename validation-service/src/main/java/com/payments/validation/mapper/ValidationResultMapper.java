package com.payments.validation.mapper;

import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import com.payments.domain.validation.FailedRule;
import com.payments.domain.validation.RiskLevel;
import com.payments.domain.validation.ValidationResult;
import com.payments.domain.validation.ValidationStatus;
import com.payments.validation.entity.ValidationResultEntity;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validation Result Mapper
 *
 * <p>Maps between domain models and JPA entities: - Domain to Entity conversion - Entity to Domain
 * conversion - Enum mapping - List serialization/deserialization
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationResultMapper {

  /**
   * Convert domain model to entity
   *
   * @param domain Domain validation result
   * @return JPA entity
   */
  public ValidationResultEntity toEntity(ValidationResult domain) {
    if (domain == null) {
      return null;
    }

    try {
      return ValidationResultEntity.builder()
          .validationId(domain.getValidationId())
          .paymentId(domain.getPaymentId().getValue())
          .tenantId(domain.getTenantContext().getTenantId())
          .businessUnitId(domain.getTenantContext().getBusinessUnitId())
          .status(mapValidationStatus(domain.getStatus()))
          .riskLevel(mapRiskLevel(domain.getRiskLevel()))
          .fraudScore(domain.getFraudScore())
          .riskScore(domain.getRiskScore())
          .appliedRules(serializeStringList(domain.getAppliedRules()))
          .failedRules(serializeFailedRules(domain.getFailedRules()))
          .validationMetadata(domain.getValidationMetadata())
          .validatedAt(domain.getValidatedAt())
          .correlationId(domain.getCorrelationId())
          .createdBy(domain.getCreatedBy())
          .reason(domain.getReason())
          .build();

    } catch (Exception e) {
      log.error("Error converting domain to entity: {}", domain.getValidationId(), e);
      throw new RuntimeException("Failed to convert domain to entity", e);
    }
  }

  /**
   * Convert entity to domain model
   *
   * @param entity JPA entity
   * @return Domain validation result
   */
  public ValidationResult toDomain(ValidationResultEntity entity) {
    if (entity == null) {
      return null;
    }

    try {
      return ValidationResult.builder()
          .validationId(entity.getValidationId())
          .paymentId(new PaymentId(entity.getPaymentId()))
          .tenantContext(
              TenantContext.builder()
                  .tenantId(entity.getTenantId())
                  .businessUnitId(entity.getBusinessUnitId())
                  .build())
          .status(mapValidationStatus(entity.getStatus()))
          .riskLevel(mapRiskLevel(entity.getRiskLevel()))
          .fraudScore(entity.getFraudScore())
          .riskScore(entity.getRiskScore())
          .appliedRules(
              entity.getAppliedRules() != null ? List.of(entity.getAppliedRules()) : List.of())
          .failedRules(deserializeFailedRules(entity.getFailedRules()))
          .validationMetadata(entity.getValidationMetadata())
          .validatedAt(entity.getValidatedAt())
          .correlationId(entity.getCorrelationId())
          .createdBy(entity.getCreatedBy())
          .reason(entity.getReason())
          .build();

    } catch (Exception e) {
      log.error("Error converting entity to domain: {}", entity.getValidationId(), e);
      throw new RuntimeException("Failed to convert entity to domain", e);
    }
  }

  /** Map domain ValidationStatus to entity enum */
  private ValidationResultEntity.ValidationStatus mapValidationStatus(ValidationStatus domain) {
    if (domain == null) {
      return null;
    }
    return switch (domain) {
      case PASSED -> ValidationResultEntity.ValidationStatus.PASSED;
      case FAILED -> ValidationResultEntity.ValidationStatus.FAILED;
    };
  }

  /** Map entity ValidationStatus to domain enum */
  private ValidationStatus mapValidationStatus(ValidationResultEntity.ValidationStatus entity) {
    if (entity == null) {
      return null;
    }
    return switch (entity) {
      case PASSED -> ValidationStatus.PASSED;
      case FAILED -> ValidationStatus.FAILED;
    };
  }

  /** Map domain RiskLevel to entity enum */
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

  /** Map entity RiskLevel to domain enum */
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

  /** Serialize string list to JSON */
  private String serializeStringList(List<String> list) {
    if (list == null || list.isEmpty()) {
      return "[]";
    }
    return "["
        + list.stream()
            .map(s -> "\"" + s.replace("\"", "\\\"") + "\"")
            .collect(Collectors.joining(","))
        + "]";
  }

  /** Deserialize JSON to string list */
  private List<String> deserializeStringList(String json) {
    if (json == null || json.trim().isEmpty() || "[]".equals(json)) {
      return List.of();
    }
    // TODO: Implement proper JSON deserialization
    // For now, return empty list
    return List.of();
  }

  /** Serialize failed rules to JSON */
  private String serializeFailedRules(List<FailedRule> failedRules) {
    if (failedRules == null || failedRules.isEmpty()) {
      return "[]";
    }
    // TODO: Implement proper JSON serialization
    // For now, return empty array
    return "[]";
  }

  /** Deserialize JSON to failed rules list */
  private List<FailedRule> deserializeFailedRules(String json) {
    if (json == null || json.trim().isEmpty() || "[]".equals(json)) {
      return List.of();
    }
    // TODO: Implement proper JSON deserialization
    // For now, return empty list
    return List.of();
  }
}
