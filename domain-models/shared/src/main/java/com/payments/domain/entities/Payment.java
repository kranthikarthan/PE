package com.payments.domain.entities;

import com.payments.domain.shared.*;
import com.payments.domain.valueobjects.*;
import com.payments.domain.events.*;
import com.payments.domain.exceptions.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Pure domain entity for Payment - no infrastructure dependencies
 * Contains only business logic and domain rules
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Payment extends AggregateRoot<PaymentId> {

    private PaymentId paymentId;
    private TenantId tenantId;
    private Money amount;
    private PaymentType paymentType;
    private PaymentStatus status;
    private PaymentReference reference;
    private ClearingSystemReference clearingReference;
    private Priority priority;
    private Instant createdAt;
    private Instant updatedAt;
    private String description;
    private String beneficiaryName;
    private String beneficiaryAccount;
    private String beneficiaryBankCode;
    private String remitterName;
    private String remitterAccount;
    private String remitterBankCode;
    private List<StatusChange> statusHistory;
    private List<ClearingConfirmation> clearingConfirmations;

    public Payment() {
        this.paymentId = PaymentId.of(UUID.randomUUID().toString());
        this.status = PaymentStatus.PENDING;
        this.statusHistory = new ArrayList<>();
        this.clearingConfirmations = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Business method to initiate a payment
     */
    public void initiate() {
        if (this.status != PaymentStatus.PENDING) {
            throw new InvalidStateTransitionException(
                "Cannot initiate payment in status: " + this.status
            );
        }

        validatePayment();
        this.status = PaymentStatus.INITIATED;
        this.updatedAt = Instant.now();
        
        addStatusChange(PaymentStatus.INITIATED, "Payment initiated");
        addDomainEvent(new PaymentInitiatedEvent(this.paymentId, this.tenantId, this.amount));
    }

    /**
     * Business method to validate a payment
     */
    public void validate() {
        if (this.status != PaymentStatus.INITIATED) {
            throw new InvalidStateTransitionException(
                "Cannot validate payment in status: " + this.status
            );
        }

        validatePayment();
        this.status = PaymentStatus.VALIDATED;
        this.updatedAt = Instant.now();
        
        addStatusChange(PaymentStatus.VALIDATED, "Payment validated");
        addDomainEvent(new PaymentValidatedEvent(this.paymentId, this.tenantId, this.amount));
    }

    /**
     * Business method to submit payment to clearing
     */
    public void submitToClearing(ClearingSystemReference clearingReference) {
        if (this.status != PaymentStatus.VALIDATED) {
            throw new InvalidStateTransitionException(
                "Cannot submit to clearing payment in status: " + this.status
            );
        }

        this.clearingReference = clearingReference;
        this.status = PaymentStatus.SUBMITTED_TO_CLEARING;
        this.updatedAt = Instant.now();
        
        addStatusChange(PaymentStatus.SUBMITTED_TO_CLEARING, "Submitted to clearing: " + clearingReference.getValue());
        addDomainEvent(new PaymentSubmittedToClearingEvent(this.paymentId, this.tenantId, this.amount, clearingReference));
    }

    /**
     * Business method to mark payment as cleared
     */
    public void clear(ClearingConfirmation confirmation) {
        if (this.status != PaymentStatus.SUBMITTED_TO_CLEARING) {
            throw new InvalidStateTransitionException(
                "Cannot clear payment in status: " + this.status
            );
        }

        this.clearingConfirmations.add(confirmation);
        this.status = PaymentStatus.CLEARED;
        this.updatedAt = Instant.now();
        
        addStatusChange(PaymentStatus.CLEARED, "Payment cleared: " + confirmation.getClearingSystemId());
        addDomainEvent(new PaymentClearedEvent(this.paymentId, this.tenantId, this.amount, confirmation));
    }

    /**
     * Business method to complete a payment
     */
    public void complete() {
        if (this.status != PaymentStatus.CLEARED) {
            throw new InvalidStateTransitionException(
                "Cannot complete payment in status: " + this.status
            );
        }

        this.status = PaymentStatus.COMPLETED;
        this.updatedAt = Instant.now();
        
        addStatusChange(PaymentStatus.COMPLETED, "Payment completed");
        addDomainEvent(new PaymentCompletedEvent(this.paymentId, this.tenantId, this.amount));
    }

    /**
     * Business method to fail a payment
     */
    public void fail(String reason) {
        if (this.status == PaymentStatus.COMPLETED || this.status == PaymentStatus.FAILED) {
            throw new InvalidStateTransitionException(
                "Cannot fail payment in status: " + this.status
            );
        }

        this.status = PaymentStatus.FAILED;
        this.updatedAt = Instant.now();
        
        addStatusChange(PaymentStatus.FAILED, "Payment failed: " + reason);
        addDomainEvent(new PaymentFailedEvent(this.paymentId, this.tenantId, this.amount, reason));
    }

    /**
     * Business validation rules
     */
    private void validatePayment() {
        if (this.amount == null || this.amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException("Payment amount must be greater than zero");
        }

        if (this.tenantId == null) {
            throw new InvalidPaymentException("Payment must have a tenant");
        }

        if (this.beneficiaryAccount == null || this.beneficiaryAccount.trim().isEmpty()) {
            throw new InvalidPaymentException("Payment must have a beneficiary account");
        }

        if (this.beneficiaryBankCode == null || this.beneficiaryBankCode.trim().isEmpty()) {
            throw new InvalidPaymentException("Payment must have a beneficiary bank code");
        }
    }

    /**
     * Add status change to history
     */
    private void addStatusChange(PaymentStatus status, String reason) {
        this.statusHistory.add(StatusChange.builder()
            .status(status)
            .reason(reason)
            .timestamp(Instant.now())
            .build());
    }

    /**
     * Get the current status
     */
    public PaymentStatus getCurrentStatus() {
        return this.status;
    }

    /**
     * Check if payment is in terminal state
     */
    public boolean isTerminal() {
        return this.status == PaymentStatus.COMPLETED || this.status == PaymentStatus.FAILED;
    }

    /**
     * Check if payment can be modified
     */
    public boolean canBeModified() {
        return this.status == PaymentStatus.PENDING || this.status == PaymentStatus.INITIATED;
    }
}
