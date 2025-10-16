package com.payments.domain.payment;

import com.payments.domain.shared.*;
import com.payments.domain.validation.ValidationResult;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.*;

/**
 * Payment Aggregate Root
 *
 * <p>Consistency Boundary: Payment + PaymentDetails + StatusHistory Business Rules Enforced: -
 * Payment can only be initiated once - Validated payments can be cleared - Failed payments cannot
 * be cleared - Status transitions must be valid
 */
@Entity
@Table(
    name = "payments",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_idempotency_tenant",
          columnNames = {"tenant_id", "idempotency_key"})
    })
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA
@AllArgsConstructor
public class Payment {

  @EmbeddedId
  @AttributeOverride(name = "value", column = @Column(name = "payment_id"))
  private PaymentId id;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id")),
    @AttributeOverride(name = "businessUnitId", column = @Column(name = "business_unit_id"))
  })
  private TenantContext tenantContext;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "amount", column = @Column(name = "amount")),
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
  })
  private Money amount;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "source_account"))
  private AccountNumber sourceAccount;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "destination_account"))
  private AccountNumber destinationAccount;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "reference"))
  private PaymentReference reference;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_type")
  private PaymentType paymentType;

  @Enumerated(EnumType.STRING)
  private PaymentStatus status;

  @Enumerated(EnumType.STRING)
  private Priority priority;

  @Column(name = "initiated_by")
  private String initiatedBy;

  @Column(name = "created_at")
  private Instant initiatedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "validated_at")
  private Instant validatedAt;

  @Column(name = "submitted_to_clearing_at")
  private Instant submittedToClearingAt;

  @Column(name = "cleared_at")
  private Instant clearedAt;

  @Column(name = "failed_at")
  private Instant failedAt;

  @Column(name = "failure_reason")
  private String failureReason;

  @Column(name = "idempotency_key", nullable = false)
  private String idempotencyKey;

  @Transient private List<StatusChange> statusHistory = new ArrayList<>();

  @Transient private List<DomainEvent> domainEvents = new ArrayList<>();

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
      String initiatedBy,
      String idempotencyKey) {
    // Business validation
    if (amount.isNegativeOrZero()) {
      throw new InvalidPaymentException("Amount must be positive");
    }

    if (sourceAccount.equals(destinationAccount)) {
      throw new InvalidPaymentException("Source and destination accounts must be different");
    }

    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      throw new InvalidPaymentException("Idempotency key cannot be null or blank");
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
    payment.idempotencyKey = idempotencyKey;
    payment.status = PaymentStatus.INITIATED;
    payment.initiatedAt = Instant.now();

    // Record status change
    payment.addStatusChange(null, PaymentStatus.INITIATED, "Payment initiated");

    // Domain event
    payment.registerEvent(
        new PaymentInitiatedEvent(
            payment.id,
            payment.tenantContext,
            payment.amount,
            payment.sourceAccount,
            payment.destinationAccount,
            payment.paymentType,
            payment.initiatedAt));

    return payment;
  }

  // ─────────────────────────────────────────────────────────
  // BUSINESS METHODS (Behavior, not getters/setters!)
  // ─────────────────────────────────────────────────────────

  /** Validate the payment Precondition: Payment must be INITIATED */
  public void validate(ValidationResult validationResult) {
    // Guard: Can only validate INITIATED payments
    if (this.status != PaymentStatus.INITIATED) {
      throw new InvalidStateTransitionException(
          "Can only validate INITIATED payments. Current status: " + this.status);
    }

    if (validationResult.isValid()) {
      this.status = PaymentStatus.VALIDATED;
      addStatusChange(
          PaymentStatus.INITIATED, PaymentStatus.VALIDATED, "Payment validated successfully");

      registerEvent(new PaymentValidatedEvent(this.id, this.tenantContext, validationResult));
    } else {
      fail(validationResult.getReason());
    }
  }

  /** Submit payment to clearing Precondition: Payment must be VALIDATED */
  public void submitToClearing(ClearingSystemReference clearingRef) {
    // Guard: Can only clear VALIDATED payments
    if (this.status != PaymentStatus.VALIDATED) {
      throw new InvalidStateTransitionException(
          "Can only clear VALIDATED payments. Current status: " + this.status);
    }

    this.status = PaymentStatus.CLEARING;
    addStatusChange(
        PaymentStatus.VALIDATED,
        PaymentStatus.CLEARING,
        "Submitted to clearing: " + clearingRef.getValue());

    registerEvent(new PaymentSubmittedToClearingEvent(this.id, this.tenantContext, clearingRef));
  }

  /** Mark payment as cleared (clearing confirmed) */
  public void markCleared(ClearingConfirmation confirmation) {
    // Guard: Payment must be in CLEARING status
    if (this.status != PaymentStatus.CLEARING) {
      throw new InvalidStateTransitionException(
          "Can only mark CLEARING payments as cleared. Current status: " + this.status);
    }

    this.status = PaymentStatus.CLEARED;
    addStatusChange(
        PaymentStatus.CLEARING,
        PaymentStatus.CLEARED,
        "Clearing confirmed: " + confirmation.getConfirmationNumber());

    registerEvent(new PaymentClearedEvent(this.id, this.tenantContext, confirmation));
  }

  /** Complete the payment (final status) */
  public void complete() {
    // Guard: Payment must be CLEARED
    if (this.status != PaymentStatus.CLEARED) {
      throw new InvalidStateTransitionException(
          "Can only complete CLEARED payments. Current status: " + this.status);
    }

    this.status = PaymentStatus.COMPLETED;
    this.completedAt = Instant.now();
    addStatusChange(
        PaymentStatus.CLEARED, PaymentStatus.COMPLETED, "Payment completed successfully");

    registerEvent(
        new PaymentCompletedEvent(this.id, this.tenantContext, this.amount, this.completedAt));
  }

  /** Fail the payment Can be called from any non-final state */
  public void fail(String reason) {
    // Guard: Cannot fail already completed/failed payments
    if (this.status == PaymentStatus.COMPLETED || this.status == PaymentStatus.FAILED) {
      throw new InvalidStateTransitionException(
          "Cannot fail payment in " + this.status + " status");
    }

    PaymentStatus previousStatus = this.status;
    this.status = PaymentStatus.FAILED;
    addStatusChange(previousStatus, PaymentStatus.FAILED, reason);

    registerEvent(new PaymentFailedEvent(this.id, this.tenantContext, reason, previousStatus));
  }

  // ─────────────────────────────────────────────────────────
  // QUERY METHODS (Read-only, expose state)
  // ─────────────────────────────────────────────────────────

  public boolean isInProgress() {
    return this.status != PaymentStatus.COMPLETED && this.status != PaymentStatus.FAILED;
  }

  public boolean canBeCleared() {
    return this.status == PaymentStatus.VALIDATED;
  }

  public List<DomainEvent> getDomainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  public void clearDomainEvents() {
    this.domainEvents.clear();
  }

  public void updateStatus(PaymentStatus newStatus, String reason) {
    PaymentStatus oldStatus = this.status;
    this.status = newStatus;
    addStatusChange(oldStatus, newStatus, reason);
  }

  // ─────────────────────────────────────────────────────────
  // PRIVATE HELPERS
  // ─────────────────────────────────────────────────────────

  private void addStatusChange(PaymentStatus from, PaymentStatus to, String reason) {
    statusHistory.add(new StatusChange(from, to, reason, "system", Instant.now()));
  }

  private void registerEvent(DomainEvent event) {
    this.domainEvents.add(event);
  }
}

