package com.payments.validation.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA Entity for Risk Assessment Results
 *
 * <p>Maps to risk_assessment_results table with: - Risk assessment details - Risk level and score -
 * Assessment factors
 */
@Entity
@Table(
    name = "risk_assessment_results",
    indexes = {
      @Index(name = "idx_risk_assessment_results_payment_id", columnList = "payment_id"),
      @Index(name = "idx_risk_assessment_results_tenant_id", columnList = "tenant_id"),
      @Index(name = "idx_risk_assessment_results_assessed_at", columnList = "assessed_at")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentResultEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "validation_result_id")
  private ValidationResultEntity validationResult;

  @Column(name = "payment_id", nullable = false)
  private String paymentId;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Enumerated(EnumType.STRING)
  @Column(name = "risk_level", nullable = false)
  private RiskLevel riskLevel;

  @Column(name = "risk_score", nullable = false)
  private Integer riskScore;

  @Column(name = "risk_factors", columnDefinition = "jsonb")
  private String riskFactors;

  @Column(name = "assessed_at", nullable = false)
  private Instant assessedAt;

  @Column(name = "correlation_id")
  private String correlationId;

  /** Risk Level Enum */
  public enum RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
  }
}
