package com.payments.paymentinitiation.service;

import com.payments.domain.shared.TenantContext;
import com.payments.iso20022.canonical.CanonicalPaymentModel;
import com.payments.paymentinitiation.entity.*;
import com.payments.paymentinitiation.repository.*;
import com.payments.paymentinitiation.saga.SagaState;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for pain.001/pain.002 database operations
 *
 * <p>Handles persistence and retrieval of pain.001 and pain.002 messages with proper correlation
 * and multi-tenancy support.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Pain001Pain002DatabaseService {

  private final Pain001MessageRepository pain001MessageRepository;
  private final Pain002StatusReportRepository pain002StatusReportRepository;
  private final Pain001Pain002CorrelationRepository correlationRepository;
  private final Pain001AuditLogRepository pain001AuditLogRepository;
  private final Pain002AuditLogRepository pain002AuditLogRepository;

  /** Save pain.001 message to database (simplified version for saga) */
  @Transactional
  public Pain001MessageEntity savePain001Message(
      CanonicalPaymentModel canonicalPayment, String sagaId) {

    log.debug("Saving pain.001 message to database for saga: {}", sagaId);

    Pain001MessageEntity entity =
        Pain001MessageEntity.builder()
            .messageId(canonicalPayment.getMessageId())
            .creationDateTime(
                canonicalPayment.getCreationDateTime().atZone(java.time.ZoneOffset.UTC).toInstant())
            .numberOfTransactions(canonicalPayment.getNumberOfTransactions())
            .controlSum(canonicalPayment.getControlSum())
            .initiatingPartyName(canonicalPayment.getInitiatingPartyName())
            .initiatingPartyId(canonicalPayment.getInitiatingPartyId())
            .messageFormat(Pain001MessageEntity.MessageFormat.XML)
            .rawMessage("") // TODO: Store actual XML
            .parsedMessage(null) // TODO: Store parsed JSON
            .validationStatus(Pain001MessageEntity.ValidationStatus.VALID)
            .validationErrors(null)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    return pain001MessageRepository.save(entity);
  }

  /** Save pain.001 message to database (full version) */
  @Transactional
  public Pain001MessageEntity savePain001Message(
      String pain001Xml,
      CanonicalPaymentModel canonicalPayment,
      TenantContext tenantContext,
      String correlationId) {

    log.debug("Saving pain.001 message to database for correlation: {}", correlationId);

    Pain001MessageEntity entity =
        Pain001MessageEntity.builder()
            .messageId(canonicalPayment.getMessageId())
            .creationDateTime(
                canonicalPayment.getCreationDateTime().atZone(java.time.ZoneOffset.UTC).toInstant())
            .numberOfTransactions(canonicalPayment.getNumberOfTransactions())
            .controlSum(canonicalPayment.getControlSum())
            .initiatingPartyName(canonicalPayment.getInitiatingPartyName())
            .initiatingPartyId(canonicalPayment.getInitiatingPartyId())
            .messageFormat(Pain001MessageEntity.MessageFormat.XML)
            .rawMessage(pain001Xml)
            .parsedMessage(convertCanonicalToJson(canonicalPayment))
            .validationStatus(Pain001MessageEntity.ValidationStatus.PENDING)
            .tenantId(tenantContext.getTenantId())
            .businessUnitId(tenantContext.getBusinessUnitId())
            .build();

    Pain001MessageEntity savedEntity = pain001MessageRepository.save(entity);

    // Create audit log
    createPain001AuditLog(
        savedEntity,
        Pain001AuditLogEntity.Action.CREATED,
        "pain.001 message created",
        correlationId,
        tenantContext);

    log.info("Successfully saved pain.001 message with ID: {}", savedEntity.getId());
    return savedEntity;
  }

  /** Save pain.001 JSON message to database */
  @Transactional
  public Pain001MessageEntity savePain001JsonMessage(
      String pain001Json,
      CanonicalPaymentModel canonicalPayment,
      TenantContext tenantContext,
      String correlationId) {

    log.debug("Saving pain.001 JSON message to database for correlation: {}", correlationId);

    Pain001MessageEntity entity =
        Pain001MessageEntity.builder()
            .messageId(canonicalPayment.getMessageId())
            .creationDateTime(
                canonicalPayment.getCreationDateTime().atZone(java.time.ZoneOffset.UTC).toInstant())
            .numberOfTransactions(canonicalPayment.getNumberOfTransactions())
            .controlSum(canonicalPayment.getControlSum())
            .initiatingPartyName(canonicalPayment.getInitiatingPartyName())
            .initiatingPartyId(canonicalPayment.getInitiatingPartyId())
            .messageFormat(Pain001MessageEntity.MessageFormat.JSON)
            .rawMessage(pain001Json)
            .parsedMessage(convertCanonicalToJson(canonicalPayment))
            .validationStatus(Pain001MessageEntity.ValidationStatus.PENDING)
            .tenantId(tenantContext.getTenantId())
            .businessUnitId(tenantContext.getBusinessUnitId())
            .build();

    Pain001MessageEntity savedEntity = pain001MessageRepository.save(entity);

    // Create audit log
    createPain001AuditLog(
        savedEntity,
        Pain001AuditLogEntity.Action.CREATED,
        "pain.001 JSON message created",
        correlationId,
        tenantContext);

    log.info("Successfully saved pain.001 JSON message with ID: {}", savedEntity.getId());
    return savedEntity;
  }

  /** Save pain.002 status report to database */
  @Transactional
  public Pain002StatusReportEntity savePain002StatusReport(
      String pain002Xml,
      CanonicalPaymentModel canonicalPayment,
      TenantContext tenantContext,
      String correlationId) {

    log.debug("Saving pain.002 status report to database for correlation: {}", correlationId);

    Pain002StatusReportEntity entity =
        Pain002StatusReportEntity.builder()
            .messageId("PAIN002-" + UUID.randomUUID().toString().substring(0, 8))
            .creationDateTime(Instant.now())
            .instigatingAgentBic(canonicalPayment.getChargesAgentBic())
            .instructedAgentBic(canonicalPayment.getChargesAgentBic())
            .messageFormat(Pain002StatusReportEntity.MessageFormat.XML)
            .rawMessage(pain002Xml)
            .parsedMessage(convertCanonicalToJson(canonicalPayment))
            .tenantId(tenantContext.getTenantId())
            .businessUnitId(tenantContext.getBusinessUnitId())
            .build();

    Pain002StatusReportEntity savedEntity = pain002StatusReportRepository.save(entity);

    // Create audit log
    createPain002AuditLog(
        savedEntity,
        Pain002AuditLogEntity.Action.CREATED,
        "pain.002 status report created",
        correlationId,
        tenantContext);

    log.info("Successfully saved pain.002 status report with ID: {}", savedEntity.getId());
    return savedEntity;
  }

  /** Save pain.002 JSON status report to database */
  @Transactional
  public Pain002StatusReportEntity savePain002JsonStatusReport(
      String pain002Json,
      CanonicalPaymentModel canonicalPayment,
      TenantContext tenantContext,
      String correlationId) {

    log.debug("Saving pain.002 JSON status report to database for correlation: {}", correlationId);

    Pain002StatusReportEntity entity =
        Pain002StatusReportEntity.builder()
            .messageId("PAIN002-" + UUID.randomUUID().toString().substring(0, 8))
            .creationDateTime(Instant.now())
            .instigatingAgentBic(canonicalPayment.getChargesAgentBic())
            .instructedAgentBic(canonicalPayment.getChargesAgentBic())
            .messageFormat(Pain002StatusReportEntity.MessageFormat.JSON)
            .rawMessage(pain002Json)
            .parsedMessage(convertCanonicalToJson(canonicalPayment))
            .tenantId(tenantContext.getTenantId())
            .businessUnitId(tenantContext.getBusinessUnitId())
            .build();

    Pain002StatusReportEntity savedEntity = pain002StatusReportRepository.save(entity);

    // Create audit log
    createPain002AuditLog(
        savedEntity,
        Pain002AuditLogEntity.Action.CREATED,
        "pain.002 JSON status report created",
        correlationId,
        tenantContext);

    log.info("Successfully saved pain.002 JSON status report with ID: {}", savedEntity.getId());
    return savedEntity;
  }

  /** Create correlation between pain.001 and pain.002 messages */
  @Transactional
  public Pain001Pain002CorrelationEntity createCorrelation(
      Pain001MessageEntity pain001Message,
      Pain002StatusReportEntity pain002StatusReport,
      String correlationId,
      TenantContext tenantContext) {

    log.debug(
        "Creating correlation between pain.001 and pain.002 messages for correlation: {}",
        correlationId);

    Pain001Pain002CorrelationEntity correlation =
        Pain001Pain002CorrelationEntity.builder()
            .pain001Message(pain001Message)
            .pain002StatusReport(pain002StatusReport)
            .correlationId(correlationId)
            .originalMessageId(pain001Message.getMessageId())
            .statusReportMessageId(pain002StatusReport.getMessageId())
            .correlationStatus(Pain001Pain002CorrelationEntity.CorrelationStatus.CORRELATED)
            .correlationNotes("Automatic correlation created during processing")
            .tenantId(tenantContext.getTenantId())
            .businessUnitId(tenantContext.getBusinessUnitId())
            .build();

    Pain001Pain002CorrelationEntity savedCorrelation = correlationRepository.save(correlation);

    log.info("Successfully created correlation with ID: {}", savedCorrelation.getId());
    return savedCorrelation;
  }

  /** Find pain.001 message by message ID */
  public Optional<Pain001MessageEntity> findPain001MessageByMessageId(String messageId) {
    return pain001MessageRepository.findByMessageId(messageId);
  }

  /** Find pain.002 status report by message ID */
  public Optional<Pain002StatusReportEntity> findPain002StatusReportByMessageId(String messageId) {
    return pain002StatusReportRepository.findByMessageId(messageId);
  }

  /** Find pain.001 messages by tenant and business unit */
  public List<Pain001MessageEntity> findPain001MessagesByTenant(
      String tenantId, String businessUnitId) {
    return pain001MessageRepository.findByTenantIdAndBusinessUnitId(tenantId, businessUnitId);
  }

  /** Find pain.002 status reports by tenant and business unit */
  public List<Pain002StatusReportEntity> findPain002StatusReportsByTenant(
      String tenantId, String businessUnitId) {
    return pain002StatusReportRepository.findByTenantIdAndBusinessUnitId(tenantId, businessUnitId);
  }

  /** Find correlations by correlation ID */
  public Optional<Pain001Pain002CorrelationEntity> findCorrelationsByCorrelationId(
      String correlationId) {
    return correlationRepository.findByCorrelationId(correlationId);
  }

  /** Update pain.001 message validation status */
  @Transactional
  public void updatePain001ValidationStatus(
      UUID pain001MessageId,
      Pain001MessageEntity.ValidationStatus validationStatus,
      String validationErrors) {

    Optional<Pain001MessageEntity> optionalEntity =
        pain001MessageRepository.findById(pain001MessageId);
    if (optionalEntity.isPresent()) {
      Pain001MessageEntity entity = optionalEntity.get();
      entity.setValidationStatus(validationStatus);
      entity.setValidationErrors(validationErrors);
      pain001MessageRepository.save(entity);

      log.info("Updated pain.001 message validation status to: {}", validationStatus);
    }
  }

  /** Convert canonical payment model to JSON string */
  private String convertCanonicalToJson(CanonicalPaymentModel canonicalPayment) {
    // Simple JSON conversion - in production, use proper JSON library
    return String.format(
        """
            {
                "paymentId": "%s",
                "messageId": "%s",
                "status": "%s",
                "amount": %s,
                "currency": "%s",
                "sourceAccount": "%s",
                "destinationAccount": "%s",
                "executionDate": "%s",
                "reference": "%s"
            }
            """,
        canonicalPayment.getPaymentId(),
        canonicalPayment.getMessageId(),
        canonicalPayment.getStatus(),
        canonicalPayment.getAmount(),
        canonicalPayment.getCurrency(),
        canonicalPayment.getSourceAccount(),
        canonicalPayment.getDestinationAccount(),
        canonicalPayment.getExecutionDate(),
        canonicalPayment.getReference());
  }

  /** Create pain.001 audit log entry */
  private void createPain001AuditLog(
      Pain001MessageEntity pain001Message,
      Pain001AuditLogEntity.Action action,
      String description,
      String correlationId,
      TenantContext tenantContext) {

    Pain001AuditLogEntity auditLog =
        Pain001AuditLogEntity.builder()
            .pain001Message(pain001Message)
            .action(action)
            .description(description)
            .tenantId(tenantContext.getTenantId())
            .businessUnitId(tenantContext.getBusinessUnitId())
            .additionalData(String.format("{\"correlationId\": \"%s\"}", correlationId))
            .build();

    // Save audit log to database for compliance
    pain001AuditLogRepository.save(auditLog);
    log.debug("Created and saved pain.001 audit log: {} - {}", action, description);
  }

  /** Create pain.002 audit log entry */
  private void createPain002AuditLog(
      Pain002StatusReportEntity statusReport,
      Pain002AuditLogEntity.Action action,
      String description,
      String correlationId,
      TenantContext tenantContext) {

    Pain002AuditLogEntity auditLog =
        Pain002AuditLogEntity.builder()
            .statusReport(statusReport)
            .action(action)
            .description(description)
            .tenantId(tenantContext.getTenantId())
            .businessUnitId(tenantContext.getBusinessUnitId())
            .additionalData(String.format("{\"correlationId\": \"%s\"}", correlationId))
            .build();

    // Save audit log to database for compliance
    pain002AuditLogRepository.save(auditLog);
    log.debug("Created and saved pain.002 audit log: {} - {}", action, description);
  }

  // ==================== SAGA STATE MANAGEMENT METHODS ====================

  /** Get saga state by saga ID */
  public SagaState getSagaState(String sagaId) {
    // TODO: Implement saga state retrieval from database
    log.debug("Getting saga state for saga: {}", sagaId);
    return SagaState.builder()
        .sagaId(sagaId)
        .status(SagaState.SagaStatus.ACTIVE)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  /** Mark saga step as completed */
  public void markSagaStepCompleted(String sagaId, String stepName, Object stepData) {
    log.debug("Marking saga step as completed: {} - {}", sagaId, stepName);
    // TODO: Implement saga step completion tracking
  }

  /** Mark saga step as failed */
  public void markSagaStepFailed(String sagaId, String stepName, String errorMessage) {
    log.debug("Marking saga step as failed: {} - {} - {}", sagaId, stepName, errorMessage);
    // TODO: Implement saga step failure tracking
  }

  /** Mark saga step as compensated */
  public void markSagaStepCompensated(String sagaId, String stepName) {
    log.debug("Marking saga step as compensated: {} - {}", sagaId, stepName);
    // TODO: Implement saga step compensation tracking
  }

  /** Mark saga step compensation as failed */
  public void markSagaStepCompensationFailed(String sagaId, String stepName, String errorMessage) {
    log.debug(
        "Marking saga step compensation as failed: {} - {} - {}", sagaId, stepName, errorMessage);
    // TODO: Implement saga step compensation failure tracking
  }

  /** Mark saga as completed */
  public void markSagaCompleted(String sagaId) {
    log.debug("Marking saga as completed: {}", sagaId);
    // TODO: Implement saga completion tracking
  }

  /** Mark saga as compensated */
  public void markSagaCompensated(String sagaId, String reason) {
    log.debug("Marking saga as compensated: {} - {}", sagaId, reason);
    // TODO: Implement saga compensation tracking
  }

  /** Mark saga compensation as failed */
  public void markSagaCompensationFailed(String sagaId, String errorMessage) {
    log.debug("Marking saga compensation as failed: {} - {}", sagaId, errorMessage);
    // TODO: Implement saga compensation failure tracking
  }

  /** Get canonical payment by saga ID */
  public CanonicalPaymentModel getCanonicalPaymentBySagaId(String sagaId) {
    log.debug("Getting canonical payment by saga ID: {}", sagaId);
    // TODO: Implement canonical payment retrieval by saga ID
    return null; // Placeholder
  }

  /** Save pain.002 response */
  public void savePain002Response(String sagaId, String pain002Xml, String paymentStatus) {
    log.debug("Saving pain.002 response for saga: {} - {}", sagaId, paymentStatus);
    // TODO: Implement pain.002 response saving
  }

  /** Save pain.002 status report (simplified version) */
  public void savePain002StatusReport(String sagaId, String paymentStatus, String statusReason) {
    log.debug(
        "Saving pain.002 status report for saga: {} - {} - {}",
        sagaId,
        paymentStatus,
        statusReason);
    // TODO: Implement pain.002 status report saving
  }

  /** Delete pain.001 data for compensation */
  public void deletePain001Data(String sagaId) {
    log.debug("Deleting pain.001 data for saga: {}", sagaId);
    // TODO: Implement pain.001 data deletion for compensation
  }
}
