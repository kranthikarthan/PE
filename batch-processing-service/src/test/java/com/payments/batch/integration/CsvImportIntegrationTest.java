package com.payments.batch.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.batch.BatchProcessingServiceApplication;
import com.payments.batch.domain.BatchRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = BatchProcessingServiceApplication.class)
@ActiveProfiles("test")
class CsvImportIntegrationTest {

  @Autowired private JobLauncher jobLauncher;

  @Autowired private Job importJob;

  @Autowired private EntityManager entityManager;

  @Test
  @Transactional
  void shouldImportCsvAndPersistRecords() throws Exception {
    jobLauncher.run(
        importJob,
        new JobParametersBuilder().addLong("ts", System.currentTimeMillis()).toJobParameters());

    TypedQuery<BatchRecord> q =
        entityManager.createQuery("select b from BatchRecord b", BatchRecord.class);
    List<BatchRecord> results = q.getResultList();
    assertThat(results.size()).isGreaterThanOrEqualTo(3);
  }
}
