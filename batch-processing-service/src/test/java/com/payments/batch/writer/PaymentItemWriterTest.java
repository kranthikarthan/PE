package com.payments.batch.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.payments.batch.domain.ProcessedPayment;
import com.payments.batch.domain.ProcessedPayment.ProcessingStatus;
import com.payments.batch.repository.ProcessedPaymentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

/**
 * Unit tests for PaymentItemWriter.
 *
 * @since PE-401
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentItemWriter")
class PaymentItemWriterTest {

  @Mock private ProcessedPaymentRepository repository;

  private PaymentItemWriter writer;

  @BeforeEach
  void setUp() {
    writer = new PaymentItemWriter(repository);
    writer.resetMetrics();
  }

  @Test
  @DisplayName("Should write validated payments to database")
  void shouldWriteValidatedPaymentsToDatabase() throws Exception {
    // Given
    ProcessedPayment payment1 = createProcessedPayment("PAY001", ProcessingStatus.VALIDATED);
    ProcessedPayment payment2 = createProcessedPayment("PAY002", ProcessingStatus.VALIDATED);

    List<ProcessedPayment> payments = Arrays.asList(payment1, payment2);
    Chunk<ProcessedPayment> chunk = new Chunk<>(payments);

    when(repository.saveAll(any())).thenReturn(payments);

    // When
    writer.write(chunk);

    // Then
    verify(repository).saveAll(payments);
    assertThat(writer.getTotalWritten()).isEqualTo(2);
    assertThat(writer.getTotalValidated()).isEqualTo(2);
    assertThat(writer.getTotalFailed()).isZero();
  }

  @Test
  @DisplayName("Should track failed payments separately")
  void shouldTrackFailedPaymentsSeparately() throws Exception {
    // Given
    ProcessedPayment validPayment = createProcessedPayment("PAY001", ProcessingStatus.VALIDATED);
    ProcessedPayment failedPayment =
        createProcessedPayment("PAY002", ProcessingStatus.VALIDATION_FAILED);
    failedPayment.setValidationErrors("Amount is required");

    List<ProcessedPayment> payments = Arrays.asList(validPayment, failedPayment);
    Chunk<ProcessedPayment> chunk = new Chunk<>(payments);

    when(repository.saveAll(any())).thenReturn(payments);

    // When
    writer.write(chunk);

    // Then
    verify(repository).saveAll(payments);
    assertThat(writer.getTotalWritten()).isEqualTo(2);
    assertThat(writer.getTotalValidated()).isEqualTo(1);
    assertThat(writer.getTotalFailed()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should handle empty chunk")
  void shouldHandleEmptyChunk() throws Exception {
    // Given
    Chunk<ProcessedPayment> emptyChunk = new Chunk<>();

    // When
    writer.write(emptyChunk);

    // Then
    verify(repository, never()).saveAll(any());
    assertThat(writer.getTotalWritten()).isZero();
  }

  @Test
  @DisplayName("Should reset metrics correctly")
  void shouldResetMetricsCorrectly() throws Exception {
    // Given
    ProcessedPayment payment = createProcessedPayment("PAY001", ProcessingStatus.VALIDATED);
    Chunk<ProcessedPayment> chunk = new Chunk<>(Arrays.asList(payment));

    when(repository.saveAll(any())).thenReturn(Arrays.asList(payment));

    writer.write(chunk);
    assertThat(writer.getTotalWritten()).isEqualTo(1);

    // When
    writer.resetMetrics();

    // Then
    assertThat(writer.getTotalWritten()).isZero();
    assertThat(writer.getTotalValidated()).isZero();
    assertThat(writer.getTotalFailed()).isZero();
  }

  @Test
  @DisplayName("Should accumulate metrics across multiple writes")
  void shouldAccumulateMetricsAcrossMultipleWrites() throws Exception {
    // Given
    ProcessedPayment payment1 = createProcessedPayment("PAY001", ProcessingStatus.VALIDATED);
    ProcessedPayment payment2 = createProcessedPayment("PAY002", ProcessingStatus.VALIDATED);
    ProcessedPayment payment3 =
        createProcessedPayment("PAY003", ProcessingStatus.VALIDATION_FAILED);

    Chunk<ProcessedPayment> chunk1 = new Chunk<>(Arrays.asList(payment1, payment2));
    Chunk<ProcessedPayment> chunk2 = new Chunk<>(Arrays.asList(payment3));

    when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    writer.write(chunk1);
    writer.write(chunk2);

    // Then
    assertThat(writer.getTotalWritten()).isEqualTo(3);
    assertThat(writer.getTotalValidated()).isEqualTo(2);
    assertThat(writer.getTotalFailed()).isEqualTo(1);
  }

  /**
   * Creates a processed payment for testing.
   *
   * @param paymentId the payment ID
   * @param status the processing status
   * @return processed payment
   */
  private ProcessedPayment createProcessedPayment(String paymentId, ProcessingStatus status) {
    return ProcessedPayment.builder()
        .id(UUID.randomUUID())
        .batchJobId(12345L)
        .tenantId("TENANT_001")
        .paymentId(paymentId)
        .debtorAccount("12345678")
        .debtorName("John Doe")
        .creditorAccount("87654321")
        .creditorName("Jane Smith")
        .amount(new BigDecimal("1000.00"))
        .currency("ZAR")
        .paymentReference("Invoice 12345")
        .valueDate(LocalDate.now().plusDays(1))
        .paymentType("EFT")
        .processingStatus(status)
        .lineNumber(1)
        .build();
  }
}
