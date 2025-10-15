package com.payments.validation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity for Validation Audit Trail
 * 
 * Maps to validation_audit_trail table with:
 * - Validation process audit trail
 * - Action tracking and details
 * - Performance metrics
 */
@Entity
@Table(name = "validation_audit_trail", indexes = {
    @Index(name = "idx_validation_audit_trail_payment_id", columnList = "payment_id"),
    @Index(name = "idx_validation_audit_trail_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_validation_audit_trail_performed_at", columnList = "performed_at"),
    @Index(name = "idx_validation_audit_trail_correlation_id", columnList = "correlation_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationAuditTrailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validation_result_id")
    private ValidationResultEntity validationResult;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "business_unit_id", nullable = false)
    private String businessUnitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private Action action;

    @Column(name = "details", columnDefinition = "jsonb")
    private String details;

    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    @Column(name = "performed_by")
    @Builder.Default
    private String performedBy = "validation-service";

    @Column(name = "correlation_id")
    private String correlationId;

    /**
     * Audit Action Enum
     */
    public enum Action {
        VALIDATION_STARTED,
        VALIDATION_COMPLETED,
        RULE_EXECUTED,
        RULE_FAILED,
        FRAUD_DETECTED,
        RISK_ASSESSED,
        VALIDATION_ERROR,
        VALIDATION_TIMEOUT
    }
}
