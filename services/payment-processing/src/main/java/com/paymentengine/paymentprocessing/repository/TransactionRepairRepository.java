package com.paymentengine.paymentprocessing.repository;

import com.paymentengine.paymentprocessing.entity.TransactionRepair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Transaction Repair entities
 */
@Repository
public interface TransactionRepairRepository extends JpaRepository<TransactionRepair, UUID> {
    
    /**
     * Find transaction repairs by transaction reference
     */
    List<TransactionRepair> findByTransactionReference(String transactionReference);
    
    /**
     * Find transaction repair by transaction reference and tenant ID
     */
    Optional<TransactionRepair> findByTransactionReferenceAndTenantId(String transactionReference, String tenantId);
    
    /**
     * Find transaction repairs by tenant ID
     */
    List<TransactionRepair> findByTenantId(String tenantId);
    
    /**
     * Find transaction repairs by repair type
     */
    List<TransactionRepair> findByRepairType(TransactionRepair.RepairType repairType);
    
    /**
     * Find transaction repairs by repair status
     */
    List<TransactionRepair> findByRepairStatus(TransactionRepair.RepairStatus repairStatus);
    
    /**
     * Find transaction repairs by tenant ID and repair status
     */
    List<TransactionRepair> findByTenantIdAndRepairStatus(String tenantId, TransactionRepair.RepairStatus repairStatus);
    
    /**
     * Find transaction repairs by assigned user
     */
    List<TransactionRepair> findByAssignedTo(String assignedTo);
    
    /**
     * Find transaction repairs by assigned user and repair status
     */
    List<TransactionRepair> findByAssignedToAndRepairStatus(String assignedTo, TransactionRepair.RepairStatus repairStatus);
    
    /**
     * Find transaction repairs that are ready for retry
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE tr.repairStatus = 'PENDING' AND tr.retryCount < tr.maxRetries AND (tr.nextRetryAt IS NULL OR tr.nextRetryAt <= :currentTime)")
    List<TransactionRepair> findReadyForRetry(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find transaction repairs that have timed out
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE tr.timeoutAt IS NOT NULL AND tr.timeoutAt <= :currentTime AND tr.repairStatus IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS')")
    List<TransactionRepair> findTimedOut(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find transaction repairs by priority range
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE tr.priority BETWEEN :minPriority AND :maxPriority ORDER BY tr.priority ASC, tr.createdAt ASC")
    List<TransactionRepair> findByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);
    
    /**
     * Find transaction repairs by corrective action
     */
    List<TransactionRepair> findByCorrectiveAction(TransactionRepair.CorrectiveAction correctiveAction);
    
    /**
     * Find transaction repairs by debit status
     */
    List<TransactionRepair> findByDebitStatus(TransactionRepair.DebitCreditStatus debitStatus);
    
    /**
     * Find transaction repairs by credit status
     */
    List<TransactionRepair> findByCreditStatus(TransactionRepair.DebitCreditStatus creditStatus);
    
    /**
     * Find transaction repairs by debit and credit status
     */
    List<TransactionRepair> findByDebitStatusAndCreditStatus(
        TransactionRepair.DebitCreditStatus debitStatus, 
        TransactionRepair.DebitCreditStatus creditStatus
    );
    
    /**
     * Find transaction repairs created within date range
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE tr.createdAt BETWEEN :startDate AND :endDate ORDER BY tr.createdAt DESC")
    List<TransactionRepair> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find transaction repairs resolved within date range
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE tr.resolvedAt BETWEEN :startDate AND :endDate ORDER BY tr.resolvedAt DESC")
    List<TransactionRepair> findByResolvedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find transaction repairs by amount range
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE tr.amount BETWEEN :minAmount AND :maxAmount ORDER BY tr.amount DESC")
    List<TransactionRepair> findByAmountBetween(@Param("minAmount") java.math.BigDecimal minAmount, @Param("maxAmount") java.math.BigDecimal maxAmount);
    
    /**
     * Find transaction repairs by currency
     */
    List<TransactionRepair> findByCurrency(String currency);
    
    /**
     * Find transaction repairs by payment type
     */
    List<TransactionRepair> findByPaymentType(String paymentType);
    
    /**
     * Find transaction repairs by account number (from or to)
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE tr.fromAccountNumber = :accountNumber OR tr.toAccountNumber = :accountNumber")
    List<TransactionRepair> findByAccountNumber(@Param("accountNumber") String accountNumber);
    
    /**
     * Find transaction repairs by parent transaction ID
     */
    List<TransactionRepair> findByParentTransactionId(String parentTransactionId);
    
    /**
     * Find transaction repairs created by specific user
     */
    List<TransactionRepair> findByCreatedBy(String createdBy);
    
    /**
     * Find transaction repairs resolved by specific user
     */
    List<TransactionRepair> findByResolvedBy(String resolvedBy);
    
