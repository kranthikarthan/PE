package com.payments.accountadapter.api;

import com.payments.accountadapter.dto.*;
import com.payments.accountadapter.service.AccountAdapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Account Adapter Controller
 *
 * <p>REST controller for account adapter operations: - Account balance retrieval - Account
 * validation - Account status checking - Resilience patterns integration
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Adapter", description = "Account adapter operations with resilience patterns")
public class AccountAdapterController {

  private final AccountAdapterService accountAdapterService;

  /**
   * Get account balance
   *
   * @param accountNumber Account number
   * @param tenantId Tenant ID
   * @param businessUnitId Business unit ID
   * @param correlationId Correlation ID
   * @return Account balance response
   */
  @GetMapping("/{accountNumber}/balance")
  @Operation(
      summary = "Get account balance",
      description = "Retrieve account balance with resilience patterns")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Account balance retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public CompletableFuture<ResponseEntity<AccountBalanceResponse>> getAccountBalance(
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
        "Getting account balance for account: {} with correlation: {}",
        accountNumber,
        actualCorrelationId);

    return accountAdapterService
        .getAccountBalance(accountNumber, tenantId, businessUnitId, actualCorrelationId)
        .thenApply(ResponseEntity::ok)
        .exceptionally(
            throwable -> {
              log.error(
                  "Failed to get account balance for account: {} with correlation: {}",
                  accountNumber,
                  actualCorrelationId,
                  throwable);
              return ResponseEntity.internalServerError().build();
            });
  }

  /**
   * Validate account
   *
   * @param accountNumber Account number
   * @param tenantId Tenant ID
   * @param businessUnitId Business unit ID
   * @param correlationId Correlation ID
   * @return Account validation response
   */
  @PostMapping("/{accountNumber}/validate")
  @Operation(
      summary = "Validate account",
      description = "Validate account with resilience patterns")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Account validation completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public CompletableFuture<ResponseEntity<AccountValidationResponse>> validateAccount(
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

    log.info("Validating account: {} with correlation: {}", accountNumber, actualCorrelationId);

    return accountAdapterService
        .validateAccount(accountNumber, tenantId, businessUnitId, actualCorrelationId)
        .thenApply(ResponseEntity::ok)
        .exceptionally(
            throwable -> {
              log.error(
                  "Failed to validate account: {} with correlation: {}",
                  accountNumber,
                  actualCorrelationId,
                  throwable);
              return ResponseEntity.internalServerError().build();
            });
  }

  /**
   * Get account status
   *
   * @param accountNumber Account number
   * @param tenantId Tenant ID
   * @param businessUnitId Business unit ID
   * @param correlationId Correlation ID
   * @return Account status response
   */
  @GetMapping("/{accountNumber}/status")
  @Operation(
      summary = "Get account status",
      description = "Retrieve account status with resilience patterns")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Account status retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public CompletableFuture<ResponseEntity<AccountStatusResponse>> getAccountStatus(
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
        "Getting account status for account: {} with correlation: {}",
        accountNumber,
        actualCorrelationId);

    return accountAdapterService
        .getAccountStatus(accountNumber, tenantId, businessUnitId, actualCorrelationId)
        .thenApply(ResponseEntity::ok)
        .exceptionally(
            throwable -> {
              log.error(
                  "Failed to get account status for account: {} with correlation: {}",
                  accountNumber,
                  actualCorrelationId,
                  throwable);
              return ResponseEntity.internalServerError().build();
            });
  }

  /**
   * Health check endpoint
   *
   * @return Health status
   */
  @GetMapping("/health")
  @Operation(summary = "Health check", description = "Check service health")
  public ResponseEntity<Map<String, Object>> health() {
    return ResponseEntity.ok(
        Map.of(
            "status", "UP",
            "service", "account-adapter-service",
            "timestamp", System.currentTimeMillis()));
  }
}
