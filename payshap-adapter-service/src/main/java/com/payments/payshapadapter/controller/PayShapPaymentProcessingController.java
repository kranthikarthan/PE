package com.payments.payshapadapter.controller;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.payshapadapter.domain.PayShapPaymentMessage;
import com.payments.payshapadapter.domain.PayShapTransactionLog;
import com.payments.payshapadapter.service.PayShapPaymentProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for PayShap payment processing */
@RestController
@RequestMapping("/api/v1/payshap/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PayShap Payment Processing", description = "PayShap payment processing operations")
public class PayShapPaymentProcessingController {

  private final PayShapPaymentProcessingService payShapPaymentProcessingService;

  /** Process PayShap payment with proxy registry integration */
  @PostMapping("/process")
  @Operation(
      summary = "Process PayShap payment",
      description = "Process instant P2P payment with proxy registry integration")
  public ResponseEntity<PayShapPaymentMessage> processPayShapPayment(
      @Valid @RequestBody ProcessPayShapPaymentRequest request) {
    log.info("Processing PayShap payment: {}", request.getTransactionId());

    PayShapPaymentMessage paymentMessage =
        payShapPaymentProcessingService.processPayShapPayment(
            ClearingAdapterId.of(request.getAdapterId()),
            request.getTransactionId(),
            request.getMessageType(),
            request.getDirection(),
            request.getMessageId(),
            request.getInstructionId(),
            request.getEndToEndId(),
            request.getTransactionType(),
            request.getAmount(),
            request.getCurrency(),
            request.getDebtorName(),
            request.getDebtorAccount(),
            request.getDebtorBankCode(),
            request.getDebtorMobile(),
            request.getDebtorEmail(),
            request.getCreditorName(),
            request.getCreditorAccount(),
            request.getCreditorBankCode(),
            request.getCreditorMobile(),
            request.getCreditorEmail(),
            request.getPaymentPurpose(),
            request.getReference(),
            request.getProxyType(),
            request.getProxyValue());

    return ResponseEntity.ok(paymentMessage);
  }

  /** Submit payment for processing */
  @PostMapping("/{messageId}/submit")
  @Operation(
      summary = "Submit PayShap payment",
      description = "Submit PayShap payment for processing")
  public ResponseEntity<PayShapPaymentMessage> submitPayShapPayment(
      @PathVariable String messageId) {
    log.info("Submitting PayShap payment: {}", messageId);

    PayShapPaymentMessage paymentMessage =
        payShapPaymentProcessingService.submitPayment(ClearingMessageId.of(messageId));

    return ResponseEntity.ok(paymentMessage);
  }

  /** Process payment response */
  @PostMapping("/{messageId}/response")
  @Operation(
      summary = "Process PayShap payment response",
      description = "Process PayShap payment response")
  public ResponseEntity<PayShapPaymentMessage> processPayShapPaymentResponse(
      @PathVariable String messageId,
      @Valid @RequestBody ProcessPayShapPaymentResponseRequest request) {
    log.info("Processing PayShap payment response: {}", messageId);

    PayShapPaymentMessage paymentMessage =
        payShapPaymentProcessingService.processPaymentResponse(
            ClearingMessageId.of(messageId), request.getResponseCode(),
            request.getResponseMessage(), request.isSuccess());

    return ResponseEntity.ok(paymentMessage);
  }

