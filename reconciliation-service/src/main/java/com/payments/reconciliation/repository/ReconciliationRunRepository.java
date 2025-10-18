package com.payments.reconciliation.repository;

import com.payments.reconciliation.domain.ReconciliationRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReconciliationRunRepository extends JpaRepository<ReconciliationRun, String> {}
