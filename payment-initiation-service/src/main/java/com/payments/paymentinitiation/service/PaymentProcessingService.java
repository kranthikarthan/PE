package com.payments.paymentinitiation.service;

import com.payments.contracts.payment.PaymentInitiationRequest;
import com.payments.contracts.payment.PaymentInitiationResponse;
import com.payments.domain.shared.PaymentId;
import com.payments.domain.shared.TenantContext;
import com.payments.iso20022.canonical.CanonicalPaymentModel;
import com.payments.iso20022.pain001.Pain001MessageParser;
import com.payments.iso20022.pain002.Pain002MessageBuilder;
import com.payments.iso20022.service.Iso20022MarshallerService;
import com.payments.iso20022.util.UetrGenerator;
import com.payments.iso20022.validation.Iso20022Validator;
import com.payments.iso20022.validation.ValidationResult;
import com.payments.paymentinitiation.port.PaymentRepositoryPort;
import com.payments.paymentinitiation.saga.SagaEventPublisher;
import com.payments.routing.engine.RoutingDecision;
import com.payments.routing.engine.RoutingRequest;
import com.payments.routing.service.RoutingService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payment Processing Service for ISO 20022 pain.001/pain.002 flows
 *
 * <p>This service orchestrates the complete pain.001 → canonical model → processing → pain.002 flow
 * while integrating with the existing payment initiation architecture.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessingService {

  private final PaymentInitiationService paymentInitiationService;
  private final Pain001MessageParser pain001MessageParser;
  private final Pain002MessageBuilder pain002MessageBuilder;
  private final PaymentRepositoryPort paymentRepository;
  private final SagaEventPublisher eventPublisher;
  private final Pain001Pain002DatabaseService databaseService;
  private final Iso20022MarshallerService marshallerService;
  private final Iso20022Validator validator;
  private final RoutingService routingService;

  /**
   * Process pain.001 message through complete flow
   *
   * @param pain001Xml pain.001 XML message
   * @param tenantContext Tenant context
   * @param correlationId Correlation ID for tracing
   * @return pain.002 XML response
   */
  @Transactional
  public String processPain001Message(
      String pain001Xml, TenantContext tenantContext, String correlationId) {
    try {
      log.info(
          "Processing pain.001 message for tenant: {}, correlation: {}",
          tenantContext.getTenantId(),
          correlationId);

      // Step 1: Parse pain.001 to canonical payment model
      CanonicalPaymentModel canonicalPayment = parsePain001ToCanonical(pain001Xml, tenantContext);

      // Step 1.5: Save pain.001 message to database
      var pain001Entity =
          databaseService.savePain001Message(
              pain001Xml, canonicalPayment, tenantContext, correlationId);

      // Step 2: Process payment through existing business logic
      CanonicalPaymentModel processedPayment = processPayment(canonicalPayment, correlationId);

      // Step 3: Generate pain.002 status report
      String pain002Xml = generatePain002Response(processedPayment);

      // Step 3.5: Save pain.002 status report to database
      var pain002Entity =
          databaseService.savePain002StatusReport(
              pain002Xml, processedPayment, tenantContext, correlationId);

      // Step 4: Create correlation between pain.001 and pain.002
      databaseService.createCorrelation(pain001Entity, pain002Entity, correlationId, tenantContext);

      log.info("Successfully processed pain.001 message, generated pain.002 response");
      return pain002Xml;

    } catch (Exception e) {
      log.error("Failed to process pain.001 message", e);
      return generateErrorPain002Response(e.getMessage(), correlationId);
    }
  }

  /**
   * Process pain.001 JSON message through complete flow
   *
   * @param pain001Json pain.001 JSON message
   * @param tenantContext Tenant context
   * @param correlationId Correlation ID for tracing
   * @return pain.002 JSON response
   */
  @Transactional
  public String processPain001JsonMessage(
      String pain001Json, TenantContext tenantContext, String correlationId) {
    try {
      log.info(
          "Processing pain.001 JSON message for tenant: {}, correlation: {}",
          tenantContext.getTenantId(),
          correlationId);

      // Step 1: Parse pain.001 to canonical payment model
      CanonicalPaymentModel canonicalPayment =
          parsePain001JsonToCanonical(pain001Json, tenantContext);

      // Step 1.5: Save pain.001 JSON message to database
      var pain001Entity =
          databaseService.savePain001JsonMessage(
              pain001Json, canonicalPayment, tenantContext, correlationId);

      // Step 2: Process payment through existing business logic
      CanonicalPaymentModel processedPayment = processPayment(canonicalPayment, correlationId);

      // Step 3: Generate pain.002 status report
      String pain002Json = generatePain002JsonResponse(processedPayment);

      // Step 3.5: Save pain.002 JSON status report to database
      var pain002Entity =
          databaseService.savePain002JsonStatusReport(
              pain002Json, processedPayment, tenantContext, correlationId);

      // Step 4: Create correlation between pain.001 and pain.002
      databaseService.createCorrelation(pain001Entity, pain002Entity, correlationId, tenantContext);

      log.info("Successfully processed pain.001 JSON message, generated pain.002 response");
      return pain002Json;

    } catch (Exception e) {
      log.error("Failed to process pain.001 JSON message", e);
      return generateErrorPain002JsonResponse(e.getMessage(), correlationId);
    }
  }

  /** Parse pain.001 XML to canonical payment model */
  private CanonicalPaymentModel parsePain001ToCanonical(
      String pain001Xml, TenantContext tenantContext) {
    log.debug("Parsing pain.001 XML to canonical payment model");

    try {
      // Step 1: Validate XML against XSD schema
      ValidationResult validationResult =
          validator.validate(pain001Xml, com.payments.iso20022.config.Iso20022MessageType.PAIN_001);

      if (!validationResult.isValid()) {
        throw new IllegalArgumentException(
            "Invalid pain.001 XML: " + validationResult.getBusinessFriendlyMessage());
      }

      // Step 2: Parse XML to JAXB object using marshaller service
      Object pain001Document =
          marshallerService.unmarshalWithValidation(
              pain001Xml, com.payments.iso20022.config.Iso20022MessageType.PAIN_001);

      // Step 3: Convert JAXB object to canonical payment model using parser
      return pain001MessageParser.parseToCanonicalModel(pain001Document, tenantContext);

    } catch (Exception e) {
      log.error("Failed to parse pain.001 XML", e);
      throw new IllegalArgumentException("Failed to parse pain.001 message: " + e.getMessage(), e);
    }
  }

  /** Parse pain.001 JSON to canonical payment model */
  private CanonicalPaymentModel parsePain001JsonToCanonical(
      String pain001Json, TenantContext tenantContext) {
    log.debug("Parsing pain.001 JSON to canonical payment model");

    try {
      // Step 1: Validate JSON (in production, use JSON schema validation)
      if (pain001Json == null || pain001Json.trim().isEmpty()) {
        throw new IllegalArgumentException("Empty pain.001 JSON message");
      }

      // Step 2: Parse JSON to canonical model using parser
      return pain001MessageParser.parseJsonToCanonicalModel(pain001Json, tenantContext);

    } catch (Exception e) {
      log.error("Failed to parse pain.001 JSON", e);
      throw new IllegalArgumentException(
          "Failed to parse pain.001 JSON message: " + e.getMessage(), e);
    }
  }

  /** Process payment through existing business logic with routing */
  private CanonicalPaymentModel processPayment(
      CanonicalPaymentModel canonicalPayment, String correlationId) {
    log.debug("Processing payment through business logic: {}", canonicalPayment.getPaymentId());

    try {
      // Step 1: Determine clearing system through routing service
      RoutingRequest routingRequest =
          RoutingRequest.builder()
              .paymentId(canonicalPayment.getPaymentId())
              .tenantId(canonicalPayment.getTenantContext().getTenantId())
              .businessUnitId(canonicalPayment.getBusinessUnitId())
              .amount(canonicalPayment.getAmount().getAmount())
              .currency(canonicalPayment.getCurrency().getCurrencyCode())
              .paymentType(canonicalPayment.getPaymentType())
              .sourceAccount(canonicalPayment.getSourceAccount())
              .destinationAccount(canonicalPayment.getDestinationAccount())
              .priority("NORMAL")
              .createdAt(java.time.Instant.now())
              .build();

      RoutingDecision routingDecision = routingService.getRoutingDecision(routingRequest);
      String clearingSystem = routingDecision.getClearingSystem();

      log.info(
          "Payment {} routed to clearing system: {}",
          canonicalPayment.getPaymentId(),
          clearingSystem);

      // Step 2: Update canonical payment with clearing system
      canonicalPayment.setClearingSystem(clearingSystem);

      // Step 3: Convert canonical model to payment initiation request
      PaymentInitiationRequest request = convertCanonicalToPaymentRequest(canonicalPayment);

      // Step 4: Process through existing payment initiation service
      PaymentInitiationResponse response =
          paymentInitiationService.initiatePayment(
              request,
              correlationId,
              canonicalPayment.getTenantContext().getTenantId(),
              canonicalPayment.getTenantContext().getBusinessUnitId());

      // Step 5: Convert response back to canonical model
      CanonicalPaymentModel result = convertPaymentResponseToCanonical(response, canonicalPayment);

      // Step 6: Publish clearing system specific events
      publishClearingSystemEvent(clearingSystem, correlationId, result);

      return result;

    } catch (Exception e) {
      log.error("Failed to process payment: {}", canonicalPayment.getPaymentId(), e);

      // Update canonical payment with error status
      canonicalPayment.setStatus(CanonicalPaymentModel.PaymentStatus.REJECTED);
      canonicalPayment.setStatusReason(e.getMessage());
      canonicalPayment.setStatusReasonCode("PROCESSING_ERROR");
      canonicalPayment.setUpdatedAt(LocalDateTime.now());

      return canonicalPayment;
    }
  }

  /** Generate pain.002 XML response */
  private String generatePain002Response(CanonicalPaymentModel processedPayment) {
    log.debug("Generating pain.002 XML response for payment: {}", processedPayment.getPaymentId());

    try {
      // Use the pain.002 message builder to create proper XML
      return pain002MessageBuilder.build(processedPayment);

    } catch (Exception e) {
      log.error("Failed to generate pain.002 XML response", e);
      throw new RuntimeException("Failed to generate pain.002 response: " + e.getMessage(), e);
    }
  }

  /** Generate pain.002 JSON response */
  private String generatePain002JsonResponse(CanonicalPaymentModel processedPayment) {
    log.debug("Generating pain.002 JSON response for payment: {}", processedPayment.getPaymentId());

    try {
      // Use the pain.002 message builder to create proper JSON
      return pain002MessageBuilder.buildJson(processedPayment);

    } catch (Exception e) {
      log.error("Failed to generate pain.002 JSON response", e);
      throw new RuntimeException("Failed to generate pain.002 JSON response: " + e.getMessage(), e);
    }
  }

  /** Generate error pain.002 XML response */
  private String generateErrorPain002Response(String errorMessage, String correlationId) {
    log.warn("Generating error pain.002 response: {}", errorMessage);

    try {
      // Create error canonical model
      CanonicalPaymentModel errorModel =
          CanonicalPaymentModel.builder()
              .paymentId(UetrGenerator.generate())
              .status(CanonicalPaymentModel.PaymentStatus.REJECTED)
              .statusReason(errorMessage)
              .statusReasonCode("PROCESSING_ERROR")
              .createdAt(LocalDateTime.now())
              .build();

      // Use pain.002 message builder for error response
      return pain002MessageBuilder.build(errorModel);

    } catch (Exception e) {
      log.error("Failed to generate error pain.002 response", e);
      return createFallbackErrorResponse(errorMessage, correlationId);
    }
  }

  /** Generate error pain.002 JSON response */
  private String generateErrorPain002JsonResponse(String errorMessage, String correlationId) {
    log.warn("Generating error pain.002 JSON response: {}", errorMessage);

    try {
      // Create error canonical model
      CanonicalPaymentModel errorModel =
          CanonicalPaymentModel.builder()
              .paymentId(UetrGenerator.generate())
              .status(CanonicalPaymentModel.PaymentStatus.REJECTED)
              .statusReason(errorMessage)
              .statusReasonCode("PROCESSING_ERROR")
              .createdAt(LocalDateTime.now())
              .build();

      // Use pain.002 message builder for error response
      return pain002MessageBuilder.buildJson(errorModel);

    } catch (Exception e) {
      log.error("Failed to generate error pain.002 JSON response", e);
      return createFallbackErrorJsonResponse(errorMessage, correlationId);
    }
  }

  /**
   * Get payment status as pain.002 message
   *
   * @param paymentId Payment ID
   * @param tenantContext Tenant context
   * @param correlationId Correlation ID
   * @return pain.002 XML status report
   */
  public String getPaymentStatusAsPain002(
      String paymentId, TenantContext tenantContext, String correlationId) {
    try {
      log.info(
          "Retrieving payment status as pain.002 for payment: {}, tenant: {}",
          paymentId,
          tenantContext.getTenantId());

      // Query actual payment status from repository
      var payment =
          paymentRepository.findByIdAndTenantId(
              PaymentId.of(paymentId), tenantContext.getTenantId());

      if (payment.isEmpty()) {
        log.warn("Payment not found: {}", paymentId);
        return generateErrorPain002Response("Payment not found", correlationId);
      }

      // Convert payment to canonical model
      CanonicalPaymentModel canonicalPayment =
          convertPaymentToCanonical(payment.get(), tenantContext);

      // Generate pain.002 response
      return generatePain002Response(canonicalPayment);

    } catch (Exception e) {
      log.error("Failed to retrieve payment status as pain.002", e);
      return generateErrorPain002Response(e.getMessage(), correlationId);
    }
  }

  /** Convert payment entity to canonical payment model */
  private CanonicalPaymentModel convertPaymentToCanonical(
      com.payments.domain.entities.Payment payment, TenantContext tenantContext) {
    return CanonicalPaymentModel.builder()
        .paymentId(payment.getId().getValue())
        .status(mapPaymentStatusToCanonical(payment.getStatus()))
        .statusReason("Payment status retrieved from database")
        .amount(payment.getAmount())
        .sourceAccount(payment.getSourceAccount().getValue())
        .destinationAccount(payment.getDestinationAccount().getValue())
        .reference(payment.getReference().getValue())
        .tenantContext(tenantContext)
        .businessUnitId(tenantContext.getBusinessUnitId())
        .createdAt(
            payment.getInitiatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
        .updatedAt(
            payment.getCompletedAt() != null
                ? payment
                    .getCompletedAt()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime()
                : payment
                    .getInitiatedAt()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime())
        .build();
  }

  /** Map payment domain status to canonical status */
  private CanonicalPaymentModel.PaymentStatus mapPaymentStatusToCanonical(
      com.payments.domain.valueobjects.PaymentStatus paymentStatus) {
    // Map domain payment status to canonical status
    switch (paymentStatus) {
      case INITIATED:
        return CanonicalPaymentModel.PaymentStatus.PENDING;
      case VALIDATED:
        return CanonicalPaymentModel.PaymentStatus.ACCEPTED;
      case SUBMITTED_TO_CLEARING:
        return CanonicalPaymentModel.PaymentStatus.PROCESSING;
      case CLEARED:
        return CanonicalPaymentModel.PaymentStatus.COMPLETED;
      case COMPLETED:
        return CanonicalPaymentModel.PaymentStatus.COMPLETED;
      case FAILED:
        return CanonicalPaymentModel.PaymentStatus.FAILED;
      case PENDING:
        return CanonicalPaymentModel.PaymentStatus.PENDING;
      default:
        return CanonicalPaymentModel.PaymentStatus.PENDING;
    }
  }

  /** Convert canonical payment model to payment initiation request */
  private PaymentInitiationRequest convertCanonicalToPaymentRequest(
      CanonicalPaymentModel canonical) {
    return PaymentInitiationRequest.builder()
        .paymentId(PaymentId.of(canonical.getPaymentId()))
        .idempotencyKey(canonical.getCorrelationId())
        .amount(canonical.getAmount())
        .sourceAccount(canonical.getSourceAccount())
        .destinationAccount(canonical.getDestinationAccount())
        .reference(canonical.getReference())
        .paymentType(com.payments.contracts.payment.PaymentType.EFT)
        .priority(com.payments.contracts.payment.Priority.NORMAL)
        .tenantContext(canonical.getTenantContext())
        .initiatedBy("system")
        .build();
  }

  /** Convert payment initiation response to canonical payment model */
  private CanonicalPaymentModel convertPaymentResponseToCanonical(
      PaymentInitiationResponse response, CanonicalPaymentModel original) {
    // Update the original canonical model with response data
    original.setStatus(mapResponseStatusToCanonical(response.getStatus().getCode()));
    original.setStatusReason(response.getErrorMessage());
    original.setProcessingDateTime(LocalDateTime.now());
    original.setUpdatedAt(LocalDateTime.now());
    return original;
  }

  /** Map payment initiation response status to canonical status */
  private CanonicalPaymentModel.PaymentStatus mapResponseStatusToCanonical(String responseStatus) {
    // Map response status to canonical status
    // This depends on the actual response status values
    switch (responseStatus.toUpperCase()) {
      case "SUCCESS":
      case "ACCEPTED":
        return CanonicalPaymentModel.PaymentStatus.ACCEPTED;
      case "PENDING":
        return CanonicalPaymentModel.PaymentStatus.PENDING;
      case "PROCESSING":
        return CanonicalPaymentModel.PaymentStatus.PROCESSING;
      case "FAILED":
      case "REJECTED":
        return CanonicalPaymentModel.PaymentStatus.REJECTED;
      default:
        return CanonicalPaymentModel.PaymentStatus.PENDING;
    }
  }

  /** Create fallback error response when pain.002 builder fails */
  private String createFallbackErrorResponse(String errorMessage, String correlationId) {
    return String.format(
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.002.001.14">
            <PmtStsRpt>
                <GrpHdr>
                    <MsgId>ERROR-%s</MsgId>
                    <CreDtTm>%s</CreDtTm>
                </GrpHdr>
                <TxInfAndSts>
                    <StsId>ERROR-STATUS</StsId>
                    <TxSts>RJCT</TxSts>
                    <StsRsnInf>
                        <Rsn>
                            <Prtry>%s</Prtry>
                        </Rsn>
                    </StsRsnInf>
                </TxInfAndSts>
            </PmtStsRpt>
        </Document>
        """,
        correlationId != null ? correlationId : UetrGenerator.generate(),
        java.time.Instant.now(),
        errorMessage);
  }

  /** Create fallback error JSON response when pain.002 builder fails */
  private String createFallbackErrorJsonResponse(String errorMessage, String correlationId) {
    return String.format(
        """
        {
            "Document": {
                "PmtStsRpt": {
                    "GrpHdr": {
                        "MsgId": "ERROR-%s",
                        "CreDtTm": "%s"
                    },
                    "TxInfAndSts": {
                        "StsId": "ERROR-STATUS",
                        "TxSts": "RJCT",
                        "StsRsnInf": {
                            "Rsn": {
                                "Prtry": "%s"
                            }
                        }
                    }
                }
            }
        }
        """,
        correlationId != null ? correlationId : UetrGenerator.generate(),
        java.time.Instant.now(),
        errorMessage);
  }

  /** Publish clearing system specific events based on routing decision */
  private void publishClearingSystemEvent(
      String clearingSystem, String correlationId, CanonicalPaymentModel canonicalPayment) {
    log.debug("Publishing clearing system event for: {}", clearingSystem);

    try {
      switch (clearingSystem.toUpperCase()) {
        case "PAYSHAP":
          eventPublisher.publishPayShapProcessingInitiated(correlationId, canonicalPayment);
          break;
        case "SAMOS":
        case "BANKSERV":
        case "RTC":
        case "SWIFT":
        default:
          // Use generic payment processing events for other clearing systems
          eventPublisher.publishPaymentProcessingInitiated(correlationId, canonicalPayment);
          break;
      }
    } catch (Exception e) {
      log.error("Failed to publish clearing system event for: {}", clearingSystem, e);
      // Don't throw exception as this is not critical to payment processing
    }
  }
}
