package com.payments.bankservafricaadapter.domain;

import com.payments.bankservafricaadapter.exception.InvalidBankservAfricaEftMessageException;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

/**
 * BankservAfrica EFT Message Entity
 *
 * <p>Represents EFT batch messages for BankservAfrica processing: - Batch processing support -
 * Message tracking - Status management - Error handling
 */
@Entity
@Table(name = "bankservafrica_eft_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class BankservAfricaEftMessage {

  @EmbeddedId private ClearingMessageId id;

  @Embedded private ClearingAdapterId bankservafricaAdapterId;

  @Column(name = "batch_id", nullable = false)
  private String batchId;

  @Column(name = "message_id", nullable = false)
  private String messageId;

  @Column(name = "message_type", nullable = false)
  private String messageType;

  @Column(name = "direction", nullable = false)
  private String direction;

  @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(name = "payload_hash", nullable = false)
  private String payloadHash;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "status_code")
  private Integer statusCode;

  @Column(name = "error_code")
  private String errorCode;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "processing_started_at")
  private Instant processingStartedAt;

  @Column(name = "processing_completed_at")
  private Instant processingCompletedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  /** Create a new EFT message */
  public static BankservAfricaEftMessage create(
      ClearingMessageId id,
      ClearingAdapterId bankservafricaAdapterId,
      String batchId,
      String messageId,
      String messageType,
      String direction,
      String payload,
      String payloadHash) {

    if (batchId == null || batchId.isBlank()) {
      throw new InvalidBankservAfricaEftMessageException("Batch ID cannot be null or blank");
    }
    if (messageId == null || messageId.isBlank()) {
      throw new InvalidBankservAfricaEftMessageException("Message ID cannot be null or blank");
    }
    if (payload == null || payload.isBlank()) {
      throw new InvalidBankservAfricaEftMessageException("Payload cannot be null or blank");
    }

    BankservAfricaEftMessage message = new BankservAfricaEftMessage();
    message.id = id;
    message.bankservafricaAdapterId = bankservafricaAdapterId;
    message.batchId = batchId;
    message.messageId = messageId;
    message.messageType = messageType;
    message.direction = direction;
    message.payload = payload;
    message.payloadHash = payloadHash;
    message.status = "PENDING";
    message.createdAt = Instant.now();
    message.updatedAt = Instant.now();

    return message;
  }

  /** Update message status */
  public void updateStatus(
      String status, Integer statusCode, String errorCode, String errorMessage) {
    this.status = status;
    this.statusCode = statusCode;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.updatedAt = Instant.now();

    if ("PROCESSING".equals(status)) {
      this.processingStartedAt = Instant.now();
    } else if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
      this.processingCompletedAt = Instant.now();
    }
  }

  /** Mark as processing */
  public void markAsProcessing() {
    updateStatus("PROCESSING", null, null, null);
  }

  /** Mark as completed */
  public void markAsCompleted(Integer statusCode) {
    updateStatus("COMPLETED", statusCode, null, null);
  }

  /** Mark as failed */
  public void markAsFailed(String errorCode, String errorMessage) {
    updateStatus("FAILED", null, errorCode, errorMessage);
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
}
