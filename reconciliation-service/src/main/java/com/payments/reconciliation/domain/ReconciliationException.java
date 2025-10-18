package com.payments.reconciliation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "reconciliation_exceptions")
public class ReconciliationException {
  @Id
  @Column(name = "exception_id")
  private String exceptionId;

  @ManyToOne
  @JoinColumn(name = "reconciliation_id", nullable = false)
  private ReconciliationRun reconciliationRun;

  @Column(name = "transaction_id")
  private String transactionId;

  @Column(name = "clearing_reference")
  private String clearingReference;

  @Column(name = "exception_type", nullable = false)
  private String exceptionType;

  @Column(name = "exception_reason")
  private String exceptionReason;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "resolved_at")
  private Instant resolvedAt;

  public String getExceptionId() {
    return exceptionId;
  }

  public void setExceptionId(String exceptionId) {
    this.exceptionId = exceptionId;
  }

  public ReconciliationRun getReconciliationRun() {
    return reconciliationRun;
  }

  public void setReconciliationRun(ReconciliationRun reconciliationRun) {
    this.reconciliationRun = reconciliationRun;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public String getClearingReference() {
    return clearingReference;
  }

  public void setClearingReference(String clearingReference) {
    this.clearingReference = clearingReference;
  }

  public String getExceptionType() {
    return exceptionType;
  }

  public void setExceptionType(String exceptionType) {
    this.exceptionType = exceptionType;
  }

  public String getExceptionReason() {
    return exceptionReason;
  }

  public void setExceptionReason(String exceptionReason) {
    this.exceptionReason = exceptionReason;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getResolvedAt() {
    return resolvedAt;
  }

  public void setResolvedAt(Instant resolvedAt) {
    this.resolvedAt = resolvedAt;
  }
}
