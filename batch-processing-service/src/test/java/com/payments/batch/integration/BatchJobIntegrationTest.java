package com.payments.batch.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.batch.domain.ProcessedPayment;
import com.payments.batch.domain.ProcessedPayment.ProcessingStatus;
import com.payments.batch.repository.ProcessedPaymentRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

/**
 * Integration test for the complete payment processing batch job.
 *
 * <p>Tests the end-to-end flow: file reading → validation → database persistence.
 *
 * @since PE-401
 */
@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@Sql(scripts = "/org/springframework/batch/core/schema-postgresql.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@DisplayName("Batch Job Integration Test")
class BatchJobIntegrationTest {

  @Autowired private JobLauncherTestUtils jobLauncherTestUtils;

  @Autowired private ProcessedPaymentRepository repository;

  @Autowired private Job paymentProcessingJob;

  private Path tempCsvFile;

  @BeforeEach
  void setUp() throws IOException {
    repository.deleteAll();
    jobLauncherTestUtils.setJob(paymentProcessingJob);
  }

  @AfterEach
  void tearDown() throws IOException {
    if (tempCsvFile != null && Files.exists(tempCsvFile)) {
      Files.delete(tempCsvFile);
    }
  }

  @Test
  @DisplayName("Should process valid payment CSV file successfully")
  void shouldProcessValidPaymentCsvFileSuccessfully() throws Exception {
    // Given
    String csvContent =
        """
            paymentId,debtorAccount,debtorName,creditorAccount,creditorName,amount,currency,paymentReference,valueDate,debtorBankCode,creditorBankCode,paymentType
            PAY001,12345678,John Doe,87654321,Jane Smith,1000.50,ZAR,Invoice 001,2025-12-31,ABSA,FNB,EFT
            PAY002,11111111,Alice Brown,22222222,Bob White,2500.00,ZAR,Invoice 002,2025-12-31,NEDBANK,CAPITEC,EFT
            PAY003,33333333,Charlie Green,44444444,Diana Black,750.25,ZAR,Invoice 003,2025-12-31,STANDARDBANK,ABSA,EFT
            """;

    tempCsvFile = Files.createTempFile("payments_", ".csv");
    Files.writeString(tempCsvFile, csvContent);

    JobParameters jobParameters =
        new JobParametersBuilder()
            .addString("filePath", tempCsvFile.toString())
            .addString("tenantId", "TENANT_TEST")
            .addString("delimiter", ",")
            .addString("skipHeader", "true")
            .addLong("time", System.currentTimeMillis()) // For uniqueness
            .toJobParameters();

    // When
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

    // Then
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

    List<ProcessedPayment> processedPayments = repository.findAll();
    assertThat(processedPayments).hasSize(3);
    assertThat(processedPayments)
        .allMatch(p -> p.getProcessingStatus() == ProcessingStatus.VALIDATED);
    assertThat(processedPayments).allMatch(p -> p.getTenantId().equals("TENANT_TEST"));
    assertThat(processedPayments).allMatch(p -> p.getValidationErrors() == null);
  }

  @Test
  @DisplayName("Should handle validation failures gracefully")
  void shouldHandleValidationFailuresGracefully() throws Exception {
    // Given - CSV with invalid data
    String csvContent =
        """
            paymentId,debtorAccount,debtorName,creditorAccount,creditorName,amount,currency,paymentReference,valueDate,debtorBankCode,creditorBankCode,paymentType
            PAY001,12345678,John Doe,87654321,Jane Smith,1000.50,ZAR,Invoice 001,2025-12-31,ABSA,FNB,EFT
            PAY002,INVALID,Alice Brown,22222222,Bob White,2500.00,ZAR,Invoice 002,2025-12-31,NEDBANK,CAPITEC,EFT
            PAY003,33333333,Charlie Green,44444444,Diana Black,-500.00,ZAR,Invoice 003,2025-12-31,STANDARDBANK,ABSA,EFT
            PAY004,,Missing Debtor,55555555,Test User,100.00,ZAR,Invoice 004,2025-12-31,ABSA,FNB,EFT
            """;

    tempCsvFile = Files.createTempFile("payments_invalid_", ".csv");
    Files.writeString(tempCsvFile, csvContent);

    JobParameters jobParameters =
        new JobParametersBuilder()
            .addString("filePath", tempCsvFile.toString())
            .addString("tenantId", "TENANT_TEST")
            .addString("delimiter", ",")
            .addString("skipHeader", "true")
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

    // When
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

    // Then
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    List<ProcessedPayment> processedPayments = repository.findAll();
    assertThat(processedPayments).hasSize(4);

    // PAY001 should be valid
    ProcessedPayment validPayment =
        processedPayments.stream()
            .filter(p -> p.getPaymentId().equals("PAY001"))
            .findFirst()
            .orElseThrow();
    assertThat(validPayment.getProcessingStatus()).isEqualTo(ProcessingStatus.VALIDATED);
    assertThat(validPayment.getValidationErrors()).isNull();

    // PAY002, PAY003, PAY004 should have validation errors
    List<ProcessedPayment> failedPayments =
        processedPayments.stream()
            .filter(p -> p.getProcessingStatus() == ProcessingStatus.VALIDATION_FAILED)
            .toList();
    assertThat(failedPayments).hasSize(3);
    assertThat(failedPayments).allMatch(p -> p.getValidationErrors() != null);
  }

  @Test
  @DisplayName("Should process large batch with custom chunk size")
  void shouldProcessLargeBatchWithCustomChunkSize() throws Exception {
    // Given - Generate larger CSV file
    StringBuilder csvContent = new StringBuilder();
    csvContent.append(
        "paymentId,debtorAccount,debtorName,creditorAccount,creditorName,amount,currency,paymentReference,valueDate,debtorBankCode,creditorBankCode,paymentType\n");

    for (int i = 1; i <= 100; i++) {
      csvContent.append(
          String.format(
              "PAY%03d,12345678,Debtor %d,87654321,Creditor %d,%.2f,ZAR,Invoice %03d,2025-12-31,ABSA,FNB,EFT\n",
              i, i, i, 100.0 * i, i));
    }

    tempCsvFile = Files.createTempFile("payments_large_", ".csv");
    Files.writeString(tempCsvFile, csvContent.toString());

    JobParameters jobParameters =
        new JobParametersBuilder()
            .addString("filePath", tempCsvFile.toString())
            .addString("tenantId", "TENANT_TEST")
            .addString("delimiter", ",")
            .addString("skipHeader", "true")
            .addLong("chunkSize", 25L) // Custom chunk size
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

    // When
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

    // Then
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    List<ProcessedPayment> processedPayments = repository.findAll();
    assertThat(processedPayments).hasSize(100);
    assertThat(processedPayments)
        .allMatch(p -> p.getProcessingStatus() == ProcessingStatus.VALIDATED);
  }

  @Test
  @DisplayName("Should track line numbers correctly")
  void shouldTrackLineNumbersCorrectly() throws Exception {
    // Given
    String csvContent =
        """
            paymentId,debtorAccount,debtorName,creditorAccount,creditorName,amount,currency,paymentReference,valueDate,debtorBankCode,creditorBankCode,paymentType
            PAY001,12345678,John Doe,87654321,Jane Smith,1000.50,ZAR,Invoice 001,2025-12-31,ABSA,FNB,EFT
            PAY002,11111111,Alice Brown,22222222,Bob White,2500.00,ZAR,Invoice 002,2025-12-31,NEDBANK,CAPITEC,EFT
            """;

    tempCsvFile = Files.createTempFile("payments_", ".csv");
    Files.writeString(tempCsvFile, csvContent);

    JobParameters jobParameters =
        new JobParametersBuilder()
            .addString("filePath", tempCsvFile.toString())
            .addString("tenantId", "TENANT_TEST")
            .addString("delimiter", ",")
            .addString("skipHeader", "true")
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();

    // When
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

    // Then
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    List<ProcessedPayment> processedPayments = repository.findByTenantId("TENANT_TEST");
    assertThat(processedPayments).hasSize(2);

    // Line numbers should be 2 and 3 (1 is header)
    assertThat(processedPayments).extracting(ProcessedPayment::getLineNumber).contains(2, 3);
  }
}
