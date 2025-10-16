package com.payments.transactionprocessing.api;

import com.payments.domain.shared.TenantContext;
import com.payments.transactionprocessing.service.BalanceReconciliationService;
import com.payments.transactionprocessing.service.BalanceValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/balances")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Balance Management", description = "Balance validation and reconciliation endpoints")
public class BalanceController {

  private final BalanceValidationService balanceValidationService;
  private final BalanceReconciliationService balanceReconciliationService;

  @GetMapping("/validate/{tenantId}")
  @Operation(
      summary = "Validate system balance",
      description = "Validates that all account balances are consistent")
  public ResponseEntity<Map<String, Object>> validateSystemBalance(
      @Parameter(description = "Tenant ID") @PathVariable String tenantId,
      @Parameter(description = "Business Unit ID") @RequestParam String businessUnitId) {

    log.info("Validating system balance for tenant {}", tenantId);

    TenantContext tenantContext =
        TenantContext.of(tenantId, "Default Tenant", businessUnitId, "Default Business Unit");
    boolean isValid = balanceValidationService.validateAllAccountBalances(tenantContext);

    return ResponseEntity.ok(
        Map.of(
            "tenantId", tenantId,
            "valid", isValid,
            "timestamp", java.time.Instant.now()));
  }

  @GetMapping("/reconcile/{tenantId}")
  @Operation(
      summary = "Reconcile all balances",
      description = "Performs comprehensive balance reconciliation")
  public ResponseEntity<BalanceReconciliationService.ReconciliationResult> reconcileAllBalances(
      @Parameter(description = "Tenant ID") @PathVariable String tenantId,
      @Parameter(description = "Business Unit ID") @RequestParam String businessUnitId) {

    log.info("Reconciling all balances for tenant {}", tenantId);

    TenantContext tenantContext =
        TenantContext.of(tenantId, "Default Tenant", businessUnitId, "Default Business Unit");
    BalanceReconciliationService.ReconciliationResult result =
        balanceReconciliationService.reconcileAllBalances(tenantContext);

    return ResponseEntity.ok(result);
  }

  @GetMapping("/reconcile/{tenantId}/period")
  @Operation(
      summary = "Reconcile balances for period",
      description = "Reconciles balances for a specific date range")
  public ResponseEntity<BalanceReconciliationService.ReconciliationResult>
      reconcileBalancesForPeriod(
          @Parameter(description = "Tenant ID") @PathVariable String tenantId,
          @Parameter(description = "Business Unit ID") @RequestParam String businessUnitId,
          @Parameter(description = "Start date")
              @RequestParam
              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
              LocalDate startDate,
          @Parameter(description = "End date")
              @RequestParam
              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
              LocalDate endDate) {

    log.info("Reconciling balances for tenant {} from {} to {}", tenantId, startDate, endDate);

    TenantContext tenantContext =
        TenantContext.of(tenantId, "Default Tenant", businessUnitId, "Default Business Unit");
    BalanceReconciliationService.ReconciliationResult result =
        balanceReconciliationService.reconcileBalancesForDateRange(
            tenantContext, startDate, endDate);

    return ResponseEntity.ok(result);
  }

  @PostMapping("/fix/{tenantId}")
  @Operation(
      summary = "Fix balance discrepancies",
      description = "Fixes balance discrepancies by updating cached balances")
  public ResponseEntity<Map<String, Object>> fixBalanceDiscrepancies(
      @Parameter(description = "Tenant ID") @PathVariable String tenantId,
      @Parameter(description = "Business Unit ID") @RequestParam String businessUnitId) {

    log.info("Fixing balance discrepancies for tenant {}", tenantId);

    TenantContext tenantContext =
        TenantContext.of(tenantId, "Default Tenant", businessUnitId, "Default Business Unit");
    balanceReconciliationService.fixBalanceDiscrepancies(tenantContext);

    return ResponseEntity.ok(
        Map.of(
            "tenantId",
            tenantId,
            "status",
            "discrepancies_fixed",
            "timestamp",
            java.time.Instant.now()));
  }

  @GetMapping("/integrity/{tenantId}")
  @Operation(
      summary = "Validate ledger integrity",
      description = "Validates the integrity of the ledger")
  public ResponseEntity<Map<String, Object>> validateLedgerIntegrity(
      @Parameter(description = "Tenant ID") @PathVariable String tenantId,
      @Parameter(description = "Business Unit ID") @RequestParam String businessUnitId) {

    log.info("Validating ledger integrity for tenant {}", tenantId);

    TenantContext tenantContext =
        TenantContext.of(tenantId, "Default Tenant", businessUnitId, "Default Business Unit");
    boolean isValid = balanceValidationService.validateLedgerIntegrity(tenantContext);

    return ResponseEntity.ok(
        Map.of(
            "tenantId", tenantId,
            "integrityValid", isValid,
            "timestamp", java.time.Instant.now()));
  }
}
