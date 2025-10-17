package com.payments.bankservafricaadapter.repository;

import com.payments.bankservafricaadapter.domain.BankservAfricaAchTransaction;
import com.payments.domain.shared.ClearingMessageId;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for BankservAfrica ACH Transaction entities */
@Repository
public interface BankservAfricaAchTransactionRepository
    extends JpaRepository<BankservAfricaAchTransaction, ClearingMessageId> {

  /** Find transaction by transaction ID */
  BankservAfricaAchTransaction findByTransactionId(String transactionId);

  /** Find transactions by batch ID */
  List<BankservAfricaAchTransaction> findByAchBatchId(String achBatchId);

  /** Find transactions by adapter ID */
  @Query(
      "SELECT t FROM BankservAfricaAchTransaction t WHERE t.bankservafricaAdapterId = :adapterId")
  List<BankservAfricaAchTransaction> findByAdapterId(@Param("adapterId") String adapterId);

  /** Find transactions by status */
  List<BankservAfricaAchTransaction> findByStatus(String status);

  /** Find transactions by transaction type */
  List<BankservAfricaAchTransaction> findByTransactionType(String transactionType);

  /** Find transactions by originator ID */
  List<BankservAfricaAchTransaction> findByOriginatorId(String originatorId);

  /** Find transactions by receiver ID */
  List<BankservAfricaAchTransaction> findByReceiverId(String receiverId);

  /** Find transactions by account number */
  List<BankservAfricaAchTransaction> findByAccountNumber(String accountNumber);

  /** Find transactions by routing number */
  List<BankservAfricaAchTransaction> findByRoutingNumber(String routingNumber);

  /** Find transactions by trace number */
  List<BankservAfricaAchTransaction> findByTraceNumber(String traceNumber);

  /** Find transactions by settlement date */
  List<BankservAfricaAchTransaction> findBySettlementDate(LocalDate settlementDate);

  /** Find transactions by amount range */
  List<BankservAfricaAchTransaction> findByAmountBetween(
      BigDecimal minAmount, BigDecimal maxAmount);

  /** Find transactions by currency code */
  List<BankservAfricaAchTransaction> findByCurrencyCode(String currencyCode);

  /** Find transactions created after timestamp */
  List<BankservAfricaAchTransaction> findByCreatedAtAfter(Instant timestamp);

  /** Count transactions by batch ID */
  long countByAchBatchId(String achBatchId);

  /** Count transactions by status */
  long countByStatus(String status);

  /** Count transactions by transaction type */
  long countByTransactionType(String transactionType);

  /** Count transactions by originator ID */
  long countByOriginatorId(String originatorId);

  /** Count transactions by receiver ID */
  long countByReceiverId(String receiverId);

  /** Count transactions by currency code */
  long countByCurrencyCode(String currencyCode);
}
