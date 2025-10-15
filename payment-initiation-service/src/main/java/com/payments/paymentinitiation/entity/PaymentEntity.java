package com.payments.paymentinitiation.entity;

import com.payments.domain.payment.PaymentId;
import com.payments.domain.payment.PaymentStatus;
import com.payments.domain.payment.PaymentType;
import com.payments.domain.payment.Priority;
import com.payments.domain.shared.AccountNumber;
import com.payments.domain.shared.Money;
import com.payments.domain.shared.TenantContext;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for Payment aggregate
 * 
 * Maps to the payments table with proper JPA annotations
 * and multi-tenancy support
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_payments_business_unit_id", columnList = "business_unit_id"),
    @Index(name = "idx_payments_status", columnList = "status"),
    @Index(name = "idx_payments_initiated_at", columnList = "initiated_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {

    @EmbeddedId
    private PaymentId paymentId;

    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "accountNumber", column = @Column(name = "source_account", nullable = false, length = 11))
    })
    private AccountNumber sourceAccount;

    @Embedded
    @AttributeOverride(name = "accountNumber", column = @Column(name = "destination_account", nullable = false, length = 11))
    private AccountNumber destinationAccount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false, precision = 19, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "currency", nullable = false, length = 3))
    })
    private Money amount;

    @Column(name = "reference", nullable = false, length = 35)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id", nullable = false, length = 20)),
        @AttributeOverride(name = "businessUnitId", column = @Column(name = "business_unit_id", nullable = false, length = 30))
    })
    private TenantContext tenantContext;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "initiated_by", nullable = false)
    private String initiatedBy;

    @CreationTimestamp
    @Column(name = "initiated_at", nullable = false)
    private Instant initiatedAt;

    @Column(name = "validated_at")
    private Instant validatedAt;

    @Column(name = "submitted_to_clearing_at")
    private Instant submittedToClearingAt;

    @Column(name = "cleared_at")
    private Instant clearedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // Status history for audit trail
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PaymentStatusHistoryEntity> statusHistory = new ArrayList<>();

    /**
     * Add status change to history
     */
    public void addStatusChange(PaymentStatus newStatus, String reason) {
        PaymentStatusHistoryEntity historyEntry = PaymentStatusHistoryEntity.builder()
                .payment(this)
                .fromStatus(this.status)
                .toStatus(newStatus)
                .reason(reason)
                .changedAt(Instant.now())
                .build();
        
        this.statusHistory.add(historyEntry);
        this.status = newStatus;
    }

    /**
     * Update status with history tracking
     */
    public void updateStatus(PaymentStatus newStatus, String reason) {
        if (this.status != newStatus) {
            addStatusChange(newStatus, reason);
        }
    }
}
