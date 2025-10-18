package com.payments.reconciliation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "reconciliation_runs")
public class ReconciliationRun {
  @Id
  @Column(name = "reconciliation_id")
  private String reconciliationId;

  @Column(name = "run_date", nullable = false)
  private LocalDate runDate;

  @Column(name = "clearing_system", nullable = false)
  private String clearingSystem;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "total_transactions", nullable = false)
  private Integer totalTransactions;

  @Column(name = "matched_count", nullable = false)
  private Integer matchedCount;

  @Column(name = "unmatched_count", nullable = false)
  private Integer unmatchedCount;

  @Column(name = "started_at", nullable = false)
  private Instant startedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  public String getReconciliationId() {
    return reconciliationId;
  }

  public void setReconciliationId(String reconciliationId) {
    this.reconciliationId = reconciliationId;
  }

  public LocalDate getRunDate() {
    return runDate;
  }

  public void setRunDate(LocalDate runDate) {
    this.runDate = runDate;
  }

  public String getClearingSystem() {
    return clearingSystem;
  }

  public void setClearingSystem(String clearingSystem) {
    this.clearingSystem = clearingSystem;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Integer getTotalTransactions() {
    return totalTransactions;
  }

  public void setTotalTransactions(Integer totalTransactions) {
    this.totalTransactions = totalTransactions;
  }

  public Integer getMatchedCount() {
    return matchedCount;
  }

  public void setMatchedCount(Integer matchedCount) {
    this.matchedCount = matchedCount;
  }

  public Integer getUnmatchedCount() {
    return unmatchedCount;
  }

  public void setUnmatchedCount(Integer unmatchedCount) {
    this.unmatchedCount = unmatchedCount;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(Instant startedAt) {
    this.startedAt = startedAt;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(Instant completedAt) {
    this.completedAt = completedAt;
  }
}
