package com.payments.operationsmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Operations Audit Log Entity
 * 
 * Tracks all operations management actions for audit and compliance.
 * Records who performed what action on which entity and when.
 */
@Entity
@Table(name = "operations_audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationsAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "audit_id", nullable = false, unique = true)
    private String auditId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "action_details", columnDefinition = "jsonb")
    private String actionDetails;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "performed_at")
    private Instant performedAt;

    public enum ActionType {
        PAYMENT_RETRY,
        SAGA_RESUME,
        SAGA_COMPENSATE,
        CIRCUIT_BREAKER_OPEN,
        CIRCUIT_BREAKER_CLOSE,
        FEATURE_FLAG_TOGGLE,
        FEATURE_FLAG_ROLLOUT,
        POD_RESTART,
        SERVICE_SCALE,
        SERVICE_RESTART,
        ALERT_ACKNOWLEDGE,
        ALERT_RESOLVE
    }

    public enum EntityType {
        PAYMENT,
        SAGA,
        CIRCUIT_BREAKER,
        FEATURE_FLAG,
        POD,
        SERVICE,
        ALERT
    }
}
