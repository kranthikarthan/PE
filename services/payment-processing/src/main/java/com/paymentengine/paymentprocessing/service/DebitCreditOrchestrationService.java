package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.dto.corebanking.*;
import com.paymentengine.paymentprocessing.entity.TransactionRepair;
import com.paymentengine.paymentprocessing.repository.TransactionRepairRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Debit-Credit Orchestration Service
 * 
 * Orchestrates debit and credit API calls to external core banking systems
 * with comprehensive failure handling, timeout management, and transaction repair.
 */
@Service
public class DebitCreditOrchestrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DebitCreditOrchestrationService.class);
    
    @Autowired
    private PaymentRoutingService paymentRoutingService;
    
    @Autowired
    private TransactionRepairService transactionRepairService;
    
    @Autowired
    private TransactionRepairRepository transactionRepairRepository;
    
    @Autowired
    @Qualifier("restCoreBankingAdapter")
    private CoreBankingAdapter coreBankingAdapter;
    
    /**
     * Process debit-credit transaction with orchestration
     */
    @Transactional
    public CompletableFuture<OrchestrationResult> processDebitCreditTransaction(
            DebitCreditTransactionRequest request) {
        
        logger.info("Starting debit-credit orchestration for transaction: {}", request.getTransactionReference());
        
        return CompletableFuture.supplyAsync(() -> {
            OrchestrationResult result = new OrchestrationResult();
            result.setTransactionReference(request.getTransactionReference());
            result.setStartTime(LocalDateTime.now());
            
            try {
                // Step 1: Process Debit
                DebitResult debitResult = processDebit(request);
                result.setDebitResult(debitResult);
                
                if (debitResult.isSuccess()) {
                    // Step 2: Process Credit (only if debit succeeded)
                    CreditResult creditResult = processCredit(request, debitResult);
                    result.setCreditResult(creditResult);
                    
                    if (creditResult.isSuccess()) {
                        // Both operations successful
                        result.setStatus(OrchestrationStatus.SUCCESS);
                        result.setMessage("Both debit and credit operations completed successfully");
                        logger.info("Debit-credit orchestration completed successfully for transaction: {}", 
                                   request.getTransactionReference());
                    } else {
                        // Debit succeeded but credit failed - need repair
                        handleCreditFailure(request, debitResult, creditResult);
                        result.setStatus(OrchestrationStatus.PARTIAL_SUCCESS);
                        result.setMessage("Debit succeeded but credit failed - marked for repair");
                        logger.warn("Credit failed after successful debit for transaction: {} - marked for repair", 
                                   request.getTransactionReference());
                    }
                } else {
                    // Debit failed - stop credit processing
                    handleDebitFailure(request, debitResult);
                    result.setStatus(OrchestrationStatus.FAILED);
                    result.setMessage("Debit failed - credit processing stopped");
                    logger.warn("Debit failed for transaction: {} - credit processing stopped", 
                               request.getTransactionReference());
                }
                
            } catch (Exception e) {
                logger.error("Error in debit-credit orchestration for transaction {}: {}", 
                           request.getTransactionReference(), e.getMessage(), e);
                result.setStatus(OrchestrationStatus.ERROR);
                result.setMessage("Orchestration error: " + e.getMessage());
                result.setError(e);
                
                // Create repair record for system error
                createRepairRecord(request, TransactionRepair.RepairType.SYSTEM_ERROR, 
                                 "System error during orchestration", e.getMessage(), null, null);
            }
            
            result.setEndTime(LocalDateTime.now());
            return result;
            
        }).orTimeout(request.getTimeoutSeconds(), TimeUnit.SECONDS)
          .exceptionally(throwable -> {
              // Handle timeout
              logger.error("Debit-credit orchestration timed out for transaction: {}", 
                         request.getTransactionReference());
              
              OrchestrationResult timeoutResult = new OrchestrationResult();
              timeoutResult.setTransactionReference(request.getTransactionReference());
              timeoutResult.setStatus(OrchestrationStatus.TIMEOUT);
              timeoutResult.setMessage("Orchestration timed out");
              timeoutResult.setStartTime(LocalDateTime.now());
              timeoutResult.setEndTime(LocalDateTime.now());
              
              // Create repair record for timeout
              createRepairRecord(request, TransactionRepair.RepairType.DEBIT_TIMEOUT, 
                               "Orchestration timeout", "TIMEOUT", null, null);
              
              return timeoutResult;
          });
    }
    
    /**
     * Process debit operation
     */
    private DebitResult processDebit(DebitCreditTransactionRequest request) {
        logger.debug("Processing debit for transaction: {}", request.getTransactionReference());
        
        try {
            // Create debit request
            DebitTransactionRequest debitRequest = new DebitTransactionRequest();
            debitRequest.setTransactionReference(request.getTransactionReference() + "-DEBIT");
            debitRequest.setAccountNumber(request.getFromAccountNumber());
            debitRequest.setAmount(request.getAmount());
            debitRequest.setCurrency(request.getCurrency());
            debitRequest.setDescription(request.getDescription());
            debitRequest.setTenantId(request.getTenantId());
            debitRequest.setPaymentType(request.getPaymentType());
            
            // Process debit with timeout
            CompletableFuture<TransactionResult> debitFuture = CompletableFuture
                .supplyAsync(() -> coreBankingAdapter.processDebit(debitRequest))
                .orTimeout(request.getDebitTimeoutSeconds(), TimeUnit.SECONDS);
            
            TransactionResult debitResult = debitFuture.get();
            
            DebitResult result = new DebitResult();
            result.setSuccess(debitResult.isSuccess());
            result.setTransactionReference(debitResult.getTransactionReference());
            result.setCoreBankingReference(debitResult.getCoreBankingReference());
            result.setResponse(debitResult);
            result.setProcessedAt(LocalDateTime.now());
            
            if (!debitResult.isSuccess()) {
                result.setErrorCode(debitResult.getErrorCode());
                result.setErrorMessage(debitResult.getErrorMessage());
            }
            
            return result;
            
        } catch (java.util.concurrent.TimeoutException e) {
            logger.error("Debit operation timed out for transaction: {}", request.getTransactionReference());
            
            DebitResult result = new DebitResult();
            result.setSuccess(false);
            result.setErrorCode("DEBIT_TIMEOUT");
            result.setErrorMessage("Debit operation timed out");
            result.setProcessedAt(LocalDateTime.now());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error processing debit for transaction {}: {}", 
                        request.getTransactionReference(), e.getMessage());
            
            DebitResult result = new DebitResult();
            result.setSuccess(false);
            result.setErrorCode("DEBIT_ERROR");
            result.setErrorMessage(e.getMessage());
            result.setProcessedAt(LocalDateTime.now());
            
            return result;
        }
    }
    
    /**
     * Process credit operation
     */
    private CreditResult processCredit(DebitCreditTransactionRequest request, DebitResult debitResult) {
        logger.debug("Processing credit for transaction: {}", request.getTransactionReference());
        
        try {
            // Create credit request
            CreditTransactionRequest creditRequest = new CreditTransactionRequest();
            creditRequest.setTransactionReference(request.getTransactionReference() + "-CREDIT");
            creditRequest.setAccountNumber(request.getToAccountNumber());
            creditRequest.setAmount(request.getAmount());
            creditRequest.setCurrency(request.getCurrency());
            creditRequest.setDescription(request.getDescription());
            creditRequest.setTenantId(request.getTenantId());
            creditRequest.setPaymentType(request.getPaymentType());
            
            // Process credit with timeout
            CompletableFuture<TransactionResult> creditFuture = CompletableFuture
                .supplyAsync(() -> coreBankingAdapter.processCredit(creditRequest))
                .orTimeout(request.getCreditTimeoutSeconds(), TimeUnit.SECONDS);
            
            TransactionResult creditResult = creditFuture.get();
            
            CreditResult result = new CreditResult();
            result.setSuccess(creditResult.isSuccess());
            result.setTransactionReference(creditResult.getTransactionReference());
            result.setCoreBankingReference(creditResult.getCoreBankingReference());
            result.setResponse(creditResult);
            result.setProcessedAt(LocalDateTime.now());
            
            if (!creditResult.isSuccess()) {
                result.setErrorCode(creditResult.getErrorCode());
                result.setErrorMessage(creditResult.getErrorMessage());
            }
            
            return result;
            
        } catch (java.util.concurrent.TimeoutException e) {
            logger.error("Credit operation timed out for transaction: {}", request.getTransactionReference());
            
            CreditResult result = new CreditResult();
            result.setSuccess(false);
            result.setErrorCode("CREDIT_TIMEOUT");
            result.setErrorMessage("Credit operation timed out");
            result.setProcessedAt(LocalDateTime.now());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error processing credit for transaction {}: {}", 
                        request.getTransactionReference(), e.getMessage());
            
            CreditResult result = new CreditResult();
            result.setSuccess(false);
            result.setErrorCode("CREDIT_ERROR");
            result.setErrorMessage(e.getMessage());
            result.setProcessedAt(LocalDateTime.now());
            
            return result;
        }
    }
    
    /**
     * Handle debit failure
     */
    private void handleDebitFailure(DebitCreditTransactionRequest request, DebitResult debitResult) {
        logger.warn("Handling debit failure for transaction: {}", request.getTransactionReference());
        
        TransactionRepair.RepairType repairType = TransactionRepair.RepairType.DEBIT_FAILED;
        if ("DEBIT_TIMEOUT".equals(debitResult.getErrorCode())) {
            repairType = TransactionRepair.RepairType.DEBIT_TIMEOUT;
        }
        
        createRepairRecord(request, repairType, 
                         "Debit operation failed", 
                         debitResult.getErrorCode(), 
                         debitResult.getErrorMessage(),
                         debitResult.getResponse());
    }
    
    /**
     * Handle credit failure
     */
    private void handleCreditFailure(DebitCreditTransactionRequest request, 
                                   DebitResult debitResult, 
                                   CreditResult creditResult) {
        logger.warn("Handling credit failure for transaction: {}", request.getTransactionReference());
        
        TransactionRepair.RepairType repairType = TransactionRepair.RepairType.CREDIT_FAILED;
        if ("CREDIT_TIMEOUT".equals(creditResult.getErrorCode())) {
            repairType = TransactionRepair.RepairType.CREDIT_TIMEOUT;
        }
        
        // Create repair record with both debit and credit information
        TransactionRepair repair = createRepairRecord(request, repairType, 
                                                    "Credit operation failed after successful debit", 
                                                    creditResult.getErrorCode(), 
                                                    creditResult.getErrorMessage(),
                                                    creditResult.getResponse());
        
        // Set debit information since it was successful
        if (repair != null) {
            repair.setDebitStatus(TransactionRepair.DebitCreditStatus.SUCCESS);
            repair.setDebitReference(debitResult.getCoreBankingReference());
            repair.setDebitResponse(debitResult.getResponse() != null ? 
                Map.of("status", debitResult.getResponse().getStatus().name(),
                      "transactionReference", debitResult.getResponse().getTransactionReference(),
                      "coreBankingReference", debitResult.getResponse().getCoreBankingReference()) : null);
            
            transactionRepairRepository.save(repair);
        }
    }
    
    /**
     * Create repair record
     */
    private TransactionRepair createRepairRecord(DebitCreditTransactionRequest request,
                                               TransactionRepair.RepairType repairType,
                                               String failureReason,
                                               String errorCode,
                                               String errorMessage,
                                               Object response) {
        try {
            TransactionRepair repair = new TransactionRepair();
            repair.setTransactionReference(request.getTransactionReference());
            repair.setTenantId(request.getTenantId());
            repair.setRepairType(repairType);
            repair.setRepairStatus(TransactionRepair.RepairStatus.PENDING);
            repair.setFailureReason(failureReason);
            repair.setErrorCode(errorCode);
            repair.setErrorMessage(errorMessage);
            repair.setFromAccountNumber(request.getFromAccountNumber());
            repair.setToAccountNumber(request.getToAccountNumber());
            repair.setAmount(request.getAmount());
            repair.setCurrency(request.getCurrency());
            repair.setPaymentType(request.getPaymentType());
            repair.setOriginalRequest(Map.of(
                "transactionReference", request.getTransactionReference(),
                "fromAccountNumber", request.getFromAccountNumber(),
                "toAccountNumber", request.getToAccountNumber(),
                "amount", request.getAmount(),
                "currency", request.getCurrency(),
                "paymentType", request.getPaymentType(),
                "description", request.getDescription()
            ));
            repair.setMaxRetries(request.getMaxRetries());
            repair.setPriority(request.getPriority());
            repair.setTimeoutAt(LocalDateTime.now().plusHours(request.getTimeoutHours()));
            
            // Set response information
            if (response instanceof TransactionResult) {
                TransactionResult tr = (TransactionResult) response;
                if (repairType == TransactionRepair.RepairType.DEBIT_FAILED || 
                    repairType == TransactionRepair.RepairType.DEBIT_TIMEOUT) {
                    repair.setDebitResponse(Map.of(
                        "status", tr.getStatus().name(),
                        "transactionReference", tr.getTransactionReference(),
                        "coreBankingReference", tr.getCoreBankingReference(),
                        "errorCode", tr.getErrorCode(),
                        "errorMessage", tr.getErrorMessage()
                    ));
                } else if (repairType == TransactionRepair.RepairType.CREDIT_FAILED || 
                          repairType == TransactionRepair.RepairType.CREDIT_TIMEOUT) {
                    repair.setCreditResponse(Map.of(
                        "status", tr.getStatus().name(),
                        "transactionReference", tr.getTransactionReference(),
                        "coreBankingReference", tr.getCoreBankingReference(),
                        "errorCode", tr.getErrorCode(),
                        "errorMessage", tr.getErrorMessage()
                    ));
                }
            }
            
            repair = transactionRepairRepository.save(repair);
            logger.info("Created repair record for transaction: {} with type: {}", 
                       request.getTransactionReference(), repairType);
            
            return repair;
            
        } catch (Exception e) {
            logger.error("Error creating repair record for transaction {}: {}", 
                        request.getTransactionReference(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Debit-Credit Transaction Request DTO
     */
    public static class DebitCreditTransactionRequest {
        private String transactionReference;
        private String fromAccountNumber;
        private String toAccountNumber;
        private BigDecimal amount;
        private String currency;
        private String description;
        private String tenantId;
        private String paymentType;
        private int timeoutSeconds = 300; // 5 minutes default
        private int debitTimeoutSeconds = 60; // 1 minute default
        private int creditTimeoutSeconds = 60; // 1 minute default
        private int maxRetries = 3;
        private int priority = 1;
        private int timeoutHours = 24; // 24 hours default
        
        // Getters and Setters
        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
        
        public String getFromAccountNumber() { return fromAccountNumber; }
        public void setFromAccountNumber(String fromAccountNumber) { this.fromAccountNumber = fromAccountNumber; }
        
        public String getToAccountNumber() { return toAccountNumber; }
        public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        
        public String getPaymentType() { return paymentType; }
        public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
        
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
        
        public int getDebitTimeoutSeconds() { return debitTimeoutSeconds; }
        public void setDebitTimeoutSeconds(int debitTimeoutSeconds) { this.debitTimeoutSeconds = debitTimeoutSeconds; }
        
        public int getCreditTimeoutSeconds() { return creditTimeoutSeconds; }
        public void setCreditTimeoutSeconds(int creditTimeoutSeconds) { this.creditTimeoutSeconds = creditTimeoutSeconds; }
        
        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
        
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
        
        public int getTimeoutHours() { return timeoutHours; }
        public void setTimeoutHours(int timeoutHours) { this.timeoutHours = timeoutHours; }
    }
    
    /**
     * Orchestration Result DTO
     */
    public static class OrchestrationResult {
        private String transactionReference;
        private OrchestrationStatus status;
        private String message;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private DebitResult debitResult;
        private CreditResult creditResult;
        private Exception error;
        
        // Getters and Setters
        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
        
        public OrchestrationStatus getStatus() { return status; }
        public void setStatus(OrchestrationStatus status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public DebitResult getDebitResult() { return debitResult; }
        public void setDebitResult(DebitResult debitResult) { this.debitResult = debitResult; }
        
        public CreditResult getCreditResult() { return creditResult; }
        public void setCreditResult(CreditResult creditResult) { this.creditResult = creditResult; }
        
        public Exception getError() { return error; }
        public void setError(Exception error) { this.error = error; }
    }
    
    /**
     * Debit Result DTO
     */
    public static class DebitResult {
        private boolean success;
        private String transactionReference;
        private String coreBankingReference;
        private String errorCode;
        private String errorMessage;
        private LocalDateTime processedAt;
        private TransactionResult response;
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
        
        public String getCoreBankingReference() { return coreBankingReference; }
        public void setCoreBankingReference(String coreBankingReference) { this.coreBankingReference = coreBankingReference; }
        
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
        
        public TransactionResult getResponse() { return response; }
        public void setResponse(TransactionResult response) { this.response = response; }
    }
    
    /**
     * Credit Result DTO
     */
    public static class CreditResult {
        private boolean success;
        private String transactionReference;
        private String coreBankingReference;
        private String errorCode;
        private String errorMessage;
        private LocalDateTime processedAt;
        private TransactionResult response;
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
        
        public String getCoreBankingReference() { return coreBankingReference; }
        public void setCoreBankingReference(String coreBankingReference) { this.coreBankingReference = coreBankingReference; }
        
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
        
        public TransactionResult getResponse() { return response; }
        public void setResponse(TransactionResult response) { this.response = response; }
    }
    
    /**
     * Orchestration Status Enumeration
     */
    public enum OrchestrationStatus {
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILED,
        TIMEOUT,
        ERROR
    }
}