# Batch Processing - Bulk Payment Files

## Overview

**Batch Processing** enables the Payments Engine to process large volumes of payments efficiently through file-based processing. This is critical for:
- **Corporate clients**: Salary runs, supplier payments, bulk transfers
- **Clearing systems**: EFT batch files, settlement files
- **Scheduled payments**: Standing orders, recurring payments

This document describes the complete batch processing architecture.

---

## What is Batch Processing?

### Use Cases

```
Batch Processing Use Cases:
├─ Inbound (Client → Engine):
│   ├─ Salary payments (10,000+ employees)
│   ├─ Supplier payments (bulk AP)
│   ├─ Dividend payments
│   ├─ Loan disbursements
│   └─ Standing orders
│
├─ Outbound (Engine → Clearing):
│   ├─ EFT batch files (daily submission)
│   ├─ Debit order files (DebiCheck)
│   ├─ PayShap bulk submissions
│   └─ Settlement files (SAMOS)
│
└─ Internal (Engine operations):
    ├─ End-of-day processing
    ├─ Reconciliation batches
    ├─ Report generation
    └─ Archival processes
```

### Batch vs Real-Time

| Aspect | Batch Processing | Real-Time Processing |
|--------|------------------|---------------------|
| **Volume** | High (1000s-100Ks) | Low (1-10) |
| **Latency** | Minutes-Hours | Milliseconds-Seconds |
| **Cost** | Lower per transaction | Higher per transaction |
| **Complexity** | File parsing, validation | API validation |
| **Use Case** | Scheduled, bulk | Immediate, single |
| **Throughput** | 10K-100K txns/min | 100-1000 txns/sec |

---

## Architecture

### Batch Processing Service

```
┌──────────────────────────────────────────────────────────────────┐
│                    Payments Engine                                │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  File Upload API / SFTP Server                                   │
│         │                                                         │
│         ├──> Batch Processing Service                            │
│         │         │                                               │
│         │         ├──> File Parser                               │
│         │         │    (CSV, Excel, XML, JSON, ISO 8583)         │
│         │         │                                               │
│         │         ├──> Validation Engine                         │
│         │         │    (schema, limits, duplicates)              │
│         │         │                                               │
│         │         ├──> Batch Job Orchestrator                    │
│         │         │    (Spring Batch)                            │
│         │         │         │                                     │
│         │         │         ├──> Payment Initiation (chunks)     │
│         │         │         │                                     │
│         │         │         ├──> Parallel Processing             │
│         │         │         │    (10-20 threads)                 │
│         │         │         │                                     │
│         │         │         └──> Progress Tracking               │
│         │         │                                               │
│         │         ├──> Result Aggregator                         │
│         │         │    (success/failure counts)                  │
│         │         │                                               │
│         │         └──> Report Generator                          │
│         │              (CSV, PDF results)                        │
│         │                                                         │
│         └──> Notification Service                                │
│                (batch completion notification)                    │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

### Processing Flow

```
Client                 Batch Service              Payment Service
  │                         │                           │
  │ 1. Upload batch file    │                           │
  ├────────────────────────>│                           │
  │                         │ 2. Validate file          │
  │                         │    (format, schema)       │
  │                         │                           │
  │                         │ 3. Parse into records     │
  │                         │    (10,000 payments)      │
  │                         │                           │
  │                         │ 4. Create batch job       │
  │                         │    (Spring Batch)         │
  │                         │                           │
  │                         │ 5. Process in chunks      │
  │                         │    (500 records/chunk)    │
  │                         ├──────────────────────────>│
  │                         │                           │ 6. Process payment
  │                         │                           │    (validate, route,
  │                         │                           │     execute)
  │                         │<──────────────────────────┤
  │                         │ 7. Aggregate results      │
  │                         │    (9,950 success,        │
  │                         │     50 failed)            │
  │                         │                           │
  │                         │ 8. Generate report        │
  │                         │    (results.csv)          │
  │                         │                           │
  │ 9. Notify completion    │                           │
  │<────────────────────────┤                           │
  │    (email + download    │                           │
  │     link)               │                           │
  │                         │                           │
