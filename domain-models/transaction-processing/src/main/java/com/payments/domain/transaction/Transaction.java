package com.payments.domain.transaction;

import com.payments.domain.shared.*;
import lombok.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Transaction Aggregate Root
 * 
 * Represents a financial transaction in the ledger.
 * Enforces double-entry bookkeeping rules.
 */
@Entity
@Table(name = "transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {
    
    @EmbeddedId
    private TransactionId id;
    
    @Embedded
    private TenantContext tenantContext;
    
    @Embedded
    private PaymentId paymentId;
    
    @Embedded
    private AccountNumber debitAccount;
    
    @Embedded
    private AccountNumber creditAccount;
    
    @Embedded
    private Money amount;
    
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    
    private String clearingSystem;
    
    private String clearingReference;
    
    private Instant createdAt;
    
    private Instant completedAt;
    
    private String failureReason;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "transaction_id")
    private List<LedgerEntry> ledgerEntries = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "transaction_id")
    private List<TransactionEvent> events = new ArrayList<>();
    
    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    // ─────────────────────────────────────────────────────────
    // FACTORY METHOD
    // ─────────────────────────────────────────────────────────
    
    public static Transaction create(
        TransactionId id,
        TenantContext tenantContext,
        PaymentId paymentId,
        AccountNumber debitAccount,
        AccountNumber creditAccount,
        Money amount,
        TransactionType transactionType
    ) {
        // Business validation
        if (amount.isNegativeOrZero()) {
            throw new InvalidTransactionException("Transaction amount must be positive");
        }
        
        if (debitAccount.equals(creditAccount)) {
            throw new InvalidTransactionException("Debit and credit accounts must be different");
        }
        
        Transaction transaction = new Transaction();
        transaction.id = id;
        transaction.tenantContext = tenantContext;
        transaction.paymentId = paymentId;
        transaction.debitAccount = debitAccount;
        transaction.creditAccount = creditAccount;
        transaction.amount = amount;
        transaction.transactionType = transactionType;
        transaction.status = TransactionStatus.CREATED;
        transaction.createdAt = Instant.now();
        
        // Create ledger entries (double-entry bookkeeping)
        transaction.createLedgerEntries();
        
        // Record event
        transaction.addEvent("TransactionCreated", "Transaction created successfully");
        
        // Domain event
        transaction.registerEvent(new TransactionCreatedEvent(
            transaction.id,
            transaction.tenantContext,
            transaction.paymentId,
            transaction.debitAccount,
            transaction.creditAccount,
            transaction.amount,
            transaction.createdAt
        ));
        
        return transaction;
    }
    
    // ─────────────────────────────────────────────────────────
    // BUSINESS METHODS
    // ─────────────────────────────────────────────────────────
    
    /**
     * Start processing the transaction
     */
    public void startProcessing() {
        if (this.status != TransactionStatus.CREATED) {
            throw new InvalidStateTransitionException(
                "Can only process CREATED transactions. Current status: " + this.status
            );
        }
        
        this.status = TransactionStatus.PROCESSING;
        addEvent("TransactionProcessing", "Transaction processing started");
        
        registerEvent(new TransactionProcessingEvent(
            this.id,
            this.tenantContext,
            this.status
        ));
    }
    
    /**
     * Mark transaction as cleared
     */
    public void markCleared(String clearingSystem, String clearingReference) {
        if (this.status != TransactionStatus.PROCESSING) {
            throw new InvalidStateTransitionException(
                "Can only clear PROCESSING transactions. Current status: " + this.status
            );
        }
        
        this.status = TransactionStatus.CLEARING;
        this.clearingSystem = clearingSystem;
        this.clearingReference = clearingReference;
        
        addEvent("TransactionClearing", "Transaction submitted to clearing: " + clearingSystem);
        
        registerEvent(new TransactionClearingEvent(
            this.id,
            this.tenantContext,
            clearingSystem,
            clearingReference
        ));
    }
    
    /**
     * Complete the transaction
     */
    public void complete() {
        if (this.status != TransactionStatus.CLEARING) {
            throw new InvalidStateTransitionException(
                "Can only complete CLEARING transactions. Current status: " + this.status
            );
        }
        
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = Instant.now();
        
        addEvent("TransactionCompleted", "Transaction completed successfully");
        
        registerEvent(new TransactionCompletedEvent(
            this.id,
            this.tenantContext,
            this.amount,
            this.completedAt
        ));
    }
    
    /**
     * Fail the transaction
     */
    public void fail(String reason) {
        if (this.status == TransactionStatus.COMPLETED || this.status == TransactionStatus.FAILED) {
            throw new InvalidStateTransitionException(
                "Cannot fail transaction in " + this.status + " status"
            );
        }
        
        TransactionStatus previousStatus = this.status;
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
        
        addEvent("TransactionFailed", "Transaction failed: " + reason);
        
        registerEvent(new TransactionFailedEvent(
            this.id,
            this.tenantContext,
            reason,
            previousStatus
        ));
    }
    
    // ─────────────────────────────────────────────────────────
    // QUERY METHODS
    // ─────────────────────────────────────────────────────────
    
    public boolean isInProgress() {
        return this.status != TransactionStatus.COMPLETED && 
               this.status != TransactionStatus.FAILED;
    }
    
    public boolean isCompleted() {
        return this.status == TransactionStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return this.status == TransactionStatus.FAILED;
    }
    
    public TransactionId getId() {
        return id;
    }
    
    public TenantContext getTenantContext() {
        return tenantContext;
    }
    
    public PaymentId getPaymentId() {
        return paymentId;
    }
    
    public Money getAmount() {
        return amount;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    // ─────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────
    
    private void createLedgerEntries() {
        // Debit entry
        ledgerEntries.add(new LedgerEntry(
            LedgerEntryId.generate(),
            this.id,
            this.debitAccount,
            LedgerEntryType.DEBIT,
            this.amount
        ));
        
        // Credit entry
        ledgerEntries.add(new LedgerEntry(
            LedgerEntryId.generate(),
            this.id,
            this.creditAccount,
            LedgerEntryType.CREDIT,
            this.amount
        ));
    }
    
    private void addEvent(String eventType, String description) {
        events.add(new TransactionEvent(
            TransactionEventId.generate(),
            this.id,
            eventType,
            description,
            Instant.now()
        ));
    }
    
    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
}

