package com.payments.bankservafricaadapter.controller;

import com.payments.bankservafricaadapter.domain.BankservAfricaEftMessage;
import com.payments.bankservafricaadapter.service.BankservAfricaEftProcessingService;
import com.payments.domain.shared.ClearingAdapterId;
import com.payments.domain.shared.ClearingMessageId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for BankservAfrica EFT processing */
@Slf4j
@RestController
@RequestMapping("/api/v1/bankservafrica-eft")
@RequiredArgsConstructor
@Tag(name = "BankservAfrica EFT", description = "BankservAfrica EFT batch processing")
public class BankservAfricaEftProcessingController {

  private final BankservAfricaEftProcessingService eftProcessingService;

  /** Process EFT batch */
  @PostMapping("/process")
  @Operation(summary = "Process EFT batch", description = "Process EFT batch for BankservAfrica")
  public ResponseEntity<BankservAfricaEftMessage> processEftBatch(
      @Valid @RequestBody ProcessEftBatchRequest request) {

    log.info("Processing EFT batch: {}", request.getBatchId());

    BankservAfricaEftMessage message =
        eftProcessingService.processEftBatch(
            ClearingAdapterId.of(request.getAdapterId()),
            request.getBatchId(),
            request.getMessageType(),
            request.getDirection(),
            request.getPayload());

    return ResponseEntity.status(HttpStatus.CREATED).body(message);
  }

  /** Update EFT message status */
  @PutMapping("/{messageId}/status")
  @Operation(summary = "Update EFT message status", description = "Update EFT message status")
  public ResponseEntity<BankservAfricaEftMessage> updateEftMessageStatus(
      @PathVariable @Parameter(description = "Message ID") String messageId,
      @Valid @RequestBody UpdateEftMessageStatusRequest request) {

    log.info("Updating EFT message status: {}", messageId);

    BankservAfricaEftMessage message =
        eftProcessingService.updateEftMessageStatus(
            ClearingMessageId.of(messageId),
            request.getStatus(),
            request.getStatusCode(),
            request.getErrorCode(),
            request.getErrorMessage());

    return ResponseEntity.ok(message);
  }

