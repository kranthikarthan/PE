package com.payments.transactionprocessing.entity;

import com.payments.domain.transaction.Transaction;
import com.payments.domain.transaction.TransactionId;
import com.payments.domain.transaction.TransactionStatus;
import com.payments.domain.transaction.TransactionType;
import com.payments.domain.shared.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "transaction_id"))
    private TransactionId id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "tenantId", column = @Column(name = "tenant_id")),
        @AttributeOverride(name = "businessUnitId", column = @Column(name = "business_unit_id"))
    })
    private TenantContext tenantContext;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "payment_id"))
    private PaymentId paymentId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "debit_account"))
    private AccountNumber debitAccount;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "credit_account"))
    private AccountNumber creditAccount;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "currency")
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @Column(name = "clearing_system")
    private String clearingSystem;

    @Column(name = "clearing_reference")
    private String clearingReference;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LedgerEntryEntity> ledgerEntries = new ArrayList<>();

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionEventEntity> events = new ArrayList<>();

    // Convert from domain model
    public static TransactionEntity fromDomain(Transaction transaction) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(transaction.getId());
        entity.setTenantContext(transaction.getTenantContext());
        entity.setPaymentId(transaction.getPaymentId());
        entity.setDebitAccount(transaction.getDebitAccount());
        entity.setCreditAccount(transaction.getCreditAccount());
        entity.setAmount(transaction.getAmount().getAmount());
        entity.setCurrency(transaction.getAmount().getCurrency().getCurrencyCode());
        entity.setStatus(transaction.getStatus());
        entity.setTransactionType(transaction.getTransactionType());
        entity.setClearingSystem(transaction.getClearingSystem());
        entity.setClearingReference(transaction.getClearingReference());
        entity.setCreatedAt(transaction.getCreatedAt());
        entity.setCompletedAt(transaction.getCompletedAt());
        entity.setFailureReason(transaction.getFailureReason());
        return entity;
    }

    // Convert to domain model
    public Transaction toDomain() {
        return Transaction.create(
            this.id,
            this.tenantContext,
            this.paymentId,
            this.debitAccount,
            this.creditAccount,
            Money.of(this.amount, Currency.getInstance(this.currency)),
            this.transactionType
        );
    }
}
