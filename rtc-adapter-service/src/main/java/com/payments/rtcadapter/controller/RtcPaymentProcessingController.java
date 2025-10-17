package com.payments.rtcadapter.controller;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import com.payments.rtcadapter.domain.RtcPaymentMessage;
import com.payments.rtcadapter.domain.RtcTransactionLog;
import com.payments.rtcadapter.service.RtcPaymentProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for RTC payment processing */
@RestController
@RequestMapping("/api/v1/rtc/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "RTC Payment Processing", description = "RTC payment processing endpoints")
public class RtcPaymentProcessingController {

  private final RtcPaymentProcessingService rtcPaymentProcessingService;

  /** Process RTC payment */
  @PostMapping("/process")
  @Operation(summary = "Process RTC payment", description = "Process a new RTC payment")
  public ResponseEntity<RtcPaymentMessage> processRtcPayment(
      @Valid @RequestBody ProcessRtcPaymentRequest request) {

    log.info("Processing RTC payment: {}", request.getTransactionId());

    RtcPaymentMessage message =
        rtcPaymentProcessingService.processRtcPayment(
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
            request.getCreditorName(),
            request.getCreditorAccount(),
            request.getCreditorBankCode(),
            request.getPaymentPurpose(),
            request.getReference());

    return ResponseEntity.ok(message);
  }

  /** Submit payment to RTC system */
  @PostMapping("/{messageId}/submit")
  @Operation(
      summary = "Submit payment to RTC",
      description = "Submit payment to RTC clearing system")
  public ResponseEntity<RtcPaymentMessage> submitPayment(
      @PathVariable @Parameter(description = "Message ID") String messageId) {

    log.info("Submitting RTC payment: {}", messageId);

    RtcPaymentMessage message =
        rtcPaymentProcessingService.submitPayment(ClearingMessageId.of(messageId));

    return ResponseEntity.ok(message);
  }

  /** Process payment response */
  @PostMapping("/{messageId}/response")
  @Operation(
      summary = "Process payment response",
      description = "Process payment response from RTC system")
  public ResponseEntity<RtcPaymentMessage> processPaymentResponse(
      @PathVariable @Parameter(description = "Message ID") String messageId,
      @Valid @RequestBody ProcessPaymentResponseRequest request) {

    log.info("Processing RTC payment response: {}", messageId);

    RtcPaymentMessage message =
        rtcPaymentProcessingService.processPaymentResponse(
            ClearingMessageId.of(messageId),
            request.getResponseCode(),
            request.getResponseMessage(),
            request.isSuccess());

    return ResponseEntity.ok(message);
  }

  /** Get payment message by ID */
  @GetMapping("/{messageId}")
  @Operation(summary = "Get payment message", description = "Get payment message by ID")
  public ResponseEntity<RtcPaymentMessage> getPaymentMessage(
      @PathVariable @Parameter(description = "Message ID") String messageId) {

    log.info("Getting RTC payment message: {}", messageId);

    Optional<RtcPaymentMessage> message =
        rtcPaymentProcessingService.getPaymentMessage(ClearingMessageId.of(messageId));

    return message.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  /** Get payment messages by adapter */
  @GetMapping("/adapter/{adapterId}")
  @Operation(
      summary = "Get payment messages by adapter",
      description = "Get payment messages by adapter ID")
  public ResponseEntity<List<RtcPaymentMessage>> getPaymentMessagesByAdapter(
      @PathVariable @Parameter(description = "Adapter ID") String adapterId) {

    log.info("Getting RTC payment messages for adapter: {}", adapterId);

    List<RtcPaymentMessage> messages =
        rtcPaymentProcessingService.getPaymentMessagesByAdapter(ClearingAdapterId.of(adapterId));

    return ResponseEntity.ok(messages);
  }

  /** Get payment message by transaction ID */
  @GetMapping("/transaction/{transactionId}")
  @Operation(
      summary = "Get payment message by transaction ID",
      description = "Get payment message by transaction ID")
  public ResponseEntity<RtcPaymentMessage> getPaymentMessageByTransactionId(
      @PathVariable @Parameter(description = "Transaction ID") String transactionId) {

    log.info("Getting RTC payment message by transaction ID: {}", transactionId);

    RtcPaymentMessage message =
        rtcPaymentProcessingService.getPaymentMessageByTransactionId(transactionId);

    if (message != null) {
      return ResponseEntity.ok(message);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  /** Get payment messages by status */
  @GetMapping("/status/{status}")
  @Operation(
      summary = "Get payment messages by status",
      description = "Get payment messages by status")
  public ResponseEntity<List<RtcPaymentMessage>> getPaymentMessagesByStatus(
      @PathVariable @Parameter(description = "Status") String status) {

    log.info("Getting RTC payment messages by status: {}", status);

    List<RtcPaymentMessage> messages =
        rtcPaymentProcessingService.getPaymentMessagesByStatus(status);
    return ResponseEntity.ok(messages);
  }

  /** Get payment messages by adapter and status */
  @GetMapping("/adapter/{adapterId}/status/{status}")
  @Operation(
      summary = "Get payment messages by adapter and status",
      description = "Get payment messages by adapter and status")
  public ResponseEntity<List<RtcPaymentMessage>> getPaymentMessagesByAdapterAndStatus(
      @PathVariable @Parameter(description = "Adapter ID") String adapterId,
      @PathVariable @Parameter(description = "Status") String status) {

    log.info("Getting RTC payment messages for adapter: {} and status: {}", adapterId, status);

    List<RtcPaymentMessage> messages =
        rtcPaymentProcessingService.getPaymentMessagesByAdapterAndStatus(
            ClearingAdapterId.of(adapterId), status);

    return ResponseEntity.ok(messages);
  }

  /** Get transaction logs by adapter */
  @GetMapping("/adapter/{adapterId}/logs")
  @Operation(
      summary = "Get transaction logs by adapter",
      description = "Get transaction logs by adapter ID")
  public ResponseEntity<List<RtcTransactionLog>> getTransactionLogsByAdapter(
      @PathVariable @Parameter(description = "Adapter ID") String adapterId) {

    log.info("Getting RTC transaction logs for adapter: {}", adapterId);

    List<RtcTransactionLog> logs =
        rtcPaymentProcessingService.getTransactionLogsByAdapter(ClearingAdapterId.of(adapterId));

    return ResponseEntity.ok(logs);
  }

  /** Get transaction logs by transaction ID */
  @GetMapping("/transaction/{transactionId}/logs")
  @Operation(
      summary = "Get transaction logs by transaction ID",
      description = "Get transaction logs by transaction ID")
  public ResponseEntity<List<RtcTransactionLog>> getTransactionLogsByTransactionId(
      @PathVariable @Parameter(description = "Transaction ID") String transactionId) {

    log.info("Getting RTC transaction logs for transaction: {}", transactionId);

    List<RtcTransactionLog> logs =
        rtcPaymentProcessingService.getTransactionLogsByTransactionId(transactionId);
    return ResponseEntity.ok(logs);
  }

  // Request DTOs
  public static class ProcessRtcPaymentRequest {
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
    private String creditorName;
    private String creditorAccount;
    private String creditorBankCode;
    private String paymentPurpose;
    private String reference;

    // Getters and setters
    public String getAdapterId() {
      return adapterId;
    }

    public void setAdapterId(String adapterId) {
      this.adapterId = adapterId;
    }

    public String getTransactionId() {
      return transactionId;
    }

    public void setTransactionId(String transactionId) {
      this.transactionId = transactionId;
    }

    public String getMessageType() {
      return messageType;
    }

    public void setMessageType(String messageType) {
      this.messageType = messageType;
    }

    public String getDirection() {
      return direction;
    }

    public void setDirection(String direction) {
      this.direction = direction;
    }

    public String getMessageId() {
      return messageId;
    }

    public void setMessageId(String messageId) {
      this.messageId = messageId;
    }

    public String getInstructionId() {
      return instructionId;
    }

    public void setInstructionId(String instructionId) {
      this.instructionId = instructionId;
    }

    public String getEndToEndId() {
      return endToEndId;
    }

    public void setEndToEndId(String endToEndId) {
      this.endToEndId = endToEndId;
    }

    public String getTransactionType() {
      return transactionType;
    }

    public void setTransactionType(String transactionType) {
      this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
      return amount;
    }

    public void setAmount(BigDecimal amount) {
      this.amount = amount;
    }

    public String getCurrency() {
      return currency;
    }

    public void setCurrency(String currency) {
      this.currency = currency;
    }

    public String getDebtorName() {
      return debtorName;
    }

    public void setDebtorName(String debtorName) {
      this.debtorName = debtorName;
    }

    public String getDebtorAccount() {
      return debtorAccount;
    }

    public void setDebtorAccount(String debtorAccount) {
      this.debtorAccount = debtorAccount;
    }

    public String getDebtorBankCode() {
      return debtorBankCode;
    }

    public void setDebtorBankCode(String debtorBankCode) {
      this.debtorBankCode = debtorBankCode;
    }

    public String getCreditorName() {
      return creditorName;
    }

    public void setCreditorName(String creditorName) {
      this.creditorName = creditorName;
    }

    public String getCreditorAccount() {
      return creditorAccount;
    }

    public void setCreditorAccount(String creditorAccount) {
      this.creditorAccount = creditorAccount;
    }

    public String getCreditorBankCode() {
      return creditorBankCode;
    }

    public void setCreditorBankCode(String creditorBankCode) {
      this.creditorBankCode = creditorBankCode;
    }

    public String getPaymentPurpose() {
      return paymentPurpose;
    }

    public void setPaymentPurpose(String paymentPurpose) {
      this.paymentPurpose = paymentPurpose;
    }

    public String getReference() {
      return reference;
    }

    public void setReference(String reference) {
      this.reference = reference;
    }
  }

  public static class ProcessPaymentResponseRequest {
    private String responseCode;
    private String responseMessage;
    private boolean success;

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

    public boolean isSuccess() {
      return success;
    }

    public void setSuccess(boolean success) {
      this.success = success;
    }
  }
}
