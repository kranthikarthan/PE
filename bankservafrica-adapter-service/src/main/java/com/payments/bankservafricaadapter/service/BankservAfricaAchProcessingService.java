package com.payments.bankservafricaadapter.service;

import com.payments.bankservafricaadapter.domain.BankservAfricaAdapter;
import com.payments.bankservafricaadapter.domain.BankservAfricaAchTransaction;
import com.payments.bankservafricaadapter.repository.BankservAfricaAdapterRepository;
import com.payments.bankservafricaadapter.repository.BankservAfricaAchTransactionRepository;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for BankservAfrica ACH transaction processing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankservAfricaAchProcessingService {
    
    private final BankservAfricaAdapterRepository adapterRepository;
    private final BankservAfricaAchTransactionRepository achTransactionRepository;
    
    /**
     * Process ACH transaction
     */
    @Transactional
    public BankservAfricaAchTransaction processAchTransaction(
            ClearingAdapterId adapterId,
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
        
        log.info("Processing ACH transaction: {} for adapter: {}", transactionId, adapterId);
        
        BankservAfricaAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + adapterId));
        
        if (!adapter.isActive()) {
            throw new IllegalStateException("Adapter is not active: " + adapterId);
        }
        
        // Create ACH transaction
        BankservAfricaAchTransaction transaction = BankservAfricaAchTransaction.create(
                ClearingMessageId.generate(),
                adapterId,
                achBatchId,
                transactionId,
                transactionType,
                amount,
                currencyCode,
                originatorId,
                originatorName,
                receiverId,
                receiverName,
                accountNumber,
                routingNumber,
                traceNumber);
        
        // Mark as processing
        transaction.markAsProcessing();
        
        // Save transaction
        BankservAfricaAchTransaction savedTransaction = achTransactionRepository.save(transaction);
        
        // Add to adapter
        adapter.addAchTransaction(savedTransaction);
        adapterRepository.save(adapter);
        
        log.info("Successfully processed ACH transaction: {} with transaction ID: {}", savedTransaction.getId(), transactionId);
        
        return savedTransaction;
    }
    
    /**
     * Update ACH transaction status
     */
    @Transactional
    public BankservAfricaAchTransaction updateAchTransactionStatus(
            ClearingMessageId transactionId,
            String status,
            String returnCode,
            String returnReason) {
        
        log.info("Updating ACH transaction status: {} to {}", transactionId, status);
        
        BankservAfricaAchTransaction transaction = achTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("ACH transaction not found: " + transactionId));
        
        transaction.updateStatus(status, returnCode, returnReason);
        
        BankservAfricaAchTransaction updatedTransaction = achTransactionRepository.save(transaction);
        
        log.info("Successfully updated ACH transaction status: {}", transactionId);
        
        return updatedTransaction;
    }
    
    /**
     * Set settlement date
     */
    @Transactional
    public BankservAfricaAchTransaction setSettlementDate(ClearingMessageId transactionId, LocalDate settlementDate) {
        log.info("Setting settlement date for ACH transaction: {} to {}", transactionId, settlementDate);
        
        BankservAfricaAchTransaction transaction = achTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("ACH transaction not found: " + transactionId));
        
        transaction.setSettlementDate(settlementDate);
        
        BankservAfricaAchTransaction updatedTransaction = achTransactionRepository.save(transaction);
        
        log.info("Successfully set settlement date for ACH transaction: {}", transactionId);
        
        return updatedTransaction;
    }
    
    /**
     * Get ACH transaction by ID
     */
    public Optional<BankservAfricaAchTransaction> getAchTransaction(ClearingMessageId transactionId) {
        return achTransactionRepository.findById(transactionId);
    }
    
    /**
     * Get ACH transaction by transaction ID
     */
    public Optional<BankservAfricaAchTransaction> getAchTransactionByTransactionId(String transactionId) {
        BankservAfricaAchTransaction transaction = achTransactionRepository.findByTransactionId(transactionId);
        return Optional.ofNullable(transaction);
    }
    
    /**
     * Get ACH transactions by batch ID
     */
    public List<BankservAfricaAchTransaction> getAchTransactionsByBatchId(String achBatchId) {
        return achTransactionRepository.findByAchBatchId(achBatchId);
    }
    
    /**
     * Get ACH transactions by adapter ID
     */
    public List<BankservAfricaAchTransaction> getAchTransactionsByAdapterId(ClearingAdapterId adapterId) {
        return achTransactionRepository.findByAdapterId(adapterId.toString());
    }
    
    /**
     * Get ACH transactions by status
     */
    public List<BankservAfricaAchTransaction> getAchTransactionsByStatus(String status) {
        return achTransactionRepository.findByStatus(status);
    }
    
    /**
     * Get ACH transactions by transaction type
     */
    public List<BankservAfricaAchTransaction> getAchTransactionsByTransactionType(String transactionType) {
        return achTransactionRepository.findByTransactionType(transactionType);
    }
    
    /**
     * Get ACH transactions by originator ID
     */
    public List<BankservAfricaAchTransaction> getAchTransactionsByOriginatorId(String originatorId) {
        return achTransactionRepository.findByOriginatorId(originatorId);
    }
    
    /**
     * Get ACH transactions by receiver ID
     */
    public List<BankservAfricaAchTransaction> getAchTransactionsByReceiverId(String receiverId) {
        return achTransactionRepository.findByReceiverId(receiverId);
    }
    
    /**
     * Get ACH transactions by account number
     */
    public List<BankservAfricaAchTransaction> getAchTransactionsByAccountNumber(String accountNumber) {
        return achTransactionRepository.findByAccountNumber(accountNumber);
    }
    
    /**
     * Get ACH transactions by routing number
     */
    public List<BankservAfricaAchTransaction> getAchTransactionsByRoutingNumber(String routingNumber) {
        return achTransactionRepository.findByRoutingNumber(routingNumber);
    }
    
    /**
     * Get ACH transactions by trace number
     */
    public List<BankservAfricaAchTransaction> getAchTransactionsByTraceNumber(String traceNumber) {
        return achTransactionRepository.findByTraceNumber(traceNumber);
    }
    
    /**
     * Get ACH transactions by settlement date
     */
    public List<BankservAfricaAchTransaction> getAchTransactionsBySettlementDate(LocalDate settlementDate) {
        return achTransactionRepository.findBySettlementDate(settlementDate);
    }
    
    /**
     * Get ACH transactions by amount range
     */
    public List<BankservAfricaAchTransaction> getAchTransactionsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return achTransactionRepository.findByAmountBetween(minAmount, maxAmount);
    }
    
    /**
     * Get ACH transactions by currency code
     */
    public List<BankservAfricaAchTransaction> getAchTransactionsByCurrencyCode(String currencyCode) {
        return achTransactionRepository.findByCurrencyCode(currencyCode);
    }
    
    /**
     * Get ACH transactions created after timestamp
     */
    public List<BankservAfricaAchTransaction> getAchTransactionsCreatedAfter(java.time.Instant timestamp) {
        return achTransactionRepository.findByCreatedAtAfter(timestamp);
    }
    
    /**
     * Count ACH transactions by batch ID
     */
    public long countAchTransactionsByBatchId(String achBatchId) {
        return achTransactionRepository.countByAchBatchId(achBatchId);
    }
    
    /**
     * Count ACH transactions by status
     */
    public long countAchTransactionsByStatus(String status) {
        return achTransactionRepository.countByStatus(status);
    }
    
    /**
     * Count ACH transactions by transaction type
     */
    public long countAchTransactionsByTransactionType(String transactionType) {
        return achTransactionRepository.countByTransactionType(transactionType);
    }
    
    /**
     * Count ACH transactions by originator ID
     */
    public long countAchTransactionsByOriginatorId(String originatorId) {
        return achTransactionRepository.countByOriginatorId(originatorId);
    }
    
    /**
     * Count ACH transactions by receiver ID
     */
    public long countAchTransactionsByReceiverId(String receiverId) {
        return achTransactionRepository.countByReceiverId(receiverId);
    }
    
    /**
     * Count ACH transactions by currency code
     */
    public long countAchTransactionsByCurrencyCode(String currencyCode) {
        return achTransactionRepository.countByCurrencyCode(currencyCode);
    }
}
