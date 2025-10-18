package com.payments.settlement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "settlement_transactions")
public class SettlementTransaction {
  @Id
  @Column(name = "settlement_txn_id")
  private String settlementTxnId;

  @ManyToOne
  @JoinColumn(name = "batch_id", nullable = false)
  private SettlementBatch batch;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "settlement_status", nullable = false)
  private String settlementStatus;

  @Column(name = "included_at", nullable = false)
  private Instant includedAt;

  public String getSettlementTxnId() {
    return settlementTxnId;
  }

  public void setSettlementTxnId(String settlementTxnId) {
    this.settlementTxnId = settlementTxnId;
  }

  public SettlementBatch getBatch() {
    return batch;
  }

  public void setBatch(SettlementBatch batch) {
    this.batch = batch;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getSettlementStatus() {
    return settlementStatus;
  }

  public void setSettlementStatus(String settlementStatus) {
    this.settlementStatus = settlementStatus;
  }

  public Instant getIncludedAt() {
    return includedAt;
  }

  public void setIncludedAt(Instant includedAt) {
    this.includedAt = includedAt;
  }
}