  /** Get EFT message by ID */
  @GetMapping("/{messageId}")
  @Operation(summary = "Get EFT message by ID", description = "Get EFT message by ID")
  public ResponseEntity<BankservAfricaEftMessage> getEftMessage(
      @PathVariable @Parameter(description = "Message ID") String messageId) {

    log.info("Getting EFT message: {}", messageId);

    Optional<BankservAfricaEftMessage> message =
        eftProcessingService.getEftMessage(ClearingMessageId.of(messageId));

    return message.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  /** Get EFT messages by batch ID */
  @GetMapping("/batch/{batchId}")
  @Operation(summary = "Get EFT messages by batch ID", description = "Get EFT messages by batch ID")
  public ResponseEntity<List<BankservAfricaEftMessage>> getEftMessagesByBatchId(
      @PathVariable @Parameter(description = "Batch ID") String batchId) {

    log.info("Getting EFT messages for batch: {}", batchId);

    List<BankservAfricaEftMessage> messages = eftProcessingService.getEftMessagesByBatchId(batchId);

    return ResponseEntity.ok(messages);
  }

  /** Get EFT messages by adapter ID */
  @GetMapping("/adapter/{adapterId}")
  @Operation(
      summary = "Get EFT messages by adapter ID",
      description = "Get EFT messages by adapter ID")
  public ResponseEntity<List<BankservAfricaEftMessage>> getEftMessagesByAdapterId(
      @PathVariable @Parameter(description = "Adapter ID") String adapterId) {

    log.info("Getting EFT messages for adapter: {}", adapterId);

    List<BankservAfricaEftMessage> messages =
        eftProcessingService.getEftMessagesByAdapterId(ClearingAdapterId.of(adapterId));

    return ResponseEntity.ok(messages);
  }

  /** Get EFT messages by status */
  @GetMapping("/status/{status}")
  @Operation(summary = "Get EFT messages by status", description = "Get EFT messages by status")
  public ResponseEntity<List<BankservAfricaEftMessage>> getEftMessagesByStatus(
      @PathVariable @Parameter(description = "Status") String status) {

    log.info("Getting EFT messages for status: {}", status);

    List<BankservAfricaEftMessage> messages = eftProcessingService.getEftMessagesByStatus(status);

    return ResponseEntity.ok(messages);
  }

  /** Get EFT messages by direction */
  @GetMapping("/direction/{direction}")
  @Operation(
      summary = "Get EFT messages by direction",
      description = "Get EFT messages by direction")
  public ResponseEntity<List<BankservAfricaEftMessage>> getEftMessagesByDirection(
      @PathVariable @Parameter(description = "Direction") String direction) {

    log.info("Getting EFT messages for direction: {}", direction);

    List<BankservAfricaEftMessage> messages =
        eftProcessingService.getEftMessagesByDirection(direction);

    return ResponseEntity.ok(messages);
  }

  /** Get EFT messages by message type */
  @GetMapping("/message-type/{messageType}")
  @Operation(
      summary = "Get EFT messages by message type",
      description = "Get EFT messages by message type")
  public ResponseEntity<List<BankservAfricaEftMessage>> getEftMessagesByMessageType(
      @PathVariable @Parameter(description = "Message Type") String messageType) {

    log.info("Getting EFT messages for message type: {}", messageType);

    List<BankservAfricaEftMessage> messages =
        eftProcessingService.getEftMessagesByMessageType(messageType);

    return ResponseEntity.ok(messages);
  }

  /** Get EFT messages created after timestamp */
  @GetMapping("/created-after/{timestamp}")
  @Operation(
      summary = "Get EFT messages created after timestamp",
      description = "Get EFT messages created after timestamp")
  public ResponseEntity<List<BankservAfricaEftMessage>> getEftMessagesCreatedAfter(
      @PathVariable @Parameter(description = "Timestamp") String timestamp) {

    log.info("Getting EFT messages created after: {}", timestamp);

    List<BankservAfricaEftMessage> messages =
        eftProcessingService.getEftMessagesCreatedAfter(Instant.parse(timestamp));

    return ResponseEntity.ok(messages);
  }

  /** Get EFT messages by batch ID and direction */
  @GetMapping("/batch/{batchId}/direction/{direction}")
  @Operation(
      summary = "Get EFT messages by batch ID and direction",
      description = "Get EFT messages by batch ID and direction")
  public ResponseEntity<List<BankservAfricaEftMessage>> getEftMessagesByBatchIdAndDirection(
      @PathVariable @Parameter(description = "Batch ID") String batchId,
      @PathVariable @Parameter(description = "Direction") String direction) {

    log.info("Getting EFT messages for batch: {} and direction: {}", batchId, direction);

    List<BankservAfricaEftMessage> messages =
        eftProcessingService.getEftMessagesByBatchIdAndDirection(batchId, direction);

    return ResponseEntity.ok(messages);
  }

  /** Count EFT messages by batch ID */
  @GetMapping("/batch/{batchId}/count")
  @Operation(
      summary = "Count EFT messages by batch ID",
      description = "Count EFT messages by batch ID")
  public ResponseEntity<Long> countEftMessagesByBatchId(
      @PathVariable @Parameter(description = "Batch ID") String batchId) {

    log.info("Counting EFT messages for batch: {}", batchId);

    long count = eftProcessingService.countEftMessagesByBatchId(batchId);

    return ResponseEntity.ok(count);
  }

  /** Count EFT messages by batch ID and status */
  @GetMapping("/batch/{batchId}/status/{status}/count")
  @Operation(
      summary = "Count EFT messages by batch ID and status",
      description = "Count EFT messages by batch ID and status")
  public ResponseEntity<Long> countEftMessagesByBatchIdAndStatus(
      @PathVariable @Parameter(description = "Batch ID") String batchId,
      @PathVariable @Parameter(description = "Status") String status) {

    log.info("Counting EFT messages for batch: {} and status: {}", batchId, status);

    long count = eftProcessingService.countEftMessagesByBatchIdAndStatus(batchId, status);

    return ResponseEntity.ok(count);
  }

  // Request DTOs
  public static class ProcessEftBatchRequest {
    private String adapterId;
    private String batchId;
    private String messageType;
    private String direction;
    private String payload;

    // Getters and setters
    public String getAdapterId() {
      return adapterId;
    }

    public void setAdapterId(String adapterId) {
      this.adapterId = adapterId;
    }

    public String getBatchId() {
      return batchId;
    }

    public void setBatchId(String batchId) {
      this.batchId = batchId;
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

    public String getPayload() {
      return payload;
    }

    public void setPayload(String payload) {
      this.payload = payload;
    }
  }

  public static class UpdateEftMessageStatusRequest {
    private String status;
    private Integer statusCode;
    private String errorCode;
    private String errorMessage;

    // Getters and setters
    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public Integer getStatusCode() {
      return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
      this.statusCode = statusCode;
    }

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
}
