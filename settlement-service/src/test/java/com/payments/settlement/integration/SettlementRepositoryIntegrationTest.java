package com.payments.settlement.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.payments.settlement.domain.SettlementBatch;
import com.payments.settlement.repository.SettlementBatchRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SettlementRepositoryIntegrationTest {

  @Autowired private SettlementBatchRepository repository;

  @Test
  void shouldPersistAndLoadBatch_WhenValidData() {
    SettlementBatch batch = new SettlementBatch();
    batch.setBatchId("BATCH-IT-001");
    batch.setBatchDate(LocalDate.now());
    batch.setClearingSystem("RTC");
    batch.setStatus("PENDING");
    batch.setTotalDebit(new BigDecimal("100.00"));
    batch.setTotalCredit(new BigDecimal("100.00"));
    batch.setNetPosition(new BigDecimal("0.00"));
    batch.setCreatedAt(Instant.now());

    repository.save(batch);

    var found = repository.findById("BATCH-IT-001");
    assertThat(found).isPresent();
    assertThat(found.get().getClearingSystem()).isEqualTo("RTC");
  }
}