```

---

## File Formats

### 1. CSV Format (Simple)

**Purpose**: Standard format for bulk payments

```csv
# salary_payments_2025_10_11.csv
FromAccount,ToAccount,Amount,Currency,Reference,BeneficiaryName
1234567890,9876543210,10000.00,ZAR,Salary Oct 2025,John Doe
1234567890,5555555555,12500.00,ZAR,Salary Oct 2025,Jane Smith
1234567890,7777777777,15000.00,ZAR,Salary Oct 2025,Bob Johnson
...
```

**Schema**:
```json
{
  "format": "CSV",
  "delimiter": ",",
  "hasHeader": true,
  "columns": [
    {"name": "FromAccount", "type": "string", "required": true},
    {"name": "ToAccount", "type": "string", "required": true},
    {"name": "Amount", "type": "decimal", "required": true},
    {"name": "Currency", "type": "string", "required": true},
    {"name": "Reference", "type": "string", "required": false},
    {"name": "BeneficiaryName", "type": "string", "required": false}
  ]
}
```

### 2. Excel Format (XLSX)

**Purpose**: User-friendly format for corporate clients

```
| From Account | To Account  | Amount    | Currency | Reference        | Beneficiary    |
|--------------|-------------|-----------|----------|------------------|----------------|
| 1234567890   | 9876543210  | 10,000.00 | ZAR      | Salary Oct 2025  | John Doe       |
| 1234567890   | 5555555555  | 12,500.00 | ZAR      | Salary Oct 2025  | Jane Smith     |
| 1234567890   | 7777777777  | 15,000.00 | ZAR      | Salary Oct 2025  | Bob Johnson    |
```

### 3. XML Format (ISO 20022)

**Purpose**: Standard pain.001 format (Customer Credit Transfer Initiation)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.09">
  <CstmrCdtTrfInitn>
    <!-- Group Header -->
    <GrpHdr>
      <MsgId>BATCH-2025-10-11-001</MsgId>
      <CreDtTm>2025-10-11T08:00:00Z</CreDtTm>
      <NbOfTxs>10000</NbOfTxs>
      <CtrlSum>125000000.00</CtrlSum>
      <InitgPty>
        <Nm>Acme Corporation</Nm>
      </InitgPty>
    </GrpHdr>
    
    <!-- Payment Information -->
    <PmtInf>
      <PmtInfId>PMT-INFO-001</PmtInfId>
      <PmtMtd>TRF</PmtMtd>
      <BtchBookg>true</BtchBookg>
      <NbOfTxs>10000</NbOfTxs>
      <CtrlSum>125000000.00</CtrlSum>
      <ReqdExctnDt>
        <Dt>2025-10-15</Dt>
      </ReqdExctnDt>
      
      <Dbtr>
        <Nm>Acme Corporation</Nm>
      </Dbtr>
      
      <DbtrAcct>
        <Id>
          <Othr>
            <Id>1234567890</Id>
          </Othr>
        </Id>
      </DbtrAcct>
      
      <!-- Credit Transfer Transactions (10,000 records) -->
      <CdtTrfTxInf>
        <PmtId>
          <EndToEndId>SALARY-0001</EndToEndId>
        </PmtId>
        <Amt>
          <InstdAmt Ccy="ZAR">10000.00</InstdAmt>
        </Amt>
        <Cdtr>
          <Nm>John Doe</Nm>
        </Cdtr>
        <CdtrAcct>
          <Id>
            <Othr>
              <Id>9876543210</Id>
            </Othr>
          </Id>
        </CdtrAcct>
        <RmtInf>
          <Ustrd>Salary Oct 2025</Ustrd>
        </RmtInf>
      </CdtTrfTxInf>
      
      <!-- ... 9,999 more transactions ... -->
    </PmtInf>
  </CstmrCdtTrfInitn>
</Document>
```

### 4. JSON Format (API-Friendly)

**Purpose**: Modern API-based batch submission

