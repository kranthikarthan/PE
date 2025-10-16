package com.payments.transactionprocessing.api;

import com.payments.domain.shared.*;
import com.payments.domain.transaction.*;
import com.payments.transactionprocessing.dto.*;
import com.payments.transactionprocessing.service.LedgerService;
import com.payments.transactionprocessing.service.TransactionEventService;
import com.payments.transactionprocessing.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Transaction Management",
    description = "API for managing financial transactions and ledger entries")
public class TransactionController {

  private final TransactionService transactionService;
  private final LedgerService ledgerService;
  private final TransactionEventService eventService;

  @PostMapping
  @Operation(
      summary = "Create a new transaction",
      description = "Creates a new financial transaction with double-entry bookkeeping")
  public ResponseEntity<TransactionResponse> createTransaction(
      @Valid @RequestBody CreateTransactionRequest request) {

    log.info(
        "Creating transaction for payment {} with amount {}",
        request.getPaymentId(),
        request.getAmount());

    TransactionId transactionId = TransactionId.generate();
    TenantContext tenantContext =
        TenantContext.of(
            request.getTenantId(),
            "Default Tenant",
            request.getBusinessUnitId(),
            "Default Business Unit");

    Transaction transaction =
        transactionService.createTransaction(
            transactionId,
            tenantContext,
            PaymentId.of(request.getPaymentId()),
            AccountNumber.of(request.getDebitAccount()),
            AccountNumber.of(request.getCreditAccount()),
            Money.of(request.getAmount(), Currency.getInstance(request.getCurrency())),
            request.getTransactionType());

    TransactionResponse response = TransactionResponse.fromDomain(transaction);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{transactionId}")
  @Operation(
      summary = "Get transaction by ID",
      description = "Retrieves a specific transaction by its ID")
  public ResponseEntity<TransactionResponse> getTransaction(
      @PathVariable String transactionId,
      @RequestParam String tenantId,
      @RequestParam String businessUnitId) {

    log.info("Getting transaction {} for tenant {}", transactionId, tenantId);

    TenantContext tenantContext =
        TenantContext.of(tenantId, "Default Tenant", businessUnitId, "Default Business Unit");
    Optional<Transaction> transaction =
        transactionService.findById(TransactionId.of(transactionId), tenantContext);

    if (transaction.isPresent()) {
      TransactionResponse response = TransactionResponse.fromDomain(transaction.get());
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/{transactionId}/start-processing")
  @Operation(
      summary = "Start processing transaction",
      description = "Marks a transaction as processing")
  public ResponseEntity<TransactionResponse> startProcessing(
      @PathVariable String transactionId,
      @RequestParam String tenantId,
      @RequestParam String businessUnitId) {

    log.info("Starting processing for transaction {}", transactionId);

    TenantContext tenantContext =
        TenantContext.of(tenantId, "Default Tenant", businessUnitId, "Default Business Unit");
    Transaction transaction =
        transactionService.startProcessing(TransactionId.of(transactionId), tenantContext);

    TransactionResponse response = TransactionResponse.fromDomain(transaction);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{transactionId}/mark-cleared")
  @Operation(
      summary = "Mark transaction as cleared",
      description = "Marks a transaction as cleared with clearing system details")
  public ResponseEntity<TransactionResponse> markCleared(
      @PathVariable String transactionId,
      @RequestParam String tenantId,
      @RequestParam String businessUnitId,
      @Valid @RequestBody MarkClearedRequest request) {

    log.info(
        "Marking transaction {} as cleared with system {}",
        transactionId,
        request.getClearingSystem());

    TenantContext tenantContext =
        TenantContext.of(tenantId, "Default Tenant", businessUnitId, "Default Business Unit");
    Transaction transaction =
        transactionService.markCleared(
            TransactionId.of(transactionId),
            tenantContext,
            request.getClearingSystem(),
            request.getClearingReference());

    TransactionResponse response = TransactionResponse.fromDomain(transaction);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{transactionId}/complete")
  @Operation(summary = "Complete transaction", description = "Marks a transaction as completed")
  public ResponseEntity<TransactionResponse> completeTransaction(
      @PathVariable String transactionId,
      @RequestParam String tenantId,
      @RequestParam String businessUnitId) {

    log.info("Completing transaction {}", transactionId);

    TenantContext tenantContext =
        TenantContext.of(tenantId, "Default Tenant", businessUnitId, "Default Business Unit");
    Transaction transaction =
        transactionService.completeTransaction(TransactionId.of(transactionId), tenantContext);

    TransactionResponse response = TransactionResponse.fromDomain(transaction);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{transactionId}/fail")
  @Operation(
      summary = "Fail transaction",
      description = "Marks a transaction as failed with reason")
  public ResponseEntity<TransactionResponse> failTransaction(
      @PathVariable String transactionId,
      @RequestParam String tenantId,
      @RequestParam String businessUnitId,
      @Valid @RequestBody FailTransactionRequest request) {

    log.info("Failing transaction {} with reason: {}", transactionId, request.getReason());

    TenantContext tenantContext =
        TenantContext.of(tenantId, "Default Tenant", businessUnitId, "Default Business Unit");
    Transaction transaction =
        transactionService.failTransaction(
            TransactionId.of(transactionId), tenantContext, request.getReason());

    TransactionResponse response = TransactionResponse.fromDomain(transaction);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(
      summary = "Get transactions",
      description = "Retrieves transactions based on various criteria")
  public ResponseEntity<List<TransactionResponse>> getTransactions(
      @RequestParam String tenantId,
      @RequestParam String businessUnitId,
      @RequestParam(required = false) String paymentId,
      @RequestParam(required = false) TransactionStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant endDate) {

    log.info("Getting transactions for tenant {} with filters", tenantId);

    TenantContext tenantContext =
        TenantContext.of(tenantId, "Default Tenant", businessUnitId, "Default Business Unit");
    List<Transaction> transactions;

    if (paymentId != null) {
      transactions = transactionService.findByPaymentId(PaymentId.of(paymentId), tenantContext);
    } else if (status != null) {
      transactions = transactionService.findByStatus(status, tenantContext);
    } else if (startDate != null && endDate != null) {
      transactions = transactionService.findByDateRange(tenantContext, startDate, endDate);
    } else {
      // Return empty list for now - would need a general query method
      transactions = List.of();
    }

    List<TransactionResponse> responses =
        transactions.stream().map(TransactionResponse::fromDomain).toList();

    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{transactionId}/ledger-entries")
  @Operation(
      summary = "Get transaction ledger entries",
      description = "Retrieves all ledger entries for a transaction")
  public ResponseEntity<List<LedgerEntryResponse>> getTransactionLedgerEntries(
      @PathVariable String transactionId) {

    log.info("Getting ledger entries for transaction {}", transactionId);

    List<LedgerEntry> entries =
        ledgerService.getTransactionEntries(TransactionId.of(transactionId));
    List<LedgerEntryResponse> responses =
        entries.stream().map(LedgerEntryResponse::fromDomain).toList();

    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{transactionId}/events")
  @Operation(
      summary = "Get transaction events",
      description = "Retrieves all events for a transaction")
  public ResponseEntity<List<TransactionEventResponse>> getTransactionEvents(
      @PathVariable String transactionId) {

    log.info("Getting events for transaction {}", transactionId);

    List<TransactionEvent> events =
        eventService.getTransactionEvents(TransactionId.of(transactionId));
    List<TransactionEventResponse> responses =
        events.stream().map(TransactionEventResponse::fromDomain).toList();

    return ResponseEntity.ok(responses);
  }
}
