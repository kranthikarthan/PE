package com.payments.rtcadapter.domain;

import com.payments.domain.shared.ClearingMessageId;
import com.payments.rtcadapter.exception.InvalidRtcPaymentMessageException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

/**
 * RTC Payment Message Entity
 *
 * <p>Represents payment messages for RTC processing: - Real-time settlement (seconds) - Amount
 * limit: R5,000 per transaction - ISO 20022 messaging (pacs.008/pacs.002) - 24/7/365 availability
 */
@Entity
@Table(name = "rtc_payment_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RtcPaymentMessage {

  @EmbeddedId private ClearingMessageId id;

  @Column(name = "rtc_adapter_id", nullable = false)
  private String rtcAdapterId;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

  @Column(name = "message_type", nullable = false)
  private String messageType;

  @Column(name = "direction", nullable = false)
  private String direction;

  @Column(name = "message_id", nullable = false)
  private String messageId;

  @Column(name = "instruction_id", nullable = false)
  private String instructionId;

  @Column(name = "end_to_end_id", nullable = false)
  private String endToEndId;

  @Column(name = "transaction_type", nullable = false)
  private String transactionType;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "currency", nullable = false)
  private String currency;

  @Column(name = "debtor_name")
  private String debtorName;

  @Column(name = "debtor_account")
  private String debtorAccount;

  @Column(name = "debtor_bank_code")
  private String debtorBankCode;

  @Column(name = "creditor_name")
  private String creditorName;

  @Column(name = "creditor_account")
  private String creditorAccount;

  @Column(name = "creditor_bank_code")
  private String creditorBankCode;

  @Column(name = "payment_purpose")
  private String paymentPurpose;

  @Column(name = "reference")
  private String reference;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "response_code")
  private String responseCode;

  @Column(name = "response_message")
  private String responseMessage;

  @Column(name = "submitted_at")
  private Instant submittedAt;

  @Column(name = "processed_at")
  private Instant processedAt;

  @Column(name = "settled_at")
  private Instant settledAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rtc_adapter_id", insertable = false, updatable = false)
  private RtcAdapter rtcAdapter;

  /** Create a new RTC payment message */
  public static RtcPaymentMessage create(
      ClearingMessageId id,
      String rtcAdapterId,
      String transactionId,
      String messageType,
      String direction,
      String messageId,
      String instructionId,
      String endToEndId,
      String transactionType,
      BigDecimal amount,
      String currency,
      String debtorName,
      String debtorAccount,
      String debtorBankCode,
      String creditorName,
      String creditorAccount,
      String creditorBankCode,
      String paymentPurpose,
      String reference) {
    if (id == null) {
      throw new InvalidRtcPaymentMessageException("Message ID cannot be null");
    }
    if (rtcAdapterId == null || rtcAdapterId.trim().isEmpty()) {
      throw new InvalidRtcPaymentMessageException("RTC adapter ID cannot be null or empty");
    }
    if (transactionId == null || transactionId.trim().isEmpty()) {
      throw new InvalidRtcPaymentMessageException("Transaction ID cannot be null or empty");
    }
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new InvalidRtcPaymentMessageException("Amount must be greater than zero");
    }
    if (amount.compareTo(new BigDecimal("5000.00")) > 0) {
      throw new InvalidRtcPaymentMessageException("Amount cannot exceed R5,000 limit");
    }

    return RtcPaymentMessage.builder()
        .id(id)
        .rtcAdapterId(rtcAdapterId)
        .transactionId(transactionId)
        .messageType(messageType)
        .direction(direction)
        .messageId(messageId)
        .instructionId(instructionId)
        .endToEndId(endToEndId)
        .transactionType(transactionType)
        .amount(amount)
        .currency(currency)
        .debtorName(debtorName)
        .debtorAccount(debtorAccount)
        .debtorBankCode(debtorBankCode)
        .creditorName(creditorName)
        .creditorAccount(creditorAccount)
        .creditorBankCode(creditorBankCode)
        .paymentPurpose(paymentPurpose)
        .reference(reference)
        .status("PENDING")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  /** Mark message as submitted */
  public void markAsSubmitted() {
    this.status = "SUBMITTED";
    this.submittedAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  /** Mark message as processing */
  public void markAsProcessing() {
    this.status = "PROCESSING";
    this.updatedAt = Instant.now();
  }

  /** Mark message as completed */
  public void markAsCompleted(String responseCode, String responseMessage) {
    this.status = "COMPLETED";
    this.responseCode = responseCode;
    this.responseMessage = responseMessage;
    this.processedAt = Instant.now();
    this.settledAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  /** Mark message as failed */
  public void markAsFailed(String responseCode, String responseMessage) {
    this.status = "FAILED";
    this.responseCode = responseCode;
    this.responseMessage = responseMessage;
    this.processedAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  /** Mark message as rejected */
  public void markAsRejected(String responseCode, String responseMessage) {
    this.status = "REJECTED";
    this.responseCode = responseCode;
    this.responseMessage = responseMessage;
    this.processedAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  /** Check if message is completed */
  public boolean isCompleted() {
    return "COMPLETED".equals(this.status);
  }

  /** Check if message is failed */
  public boolean isFailed() {
    return "FAILED".equals(this.status) || "REJECTED".equals(this.status);
  }

  /** Check if message is pending */
  public boolean isPending() {
    return "PENDING".equals(this.status)
        || "SUBMITTED".equals(this.status)
        || "PROCESSING".equals(this.status);
  }
}