```json
{
  "batchId": "BATCH-2025-10-11-001",
  "submittedBy": "acme-corp",
  "submittedAt": "2025-10-11T08:00:00Z",
  "executionDate": "2025-10-15",
  "debtorAccount": "1234567890",
  "totalAmount": 125000000.00,
  "currency": "ZAR",
  "payments": [
    {
      "id": "SALARY-0001",
      "creditorAccount": "9876543210",
      "creditorName": "John Doe",
      "amount": 10000.00,
      "reference": "Salary Oct 2025"
    },
    {
      "id": "SALARY-0002",
      "creditorAccount": "5555555555",
      "creditorName": "Jane Smith",
      "amount": 12500.00,
      "reference": "Salary Oct 2025"
    }
    // ... 9,998 more payments ...
  ]
}
```

---

## Implementation

### Batch Processing Service (Spring Batch)

```java
@Service
@Slf4j
public class BatchProcessingService {
    
    private final JobLauncher jobLauncher;
    private final Job paymentBatchJob;
    private final BatchJobRepository batchRepo;
    private final FileParser fileParser;
    
    /**
     * Submit batch file for processing.
     */
    @Async
    public CompletableFuture<BatchJobResult> submitBatch(
        MultipartFile file, 
        BatchSubmissionRequest request
    ) {
        log.info("Submitting batch: filename={}, tenantId={}", 
            file.getOriginalFilename(), request.getTenantId());
        
        // Step 1: Save file to storage
        String filePath = saveFile(file);
        
        // Step 2: Validate file format
        FileFormat format = detectFormat(file);
        validateFileFormat(file, format);
        
        // Step 3: Parse file and count records
        List<PaymentRecord> records = fileParser.parse(filePath, format);
        log.info("Parsed {} payment records", records.size());
        
        // Step 4: Validate batch limits
        validateBatchLimits(records, request);
        
        // Step 5: Create batch job
        BatchJob batchJob = BatchJob.builder()
            .batchId(UUID.randomUUID())
            .tenantId(request.getTenantId())
            .filename(file.getOriginalFilename())
            .format(format)
            .totalRecords(records.size())
            .status(BatchStatus.PENDING)
            .submittedBy(request.getSubmittedBy())
            .submittedAt(Instant.now())
            .executionDate(request.getExecutionDate())
            .build();
        
        batchJob = batchRepo.save(batchJob);
        
        // Step 6: Launch Spring Batch job (async)
        JobParameters jobParams = new JobParametersBuilder()
            .addString("batchId", batchJob.getBatchId().toString())
            .addString("filePath", filePath)
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();
        
        try {
            JobExecution execution = jobLauncher.run(paymentBatchJob, jobParams);
            
            log.info("Batch job launched: batchId={}, executionId={}", 
                batchJob.getBatchId(), execution.getId());
            
            return CompletableFuture.completedFuture(
                BatchJobResult.builder()
                    .batchId(batchJob.getBatchId())
                    .status(BatchStatus.PROCESSING)
                    .totalRecords(records.size())
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Failed to launch batch job: batchId={}", 
                batchJob.getBatchId(), e);
            
            batchJob.setStatus(BatchStatus.FAILED);
            batchJob.setErrorMessage(e.getMessage());
            batchRepo.save(batchJob);
            
            return CompletableFuture.completedFuture(
                BatchJobResult.builder()
                    .batchId(batchJob.getBatchId())
                    .status(BatchStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .build()
            );
        }
    }
    
    private void validateBatchLimits(
        List<PaymentRecord> records, 
        BatchSubmissionRequest request
    ) {
        // Max records per batch
        if (records.size() > 100_000) {
            throw new BatchLimitExceededException(
                "Maximum 100,000 payments per batch");
        }
        
        // Total amount limit
        BigDecimal totalAmount = records.stream()
            .map(PaymentRecord::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalAmount.compareTo(new BigDecimal("100000000")) > 0) {
            throw new BatchLimitExceededException(
                "Maximum R100M total per batch");
        }
    }
}
```

### Spring Batch Job Configuration

