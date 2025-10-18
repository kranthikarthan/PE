package com.payments.settlement.repository;

import com.payments.settlement.domain.SettlementBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, String> {}
