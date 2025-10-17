package com.payments.bankservafricaadapter.service;

import com.payments.bankservafricaadapter.domain.BankservAfricaAdapter;
import com.payments.bankservafricaadapter.domain.BankservAfricaEftMessage;
import com.payments.bankservafricaadapter.repository.BankservAfricaAdapterRepository;
import com.payments.bankservafricaadapter.repository.BankservAfricaEftMessageRepository;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for BankservAfrica EFT batch processing */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankservAfricaEftProcessingService {

  private final BankservAfricaAdapterRepository adapterRepository;
  private final BankservAfricaEftMessageRepository eftMessageRepository;

  /** Process EFT batch */
  @Transactional
  public BankservAfricaEftMessage processEftBatch(
      ClearingAdapterId adapterId,
      String batchId,
      String messageType,
      String direction,
      String payload) {

    log.info("Processing EFT batch: {} for adapter: {}", batchId, adapterId);

    BankservAfricaAdapter adapter =
        adapterRepository
            .findById(adapterId)
            .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + adapterId));

    if (!adapter.isActive()) {
      throw new IllegalStateException("Adapter is not active: " + adapterId);
    }

    // Generate message ID
    String messageId = ClearingMessageId.generate().toString();

    // Create payload hash
    String payloadHash = generatePayloadHash(payload);

    // Create EFT message
    BankservAfricaEftMessage message =
        BankservAfricaEftMessage.create(
            ClearingMessageId.generate(),
            adapterId,
            batchId,
            messageId,
            messageType,
            direction,
            payload,
            payloadHash);

    // Mark as processing
    message.markAsProcessing();

    // Save message
    BankservAfricaEftMessage savedMessage = eftMessageRepository.save(message);

    // Add to adapter
    adapter.addEftMessage(savedMessage);
    adapterRepository.save(adapter);

    log.info("Successfully processed EFT batch: {} with message ID: {}", batchId, messageId);

    return savedMessage;
  }

  /** Update EFT message status */
  @Transactional
  public BankservAfricaEftMessage updateEftMessageStatus(
      ClearingMessageId messageId,
      String status,
      Integer statusCode,
      String errorCode,
      String errorMessage) {

    log.info("Updating EFT message status: {} to {}", messageId, status);

    BankservAfricaEftMessage message =
        eftMessageRepository
            .findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("EFT message not found: " + messageId));

    message.updateStatus(status, statusCode, errorCode, errorMessage);

    BankservAfricaEftMessage updatedMessage = eftMessageRepository.save(message);

    log.info("Successfully updated EFT message status: {}", messageId);

    return updatedMessage;
  }

  /** Get EFT message by ID */
  public Optional<BankservAfricaEftMessage> getEftMessage(ClearingMessageId messageId) {
    return eftMessageRepository.findById(messageId);
  }

  /** Get EFT messages by batch ID */
  public List<BankservAfricaEftMessage> getEftMessagesByBatchId(String batchId) {
    return eftMessageRepository.findByBatchId(batchId);
  }

  /** Get EFT messages by adapter ID */
  public List<BankservAfricaEftMessage> getEftMessagesByAdapterId(ClearingAdapterId adapterId) {
    return eftMessageRepository.findByAdapterId(adapterId.toString());
  }

  /** Get EFT messages by status */
  public List<BankservAfricaEftMessage> getEftMessagesByStatus(String status) {
    return eftMessageRepository.findByStatus(status);
  }

  /** Get EFT messages by batch ID and status */
  public List<BankservAfricaEftMessage> getEftMessagesByBatchIdAndStatus(
      String batchId, String status) {
    return eftMessageRepository.findByBatchIdAndStatus(batchId, status);
  }

  /** Get EFT messages by direction */
  public List<BankservAfricaEftMessage> getEftMessagesByDirection(String direction) {
    return eftMessageRepository.findByDirection(direction);
  }

  /** Get EFT messages by message type */
  public List<BankservAfricaEftMessage> getEftMessagesByMessageType(String messageType) {
    return eftMessageRepository.findByMessageType(messageType);
  }

  /** Get EFT messages created after timestamp */
  public List<BankservAfricaEftMessage> getEftMessagesCreatedAfter(Instant timestamp) {
    return eftMessageRepository.findByCreatedAtAfter(timestamp);
  }

  /** Get EFT messages by batch ID and direction */
  public List<BankservAfricaEftMessage> getEftMessagesByBatchIdAndDirection(
      String batchId, String direction) {
    return eftMessageRepository.findByBatchIdAndDirection(batchId, direction);
  }

  /** Count EFT messages by batch ID */
  public long countEftMessagesByBatchId(String batchId) {
    return eftMessageRepository.countByBatchId(batchId);
  }

  /** Count EFT messages by batch ID and status */
  public long countEftMessagesByBatchIdAndStatus(String batchId, String status) {
    return eftMessageRepository.countByBatchIdAndStatus(batchId, status);
  }

  /** Generate payload hash */
  private String generatePayloadHash(String payload) {
    // Simple hash implementation - in production, use proper hashing
    return String.valueOf(payload.hashCode());
  }
}
