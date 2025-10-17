package com.payments.swiftadapter.repository;

import com.payments.swiftadapter.domain.SwiftSettlementRecord;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for SWIFT Settlement Record entities */
@Repository
public interface SwiftSettlementRecordRepository
    extends JpaRepository<SwiftSettlementRecord, String> {

  /** Find settlement record by ID */
  Optional<SwiftSettlementRecord> findById(String id);

  /** Find settlement records by adapter ID */
  List<SwiftSettlementRecord> findBySwiftAdapterId(String swiftAdapterId);

  /** Find settlement record by transaction ID */
  Optional<SwiftSettlementRecord> findByTransactionId(String transactionId);

  /** Find settlement record by message ID */
  Optional<SwiftSettlementRecord> findByMessageId(String messageId);

  /** Find settlement record by instruction ID */
  Optional<SwiftSettlementRecord> findByInstructionId(String instructionId);

  /** Find settlement record by end-to-end ID */
  Optional<SwiftSettlementRecord> findByEndToEndId(String endToEndId);

  /** Find settlement records by status */
  List<SwiftSettlementRecord> findByStatus(String status);

  /** Find settlement records by adapter ID and status */
  List<SwiftSettlementRecord> findBySwiftAdapterIdAndStatus(String swiftAdapterId, String status);

  /** Find settlement records by settlement type */
  List<SwiftSettlementRecord> findBySettlementType(String settlementType);

  /** Find settlement records by currency */
  List<SwiftSettlementRecord> findByCurrency(String currency);

  /** Find settlement records by debtor bank SWIFT code */
  List<SwiftSettlementRecord> findByDebtorBankSwiftCode(String debtorBankSwiftCode);

  /** Find settlement records by creditor bank SWIFT code */
  List<SwiftSettlementRecord> findByCreditorBankSwiftCode(String creditorBankSwiftCode);

  /** Find settlement records by correspondent bank SWIFT code */
  List<SwiftSettlementRecord> findByCorrespondentBankSwiftCode(String correspondentBankSwiftCode);

  /** Find settlement records by intermediary bank SWIFT code */
  List<SwiftSettlementRecord> findByIntermediaryBankSwiftCode(String intermediaryBankSwiftCode);

  /** Find settlement records by settlement bank SWIFT code */
  List<SwiftSettlementRecord> findBySettlementBankSwiftCode(String settlementBankSwiftCode);

  /** Find settlement records by nostro account */
  List<SwiftSettlementRecord> findByNostroAccount(String nostroAccount);

  /** Find settlement records by vostro account */
  List<SwiftSettlementRecord> findByVostroAccount(String vostroAccount);

  /** Find settlement records by date range */
  @Query(
      "SELECT ssr FROM SwiftSettlementRecord ssr WHERE ssr.createdAt >= :startDate AND ssr.createdAt <= :endDate")
  List<SwiftSettlementRecord> findByDateRange(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  /** Find settlement records by adapter ID and date range */
  @Query(
      "SELECT ssr FROM SwiftSettlementRecord ssr WHERE ssr.swiftAdapterId = :swiftAdapterId AND ssr.createdAt >= :startDate AND ssr.createdAt <= :endDate")
  List<SwiftSettlementRecord> findByAdapterIdAndDateRange(
      @Param("swiftAdapterId") String swiftAdapterId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  /** Find settlement records by settlement date range */
  @Query(
      "SELECT ssr FROM SwiftSettlementRecord ssr WHERE ssr.settlementDate >= :startDate AND ssr.settlementDate <= :endDate")
  List<SwiftSettlementRecord> findBySettlementDateRange(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  /** Find settlement records by value date range */
  @Query(
      "SELECT ssr FROM SwiftSettlementRecord ssr WHERE ssr.valueDate >= :startDate AND ssr.valueDate <= :endDate")
  List<SwiftSettlementRecord> findByValueDateRange(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  /** Find settlement records by amount range */
  @Query(
      "SELECT ssr FROM SwiftSettlementRecord ssr WHERE ssr.amount >= :minAmount AND ssr.amount <= :maxAmount")
  List<SwiftSettlementRecord> findByAmountRange(
      @Param("minAmount") java.math.BigDecimal minAmount,
      @Param("maxAmount") java.math.BigDecimal maxAmount);

  /** Find settlement records by adapter ID and amount range */
  @Query(
      "SELECT ssr FROM SwiftSettlementRecord ssr WHERE ssr.swiftAdapterId = :swiftAdapterId AND ssr.amount >= :minAmount AND ssr.amount <= :maxAmount")
  List<SwiftSettlementRecord> findByAdapterIdAndAmountRange(
      @Param("swiftAdapterId") String swiftAdapterId,
      @Param("minAmount") java.math.BigDecimal minAmount,
      @Param("maxAmount") java.math.BigDecimal maxAmount);

  /** Find settlement records by net amount range */
  @Query(
      "SELECT ssr FROM SwiftSettlementRecord ssr WHERE ssr.netAmount >= :minAmount AND ssr.netAmount <= :maxAmount")
  List<SwiftSettlementRecord> findByNetAmountRange(
      @Param("minAmount") java.math.BigDecimal minAmount,
      @Param("maxAmount") java.math.BigDecimal maxAmount);

  /** Count settlement records by status */
  long countByStatus(String status);

  /** Count settlement records by adapter ID and status */
  long countBySwiftAdapterIdAndStatus(String swiftAdapterId, String status);

  /** Count settlement records by settlement type */
  long countBySettlementType(String settlementType);
}
