package com.payments.batch.writer;

import com.payments.batch.domain.ProcessedPayment;
import com.payments.batch.domain.ProcessedPayment.ProcessingStatus;
import com.payments.batch.repository.ProcessedPaymentRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.annotation.Transactional;

/**
 * ItemWriter for persisting processed payments to the database.
 *
 * <p>This writer performs batch inserts for efficiency and publishes domain events for validated
 * payments. It supports transaction management and provides detailed logging for monitoring.
 *
 * <p><b>Features:</b>
 *
 * <ul>
 *   <li>Batch database inserts (transactional)
 *   <li>Event publishing for validated payments
 *   <li>Separate handling of validated vs failed payments
 *   <li>Comprehensive metrics and logging
 * </ul>
 *
 * @since PE-401
 */
@Slf4j
public class PaymentItemWriter implements ItemWriter<ProcessedPayment> {

  private final ProcessedPaymentRepository repository;
  // TODO PE-405: Add Kafka event publisher for validated payments

  private long totalWritten = 0;
  private long totalValidated = 0;
  private long totalFailed = 0;

  public PaymentItemWriter(ProcessedPaymentRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public void write(Chunk<? extends ProcessedPayment> chunk) throws Exception {
    List<? extends ProcessedPayment> payments = chunk.getItems();

    if (payments.isEmpty()) {
      return;
    }

    log.debug("Writing {} processed payments to database", payments.size());

    // Batch save all payments
    @SuppressWarnings("unchecked")
    List<ProcessedPayment> saved =
        (List<ProcessedPayment>) repository.saveAll((List<ProcessedPayment>) payments);

    // Update metrics
    totalWritten += saved.size();

    // Count by status
    long validatedCount =
        saved.stream().filter(p -> p.getProcessingStatus() == ProcessingStatus.VALIDATED).count();
    long failedCount =
        saved.stream()
            .filter(p -> p.getProcessingStatus() == ProcessingStatus.VALIDATION_FAILED)
            .count();

    totalValidated += validatedCount;
    totalFailed += failedCount;

    // Publish events for validated payments
    saved.stream()
        .filter(p -> p.getProcessingStatus() == ProcessingStatus.VALIDATED)
        .forEach(this::publishPaymentValidatedEvent);

    log.info(
        "Batch write complete: {} total ({}validated, {} failed)",
        saved.size(),
        validatedCount,
        failedCount);
  }

  /**
   * Publishes a domain event for a validated payment.
   *
   * <p>TODO PE-405: Integrate with Kafka for event publishing
   *
   * @param payment the validated payment
   */
  private void publishPaymentValidatedEvent(ProcessedPayment payment) {
    log.debug("Publishing PaymentValidated event for payment: {}", payment.getPaymentId());

    // TODO: Publish to Kafka topic 'payment.validated'
    // Event should include: paymentId, tenantId, amount, currency, valueDate
    // This will trigger downstream processing (routing, submission, etc.)
  }

  /**
   * Gets the total number of payments written.
   *
   * @return total written count
   */
  public long getTotalWritten() {
    return totalWritten;
  }

  /**
   * Gets the total number of validated payments.
   *
   * @return total validated count
   */
  public long getTotalValidated() {
    return totalValidated;
  }

  /**
   * Gets the total number of failed payments.
   *
   * @return total failed count
   */
  public long getTotalFailed() {
    return totalFailed;
  }

  /** Resets all metrics counters. */
  public void resetMetrics() {
    totalWritten = 0;
    totalValidated = 0;
    totalFailed = 0;
  }
}
