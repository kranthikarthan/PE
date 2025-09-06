package com.paymentengine.corebanking.service;

import com.paymentengine.corebanking.dto.CreateTransactionRequest;
import com.paymentengine.corebanking.dto.TransactionResponse;
import com.paymentengine.corebanking.entity.Account;
import com.paymentengine.corebanking.entity.PaymentType;
import com.paymentengine.corebanking.entity.Transaction;
import com.paymentengine.corebanking.repository.AccountRepository;
import com.paymentengine.corebanking.repository.PaymentTypeRepository;
import com.paymentengine.corebanking.repository.TransactionRepository;
import com.paymentengine.shared.constants.KafkaTopics;
import com.paymentengine.shared.event.TransactionCreatedEvent;
import com.paymentengine.shared.event.TransactionCompletedEvent;
import com.paymentengine.shared.event.TransactionFailedEvent;
import com.paymentengine.shared.event.TransactionUpdatedEvent;
import com.paymentengine.shared.util.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling transaction operations
 */
@Service
@Transactional
public class TransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final EventPublisher eventPublisher;
    
    @Autowired
    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            PaymentTypeRepository paymentTypeRepository,
            EventPublisher eventPublisher) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.paymentTypeRepository = paymentTypeRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Create a new transaction
     */
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        logger.info("Creating transaction: {}", request);
        
        try {
            // Validate request
            validateTransactionRequest(request);
            
            // Get payment type
            PaymentType paymentType = paymentTypeRepository.findById(request.getPaymentTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Payment type not found"));
            
            if (!paymentType.getIsActive()) {
                throw new IllegalArgumentException("Payment type is not active");
            }
            
            // Validate amount
            if (!paymentType.isAmountValid(request.getAmount())) {
                throw new IllegalArgumentException("Amount is outside allowed range for payment type");
            }
            
            // Create transaction
            Transaction transaction = new Transaction();
            transaction.setTransactionReference(generateTransactionReference());
            transaction.setExternalReference(request.getExternalReference());
            transaction.setFromAccountId(request.getFromAccountId());
            transaction.setToAccountId(request.getToAccountId());
            transaction.setPaymentTypeId(request.getPaymentTypeId());
            transaction.setAmount(request.getAmount());
            transaction.setCurrencyCode(request.getCurrencyCode());
            transaction.setFeeAmount(paymentType.calculateFee(request.getAmount()));
            transaction.setTransactionType(determineTransactionType(request));
            transaction.setDescription(request.getDescription());
            transaction.setMetadata(request.getMetadata());
            
            // Validate accounts and balances
            if (transaction.getFromAccountId() != null) {
                validateDebitAccount(transaction);
            }
            
            if (transaction.getToAccountId() != null) {
                validateCreditAccount(transaction);
            }
            
            // Save transaction
            transaction = transactionRepository.save(transaction);
            
            // Publish event
            TransactionCreatedEvent event = createTransactionCreatedEvent(transaction, request);
            eventPublisher.publishEvent(KafkaTopics.TRANSACTION_CREATED, 
                                      transaction.getTransactionReference(), event);
            
            // Process transaction based on payment type
            if (paymentType.getIsSynchronous()) {
                processTransactionSync(transaction);
            } else {
                // Async processing will be handled by event listeners
                logger.info("Transaction {} queued for async processing", transaction.getTransactionReference());
            }
            
            return mapToTransactionResponse(transaction);
            
        } catch (Exception e) {
            logger.error("Error creating transaction: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create transaction: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process transaction synchronously
     */
    private void processTransactionSync(Transaction transaction) {
        try {
            transaction.markAsProcessing();
            transactionRepository.save(transaction);
            
            // Publish processing event
            TransactionUpdatedEvent processingEvent = new TransactionUpdatedEvent(
                transaction.getId(), transaction.getTransactionReference(),
                "PENDING", "PROCESSING"
            );
            eventPublisher.publishEvent(KafkaTopics.TRANSACTION_UPDATED, 
                                      transaction.getTransactionReference(), processingEvent);
            
            // Update account balances
            updateAccountBalances(transaction);
            
            // Complete transaction
            transaction.markAsCompleted();
            transactionRepository.save(transaction);
            
            // Publish completion event
            TransactionCompletedEvent completedEvent = new TransactionCompletedEvent(
                transaction.getId(), transaction.getTransactionReference(),
                transaction.getAmount(), transaction.getCurrencyCode(),
                Instant.now()
            );
            completedEvent.setFromAccountId(transaction.getFromAccountId());
            completedEvent.setToAccountId(transaction.getToAccountId());
            
            eventPublisher.publishEvent(KafkaTopics.TRANSACTION_COMPLETED, 
                                      transaction.getTransactionReference(), completedEvent);
            
            logger.info("Transaction {} completed successfully", transaction.getTransactionReference());
            
        } catch (Exception e) {
            logger.error("Error processing transaction {}: {}", 
                        transaction.getTransactionReference(), e.getMessage(), e);
            
            // Mark as failed
            transaction.markAsFailed(e.getMessage());
            transactionRepository.save(transaction);
            
            // Publish failure event
            TransactionFailedEvent failedEvent = new TransactionFailedEvent(
                transaction.getId(), transaction.getTransactionReference(),
                e.getMessage(), Instant.now()
            );
            failedEvent.setErrorMessage(e.getMessage());
            
            eventPublisher.publishEvent(KafkaTopics.TRANSACTION_FAILED, 
                                      transaction.getTransactionReference(), failedEvent);
            
            throw e;
        }
    }
    
    /**
     * Update account balances for a transaction
     */
    private void updateAccountBalances(Transaction transaction) {
        if (transaction.getFromAccountId() != null) {
            Account fromAccount = accountRepository.findByIdForUpdate(transaction.getFromAccountId())
                .orElseThrow(() -> new IllegalArgumentException("From account not found"));
            
            fromAccount.debit(transaction.getTotalAmount());
            accountRepository.save(fromAccount);
        }
        
        if (transaction.getToAccountId() != null) {
            Account toAccount = accountRepository.findByIdForUpdate(transaction.getToAccountId())
                .orElseThrow(() -> new IllegalArgumentException("To account not found"));
            
            toAccount.credit(transaction.getAmount());
            accountRepository.save(toAccount);
        }
    }
    
    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    public Optional<TransactionResponse> getTransactionById(UUID transactionId) {
        return transactionRepository.findById(transactionId)
            .map(this::mapToTransactionResponse);
    }
    
    /**
     * Get transaction by reference
     */
    @Transactional(readOnly = true)
    public Optional<TransactionResponse> getTransactionByReference(String reference) {
        return transactionRepository.findByTransactionReference(reference)
            .map(this::mapToTransactionResponse);
    }
    
    /**
     * Get transactions for an account
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByAccountId(UUID accountId, Pageable pageable) {
        return transactionRepository.findTransactionsByAccountId(accountId, pageable)
            .map(this::mapToTransactionResponse);
    }
    
    /**
     * Search transactions by criteria
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> searchTransactions(
            String transactionReference,
            UUID accountId,
            Transaction.TransactionStatus status,
            UUID paymentTypeId,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        
        return transactionRepository.findTransactionsByCriteria(
            transactionReference, accountId, status, paymentTypeId,
            minAmount, maxAmount, startDate, endDate, pageable
        ).map(this::mapToTransactionResponse);
    }
    
    /**
     * Cancel a transaction
     */
    public TransactionResponse cancelTransaction(UUID transactionId, String reason) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        
        transaction.cancel(reason);
        transaction = transactionRepository.save(transaction);
        
        // Publish cancellation event
        TransactionUpdatedEvent event = new TransactionUpdatedEvent(
            transaction.getId(), transaction.getTransactionReference(),
            "PROCESSING", "CANCELLED"
        );
        event.setReason(reason);
        
        eventPublisher.publishEvent(KafkaTopics.TRANSACTION_UPDATED, 
                                  transaction.getTransactionReference(), event);
        
        return mapToTransactionResponse(transaction);
    }
    
    /**
     * Get pending transactions older than specified minutes
     */
    @Transactional(readOnly = true)
    public List<Transaction> getPendingTransactionsOlderThan(int minutes) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(minutes);
        return transactionRepository.findPendingTransactionsOlderThan(cutoffTime);
    }
    
    /**
     * Get transaction statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTransactionStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Object[] stats = transactionRepository.getTransactionStatistics(startDate, endDate);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", stats[0]);
        result.put("totalAmount", stats[1]);
        result.put("averageAmount", stats[2]);
        result.put("maxAmount", stats[3]);
        result.put("minAmount", stats[4]);
        
        return result;
    }
    
    // Helper methods
    
    private void validateTransactionRequest(CreateTransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        if (request.getPaymentTypeId() == null) {
            throw new IllegalArgumentException("Payment type ID is required");
        }
        
        if (request.getFromAccountId() == null && request.getToAccountId() == null) {
            throw new IllegalArgumentException("At least one account (from or to) is required");
        }
    }
    
    private void validateDebitAccount(Transaction transaction) {
        Account account = accountRepository.findByIdForUpdate(transaction.getFromAccountId())
            .orElseThrow(() -> new IllegalArgumentException("From account not found"));
        
        if (!account.isActive()) {
            throw new IllegalArgumentException("From account is not active");
        }
        
        if (!account.hasSufficientFunds(transaction.getTotalAmount())) {
            throw new IllegalArgumentException("Insufficient funds in from account");
        }
    }
    
    private void validateCreditAccount(Transaction transaction) {
        Account account = accountRepository.findById(transaction.getToAccountId())
            .orElseThrow(() -> new IllegalArgumentException("To account not found"));
        
        if (!account.isActive()) {
            throw new IllegalArgumentException("To account is not active");
        }
    }
    
    private Transaction.TransactionType determineTransactionType(CreateTransactionRequest request) {
        if (request.getFromAccountId() != null && request.getToAccountId() != null) {
            return Transaction.TransactionType.TRANSFER;
        } else if (request.getFromAccountId() != null) {
            return Transaction.TransactionType.DEBIT;
        } else {
            return Transaction.TransactionType.CREDIT;
        }
    }
    
    private String generateTransactionReference() {
        return "TXN-" + System.currentTimeMillis() + "-" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private TransactionCreatedEvent createTransactionCreatedEvent(Transaction transaction, CreateTransactionRequest request) {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
            transaction.getId(),
            transaction.getTransactionReference(),
            transaction.getFromAccountId(),
            transaction.getToAccountId(),
            transaction.getPaymentTypeId(),
            transaction.getAmount(),
            transaction.getCurrencyCode(),
            transaction.getTransactionType().name()
        );
        
        event.setExternalReference(transaction.getExternalReference());
        event.setFeeAmount(transaction.getFeeAmount());
        event.setDescription(transaction.getDescription());
        event.setMetadata(transaction.getMetadata());
        event.setChannel(request.getChannel());
        event.setIpAddress(request.getIpAddress());
        event.setDeviceId(request.getDeviceId());
        
        return event;
    }
    
    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setTransactionReference(transaction.getTransactionReference());
        response.setExternalReference(transaction.getExternalReference());
        response.setFromAccountId(transaction.getFromAccountId());
        response.setToAccountId(transaction.getToAccountId());
        response.setPaymentTypeId(transaction.getPaymentTypeId());
        response.setAmount(transaction.getAmount());
        response.setCurrencyCode(transaction.getCurrencyCode());
        response.setFeeAmount(transaction.getFeeAmount());
        response.setStatus(transaction.getStatus());
        response.setTransactionType(transaction.getTransactionType());
        response.setDescription(transaction.getDescription());
        response.setMetadata(transaction.getMetadata());
        response.setInitiatedAt(transaction.getInitiatedAt());
        response.setProcessedAt(transaction.getProcessedAt());
        response.setCompletedAt(transaction.getCompletedAt());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        
        return response;
    }
}