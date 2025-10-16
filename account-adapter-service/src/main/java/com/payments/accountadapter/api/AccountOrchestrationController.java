package com.payments.accountadapter.api;

import com.payments.accountadapter.service.AccountOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Account Orchestration Controller
 *
 * <p>REST controller for orchestrated account operations: - Comprehensive account checks - Batch
 * account operations - Complex workflows - Error handling
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orchestration")
@RequiredArgsConstructor
@Tag(name = "Account Orchestration", description = "Orchestrated account operations")
public class AccountOrchestrationController {

  private final AccountOrchestrationService accountOrchestrationService;

  /**
   * Get comprehensive account information
   *
   * <p>Performs multiple account operations in sequence: 1. Account validation 2. Account status
   * check 3. Account balance retrieval
   *
   * @param accountNumber Account number
   * @param tenantId Tenant ID
   * @param businessUnitId Business unit ID
   * @param correlationId Correlation ID
   * @return Comprehensive account information
   */
  @GetMapping("/accounts/{accountNumber}/comprehensive")
  @Operation(
      summary = "Get comprehensive account information",
      description =
          "Retrieve comprehensive account information including validation, status, and balance")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comprehensive account information retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public CompletableFuture<ResponseEntity<AccountOrchestrationService.ComprehensiveAccountInfo>>
      getComprehensiveAccountInfo(
          @Parameter(description = "Account number", required = true) @PathVariable
              String accountNumber,
          @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID")
              String tenantId,
          @Parameter(description = "Business unit ID", required = true)
              @RequestHeader("X-Business-Unit-ID")
              String businessUnitId,
          @Parameter(description = "Correlation ID")
              @RequestHeader(value = "X-Correlation-ID", required = false)
              String correlationId) {

    String actualCorrelationId =
        correlationId != null ? correlationId : UUID.randomUUID().toString();

    log.info(
        "Getting comprehensive account information for account: {} with correlation: {}",
        accountNumber,
        actualCorrelationId);

    return accountOrchestrationService
        .getComprehensiveAccountInfo(accountNumber, tenantId, businessUnitId, actualCorrelationId)
        .thenApply(ResponseEntity::ok)
        .exceptionally(
            throwable -> {
              log.error(
                  "Failed to get comprehensive account information for account: {} with correlation: {}",
                  accountNumber,
                  actualCorrelationId,
                  throwable);
              return ResponseEntity.internalServerError().build();
            });
  }

  /**
   * Batch account validation
   *
   * <p>Validates multiple accounts in parallel: - Parallel processing - Result aggregation - Error
   * handling
   *
   * @param accountNumbers List of account numbers
   * @param tenantId Tenant ID
   * @param businessUnitId Business unit ID
   * @param correlationId Correlation ID
   * @return Batch validation results
   */
  @PostMapping("/accounts/batch-validation")
  @Operation(
      summary = "Batch account validation",
      description = "Validate multiple accounts in parallel")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Batch validation completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public CompletableFuture<ResponseEntity<AccountOrchestrationService.BatchAccountResult>>
      batchAccountValidation(
          @Parameter(description = "List of account numbers", required = true) @RequestBody
              List<String> accountNumbers,
          @Parameter(description = "Tenant ID", required = true) @RequestHeader("X-Tenant-ID")
              String tenantId,
          @Parameter(description = "Business unit ID", required = true)
              @RequestHeader("X-Business-Unit-ID")
              String businessUnitId,
          @Parameter(description = "Correlation ID")
              @RequestHeader(value = "X-Correlation-ID", required = false)
              String correlationId) {

    String actualCorrelationId =
        correlationId != null ? correlationId : UUID.randomUUID().toString();

    log.info(
        "Starting batch account validation for {} accounts with correlation: {}",
        accountNumbers.size(),
        actualCorrelationId);

    return accountOrchestrationService
        .batchAccountValidation(accountNumbers, tenantId, businessUnitId, actualCorrelationId)
        .thenApply(ResponseEntity::ok)
        .exceptionally(
            throwable -> {
              log.error(
                  "Failed to perform batch account validation with correlation: {}",
                  actualCorrelationId,
                  throwable);
              return ResponseEntity.internalServerError().build();
            });
  }

  /**
   * Health check for orchestration service
   *
   * @return Health status
   */
  @GetMapping("/health")
  @Operation(
      summary = "Orchestration health check",
      description = "Check orchestration service health")
  public ResponseEntity<Map<String, Object>> health() {
    return ResponseEntity.ok(
        Map.of(
            "status", "UP",
            "service", "account-orchestration-service",
            "timestamp", System.currentTimeMillis()));
  }
}
