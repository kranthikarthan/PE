package com.payments.swiftadapter.controller;

import com.payments.domain.shared.ClearingAdapterId;
import com.payments.swiftadapter.domain.SwiftPaymentMessage;
import com.payments.swiftadapter.service.SwiftIso20022Service;
import com.payments.swiftadapter.service.SwiftPaymentProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for SWIFT payment processing operations */
@RestController
@RequestMapping("/api/v1/swift-payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SWIFT Payment Processing", description = "SWIFT payment processing operations")
public class SwiftPaymentProcessingController {

  private final SwiftPaymentProcessingService swiftPaymentProcessingService;
  private final SwiftIso20022Service swiftIso20022Service;

  @PostMapping("/process")
  @Operation(
      summary = "Process SWIFT payment",
      description = "Process a SWIFT payment with sanctions screening and FX conversion")
  public ResponseEntity<SwiftPaymentMessage> processSwiftPayment(
      @Valid @RequestBody ProcessSwiftPaymentRequest request) {
    log.info("Processing SWIFT payment: {}", request.getTransactionId());

    SwiftPaymentMessage paymentMessage =
        swiftPaymentProcessingService.processSwiftPayment(
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
            request.getDebtorBankName(),
            request.getDebtorBankCountry(),
            request.getDebtorBankSwiftCode(),
            request.getCreditorName(),
            request.getCreditorAccount(),
            request.getCreditorBankCode(),
            request.getCreditorBankName(),
            request.getCreditorBankCountry(),
            request.getCreditorBankSwiftCode(),
            request.getPaymentPurpose(),
            request.getReference(),
            request.getCorrespondentBankCode(),
            request.getCorrespondentBankName(),
            request.getCorrespondentBankSwiftCode(),
            request.getIntermediaryBankCode(),
            request.getIntermediaryBankName(),
            request.getIntermediaryBankSwiftCode(),
            request.getChargesBearer());

    return ResponseEntity.status(HttpStatus.CREATED).body(paymentMessage);
  }

  @PostMapping("/pacs008")
  @Operation(
      summary = "Process pacs.008 message",
      description = "Process ISO 20022 pacs.008 Customer Credit Transfer message")
  public ResponseEntity<SwiftPaymentMessage> processPacs008Message(
      @Valid @RequestBody ProcessPacs008Request request) {
    log.info("Processing pacs.008 message: {}", request.getMessageId());

    SwiftPaymentMessage paymentMessage =
        swiftIso20022Service.processPacs008Message(
            ClearingAdapterId.of(request.getAdapterId()),
            request.getMessageId(),
            request.getInstructionId(),
            request.getEndToEndId(),
            request.getTransactionId(),
            request.getAmount(),
            request.getCurrency(),
            request.getDebtorName(),
            request.getDebtorAccount(),
            request.getDebtorBankSwiftCode(),
            request.getCreditorName(),
            request.getCreditorAccount(),
            request.getCreditorBankSwiftCode(),
            request.getPaymentPurpose(),
            request.getReference());

    return ResponseEntity.status(HttpStatus.CREATED).body(paymentMessage);
  }

  @PostMapping("/pacs002")
  @Operation(
      summary = "Process pacs.002 message",
      description = "Process ISO 20022 pacs.002 Payment Status Report message")
  public ResponseEntity<SwiftPaymentMessage> processPacs002Message(
      @Valid @RequestBody ProcessPacs002Request request) {
    log.info("Processing pacs.002 message: {}", request.getMessageId());

    SwiftPaymentMessage paymentMessage =
        swiftIso20022Service.processPacs002Message(
            ClearingAdapterId.of(request.getAdapterId()),
            request.getMessageId(),
            request.getInstructionId(),
            request.getEndToEndId(),
            request.getTransactionId(),
            request.getStatus(),
            request.getResponseCode(),
            request.getResponseMessage());

    return ResponseEntity.ok(paymentMessage);
  }

  @PostMapping("/mt103")
  @Operation(
      summary = "Process MT103 message",
      description = "Process MT103 Customer Transfer message")
  public ResponseEntity<SwiftPaymentMessage> processMt103Message(
      @Valid @RequestBody ProcessMt103Request request) {
    log.info("Processing MT103 message: {}", request.getMessageId());

    SwiftPaymentMessage paymentMessage =
        swiftIso20022Service.processMt103Message(
            ClearingAdapterId.of(request.getAdapterId()),
            request.getMessageId(),
            request.getTransactionId(),
            request.getAmount(),
            request.getCurrency(),
            request.getDebtorName(),
            request.getDebtorAccount(),
            request.getDebtorBankSwiftCode(),
            request.getCreditorName(),
            request.getCreditorAccount(),
            request.getCreditorBankSwiftCode(),
            request.getPaymentPurpose(),
            request.getReference());

    return ResponseEntity.status(HttpStatus.CREATED).body(paymentMessage);
  }

