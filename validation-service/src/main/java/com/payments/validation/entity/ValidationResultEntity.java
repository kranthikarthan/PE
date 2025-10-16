package com.payments.validation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Validation Result Entity
 * 
 * JPA entity for validation results persistence:
 * - Validation outcome and metadata
 * - Risk and fraud assessment
 * - Applied and failed rules
 * - Audit trail information
 */
@Entity
@Table(name = "validation_results", indexes = {
    @Index(name = "idx_validation_id", columnList = "validation_id"),
    @Index(name = "idx_payment_id", columnList = "payment_id"),
    @Index(name = "idx_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_business_unit_id", columnList = "business_unit_id"),
    @Index(name = "idx_correlation_id", columnList = "correlation_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_risk_level", columnList = "risk_level"),
    @Index(name = "idx_validated_at", columnList = "validated_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResultEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "validation_id", nullable = false, unique = true, length = 255)
    private String validationId;
    
    @Column(name = "payment_id", nullable = false, length = 255)
    private String paymentId;
    
    @Column(name = "tenant_id", nullable = false, length = 255)
    private String tenantId;
    
    @Column(name = "business_unit_id", nullable = false, length = 255)
    private String businessUnitId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ValidationStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;
    
    @Column(name = "fraud_score", precision = 5, scale = 2)
    private BigDecimal fraudScore;
    
    @Column(name = "risk_score", precision = 5, scale = 2)
    private BigDecimal riskScore;
    
    @Column(name = "applied_rules", columnDefinition = "TEXT")
    private String appliedRules;
    
    @Column(name = "failed_rules", columnDefinition = "TEXT")
    private String failedRules;
    
    @Column(name = "validation_metadata", columnDefinition = "TEXT")
    private String validationMetadata;
    
    @Column(name = "validated_at", nullable = false)
    private Instant validatedAt;
    
    @Column(name = "correlation_id", length = 255)
    private String correlationId;
    
    @Column(name = "created_by", length = 255)
    private String createdBy;
    
    @Column(name = "reason", length = 1000)
    private String reason;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
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