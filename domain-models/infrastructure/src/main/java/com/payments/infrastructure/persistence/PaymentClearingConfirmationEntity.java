package com.payments.infrastructure.persistence;

import com.payments.domain.valueobjects.ClearingConfirmation;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/** JPA entity for Payment Clearing Confirmation */
@Entity
@Table(
    name = "payment_clearing_confirmations",
    indexes = {
      @Index(name = "idx_clearing_confirmation_payment", columnList = "payment_id"),
      @Index(name = "idx_clearing_confirmation_system", columnList = "clearing_system_id"),
      @Index(name = "idx_clearing_confirmation_timestamp", columnList = "confirmed_at")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentClearingConfirmationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "payment_id", length = 36, nullable = false)
  private String paymentId;

  @Column(name = "clearing_system_id", length = 50, nullable = false)
  private String clearingSystemId;

  @Column(name = "confirmation_id", length = 100, nullable = false)
  private String confirmationId;

  @Column(name = "status", length = 50, nullable = false)
  private String status;

  @Column(name = "message", length = 1000)
  private String message;

  @CreationTimestamp
  @Column(name = "confirmed_at", nullable = false)
  private Instant confirmedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", insertable = false, updatable = false)
  private PaymentEntity payment;

  /** Convert from domain value object to JPA entity */
  public static PaymentClearingConfirmationEntity fromDomain(
      String paymentId, ClearingConfirmation confirmation) {
    return PaymentClearingConfirmationEntity.builder()
        .paymentId(paymentId)
        .clearingSystemId(confirmation.getClearingSystemId())
        .confirmationId(confirmation.getConfirmationId())
        .status(confirmation.getStatus())
        .message(confirmation.getMessage())
        .confirmedAt(confirmation.getConfirmedAt())
        .build();
  }

  /** Convert from JPA entity to domain value object */
  public ClearingConfirmation toDomain() {
    return new ClearingConfirmation(
        this.clearingSystemId, this.confirmationId, this.status, this.message, this.confirmedAt);
  }
}