  @GetMapping("/transaction/{transactionId}")
  @Operation(
      summary = "Get payment by transaction ID",
      description = "Get SWIFT payment message by transaction ID")
  public ResponseEntity<SwiftPaymentMessage> getPaymentByTransactionId(
      @PathVariable String transactionId) {
    log.info("Getting SWIFT payment by transaction ID: {}", transactionId);

    Optional<SwiftPaymentMessage> paymentMessage =
        swiftPaymentProcessingService.getPaymentMessageByTransactionId(transactionId);
    return paymentMessage.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/message/{messageId}")
  @Operation(
      summary = "Get payment by message ID",
      description = "Get SWIFT payment message by message ID")
  public ResponseEntity<SwiftPaymentMessage> getPaymentByMessageId(@PathVariable String messageId) {
    log.info("Getting SWIFT payment by message ID: {}", messageId);

    Optional<SwiftPaymentMessage> paymentMessage =
        swiftPaymentProcessingService.getPaymentMessageByMessageId(messageId);
    return paymentMessage.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/adapter/{adapterId}")
  @Operation(
      summary = "Get payments by adapter",
      description = "Get all SWIFT payment messages for an adapter")
  public ResponseEntity<List<SwiftPaymentMessage>> getPaymentsByAdapter(
      @PathVariable String adapterId) {
    log.info("Getting SWIFT payments for adapter: {}", adapterId);

    List<SwiftPaymentMessage> paymentMessages =
        swiftPaymentProcessingService.getPaymentMessagesByAdapterId(adapterId);
    return ResponseEntity.ok(paymentMessages);
  }

  @GetMapping("/status/{status}")
  @Operation(
      summary = "Get payments by status",
      description = "Get SWIFT payment messages by status")
  public ResponseEntity<List<SwiftPaymentMessage>> getPaymentsByStatus(
      @PathVariable String status) {
    log.info("Getting SWIFT payments by status: {}", status);

    List<SwiftPaymentMessage> paymentMessages =
        swiftPaymentProcessingService.getPaymentMessagesByStatus(status);
    return ResponseEntity.ok(paymentMessages);
  }

  @GetMapping("/currency/{currency}")
  @Operation(
      summary = "Get payments by currency",
      description = "Get SWIFT payment messages by currency")
  public ResponseEntity<List<SwiftPaymentMessage>> getPaymentsByCurrency(
      @PathVariable String currency) {
    log.info("Getting SWIFT payments by currency: {}", currency);

    List<SwiftPaymentMessage> paymentMessages =
        swiftPaymentProcessingService.getPaymentMessagesByCurrency(currency);
    return ResponseEntity.ok(paymentMessages);
  }

  // DTOs
  public static class ProcessSwiftPaymentRequest {
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
    private String debtorBankName;
    private String debtorBankCountry;
    private String debtorBankSwiftCode;
    private String creditorName;
    private String creditorAccount;
    private String creditorBankCode;
    private String creditorBankName;
    private String creditorBankCountry;
    private String creditorBankSwiftCode;
    private String paymentPurpose;
    private String reference;
    private String correspondentBankCode;
    private String correspondentBankName;
    private String correspondentBankSwiftCode;
    private String intermediaryBankCode;
    private String intermediaryBankName;
    private String intermediaryBankSwiftCode;
    private String chargesBearer;

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

    public String getDebtorBankName() {
      return debtorBankName;
    }

    public void setDebtorBankName(String debtorBankName) {
      this.debtorBankName = debtorBankName;
    }

    public String getDebtorBankCountry() {
      return debtorBankCountry;
    }

    public void setDebtorBankCountry(String debtorBankCountry) {
      this.debtorBankCountry = debtorBankCountry;
    }

    public String getDebtorBankSwiftCode() {
      return debtorBankSwiftCode;
    }

    public void setDebtorBankSwiftCode(String debtorBankSwiftCode) {
      this.debtorBankSwiftCode = debtorBankSwiftCode;
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

    public String getCreditorBankName() {
      return creditorBankName;
    }

    public void setCreditorBankName(String creditorBankName) {
      this.creditorBankName = creditorBankName;
    }

    public String getCreditorBankCountry() {
      return creditorBankCountry;
    }

    public void setCreditorBankCountry(String creditorBankCountry) {
      this.creditorBankCountry = creditorBankCountry;
    }

    public String getCreditorBankSwiftCode() {
      return creditorBankSwiftCode;
    }

    public void setCreditorBankSwiftCode(String creditorBankSwiftCode) {
      this.creditorBankSwiftCode = creditorBankSwiftCode;
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

    public String getCorrespondentBankCode() {
      return correspondentBankCode;
    }

    public void setCorrespondentBankCode(String correspondentBankCode) {
      this.correspondentBankCode = correspondentBankCode;
    }

    public String getCorrespondentBankName() {
      return correspondentBankName;
    }

    public void setCorrespondentBankName(String correspondentBankName) {
      this.correspondentBankName = correspondentBankName;
    }

    public String getCorrespondentBankSwiftCode() {
      return correspondentBankSwiftCode;
    }

    public void setCorrespondentBankSwiftCode(String correspondentBankSwiftCode) {
      this.correspondentBankSwiftCode = correspondentBankSwiftCode;
    }

    public String getIntermediaryBankCode() {
      return intermediaryBankCode;
    }

    public void setIntermediaryBankCode(String intermediaryBankCode) {
      this.intermediaryBankCode = intermediaryBankCode;
    }

    public String getIntermediaryBankName() {
      return intermediaryBankName;
    }

    public void setIntermediaryBankName(String intermediaryBankName) {
      this.intermediaryBankName = intermediaryBankName;
    }

    public String getIntermediaryBankSwiftCode() {
      return intermediaryBankSwiftCode;
    }

    public void setIntermediaryBankSwiftCode(String intermediaryBankSwiftCode) {
      this.intermediaryBankSwiftCode = intermediaryBankSwiftCode;
    }

    public String getChargesBearer() {
      return chargesBearer;
    }

    public void setChargesBearer(String chargesBearer) {
      this.chargesBearer = chargesBearer;
    }
  }

  public static class ProcessPacs008Request {
    private String adapterId;
    private String messageId;
    private String instructionId;
    private String endToEndId;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private String debtorName;
    private String debtorAccount;
    private String debtorBankSwiftCode;
    private String creditorName;
    private String creditorAccount;
    private String creditorBankSwiftCode;
    private String paymentPurpose;
    private String reference;

    // Getters and setters
    public String getAdapterId() {
      return adapterId;
    }

    public void setAdapterId(String adapterId) {
      this.adapterId = adapterId;
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

    public String getTransactionId() {
      return transactionId;
    }

    public void setTransactionId(String transactionId) {
      this.transactionId = transactionId;
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

    public String getDebtorBankSwiftCode() {
      return debtorBankSwiftCode;
    }

    public void setDebtorBankSwiftCode(String debtorBankSwiftCode) {
      this.debtorBankSwiftCode = debtorBankSwiftCode;
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

    public String getCreditorBankSwiftCode() {
      return creditorBankSwiftCode;
    }

    public void setCreditorBankSwiftCode(String creditorBankSwiftCode) {
      this.creditorBankSwiftCode = creditorBankSwiftCode;
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

  public static class ProcessPacs002Request {
    private String adapterId;
    private String messageId;
    private String instructionId;
    private String endToEndId;
    private String transactionId;
    private String status;
    private String responseCode;
    private String responseMessage;

    // Getters and setters
    public String getAdapterId() {
      return adapterId;
    }

    public void setAdapterId(String adapterId) {
      this.adapterId = adapterId;
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

    public String getTransactionId() {
      return transactionId;
    }

    public void setTransactionId(String transactionId) {
      this.transactionId = transactionId;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

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

  public static class ProcessMt103Request {
    private String adapterId;
    private String messageId;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private String debtorName;
    private String debtorAccount;
    private String debtorBankSwiftCode;
    private String creditorName;
    private String creditorAccount;
    private String creditorBankSwiftCode;
    private String paymentPurpose;
    private String reference;

    // Getters and setters
    public String getAdapterId() {
      return adapterId;
    }

    public void setAdapterId(String adapterId) {
      this.adapterId = adapterId;
    }

    public String getMessageId() {
      return messageId;
    }

    public void setMessageId(String messageId) {
      this.messageId = messageId;
    }

    public String getTransactionId() {
      return transactionId;
    }

    public void setTransactionId(String transactionId) {
      this.transactionId = transactionId;
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

    public String getDebtorBankSwiftCode() {
      return debtorBankSwiftCode;
    }

    public void setDebtorBankSwiftCode(String debtorBankSwiftCode) {
      this.debtorBankSwiftCode = debtorBankSwiftCode;
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

    public String getCreditorBankSwiftCode() {
      return creditorBankSwiftCode;
    }

    public void setCreditorBankSwiftCode(String creditorBankSwiftCode) {
      this.creditorBankSwiftCode = creditorBankSwiftCode;
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
}
