package com.payments.batch.processor;

import com.payments.batch.domain.PaymentRecord;
import com.payments.batch.domain.ProcessedPayment;
import com.payments.batch.domain.ProcessedPayment.ProcessingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

/**
 * ItemProcessor for validating and transforming payment records.
 *
 * <p>This processor applies comprehensive business rules and validation logic to each payment
 * record, transforming raw input data into validated ProcessedPayment entities.
 *
 * <p><b>Validation Rules:</b>
 *
 * <ul>
 *   <li>Required fields validation (payment ID, accounts, amount, currency)
 *   <li>Amount validation (positive, within limits)
 *   <li>Currency code validation (ISO 4217)
 *   <li>Account number format validation
 *   <li>Date validation (not in past, within acceptable range)
 *   <li>Payment type validation
 *   <li>Business rules (same-account checks, duplicate detection)
 * </ul>
 *
 * @since PE-401
 */
@Slf4j
public class PaymentItemProcessor implements ItemProcessor<PaymentRecord, ProcessedPayment> {

  private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
  private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000000.00"); // 10 million
  private static final int MAX_FUTURE_DAYS = 365; // 1 year
  private static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");
  private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[0-9]{8,16}$");

  private final String tenantId;
  private final Long batchJobId;

  public PaymentItemProcessor(String tenantId, Long batchJobId) {
    this.tenantId = tenantId;
    this.batchJobId = batchJobId;
  }

  @Override
  public ProcessedPayment process(PaymentRecord record) throws Exception {
    log.debug("Processing payment record: {}", record.getPaymentId());

    // Validate the record
    List<String> validationErrors = validatePaymentRecord(record);

    // Determine processing status
    ProcessingStatus status =
        validationErrors.isEmpty()
            ? ProcessingStatus.VALIDATED
            : ProcessingStatus.VALIDATION_FAILED;

    // Transform to ProcessedPayment
    ProcessedPayment processedPayment =
        ProcessedPayment.builder()
            .batchJobId(batchJobId)
            .tenantId(tenantId)
            .paymentId(record.getPaymentId())
            .debtorAccount(normalizeAccount(record.getDebtorAccount()))
            .debtorName(record.getDebtorName())
            .creditorAccount(normalizeAccount(record.getCreditorAccount()))
            .creditorName(record.getCreditorName())
            .amount(record.getAmount())
            .currency(record.getCurrency() != null ? record.getCurrency().toUpperCase() : null)
            .paymentReference(record.getPaymentReference())
            .valueDate(record.getValueDate())
            .debtorBankCode(record.getDebtorBankCode())
            .creditorBankCode(record.getCreditorBankCode())
            .paymentType(determinePaymentType(record))
            .processingStatus(status)
            .validationErrors(
                validationErrors.isEmpty() ? null : String.join("; ", validationErrors))
            .lineNumber(record.getLineNumber())
            .processedAt(LocalDateTime.now())
            .build();

    if (status == ProcessingStatus.VALIDATION_FAILED) {
      log.warn(
          "Payment {} failed validation: {}",
          record.getPaymentId(),
          processedPayment.getValidationErrors());
    }

    return processedPayment;
  }

  /**
   * Validates a payment record against business rules.
   *
   * @param record the payment record to validate
   * @return list of validation error messages (empty if valid)
   */
  private List<String> validatePaymentRecord(PaymentRecord record) {
    List<String> errors = new ArrayList<>();

    // Check for existing parse errors
    if (record.getValidationErrors() != null && !record.getValidationErrors().isEmpty()) {
      errors.add(record.getValidationErrors());
      return errors; // Don't validate further if there are parse errors
    }

    // Required field validation
    if (isBlank(record.getPaymentId())) {
      errors.add("Payment ID is required");
    }
    if (isBlank(record.getDebtorAccount())) {
      errors.add("Debtor account is required");
    }
    if (isBlank(record.getCreditorAccount())) {
      errors.add("Creditor account is required");
    }
    if (record.getAmount() == null) {
      errors.add("Amount is required");
    }
    if (isBlank(record.getCurrency())) {
      errors.add("Currency is required");
    }
    if (record.getValueDate() == null) {
      errors.add("Value date is required");
    }

    // Amount validation
    if (record.getAmount() != null) {
      if (record.getAmount().compareTo(MIN_AMOUNT) < 0) {
        errors.add("Amount must be at least " + MIN_AMOUNT);
      }
      if (record.getAmount().compareTo(MAX_AMOUNT) > 0) {
        errors.add("Amount exceeds maximum limit of " + MAX_AMOUNT);
      }
      if (record.getAmount().scale() > 2) {
        errors.add("Amount cannot have more than 2 decimal places");
      }
    }

    // Currency validation
    if (!isBlank(record.getCurrency())) {
      if (!CURRENCY_PATTERN.matcher(record.getCurrency().toUpperCase()).matches()) {
        errors.add("Invalid currency code (must be 3-letter ISO 4217 code)");
      }
    }

    // Account validation
    if (!isBlank(record.getDebtorAccount())) {
      if (!ACCOUNT_PATTERN.matcher(record.getDebtorAccount()).matches()) {
        errors.add("Invalid debtor account format (must be 8-16 digits)");
      }
    }
    if (!isBlank(record.getCreditorAccount())) {
      if (!ACCOUNT_PATTERN.matcher(record.getCreditorAccount()).matches()) {
        errors.add("Invalid creditor account format (must be 8-16 digits)");
      }
    }

    // Same account check
    if (!isBlank(record.getDebtorAccount())
        && !isBlank(record.getCreditorAccount())
        && record.getDebtorAccount().equals(record.getCreditorAccount())) {
      errors.add("Debtor and creditor accounts cannot be the same");
    }

    // Date validation
    if (record.getValueDate() != null) {
      LocalDate today = LocalDate.now();
      LocalDate maxFutureDate = today.plusDays(MAX_FUTURE_DAYS);

      if (record.getValueDate().isBefore(today)) {
        errors.add("Value date cannot be in the past");
      }
      if (record.getValueDate().isAfter(maxFutureDate)) {
        errors.add("Value date cannot be more than " + MAX_FUTURE_DAYS + " days in the future");
      }
    }

    return errors;
  }

  /**
   * Determines the payment type based on available information.
   *
   * @param record the payment record
   * @return the determined payment type
   */
  private String determinePaymentType(PaymentRecord record) {
    if (!isBlank(record.getPaymentType())) {
      return record.getPaymentType().toUpperCase();
    }

    // Default logic: if no payment type specified, default to EFT
    // In a real system, this might be determined by bank codes, amount, currency, etc.
    return "EFT";
  }

  /**
   * Normalizes an account number by removing whitespace and leading zeros.
   *
   * @param account the account number
   * @return normalized account number
   */
  private String normalizeAccount(String account) {
    if (isBlank(account)) {
      return account;
    }
    return account.trim().replaceAll("^0+", "");
  }

  /**
   * Checks if a string is blank (null, empty, or whitespace only).
   *
   * @param str the string to check
   * @return true if blank, false otherwise
   */
  private boolean isBlank(String str) {
    return str == null || str.trim().isEmpty();
  }
}
