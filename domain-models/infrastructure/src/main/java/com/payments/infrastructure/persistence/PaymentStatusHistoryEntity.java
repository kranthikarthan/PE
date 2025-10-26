package com.payments.infrastructure.persistence;

import com.payments.domain.valueobjects.PaymentStatus;
import com.payments.domain.valueobjects.StatusChange;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/** JPA entity for Payment Status History */
@Entity
@Table(
    name = "payment_status_history",
    indexes = {
      @Index(name = "idx_status_history_payment", columnList = "payment_id"),
      @Index(name = "idx_status_history_timestamp", columnList = "timestamp")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "payment_id", length = 36, nullable = false)
  private String paymentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "from_status")
  private PaymentStatus fromStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "to_status", nullable = false)
  private PaymentStatus toStatus;

  @Column(name = "reason", length = 500, nullable = false)
  private String reason;

  @CreationTimestamp
  @Column(name = "timestamp", nullable = false)
  private Instant timestamp;

  @Column(name = "changed_by", length = 100)
  private String changedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", insertable = false, updatable = false)
  private PaymentEntity payment;

  /** Convert from domain value object to JPA entity */
  public static PaymentStatusHistoryEntity fromDomain(String paymentId, StatusChange statusChange) {
    return PaymentStatusHistoryEntity.builder()
        .paymentId(paymentId)
        .fromStatus(statusChange.getFromStatus())
        .toStatus(statusChange.getToStatus())
        .reason(statusChange.getReason())
        .timestamp(statusChange.getChangedAt())
        .changedBy(statusChange.getChangedBy())
        .build();
  }

  /** Convert from JPA entity to domain value object */
  public StatusChange toDomain() {
    return StatusChange.builder()
        .fromStatus(this.fromStatus)
        .toStatus(this.toStatus)
        .reason(this.reason)
        .timestamp(this.timestamp)
        .changedBy(this.changedBy)
        .build();
  }
}
