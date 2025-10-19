package com.payments.batch.repository;

import com.payments.batch.domain.ProcessedPayment;
import com.payments.batch.domain.ProcessedPayment.ProcessingStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing processed payment data.
 *
 * @since PE-401
 */
@Repository
public interface ProcessedPaymentRepository extends JpaRepository<ProcessedPayment, UUID> {

  /**
   * Find all processed payments for a specific batch job.
   *
   * @param batchJobId the batch job ID
   * @return list of processed payments
   */
  List<ProcessedPayment> findByBatchJobId(Long batchJobId);

  /**
   * Find all processed payments for a specific batch job and tenant.
   *
   * @param batchJobId the batch job ID
   * @param tenantId the tenant ID
   * @return list of processed payments
   */
  List<ProcessedPayment> findByBatchJobIdAndTenantId(Long batchJobId, String tenantId);

  /**
   * Count payments by status for a specific batch job.
   *
   * @param batchJobId the batch job ID
   * @param status the processing status
   * @return count of payments
   */
  long countByBatchJobIdAndProcessingStatus(Long batchJobId, ProcessingStatus status);

  /**
   * Find all failed payments for a specific batch job.
   *
   * @param batchJobId the batch job ID
   * @return list of failed payments
   */
  @Query(
      "SELECT p FROM ProcessedPayment p WHERE p.batchJobId = :batchJobId "
          + "AND p.processingStatus = 'VALIDATION_FAILED' ORDER BY p.lineNumber")
  List<ProcessedPayment> findFailedPaymentsByBatchJobId(@Param("batchJobId") Long batchJobId);

  /**
   * Find all validated payments ready for submission.
   *
   * @param batchJobId the batch job ID
   * @param tenantId the tenant ID
   * @return list of validated payments
   */
  @Query(
      "SELECT p FROM ProcessedPayment p WHERE p.batchJobId = :batchJobId "
          + "AND p.tenantId = :tenantId AND p.processingStatus = 'VALIDATED' "
          + "ORDER BY p.valueDate, p.createdAt")
  List<ProcessedPayment> findValidatedPaymentsForSubmission(
      @Param("batchJobId") Long batchJobId, @Param("tenantId") String tenantId);

  /**
   * Find all processed payments for a specific tenant.
   *
   * @param tenantId the tenant ID
   * @return list of processed payments
   */
  List<ProcessedPayment> findByTenantId(String tenantId);
}
