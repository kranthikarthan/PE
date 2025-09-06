package com.paymentengine.corebanking.repository;

import com.paymentengine.corebanking.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Account entity operations
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    
    /**
     * Find account by account number
     */
    Optional<Account> findByAccountNumber(String accountNumber);
    
    /**
     * Find account by account number with pessimistic lock for concurrent updates
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);
    
    /**
     * Find account by ID with pessimistic lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") UUID id);
    
    /**
     * Find all accounts for a customer
     */
    List<Account> findByCustomerId(UUID customerId);
    
    /**
     * Find active accounts for a customer
     */
    @Query("SELECT a FROM Account a WHERE a.customerId = :customerId AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByCustomerId(@Param("customerId") UUID customerId);
    
    /**
     * Find accounts by account type
     */
    List<Account> findByAccountTypeId(UUID accountTypeId);
    
    /**
     * Find accounts with balance greater than specified amount
     */
    @Query("SELECT a FROM Account a WHERE a.balance > :minBalance")
    List<Account> findAccountsWithBalanceGreaterThan(@Param("minBalance") BigDecimal minBalance);
    
    /**
     * Find accounts with low balance (below specified threshold)
     */
    @Query("SELECT a FROM Account a WHERE a.availableBalance < :threshold AND a.status = 'ACTIVE'")
    List<Account> findAccountsWithLowBalance(@Param("threshold") BigDecimal threshold);
    
    /**
     * Find accounts by status with pagination
     */
    Page<Account> findByStatus(Account.AccountStatus status, Pageable pageable);
    
    /**
     * Count accounts by status
     */
    long countByStatus(Account.AccountStatus status);
    
    /**
     * Count accounts by customer
     */
    long countByCustomerId(UUID customerId);
    
    /**
     * Update account balance atomically
     */
    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :amount, " +
           "a.availableBalance = a.availableBalance + :amount, " +
           "a.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE a.id = :accountId")
    int updateAccountBalance(@Param("accountId") UUID accountId, @Param("amount") BigDecimal amount);
    
    /**
     * Update available balance (for holds/releases)
     */
    @Modifying
    @Query("UPDATE Account a SET a.availableBalance = a.availableBalance + :amount, " +
           "a.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE a.id = :accountId")
    int updateAvailableBalance(@Param("accountId") UUID accountId, @Param("amount") BigDecimal amount);
    
    /**
     * Check if account exists and is active
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM Account a WHERE a.accountNumber = :accountNumber AND a.status = 'ACTIVE'")
    boolean isAccountActiveByNumber(@Param("accountNumber") String accountNumber);
    
    /**
     * Get total balance across all accounts for a customer
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a " +
           "WHERE a.customerId = :customerId AND a.status = 'ACTIVE'")
    BigDecimal getTotalBalanceByCustomerId(@Param("customerId") UUID customerId);
    
    /**
     * Get account summary with customer information
     */
    @Query("SELECT a FROM Account a " +
           "WHERE a.customerId = :customerId " +
           "ORDER BY a.createdAt DESC")
    List<Account> findAccountSummaryByCustomerId(@Param("customerId") UUID customerId);
    
    /**
     * Find accounts that need balance reconciliation
     */
    @Query("SELECT a FROM Account a " +
           "WHERE a.balance != a.availableBalance + " +
           "(SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           " WHERE (t.fromAccountId = a.id OR t.toAccountId = a.id) " +
           " AND t.status = 'PROCESSING')")
    List<Account> findAccountsNeedingReconciliation();
    
    /**
     * Search accounts by multiple criteria
     */
    @Query("SELECT a FROM Account a WHERE " +
           "(:accountNumber IS NULL OR a.accountNumber LIKE %:accountNumber%) AND " +
           "(:customerId IS NULL OR a.customerId = :customerId) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:minBalance IS NULL OR a.balance >= :minBalance) AND " +
           "(:maxBalance IS NULL OR a.balance <= :maxBalance)")
    Page<Account> findAccountsByCriteria(
        @Param("accountNumber") String accountNumber,
        @Param("customerId") UUID customerId,
        @Param("status") Account.AccountStatus status,
        @Param("minBalance") BigDecimal minBalance,
        @Param("maxBalance") BigDecimal maxBalance,
        Pageable pageable
    );
    
    /**
     * Check account number exists (for uniqueness validation)
     */
    boolean existsByAccountNumber(String accountNumber);
    
    /**
     * Find accounts created within date range
     */
    @Query("SELECT a FROM Account a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    List<Account> findAccountsCreatedBetween(
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("endDate") java.time.LocalDateTime endDate
    );
}