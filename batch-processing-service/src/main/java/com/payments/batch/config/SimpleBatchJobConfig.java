package com.payments.batch.config;

import com.payments.batch.domain.BatchRecord;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class SimpleBatchJobConfig {

  @Bean
  public Job importJob(JobRepository jobRepository, Step csvImportStep) {
    return new JobBuilder("importJob", jobRepository).start(csvImportStep).build();
  }

  @Bean
  public Step csvImportStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      FlatFileItemReader<BatchRecord> csvReader,
      JpaItemWriter<BatchRecord> jpaWriter) {
    return new StepBuilder("csvImportStep", jobRepository)
        .<BatchRecord, BatchRecord>chunk(50, transactionManager)
        .reader(csvReader)
        .writer(jpaWriter)
        .build();
  }

  @Bean
  public FlatFileItemReader<BatchRecord> csvReader() {
    return new FlatFileItemReaderBuilder<BatchRecord>()
        .name("csvReader")
        .resource(new ClassPathResource("sample-batch.csv"))
        .linesToSkip(1)
        .lineTokenizer(
            new DelimitedLineTokenizer() {
              {
                setNames("paymentId", "debtorAccount", "creditorAccount", "amount", "currency");
              }
            })
        .fieldSetMapper(
            new BeanWrapperFieldSetMapper<BatchRecord>() {
              {
                setTargetType(BatchRecord.class);
              }
            })
        .build();
  }

  @Bean
  public JpaItemWriter<BatchRecord> jpaWriter(EntityManagerFactory emf) {
    JpaItemWriter<BatchRecord> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(emf);
    return writer;
  }
}
