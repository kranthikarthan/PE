package com.payments.infrastructure.persistence;

import com.payments.domain.entities.Payment;
import com.payments.domain.shared.*;
import com.payments.domain.valueobjects.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA entity for Payment - contains infrastructure concerns This is the persistence representation
 * of the domain Payment entity
 */
@Entity
@Table(
    name = "payments",
    indexes = {
      @Index(name = "idx_payment_tenant", columnList = "tenant_id"),
      @Index(name = "idx_payment_status", columnList = "status"),
      @Index(name = "idx_payment_created", columnList = "created_at")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {

  @Id
  @Column(name = "payment_id", length = 36)
  private String paymentId;

  @Column(name = "tenant_id", length = 36, nullable = false)
  private String tenantId;

  @Column(name = "amount", precision = 19, scale = 2, nullable = false)
  private BigDecimal amount;

  @Column(name = "currency", length = 3, nullable = false)
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_type", nullable = false)
  private PaymentType paymentType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private PaymentStatus status;

  @Column(name = "reference", length = 100)
  private String reference;

  @Column(name = "clearing_reference", length = 100)
  private String clearingReference;

  @Enumerated(EnumType.STRING)
  @Column(name = "priority")
  private Priority priority;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "description", length = 200)
  private String description;

  @Column(name = "beneficiary_name", length = 100)
  private String beneficiaryName;

  @Column(name = "beneficiary_account", length = 20, nullable = false)
  private String beneficiaryAccount;

  @Column(name = "beneficiary_bank_code", length = 6, nullable = false)
  private String beneficiaryBankCode;

  @Column(name = "remitter_name", length = 100)
  private String remitterName;

  @Column(name = "remitter_account", length = 20)
  private String remitterAccount;

  @Column(name = "remitter_bank_code", length = 6)
  private String remitterBankCode;

  @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<PaymentStatusHistoryEntity> statusHistory = new ArrayList<>();

  @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<PaymentClearingConfirmationEntity> clearingConfirmations = new ArrayList<>();

  /** Convert from domain entity to JPA entity */
  public static PaymentEntity fromDomain(Payment payment) {
    return PaymentEntity.builder()
        .paymentId(payment.getPaymentId().getValue())
        .tenantId(payment.getTenantId().getValue())
        .amount(payment.getAmount().getAmount())
        .currency(payment.getAmount().getCurrency().getCurrencyCode())
        .paymentType(payment.getPaymentType())
        .status(payment.getStatus())
        .reference(payment.getReference() != null ? payment.getReference().getValue() : null)
        .clearingReference(
            payment.getClearingReference() != null
                ? payment.getClearingReference().getValue()
                : null)
        .priority(payment.getPriority())
        .createdAt(payment.getCreatedAt())
        .updatedAt(payment.getUpdatedAt())
        .description(payment.getDescription())
        .beneficiaryName(payment.getBeneficiaryName())
        .beneficiaryAccount(payment.getBeneficiaryAccount())
        .beneficiaryBankCode(payment.getBeneficiaryBankCode())
        .remitterName(payment.getRemitterName())
        .remitterAccount(payment.getRemitterAccount())
        .remitterBankCode(payment.getRemitterBankCode())
        .build();
  }

  /** Convert from JPA entity to domain entity */
  public Payment toDomain() {
    Payment payment = new Payment();
    payment.setPaymentId(PaymentId.of(this.paymentId));
    payment.setTenantId(TenantId.of(this.tenantId));
    payment.setAmount(Money.of(this.amount, java.util.Currency.getInstance(this.currency)));
    payment.setPaymentType(this.paymentType);
    payment.setStatus(this.status);
    payment.setReference(this.reference != null ? PaymentReference.of(this.reference) : null);
    payment.setClearingReference(
        this.clearingReference != null ? ClearingSystemReference.of(this.clearingReference) : null);
    payment.setPriority(this.priority);
    payment.setCreatedAt(this.createdAt);
    payment.setUpdatedAt(this.updatedAt);
    payment.setDescription(this.description);
    payment.setBeneficiaryName(this.beneficiaryName);
    payment.setBeneficiaryAccount(this.beneficiaryAccount);
    payment.setBeneficiaryBankCode(this.beneficiaryBankCode);
    payment.setRemitterName(this.remitterName);
    payment.setRemitterAccount(this.remitterAccount);
    payment.setRemitterBankCode(this.remitterBankCode);

    // Convert status history
    if (this.statusHistory != null) {
      payment.setStatusHistory(
          this.statusHistory.stream().map(PaymentStatusHistoryEntity::toDomain).toList());
    }

    // Convert clearing confirmations
    if (this.clearingConfirmations != null) {
      payment.setClearingConfirmations(
          this.clearingConfirmations.stream()
              .map(PaymentClearingConfirmationEntity::toDomain)
              .toList());
    }

    return payment;
  }
}
