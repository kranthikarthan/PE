package com.payments.domain.payment;

import com.payments.domain.shared.*;
import com.payments.domain.validation.ValidationResult;
import lombok.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Payment Aggregate Root
 * 
 * Consistency Boundary: Payment + PaymentDetails + StatusHistory
 * Business Rules Enforced:
 * - Payment can only be initiated once
 * - Validated payments can be cleared
 * - Failed payments cannot be cleared
 * - Status transitions must be valid
 */
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
public class Payment {
    
    @EmbeddedId
    private PaymentId id;
    
    @Embedded
    private TenantContext tenantContext;
    
    @Embedded
    private Money amount;
    
    @Embedded
    private AccountNumber sourceAccount;
    
    @Embedded
    private AccountNumber destinationAccount;
    
    @Embedded
    private PaymentReference reference;
    
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    @Enumerated(EnumType.STRING)
    private Priority priority;
    
    private String initiatedBy;
    
    private Instant initiatedAt;
    
    private Instant completedAt;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "payment_id")
    private List<StatusChange> statusHistory = new ArrayList<>();
    
    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    // ─────────────────────────────────────────────────────────
    // FACTORY METHOD (Create new payment)
    // ─────────────────────────────────────────────────────────
    
    public static Payment initiate(
        PaymentId id,
        TenantContext tenantContext,
        Money amount,
        AccountNumber sourceAccount,
        AccountNumber destinationAccount,
        PaymentReference reference,
        PaymentType paymentType,
        Priority priority,
        String initiatedBy
    ) {
        // Business validation
        if (amount.isNegativeOrZero()) {
            throw new InvalidPaymentException("Amount must be positive");
        }
        
        if (sourceAccount.equals(destinationAccount)) {
            throw new InvalidPaymentException("Source and destination accounts must be different");
        }
        
        // Create payment
        Payment payment = new Payment();
        payment.id = id;
        payment.tenantContext = tenantContext;
        payment.amount = amount;
        payment.sourceAccount = sourceAccount;
        payment.destinationAccount = destinationAccount;
        payment.reference = reference;
        payment.paymentType = paymentType;
        payment.priority = priority;
        payment.initiatedBy = initiatedBy;
        payment.status = PaymentStatus.INITIATED;
        payment.initiatedAt = Instant.now();
        
        // Record status change
        payment.addStatusChange(null, PaymentStatus.INITIATED, "Payment initiated");
        
        // Domain event
        payment.registerEvent(new PaymentInitiatedEvent(
            payment.id,
            payment.tenantContext,
            payment.amount,
            payment.sourceAccount,
            payment.destinationAccount,
            payment.paymentType,
            payment.initiatedAt
        ));
        
        return payment;
    }
    
    // ─────────────────────────────────────────────────────────
    // BUSINESS METHODS (Behavior, not getters/setters!)
    // ─────────────────────────────────────────────────────────
    
    /**
     * Validate the payment
     * Precondition: Payment must be INITIATED
     */
    public void validate(ValidationResult validationResult) {
        // Guard: Can only validate INITIATED payments
        if (this.status != PaymentStatus.INITIATED) {
            throw new InvalidStateTransitionException(
                "Can only validate INITIATED payments. Current status: " + this.status
            );
        }
        
        if (validationResult.isValid()) {
            this.status = PaymentStatus.VALIDATED;
            addStatusChange(PaymentStatus.INITIATED, PaymentStatus.VALIDATED, 
                "Payment validated successfully");
            
            registerEvent(new PaymentValidatedEvent(
                this.id,
                this.tenantContext,
                validationResult
            ));
        } else {
            fail(validationResult.getReason());
        }
    }
    
    /**
     * Submit payment to clearing
     * Precondition: Payment must be VALIDATED
     */
    public void submitToClearing(ClearingSystemReference clearingRef) {
        // Guard: Can only clear VALIDATED payments
        if (this.status != PaymentStatus.VALIDATED) {
            throw new InvalidStateTransitionException(
                "Can only clear VALIDATED payments. Current status: " + this.status
            );
        }
        
        this.status = PaymentStatus.CLEARING;
        addStatusChange(PaymentStatus.VALIDATED, PaymentStatus.CLEARING, 
            "Submitted to clearing: " + clearingRef.getValue());
        
        registerEvent(new PaymentSubmittedToClearingEvent(
            this.id,
            this.tenantContext,
            clearingRef
        ));
    }
    
    /**
     * Mark payment as cleared (clearing confirmed)
     */
    public void markCleared(ClearingConfirmation confirmation) {
        // Guard: Payment must be in CLEARING status
        if (this.status != PaymentStatus.CLEARING) {
            throw new InvalidStateTransitionException(
                "Can only mark CLEARING payments as cleared. Current status: " + this.status
            );
        }
        
        this.status = PaymentStatus.CLEARED;
        addStatusChange(PaymentStatus.CLEARING, PaymentStatus.CLEARED, 
            "Clearing confirmed: " + confirmation.getConfirmationNumber());
        
        registerEvent(new PaymentClearedEvent(
            this.id,
            this.tenantContext,
            confirmation
        ));
    }
    
    /**
     * Complete the payment (final status)
     */
    public void complete() {
        // Guard: Payment must be CLEARED
        if (this.status != PaymentStatus.CLEARED) {
            throw new InvalidStateTransitionException(
                "Can only complete CLEARED payments. Current status: " + this.status
            );
        }
        
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = Instant.now();
        addStatusChange(PaymentStatus.CLEARED, PaymentStatus.COMPLETED, 
            "Payment completed successfully");
        
        registerEvent(new PaymentCompletedEvent(
            this.id,
            this.tenantContext,
            this.amount,
            this.completedAt
        ));
    }
    
    /**
     * Fail the payment
     * Can be called from any non-final state
     */
    public void fail(String reason) {
        // Guard: Cannot fail already completed/failed payments
        if (this.status == PaymentStatus.COMPLETED || this.status == PaymentStatus.FAILED) {
            throw new InvalidStateTransitionException(
                "Cannot fail payment in " + this.status + " status"
            );
        }
        
        PaymentStatus previousStatus = this.status;
        this.status = PaymentStatus.FAILED;
        addStatusChange(previousStatus, PaymentStatus.FAILED, reason);
        
        registerEvent(new PaymentFailedEvent(
            this.id,
            this.tenantContext,
            reason,
            previousStatus
        ));
    }
    
    // ─────────────────────────────────────────────────────────
    // QUERY METHODS (Read-only, expose state)
    // ─────────────────────────────────────────────────────────
    
    public boolean isInProgress() {
        return this.status != PaymentStatus.COMPLETED && 
               this.status != PaymentStatus.FAILED;
    }
    
    public boolean canBeCleared() {
        return this.status == PaymentStatus.VALIDATED;
    }
    
    public PaymentId getId() {
        return id;
    }
    
    public TenantContext getTenantContext() {
        return tenantContext;
    }
    
    public Money getAmount() {
        return amount; // Money is immutable, safe to return
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    // ─────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────
    
    private void addStatusChange(PaymentStatus from, PaymentStatus to, String reason) {
        statusHistory.add(new StatusChange(from, to, reason, Instant.now()));
    }
    
    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
}

/**
 * Status Change (Entity within Payment Aggregate)
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class StatusChange {
    @Enumerated(EnumType.STRING)
    private PaymentStatus fromStatus;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus toStatus;
    
    private String reason;
    
    private Instant changedAt;
}
