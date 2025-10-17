package com.payments.bankservafricaadapter.domain;

import com.payments.bankservafricaadapter.exception.InvalidBankservAfricaIso8583MessageException;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

/**
 * BankservAfrica ISO 8583 Message Entity
 *
 * <p>Represents ISO 8583 messages for BankservAfrica processing: - Card transaction processing -
 * Message type identification (MTI) - Processing code handling - Response code management
 */
@Entity
@Table(name = "bankservafrica_iso8583_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class BankservAfricaIso8583Message {

  @EmbeddedId private ClearingMessageId id;

  @Embedded private ClearingAdapterId bankservafricaAdapterId;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

  @Column(name = "message_type", nullable = false)
  private String messageType;

  @Column(name = "direction", nullable = false)
  private String direction;

  @Column(name = "mti", nullable = false, length = 4)
  private String mti;

  @Column(name = "processing_code", nullable = false, length = 6)
  private String processingCode;

  @Column(name = "amount", precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "currency_code", length = 3)
  private String currencyCode;

  @Column(name = "card_number", length = 19)
  private String cardNumber;

  @Column(name = "merchant_id", length = 15)
  private String merchantId;

  @Column(name = "terminal_id", length = 8)
  private String terminalId;

  @Column(name = "response_code", length = 2)
  private String responseCode;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "raw_message", columnDefinition = "TEXT")
  private String rawMessage;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  /** Create a new ISO 8583 message */
  public static BankservAfricaIso8583Message create(
      ClearingMessageId id,
      ClearingAdapterId bankservafricaAdapterId,
      String transactionId,
      String messageType,
      String direction,
      String mti,
      String processingCode) {

    if (transactionId == null || transactionId.isBlank()) {
      throw new InvalidBankservAfricaIso8583MessageException(
          "Transaction ID cannot be null or blank");
    }
    if (mti == null || mti.isBlank()) {
      throw new InvalidBankservAfricaIso8583MessageException("MTI cannot be null or blank");
    }
    if (processingCode == null || processingCode.isBlank()) {
      throw new InvalidBankservAfricaIso8583MessageException(
          "Processing code cannot be null or blank");
    }

    BankservAfricaIso8583Message message = new BankservAfricaIso8583Message();
    message.id = id;
    message.bankservafricaAdapterId = bankservafricaAdapterId;
    message.transactionId = transactionId;
    message.messageType = messageType;
    message.direction = direction;
    message.mti = mti;
    message.processingCode = processingCode;
    message.status = "PENDING";
    message.createdAt = Instant.now();
    message.updatedAt = Instant.now();

    return message;
  }

  /** Update message with transaction details */
  public void updateTransactionDetails(
      BigDecimal amount,
      String currencyCode,
      String cardNumber,
      String merchantId,
      String terminalId) {

    this.amount = amount;
    this.currencyCode = currencyCode;
    this.cardNumber = cardNumber;
    this.merchantId = merchantId;
    this.terminalId = terminalId;
    this.updatedAt = Instant.now();
  }

  /** Update message status */
  public void updateStatus(String status, String responseCode) {
    this.status = status;
    this.responseCode = responseCode;
    this.updatedAt = Instant.now();
  }

  /** Set raw message */
  public void setRawMessage(String rawMessage) {
    this.rawMessage = rawMessage;
    this.updatedAt = Instant.now();
  }

  /** Mark as processing */
  public void markAsProcessing() {
    updateStatus("PROCESSING", null);
  }

  /** Mark as completed */
  public void markAsCompleted(String responseCode) {
    updateStatus("COMPLETED", responseCode);
  }

  /** Mark as failed */
  public void markAsFailed(String responseCode) {
    updateStatus("FAILED", responseCode);
  }

  /** Check if message is pending */
  public boolean isPending() {
    return "PENDING".equals(this.status);
  }

  /** Check if message is processing */
  public boolean isProcessing() {
    return "PROCESSING".equals(this.status);
  }

  /** Check if message is completed */
  public boolean isCompleted() {
    return "COMPLETED".equals(this.status);
  }

  /** Check if message is failed */
  public boolean isFailed() {
    return "FAILED".equals(this.status);
  }

  /** Check if response is successful */
  public boolean isSuccessful() {
    return "00".equals(this.responseCode);
  }
}
