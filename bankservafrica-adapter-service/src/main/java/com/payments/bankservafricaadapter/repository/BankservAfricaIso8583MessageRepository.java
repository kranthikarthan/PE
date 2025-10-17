package com.payments.bankservafricaadapter.repository;

import com.payments.bankservafricaadapter.domain.BankservAfricaIso8583Message;
import com.payments.domain.shared.ClearingMessageId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for BankservAfrica ISO 8583 Message entities */
@Repository
public interface BankservAfricaIso8583MessageRepository
    extends JpaRepository<BankservAfricaIso8583Message, ClearingMessageId> {

  /** Find message by transaction ID */
  BankservAfricaIso8583Message findByTransactionId(String transactionId);

  /** Find messages by adapter ID */
  @Query(
      "SELECT m FROM BankservAfricaIso8583Message m WHERE m.bankservafricaAdapterId = :adapterId")
  List<BankservAfricaIso8583Message> findByAdapterId(@Param("adapterId") String adapterId);

  /** Find messages by status */
  List<BankservAfricaIso8583Message> findByStatus(String status);

  /** Find messages by direction */
  List<BankservAfricaIso8583Message> findByDirection(String direction);

  /** Find messages by MTI */
  List<BankservAfricaIso8583Message> findByMti(String mti);

  /** Find messages by processing code */
  List<BankservAfricaIso8583Message> findByProcessingCode(String processingCode);

  /** Find messages by response code */
  List<BankservAfricaIso8583Message> findByResponseCode(String responseCode);

  /** Find messages by merchant ID */
  List<BankservAfricaIso8583Message> findByMerchantId(String merchantId);

  /** Find messages by terminal ID */
  List<BankservAfricaIso8583Message> findByTerminalId(String terminalId);

  /** Find messages by card number */
  List<BankservAfricaIso8583Message> findByCardNumber(String cardNumber);

  /** Find messages by amount range */
  List<BankservAfricaIso8583Message> findByAmountBetween(
      BigDecimal minAmount, BigDecimal maxAmount);

  /** Find messages by currency code */
  List<BankservAfricaIso8583Message> findByCurrencyCode(String currencyCode);

  /** Find messages created after timestamp */
  List<BankservAfricaIso8583Message> findByCreatedAtAfter(Instant timestamp);

  /** Count messages by status */
  long countByStatus(String status);

  /** Count messages by direction */
  long countByDirection(String direction);

  /** Count messages by MTI */
  long countByMti(String mti);

  /** Count messages by processing code */
  long countByProcessingCode(String processingCode);

  /** Count messages by response code */
  long countByResponseCode(String responseCode);

  /** Count messages by merchant ID */
  long countByMerchantId(String merchantId);

  /** Count messages by terminal ID */
  long countByTerminalId(String terminalId);

  /** Count messages by card number */
  long countByCardNumber(String cardNumber);

  /** Count messages by currency code */
  long countByCurrencyCode(String currencyCode);
}
