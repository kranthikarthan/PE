package com.payments.samosadapter.service;

import com.payments.domain.shared.TenantContext;
import com.payments.samosadapter.domain.SamosPaymentMessage;
import com.payments.samosadapter.repository.SamosPaymentMessageRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SAMOS Payment Service
 *
 * <p>Business logic for managing SAMOS payment messages and ISO 20022 processing. Handles payment
 * submission, status tracking, and message lifecycle management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SamosPaymentService {

  private final SamosPaymentMessageRepository samosPaymentMessageRepository;
  private final SamosIso20022Service samosIso20022Service;

  /** Submit payment to SAMOS */
  public SamosPaymentMessage submitPayment(
      TenantContext tenantContext,
      String paymentId,
      String messageId,
      SamosPaymentMessage.MessageType messageType,
      String iso20022Payload) {

    log.info(
        "Submitting payment to SAMOS: {} for tenant: {}", paymentId, tenantContext.getTenantId());

    // Generate payload hash for integrity checking
    String payloadHash = samosIso20022Service.generatePayloadHash(iso20022Payload);

    SamosPaymentMessage message =
        SamosPaymentMessage.create(
            tenantContext,
            paymentId,
            messageId,
            messageType,
            SamosPaymentMessage.Direction.OUTBOUND,
            iso20022Payload,
            payloadHash);

    SamosPaymentMessage savedMessage = samosPaymentMessageRepository.save(message);

    log.info("Created SAMOS payment message: {} for payment: {}", messageId, paymentId);
    return savedMessage;
  }

  /** Process incoming SAMOS message */
  public SamosPaymentMessage processIncomingMessage(
      TenantContext tenantContext,
      String paymentId,
      String messageId,
      SamosPaymentMessage.MessageType messageType,
      String iso20022Payload) {

    log.info("Processing incoming SAMOS message: {} for payment: {}", messageId, paymentId);

    String payloadHash = samosIso20022Service.generatePayloadHash(iso20022Payload);

    SamosPaymentMessage message =
        SamosPaymentMessage.create(
            tenantContext,
            paymentId,
            messageId,
            messageType,
            SamosPaymentMessage.Direction.INBOUND,
            iso20022Payload,
            payloadHash);

    SamosPaymentMessage savedMessage = samosPaymentMessageRepository.save(message);

    log.info("Processed incoming SAMOS message: {} for payment: {}", messageId, paymentId);
    return savedMessage;
  }

  /** Mark message as sent */
  public SamosPaymentMessage markMessageAsSent(String messageId) {
    log.info("Marking SAMOS message as sent: {}", messageId);

    SamosPaymentMessage message =
        samosPaymentMessageRepository
            .findByMessageId(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

    message.markAsSent();

    SamosPaymentMessage updatedMessage = samosPaymentMessageRepository.save(message);

    log.info("Marked SAMOS message as sent: {}", messageId);
    return updatedMessage;
  }

  /** Mark message as received with response */
  public SamosPaymentMessage markMessageAsReceived(
      String messageId, String responseCode, String responseMessage) {

    log.info(
        "Marking SAMOS message as received: {} with response code: {}", messageId, responseCode);

    SamosPaymentMessage message =
        samosPaymentMessageRepository
            .findByMessageId(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

    message.markAsReceived(responseCode, responseMessage);

    SamosPaymentMessage updatedMessage = samosPaymentMessageRepository.save(message);

    log.info("Marked SAMOS message as received: {}", messageId);
    return updatedMessage;
  }

  /** Mark message as failed */
  public SamosPaymentMessage markMessageAsFailed(
      String messageId, String errorCode, String errorMessage) {

    log.info("Marking SAMOS message as failed: {} with error code: {}", messageId, errorCode);

    SamosPaymentMessage message =
        samosPaymentMessageRepository
            .findByMessageId(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

    message.markAsFailed(errorCode, errorMessage);

    SamosPaymentMessage updatedMessage = samosPaymentMessageRepository.save(message);

    log.info("Marked SAMOS message as failed: {}", messageId);
    return updatedMessage;
  }

  /** Get payment messages by payment ID */
  @Transactional(readOnly = true)
  public List<SamosPaymentMessage> getPaymentMessages(UUID tenantId, String paymentId) {
    return samosPaymentMessageRepository.findByTenantIdAndPaymentId(tenantId, paymentId);
  }

  /** Get payment message by message ID */
  @Transactional(readOnly = true)
  public Optional<SamosPaymentMessage> getPaymentMessage(String messageId) {
    return samosPaymentMessageRepository.findByMessageId(messageId);
  }

  /** Get pending outbound messages for processing */
  @Transactional(readOnly = true)
  public List<SamosPaymentMessage> getPendingOutboundMessages(UUID tenantId) {
    return samosPaymentMessageRepository.findPendingOutboundMessages(tenantId);
  }

  /** Get failed outbound messages for retry */
  @Transactional(readOnly = true)
  public List<SamosPaymentMessage> getFailedOutboundMessages(UUID tenantId) {
    return samosPaymentMessageRepository.findFailedOutboundMessages(tenantId);
  }

  /** Get payment messages with pagination */
  @Transactional(readOnly = true)
  public Page<SamosPaymentMessage> getPaymentMessages(UUID tenantId, Pageable pageable) {
    return samosPaymentMessageRepository.findByTenantId(tenantId, pageable);
  }

  /** Get payment messages by status */
  @Transactional(readOnly = true)
  public List<SamosPaymentMessage> getPaymentMessagesByStatus(
      UUID tenantId, SamosPaymentMessage.MessageStatus status) {
    return samosPaymentMessageRepository.findByTenantIdAndStatus(tenantId, status);
  }

  /** Get payment messages by message type */
  @Transactional(readOnly = true)
  public List<SamosPaymentMessage> getPaymentMessagesByType(
      UUID tenantId, SamosPaymentMessage.MessageType messageType) {
    return samosPaymentMessageRepository.findByTenantIdAndMessageType(tenantId, messageType);
  }

  /** Get payment messages by date range */
  @Transactional(readOnly = true)
  public List<SamosPaymentMessage> getPaymentMessagesByDateRange(
      UUID tenantId, Instant startDate, Instant endDate) {
    return samosPaymentMessageRepository.findByTenantIdAndCreatedAtBetween(
        tenantId, startDate, endDate);
  }

  /** Get message count by status */
  @Transactional(readOnly = true)
  public long getMessageCountByStatus(UUID tenantId, SamosPaymentMessage.MessageStatus status) {
    return samosPaymentMessageRepository.countByTenantIdAndStatus(tenantId, status);
  }

  /** Validate ISO 20022 message format */
  @Transactional(readOnly = true)
  public boolean validateIso20022Message(
      String iso20022Payload, SamosPaymentMessage.MessageType messageType) {
    return samosIso20022Service.validateMessage(iso20022Payload, messageType);
  }

  /** Generate ISO 20022 message */
  @Transactional(readOnly = true)
  public String generateIso20022Message(
      SamosPaymentMessage.MessageType messageType, Object paymentData) {
    return samosIso20022Service.generateMessage(messageType, paymentData);
  }
}
