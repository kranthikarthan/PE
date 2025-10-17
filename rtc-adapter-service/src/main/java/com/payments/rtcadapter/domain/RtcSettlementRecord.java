package com.payments.rtcadapter.domain;

import com.payments.domain.shared.ClearingMessageId;
import com.payments.rtcadapter.exception.InvalidRtcSettlementRecordException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.*;

/**
 * RTC Settlement Record Entity
 *
 * <p>Represents settlement records for RTC processing: - Daily settlement tracking - Transaction
 * count monitoring - Settlement status management - Reference tracking
 */
@Entity
@Table(name = "rtc_settlement_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RtcSettlementRecord {

  @EmbeddedId private ClearingMessageId id;

  @Column(name = "rtc_adapter_id", nullable = false)
  private String rtcAdapterId;

  @Column(name = "settlement_date", nullable = false)
  private LocalDate settlementDate;

  @Column(name = "settlement_reference", nullable = false)
  private String settlementReference;

  @Column(name = "total_transactions")
  private Integer totalTransactions;

  @Column(name = "total_amount")
  private BigDecimal totalAmount;

  @Column(name = "successful_transactions")
  private Integer successfulTransactions;

  @Column(name = "failed_transactions")
  private Integer failedTransactions;

  @Column(name = "settlement_status", nullable = false)
  private String settlementStatus;

  @Column(name = "settlement_time")
  private Instant settlementTime;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rtc_adapter_id", insertable = false, updatable = false)
  private RtcAdapter rtcAdapter;

  /** Create a new RTC settlement record */
  public static RtcSettlementRecord create(
      ClearingMessageId id,
      String rtcAdapterId,
      LocalDate settlementDate,
      String settlementReference) {
    if (id == null) {
      throw new InvalidRtcSettlementRecordException("Settlement record ID cannot be null");
    }
    if (rtcAdapterId == null || rtcAdapterId.trim().isEmpty()) {
      throw new InvalidRtcSettlementRecordException("RTC adapter ID cannot be null or empty");
    }
    if (settlementDate == null) {
      throw new InvalidRtcSettlementRecordException("Settlement date cannot be null");
    }
    if (settlementReference == null || settlementReference.trim().isEmpty()) {
      throw new InvalidRtcSettlementRecordException("Settlement reference cannot be null or empty");
    }

    return RtcSettlementRecord.builder()
        .id(id)
        .rtcAdapterId(rtcAdapterId)
        .settlementDate(settlementDate)
        .settlementReference(settlementReference)
        .totalTransactions(0)
        .totalAmount(BigDecimal.ZERO)
        .successfulTransactions(0)
        .failedTransactions(0)
        .settlementStatus("PENDING")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  /** Update settlement statistics */
  public void updateStatistics(
      Integer totalTransactions,
      BigDecimal totalAmount,
      Integer successfulTransactions,
      Integer failedTransactions) {
    this.totalTransactions = totalTransactions;
    this.totalAmount = totalAmount;
    this.successfulTransactions = successfulTransactions;
    this.failedTransactions = failedTransactions;
    this.updatedAt = Instant.now();
  }

  /** Mark settlement as processing */
  public void markAsProcessing() {
    this.settlementStatus = "PROCESSING";
    this.updatedAt = Instant.now();
  }

  /** Mark settlement as completed */
  public void markAsCompleted() {
    this.settlementStatus = "COMPLETED";
    this.settlementTime = Instant.now();
    this.updatedAt = Instant.now();
  }

  /** Mark settlement as failed */
  public void markAsFailed() {
    this.settlementStatus = "FAILED";
    this.updatedAt = Instant.now();
  }

  /** Check if settlement is completed */
  public boolean isCompleted() {
    return "COMPLETED".equals(this.settlementStatus);
  }

  /** Check if settlement is failed */
  public boolean isFailed() {
    return "FAILED".equals(this.settlementStatus);
  }

  /** Check if settlement is pending */
  public boolean isPending() {
    return "PENDING".equals(this.settlementStatus) || "PROCESSING".equals(this.settlementStatus);
  }

  /** Get success rate percentage */
  public Double getSuccessRate() {
    if (totalTransactions == null || totalTransactions == 0) {
      return 0.0;
    }
    return (double) successfulTransactions / totalTransactions * 100;
  }
}