    /**
     * Find transaction repairs updated by specific user
     */
    List<TransactionRepair> findByUpdatedBy(String updatedBy);
    
    /**
     * Count transaction repairs by repair status
     */
    long countByRepairStatus(TransactionRepair.RepairStatus repairStatus);
    
    /**
     * Count transaction repairs by tenant ID and repair status
     */
    long countByTenantIdAndRepairStatus(String tenantId, TransactionRepair.RepairStatus repairStatus);
    
    /**
     * Count transaction repairs by repair type
     */
    long countByRepairType(TransactionRepair.RepairType repairType);
    
    /**
     * Count transaction repairs by assigned user
     */
    long countByAssignedTo(String assignedTo);
    
    /**
     * Count transaction repairs by assigned user and repair status
     */
    long countByAssignedToAndRepairStatus(String assignedTo, TransactionRepair.RepairStatus repairStatus);
    
    /**
     * Count transaction repairs that are ready for retry
     */
    @Query("SELECT COUNT(tr) FROM TransactionRepair tr WHERE tr.repairStatus = 'PENDING' AND tr.retryCount < tr.maxRetries AND (tr.nextRetryAt IS NULL OR tr.nextRetryAt <= :currentTime)")
    long countReadyForRetry(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Count transaction repairs that have timed out
     */
    @Query("SELECT COUNT(tr) FROM TransactionRepair tr WHERE tr.timeoutAt IS NOT NULL AND tr.timeoutAt <= :currentTime AND tr.repairStatus IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS')")
    long countTimedOut(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find transaction repairs with high priority
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE tr.priority >= 8 ORDER BY tr.priority DESC, tr.createdAt ASC")
    List<TransactionRepair> findHighPriority();
    
    /**
     * Find transaction repairs with high priority by tenant
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE tr.tenantId = :tenantId AND tr.priority >= 8 ORDER BY tr.priority DESC, tr.createdAt ASC")
    List<TransactionRepair> findHighPriorityByTenant(@Param("tenantId") String tenantId);
    
    /**
     * Find transaction repairs that need manual review
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE tr.repairType = 'MANUAL_REVIEW' OR tr.correctiveAction = 'ESCALATE' ORDER BY tr.priority DESC, tr.createdAt ASC")
    List<TransactionRepair> findNeedingManualReview();
    
    /**
     * Find transaction repairs that need manual review by tenant
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE tr.tenantId = :tenantId AND (tr.repairType = 'MANUAL_REVIEW' OR tr.correctiveAction = 'ESCALATE') ORDER BY tr.priority DESC, tr.createdAt ASC")
    List<TransactionRepair> findNeedingManualReviewByTenant(@Param("tenantId") String tenantId);
    
    /**
     * Find transaction repairs by error code
     */
    List<TransactionRepair> findByErrorCode(String errorCode);
    
    /**
     * Find transaction repairs by error code and tenant
     */
    List<TransactionRepair> findByErrorCodeAndTenantId(String errorCode, String tenantId);
    
    /**
     * Find transaction repairs with specific retry count
     */
    List<TransactionRepair> findByRetryCount(Integer retryCount);
    
    /**
     * Find transaction repairs with maximum retry count reached
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE tr.retryCount >= tr.maxRetries AND tr.repairStatus = 'PENDING'")
    List<TransactionRepair> findMaxRetriesReached();
    
    /**
     * Find transaction repairs with maximum retry count reached by tenant
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE tr.tenantId = :tenantId AND tr.retryCount >= tr.maxRetries AND tr.repairStatus = 'PENDING'")
    List<TransactionRepair> findMaxRetriesReachedByTenant(@Param("tenantId") String tenantId);
    
    /**
     * Find transaction repairs by resolution notes containing text
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE LOWER(tr.resolutionNotes) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<TransactionRepair> findByResolutionNotesContaining(@Param("searchText") String searchText);
    
    /**
     * Find transaction repairs by failure reason containing text
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE LOWER(tr.failureReason) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<TransactionRepair> findByFailureReasonContaining(@Param("searchText") String searchText);
    
    /**
     * Find transaction repairs by error message containing text
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE LOWER(tr.errorMessage) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<TransactionRepair> findByErrorMessageContaining(@Param("searchText") String searchText);
    
    /**
     * Find transaction repairs that are overdue (past next retry time)
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE tr.nextRetryAt IS NOT NULL AND tr.nextRetryAt < :currentTime AND tr.repairStatus = 'PENDING'")
    List<TransactionRepair> findOverdue(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find transaction repairs that are overdue by tenant
     */
    @Query("SELECT tr FROM TransactionRepair tr WHERE tr.tenantId = :tenantId AND tr.nextRetryAt IS NOT NULL AND tr.nextRetryAt < :currentTime AND tr.repairStatus = 'PENDING'")
    List<TransactionRepair> findOverdueByTenant(@Param("tenantId") String tenantId, @Param("currentTime") LocalDateTime currentTime);
}