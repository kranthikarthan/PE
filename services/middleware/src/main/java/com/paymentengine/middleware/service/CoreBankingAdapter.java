package com.paymentengine.middleware.service;

import com.paymentengine.middleware.dto.corebanking.*;
import com.paymentengine.middleware.exception.CoreBankingException;
import com.paymentengine.middleware.exception.AccountException;
import com.paymentengine.middleware.exception.ValidationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Core Banking Adapter Interface
 * 
 * This interface abstracts the core banking functionality to allow the payment engine
 * to integrate with different core banking systems (internal or external) via REST API or gRPC.
 * 
 * The adapter handles:
 * - Account management and validation
 * - Balance inquiries and updates
 * - Transaction processing
 * - Payment routing decisions (same bank vs other bank)
 * - ISO 20022 message integration
 */
public interface CoreBankingAdapter {
    
    /**
     * Get adapter type (REST, GRPC, INTERNAL)
     */
    String getAdapterType();
    
    /**
     * Check if adapter is available and healthy
     */
    boolean isHealthy();
    
    // ============================================================================
    // ACCOUNT MANAGEMENT
    // ============================================================================
    
    /**
     * Get account information by account number
     */
    AccountInfo getAccountInfo(String accountNumber, String tenantId);
    
    /**
     * Validate account exists and is active
     */
    boolean validateAccount(String accountNumber, String tenantId);
    
    /**
     * Get account balance
     */
    AccountBalance getAccountBalance(String accountNumber, String tenantId);
    
    /**
     * Check if account has sufficient funds
     */
    boolean hasSufficientFunds(String accountNumber, BigDecimal amount, String currency, String tenantId);
    
    /**
     * Get account holder information
     */
    AccountHolder getAccountHolder(String accountNumber, String tenantId);
    
    // ============================================================================
    // TRANSACTION PROCESSING
    // ============================================================================
    
    /**
     * Process a debit transaction
     */
    TransactionResult processDebit(DebitTransactionRequest request);
    
    /**
     * Process a credit transaction
     */
    TransactionResult processCredit(CreditTransactionRequest request);
    
    /**
     * Process a transfer transaction (debit + credit)
     */
    TransactionResult processTransfer(TransferTransactionRequest request);
    
    /**
     * Hold funds for a pending transaction
     */
    TransactionResult holdFunds(HoldFundsRequest request);
    
    /**
     * Release held funds
     */
    TransactionResult releaseFunds(ReleaseFundsRequest request);
    
    /**
     * Get transaction status
     */
    TransactionStatus getTransactionStatus(String transactionReference, String tenantId);
    
    // ============================================================================
    // PAYMENT ROUTING
    // ============================================================================
    
    /**
     * Determine payment routing (same bank vs other bank)
     */
    PaymentRouting determinePaymentRouting(PaymentRoutingRequest request);
    
    /**
     * Check if both accounts belong to the same bank
     */
    boolean isSameBankPayment(String fromAccountNumber, String toAccountNumber, String tenantId);
    
    /**
     * Get clearing system for other bank payment
     */
    String getClearingSystemForPayment(String toAccountNumber, String paymentType, String tenantId);
    
    /**
     * Get local instrumentation code for routing
     */
    String getLocalInstrumentationCode(String paymentType, String tenantId);
    
    // ============================================================================
    // ISO 20022 INTEGRATION
    // ============================================================================
    
    /**
     * Process ISO 20022 payment instruction (pain.001)
     */
    Iso20022PaymentResult processIso20022Payment(Iso20022PaymentRequest request);
    
    /**
     * Generate ISO 20022 payment response (pain.002)
     */
    Iso20022PaymentResponse generateIso20022Response(Iso20022ResponseRequest request);
    
    /**
     * Validate ISO 20022 message format
     */
    boolean validateIso20022Message(String message, String messageType, String tenantId);
    
    // ============================================================================
    // BATCH PROCESSING
    // ============================================================================
    
    /**
     * Process batch of transactions
     */
    BatchTransactionResult processBatchTransactions(BatchTransactionRequest request);
    
    /**
     * Get batch processing status
     */
    BatchStatus getBatchStatus(String batchId, String tenantId);
    
    // ============================================================================
    // RECONCILIATION
    // ============================================================================
    
    /**
     * Get transactions for reconciliation
     */
    List<TransactionRecord> getTransactionsForReconciliation(ReconciliationRequest request);
    
    /**
     * Process reconciliation
     */
    ReconciliationResult processReconciliation(ReconciliationRequest request);
    
    // ============================================================================
    // REPORTING
    // ============================================================================
    
    /**
     * Get transaction history
     */
    List<TransactionRecord> getTransactionHistory(TransactionHistoryRequest request);
    
    /**
     * Get account statement
     */
    AccountStatement getAccountStatement(AccountStatementRequest request);
    
    /**
     * Get payment statistics
     */
    PaymentStatistics getPaymentStatistics(PaymentStatisticsRequest request);
}