package com.payments.samosadapter.repository;

import com.payments.samosadapter.domain.SamosSettlementAccount;
import com.payments.samosadapter.domain.SamosSettlementAccount.AccountStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for SAMOS Settlement Accounts */
@Repository
public interface SamosSettlementAccountRepository
    extends JpaRepository<SamosSettlementAccount, String> {

  /**
   * Find settlement account by account number for a tenant
   *
   * @param tenantId Tenant ID
   * @param accountNumber SARB account number
   * @return Settlement account
   */
  @Query(
      "SELECT a FROM SamosSettlementAccount a WHERE a.tenantContext.tenantId = :tenantId AND a.accountNumber = :accountNumber")
  Optional<SamosSettlementAccount> findByTenantIdAndAccountNumber(
      @Param("tenantId") String tenantId, @Param("accountNumber") String accountNumber);

  /**
   * Find settlement account by bank code for a tenant
   *
   * @param tenantId Tenant ID
   * @param bankCode SARB member bank code
   * @return Settlement account
   */
  @Query(
      "SELECT a FROM SamosSettlementAccount a WHERE a.tenantContext.tenantId = :tenantId AND a.bankCode = :bankCode")
  Optional<SamosSettlementAccount> findByTenantIdAndBankCode(
      @Param("tenantId") String tenantId, @Param("bankCode") String bankCode);

  /**
   * Find all settlement accounts for a tenant
   *
   * @param tenantId Tenant ID
   * @return List of settlement accounts
   */
  @Query("SELECT a FROM SamosSettlementAccount a WHERE a.tenantContext.tenantId = :tenantId")
  List<SamosSettlementAccount> findAllByTenantId(@Param("tenantId") String tenantId);

  /**
   * Find active settlement accounts for a tenant
   *
   * @param tenantId Tenant ID
   * @return List of active settlement accounts
   */
  @Query(
      "SELECT a FROM SamosSettlementAccount a WHERE a.tenantContext.tenantId = :tenantId AND a.status = 'ACTIVE'")
  List<SamosSettlementAccount> findActiveAccountsByTenantId(@Param("tenantId") String tenantId);

  /**
   * Find settlement accounts by status for a tenant
   *
   * @param tenantId Tenant ID
   * @param status Account status
   * @return List of settlement accounts
   */
  @Query(
      "SELECT a FROM SamosSettlementAccount a WHERE a.tenantContext.tenantId = :tenantId AND a.status = :status")
  List<SamosSettlementAccount> findByTenantIdAndStatus(
      @Param("tenantId") String tenantId, @Param("status") AccountStatus status);

  /**
   * Find settlement accounts with insufficient collateral
   *
   * @param tenantId Tenant ID
   * @return List of accounts with insufficient collateral
   */
  @Query(
      "SELECT a FROM SamosSettlementAccount a WHERE a.tenantContext.tenantId = :tenantId "
          + "AND a.status = 'ACTIVE' AND a.collateralPledged < a.collateralRequirement")
  List<SamosSettlementAccount> findAccountsWithInsufficientCollateral(
      @Param("tenantId") String tenantId);

  /**
   * Find settlement accounts with low balance (below threshold)
   *
   * @param tenantId Tenant ID
   * @param threshold Balance threshold
   * @return List of accounts with low balance
   */
  @Query(
      "SELECT a FROM SamosSettlementAccount a WHERE a.tenantContext.tenantId = :tenantId "
          + "AND a.status = 'ACTIVE' AND (a.currentBalance - a.reservedBalance) < :threshold")
  List<SamosSettlementAccount> findAccountsWithLowBalance(
      @Param("tenantId") String tenantId, @Param("threshold") BigDecimal threshold);

  /**
   * Get total balance across all active accounts for a tenant
   *
   * @param tenantId Tenant ID
   * @return Total balance
   */
  @Query(
      "SELECT COALESCE(SUM(a.currentBalance), 0) FROM SamosSettlementAccount a "
          + "WHERE a.tenantContext.tenantId = :tenantId AND a.status = 'ACTIVE'")
  BigDecimal getTotalBalanceByTenantId(@Param("tenantId") String tenantId);

  /**
   * Get total available balance across all active accounts for a tenant
   *
   * @param tenantId Tenant ID
   * @return Total available balance
   */
  @Query(
      "SELECT COALESCE(SUM(a.currentBalance - a.reservedBalance), 0) FROM SamosSettlementAccount a "
          + "WHERE a.tenantContext.tenantId = :tenantId AND a.status = 'ACTIVE'")
  BigDecimal getTotalAvailableBalanceByTenantId(@Param("tenantId") String tenantId);

  /**
   * Get total reserved balance across all active accounts for a tenant
   *
   * @param tenantId Tenant ID
   * @return Total reserved balance
   */
  @Query(
      "SELECT COALESCE(SUM(a.reservedBalance), 0) FROM SamosSettlementAccount a "
          + "WHERE a.tenantContext.tenantId = :tenantId AND a.status = 'ACTIVE'")
  BigDecimal getTotalReservedBalanceByTenantId(@Param("tenantId") String tenantId);

  /**
   * Count active settlement accounts for a tenant
   *
   * @param tenantId Tenant ID
   * @return Number of active accounts
   */
  @Query(
      "SELECT COUNT(a) FROM SamosSettlementAccount a WHERE a.tenantContext.tenantId = :tenantId AND a.status = 'ACTIVE'")
  long countActiveAccountsByTenantId(@Param("tenantId") String tenantId);

  /**
   * Check if account number exists for a tenant
   *
   * @param tenantId Tenant ID
   * @param accountNumber SARB account number
   * @return true if exists
   */
  @Query(
      "SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM SamosSettlementAccount a "
          + "WHERE a.tenantContext.tenantId = :tenantId AND a.accountNumber = :accountNumber")
  boolean existsByTenantIdAndAccountNumber(
      @Param("tenantId") String tenantId, @Param("accountNumber") String accountNumber);

  /**
   * Check if bank code exists for a tenant
   *
   * @param tenantId Tenant ID
   * @param bankCode SARB member bank code
   * @return true if exists
   */
  @Query(
      "SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM SamosSettlementAccount a "
          + "WHERE a.tenantContext.tenantId = :tenantId AND a.bankCode = :bankCode")
  boolean existsByTenantIdAndBankCode(
      @Param("tenantId") String tenantId, @Param("bankCode") String bankCode);
}
