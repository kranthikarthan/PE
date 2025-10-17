package com.payments.payshapadapter.domain;

import com.payments.payshapadapter.exception.InvalidPayShapSettlementRecordException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** PayShap Settlement Record Entity */
@Entity
@Table(name = "payshap_settlement_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayShapSettlementRecord {

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "payshap_adapter_id", nullable = false)
  private String payshapAdapterId;

  @Column(name = "settlement_id", nullable = false)
  private String settlementId;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "currency", nullable = false)
  private String currency;

  @Column(name = "settlement_type", nullable = false)
  private String settlementType;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "settlement_date")
  private LocalDate settlementDate;

  @Column(name = "processed_at")
  private Instant processedAt;

  @Column(name = "created_at")
  private Instant createdAt;

  public static PayShapSettlementRecord create(
      String payshapAdapterId,
      String settlementId,
      String transactionId,
      BigDecimal amount,
      String currency,
      String settlementType) {
    if (payshapAdapterId == null || payshapAdapterId.trim().isEmpty()) {
      throw new InvalidPayShapSettlementRecordException(
          "PayShap adapter ID cannot be null or empty");
    }
    if (settlementId == null || settlementId.trim().isEmpty()) {
      throw new InvalidPayShapSettlementRecordException("Settlement ID cannot be null or empty");
    }
    if (transactionId == null || transactionId.trim().isEmpty()) {
      throw new InvalidPayShapSettlementRecordException("Transaction ID cannot be null or empty");
    }
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidPayShapSettlementRecordException("Amount must be greater than zero");
    }

    return PayShapSettlementRecord.builder()
        .id(java.util.UUID.randomUUID().toString())
        .payshapAdapterId(payshapAdapterId)
        .settlementId(settlementId)
        .transactionId(transactionId)
        .amount(amount)
        .currency(currency != null ? currency : "ZAR")
        .settlementType(settlementType != null ? settlementType : "INSTANT")
        .status("PENDING")
        .createdAt(Instant.now())
        .build();
  }
}
