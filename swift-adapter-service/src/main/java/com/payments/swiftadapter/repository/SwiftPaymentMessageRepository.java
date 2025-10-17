package com.payments.swiftadapter.repository;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.swiftadapter.domain.SwiftPaymentMessage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for SWIFT Payment Message entities
 */
@Repository
public interface SwiftPaymentMessageRepository extends JpaRepository<SwiftPaymentMessage, String> {

  /**
   * Find payment message by ID
   */
  Optional<SwiftPaymentMessage> findById(String id);

  /**
   * Find payment messages by adapter ID
   */
  List<SwiftPaymentMessage> findBySwiftAdapterId(String swiftAdapterId);

  /**
   * Find payment message by transaction ID
   */
  Optional<SwiftPaymentMessage> findByTransactionId(String transactionId);

  /**
   * Find payment message by message ID
   */
  Optional<SwiftPaymentMessage> findByMessageId(String messageId);

  /**
   * Find payment message by instruction ID
   */
  Optional<SwiftPaymentMessage> findByInstructionId(String instructionId);

  /**
   * Find payment message by end-to-end ID
   */
  Optional<SwiftPaymentMessage> findByEndToEndId(String endToEndId);

  /**
   * Find payment messages by status
   */
  List<SwiftPaymentMessage> findByStatus(String status);

  /**
   * Find payment messages by adapter ID and status
   */
  List<SwiftPaymentMessage> findBySwiftAdapterIdAndStatus(String swiftAdapterId, String status);

  /**
   * Find payment messages by transaction type
   */
  List<SwiftPaymentMessage> findByTransactionType(String transactionType);

  /**
   * Find payment messages by direction
   */
  List<SwiftPaymentMessage> findByDirection(String direction);

  /**
   * Find payment messages by message type
   */
  List<SwiftPaymentMessage> findByMessageType(String messageType);

  /**
   * Find payment messages by currency
   */
  List<SwiftPaymentMessage> findByCurrency(String currency);

  /**
   * Find payment messages by sanctions screening status
   */
  List<SwiftPaymentMessage> findBySanctionsScreeningStatus(String sanctionsScreeningStatus);

  /**
   * Find payment messages by FX conversion status
   */
  List<SwiftPaymentMessage> findByFxConversionStatus(String fxConversionStatus);

  /**
   * Find payment messages by debtor bank SWIFT code
   */
  List<SwiftPaymentMessage> findByDebtorBankSwiftCode(String debtorBankSwiftCode);

  /**
   * Find payment messages by creditor bank SWIFT code
   */
  List<SwiftPaymentMessage> findByCreditorBankSwiftCode(String creditorBankSwiftCode);

  /**
   * Find payment messages by correspondent bank SWIFT code
   */
  List<SwiftPaymentMessage> findByCorrespondentBankSwiftCode(String correspondentBankSwiftCode);

  /**
   * Find payment messages by intermediary bank SWIFT code
   */
  List<SwiftPaymentMessage> findByIntermediaryBankSwiftCode(String intermediaryBankSwiftCode);

  /**
   * Find payment messages by date range
   */
  @Query(
      "SELECT spm FROM SwiftPaymentMessage spm WHERE spm.createdAt >= :startDate AND spm.createdAt <= :endDate")
  List<SwiftPaymentMessage> findByDateRange(
      @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  /**
   * Find payment messages by adapter ID and date range
   */
  @Query(
      "SELECT spm FROM SwiftPaymentMessage spm WHERE spm.swiftAdapterId = :swiftAdapterId AND spm.createdAt >= :startDate AND spm.createdAt <= :endDate")
  List<SwiftPaymentMessage> findByAdapterIdAndDateRange(
      @Param("swiftAdapterId") String swiftAdapterId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  /**
   * Find payment messages by amount range
   */
  @Query(
      "SELECT spm FROM SwiftPaymentMessage spm WHERE spm.amount >= :minAmount AND spm.amount <= :maxAmount")
  List<SwiftPaymentMessage> findByAmountRange(
      @Param("minAmount") java.math.BigDecimal minAmount,
      @Param("maxAmount") java.math.BigDecimal maxAmount);

  /**
   * Find payment messages by adapter ID and amount range
   */
  @Query(
      "SELECT spm FROM SwiftPaymentMessage spm WHERE spm.swiftAdapterId = :swiftAdapterId AND spm.amount >= :minAmount AND spm.amount <= :maxAmount")
  List<SwiftPaymentMessage> findByAdapterIdAndAmountRange(
      @Param("swiftAdapterId") String swiftAdapterId,
      @Param("minAmount") java.math.BigDecimal minAmount,
      @Param("maxAmount") java.math.BigDecimal maxAmount);

  /**
   * Count payment messages by status
   */
  long countByStatus(String status);

  /**
   * Count payment messages by adapter ID and status
   */
  long countBySwiftAdapterIdAndStatus(String swiftAdapterId, String status);

  /**
   * Count payment messages by sanctions screening status
   */
  long countBySanctionsScreeningStatus(String sanctionsScreeningStatus);

  /**
   * Count payment messages by FX conversion status
   */
  long countByFxConversionStatus(String fxConversionStatus);
}
