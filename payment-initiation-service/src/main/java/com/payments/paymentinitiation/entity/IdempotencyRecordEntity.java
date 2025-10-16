package com.payments.paymentinitiation.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/**
 * JPA Entity for Idempotency Records
 *
 * <p>Tracks idempotency keys to prevent duplicate payment processing
 */
@Entity
@Table(
    name = "idempotency_records",
    indexes = {
      @Index(name = "idx_idempotency_key_tenant", columnList = "idempotency_key, tenant_id"),
      @Index(name = "idx_idempotency_created_at", columnList = "created_at")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyRecordEntity {

  @Id
  @Column(name = "id", length = 36)
  private String id;

  @Column(name = "idempotency_key", nullable = false, length = 255)
  private String idempotencyKey;

  @Column(name = "tenant_id", nullable = false, length = 20)
  private String tenantId;

  @Column(name = "payment_id", nullable = false, length = 36)
  private String paymentId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
