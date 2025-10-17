package com.payments.samosadapter.controller;

import com.payments.domain.shared.TenantContext;
import com.payments.samosadapter.domain.SamosPaymentMessage;
import com.payments.samosadapter.service.SamosPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SAMOS Payment REST Controller
 *
 * <p>REST API for managing SAMOS payment messages and ISO 20022 processing. Provides endpoints for
 * payment submission, status tracking, and message management.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SAMOS Payment", description = "SAMOS payment message management API")
public class SamosPaymentController {

  private final SamosPaymentService samosPaymentService;

  /** Submit payment to SAMOS */
  @PostMapping("/submit")
  @Operation(
      summary = "Submit payment to SAMOS",
      description = "Submit payment to SAMOS for processing")
  public ResponseEntity<SamosPaymentMessage> submitPayment(
      @Valid @RequestBody SubmitSamosPaymentRequest request,
      @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-Id") UUID tenantId,
      @Parameter(description = "Business Unit ID")
          @RequestHeader(value = "X-Business-Unit-Id", required = false)
          UUID businessUnitId) {

    log.info("Submitting payment to SAMOS: {} for tenant: {}", request.getPaymentId(), tenantId);

    TenantContext tenantContext =
        TenantContext.builder()
            .tenantId(tenantId != null ? tenantId.toString() : null)
            .businessUnitId(businessUnitId != null ? businessUnitId.toString() : null)
            .build();

    SamosPaymentMessage message =
        samosPaymentService.submitPayment(
            tenantContext,
            request.getPaymentId(),
            request.getMessageId(),
            request.getMessageType(),
            request.getIso20022Payload());

    return ResponseEntity.status(HttpStatus.CREATED).body(message);
  }

  /** Process incoming SAMOS message */
  @PostMapping("/incoming")
  @Operation(
      summary = "Process incoming SAMOS message",
      description = "Process incoming message from SAMOS")
  public ResponseEntity<SamosPaymentMessage> processIncomingMessage(
      @Valid @RequestBody ProcessIncomingSamosMessageRequest request,
      @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-Id") UUID tenantId,
      @Parameter(description = "Business Unit ID")
          @RequestHeader(value = "X-Business-Unit-Id", required = false)
          UUID businessUnitId) {

    log.info(
        "Processing incoming SAMOS message: {} for payment: {}",
        request.getMessageId(),
        request.getPaymentId());

    TenantContext tenantContext =
        TenantContext.builder()
            .tenantId(tenantId != null ? tenantId.toString() : null)
            .businessUnitId(businessUnitId != null ? businessUnitId.toString() : null)
            .build();

    SamosPaymentMessage message =
        samosPaymentService.processIncomingMessage(
            tenantContext,
            request.getPaymentId(),
            request.getMessageId(),
            request.getMessageType(),
            request.getIso20022Payload());

    return ResponseEntity.status(HttpStatus.CREATED).body(message);
  }

  /** Mark message as sent */
  @PostMapping("/{messageId}/sent")
  @Operation(summary = "Mark message as sent", description = "Mark SAMOS message as sent")
  public ResponseEntity<SamosPaymentMessage> markMessageAsSent(
      @Parameter(description = "Message ID") @PathVariable String messageId) {

    log.info("Marking SAMOS message as sent: {}", messageId);

    SamosPaymentMessage message = samosPaymentService.markMessageAsSent(messageId);
    return ResponseEntity.ok(message);
  }

  /** Mark message as received */
  @PostMapping("/{messageId}/received")
  @Operation(
      summary = "Mark message as received",
      description = "Mark SAMOS message as received with response")
  public ResponseEntity<SamosPaymentMessage> markMessageAsReceived(
      @Parameter(description = "Message ID") @PathVariable String messageId,
      @Valid @RequestBody MarkMessageReceivedRequest request) {

    log.info(
        "Marking SAMOS message as received: {} with response code: {}",
        messageId,
        request.getResponseCode());

    SamosPaymentMessage message =
        samosPaymentService.markMessageAsReceived(
            messageId, request.getResponseCode(), request.getResponseMessage());

    return ResponseEntity.ok(message);
  }

  /** Mark message as failed */
  @PostMapping("/{messageId}/failed")
  @Operation(summary = "Mark message as failed", description = "Mark SAMOS message as failed")
  public ResponseEntity<SamosPaymentMessage> markMessageAsFailed(
      @Parameter(description = "Message ID") @PathVariable String messageId,
      @Valid @RequestBody MarkMessageFailedRequest request) {

    log.info(
        "Marking SAMOS message as failed: {} with error code: {}",
        messageId,
        request.getErrorCode());

    SamosPaymentMessage message =
        samosPaymentService.markMessageAsFailed(
            messageId, request.getErrorCode(), request.getErrorMessage());

    return ResponseEntity.ok(message);
  }

