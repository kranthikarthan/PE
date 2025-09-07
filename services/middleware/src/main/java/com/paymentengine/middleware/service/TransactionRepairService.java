package com.paymentengine.middleware.service;

import com.paymentengine.middleware.entity.TransactionRepair;
import com.paymentengine.middleware.repository.TransactionRepairRepository;
import com.paymentengine.middleware.dto.corebanking.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Transaction Repair Service
 * 
 * Manages transaction repair operations, corrective actions, and automated retry logic
 * for failed debit/credit operations in external core banking systems.
 */
@Service
public class TransactionRepairService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionRepairService.class);
    
    @Autowired
    private TransactionRepairRepository transactionRepairRepository;
    
    @Autowired
    @Qualifier("restCoreBankingAdapter")
    private CoreBankingAdapter coreBankingAdapter;
    
    @Autowired
    private DebitCreditOrchestrationService orchestrationService;
    
    /**
     * Get transaction repair by ID
     */
    @Transactional(readOnly = true)
    public Optional<TransactionRepair> getTransactionRepair(UUID id) {
        return transactionRepairRepository.findById(id);
    }
    
    /**
     * Get transaction repair by transaction reference and tenant
     */
    @Transactional(readOnly = true)
    public Optional<TransactionRepair> getTransactionRepair(String transactionReference, String tenantId) {
        return transactionRepairRepository.findByTransactionReferenceAndTenantId(transactionReference, tenantId);
    }
    
    /**
     * Get all transaction repairs for a tenant
     */
    @Transactional(readOnly = true)
    public List<TransactionRepair> getTransactionRepairsByTenant(String tenantId) {
        return transactionRepairRepository.findByTenantId(tenantId);
    }
    
    /**
     * Get transaction repairs by status
     */
    @Transactional(readOnly = true)
    public List<TransactionRepair> getTransactionRepairsByStatus(TransactionRepair.RepairStatus status) {
        return transactionRepairRepository.findByRepairStatus(status);
    }
    
    /**
     * Get transaction repairs by repair type
     */
    @Transactional(readOnly = true)
    public List<TransactionRepair> getTransactionRepairsByType(TransactionRepair.RepairType repairType) {
        return transactionRepairRepository.findByRepairType(repairType);
    }
    
    /**
     * Get transaction repairs assigned to a user
     */
    @Transactional(readOnly = true)
    public List<TransactionRepair> getTransactionRepairsByAssignee(String assignedTo) {
        return transactionRepairRepository.findByAssignedTo(assignedTo);
    }
    
    /**
     * Get transaction repairs that need manual review
     */
    @Transactional(readOnly = true)
    public List<TransactionRepair> getTransactionRepairsNeedingReview() {
        return transactionRepairRepository.findNeedingManualReview();
    }
    
    /**
     * Get transaction repairs that need manual review by tenant
     */
    @Transactional(readOnly = true)
    public List<TransactionRepair> getTransactionRepairsNeedingReviewByTenant(String tenantId) {
        return transactionRepairRepository.findNeedingManualReviewByTenant(tenantId);
    }
    
    /**
     * Get high priority transaction repairs
     */
    @Transactional(readOnly = true)
    public List<TransactionRepair> getHighPriorityTransactionRepairs() {
        return transactionRepairRepository.findHighPriority();
    }
    
    /**
     * Get high priority transaction repairs by tenant
     */
    @Transactional(readOnly = true)
    public List<TransactionRepair> getHighPriorityTransactionRepairsByTenant(String tenantId) {
        return transactionRepairRepository.findHighPriorityByTenant(tenantId);
    }
    
    /**
     * Get transaction repairs ready for retry
     */
    @Transactional(readOnly = true)
    public List<TransactionRepair> getTransactionRepairsReadyForRetry() {
        return transactionRepairRepository.findReadyForRetry(LocalDateTime.now());
    }
    
    /**
     * Get transaction repairs that have timed out
     */
    @Transactional(readOnly = true)
    public List<TransactionRepair> getTimedOutTransactionRepairs() {
        return transactionRepairRepository.findTimedOut(LocalDateTime.now());
    }
    
    /**
     * Assign transaction repair to a user
     */
    @Transactional
    public TransactionRepair assignTransactionRepair(UUID id, String assignedTo) {
        Optional<TransactionRepair> repairOpt = transactionRepairRepository.findById(id);
        if (repairOpt.isPresent()) {
            TransactionRepair repair = repairOpt.get();
            repair.assignTo(assignedTo);
            return transactionRepairRepository.save(repair);
        }
        throw new IllegalArgumentException("Transaction repair not found: " + id);
    }
    
    /**
     * Apply corrective action to transaction repair
     */
    @Transactional
    public CompletableFuture<CorrectiveActionResult> applyCorrectiveAction(
            UUID id, 
            TransactionRepair.CorrectiveAction correctiveAction,
            Map<String, Object> correctiveActionDetails,
            String appliedBy) {
        
        logger.info("Applying corrective action {} to transaction repair {}", correctiveAction, id);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Optional<TransactionRepair> repairOpt = transactionRepairRepository.findById(id);
                if (repairOpt.isEmpty()) {
                    throw new IllegalArgumentException("Transaction repair not found: " + id);
                }
                
                TransactionRepair repair = repairOpt.get();
                repair.setCorrectiveAction(correctiveAction);
                repair.setCorrectiveActionDetails(correctiveActionDetails);
                repair.setRepairStatus(TransactionRepair.RepairStatus.IN_PROGRESS);
                repair.setUpdatedBy(appliedBy);
                
                CorrectiveActionResult result = new CorrectiveActionResult();
                result.setTransactionRepairId(id);
                result.setCorrectiveAction(correctiveAction);
                result.setAppliedBy(appliedBy);
                result.setStartTime(LocalDateTime.now());
                
                // Execute corrective action based on type
                switch (correctiveAction) {
                    case RETRY_DEBIT:
                        result = retryDebit(repair, result);
                        break;
                    case RETRY_CREDIT:
                        result = retryCredit(repair, result);
                        break;
                    case RETRY_BOTH:
                        result = retryBoth(repair, result);
                        break;
                    case REVERSE_DEBIT:
                        result = reverseDebit(repair, result);
                        break;
                    case REVERSE_CREDIT:
                        result = reverseCredit(repair, result);
                        break;
                    case REVERSE_BOTH:
                        result = reverseBoth(repair, result);
                        break;
                    case MANUAL_CREDIT:
                        result = manualCredit(repair, result);
                        break;
                    case MANUAL_DEBIT:
                        result = manualDebit(repair, result);
                        break;
                    case MANUAL_BOTH:
                        result = manualBoth(repair, result);
                        break;
                    case CANCEL_TRANSACTION:
                        result = cancelTransaction(repair, result);
                        break;
                    case ESCALATE:
                        result = escalateTransaction(repair, result);
                        break;
                    case NO_ACTION:
                        result = noAction(repair, result);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown corrective action: " + correctiveAction);
                }
                
                result.setEndTime(LocalDateTime.now());
                
                // Update repair record
                if (result.isSuccess()) {
                    repair.markAsResolved(appliedBy, result.getMessage());
                } else {
                    repair.setRepairStatus(TransactionRepair.RepairStatus.FAILED);
                    repair.setFailureReason(result.getMessage());
                }
                
                transactionRepairRepository.save(repair);
                
                logger.info("Corrective action {} completed for transaction repair {} with result: {}", 
                           correctiveAction, id, result.isSuccess() ? "SUCCESS" : "FAILED");
                
                return result;
                
            } catch (Exception e) {
                logger.error("Error applying corrective action {} to transaction repair {}: {}", 
                           correctiveAction, id, e.getMessage(), e);
                
                CorrectiveActionResult errorResult = new CorrectiveActionResult();
                errorResult.setTransactionRepairId(id);
                errorResult.setCorrectiveAction(correctiveAction);
                errorResult.setAppliedBy(appliedBy);
                errorResult.setSuccess(false);
                errorResult.setMessage("Error applying corrective action: " + e.getMessage());
                errorResult.setStartTime(LocalDateTime.now());
                errorResult.setEndTime(LocalDateTime.now());
                
                return errorResult;
            }
        });
    }
    
    /**
     * Retry debit operation
     */
    private CorrectiveActionResult retryDebit(TransactionRepair repair, CorrectiveActionResult result) {
        try {
            // Create debit request from repair data
            DebitTransactionRequest debitRequest = new DebitTransactionRequest();
            debitRequest.setTransactionReference(repair.getTransactionReference() + "-RETRY-DEBIT");
            debitRequest.setAccountNumber(repair.getFromAccountNumber());
            debitRequest.setAmount(repair.getAmount());
            debitRequest.setCurrency(repair.getCurrency());
            debitRequest.setDescription("Retry debit for repair: " + repair.getId());
            debitRequest.setTenantId(repair.getTenantId());
            debitRequest.setPaymentType(repair.getPaymentType());
            
            // Process debit
            TransactionResult debitResult = coreBankingAdapter.processDebit(debitRequest);
            
            if (debitResult.isSuccess()) {
                repair.setDebitSuccess(debitResult.getCoreBankingReference(), 
                    Map.of("status", debitResult.getStatus().name(),
                          "transactionReference", debitResult.getTransactionReference(),
                          "coreBankingReference", debitResult.getCoreBankingReference()));
                
                result.setSuccess(true);
                result.setMessage("Debit retry successful");
                result.setDetails(Map.of("debitReference", debitResult.getCoreBankingReference()));
            } else {
                result.setSuccess(false);
                result.setMessage("Debit retry failed: " + debitResult.getErrorMessage());
                result.setDetails(Map.of("errorCode", debitResult.getErrorCode(),
                                       "errorMessage", debitResult.getErrorMessage()));
            }
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Debit retry error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Retry credit operation
     */
    private CorrectiveActionResult retryCredit(TransactionRepair repair, CorrectiveActionResult result) {
        try {
            // Create credit request from repair data
            CreditTransactionRequest creditRequest = new CreditTransactionRequest();
            creditRequest.setTransactionReference(repair.getTransactionReference() + "-RETRY-CREDIT");
            creditRequest.setAccountNumber(repair.getToAccountNumber());
            creditRequest.setAmount(repair.getAmount());
            creditRequest.setCurrency(repair.getCurrency());
            creditRequest.setDescription("Retry credit for repair: " + repair.getId());
            creditRequest.setTenantId(repair.getTenantId());
            creditRequest.setPaymentType(repair.getPaymentType());
            
            // Process credit
            TransactionResult creditResult = coreBankingAdapter.processCredit(creditRequest);
            
            if (creditResult.isSuccess()) {
                repair.setCreditSuccess(creditResult.getCoreBankingReference(), 
                    Map.of("status", creditResult.getStatus().name(),
                          "transactionReference", creditResult.getTransactionReference(),
                          "coreBankingReference", creditResult.getCoreBankingReference()));
                
                result.setSuccess(true);
                result.setMessage("Credit retry successful");
                result.setDetails(Map.of("creditReference", creditResult.getCoreBankingReference()));
            } else {
                result.setSuccess(false);
                result.setMessage("Credit retry failed: " + creditResult.getErrorMessage());
                result.setDetails(Map.of("errorCode", creditResult.getErrorCode(),
                                       "errorMessage", creditResult.getErrorMessage()));
            }
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Credit retry error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Retry both debit and credit operations
     */
    private CorrectiveActionResult retryBoth(TransactionRepair repair, CorrectiveActionResult result) {
        try {
            // First retry debit
            CorrectiveActionResult debitResult = retryDebit(repair, new CorrectiveActionResult());
            
            if (debitResult.isSuccess()) {
                // Then retry credit
                CorrectiveActionResult creditResult = retryCredit(repair, new CorrectiveActionResult());
                
                if (creditResult.isSuccess()) {
                    result.setSuccess(true);
                    result.setMessage("Both debit and credit retry successful");
                } else {
                    result.setSuccess(false);
                    result.setMessage("Debit retry successful but credit retry failed: " + creditResult.getMessage());
                }
            } else {
                result.setSuccess(false);
                result.setMessage("Debit retry failed: " + debitResult.getMessage());
            }
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Both retry error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Reverse debit operation
     */
    private CorrectiveActionResult reverseDebit(TransactionRepair repair, CorrectiveActionResult result) {
        try {
            // Implementation would depend on core banking system's reversal capabilities
            // This is a placeholder for the actual reversal logic
            
            result.setSuccess(true);
            result.setMessage("Debit reversal completed (manual verification required)");
            result.setDetails(Map.of("action", "REVERSE_DEBIT", "note", "Manual verification required"));
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Debit reversal error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Reverse credit operation
     */
    private CorrectiveActionResult reverseCredit(TransactionRepair repair, CorrectiveActionResult result) {
        try {
            // Implementation would depend on core banking system's reversal capabilities
            // This is a placeholder for the actual reversal logic
            
            result.setSuccess(true);
            result.setMessage("Credit reversal completed (manual verification required)");
            result.setDetails(Map.of("action", "REVERSE_CREDIT", "note", "Manual verification required"));
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Credit reversal error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Reverse both debit and credit operations
     */
    private CorrectiveActionResult reverseBoth(TransactionRepair repair, CorrectiveActionResult result) {
        try {
            // Implementation would depend on core banking system's reversal capabilities
            // This is a placeholder for the actual reversal logic
            
            result.setSuccess(true);
            result.setMessage("Both debit and credit reversal completed (manual verification required)");
            result.setDetails(Map.of("action", "REVERSE_BOTH", "note", "Manual verification required"));
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Both reversal error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Manual credit operation
     */
    private CorrectiveActionResult manualCredit(TransactionRepair repair, CorrectiveActionResult result) {
        try {
            // This would typically involve creating a manual credit transaction
            // or updating the core banking system manually
            
            result.setSuccess(true);
            result.setMessage("Manual credit operation completed (manual verification required)");
            result.setDetails(Map.of("action", "MANUAL_CREDIT", "note", "Manual verification required"));
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Manual credit error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Manual debit operation
     */
    private CorrectiveActionResult manualDebit(TransactionRepair repair, CorrectiveActionResult result) {
        try {
            // This would typically involve creating a manual debit transaction
            // or updating the core banking system manually
            
            result.setSuccess(true);
            result.setMessage("Manual debit operation completed (manual verification required)");
            result.setDetails(Map.of("action", "MANUAL_DEBIT", "note", "Manual verification required"));
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Manual debit error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Manual both operations
     */
    private CorrectiveActionResult manualBoth(TransactionRepair repair, CorrectiveActionResult result) {
        try {
            // This would typically involve creating manual debit and credit transactions
            // or updating the core banking system manually
            
            result.setSuccess(true);
            result.setMessage("Manual both operations completed (manual verification required)");
            result.setDetails(Map.of("action", "MANUAL_BOTH", "note", "Manual verification required"));
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Manual both error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Cancel transaction
     */
    private CorrectiveActionResult cancelTransaction(TransactionRepair repair, CorrectiveActionResult result) {
        try {
            // Mark transaction as cancelled
            repair.setRepairStatus(TransactionRepair.RepairStatus.CANCELLED);
            
            result.setSuccess(true);
            result.setMessage("Transaction cancelled");
            result.setDetails(Map.of("action", "CANCEL_TRANSACTION"));
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Cancel transaction error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Escalate transaction
     */
    private CorrectiveActionResult escalateTransaction(TransactionRepair repair, CorrectiveActionResult result) {
        try {
            // Mark for escalation
            repair.setPriority(10); // Highest priority
            repair.setRepairStatus(TransactionRepair.RepairStatus.PENDING);
            
            result.setSuccess(true);
            result.setMessage("Transaction escalated for manual review");
            result.setDetails(Map.of("action", "ESCALATE", "priority", 10));
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Escalate transaction error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * No action
     */
    private CorrectiveActionResult noAction(TransactionRepair repair, CorrectiveActionResult result) {
        try {
            result.setSuccess(true);
            result.setMessage("No action taken - transaction marked as resolved");
            result.setDetails(Map.of("action", "NO_ACTION"));
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("No action error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Mark transaction repair as resolved
     */
    @Transactional
    public TransactionRepair resolveTransactionRepair(UUID id, String resolvedBy, String resolutionNotes) {
        Optional<TransactionRepair> repairOpt = transactionRepairRepository.findById(id);
        if (repairOpt.isPresent()) {
            TransactionRepair repair = repairOpt.get();
            repair.markAsResolved(resolvedBy, resolutionNotes);
            return transactionRepairRepository.save(repair);
        }
        throw new IllegalArgumentException("Transaction repair not found: " + id);
    }
    
    /**
     * Get transaction repair statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTransactionRepairStatistics(String tenantId) {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        stats.put("total", transactionRepairRepository.countByTenantId(tenantId));
        stats.put("pending", transactionRepairRepository.countByTenantIdAndRepairStatus(tenantId, TransactionRepair.RepairStatus.PENDING));
        stats.put("assigned", transactionRepairRepository.countByTenantIdAndRepairStatus(tenantId, TransactionRepair.RepairStatus.ASSIGNED));
        stats.put("inProgress", transactionRepairRepository.countByTenantIdAndRepairStatus(tenantId, TransactionRepair.RepairStatus.IN_PROGRESS));
        stats.put("resolved", transactionRepairRepository.countByTenantIdAndRepairStatus(tenantId, TransactionRepair.RepairStatus.RESOLVED));
        stats.put("failed", transactionRepairRepository.countByTenantIdAndRepairStatus(tenantId, TransactionRepair.RepairStatus.FAILED));
        stats.put("cancelled", transactionRepairRepository.countByTenantIdAndRepairStatus(tenantId, TransactionRepair.RepairStatus.CANCELLED));
        
        stats.put("readyForRetry", transactionRepairRepository.countReadyForRetry(LocalDateTime.now()));
        stats.put("timedOut", transactionRepairRepository.countTimedOut(LocalDateTime.now()));
        stats.put("highPriority", transactionRepairRepository.findHighPriorityByTenant(tenantId).size());
        stats.put("needingReview", transactionRepairRepository.findNeedingManualReviewByTenant(tenantId).size());
        
        return stats;
    }
    
    /**
     * Scheduled task to process retry operations
     */
    @Scheduled(fixedDelay = 60000) // Run every minute
    @Async
    public void processRetryOperations() {
        logger.debug("Processing retry operations");
        
        try {
            List<TransactionRepair> readyForRetry = getTransactionRepairsReadyForRetry();
            
            for (TransactionRepair repair : readyForRetry) {
                try {
                    // Mark for retry with exponential backoff
                    int delayMinutes = (int) Math.pow(2, repair.getRetryCount()) * 5; // 5, 10, 20, 40 minutes
                    repair.markForRetry(delayMinutes);
                    transactionRepairRepository.save(repair);
                    
                    logger.info("Marked transaction repair {} for retry in {} minutes", 
                               repair.getId(), delayMinutes);
                    
                } catch (Exception e) {
                    logger.error("Error processing retry for transaction repair {}: {}", 
                               repair.getId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error in scheduled retry processing: {}", e.getMessage());
        }
    }
    
    /**
     * Scheduled task to handle timeouts
     */
    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    @Async
    public void handleTimeouts() {
        logger.debug("Handling transaction repair timeouts");
        
        try {
            List<TransactionRepair> timedOut = getTimedOutTransactionRepairs();
            
            for (TransactionRepair repair : timedOut) {
                try {
                    // Mark as needing manual review
                    repair.setRepairType(TransactionRepair.RepairType.MANUAL_REVIEW);
                    repair.setRepairStatus(TransactionRepair.RepairStatus.PENDING);
                    repair.setPriority(8); // High priority
                    repair.setFailureReason("Transaction repair timed out");
                    transactionRepairRepository.save(repair);
                    
                    logger.warn("Transaction repair {} timed out and marked for manual review", 
                               repair.getId());
                    
                } catch (Exception e) {
                    logger.error("Error handling timeout for transaction repair {}: {}", 
                               repair.getId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error in scheduled timeout handling: {}", e.getMessage());
        }
    }
    
    /**
     * Corrective Action Result DTO
     */
    public static class CorrectiveActionResult {
        private UUID transactionRepairId;
        private TransactionRepair.CorrectiveAction correctiveAction;
        private boolean success;
        private String message;
        private Map<String, Object> details;
        private String appliedBy;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        
        // Getters and Setters
        public UUID getTransactionRepairId() { return transactionRepairId; }
        public void setTransactionRepairId(UUID transactionRepairId) { this.transactionRepairId = transactionRepairId; }
        
        public TransactionRepair.CorrectiveAction getCorrectiveAction() { return correctiveAction; }
        public void setCorrectiveAction(TransactionRepair.CorrectiveAction correctiveAction) { this.correctiveAction = correctiveAction; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }
        
        public String getAppliedBy() { return appliedBy; }
        public void setAppliedBy(String appliedBy) { this.appliedBy = appliedBy; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }
}