  /** Get PayShap payment message by ID */
  @GetMapping("/{messageId}")
  @Operation(
      summary = "Get PayShap payment message",
      description = "Get PayShap payment message by ID")
  public ResponseEntity<PayShapPaymentMessage> getPayShapPaymentMessage(
      @PathVariable String messageId) {
    Optional<PayShapPaymentMessage> paymentMessage =
        payShapPaymentProcessingService.getPaymentMessage(ClearingMessageId.of(messageId));

    return paymentMessage.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  /** Get PayShap payment messages by adapter */
  @GetMapping("/adapter/{adapterId}")
  @Operation(
      summary = "Get PayShap payment messages by adapter",
      description = "Get PayShap payment messages for a specific adapter")
  public ResponseEntity<List<PayShapPaymentMessage>> getPayShapPaymentMessagesByAdapter(
      @PathVariable String adapterId) {
    List<PayShapPaymentMessage> messages =
        payShapPaymentProcessingService.getPaymentMessagesByAdapter(
            ClearingAdapterId.of(adapterId));

    return ResponseEntity.ok(messages);
  }

  /** Get PayShap payment message by transaction ID */
  @GetMapping("/transaction/{transactionId}")
  @Operation(
      summary = "Get PayShap payment message by transaction ID",
      description = "Get PayShap payment message by transaction ID")
  public ResponseEntity<PayShapPaymentMessage> getPayShapPaymentMessageByTransactionId(
      @PathVariable String transactionId) {
    PayShapPaymentMessage paymentMessage =
        payShapPaymentProcessingService.getPaymentMessageByTransactionId(transactionId);

    if (paymentMessage == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(paymentMessage);
  }

  /** Get PayShap payment messages by status */
  @GetMapping("/status/{status}")
  @Operation(
      summary = "Get PayShap payment messages by status",
      description = "Get PayShap payment messages by status")
  public ResponseEntity<List<PayShapPaymentMessage>> getPayShapPaymentMessagesByStatus(
      @PathVariable String status) {
    List<PayShapPaymentMessage> messages =
        payShapPaymentProcessingService.getPaymentMessagesByStatus(status);
    return ResponseEntity.ok(messages);
  }

  /** Get PayShap payment messages by adapter and status */
  @GetMapping("/adapter/{adapterId}/status/{status}")
  @Operation(
      summary = "Get PayShap payment messages by adapter and status",
      description = "Get PayShap payment messages by adapter and status")
  public ResponseEntity<List<PayShapPaymentMessage>> getPayShapPaymentMessagesByAdapterAndStatus(
      @PathVariable String adapterId, @PathVariable String status) {
    List<PayShapPaymentMessage> messages =
        payShapPaymentProcessingService.getPaymentMessagesByAdapterAndStatus(
            ClearingAdapterId.of(adapterId), status);

    return ResponseEntity.ok(messages);
  }

  /** Get PayShap transaction logs by adapter */
  @GetMapping("/adapter/{adapterId}/logs")
  @Operation(
      summary = "Get PayShap transaction logs by adapter",
      description = "Get PayShap transaction logs for a specific adapter")
  public ResponseEntity<List<PayShapTransactionLog>> getPayShapTransactionLogsByAdapter(
      @PathVariable String adapterId) {
    List<PayShapTransactionLog> logs =
        payShapPaymentProcessingService.getTransactionLogsByAdapter(
            ClearingAdapterId.of(adapterId));

    return ResponseEntity.ok(logs);
  }

  /** Get PayShap transaction logs by transaction ID */
  @GetMapping("/transaction/{transactionId}/logs")
  @Operation(
      summary = "Get PayShap transaction logs by transaction ID",
      description = "Get PayShap transaction logs by transaction ID")
  public ResponseEntity<List<PayShapTransactionLog>> getPayShapTransactionLogsByTransactionId(
      @PathVariable String transactionId) {
    List<PayShapTransactionLog> logs =
        payShapPaymentProcessingService.getTransactionLogsByTransactionId(transactionId);
    return ResponseEntity.ok(logs);
  }

  // DTOs
  @lombok.Data
  public static class ProcessPayShapPaymentRequest {
    private String adapterId;
    private String transactionId;
    private String messageType;
    private String direction;
    private String messageId;
    private String instructionId;
    private String endToEndId;
    private String transactionType;
    private BigDecimal amount;
    private String currency;
    private String debtorName;
    private String debtorAccount;
    private String debtorBankCode;
    private String debtorMobile;
    private String debtorEmail;
    private String creditorName;
    private String creditorAccount;
    private String creditorBankCode;
    private String creditorMobile;
    private String creditorEmail;
    private String paymentPurpose;
    private String reference;
    private String proxyType;
    private String proxyValue;
  }

  @lombok.Data
  public static class ProcessPayShapPaymentResponseRequest {
    private String responseCode;
    private String responseMessage;
    private boolean success;
  }
}
