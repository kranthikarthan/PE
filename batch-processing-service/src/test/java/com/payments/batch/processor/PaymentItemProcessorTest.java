package com.payments.batch.processor;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.batch.domain.PaymentRecord;
import com.payments.batch.domain.ProcessedPayment;
import com.payments.batch.domain.ProcessedPayment.ProcessingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for PaymentItemProcessor.
 *
 * @since PE-401
 */
@DisplayName("PaymentItemProcessor")
class PaymentItemProcessorTest {

  private static final String TENANT_ID = "TENANT_001";
  private static final Long BATCH_JOB_ID = 12345L;

  private PaymentItemProcessor processor;

  @BeforeEach
  void setUp() {
    processor = new PaymentItemProcessor(TENANT_ID, BATCH_JOB_ID);
  }

  @Nested
  @DisplayName("Valid Payment Processing")
  class ValidPaymentProcessing {

    @Test
    @DisplayName("Should process valid payment record successfully")
    void shouldProcessValidPaymentRecord() throws Exception {
      // Given
      PaymentRecord record = createValidPaymentRecord();

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getProcessingStatus()).isEqualTo(ProcessingStatus.VALIDATED);
      assertThat(result.getValidationErrors()).isNull();
      assertThat(result.getTenantId()).isEqualTo(TENANT_ID);
      assertThat(result.getBatchJobId()).isEqualTo(BATCH_JOB_ID);
      assertThat(result.getPaymentId()).isEqualTo("PAY001");
      assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("1000.50"));
      assertThat(result.getCurrency()).isEqualTo("ZAR");
    }

    @Test
    @DisplayName("Should normalize account numbers")
    void shouldNormalizeAccountNumbers() throws Exception {
      // Given
      PaymentRecord record = createValidPaymentRecord();
      record.setDebtorAccount("00012345678"); // Leading zeros
      record.setCreditorAccount("98765432 "); // Trailing space

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result.getDebtorAccount()).isEqualTo("12345678");
      assertThat(result.getCreditorAccount()).isEqualTo("98765432");
    }

    @Test
    @DisplayName("Should convert currency to uppercase")
    void shouldConvertCurrencyToUppercase() throws Exception {
      // Given
      PaymentRecord record = createValidPaymentRecord();
      record.setCurrency("zar");

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result.getCurrency()).isEqualTo("ZAR");
    }
  }

  @Nested
  @DisplayName("Validation Failures")
  class ValidationFailures {

    @Test
    @DisplayName("Should fail when payment ID is missing")
    void shouldFailWhenPaymentIdIsMissing() throws Exception {
      // Given
      PaymentRecord record = createValidPaymentRecord();
      record.setPaymentId(null);

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result.getProcessingStatus()).isEqualTo(ProcessingStatus.VALIDATION_FAILED);
      assertThat(result.getValidationErrors()).contains("Payment ID is required");
    }

    @Test
    @DisplayName("Should fail when debtor account is missing")
    void shouldFailWhenDebtorAccountIsMissing() throws Exception {
      // Given
      PaymentRecord record = createValidPaymentRecord();
      record.setDebtorAccount(null);

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result.getProcessingStatus()).isEqualTo(ProcessingStatus.VALIDATION_FAILED);
      assertThat(result.getValidationErrors()).contains("Debtor account is required");
    }

    @Test
    @DisplayName("Should fail when amount is below minimum")
    void shouldFailWhenAmountIsBelowMinimum() throws Exception {
      // Given
      PaymentRecord record = createValidPaymentRecord();
      record.setAmount(new BigDecimal("0.001"));

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result.getProcessingStatus()).isEqualTo(ProcessingStatus.VALIDATION_FAILED);
      assertThat(result.getValidationErrors()).contains("Amount must be at least");
    }

    @Test
    @DisplayName("Should fail when amount exceeds maximum")
    void shouldFailWhenAmountExceedsMaximum() throws Exception {
      // Given
      PaymentRecord record = createValidPaymentRecord();
      record.setAmount(new BigDecimal("20000000.00")); // 20 million

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result.getProcessingStatus()).isEqualTo(ProcessingStatus.VALIDATION_FAILED);
      assertThat(result.getValidationErrors()).contains("Amount exceeds maximum limit");
    }

    @Test
    @DisplayName("Should fail when currency code is invalid")
    void shouldFailWhenCurrencyCodeIsInvalid() throws Exception {
      // Given
      PaymentRecord record = createValidPaymentRecord();
      record.setCurrency("USD1"); // Invalid format

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result.getProcessingStatus()).isEqualTo(ProcessingStatus.VALIDATION_FAILED);
      assertThat(result.getValidationErrors()).contains("Invalid currency code");
    }

    @Test
    @DisplayName("Should fail when account numbers are the same")
    void shouldFailWhenAccountNumbersAreTheSame() throws Exception {
      // Given
      PaymentRecord record = createValidPaymentRecord();
      record.setDebtorAccount("12345678");
      record.setCreditorAccount("12345678");

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result.getProcessingStatus()).isEqualTo(ProcessingStatus.VALIDATION_FAILED);
      assertThat(result.getValidationErrors())
          .contains("Debtor and creditor accounts cannot be the same");
    }

    @Test
    @DisplayName("Should fail when value date is in the past")
    void shouldFailWhenValueDateIsInThePast() throws Exception {
      // Given
      PaymentRecord record = createValidPaymentRecord();
      record.setValueDate(LocalDate.now().minusDays(1));

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result.getProcessingStatus()).isEqualTo(ProcessingStatus.VALIDATION_FAILED);
      assertThat(result.getValidationErrors()).contains("Value date cannot be in the past");
    }

    @Test
    @DisplayName("Should fail when value date is too far in future")
    void shouldFailWhenValueDateIsTooFarInFuture() throws Exception {
      // Given
      PaymentRecord record = createValidPaymentRecord();
      record.setValueDate(LocalDate.now().plusDays(400)); // More than 365 days

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result.getProcessingStatus()).isEqualTo(ProcessingStatus.VALIDATION_FAILED);
      assertThat(result.getValidationErrors()).contains("Value date cannot be more than");
    }

    @Test
    @DisplayName("Should fail when account format is invalid")
    void shouldFailWhenAccountFormatIsInvalid() throws Exception {
      // Given
      PaymentRecord record = createValidPaymentRecord();
      record.setDebtorAccount("ABC123"); // Not numeric

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result.getProcessingStatus()).isEqualTo(ProcessingStatus.VALIDATION_FAILED);
      assertThat(result.getValidationErrors()).contains("Invalid debtor account format");
    }
  }

  @Nested
  @DisplayName("Multiple Validation Errors")
  class MultipleValidationErrors {

    @Test
    @DisplayName("Should collect multiple validation errors")
    void shouldCollectMultipleValidationErrors() throws Exception {
      // Given
      PaymentRecord record = new PaymentRecord();
      record.setPaymentId(null); // Error 1
      record.setDebtorAccount(null); // Error 2
      record.setCreditorAccount(null); // Error 3
      record.setAmount(null); // Error 4
      record.setCurrency(null); // Error 5
      record.setValueDate(null); // Error 6

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result.getProcessingStatus()).isEqualTo(ProcessingStatus.VALIDATION_FAILED);
      assertThat(result.getValidationErrors()).isNotNull();
      assertThat(result.getValidationErrors()).contains("Payment ID is required");
      assertThat(result.getValidationErrors()).contains("Debtor account is required");
      assertThat(result.getValidationErrors()).contains("Amount is required");
      assertThat(result.getValidationErrors()).contains("Currency is required");
    }
  }

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCases {

    @Test
    @DisplayName("Should handle minimum valid amount")
    void shouldHandleMinimumValidAmount() throws Exception {
      // Given
      PaymentRecord record = createValidPaymentRecord();
      record.setAmount(new BigDecimal("0.01")); // Minimum

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result.getProcessingStatus()).isEqualTo(ProcessingStatus.VALIDATED);
      assertThat(result.getValidationErrors()).isNull();
    }

    @Test
    @DisplayName("Should handle maximum valid amount")
    void shouldHandleMaximumValidAmount() throws Exception {
      // Given
      PaymentRecord record = createValidPaymentRecord();
      record.setAmount(new BigDecimal("10000000.00")); // Maximum

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result.getProcessingStatus()).isEqualTo(ProcessingStatus.VALIDATED);
      assertThat(result.getValidationErrors()).isNull();
    }

    @Test
    @DisplayName("Should handle today as value date")
    void shouldHandleTodayAsValueDate() throws Exception {
      // Given
      PaymentRecord record = createValidPaymentRecord();
      record.setValueDate(LocalDate.now());

      // When
      ProcessedPayment result = processor.process(record);

      // Then
      assertThat(result.getProcessingStatus()).isEqualTo(ProcessingStatus.VALIDATED);
      assertThat(result.getValidationErrors()).isNull();
    }
  }

  /**
   * Creates a valid payment record for testing.
   *
   * @return valid payment record
   */
  private PaymentRecord createValidPaymentRecord() {
    return PaymentRecord.builder()
        .paymentId("PAY001")
        .debtorAccount("12345678")
        .debtorName("John Doe")
        .creditorAccount("87654321")
        .creditorName("Jane Smith")
        .amount(new BigDecimal("1000.50"))
        .currency("ZAR")
        .paymentReference("Invoice 12345")
        .valueDate(LocalDate.now().plusDays(1))
        .debtorBankCode("ABSA")
        .creditorBankCode("FNB")
        .paymentType("EFT")
        .lineNumber(1)
        .build();
  }
}
