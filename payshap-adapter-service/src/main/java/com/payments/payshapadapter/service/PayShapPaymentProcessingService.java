package com.payments.payshapadapter.service;

import com.payments.domain.clearing.AdapterOperationalStatus;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.payshapadapter.domain.PayShapAdapter;
import com.payments.payshapadapter.domain.PayShapPaymentMessage;
import com.payments.payshapadapter.domain.PayShapSettlementRecord;
import com.payments.payshapadapter.domain.PayShapTransactionLog;
import com.payments.payshapadapter.repository.PayShapAdapterRepository;
import com.payments.payshapadapter.repository.PayShapPaymentMessageRepository;
import com.payments.payshapadapter.repository.PayShapSettlementRecordRepository;
import com.payments.payshapadapter.repository.PayShapTransactionLogRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for processing PayShap payments with proxy registry integration */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayShapPaymentProcessingService {

  private final PayShapAdapterRepository payShapAdapterRepository;
  private final PayShapPaymentMessageRepository payShapPaymentMessageRepository;
  private final PayShapTransactionLogRepository payShapTransactionLogRepository;
  private final PayShapSettlementRecordRepository payShapSettlementRecordRepository;

  /** Process PayShap payment with proxy registry integration */
  @Transactional
  public PayShapPaymentMessage processPayShapPayment(
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
      String debtorMobile,
      String debtorEmail,
      String creditorName,
      String creditorAccount,
      String creditorBankCode,
      String creditorMobile,
      String creditorEmail,
      String paymentPurpose,
      String reference,
      String proxyType,
      String proxyValue) {
    log.info("Processing PayShap payment: {} for adapter: {}", transactionId, adapterId);

    // Validate adapter is active
    PayShapAdapter adapter =
        payShapAdapterRepository
            .findById(adapterId)
            .orElseThrow(
                () -> new IllegalArgumentException("PayShap adapter not found: " + adapterId));

    if (adapter.getStatus() != AdapterOperationalStatus.ACTIVE) {
      throw new IllegalStateException("PayShap adapter is not active: " + adapterId);
    }

    // Validate amount limit (R3,000 for PayShap)
    if (amount.compareTo(new BigDecimal("3000.00")) > 0) {
      throw new IllegalArgumentException("PayShap payment amount cannot exceed R3,000");
    }

    // Create payment message
    PayShapPaymentMessage paymentMessage =
        PayShapPaymentMessage.create(
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
            debtorMobile,
            debtorEmail,
            creditorName,
            creditorAccount,
            creditorBankCode,
            creditorMobile,
            creditorEmail,
            paymentPurpose,
            reference,
            proxyType,
            proxyValue);

    // Save payment message
    PayShapPaymentMessage savedMessage = payShapPaymentMessageRepository.save(paymentMessage);

    // Log transaction
    PayShapTransactionLog transactionLog =
        PayShapTransactionLog.create(
            adapterId.toString(),
            transactionId,
            "INFO",
            "PayShap payment initiated",
            "Payment amount: " + amount + " " + currency);
    payShapTransactionLogRepository.save(transactionLog);

    // Create settlement record for instant settlement
    PayShapSettlementRecord settlementRecord =
        PayShapSettlementRecord.create(
            adapterId.toString(),
            "SETTLE-" + transactionId,
            transactionId,
            amount,
            currency,
            "INSTANT");
    payShapSettlementRecordRepository.save(settlementRecord);

    log.info("PayShap payment processed successfully: {}", transactionId);
    return savedMessage;
  }

  /** Submit payment for processing */
  @Transactional
  public PayShapPaymentMessage submitPayment(ClearingMessageId messageId) {
    log.info("Submitting PayShap payment: {}", messageId);

    PayShapPaymentMessage paymentMessage =
        payShapPaymentMessageRepository
            .findById(messageId.toString())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "PayShap payment message not found: " + messageId));

    paymentMessage.markAsSubmitted();
    return payShapPaymentMessageRepository.save(paymentMessage);
  }

  /** Process payment response */
  @Transactional
  public PayShapPaymentMessage processPaymentResponse(
      ClearingMessageId messageId, String responseCode, String responseMessage, boolean success) {
    log.info("Processing PayShap payment response: {} - Success: {}", messageId, success);

    PayShapPaymentMessage paymentMessage =
        payShapPaymentMessageRepository
            .findById(messageId.toString())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "PayShap payment message not found: " + messageId));

    paymentMessage.processResponse(responseCode, responseMessage, success);
    return payShapPaymentMessageRepository.save(paymentMessage);
  }

  /** Get payment message by ID */
  public Optional<PayShapPaymentMessage> getPaymentMessage(ClearingMessageId messageId) {
    return payShapPaymentMessageRepository.findById(messageId.toString());
  }

  /** Get payment messages by adapter */
  public List<PayShapPaymentMessage> getPaymentMessagesByAdapter(ClearingAdapterId adapterId) {
    return payShapPaymentMessageRepository.findByAdapterId(adapterId.toString());
  }

  /** Get payment message by transaction ID */
  public PayShapPaymentMessage getPaymentMessageByTransactionId(String transactionId) {
    return payShapPaymentMessageRepository.findByTransactionId(transactionId);
  }

  /** Get payment messages by status */
  public List<PayShapPaymentMessage> getPaymentMessagesByStatus(String status) {
    return payShapPaymentMessageRepository.findByStatus(status);
  }

  /** Get payment messages by adapter and status */
  public List<PayShapPaymentMessage> getPaymentMessagesByAdapterAndStatus(
      ClearingAdapterId adapterId, String status) {
    return payShapPaymentMessageRepository.findByAdapterIdAndStatus(adapterId.toString(), status);
  }

  /** Get transaction logs by adapter */
  public List<PayShapTransactionLog> getTransactionLogsByAdapter(ClearingAdapterId adapterId) {
    return payShapTransactionLogRepository.findByAdapterId(adapterId.toString());
  }

  /** Get transaction logs by transaction ID */
  public List<PayShapTransactionLog> getTransactionLogsByTransactionId(String transactionId) {
    return payShapTransactionLogRepository.findByTransactionId(transactionId);
  }

  /** Get payment message count */
  public long getPaymentMessageCount() {
    return payShapPaymentMessageRepository.count();
  }

  /** Get payment message count by status */
  public long getPaymentMessageCountByStatus(String status) {
    return payShapPaymentMessageRepository.countByStatus(status);
  }

  /** Get payment message count by adapter */
  public long getPaymentMessageCountByAdapter(ClearingAdapterId adapterId) {
    return payShapPaymentMessageRepository.countByAdapterId(adapterId.toString());
  }
}
