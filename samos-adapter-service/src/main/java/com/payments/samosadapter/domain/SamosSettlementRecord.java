package com.payments.samosadapter.domain;

import com.payments.domain.shared.TenantContext;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.*;

/**
 * SAMOS Settlement Record Entity
 *
 * <p>Represents settlement records for high-value RTGS payments processed through SAMOS. Tracks
 * nostro/vostro account movements and settlement confirmations.
 */
@Entity
@Table(name = "samos_settlement_records")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class SamosSettlementRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Embedded
  @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id"))
  private TenantContext tenantContext;

  @Column(name = "settlement_id", nullable = false, unique = true)
  private String settlementId;

  @Column(name = "payment_id", nullable = false)
  private String paymentId;

  @Column(name = "nostro_account", nullable = false)
  private String nostroAccount;

  @Column(name = "vostro_account", nullable = false)
  private String vostroAccount;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency = "ZAR";

  @Column(name = "settlement_date", nullable = false)
  private LocalDate settlementDate;

  @Column(name = "settlement_time", nullable = false)
  private LocalTime settlementTime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SettlementStatus status = SettlementStatus.PENDING;

  @Column(name = "reference")
  private String reference;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "samos_adapter_id")
  private SamosAdapter samosAdapter;

  public static SamosSettlementRecord create(
      TenantContext tenantContext,
      String settlementId,
      String paymentId,
      String nostroAccount,
      String vostroAccount,
      BigDecimal amount,
      String currency,
      LocalDate settlementDate,
      LocalTime settlementTime,
      String reference) {

    if (settlementId == null || settlementId.isBlank()) {
      throw new IllegalArgumentException("Settlement ID cannot be null or blank");
    }
    if (paymentId == null || paymentId.isBlank()) {
      throw new IllegalArgumentException("Payment ID cannot be null or blank");
    }
    if (nostroAccount == null || nostroAccount.isBlank()) {
      throw new IllegalArgumentException("Nostro account cannot be null or blank");
    }
    if (vostroAccount == null || vostroAccount.isBlank()) {
      throw new IllegalArgumentException("Vostro account cannot be null or blank");
    }
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }

    SamosSettlementRecord record = new SamosSettlementRecord();
    record.tenantContext = tenantContext;
    record.settlementId = settlementId;
    record.paymentId = paymentId;
    record.nostroAccount = nostroAccount;
    record.vostroAccount = vostroAccount;
    record.amount = amount;
    record.currency = currency != null ? currency : "ZAR";
    record.settlementDate = settlementDate;
    record.settlementTime = settlementTime;
    record.reference = reference;
    record.createdAt = Instant.now();
    record.updatedAt = Instant.now();

    return record;
  }

  public void markAsSettled() {
    if (this.status == SettlementStatus.SETTLED) {
      throw new IllegalStateException("Settlement already marked as settled");
    }
    this.status = SettlementStatus.SETTLED;
    this.updatedAt = Instant.now();
  }

  public void markAsFailed(String reason) {
    if (this.status == SettlementStatus.FAILED) {
      throw new IllegalStateException("Settlement already marked as failed");
    }
    this.status = SettlementStatus.FAILED;
    this.reference = reason; // Store failure reason in reference field
    this.updatedAt = Instant.now();
  }

  public boolean isSettled() {
    return this.status == SettlementStatus.SETTLED;
  }

  public boolean isFailed() {
    return this.status == SettlementStatus.FAILED;
  }

  public boolean isPending() {
    return this.status == SettlementStatus.PENDING;
  }

  public boolean isZarCurrency() {
    return "ZAR".equals(this.currency);
  }

  public enum SettlementStatus {
    PENDING,
    SETTLED,
    FAILED
  }
}
