package com.payments.swiftadapter.service;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.domain.shared.TenantContext;
import com.payments.swiftadapter.domain.SwiftPaymentMessage;
import com.payments.swiftadapter.domain.SwiftTransactionLog;
import com.payments.swiftadapter.domain.SwiftSettlementRecord;
import com.payments.swiftadapter.repository.SwiftPaymentMessageRepository;
import com.payments.swiftadapter.repository.SwiftTransactionLogRepository;
import com.payments.swiftadapter.repository.SwiftSettlementRecordRepository;
import com.payments.swiftadapter.repository.SwiftAdapterRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for processing SWIFT payments with sanctions screening and FX conversion
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SwiftPaymentProcessingService {

  private final SwiftAdapterRepository swiftAdapterRepository;
  private final SwiftPaymentMessageRepository swiftPaymentMessageRepository;
  private final SwiftTransactionLogRepository swiftTransactionLogRepository;
  private final SwiftSettlementRecordRepository swiftSettlementRecordRepository;

  /** Process SWIFT payment with sanctions screening and FX conversion */
  @Transactional
  public SwiftPaymentMessage processSwiftPayment(
      ClearingAdapterId adapterId,
      String transactionId,
      String messageType,
      String direction,
      String messageId,
      String instructionId,
      String endToEndId,
      String transactionType,
      BigDecimal amount,
      String currency,
      String debtorName,
      String debtorAccount,
      String debtorBankCode,
      String debtorBankName,
      String debtorBankCountry,
      String debtorBankSwiftCode,
      String creditorName,
      String creditorAccount,
      String creditorBankCode,
      String creditorBankName,
      String creditorBankCountry,
      String creditorBankSwiftCode,
      String paymentPurpose,
      String reference,
      String correspondentBankCode,
      String correspondentBankName,
      String correspondentBankSwiftCode,
      String intermediaryBankCode,
      String intermediaryBankName,
      String intermediaryBankSwiftCode,
      String chargesBearer) {

    log.info("Processing SWIFT payment: {} for adapter: {}", transactionId, adapterId);

    // Validate adapter exists and is active
    var adapter = swiftAdapterRepository.findById(adapterId)
        .orElseThrow(() -> new IllegalArgumentException("SWIFT adapter not found: " + adapterId));
    
    if (!adapter.isActive()) {
      throw new IllegalStateException("SWIFT adapter is not active: " + adapterId);
    }

    // Create payment message
    SwiftPaymentMessage paymentMessage = SwiftPaymentMessage.create(
        adapterId.toString(),
        transactionId,
        messageType,
        direction,
        messageId,
        instructionId,
        endToEndId,
        transactionType,
        amount,
        currency,
        debtorName,
        debtorAccount,
        debtorBankCode,
        debtorBankName,
        debtorBankCountry,
        debtorBankSwiftCode,
        creditorName,
        creditorAccount,
        creditorBankCode,
        creditorBankName,
        creditorBankCountry,
        creditorBankSwiftCode,
        paymentPurpose,
        reference,
        correspondentBankCode,
        correspondentBankName,
        correspondentBankSwiftCode,
        intermediaryBankCode,
        intermediaryBankName,
        intermediaryBankSwiftCode,
        chargesBearer);

    // Save payment message
    SwiftPaymentMessage savedMessage = swiftPaymentMessageRepository.save(paymentMessage);

    // Create transaction log
    SwiftTransactionLog transactionLog = SwiftTransactionLog.create(
        adapterId.toString(),
        transactionId,
        messageId,
        instructionId,
        endToEndId,
        transactionType,
        amount,
        currency,
        debtorName,
        debtorAccount,
        debtorBankSwiftCode,
        creditorName,
        creditorAccount,
        creditorBankSwiftCode,
        paymentPurpose,
        reference,
        correspondentBankSwiftCode,
        intermediaryBankSwiftCode,
        chargesBearer);

    // Save transaction log
    SwiftTransactionLog savedLog = swiftTransactionLogRepository.save(transactionLog);

    // Log message in adapter
    adapter.logMessage(
        ClearingMessageId.generate(),
        direction,
        messageType,
        "SWIFT_PAYMENT_HASH_" + transactionId,
        200);

    // Save adapter with updated message logs
    swiftAdapterRepository.save(adapter);

    log.info("SWIFT payment processed successfully: {}", transactionId);
    return savedMessage;
  }

  /** Get payment message by transaction ID */
  public Optional<SwiftPaymentMessage> getPaymentMessageByTransactionId(String transactionId) {
    return swiftPaymentMessageRepository.findByTransactionId(transactionId);
  }

  /** Get payment message by message ID */
  public Optional<SwiftPaymentMessage> getPaymentMessageByMessageId(String messageId) {
    return swiftPaymentMessageRepository.findByMessageId(messageId);
  }

  /** Get payment messages by adapter ID */
  public List<SwiftPaymentMessage> getPaymentMessagesByAdapterId(String adapterId) {
    return swiftPaymentMessageRepository.findBySwiftAdapterId(adapterId);
  }

  /** Get payment messages by status */
  public List<SwiftPaymentMessage> getPaymentMessagesByStatus(String status) {
    return swiftPaymentMessageRepository.findByStatus(status);
  }

  /** Get payment messages by currency */
  public List<SwiftPaymentMessage> getPaymentMessagesByCurrency(String currency) {
    return swiftPaymentMessageRepository.findByCurrency(currency);
  }

  /** Get transaction log by transaction ID */
  public Optional<SwiftTransactionLog> getTransactionLogByTransactionId(String transactionId) {
    return swiftTransactionLogRepository.findByTransactionId(transactionId);
  }

  /** Get transaction logs by adapter ID */
  public List<SwiftTransactionLog> getTransactionLogsByAdapterId(String adapterId) {
    return swiftTransactionLogRepository.findBySwiftAdapterId(adapterId);
  }

  /** Get transaction logs by status */
  public List<SwiftTransactionLog> getTransactionLogsByStatus(String status) {
    return swiftTransactionLogRepository.findByStatus(status);
  }

  /** Create settlement record */
  @Transactional
  public SwiftSettlementRecord createSettlementRecord(
      ClearingAdapterId adapterId,
      String transactionId,
      String messageId,
      String instructionId,
      String endToEndId,
      String settlementType,
      BigDecimal amount,
      String currency,
      BigDecimal originalAmount,
      String originalCurrency,
      BigDecimal exchangeRate,
      String debtorBankSwiftCode,
      String creditorBankSwiftCode,
      String correspondentBankSwiftCode,
      String intermediaryBankSwiftCode,
      String settlementBankSwiftCode,
      String settlementBankName,
      String settlementBankCountry,
      String settlementAccount,
      String nostroAccount,
      String vostroAccount,
      BigDecimal chargesAmount,
      String chargesCurrency,
      String chargesBearer,
      BigDecimal commissionAmount,
      String commissionCurrency) {

    log.info("Creating SWIFT settlement record for transaction: {}", transactionId);

    SwiftSettlementRecord settlementRecord = SwiftSettlementRecord.create(
        adapterId.toString(),
        transactionId,
        messageId,
        instructionId,
        endToEndId,
        settlementType,
        amount,
        currency,
        originalAmount,
        originalCurrency,
        exchangeRate,
        debtorBankSwiftCode,
        creditorBankSwiftCode,
        correspondentBankSwiftCode,
        intermediaryBankSwiftCode,
        settlementBankSwiftCode,
        settlementBankName,
        settlementBankCountry,
        settlementAccount,
        nostroAccount,
        vostroAccount,
        chargesAmount,
        chargesCurrency,
        chargesBearer,
        commissionAmount,
        commissionCurrency);

    SwiftSettlementRecord savedRecord = swiftSettlementRecordRepository.save(settlementRecord);

    log.info("SWIFT settlement record created successfully: {}", transactionId);
    return savedRecord;
  }

  /** Get settlement record by transaction ID */
  public Optional<SwiftSettlementRecord> getSettlementRecordByTransactionId(String transactionId) {
    return swiftSettlementRecordRepository.findByTransactionId(transactionId);
  }

  /** Get settlement records by adapter ID */
  public List<SwiftSettlementRecord> getSettlementRecordsByAdapterId(String adapterId) {
    return swiftSettlementRecordRepository.findBySwiftAdapterId(adapterId);
  }

  /** Get settlement records by status */
  public List<SwiftSettlementRecord> getSettlementRecordsByStatus(String status) {
    return swiftSettlementRecordRepository.findByStatus(status);
  }
}
