package com.payments.batch.config;

import com.payments.batch.domain.PaymentRecord;
import com.payments.batch.domain.ProcessedPayment;
import com.payments.batch.format.FileFormatFactory;
import com.payments.batch.processor.PaymentItemProcessor;
import com.payments.batch.reader.PaymentItemReader;
import com.payments.batch.reader.PaymentItemReaderAdapter;
import com.payments.batch.repository.ProcessedPaymentRepository;
import com.payments.batch.writer.PaymentItemWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch job configuration for processing bulk payment files.
 *
 * <p>This configuration defines a chunk-oriented batch job that reads payment records from files,
 * validates and transforms them, and persists the results to the database.
 *
 * <p><b>Job Parameters:</b>
 *
 * <ul>
 *   <li><b>filePath</b>: Path to the payment file to process (required)
 *   <li><b>tenantId</b>: Tenant identifier for multi-tenancy support (required)
 *   <li><b>delimiter</b>: CSV field delimiter (optional, default: ",")
 *   <li><b>skipHeader</b>: Whether to skip the first line (optional, default: true)
 *   <li><b>chunkSize</b>: Number of records to process per chunk (optional, default: 1000)
 * </ul>
 *
 * <p><b>Job Flow:</b>
 *
 * <ol>
 *   <li>Read payment records from file (CSV) using PaymentItemReader
 *   <li>Validate and transform each record using PaymentItemProcessor
 *   <li>Write processed payments to database using PaymentItemWriter
 *   <li>Commit in chunks for transaction efficiency
 * </ol>
 *
 * @see PaymentItemReader
 * @see PaymentItemProcessor
 * @see PaymentItemWriter
 * @since PE-401
 */
@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchJobConfiguration {

  private static final int DEFAULT_CHUNK_SIZE = 1000;
  private static final String DEFAULT_DELIMITER = ",";

  /**
   * Defines the main payment processing batch job.
   *
   * @param jobRepository the Spring Batch job repository
   * @param paymentProcessingStep the payment processing step
   * @return configured batch job
   */
  @Bean(name = "paymentProcessingJob")
  @Primary
  public Job paymentProcessingJob(JobRepository jobRepository, Step paymentProcessingStep) {
    return new JobBuilder("paymentProcessingJob", jobRepository)
        .incrementer(new RunIdIncrementer()) // Automatically increment run ID
        .start(paymentProcessingStep)
        .build();
  }

  /**
   * Defines the payment processing step.
   *
   * <p>This step performs chunk-oriented processing with configurable chunk size. Each chunk is
   * processed in a separate transaction, allowing for efficient rollback on errors without losing
   * the entire batch.
   *
   * @param jobRepository the Spring Batch job repository
   * @param transactionManager the transaction manager
   * @param reader the payment item reader
   * @param processor the payment item processor
   * @param writer the payment item writer
   * @param chunkSize the chunk size (from application properties, default: 1000)
   * @return configured step
   */
  @Bean
  public Step paymentProcessingStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      PaymentItemReader reader,
      PaymentItemProcessor processor,
      PaymentItemWriter writer,
      @Value("${batch.processing.chunk-size:" + DEFAULT_CHUNK_SIZE + "}") Integer chunkSize) {

    log.info("Configuring payment processing step with chunk size: {}", chunkSize);

    return new StepBuilder("paymentProcessingStep", jobRepository)
        .<PaymentRecord, ProcessedPayment>chunk(chunkSize, transactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
  }

  /**
   * Creates a step-scoped PaymentItemReader using file format detection.
   *
   * <p>Step-scoped beans are created for each step execution, allowing job parameters to be
   * injected at runtime. This enhanced version automatically detects the file format
   * and creates the appropriate reader (CSV, Excel, XML, or JSON).
   *
   * @param filePath the file path (from job parameters)
   * @param delimiter the CSV delimiter (from job parameters, default: ",")
   * @param skipHeader whether to skip header line (from job parameters, default: true)
   * @param fileFormatFactory the file format factory for reader creation
   * @return configured reader
   */
  @Bean
  @StepScope
  public PaymentItemReader paymentItemReader(
      @Value("#{jobParameters['filePath']}") String filePath,
      @Value("#{jobParameters['delimiter'] ?: '" + DEFAULT_DELIMITER + "'}") String delimiter,
      @Value("#{jobParameters['skipHeader'] ?: true}") Boolean skipHeader,
      FileFormatFactory fileFormatFactory) {

    log.info("Creating PaymentItemReader for file: {}, delimiter: '{}'", filePath, delimiter);

    Resource resource = new FileSystemResource(filePath);
    
    try {
      // Use file format factory to create appropriate reader and wrap it in an adapter
      var formatReader = fileFormatFactory.createReader(resource, delimiter, skipHeader);
      return new PaymentItemReaderAdapter(formatReader);
    } catch (Exception e) {
      log.error("Failed to create reader for file: {}", filePath, e);
      throw new RuntimeException("Failed to create reader for file: " + filePath, e);
    }
  }

  /**
   * Creates a step-scoped PaymentItemProcessor.
   *
   * @param tenantId the tenant ID (from job parameters)
   * @param jobExecutionId the batch job execution ID (from step execution)
   * @return configured processor
   */
  @Bean
  @StepScope
  public PaymentItemProcessor paymentItemProcessor(
      @Value("#{jobParameters['tenantId']}") String tenantId,
      @Value("#{stepExecution.jobExecutionId}") Long jobExecutionId) {

    log.info("Creating PaymentItemProcessor for tenant: {}, jobExecutionId: {}", tenantId, jobExecutionId);

    return new PaymentItemProcessor(tenantId, jobExecutionId);
  }

  /**
   * Creates a PaymentItemWriter.
   *
   * <p>Note: Writer is not step-scoped as it doesn't require job parameters.
   *
   * @param repository the processed payment repository
   * @return configured writer
   */
  @Bean
  public PaymentItemWriter paymentItemWriter(ProcessedPaymentRepository repository) {
    log.info("Creating PaymentItemWriter");
    return new PaymentItemWriter(repository);
  }
}
