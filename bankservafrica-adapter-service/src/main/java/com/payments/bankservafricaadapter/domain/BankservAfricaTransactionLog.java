package com.payments.bankservafricaadapter.domain;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.bankservafricaadapter.exception.InvalidBankservAfricaTransactionLogException;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * BankservAfrica Transaction Log Entity
 *
 * <p>Represents transaction logs for BankservAfrica processing:
 * - Operation audit trail
 * - Performance monitoring
 * - Error tracking
 * - Processing time measurement
 */
@Entity
@Table(name = "bankservafrica_transaction_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class BankservAfricaTransactionLog {

    @EmbeddedId
    private ClearingMessageId id;

    @Embedded
    private ClearingAdapterId bankservafricaAdapterId;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "message_type", nullable = false)
    private String messageType;

    @Column(name = "direction", nullable = false)
    private String direction;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Create a new transaction log
     */
    public static BankservAfricaTransactionLog create(
            ClearingMessageId id,
            ClearingAdapterId bankservafricaAdapterId,
            String transactionId,
            String messageType,
            String direction,
            String status) {
        
        if (transactionId == null || transactionId.isBlank()) {
            throw new InvalidBankservAfricaTransactionLogException("Transaction ID cannot be null or blank");
        }
        if (messageType == null || messageType.isBlank()) {
            throw new InvalidBankservAfricaTransactionLogException("Message type cannot be null or blank");
        }
        if (direction == null || direction.isBlank()) {
            throw new InvalidBankservAfricaTransactionLogException("Direction cannot be null or blank");
        }
        if (status == null || status.isBlank()) {
            throw new InvalidBankservAfricaTransactionLogException("Status cannot be null or blank");
        }

        BankservAfricaTransactionLog log = new BankservAfricaTransactionLog();
        log.id = id;
        log.bankservafricaAdapterId = bankservafricaAdapterId;
        log.transactionId = transactionId;
        log.messageType = messageType;
        log.direction = direction;
        log.status = status;
        log.createdAt = Instant.now();

        return log;
    }

    /**
     * Update log with status details
     */
    public void updateStatusDetails(Integer statusCode, String errorCode, String errorMessage) {
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Set processing time
     */
    public void setProcessingTimeMs(Integer processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    /**
     * Check if log is successful
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(this.status) || "COMPLETED".equals(this.status);
    }

    /**
     * Check if log is failed
     */
    public boolean isFailed() {
        return "FAILED".equals(this.status) || "ERROR".equals(this.status);
    }

    /**
     * Check if log has error
     */
    public boolean hasError() {
        return this.errorCode != null || this.errorMessage != null;
    }
}
