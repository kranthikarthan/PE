package com.payments.validation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity for ValidationResult
 * 
 * Maps to validation_results table with:
 * - Validation result details
 * - Tenant and business unit context
 * - Risk and fraud assessment
 * - Audit trail information
 */
@Entity
@Table(name = "validation_results", indexes = {
    @Index(name = "idx_validation_results_payment_id", columnList = "payment_id"),
    @Index(name = "idx_validation_results_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_validation_results_business_unit_id", columnList = "business_unit_id"),
    @Index(name = "idx_validation_results_validated_at", columnList = "validated_at"),
    @Index(name = "idx_validation_results_correlation_id", columnList = "correlation_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "validation_id", nullable = false, unique = true)
    private String validationId;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "business_unit_id", nullable = false)
    private String businessUnitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ValidationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "fraud_score")
    @Builder.Default
    private Integer fraudScore = 0;

    @Column(name = "risk_score")
    @Builder.Default
    private Integer riskScore = 0;

    @ElementCollection
    @CollectionTable(name = "validation_applied_rules", joinColumns = @JoinColumn(name = "validation_result_id"))
    @Column(name = "rule_id")
    private List<String> appliedRules;

    @Column(name = "validation_metadata", columnDefinition = "jsonb")
    private String validationMetadata;

    @Column(name = "validated_at", nullable = false)
    private Instant validatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "created_by")
    @Builder.Default
    private String createdBy = "validation-service";

    @OneToMany(mappedBy = "validationResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ValidationFailedRuleEntity> failedRules;

    @OneToOne(mappedBy = "validationResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private FraudDetectionResultEntity fraudDetectionResult;

    @OneToOne(mappedBy = "validationResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RiskAssessmentResultEntity riskAssessmentResult;

    @OneToMany(mappedBy = "validationResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ValidationAuditTrailEntity> auditTrail;

    /**
     * Validation Status Enum
     */
    public enum ValidationStatus {
        PASSED, FAILED
    }

    /**
     * Risk Level Enum
     */
    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
