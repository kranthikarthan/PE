package com.payments.bankservafricaadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.bankservafricaadapter.exception.InvalidBankservAfricaAchTransactionException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * BankservAfrica ACH Transaction Entity
 *
 * <p>Represents ACH transactions for BankservAfrica processing:
 * - ACH batch processing
 * - Originator and receiver information
 * - Settlement tracking
 * - Return code handling
 */
@Entity
@Table(name = "bankservafrica_ach_transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class BankservAfricaAchTransaction {

    @EmbeddedId
    private ClearingMessageId id;

    @Embedded
    private ClearingAdapterId bankservafricaAdapterId;

    @Column(name = "ach_batch_id", nullable = false)
    private String achBatchId;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "originator_id", nullable = false, length = 15)
    private String originatorId;

    @Column(name = "originator_name", nullable = false)
    private String originatorName;

    @Column(name = "receiver_id", nullable = false, length = 15)
    private String receiverId;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "account_number", nullable = false, length = 17)
    private String accountNumber;

    @Column(name = "routing_number", nullable = false, length = 9)
    private String routingNumber;

    @Column(name = "trace_number", nullable = false, length = 15)
    private String traceNumber;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "return_code", length = 3)
    private String returnCode;

    @Column(name = "return_reason")
    private String returnReason;

    @Column(name = "settlement_date")
    private LocalDate settlementDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Create a new ACH transaction
     */
    public static BankservAfricaAchTransaction create(
            ClearingMessageId id,
            ClearingAdapterId bankservafricaAdapterId,
            String achBatchId,
            String transactionId,
            String transactionType,
            BigDecimal amount,
            String currencyCode,
            String originatorId,
            String originatorName,
            String receiverId,
            String receiverName,
            String accountNumber,
            String routingNumber,
            String traceNumber) {
        
        if (achBatchId == null || achBatchId.isBlank()) {
            throw new InvalidBankservAfricaAchTransactionException("ACH Batch ID cannot be null or blank");
        }
        if (transactionId == null || transactionId.isBlank()) {
            throw new InvalidBankservAfricaAchTransactionException("Transaction ID cannot be null or blank");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBankservAfricaAchTransactionException("Amount must be positive");
        }
        if (traceNumber == null || traceNumber.isBlank()) {
            throw new InvalidBankservAfricaAchTransactionException("Trace number cannot be null or blank");
        }

        BankservAfricaAchTransaction transaction = new BankservAfricaAchTransaction();
        transaction.id = id;
        transaction.bankservafricaAdapterId = bankservafricaAdapterId;
        transaction.achBatchId = achBatchId;
        transaction.transactionId = transactionId;
        transaction.transactionType = transactionType;
        transaction.amount = amount;
        transaction.currencyCode = currencyCode;
        transaction.originatorId = originatorId;
        transaction.originatorName = originatorName;
        transaction.receiverId = receiverId;
        transaction.receiverName = receiverName;
        transaction.accountNumber = accountNumber;
        transaction.routingNumber = routingNumber;
        transaction.traceNumber = traceNumber;
        transaction.status = "PENDING";
        transaction.createdAt = Instant.now();
        transaction.updatedAt = Instant.now();

        return transaction;
    }

    /**
     * Update transaction status
     */
    public void updateStatus(String status, String returnCode, String returnReason) {
        this.status = status;
        this.returnCode = returnCode;
        this.returnReason = returnReason;
        this.updatedAt = Instant.now();
    }

    /**
     * Set settlement date
     */
    public void setSettlementDate(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
        this.updatedAt = Instant.now();
    }

    /**
     * Mark as processing
     */
    public void markAsProcessing() {
        updateStatus("PROCESSING", null, null);
    }

    /**
     * Mark as completed
     */
    public void markAsCompleted() {
        updateStatus("COMPLETED", null, null);
    }

    /**
     * Mark as failed
     */
    public void markAsFailed(String returnCode, String returnReason) {
        updateStatus("FAILED", returnCode, returnReason);
    }

    /**
     * Mark as returned
     */
    public void markAsReturned(String returnCode, String returnReason) {
        updateStatus("RETURNED", returnCode, returnReason);
    }

    /**
     * Check if transaction is pending
     */
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }

    /**
     * Check if transaction is processing
     */
    public boolean isProcessing() {
        return "PROCESSING".equals(this.status);
    }

    /**
     * Check if transaction is completed
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(this.status);
    }

    /**
     * Check if transaction is failed
     */
    public boolean isFailed() {
        return "FAILED".equals(this.status);
    }

    /**
     * Check if transaction is returned
     */
    public boolean isReturned() {
        return "RETURNED".equals(this.status);
    }

    /**
     * Check if transaction is settled
     */
    public boolean isSettled() {
        return this.settlementDate != null;
    }
}