```java
@Configuration
@EnableBatchProcessing
public class BatchJobConfiguration {
    
    @Bean
    public Job paymentBatchJob(
        JobBuilderFactory jobs,
        Step processPaymentsStep
    ) {
        return jobs.get("paymentBatchJob")
            .incrementer(new RunIdIncrementer())
            .start(processPaymentsStep)
            .listener(batchJobListener())
            .build();
    }
    
    @Bean
    public Step processPaymentsStep(
        StepBuilderFactory steps,
        ItemReader<PaymentRecord> reader,
        ItemProcessor<PaymentRecord, Payment> processor,
        ItemWriter<Payment> writer
    ) {
        return steps.get("processPaymentsStep")
            .<PaymentRecord, Payment>chunk(500)  // 500 records per chunk
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            .skipLimit(100)  // Skip up to 100 failed records
            .skip(PaymentValidationException.class)
            .retryLimit(3)
            .retry(TransientException.class)
            .listener(stepExecutionListener())
            .taskExecutor(taskExecutor())  // Parallel processing
            .throttleLimit(10)  // Max 10 threads
            .build();
    }
    
    @Bean
    public ItemReader<PaymentRecord> paymentRecordReader(
        @Value("#{jobParameters['filePath']}") String filePath
    ) {
        return new FlatFileItemReaderBuilder<PaymentRecord>()
            .name("paymentRecordReader")
            .resource(new FileSystemResource(filePath))
            .delimited()
            .names("fromAccount", "toAccount", "amount", "currency", "reference")
            .targetType(PaymentRecord.class)
            .build();
    }
    
    @Bean
    public ItemProcessor<PaymentRecord, Payment> paymentProcessor() {
        return record -> {
            // Validate record
            validatePaymentRecord(record);
            
            // Convert to Payment entity
            Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID())
                .fromAccount(record.getFromAccount())
                .toAccount(record.getToAccount())
                .amount(Money.of(record.getAmount(), record.getCurrency()))
                .reference(record.getReference())
                .status(PaymentStatus.PENDING)
                .source(PaymentSource.BATCH)
                .build();
            
            return payment;
        };
    }
    
    @Bean
    public ItemWriter<Payment> paymentWriter(PaymentService paymentService) {
        return payments -> {
            // Process batch of payments
            for (Payment payment : payments) {
                try {
                    paymentService.initiatePayment(payment);
                } catch (Exception e) {
                    log.error("Failed to process payment: {}", 
                        payment.getPaymentId(), e);
                    // Skip and continue (fault-tolerant)
                }
            }
        };
    }
    
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("batch-");
        executor.initialize();
        return executor;
    }
}
```

### Batch Job Listener

```java
@Component
@Slf4j
public class BatchJobListener implements JobExecutionListener {
    
    private final BatchJobRepository batchRepo;
    private final NotificationService notificationService;
    
    @Override
    public void beforeJob(JobExecution execution) {
        String batchId = execution.getJobParameters().getString("batchId");
        log.info("Starting batch job: batchId={}", batchId);
        
        // Update status to PROCESSING
        BatchJob batchJob = batchRepo.findByBatchId(UUID.fromString(batchId));
        batchJob.setStatus(BatchStatus.PROCESSING);
        batchJob.setStartedAt(Instant.now());
        batchRepo.save(batchJob);
    }
    
    @Override
    public void afterJob(JobExecution execution) {
        String batchId = execution.getJobParameters().getString("batchId");
        BatchJob batchJob = batchRepo.findByBatchId(UUID.fromString(batchId));
        
        // Collect statistics
        int totalRecords = batchJob.getTotalRecords();
        int processed = execution.getStepExecutions().stream()
            .mapToInt(StepExecution::getWriteCount)
            .sum();
        int failed = totalRecords - processed;
        
        // Update batch job
        batchJob.setProcessedRecords(processed);
        batchJob.setSuccessCount(processed - execution.getStepExecutions().stream()
            .mapToInt(StepExecution::getSkipCount).sum());
        batchJob.setFailedCount(failed);
        batchJob.setStatus(execution.getStatus() == BatchStatus.COMPLETED 
            ? BatchStatus.COMPLETED : BatchStatus.FAILED);
        batchJob.setCompletedAt(Instant.now());
        batchJob.setDuration(
            Duration.between(batchJob.getStartedAt(), batchJob.getCompletedAt())
        );
        
        batchRepo.save(batchJob);
        
        log.info("Batch job completed: batchId={}, processed={}, failed={}, duration={}", 
            batchId, processed, failed, batchJob.getDuration());
        
        // Generate result report
        generateResultReport(batchJob, execution);
        
        // Send notification
        notificationService.sendBatchCompletionNotification(batchJob);
    }
    
    private void generateResultReport(BatchJob batchJob, JobExecution execution) {
        // Generate CSV report with success/failure details
        // Store in Azure Blob Storage
        // Provide download link to user
    }
}
```

