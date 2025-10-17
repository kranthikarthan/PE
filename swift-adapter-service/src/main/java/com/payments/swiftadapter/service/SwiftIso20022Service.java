package com.payments.swiftadapter.service;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.swiftadapter.domain.SwiftPaymentMessage;
import com.payments.swiftadapter.repository.SwiftPaymentMessageRepository;
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
 * Service for handling SWIFT ISO 20022 message processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SwiftIso20022Service {

  private final SwiftAdapterRepository swiftAdapterRepository;
  private final SwiftPaymentMessageRepository swiftPaymentMessageRepository;

  /** Process ISO 20022 pacs.008 message (Customer Credit Transfer) */
  @Transactional
  public SwiftPaymentMessage processPacs008Message(
      ClearingAdapterId adapterId,
      String messageId,
      String instructionId,
      String endToEndId,
      String transactionId,
      BigDecimal amount,
      String currency,
      String debtorName,
      String debtorAccount,
      String debtorBankSwiftCode,
      String creditorName,
      String creditorAccount,
      String creditorBankSwiftCode,
      String paymentPurpose,
      String reference) {

    log.info("Processing ISO 20022 pacs.008 message: {} for adapter: {}", messageId, adapterId);

    // Validate adapter exists and is active
    var adapter = swiftAdapterRepository.findById(adapterId)
        .orElseThrow(() -> new IllegalArgumentException("SWIFT adapter not found: " + adapterId));
    
    if (!adapter.isActive()) {
      throw new IllegalStateException("SWIFT adapter is not active: " + adapterId);
    }

    // Create payment message for pacs.008
    SwiftPaymentMessage paymentMessage = SwiftPaymentMessage.create(
        adapterId.toString(),
        transactionId,
        "pacs.008",
        "OUTBOUND",
        messageId,
        instructionId,
        endToEndId,
        "CREDIT",
        amount,
        currency,
        debtorName,
        debtorAccount,
        null, // debtorBankCode
        null, // debtorBankName
        null, // debtorBankCountry
        debtorBankSwiftCode,
        creditorName,
        creditorAccount,
        null, // creditorBankCode
        null, // creditorBankName
        null, // creditorBankCountry
        creditorBankSwiftCode,
        paymentPurpose,
        reference,
        null, // correspondentBankCode
        null, // correspondentBankName
        null, // correspondentBankSwiftCode
        null, // intermediaryBankCode
        null, // intermediaryBankName
        null, // intermediaryBankSwiftCode
        "OUR");

    // Save payment message
    SwiftPaymentMessage savedMessage = swiftPaymentMessageRepository.save(paymentMessage);

    // Log message in adapter
    adapter.logMessage(
        ClearingMessageId.generate(),
        "OUTBOUND",
        "pacs.008",
        "PACS008_HASH_" + messageId,
        200);

    // Save adapter with updated message logs
    swiftAdapterRepository.save(adapter);

    log.info("ISO 20022 pacs.008 message processed successfully: {}", messageId);
    return savedMessage;
  }

  /** Process ISO 20022 pacs.002 message (Payment Status Report) */
  @Transactional
  public SwiftPaymentMessage processPacs002Message(
      ClearingAdapterId adapterId,
      String messageId,
      String instructionId,
      String endToEndId,
      String transactionId,
      String status,
      String responseCode,
      String responseMessage) {

    log.info("Processing ISO 20022 pacs.002 message: {} for adapter: {}", messageId, adapterId);

    // Validate adapter exists and is active
    var adapter = swiftAdapterRepository.findById(adapterId)
        .orElseThrow(() -> new IllegalArgumentException("SWIFT adapter not found: " + adapterId));
    
    if (!adapter.isActive()) {
      throw new IllegalStateException("SWIFT adapter is not active: " + adapterId);
    }

    // Find existing payment message by instruction ID
    Optional<SwiftPaymentMessage> existingMessage = swiftPaymentMessageRepository
        .findByInstructionId(instructionId);

    if (existingMessage.isPresent()) {
      SwiftPaymentMessage paymentMessage = existingMessage.get();
      
      // Update payment message with status
      paymentMessage.processResponse(responseCode, responseMessage, "COMPLETED".equals(status));
      
      // Save updated payment message
      SwiftPaymentMessage savedMessage = swiftPaymentMessageRepository.save(paymentMessage);

      // Log message in adapter
      adapter.logMessage(
          ClearingMessageId.generate(),
          "INBOUND",
          "pacs.002",
          "PACS002_HASH_" + messageId,
          200);

      // Save adapter with updated message logs
      swiftAdapterRepository.save(adapter);

      log.info("ISO 20022 pacs.002 message processed successfully: {}", messageId);
      return savedMessage;
    } else {
      throw new IllegalArgumentException("Payment message not found for instruction ID: " + instructionId);
    }
  }

  /** Process MT103 message (Customer Transfer) */
  @Transactional
  public SwiftPaymentMessage processMt103Message(
      ClearingAdapterId adapterId,
      String messageId,
      String transactionId,
      BigDecimal amount,
      String currency,
      String debtorName,
      String debtorAccount,
      String debtorBankSwiftCode,
      String creditorName,
      String creditorAccount,
      String creditorBankSwiftCode,
      String paymentPurpose,
      String reference) {

    log.info("Processing MT103 message: {} for adapter: {}", messageId, adapterId);

    // Validate adapter exists and is active
    var adapter = swiftAdapterRepository.findById(adapterId)
        .orElseThrow(() -> new IllegalArgumentException("SWIFT adapter not found: " + adapterId));
    
    if (!adapter.isActive()) {
      throw new IllegalStateException("SWIFT adapter is not active: " + adapterId);
    }

    // Create payment message for MT103
    SwiftPaymentMessage paymentMessage = SwiftPaymentMessage.create(
        adapterId.toString(),
        transactionId,
        "MT103",
        "OUTBOUND",
        messageId,
        null, // instructionId
        null, // endToEndId
        "CREDIT",
        amount,
        currency,
        debtorName,
        debtorAccount,
        null, // debtorBankCode
        null, // debtorBankName
        null, // debtorBankCountry
        debtorBankSwiftCode,
        creditorName,
        creditorAccount,
        null, // creditorBankCode
        null, // creditorBankName
        null, // creditorBankCountry
        creditorBankSwiftCode,
        paymentPurpose,
        reference,
        null, // correspondentBankCode
        null, // correspondentBankName
        null, // correspondentBankSwiftCode
        null, // intermediaryBankCode
        null, // intermediaryBankName
        null, // intermediaryBankSwiftCode
        "OUR");

    // Save payment message
    SwiftPaymentMessage savedMessage = swiftPaymentMessageRepository.save(paymentMessage);

    // Log message in adapter
    adapter.logMessage(
        ClearingMessageId.generate(),
        "OUTBOUND",
        "MT103",
        "MT103_HASH_" + messageId,
        200);

    // Save adapter with updated message logs
    swiftAdapterRepository.save(adapter);

    log.info("MT103 message processed successfully: {}", messageId);
    return savedMessage;
  }

  /** Get payment message by message ID */
  public Optional<SwiftPaymentMessage> getPaymentMessageByMessageId(String messageId) {
    return swiftPaymentMessageRepository.findByMessageId(messageId);
  }

  /** Get payment message by instruction ID */
  public Optional<SwiftPaymentMessage> getPaymentMessageByInstructionId(String instructionId) {
    return swiftPaymentMessageRepository.findByInstructionId(instructionId);
  }

  /** Get payment messages by message type */
  public List<SwiftPaymentMessage> getPaymentMessagesByMessageType(String messageType) {
    return swiftPaymentMessageRepository.findByMessageType(messageType);
  }

  /** Get payment messages by direction */
  public List<SwiftPaymentMessage> getPaymentMessagesByDirection(String direction) {
    return swiftPaymentMessageRepository.findByDirection(direction);
  }

  /** Get payment messages by currency */
  public List<SwiftPaymentMessage> getPaymentMessagesByCurrency(String currency) {
    return swiftPaymentMessageRepository.findByCurrency(currency);
  }

  /** Get payment messages by status */
  public List<SwiftPaymentMessage> getPaymentMessagesByStatus(String status) {
    return swiftPaymentMessageRepository.findByStatus(status);
  }

  /** Get payment messages by sanctions screening status */
  public List<SwiftPaymentMessage> getPaymentMessagesBySanctionsScreeningStatus(String status) {
    return swiftPaymentMessageRepository.findBySanctionsScreeningStatus(status);
  }

  /** Get payment messages by FX conversion status */
  public List<SwiftPaymentMessage> getPaymentMessagesByFxConversionStatus(String status) {
    return swiftPaymentMessageRepository.findByFxConversionStatus(status);
  }
}
