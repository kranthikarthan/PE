package com.payments.swiftadapter.repository;

import com.payments.swiftadapter.domain.SwiftTransactionLog;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for SWIFT Transaction Log entities
 */
@Repository
public interface SwiftTransactionLogRepository extends JpaRepository<SwiftTransactionLog, String> {

  /**
   * Find transaction log by ID
   */
  Optional<SwiftTransactionLog> findById(String id);

  /**
   * Find transaction logs by adapter ID
   */
  List<SwiftTransactionLog> findBySwiftAdapterId(String swiftAdapterId);

  /**
   * Find transaction log by transaction ID
   */
  Optional<SwiftTransactionLog> findByTransactionId(String transactionId);

  /**
   * Find transaction log by message ID
   */
  Optional<SwiftTransactionLog> findByMessageId(String messageId);

  /**
   * Find transaction log by instruction ID
   */
  Optional<SwiftTransactionLog> findByInstructionId(String instructionId);

  /**
   * Find transaction log by end-to-end ID
   */
  Optional<SwiftTransactionLog> findByEndToEndId(String endToEndId);

  /**
   * Find transaction logs by status
   */
  List<SwiftTransactionLog> findByStatus(String status);

  /**
   * Find transaction logs by adapter ID and status
   */
  List<SwiftTransactionLog> findBySwiftAdapterIdAndStatus(String swiftAdapterId, String status);

  /**
   * Find transaction logs by transaction type
   */
  List<SwiftTransactionLog> findByTransactionType(String transactionType);

  /**
   * Find transaction logs by currency
   */
  List<SwiftTransactionLog> findByCurrency(String currency);

  /**
   * Find transaction logs by sanctions screening status
   */
  List<SwiftTransactionLog> findBySanctionsScreeningStatus(String sanctionsScreeningStatus);

  /**
   * Find transaction logs by FX conversion status
   */
  List<SwiftTransactionLog> findByFxConversionStatus(String fxConversionStatus);

  /**
   * Find transaction logs by debtor bank SWIFT code
   */
  List<SwiftTransactionLog> findByDebtorBankSwiftCode(String debtorBankSwiftCode);

  /**
   * Find transaction logs by creditor bank SWIFT code
   */
  List<SwiftTransactionLog> findByCreditorBankSwiftCode(String creditorBankSwiftCode);

  /**
   * Find transaction logs by correspondent bank SWIFT code
   */
  List<SwiftTransactionLog> findByCorrespondentBankSwiftCode(String correspondentBankSwiftCode);

  /**
   * Find transaction logs by intermediary bank SWIFT code
   */
  List<SwiftTransactionLog> findByIntermediaryBankSwiftCode(String intermediaryBankSwiftCode);

  /**
   * Find transaction logs by date range
   */
  @Query(
      "SELECT stl FROM SwiftTransactionLog stl WHERE stl.createdAt >= :startDate AND stl.createdAt <= :endDate")
  List<SwiftTransactionLog> findByDateRange(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  /**
   * Find transaction logs by adapter ID and date range
   */
  @Query(
      "SELECT stl FROM SwiftTransactionLog stl WHERE stl.swiftAdapterId = :swiftAdapterId AND stl.createdAt >= :startDate AND stl.createdAt <= :endDate")
  List<SwiftTransactionLog> findByAdapterIdAndDateRange(
      @Param("swiftAdapterId") String swiftAdapterId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  /**
   * Find transaction logs by amount range
   */
  @Query(
      "SELECT stl FROM SwiftTransactionLog stl WHERE stl.amount >= :minAmount AND stl.amount <= :maxAmount")
  List<SwiftTransactionLog> findByAmountRange(
      @Param("minAmount") java.math.BigDecimal minAmount,
      @Param("maxAmount") java.math.BigDecimal maxAmount);

  /**
   * Find transaction logs by adapter ID and amount range
   */
  @Query(
      "SELECT stl FROM SwiftTransactionLog stl WHERE stl.swiftAdapterId = :swiftAdapterId AND stl.amount >= :minAmount AND stl.amount <= :maxAmount")
  List<SwiftTransactionLog> findByAdapterIdAndAmountRange(
      @Param("swiftAdapterId") String swiftAdapterId,
      @Param("minAmount") java.math.BigDecimal minAmount,
      @Param("maxAmount") java.math.BigDecimal maxAmount);

  /**
   * Find transaction logs by processed date range
   */
  @Query(
      "SELECT stl FROM SwiftTransactionLog stl WHERE stl.processedAt >= :startDate AND stl.processedAt <= :endDate")
  List<SwiftTransactionLog> findByProcessedDateRange(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  /**
   * Count transaction logs by status
   */
  long countByStatus(String status);

  /**
   * Count transaction logs by adapter ID and status
   */
  long countBySwiftAdapterIdAndStatus(String swiftAdapterId, String status);

  /**
   * Count transaction logs by sanctions screening status
   */
  long countBySanctionsScreeningStatus(String sanctionsScreeningStatus);

  /**
   * Count transaction logs by FX conversion status
   */
  long countByFxConversionStatus(String fxConversionStatus);
}
