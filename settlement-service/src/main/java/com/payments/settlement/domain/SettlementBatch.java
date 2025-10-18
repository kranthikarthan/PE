package com.payments.settlement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "settlement_batches")
public class SettlementBatch {
  @Id
  @Column(name = "batch_id")
  private String batchId;

  @Column(name = "batch_date", nullable = false)
  private LocalDate batchDate;

  @Column(name = "clearing_system", nullable = false)
  private String clearingSystem;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "total_debit", nullable = false)
  private BigDecimal totalDebit;

  @Column(name = "total_credit", nullable = false)
  private BigDecimal totalCredit;

  @Column(name = "net_position", nullable = false)
  private BigDecimal netPosition;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "finalized_at")
  private Instant finalizedAt;

  public String getBatchId() {
    return batchId;
  }

  public void setBatchId(String batchId) {
    this.batchId = batchId;
  }

  public LocalDate getBatchDate() {
    return batchDate;
  }

  public void setBatchDate(LocalDate batchDate) {
    this.batchDate = batchDate;
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

  public BigDecimal getTotalDebit() {
    return totalDebit;
  }

  public void setTotalDebit(BigDecimal totalDebit) {
    this.totalDebit = totalDebit;
  }

  public BigDecimal getTotalCredit() {
    return totalCredit;
  }

  public void setTotalCredit(BigDecimal totalCredit) {
    this.totalCredit = totalCredit;
  }

  public BigDecimal getNetPosition() {
    return netPosition;
  }

  public void setNetPosition(BigDecimal netPosition) {
    this.netPosition = netPosition;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getFinalizedAt() {
    return finalizedAt;
  }

  public void setFinalizedAt(Instant finalizedAt) {
    this.finalizedAt = finalizedAt;
  }
}