---

## Database Schema

```sql
-- Batch Jobs
CREATE TABLE batch_jobs (
    batch_id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    filename VARCHAR(500) NOT NULL,
    file_format VARCHAR(20) NOT NULL,  -- CSV, XLSX, XML, JSON
    file_path TEXT NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    
    -- Submission
    submitted_by VARCHAR(200) NOT NULL,
    submitted_at TIMESTAMP NOT NULL,
    execution_date DATE,  -- For scheduled batches
    
    -- Processing
    status VARCHAR(20) NOT NULL,  -- PENDING, PROCESSING, COMPLETED, FAILED
    total_records INT NOT NULL,
    processed_records INT,
    success_count INT,
    failed_count INT,
    
    -- Timing
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    duration_seconds INT,
    
    -- Results
    result_file_path TEXT,
    error_message TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_status (status),
    INDEX idx_submitted_at (submitted_at),
    INDEX idx_execution_date (execution_date)
);

-- Batch Payment Records (for tracking)
CREATE TABLE batch_payment_records (
    id BIGSERIAL PRIMARY KEY,
    batch_id UUID NOT NULL REFERENCES batch_jobs(batch_id),
    record_number INT NOT NULL,
    payment_id UUID,  -- If successfully created
    
    -- Record data
    from_account VARCHAR(50) NOT NULL,
    to_account VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    reference VARCHAR(200),
    
    -- Status
    status VARCHAR(20) NOT NULL,  -- SUCCESS, FAILED, SKIPPED
    error_code VARCHAR(50),
    error_message TEXT,
    
    processed_at TIMESTAMP,
    
    INDEX idx_batch_id (batch_id),
    INDEX idx_status (status),
    INDEX idx_payment_id (payment_id)
);
```

---

## File Upload Options

### 1. REST API Upload

```java
@RestController
@RequestMapping("/api/v1/batch")
public class BatchController {
    
    private final BatchProcessingService batchService;
    
    @PostMapping("/upload")
    public ResponseEntity<BatchJobResponse> uploadBatch(
        @RequestParam("file") MultipartFile file,
        @RequestParam("executionDate") @DateTimeFormat(iso = ISO.DATE) LocalDate executionDate,
        @RequestHeader("X-Tenant-ID") String tenantId
    ) {
        // Validate file
        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }
        if (file.getSize() > 100_000_000) {  // 100MB limit
            throw new FileTooLargeException("Max file size is 100MB");
        }
        
        // Submit batch
        BatchSubmissionRequest request = BatchSubmissionRequest.builder()
            .tenantId(UUID.fromString(tenantId))
            .executionDate(executionDate)
            .submittedBy(SecurityContextHolder.getContext()
                .getAuthentication().getName())
            .build();
        
        CompletableFuture<BatchJobResult> result = 
            batchService.submitBatch(file, request);
        
        BatchJobResult jobResult = result.join();
        
        return ResponseEntity.accepted()
            .body(BatchJobResponse.builder()
                .batchId(jobResult.getBatchId())
                .status(jobResult.getStatus())
                .totalRecords(jobResult.getTotalRecords())
                .message("Batch submitted successfully")
                .build());
    }
    
    @GetMapping("/{batchId}/status")
    public ResponseEntity<BatchJobStatus> getBatchStatus(
        @PathVariable UUID batchId
    ) {
        BatchJob batchJob = batchService.getBatchJob(batchId);
        
        return ResponseEntity.ok(BatchJobStatus.builder()
            .batchId(batchJob.getBatchId())
            .status(batchJob.getStatus())
            .totalRecords(batchJob.getTotalRecords())
            .processedRecords(batchJob.getProcessedRecords())
            .successCount(batchJob.getSuccessCount())
            .failedCount(batchJob.getFailedCount())
            .progress(calculateProgress(batchJob))
            .build());
    }
    
    @GetMapping("/{batchId}/download-results")
    public ResponseEntity<Resource> downloadResults(
        @PathVariable UUID batchId
    ) {
        BatchJob batchJob = batchService.getBatchJob(batchId);
        
        if (batchJob.getResultFilePath() == null) {
            throw new ResultsNotReadyException("Results not available yet");
        }
        
        Resource file = batchService.loadResultFile(
            batchJob.getResultFilePath()
        );
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=batch_results_" + batchId + ".csv")
            .body(file);
    }
}
```

