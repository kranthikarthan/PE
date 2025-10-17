package com.payments.rtcadapter.service;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.rtcadapter.domain.RtcAdapter;
import com.payments.rtcadapter.domain.RtcPaymentMessage;
import com.payments.rtcadapter.domain.RtcTransactionLog;
import com.payments.rtcadapter.repository.RtcAdapterRepository;
import com.payments.rtcadapter.repository.RtcPaymentMessageRepository;
import com.payments.rtcadapter.repository.RtcTransactionLogRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for handling RTC payment processing */
@Service
@RequiredArgsConstructor
@Slf4j
public class RtcPaymentProcessingService {

  private final RtcAdapterRepository rtcAdapterRepository;
  private final RtcPaymentMessageRepository rtcPaymentMessageRepository;
  private final RtcTransactionLogRepository rtcTransactionLogRepository;

  /** Process RTC payment */
  @Transactional
  public RtcPaymentMessage processRtcPayment(
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
      String creditorName,
      String creditorAccount,
      String creditorBankCode,
      String paymentPurpose,
      String reference) {
    log.info("Processing RTC payment: {} for adapter: {}", transactionId, adapterId);

    // Validate adapter is active
    RtcAdapter adapter =
        rtcAdapterRepository
            .findById(adapterId)
            .orElseThrow(() -> new IllegalArgumentException("RTC adapter not found: " + adapterId));

    if (!adapter.isActive()) {
      throw new IllegalStateException("RTC adapter is not active: " + adapterId);
    }

    // Validate amount limit
    if (amount.compareTo(new BigDecimal("5000.00")) > 0) {
      throw new IllegalArgumentException("Amount exceeds R5,000 limit for RTC payments");
    }

    // Create payment message
    ClearingMessageId messageIdObj = ClearingMessageId.generate();
    RtcPaymentMessage paymentMessage =
        RtcPaymentMessage.create(
            messageIdObj,
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
            creditorName,
            creditorAccount,
            creditorBankCode,
            paymentPurpose,
            reference);

    // Save payment message
    RtcPaymentMessage savedMessage = rtcPaymentMessageRepository.save(paymentMessage);

    // Add to adapter
    adapter.addPaymentMessage(savedMessage);
    rtcAdapterRepository.save(adapter);

    // Log transaction
    logTransaction(adapterId, transactionId, "PAYMENT_CREATED", "SUCCESS", null, null, null, null);

    log.info("RTC payment message created successfully: {}", savedMessage.getId());
    return savedMessage;
  }

  /** Submit payment to RTC system */
  @Transactional
  public RtcPaymentMessage submitPayment(ClearingMessageId messageId) {
    log.info("Submitting RTC payment to clearing system: {}", messageId);

    RtcPaymentMessage message =
        rtcPaymentMessageRepository
            .findById(messageId)
            .orElseThrow(
                () -> new IllegalArgumentException("Payment message not found: " + messageId));

    // Mark as submitted
    message.markAsSubmitted();
    RtcPaymentMessage updatedMessage = rtcPaymentMessageRepository.save(message);

    // Log transaction
    logTransaction(
        ClearingAdapterId.of(message.getRtcAdapterId()),
        message.getTransactionId(),
        "PAYMENT_SUBMITTED",
        "SUCCESS",
        null,
        null,
        null,
        null);

    log.info("RTC payment submitted successfully: {}", messageId);
    return updatedMessage;
  }

  /** Process payment response */
  @Transactional
  public RtcPaymentMessage processPaymentResponse(
      ClearingMessageId messageId, String responseCode, String responseMessage, boolean isSuccess) {
    log.info("Processing RTC payment response: {} - Success: {}", messageId, isSuccess);

    RtcPaymentMessage message =
        rtcPaymentMessageRepository
            .findById(messageId)
            .orElseThrow(
                () -> new IllegalArgumentException("Payment message not found: " + messageId));

    if (isSuccess) {
      message.markAsCompleted(responseCode, responseMessage);
    } else {
      message.markAsFailed(responseCode, responseMessage);
    }

    RtcPaymentMessage updatedMessage = rtcPaymentMessageRepository.save(message);

    // Log transaction
    logTransaction(
        ClearingAdapterId.of(message.getRtcAdapterId()),
        message.getTransactionId(),
        "PAYMENT_RESPONSE_PROCESSED",
        isSuccess ? "SUCCESS" : "FAILED",
        responseCode,
        responseMessage,
        null,
        null);

    log.info(
        "RTC payment response processed: {} - Status: {}", messageId, updatedMessage.getStatus());
    return updatedMessage;
  }

  /** Get payment message by ID */
  public Optional<RtcPaymentMessage> getPaymentMessage(ClearingMessageId messageId) {
    return rtcPaymentMessageRepository.findById(messageId);
  }

  /** Get payment messages by adapter */
  public List<RtcPaymentMessage> getPaymentMessagesByAdapter(ClearingAdapterId adapterId) {
    return rtcPaymentMessageRepository.findByAdapterId(adapterId.toString());
  }

  /** Get payment messages by transaction ID */
  public RtcPaymentMessage getPaymentMessageByTransactionId(String transactionId) {
    return rtcPaymentMessageRepository.findByTransactionId(transactionId);
  }

  /** Get payment messages by status */
  public List<RtcPaymentMessage> getPaymentMessagesByStatus(String status) {
    return rtcPaymentMessageRepository.findByStatus(status);
  }

  /** Get payment messages by adapter and status */
  public List<RtcPaymentMessage> getPaymentMessagesByAdapterAndStatus(
      ClearingAdapterId adapterId, String status) {
    return rtcPaymentMessageRepository.findByAdapterIdAndStatus(adapterId.toString(), status);
  }

  /** Log transaction operation */
  private void logTransaction(
      ClearingAdapterId adapterId,
      String transactionId,
      String operation,
      String status,
      String errorCode,
      String errorMessage,
      String requestPayload,
      String responsePayload) {
    try {
      ClearingMessageId logId = ClearingMessageId.generate();
      RtcTransactionLog log =
          RtcTransactionLog.create(
              logId,
              adapterId.toString(),
              transactionId,
              operation,
              status,
              null,
              errorCode,
              errorMessage,
              requestPayload,
              responsePayload);

      rtcTransactionLogRepository.save(log);
    } catch (Exception e) {
      log.error("Failed to log transaction: {}", e.getMessage(), e);
    }
  }

  /** Get transaction logs by adapter */
  public List<RtcTransactionLog> getTransactionLogsByAdapter(ClearingAdapterId adapterId) {
    return rtcTransactionLogRepository.findByAdapterId(adapterId.toString());
  }

  /** Get transaction logs by transaction ID */
  public List<RtcTransactionLog> getTransactionLogsByTransactionId(String transactionId) {
    return rtcTransactionLogRepository.findByTransactionId(transactionId);
  }

  /** Get transaction statistics */
  public long getPaymentMessageCount() {
    return rtcPaymentMessageRepository.count();
  }

  /** Get payment message count by status */
  public long getPaymentMessageCountByStatus(String status) {
    return rtcPaymentMessageRepository.countByStatus(status);
  }

  /** Get payment message count by adapter */
  public long getPaymentMessageCountByAdapter(ClearingAdapterId adapterId) {
    return rtcPaymentMessageRepository.countByAdapterId(adapterId.toString());
  }
}
