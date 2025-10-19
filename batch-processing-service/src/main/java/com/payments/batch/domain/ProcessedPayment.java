package com.payments.batch.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing a processed payment after validation and transformation.
 *
 * <p>This is the OUTPUT model used by ItemWriter to persist validated payment data to the database
 * and publish events. It represents payment data after business rule application and validation.
 *
 * @see PaymentRecord
 * @since PE-401
 */
@Entity
@Table(
    name = "processed_payments",
    indexes = {
      @Index(name = "idx_processed_payments_batch_id", columnList = "batch_job_id"),
      @Index(name = "idx_processed_payments_tenant", columnList = "tenant_id"),
      @Index(name = "idx_processed_payments_status", columnList = "processing_status"),
      @Index(name = "idx_processed_payments_value_date", columnList = "value_date")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedPayment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "batch_job_id", nullable = false)
  private Long batchJobId;

  @Column(name = "tenant_id", nullable = false, length = 50)
  private String tenantId;

  @Column(name = "payment_id", nullable = false, length = 100)
  private String paymentId;

  @Column(name = "debtor_account", nullable = false, length = 50)
  private String debtorAccount;

  @Column(name = "debtor_name", length = 200)
  private String debtorName;

  @Column(name = "creditor_account", nullable = false, length = 50)
  private String creditorAccount;

  @Column(name = "creditor_name", length = 200)
  private String creditorName;

  @Column(name = "amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "payment_reference", length = 500)
  private String paymentReference;

  @Column(name = "value_date", nullable = false)
  private LocalDate valueDate;

  @Column(name = "debtor_bank_code", length = 20)
  private String debtorBankCode;

  @Column(name = "creditor_bank_code", length = 20)
  private String creditorBankCode;

  @Column(name = "payment_type", nullable = false, length = 20)
  private String paymentType;

  @Enumerated(EnumType.STRING)
  @Column(name = "processing_status", nullable = false, length = 20)
  private ProcessingStatus processingStatus;

  @Column(name = "validation_errors", columnDefinition = "TEXT")
  private String validationErrors;

  @Column(name = "line_number")
  private Integer lineNumber;

  @Column(name = "processed_at", nullable = false)
  private LocalDateTime processedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    if (processedAt == null) {
      processedAt = LocalDateTime.now();
    }
  }

  /** Processing status of the payment record */
  public enum ProcessingStatus {
    /** Payment passed validation and is ready for submission */
    VALIDATED,
    /** Payment has validation errors and needs correction */
    VALIDATION_FAILED,
    /** Payment is pending external validation (e.g., account verification) */
    PENDING_VALIDATION,
    /** Payment has been submitted to clearing system */
    SUBMITTED
  }
}
