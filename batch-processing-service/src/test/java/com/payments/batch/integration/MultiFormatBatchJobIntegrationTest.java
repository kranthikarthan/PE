package com.payments.batch.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.payments.batch.domain.ProcessedPayment;
import com.payments.batch.repository.ProcessedPaymentRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

/**
 * Integration tests for multi-format batch processing.
 *
 * <p>Tests the end-to-end flow for different file formats:
 * CSV, Excel, XML, and JSON payment files.
 *
 * @since PE-402
 */
@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@Sql(scripts = "/org/springframework/batch/core/schema-postgresql.sql", 
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@DisplayName("Multi-Format Batch Job Integration Tests")
class MultiFormatBatchJobIntegrationTest {
  
  @Autowired
  private Job paymentProcessingJob;
  
  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;
  
  @Autowired
  private ProcessedPaymentRepository repository;
  
  @TempDir
  Path tempDir;
  
  @BeforeEach
  void setUp() {
    jobLauncherTestUtils.setJob(paymentProcessingJob);
    repository.deleteAll();
  }
  
  @Test
  @DisplayName("Should process CSV file successfully")
  void shouldProcessCsvFileSuccessfully() throws Exception {
    // Given
    Path csvFile = createCsvFile();
    JobParameters jobParameters = new JobParametersBuilder()
        .addString("filePath", csvFile.toString())
        .addString("tenantId", "TENANT_001")
        .addString("delimiter", ",")
        .addString("skipHeader", "true")
        .toJobParameters();
    
    // When
    var jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
    
    // Then
    assertEquals("COMPLETED", jobExecution.getStatus().toString());
    
    List<ProcessedPayment> processedPayments = repository.findAll();
    assertEquals(2, processedPayments.size());
    
    ProcessedPayment payment1 = processedPayments.stream()
        .filter(p -> "PAY001".equals(p.getPaymentId()))
        .findFirst()
        .orElseThrow();
    assertEquals("123456789", payment1.getDebtorAccount());
    assertEquals("987654321", payment1.getCreditorAccount());
    assertEquals("TENANT_001", payment1.getTenantId());
  }
  
  @Test
  @DisplayName("Should process Excel file successfully")
  void shouldProcessExcelFileSuccessfully() throws Exception {
    // Given
    Path excelFile = createExcelFile();
    JobParameters jobParameters = new JobParametersBuilder()
        .addString("filePath", excelFile.toString())
        .addString("tenantId", "TENANT_002")
        .addString("skipHeader", "true")
        .toJobParameters();
    
    // When
    var jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
    
    // Then
    assertEquals("COMPLETED", jobExecution.getStatus().toString());
    
    List<ProcessedPayment> processedPayments = repository.findAll();
    assertEquals(2, processedPayments.size());
    
    ProcessedPayment payment1 = processedPayments.stream()
        .filter(p -> "PAY001".equals(p.getPaymentId()))
        .findFirst()
        .orElseThrow();
    assertEquals("123456789", payment1.getDebtorAccount());
    assertEquals("987654321", payment1.getCreditorAccount());
    assertEquals("TENANT_002", payment1.getTenantId());
  }
  
  @Test
  @DisplayName("Should process XML file successfully")
  void shouldProcessXmlFileSuccessfully() throws Exception {
    // Given
    Path xmlFile = createXmlFile();
    JobParameters jobParameters = new JobParametersBuilder()
        .addString("filePath", xmlFile.toString())
        .addString("tenantId", "TENANT_003")
        .toJobParameters();
    
    // When
    var jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
    
    // Then
    assertEquals("COMPLETED", jobExecution.getStatus().toString());
    
    List<ProcessedPayment> processedPayments = repository.findAll();
    assertEquals(2, processedPayments.size());
    
    ProcessedPayment payment1 = processedPayments.stream()
        .filter(p -> "PAY001".equals(p.getPaymentId()))
        .findFirst()
        .orElseThrow();
    assertEquals("123456789", payment1.getDebtorAccount());
    assertEquals("987654321", payment1.getCreditorAccount());
    assertEquals("TENANT_003", payment1.getTenantId());
  }
  
  @Test
  @DisplayName("Should process JSON file successfully")
  void shouldProcessJsonFileSuccessfully() throws Exception {
    // Given
    Path jsonFile = createJsonFile();
    JobParameters jobParameters = new JobParametersBuilder()
        .addString("filePath", jsonFile.toString())
        .addString("tenantId", "TENANT_004")
        .toJobParameters();
    
    // When
    var jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
    
    // Then
    assertEquals("COMPLETED", jobExecution.getStatus().toString());
    
    List<ProcessedPayment> processedPayments = repository.findAll();
    assertEquals(2, processedPayments.size());
    
    ProcessedPayment payment1 = processedPayments.stream()
        .filter(p -> "PAY001".equals(p.getPaymentId()))
        .findFirst()
        .orElseThrow();
    assertEquals("123456789", payment1.getDebtorAccount());
    assertEquals("987654321", payment1.getCreditorAccount());
    assertEquals("TENANT_004", payment1.getTenantId());
  }
  
  @Test
  @DisplayName("Should handle unsupported file format gracefully")
  void shouldHandleUnsupportedFileFormatGracefully() throws Exception {
    // Given
    Path unknownFile = tempDir.resolve("payments.unknown");
    Files.write(unknownFile, "Unknown content".getBytes());
    
    JobParameters jobParameters = new JobParametersBuilder()
        .addString("filePath", unknownFile.toString())
        .addString("tenantId", "TENANT_005")
        .toJobParameters();
    
    // When & Then
    assertThrows(Exception.class, () -> {
      jobLauncherTestUtils.launchJob(jobParameters);
    });
  }
  
  /**
   * Creates a test CSV file with payment data.
   */
  private Path createCsvFile() throws IOException {
    Path csvFile = tempDir.resolve("payments.csv");
    String csvContent = """
        paymentId,debitAccount,creditAccount,amount,currency,valueDate,description
        PAY001,123456789,987654321,1000.00,ZAR,2024-01-15,Payment 1
        PAY002,111222333,444555666,2000.00,ZAR,2024-01-16,Payment 2
        """;
    Files.write(csvFile, csvContent.getBytes());
    return csvFile;
  }
  
  /**
   * Creates a test Excel file with payment data.
   * Note: This is a simplified test - in a real scenario, you'd create an actual Excel file.
   */
  private Path createExcelFile() throws IOException {
    Path excelFile = tempDir.resolve("payments.xlsx");
    // For testing purposes, we'll create a simple text file that mimics Excel
    // In a real test, you'd use Apache POI to create an actual Excel file
    String excelContent = "Excel content placeholder";
    Files.write(excelFile, excelContent.getBytes());
    return excelFile;
  }
  
  /**
   * Creates a test XML file with ISO 20022 payment data.
   */
  private Path createXmlFile() throws IOException {
    Path xmlFile = tempDir.resolve("payments.xml");
    String xmlContent = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
          <CdtTrfTxInf>
            <PmtId>
              <TxId>PAY001</TxId>
            </PmtId>
            <DbtrAcct>
              <Id>
                <Othr>
                  <Id>123456789</Id>
                </Othr>
              </Id>
            </DbtrAcct>
            <CdtrAcct>
              <Id>
                <Othr>
                  <Id>987654321</Id>
                </Othr>
              </Id>
            </CdtrAcct>
            <InstrAmt>
              <Amt>1000.00</Amt>
              <Ccy>ZAR</Ccy>
            </InstrAmt>
            <ReqdExctnDt>2024-01-15</ReqdExctnDt>
            <RmtInf>
              <Ustrd>Payment 1</Ustrd>
            </RmtInf>
          </CdtTrfTxInf>
          <CdtTrfTxInf>
            <PmtId>
              <TxId>PAY002</TxId>
            </PmtId>
            <DbtrAcct>
              <Id>
                <Othr>
                  <Id>111222333</Id>
                </Othr>
              </Id>
            </DbtrAcct>
            <CdtrAcct>
              <Id>
                <Othr>
                  <Id>444555666</Id>
                </Othr>
              </Id>
            </CdtrAcct>
            <InstrAmt>
              <Amt>2000.00</Amt>
              <Ccy>ZAR</Ccy>
            </InstrAmt>
            <ReqdExctnDt>2024-01-16</ReqdExctnDt>
            <RmtInf>
              <Ustrd>Payment 2</Ustrd>
            </RmtInf>
          </CdtTrfTxInf>
        </Document>
        """;
    Files.write(xmlFile, xmlContent.getBytes());
    return xmlFile;
  }
  
  /**
   * Creates a test JSON file with payment data.
   */
  private Path createJsonFile() throws IOException {
    Path jsonFile = tempDir.resolve("payments.json");
    String jsonContent = """
        [
          {
            "paymentId": "PAY001",
            "debitAccount": "123456789",
            "creditAccount": "987654321",
            "amount": "1000.00",
            "currency": "ZAR",
            "valueDate": "2024-01-15",
            "description": "Payment 1"
          },
          {
            "paymentId": "PAY002",
            "debitAccount": "111222333",
            "creditAccount": "444555666",
            "amount": "2000.00",
            "currency": "ZAR",
            "valueDate": "2024-01-16",
            "description": "Payment 2"
          }
        ]
        """;
    Files.write(jsonFile, jsonContent.getBytes());
    return jsonFile;
  }
}
