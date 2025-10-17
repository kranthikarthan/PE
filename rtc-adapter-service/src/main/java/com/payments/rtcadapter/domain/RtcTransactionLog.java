package com.payments.rtcadapter.domain;

import com.payments.domain.shared.ClearingMessageId;
import com.payments.rtcadapter.exception.InvalidRtcTransactionLogException;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

/**
 * RTC Transaction Log Entity
 *
 * <p>Represents transaction logs for RTC processing: - Operation audit trail - Performance
 * monitoring - Error tracking - Processing time measurement
 */
@Entity
@Table(name = "rtc_transaction_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RtcTransactionLog {

  @EmbeddedId private ClearingMessageId id;

  @Column(name = "rtc_adapter_id", nullable = false)
  private String rtcAdapterId;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

  @Column(name = "operation", nullable = false)
  private String operation;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "processing_time_ms")
  private Integer processingTimeMs;

  @Column(name = "error_code")
  private String errorCode;

  @Column(name = "error_message")
  private String errorMessage;

  @Column(name = "request_payload")
  private String requestPayload;

  @Column(name = "response_payload")
  private String responsePayload;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rtc_adapter_id", insertable = false, updatable = false)
  private RtcAdapter rtcAdapter;

  /** Create a new RTC transaction log */
  public static RtcTransactionLog create(
      ClearingMessageId id,
      String rtcAdapterId,
      String transactionId,
      String operation,
      String status,
      Integer processingTimeMs,
      String errorCode,
      String errorMessage,
      String requestPayload,
      String responsePayload) {
    if (id == null) {
      throw new InvalidRtcTransactionLogException("Log ID cannot be null");
    }
    if (rtcAdapterId == null || rtcAdapterId.trim().isEmpty()) {
      throw new InvalidRtcTransactionLogException("RTC adapter ID cannot be null or empty");
    }
    if (transactionId == null || transactionId.trim().isEmpty()) {
      throw new InvalidRtcTransactionLogException("Transaction ID cannot be null or empty");
    }
    if (operation == null || operation.trim().isEmpty()) {
      throw new InvalidRtcTransactionLogException("Operation cannot be null or empty");
    }
    if (status == null || status.trim().isEmpty()) {
      throw new InvalidRtcTransactionLogException("Status cannot be null or empty");
    }

    return RtcTransactionLog.builder()
        .id(id)
        .rtcAdapterId(rtcAdapterId)
        .transactionId(transactionId)
        .operation(operation)
        .status(status)
        .processingTimeMs(processingTimeMs)
        .errorCode(errorCode)
        .errorMessage(errorMessage)
        .requestPayload(requestPayload)
        .responsePayload(responsePayload)
        .occurredAt(Instant.now())
        .build();
  }

  /** Check if log represents success */
  public boolean isSuccess() {
    return "SUCCESS".equals(this.status);
  }

  /** Check if log represents failure */
  public boolean isFailure() {
    return "FAILED".equals(this.status)
        || "ERROR".equals(this.status)
        || "TIMEOUT".equals(this.status);
  }

  /** Get processing time in seconds */
  public Double getProcessingTimeSeconds() {
    return processingTimeMs != null ? processingTimeMs / 1000.0 : null;
  }
}
