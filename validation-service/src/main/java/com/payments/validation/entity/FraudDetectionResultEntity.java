package com.payments.validation.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA Entity for Fraud Detection Results
 *
 * <p>Maps to fraud_detection_results table with: - Fraud detection details - Fraud score and
 * reasons - Detection metadata
 */
@Entity
@Table(
    name = "fraud_detection_results",
    indexes = {
      @Index(name = "idx_fraud_detection_results_payment_id", columnList = "payment_id"),
      @Index(name = "idx_fraud_detection_results_tenant_id", columnList = "tenant_id"),
      @Index(name = "idx_fraud_detection_results_detected_at", columnList = "detected_at")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudDetectionResultEntity {

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

  @Column(name = "fraud_score", nullable = false)
  private Integer fraudScore;

  @ElementCollection
  @CollectionTable(
      name = "fraud_detection_reasons",
      joinColumns = @JoinColumn(name = "fraud_detection_result_id"))
  @Column(name = "reason")
  private List<String> fraudReasons;

  @Column(name = "fraud_metadata", columnDefinition = "jsonb")
  private String fraudMetadata;

  @Column(name = "detected_at", nullable = false)
  private Instant detectedAt;

  @Column(name = "correlation_id")
  private String correlationId;
}