/**
 * Ledger Entry (Entity within Transaction Aggregate)
 */
@Entity
@Table(name = "ledger_entries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
class LedgerEntry {
    
    @EmbeddedId
    private LedgerEntryId id;
    
    @Embedded
    private TransactionId transactionId;
    
    @Embedded
    private AccountNumber accountNumber;
    
    @Enumerated(EnumType.STRING)
    private LedgerEntryType entryType;
    
    @Embedded
    private Money amount;
    
    private Instant createdAt;
    
    public LedgerEntry(
        LedgerEntryId id,
        TransactionId transactionId,
        AccountNumber accountNumber,
        LedgerEntryType entryType,
        Money amount
    ) {
        this.id = id;
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.entryType = entryType;
        this.amount = amount;
        this.createdAt = Instant.now();
    }
}

/**
 * Transaction Event (Entity within Transaction Aggregate)
 */
@Entity
@Table(name = "transaction_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
class TransactionEvent {
    
    @EmbeddedId
    private TransactionEventId id;
    
    @Embedded
    private TransactionId transactionId;
    
    private String eventType;
    
    private String description;
    
    private Instant occurredAt;
    
    public TransactionEvent(
        TransactionEventId id,
        TransactionId transactionId,
        String eventType,
        String description,
        Instant occurredAt
    ) {
        this.id = id;
        this.transactionId = transactionId;
        this.eventType = eventType;
        this.description = description;
        this.occurredAt = occurredAt;
    }
}
