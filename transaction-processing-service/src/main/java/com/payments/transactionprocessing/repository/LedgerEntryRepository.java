package com.payments.transactionprocessing.repository;

import com.payments.domain.shared.AccountNumber;
import com.payments.domain.shared.TenantContext;
import com.payments.domain.transaction.LedgerEntryId;
import com.payments.domain.transaction.LedgerEntryType;
import com.payments.domain.transaction.TransactionId;
import com.payments.transactionprocessing.entity.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, LedgerEntryId> {

    List<LedgerEntryEntity> findByTenantContextAndAccountNumber(
        TenantContext tenantContext, 
        AccountNumber accountNumber
    );

    List<LedgerEntryEntity> findByTenantContextAndAccountNumberAndEntryType(
        TenantContext tenantContext, 
        AccountNumber accountNumber,
        LedgerEntryType entryType
    );

    @Query("SELECT le FROM LedgerEntryEntity le WHERE le.tenantContext = :tenantContext " +
           "AND le.accountNumber = :accountNumber " +
           "AND le.entryDate BETWEEN :startDate AND :endDate " +
           "ORDER BY le.entryDate DESC, le.createdAt DESC")
    List<LedgerEntryEntity> findByTenantContextAndAccountNumberAndDateRange(
        @Param("tenantContext") TenantContext tenantContext,
        @Param("accountNumber") AccountNumber accountNumber,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(le.amount) FROM LedgerEntryEntity le WHERE le.tenantContext = :tenantContext " +
           "AND le.accountNumber = :accountNumber " +
           "AND le.entryType = :entryType")
    BigDecimal sumAmountByTenantContextAndAccountNumberAndEntryType(
        @Param("tenantContext") TenantContext tenantContext,
        @Param("accountNumber") AccountNumber accountNumber,
        @Param("entryType") LedgerEntryType entryType
    );

    @Query("SELECT le FROM LedgerEntryEntity le WHERE le.transaction.id = :transactionId " +
           "ORDER BY le.entryType, le.createdAt")
    List<LedgerEntryEntity> findByTransactionId(@Param("transactionId") TransactionId transactionId);

    @Query("SELECT le FROM LedgerEntryEntity le WHERE le.tenantContext = :tenantContext " +
           "AND le.accountNumber = :accountNumber " +
           "ORDER BY le.entryDate DESC, le.createdAt DESC")
    List<LedgerEntryEntity> findLatestByTenantContextAndAccountNumber(
        @Param("tenantContext") TenantContext tenantContext,
        @Param("accountNumber") AccountNumber accountNumber
    );

    @Query("SELECT DISTINCT le.accountNumber FROM LedgerEntryEntity le WHERE le.tenantContext = :tenantContext")
    List<String> findDistinctAccountNumbersByTenantContext(@Param("tenantContext") TenantContext tenantContext);

    List<LedgerEntryEntity> findByTenantContext(TenantContext tenantContext);

    @Query("SELECT COUNT(le) FROM LedgerEntryEntity le WHERE le.tenantContext = :tenantContext " +
           "AND le.accountNumber = :accountNumber")
    long countByTenantContextAndAccountNumber(
        @Param("tenantContext") TenantContext tenantContext,
        @Param("accountNumber") AccountNumber accountNumber
    );
}
