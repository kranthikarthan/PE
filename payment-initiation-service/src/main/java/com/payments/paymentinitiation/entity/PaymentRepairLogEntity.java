package com.payments.paymentinitiation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Payment Repair Log Entity
 * 
 * Tracks all repair actions performed on payments for audit and compliance.
 * Records who performed what action on which payment and when.
 */
@Entity
@Table(name = "payment_repair_log", indexes = {
    @Index(name = "idx_payment_repair_payment_id", columnList = "payment_id"),
    @Index(name = "idx_payment_repair_action", columnList = "action"),
    @Index(name = "idx_payment_repair_timestamp", columnList = "timestamp"),
    @Index(name = "idx_payment_repair_performed_by", columnList = "performed_by")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRepairLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repair_id", nullable = false, unique = true)
    private String repairId;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private RepairAction action;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "result", nullable = false)
    private String result;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    public enum RepairAction {
        RETRY, CANCEL, FORCE_COMPLETE, FORCE_FAIL, STATUS_UPDATE
    }
}