  /** Get payment messages by payment ID */
  @GetMapping("/by-payment/{paymentId}")
  @Operation(
      summary = "Get payment messages by payment ID",
      description = "Get all messages for a specific payment")
  public ResponseEntity<List<SamosPaymentMessage>> getPaymentMessages(
      @Parameter(description = "Payment ID") @PathVariable String paymentId,
      @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-Id") UUID tenantId) {

    log.info("Getting SAMOS payment messages for payment: {} and tenant: {}", paymentId, tenantId);

    List<SamosPaymentMessage> messages =
        samosPaymentService.getPaymentMessages(tenantId, paymentId);
    return ResponseEntity.ok(messages);
  }

  /** Get payment message by message ID */
  @GetMapping("/{messageId}")
  @Operation(
      summary = "Get payment message by message ID",
      description = "Get SAMOS payment message by message ID")
  public ResponseEntity<SamosPaymentMessage> getPaymentMessage(
      @Parameter(description = "Message ID") @PathVariable String messageId) {

    log.info("Getting SAMOS payment message: {}", messageId);

    return samosPaymentService
        .getPaymentMessage(messageId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** Get pending outbound messages */
  @GetMapping("/pending")
  @Operation(
      summary = "Get pending outbound messages",
      description = "Get pending outbound messages for processing")
  public ResponseEntity<List<SamosPaymentMessage>> getPendingOutboundMessages(
      @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-Id") UUID tenantId) {

    log.info("Getting pending SAMOS outbound messages for tenant: {}", tenantId);

    List<SamosPaymentMessage> messages = samosPaymentService.getPendingOutboundMessages(tenantId);
    return ResponseEntity.ok(messages);
  }

  /** Get failed outbound messages */
  @GetMapping("/failed")
  @Operation(
      summary = "Get failed outbound messages",
      description = "Get failed outbound messages for retry")
  public ResponseEntity<List<SamosPaymentMessage>> getFailedOutboundMessages(
      @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-Id") UUID tenantId) {

    log.info("Getting failed SAMOS outbound messages for tenant: {}", tenantId);

    List<SamosPaymentMessage> messages = samosPaymentService.getFailedOutboundMessages(tenantId);
    return ResponseEntity.ok(messages);
  }

  /** Get payment messages with pagination */
  @GetMapping
  @Operation(
      summary = "Get payment messages with pagination",
      description = "Get payment messages with pagination")
  public ResponseEntity<Page<SamosPaymentMessage>> getPaymentMessages(
      @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-Id") UUID tenantId,
      Pageable pageable) {

    log.info("Getting SAMOS payment messages with pagination for tenant: {}", tenantId);

    Page<SamosPaymentMessage> messages = samosPaymentService.getPaymentMessages(tenantId, pageable);
    return ResponseEntity.ok(messages);
  }

  /** Get payment messages by status */
  @GetMapping("/by-status/{status}")
  @Operation(
      summary = "Get payment messages by status",
      description = "Get payment messages by status")
  public ResponseEntity<List<SamosPaymentMessage>> getPaymentMessagesByStatus(
      @Parameter(description = "Message status") @PathVariable
          SamosPaymentMessage.MessageStatus status,
      @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-Id") UUID tenantId) {

    log.info("Getting SAMOS payment messages by status: {} for tenant: {}", status, tenantId);

    List<SamosPaymentMessage> messages =
        samosPaymentService.getPaymentMessagesByStatus(tenantId, status);
    return ResponseEntity.ok(messages);
  }

  /** Get payment messages by message type */
  @GetMapping("/by-type/{messageType}")
  @Operation(
      summary = "Get payment messages by message type",
      description = "Get payment messages by message type")
  public ResponseEntity<List<SamosPaymentMessage>> getPaymentMessagesByType(
      @Parameter(description = "Message type") @PathVariable
          SamosPaymentMessage.MessageType messageType,
      @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-Id") UUID tenantId) {

    log.info("Getting SAMOS payment messages by type: {} for tenant: {}", messageType, tenantId);

    List<SamosPaymentMessage> messages =
        samosPaymentService.getPaymentMessagesByType(tenantId, messageType);
    return ResponseEntity.ok(messages);
  }

  /** Get payment messages by date range */
  @GetMapping("/by-date-range")
  @Operation(
      summary = "Get payment messages by date range",
      description = "Get payment messages by date range")
  public ResponseEntity<List<SamosPaymentMessage>> getPaymentMessagesByDateRange(
      @Parameter(description = "Start date") @RequestParam Instant startDate,
      @Parameter(description = "End date") @RequestParam Instant endDate,
      @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-Id") UUID tenantId) {

    log.info(
        "Getting SAMOS payment messages by date range: {} to {} for tenant: {}",
        startDate,
        endDate,
        tenantId);

    List<SamosPaymentMessage> messages =
        samosPaymentService.getPaymentMessagesByDateRange(tenantId, startDate, endDate);
    return ResponseEntity.ok(messages);
  }

  /** Get message count by status */
  @GetMapping("/count/{status}")
  @Operation(summary = "Get message count by status", description = "Get message count by status")
  public ResponseEntity<MessageCountResponse> getMessageCountByStatus(
      @Parameter(description = "Message status") @PathVariable
          SamosPaymentMessage.MessageStatus status,
      @Parameter(description = "Tenant ID") @RequestHeader("X-Tenant-Id") UUID tenantId) {

    log.info("Getting SAMOS payment message count by status: {} for tenant: {}", status, tenantId);

    long count = samosPaymentService.getMessageCountByStatus(tenantId, status);
    MessageCountResponse response = new MessageCountResponse(tenantId, status, count);

    return ResponseEntity.ok(response);
  }

  /** Validate ISO 20022 message */
  @PostMapping("/validate")
  @Operation(
      summary = "Validate ISO 20022 message",
      description = "Validate ISO 20022 message format")
  public ResponseEntity<ValidationResponse> validateIso20022Message(
      @Valid @RequestBody ValidateIso20022MessageRequest request) {

    log.info("Validating ISO 20022 message of type: {}", request.getMessageType());

    boolean isValid =
        samosPaymentService.validateIso20022Message(
            request.getIso20022Payload(), request.getMessageType());

    ValidationResponse response = new ValidationResponse(request.getMessageId(), isValid);
    return ResponseEntity.ok(response);
  }

  // Request/Response DTOs
  public static class SubmitSamosPaymentRequest {
    private String paymentId;
    private String messageId;
    private SamosPaymentMessage.MessageType messageType;
    private String iso20022Payload;

    // Getters and setters
    public String getPaymentId() {
      return paymentId;
    }

    public void setPaymentId(String paymentId) {
      this.paymentId = paymentId;
    }

    public String getMessageId() {
      return messageId;
    }

    public void setMessageId(String messageId) {
      this.messageId = messageId;
    }

    public SamosPaymentMessage.MessageType getMessageType() {
      return messageType;
    }

    public void setMessageType(SamosPaymentMessage.MessageType messageType) {
      this.messageType = messageType;
    }

    public String getIso20022Payload() {
      return iso20022Payload;
    }

    public void setIso20022Payload(String iso20022Payload) {
      this.iso20022Payload = iso20022Payload;
    }
  }

  public static class ProcessIncomingSamosMessageRequest {
    private String paymentId;
    private String messageId;
    private SamosPaymentMessage.MessageType messageType;
    private String iso20022Payload;

    // Getters and setters
    public String getPaymentId() {
      return paymentId;
    }

    public void setPaymentId(String paymentId) {
      this.paymentId = paymentId;
    }

    public String getMessageId() {
      return messageId;
    }

    public void setMessageId(String messageId) {
      this.messageId = messageId;
    }

    public SamosPaymentMessage.MessageType getMessageType() {
      return messageType;
    }

    public void setMessageType(SamosPaymentMessage.MessageType messageType) {
      this.messageType = messageType;
    }

    public String getIso20022Payload() {
      return iso20022Payload;
    }

    public void setIso20022Payload(String iso20022Payload) {
      this.iso20022Payload = iso20022Payload;
    }
  }

  public static class MarkMessageReceivedRequest {
    private String responseCode;
    private String responseMessage;

    public String getResponseCode() {
      return responseCode;
    }

    public void setResponseCode(String responseCode) {
      this.responseCode = responseCode;
    }

    public String getResponseMessage() {
      return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
      this.responseMessage = responseMessage;
    }
  }

  public static class MarkMessageFailedRequest {
    private String errorCode;
    private String errorMessage;

    public String getErrorCode() {
      return errorCode;
    }

    public void setErrorCode(String errorCode) {
      this.errorCode = errorCode;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
    }
  }

  public static class ValidateIso20022MessageRequest {
    private String messageId;
    private SamosPaymentMessage.MessageType messageType;
    private String iso20022Payload;

    public String getMessageId() {
      return messageId;
    }

    public void setMessageId(String messageId) {
      this.messageId = messageId;
    }

    public SamosPaymentMessage.MessageType getMessageType() {
      return messageType;
    }

    public void setMessageType(SamosPaymentMessage.MessageType messageType) {
      this.messageType = messageType;
    }

    public String getIso20022Payload() {
      return iso20022Payload;
    }

    public void setIso20022Payload(String iso20022Payload) {
      this.iso20022Payload = iso20022Payload;
    }
  }

  public static class MessageCountResponse {
    private UUID tenantId;
    private SamosPaymentMessage.MessageStatus status;
    private long count;

    public MessageCountResponse(
        UUID tenantId, SamosPaymentMessage.MessageStatus status, long count) {
      this.tenantId = tenantId;
      this.status = status;
      this.count = count;
    }

    public UUID getTenantId() {
      return tenantId;
    }

    public SamosPaymentMessage.MessageStatus getStatus() {
      return status;
    }

    public long getCount() {
      return count;
    }
  }

  public static class ValidationResponse {
    private String messageId;
    private boolean valid;

    public ValidationResponse(String messageId, boolean valid) {
      this.messageId = messageId;
      this.valid = valid;
    }

    public String getMessageId() {
      return messageId;
    }

    public boolean isValid() {
      return valid;
    }
  }
}
