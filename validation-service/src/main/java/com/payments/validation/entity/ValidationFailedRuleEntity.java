package com.payments.validation.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/**
 * JPA Entity for Validation Failed Rules
 *
 * <p>Maps to validation_failed_rules table with: - Failed rule details - Rule execution metadata -
 * Failure reasons and context
 */
@Entity
@Table(
    name = "validation_failed_rules",
    indexes = {
      @Index(
          name = "idx_validation_failed_rules_validation_result_id",
          columnList = "validation_result_id"),
      @Index(name = "idx_validation_failed_rules_rule_id", columnList = "rule_id")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationFailedRuleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "validation_result_id", nullable = false)
  private ValidationResultEntity validationResult;

  @Column(name = "rule_id", nullable = false)
  private String ruleId;

  @Column(name = "rule_name", nullable = false)
  private String ruleName;

  @Enumerated(EnumType.STRING)
  @Column(name = "rule_type", nullable = false)
  private RuleType ruleType;

  @Column(name = "failure_reason", nullable = false, columnDefinition = "TEXT")
  private String failureReason;

  @Column(name = "rule_metadata", columnDefinition = "jsonb")
  private String ruleMetadata;

  @Column(name = "failed_at", nullable = false)
  private Instant failedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  /** Rule Type Enum */
  public enum RuleType {
    BUSINESS,
    COMPLIANCE,
    FRAUD,
    RISK
  }
}