### 2. SFTP Server

```yaml
# SFTP Configuration
sftp:
  enabled: true
  host: 0.0.0.0
  port: 2222
  root-directory: /data/sftp
  
  users:
    - username: acme-corp
      password: ${SFTP_PASSWORD_ACME}
      home-directory: /acme-corp
      upload-directory: /acme-corp/upload
      permissions: [READ, WRITE]
```

```java
@Component
@Slf4j
public class SftpBatchMonitor {
    
    @Scheduled(fixedDelay = 60000)  // Every minute
    public void monitorSftpUploads() {
        // Scan SFTP upload directories
        // Auto-submit new files for processing
        // Move processed files to archive
    }
}
```

---

## Configuration

```yaml
# application.yml
batch:
  enabled: true
  
  limits:
    max-records-per-batch: 100000
    max-batch-size-mb: 100
    max-total-amount: 100000000.00  # R100M
    
  processing:
    chunk-size: 500
    thread-pool-size: 10
    max-threads: 20
    skip-limit: 100
    retry-limit: 3
    
  file-formats:
    supported: [CSV, XLSX, XML, JSON]
    csv:
      delimiter: ","
      has-header: true
    
  storage:
    upload-directory: /data/batch/upload
    archive-directory: /data/batch/archive
    result-directory: /data/batch/results
    retention-days: 90
    
  scheduling:
    enabled: true
    cron: "0 0 2 * * *"  # 2 AM daily
```

---

## Monitoring & Alerts

### Metrics

```yaml
batch.jobs.total:
  type: counter
  tags: [status, format]
  
batch.jobs.duration:
  type: timer
  tags: [status]
  
batch.records.processed.total:
  type: counter
  tags: [batch_id, status]
  
batch.jobs.active:
  type: gauge
```

### Alerts

```yaml
- alert: BatchJobStuck
  expr: batch_jobs_active > 0 AND time() - batch_job_started_timestamp > 3600
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: Batch job running for over 1 hour
```

---

## Summary

### Key Features

✅ **Spring Batch**: Enterprise-grade batch processing  
✅ **Multiple Formats**: CSV, Excel, XML, JSON  
✅ **Parallel Processing**: 10-20 threads, 10K-100K txns/min  
✅ **Fault Tolerant**: Skip failed records, retry transient errors  
✅ **Large Files**: Up to 100K records, 100MB files  
✅ **Progress Tracking**: Real-time status updates  
✅ **Result Reports**: Download success/failure details  
✅ **SFTP Support**: Automated file pickup  

### Performance

- **Throughput**: 10K-100K transactions/minute
- **Latency**: Minutes to hours (not real-time)
- **Scalability**: Horizontal (add more workers)
- **Reliability**: Fault-tolerant, retryable

---

**Last Updated**: 2025-10-11  
**Version**: 1.0  
**Classification**: Internal
