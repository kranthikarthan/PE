package com.paymentengine.corebanking.service;

import com.paymentengine.corebanking.dto.CreateTransactionRequest;
import com.paymentengine.corebanking.dto.TransactionResponse;
import com.paymentengine.corebanking.entity.Transaction;
import com.paymentengine.corebanking.repository.OptimizedTransactionRepository;
import com.paymentengine.shared.constants.KafkaTopics;
import com.paymentengine.shared.event.TransactionCreatedEvent;
import com.paymentengine.shared.util.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Batch Transaction Processor for High TPS
 * 
 * Optimized for processing large volumes of transactions efficiently
 * using batch operations and parallel processing.
 */
@Service
public class BatchTransactionProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchTransactionProcessor.class);
    private static final int BATCH_SIZE = 100;
    private static final int MAX_PARALLEL_BATCHES = 10;
    
    private final OptimizedTransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final EventPublisher eventPublisher;
    
    @Autowired
    public BatchTransactionProcessor(
            OptimizedTransactionRepository transactionRepository,
            TransactionService transactionService,
            EventPublisher eventPublisher) {
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Process transactions in batches for high throughput
     */
    @Async("batchProcessingExecutor")
    @Transactional
    public CompletableFuture<BatchProcessingResult> processBatch(
            List<CreateTransactionRequest> requests) {
        
        logger.info("Processing batch of {} transactions", requests.size());
        
        long startTime = System.currentTimeMillis();
        BatchProcessingResult result = new BatchProcessingResult();
        
        try {
            // Group transactions by tenant for parallel processing
            Map<String, List<CreateTransactionRequest>> tenantGroups = 
                requests.stream()
                    .collect(Collectors.groupingBy(
                        req -> req.getTenantId() != null ? req.getTenantId() : "default"
                    ));
            
            // Process each tenant group in parallel
            List<CompletableFuture<TenantBatchResult>> futures = tenantGroups.entrySet()
                .stream()
                .map(entry -> processTenantBatch(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
            
            // Wait for all tenant batches to complete
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            allFutures.get(); // Wait for completion
            
            // Collect results
            for (CompletableFuture<TenantBatchResult> future : futures) {
                TenantBatchResult tenantResult = future.get();
                result.addTenantResult(tenantResult);
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            result.setProcessingTimeMs(processingTime);
            result.setSuccess(true);
            
            logger.info("Batch processing completed in {}ms. Success: {}, Failed: {}", 
                       processingTime, result.getSuccessCount(), result.getFailureCount());
            
        } catch (Exception e) {
            logger.error("Error processing batch: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return CompletableFuture.completedFuture(result);
    }
    
    /**
     * Process transactions for a specific tenant
     */
    @Async("batchProcessingExecutor")
    private CompletableFuture<TenantBatchResult> processTenantBatch(
            String tenantId, List<CreateTransactionRequest> requests) {
        
        logger.debug("Processing {} transactions for tenant: {}", requests.size(), tenantId);
        
        TenantBatchResult result = new TenantBatchResult(tenantId);
        
        try {
            // Split into smaller batches for processing
            List<List<CreateTransactionRequest>> batches = splitIntoBatches(requests, BATCH_SIZE);
            
            for (List<CreateTransactionRequest> batch : batches) {
                TenantBatchResult batchResult = processSingleBatch(tenantId, batch);
                result.merge(batchResult);
            }
            
        } catch (Exception e) {
            logger.error("Error processing tenant batch for {}: {}", tenantId, e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return CompletableFuture.completedFuture(result);
    }
    
    /**
     * Process a single batch of transactions
     */
    @Transactional
    private TenantBatchResult processSingleBatch(
            String tenantId, List<CreateTransactionRequest> batch) {
        
        TenantBatchResult result = new TenantBatchResult(tenantId);
        
        try {
            // Prepare batch data for database insertion
            BatchInsertData insertData = prepareBatchInsertData(batch);
            
            // Insert transactions in batch
            transactionRepository.batchInsertTransactions(
                insertData.getIds(),
                insertData.getReferences(),
                insertData.getExternalReferences(),
                insertData.getFromAccountIds(),
                insertData.getToAccountIds(),
                insertData.getPaymentTypeIds(),
                insertData.getAmounts(),
                insertData.getCurrencyCodes(),
                insertData.getFeeAmounts(),
                insertData.getStatuses(),
                insertData.getTransactionTypes(),
                insertData.getDescriptions(),
                insertData.getMetadata(),
                insertData.getInitiatedAts(),
                insertData.getProcessedAts(),
                insertData.getCompletedAts(),
                insertData.getCreatedAts(),
                insertData.getUpdatedAts(),
                insertData.getTenantIds()
            );
            
            // Publish events for each transaction
            for (int i = 0; i < batch.size(); i++) {
                CreateTransactionRequest request = batch.get(i);
                TransactionCreatedEvent event = createTransactionEvent(
                    insertData.getIds()[i],
                    insertData.getReferences()[i],
                    request
                );
                
                eventPublisher.publishEvent(
                    KafkaTopics.TRANSACTION_CREATED,
                    insertData.getReferences()[i],
                    event
                );
            }
            
            result.setSuccessCount(batch.size());
            result.setSuccess(true);
            
        } catch (Exception e) {
            logger.error("Error processing single batch: {}", e.getMessage(), e);
            result.setFailureCount(batch.size());
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Prepare batch insert data for database operation
     */
    private BatchInsertData prepareBatchInsertData(List<CreateTransactionRequest> requests) {
        int size = requests.size();
        
        UUID[] ids = new UUID[size];
        String[] references = new String[size];
        String[] externalReferences = new String[size];
        UUID[] fromAccountIds = new UUID[size];
        UUID[] toAccountIds = new UUID[size];
        UUID[] paymentTypeIds = new UUID[size];
        BigDecimal[] amounts = new BigDecimal[size];
        String[] currencyCodes = new String[size];
        BigDecimal[] feeAmounts = new BigDecimal[size];
        String[] statuses = new String[size];
        String[] transactionTypes = new String[size];
        String[] descriptions = new String[size];
        String[] metadata = new String[size];
        LocalDateTime[] initiatedAts = new LocalDateTime[size];
        LocalDateTime[] processedAts = new LocalDateTime[size];
        LocalDateTime[] completedAts = new LocalDateTime[size];
        LocalDateTime[] createdAts = new LocalDateTime[size];
        LocalDateTime[] updatedAts = new LocalDateTime[size];
        String[] tenantIds = new String[size];
        
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < size; i++) {
            CreateTransactionRequest request = requests.get(i);
            
            ids[i] = UUID.randomUUID();
            references[i] = generateTransactionReference();
            externalReferences[i] = request.getExternalReference();
            fromAccountIds[i] = request.getFromAccountId();
            toAccountIds[i] = request.getToAccountId();
            paymentTypeIds[i] = request.getPaymentTypeId();
            amounts[i] = request.getAmount();
            currencyCodes[i] = request.getCurrencyCode();
            feeAmounts[i] = BigDecimal.ZERO; // Calculate fee separately
            statuses[i] = "PENDING";
            transactionTypes[i] = determineTransactionType(request).name();
            descriptions[i] = request.getDescription();
            metadata[i] = request.getMetadata() != null ? 
                request.getMetadata().toString() : "{}";
            initiatedAts[i] = now;
            processedAts[i] = null;
            completedAts[i] = null;
            createdAts[i] = now;
            updatedAts[i] = now;
            tenantIds[i] = request.getTenantId() != null ? 
                request.getTenantId() : "default";
        }
        
        return new BatchInsertData(
            ids, references, externalReferences, fromAccountIds, toAccountIds,
            paymentTypeIds, amounts, currencyCodes, feeAmounts, statuses,
            transactionTypes, descriptions, metadata, initiatedAts,
            processedAts, completedAts, createdAts, updatedAts, tenantIds
        );
    }
    
    /**
     * Split list into batches of specified size
     */
    private <T> List<List<T>> splitIntoBatches(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        
        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            batches.add(list.subList(i, end));
        }
        
        return batches;
    }
    
    /**
     * Generate unique transaction reference
     */
    private String generateTransactionReference() {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "TXN-" + uuid + "-" + timestamp;
    }
    
    /**
     * Determine transaction type from request
     */
    private Transaction.TransactionType determineTransactionType(CreateTransactionRequest request) {
        if (request.getFromAccountId() != null && request.getToAccountId() != null) {
            return Transaction.TransactionType.TRANSFER;
        } else if (request.getFromAccountId() != null) {
            return Transaction.TransactionType.DEBIT;
        } else {
            return Transaction.TransactionType.CREDIT;
        }
    }
    
    /**
     * Create transaction event for publishing
     */
    private TransactionCreatedEvent createTransactionEvent(
            UUID transactionId, String reference, CreateTransactionRequest request) {
        
        TransactionCreatedEvent event = new TransactionCreatedEvent(
            transactionId,
            reference,
            request.getFromAccountId(),
            request.getToAccountId(),
            request.getPaymentTypeId(),
            request.getAmount(),
            request.getCurrencyCode(),
            determineTransactionType(request).name()
        );
        
        event.setExternalReference(request.getExternalReference());
        event.setDescription(request.getDescription());
        event.setMetadata(request.getMetadata());
        event.setChannel("batch-processing");
        
        return event;
    }
    
    /**
     * Batch processing result
     */
    public static class BatchProcessingResult {
        private boolean success = true;
        private String errorMessage;
        private long processingTimeMs;
        private int totalCount = 0;
        private int successCount = 0;
        private int failureCount = 0;
        private List<TenantBatchResult> tenantResults = new ArrayList<>();
        
        public void addTenantResult(TenantBatchResult tenantResult) {
            tenantResults.add(tenantResult);
            totalCount += tenantResult.getTotalCount();
            successCount += tenantResult.getSuccessCount();
            failureCount += tenantResult.getFailureCount();
        }
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public long getProcessingTimeMs() { return processingTimeMs; }
        public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
        
        public int getTotalCount() { return totalCount; }
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        
        public List<TenantBatchResult> getTenantResults() { return tenantResults; }
    }
    
    /**
     * Tenant batch processing result
     */
    public static class TenantBatchResult {
        private String tenantId;
        private boolean success = true;
        private String errorMessage;
        private int successCount = 0;
        private int failureCount = 0;
        
        public TenantBatchResult(String tenantId) {
            this.tenantId = tenantId;
        }
        
        public void merge(TenantBatchResult other) {
            this.successCount += other.successCount;
            this.failureCount += other.failureCount;
            if (!other.success) {
                this.success = false;
                this.errorMessage = other.errorMessage;
            }
        }
        
        // Getters and setters
        public String getTenantId() { return tenantId; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
        
        public int getTotalCount() { return successCount + failureCount; }
    }
    
    /**
     * Batch insert data container
     */
    private static class BatchInsertData {
        private final UUID[] ids;
        private final String[] references;
        private final String[] externalReferences;
        private final UUID[] fromAccountIds;
        private final UUID[] toAccountIds;
        private final UUID[] paymentTypeIds;
        private final BigDecimal[] amounts;
        private final String[] currencyCodes;
        private final BigDecimal[] feeAmounts;
        private final String[] statuses;
        private final String[] transactionTypes;
        private final String[] descriptions;
        private final String[] metadata;
        private final LocalDateTime[] initiatedAts;
        private final LocalDateTime[] processedAts;
        private final LocalDateTime[] completedAts;
        private final LocalDateTime[] createdAts;
        private final LocalDateTime[] updatedAts;
        private final String[] tenantIds;
        
        public BatchInsertData(UUID[] ids, String[] references, String[] externalReferences,
                              UUID[] fromAccountIds, UUID[] toAccountIds, UUID[] paymentTypeIds,
                              BigDecimal[] amounts, String[] currencyCodes, BigDecimal[] feeAmounts,
                              String[] statuses, String[] transactionTypes, String[] descriptions,
                              String[] metadata, LocalDateTime[] initiatedAts, LocalDateTime[] processedAts,
                              LocalDateTime[] completedAts, LocalDateTime[] createdAts,
                              LocalDateTime[] updatedAts, String[] tenantIds) {
            this.ids = ids;
            this.references = references;
            this.externalReferences = externalReferences;
            this.fromAccountIds = fromAccountIds;
            this.toAccountIds = toAccountIds;
            this.paymentTypeIds = paymentTypeIds;
            this.amounts = amounts;
            this.currencyCodes = currencyCodes;
            this.feeAmounts = feeAmounts;
            this.statuses = statuses;
            this.transactionTypes = transactionTypes;
            this.descriptions = descriptions;
            this.metadata = metadata;
            this.initiatedAts = initiatedAts;
            this.processedAts = processedAts;
            this.completedAts = completedAts;
            this.createdAts = createdAts;
            this.updatedAts = updatedAts;
            this.tenantIds = tenantIds;
        }
        
        // Getters
        public UUID[] getIds() { return ids; }
        public String[] getReferences() { return references; }
        public String[] getExternalReferences() { return externalReferences; }
        public UUID[] getFromAccountIds() { return fromAccountIds; }
        public UUID[] getToAccountIds() { return toAccountIds; }
        public UUID[] getPaymentTypeIds() { return paymentTypeIds; }
        public BigDecimal[] getAmounts() { return amounts; }
        public String[] getCurrencyCodes() { return currencyCodes; }
        public BigDecimal[] getFeeAmounts() { return feeAmounts; }
        public String[] getStatuses() { return statuses; }
        public String[] getTransactionTypes() { return transactionTypes; }
        public String[] getDescriptions() { return descriptions; }
        public String[] getMetadata() { return metadata; }
        public LocalDateTime[] getInitiatedAts() { return initiatedAts; }
        public LocalDateTime[] getProcessedAts() { return processedAts; }
        public LocalDateTime[] getCompletedAts() { return completedAts; }
        public LocalDateTime[] getCreatedAts() { return createdAts; }
        public LocalDateTime[] getUpdatedAts() { return updatedAts; }
        public String[] getTenantIds() { return tenantIds; }
    }
}