package com.payments.samosadapter.domain;

import com.payments.domain.shared.TenantContext;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

/**
 * SAMOS Transaction Log Entity
 *
 * <p>Logs all SAMOS operations including submit, query, and cancel operations with their
 * request/response payloads and performance metrics.
 */
@Entity
@Table(name = "samos_transaction_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class SamosTransactionLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Embedded
  @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id"))
  private TenantContext tenantContext;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

  @Column(name = "payment_id", nullable = false)
  private String paymentId;

  @Column(name = "message_id", nullable = false)
  private String messageId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Operation operation;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransactionStatus status;

  @Column(name = "request_payload", columnDefinition = "TEXT")
  private String requestPayload;

  @Column(name = "response_payload", columnDefinition = "TEXT")
  private String responsePayload;

  @Column(name = "error_code")
  private String errorCode;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "processing_time_ms")
  private Integer processingTimeMs;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "samos_adapter_id")
  private SamosAdapter samosAdapter;

  public static SamosTransactionLog create(
      TenantContext tenantContext,
      String transactionId,
      String paymentId,
      String messageId,
      Operation operation,
      String requestPayload) {

    if (transactionId == null || transactionId.isBlank()) {
      throw new IllegalArgumentException("Transaction ID cannot be null or blank");
    }
    if (paymentId == null || paymentId.isBlank()) {
      throw new IllegalArgumentException("Payment ID cannot be null or blank");
    }
    if (messageId == null || messageId.isBlank()) {
      throw new IllegalArgumentException("Message ID cannot be null or blank");
    }

    SamosTransactionLog log = new SamosTransactionLog();
    log.tenantContext = tenantContext;
    log.transactionId = transactionId;
    log.paymentId = paymentId;
    log.messageId = messageId;
    log.operation = operation;
    log.status = TransactionStatus.PENDING;
    log.requestPayload = requestPayload;
    log.createdAt = Instant.now();

    return log;
  }

  public void markAsSuccess(String responsePayload, Integer processingTimeMs) {
    this.status = TransactionStatus.SUCCESS;
    this.responsePayload = responsePayload;
    this.processingTimeMs = processingTimeMs;
  }

  public void markAsFailed(String errorCode, String errorMessage, Integer processingTimeMs) {
    this.status = TransactionStatus.FAILED;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.processingTimeMs = processingTimeMs;
  }

  public void markAsTimeout(Integer processingTimeMs) {
    this.status = TransactionStatus.TIMEOUT;
    this.errorCode = "TIMEOUT";
    this.errorMessage = "Request timed out";
    this.processingTimeMs = processingTimeMs;
  }

  public boolean isSuccess() {
    return this.status == TransactionStatus.SUCCESS;
  }

  public boolean isFailed() {
    return this.status == TransactionStatus.FAILED;
  }

  public boolean isTimeout() {
    return this.status == TransactionStatus.TIMEOUT;
  }

  public enum Operation {
    SUBMIT("Submit payment to SAMOS"),
    QUERY("Query payment status"),
    CANCEL("Cancel payment");

    private final String description;

    Operation(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    TIMEOUT
  }
}
