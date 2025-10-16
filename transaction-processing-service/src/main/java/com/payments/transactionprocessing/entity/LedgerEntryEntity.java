package com.payments.transactionprocessing.entity;

import com.payments.domain.shared.AccountNumber;
import com.payments.domain.shared.TenantContext;
import com.payments.domain.transaction.LedgerEntry;
import com.payments.domain.transaction.LedgerEntryId;
import com.payments.domain.transaction.LedgerEntryType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ledger_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryEntity {

  @EmbeddedId
  @AttributeOverride(name = "value", column = @Column(name = "entry_id"))
  private LedgerEntryId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id")
  private TransactionEntity transaction;

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

  // Convert from domain model
  public static LedgerEntryEntity fromDomain(LedgerEntry ledgerEntry) {
    LedgerEntryEntity entity = new LedgerEntryEntity();
    entity.setId(ledgerEntry.getId());
    entity.setTenantContext(ledgerEntry.getTenantContext());
    entity.setAccountNumber(ledgerEntry.getAccountNumber());
    entity.setEntryType(ledgerEntry.getEntryType());
    entity.setAmount(ledgerEntry.getAmount());
    entity.setBalanceBefore(ledgerEntry.getBalanceBefore());
    entity.setBalanceAfter(ledgerEntry.getBalanceAfter());
    entity.setEntryDate(ledgerEntry.getEntryDate());
    entity.setCreatedAt(ledgerEntry.getCreatedAt());
    return entity;
  }

  // Convert to domain model
  public LedgerEntry toDomain() {
    return new LedgerEntry(
        this.id,
        this.transaction.getId(),
        this.tenantContext,
        this.accountNumber,
        this.entryType,
        this.amount);
  }
}
