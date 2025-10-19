package com.payments.samosadapter.controller;

import com.payments.samosadapter.dto.SamosSettlementAccountBalanceResponse;
import com.payments.samosadapter.dto.SamosSettlementAccountCreateRequest;
import com.payments.samosadapter.dto.SamosSettlementAccountResponse;
import com.payments.samosadapter.service.SamosSettlementAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for SAMOS Settlement Account Management */
@RestController
@RequestMapping("/api/v1/samos/settlement-accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SAMOS Settlement Accounts", description = "SARB settlement account management APIs")
public class SamosSettlementAccountController {

  private final SamosSettlementAccountService accountService;

  @Operation(
      summary = "Create settlement account",
      description = "Create a new SARB settlement account")
  @PostMapping
  public ResponseEntity<SamosSettlementAccountResponse> createAccount(
      @Valid @RequestBody SamosSettlementAccountCreateRequest request) {
    log.info(
        "POST /api/v1/samos/settlement-accounts - Create account for tenant: {}",
        request.getTenantId());
    SamosSettlementAccountResponse response = accountService.createAccount(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "Get settlement account", description = "Get settlement account by ID")
  @GetMapping("/{accountId}")
  public ResponseEntity<SamosSettlementAccountResponse> getAccount(
      @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable String accountId) {
    log.info("GET /api/v1/samos/settlement-accounts/{} - tenant: {}", accountId, tenantId);
    SamosSettlementAccountResponse response = accountService.getAccount(tenantId, accountId);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get settlement account by number",
      description = "Get settlement account by SARB account number")
  @GetMapping("/by-number/{accountNumber}")
  public ResponseEntity<SamosSettlementAccountResponse> getAccountByNumber(
      @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable String accountNumber) {
    log.info(
        "GET /api/v1/samos/settlement-accounts/by-number/{} - tenant: {}", accountNumber, tenantId);
    SamosSettlementAccountResponse response =
        accountService.getAccountByNumber(tenantId, accountNumber);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get all settlement accounts",
      description = "Get all settlement accounts for a tenant")
  @GetMapping
  public ResponseEntity<List<SamosSettlementAccountResponse>> getAllAccounts(
      @RequestHeader("X-Tenant-ID") String tenantId) {
    log.info("GET /api/v1/samos/settlement-accounts - tenant: {}", tenantId);
    List<SamosSettlementAccountResponse> response = accountService.getAllAccounts(tenantId);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get active settlement accounts",
      description = "Get all active settlement accounts for a tenant")
  @GetMapping("/active")
  public ResponseEntity<List<SamosSettlementAccountResponse>> getActiveAccounts(
      @RequestHeader("X-Tenant-ID") String tenantId) {
    log.info("GET /api/v1/samos/settlement-accounts/active - tenant: {}", tenantId);
    List<SamosSettlementAccountResponse> response = accountService.getActiveAccounts(tenantId);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get account balance",
      description = "Get detailed balance information for an account")
  @GetMapping("/{accountId}/balance")
  public ResponseEntity<SamosSettlementAccountBalanceResponse> getAccountBalance(
      @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable String accountId) {
    log.info("GET /api/v1/samos/settlement-accounts/{}/balance - tenant: {}", accountId, tenantId);
    SamosSettlementAccountBalanceResponse response =
        accountService.getAccountBalance(tenantId, accountId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Credit account", description = "Credit funds to settlement account")
  @PostMapping("/{accountId}/credit")
  public ResponseEntity<SamosSettlementAccountResponse> creditAccount(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String accountId,
      @RequestParam BigDecimal amount,
      @RequestParam String updatedBy) {
    log.info(
        "POST /api/v1/samos/settlement-accounts/{}/credit - amount: {} ZAR", accountId, amount);
    SamosSettlementAccountResponse response =
        accountService.creditAccount(tenantId, accountId, amount, updatedBy);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Debit account", description = "Debit funds from settlement account")
  @PostMapping("/{accountId}/debit")
  public ResponseEntity<SamosSettlementAccountResponse> debitAccount(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String accountId,
      @RequestParam BigDecimal amount,
      @RequestParam String updatedBy) {
    log.info("POST /api/v1/samos/settlement-accounts/{}/debit - amount: {} ZAR", accountId, amount);
    SamosSettlementAccountResponse response =
        accountService.debitAccount(tenantId, accountId, amount, updatedBy);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Reserve funds", description = "Reserve funds for pending settlement")
  @PostMapping("/{accountId}/reserve")
  public ResponseEntity<SamosSettlementAccountResponse> reserveFunds(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String accountId,
      @RequestParam BigDecimal amount,
      @RequestParam String updatedBy) {
    log.info(
        "POST /api/v1/samos/settlement-accounts/{}/reserve - amount: {} ZAR", accountId, amount);
    SamosSettlementAccountResponse response =
        accountService.reserveFunds(tenantId, accountId, amount, updatedBy);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Release reservation", description = "Release reserved funds")
  @PostMapping("/{accountId}/release-reservation")
  public ResponseEntity<SamosSettlementAccountResponse> releaseReservation(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String accountId,
      @RequestParam BigDecimal amount,
      @RequestParam String updatedBy) {
    log.info(
        "POST /api/v1/samos/settlement-accounts/{}/release-reservation - amount: {} ZAR",
        accountId,
        amount);
    SamosSettlementAccountResponse response =
        accountService.releaseReservation(tenantId, accountId, amount, updatedBy);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Settle reserved funds",
      description = "Settle reserved funds (complete payment)")
  @PostMapping("/{accountId}/settle")
  public ResponseEntity<SamosSettlementAccountResponse> settleReservedFunds(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String accountId,
      @RequestParam BigDecimal amount,
      @RequestParam String updatedBy) {
    log.info(
        "POST /api/v1/samos/settlement-accounts/{}/settle - amount: {} ZAR", accountId, amount);
    SamosSettlementAccountResponse response =
        accountService.settleReservedFunds(tenantId, accountId, amount, updatedBy);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Pledge collateral",
      description = "Pledge collateral for settlement account")
  @PostMapping("/{accountId}/pledge-collateral")
  public ResponseEntity<SamosSettlementAccountResponse> pledgeCollateral(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String accountId,
      @RequestParam BigDecimal amount,
      @RequestParam String updatedBy) {
    log.info(
        "POST /api/v1/samos/settlement-accounts/{}/pledge-collateral - amount: {} ZAR",
        accountId,
        amount);
    SamosSettlementAccountResponse response =
        accountService.pledgeCollateral(tenantId, accountId, amount, updatedBy);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Release collateral",
      description = "Release collateral from settlement account")
  @PostMapping("/{accountId}/release-collateral")
  public ResponseEntity<SamosSettlementAccountResponse> releaseCollateral(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String accountId,
      @RequestParam BigDecimal amount,
      @RequestParam String updatedBy) {
    log.info(
        "POST /api/v1/samos/settlement-accounts/{}/release-collateral - amount: {} ZAR",
        accountId,
        amount);
    SamosSettlementAccountResponse response =
        accountService.releaseCollateral(tenantId, accountId, amount, updatedBy);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Activate account", description = "Activate a suspended settlement account")
  @PostMapping("/{accountId}/activate")
  public ResponseEntity<SamosSettlementAccountResponse> activateAccount(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String accountId,
      @RequestParam String updatedBy) {
    log.info("POST /api/v1/samos/settlement-accounts/{}/activate", accountId);
    SamosSettlementAccountResponse response =
        accountService.activateAccount(tenantId, accountId, updatedBy);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Suspend account", description = "Suspend a settlement account")
  @PostMapping("/{accountId}/suspend")
  public ResponseEntity<SamosSettlementAccountResponse> suspendAccount(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String accountId,
      @RequestParam String updatedBy) {
    log.info("POST /api/v1/samos/settlement-accounts/{}/suspend", accountId);
    SamosSettlementAccountResponse response =
        accountService.suspendAccount(tenantId, accountId, updatedBy);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Close account", description = "Close a settlement account")
  @PostMapping("/{accountId}/close")
  public ResponseEntity<SamosSettlementAccountResponse> closeAccount(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String accountId,
      @RequestParam String updatedBy) {
    log.info("POST /api/v1/samos/settlement-accounts/{}/close", accountId);
    SamosSettlementAccountResponse response =
        accountService.closeAccount(tenantId, accountId, updatedBy);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Reset daily counters",
      description = "Reset daily settlement counters (start of business day)")
  @PostMapping("/reset-daily-counters")
  public ResponseEntity<Void> resetDailyCounters(@RequestHeader("X-Tenant-ID") String tenantId) {
    log.info("POST /api/v1/samos/settlement-accounts/reset-daily-counters - tenant: {}", tenantId);
    accountService.resetDailyCounters(tenantId);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Get accounts with insufficient collateral",
      description = "Get accounts that don't meet collateral requirements")
  @GetMapping("/insufficient-collateral")
  public ResponseEntity<List<SamosSettlementAccountResponse>> getAccountsWithInsufficientCollateral(
      @RequestHeader("X-Tenant-ID") String tenantId) {
    log.info(
        "GET /api/v1/samos/settlement-accounts/insufficient-collateral - tenant: {}", tenantId);
    List<SamosSettlementAccountResponse> response =
        accountService.getAccountsWithInsufficientCollateral(tenantId);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get accounts with low balance",
      description = "Get accounts with balance below threshold")
  @GetMapping("/low-balance")
  public ResponseEntity<List<SamosSettlementAccountResponse>> getAccountsWithLowBalance(
      @RequestHeader("X-Tenant-ID") String tenantId, @RequestParam BigDecimal threshold) {
    log.info(
        "GET /api/v1/samos/settlement-accounts/low-balance?threshold={} - tenant: {}",
        threshold,
        tenantId);
    List<SamosSettlementAccountResponse> response =
        accountService.getAccountsWithLowBalance(tenantId, threshold);
    return ResponseEntity.ok(response);
  }
}
