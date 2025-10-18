package com.payments.reconciliation.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.reconciliation.domain.ReconciliationRun;
import com.payments.reconciliation.repository.ReconciliationRunRepository;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ReconciliationRepositoryIntegrationTest {

  @Autowired private ReconciliationRunRepository repository;

  @Test
  void shouldPersistAndLoadRun_WhenValidData() {
    ReconciliationRun run = new ReconciliationRun();
    run.setReconciliationId("RECON-IT-001");
    run.setRunDate(LocalDate.now());
    run.setClearingSystem("RTC");
    run.setStatus("RUNNING");
    run.setTotalTransactions(10);
    run.setMatchedCount(8);
    run.setUnmatchedCount(2);
    run.setStartedAt(Instant.now());

    repository.save(run);

    var found = repository.findById("RECON-IT-001");
    assertThat(found).isPresent();
    assertThat(found.get().getClearingSystem()).isEqualTo("RTC");
  }
}
