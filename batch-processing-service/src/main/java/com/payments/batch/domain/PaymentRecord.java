package com.payments.batch.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing a payment record from batch file input.
 *
 * <p>This is the INPUT model used by ItemReader to read payment data from various file formats
 * (CSV, Excel, XML, JSON). It represents raw payment data before validation and processing.
 *
 * @see ProcessedPayment
 * @since PE-401
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRecord {

  /** Unique payment identifier from the batch file */
  private String paymentId;

  /** Debtor account number */
  private String debtorAccount;

  /** Debtor account name */
  private String debtorName;

  /** Creditor account number */
  private String creditorAccount;

  /** Creditor account name */
  private String creditorName;

  /** Payment amount */
  private BigDecimal amount;

  /** Currency code (ISO 4217) */
  private String currency;

  /** Payment reference/description */
  private String paymentReference;

  /** Value date (execution date) */
  private LocalDate valueDate;

  /** Debtor bank code */
  private String debtorBankCode;

  /** Creditor bank code */
  private String creditorBankCode;

  /** Payment type (EFT, RTC, SWIFT, etc.) */
  private String paymentType;

  /** Line number in source file (for error reporting) */
  private Integer lineNumber;

  /** Validation errors (populated during processing) */
  private transient String validationErrors;
}
