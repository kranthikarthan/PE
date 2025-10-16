package com.payments.domain.transaction;

import com.payments.domain.shared.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Ledger Entry (Entity within Transaction Aggregate) */
@Entity
@Table(name = "ledger_entries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@AllArgsConstructor
public class LedgerEntry {

  @EmbeddedId
  @AttributeOverride(name = "value", column = @Column(name = "entry_id"))
  private LedgerEntryId id;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "transaction_id"))
  private TransactionId transactionId;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id")),
    @AttributeOverride(name = "businessUnitId", column = @Column(name = "business_unit_id"))
  })
  private TenantContext tenantContext;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "account_number"))
  private AccountNumber accountNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "entry_type")
  private LedgerEntryType entryType;

  @Column(name = "amount")
  private BigDecimal amount;

  @Column(name = "balance_before")
  private BigDecimal balanceBefore;

  @Column(name = "balance_after")
  private BigDecimal balanceAfter;

  @Column(name = "entry_date")
  private LocalDate entryDate;

  @Column(name = "created_at")
  private Instant createdAt;

  public LedgerEntry(
      LedgerEntryId id,
      TransactionId transactionId,
      TenantContext tenantContext,
      AccountNumber accountNumber,
      LedgerEntryType entryType,
      BigDecimal amount) {
    this.id = id;
    this.transactionId = transactionId;
    this.tenantContext = tenantContext;
    this.accountNumber = accountNumber;
    this.entryType = entryType;
    this.amount = amount;
    this.entryDate = LocalDate.now();
    this.createdAt = Instant.now();
  }
}
