package com.payments.payshapadapter.domain;

import com.payments.payshapadapter.exception.InvalidPayShapTransactionLogException;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** PayShap Transaction Log Entity */
@Entity
@Table(name = "payshap_transaction_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayShapTransactionLog {

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "payshap_adapter_id", nullable = false)
  private String payshapAdapterId;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

  @Column(name = "log_level", nullable = false)
  private String logLevel;

  @Column(name = "message", nullable = false)
  private String message;

  @Column(name = "details")
  private String details;

  @Column(name = "occurred_at")
  private Instant occurredAt;

  public static PayShapTransactionLog create(
      String payshapAdapterId,
      String transactionId,
      String logLevel,
      String message,
      String details) {
    if (payshapAdapterId == null || payshapAdapterId.trim().isEmpty()) {
      throw new InvalidPayShapTransactionLogException("PayShap adapter ID cannot be null or empty");
    }
    if (transactionId == null || transactionId.trim().isEmpty()) {
      throw new InvalidPayShapTransactionLogException("Transaction ID cannot be null or empty");
    }
    if (message == null || message.trim().isEmpty()) {
      throw new InvalidPayShapTransactionLogException("Message cannot be null or empty");
    }

    return PayShapTransactionLog.builder()
        .id(java.util.UUID.randomUUID().toString())
        .payshapAdapterId(payshapAdapterId)
        .transactionId(transactionId)
        .logLevel(logLevel != null ? logLevel : "INFO")
        .message(message)
        .details(details)
        .occurredAt(Instant.now())
        .build();
  }
}
