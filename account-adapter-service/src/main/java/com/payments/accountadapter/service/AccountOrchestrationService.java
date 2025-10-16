package com.payments.accountadapter.service;

import com.payments.accountadapter.dto.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Account Orchestration Service
 *
 * <p>Orchestrates complex account operations: - Multi-step account operations - Error handling and
 * mapping - Operation coordination - Result aggregation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountOrchestrationService {

  private final AccountAdapterService accountAdapterService;
  private final AccountCacheService accountCacheService;
  private final OAuth2TokenService oAuth2TokenService;
  private final ErrorMappingService errorMappingService;

  /**
   * Comprehensive account check
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
  public CompletableFuture<ComprehensiveAccountInfo> getComprehensiveAccountInfo(
      String accountNumber, String tenantId, String businessUnitId, String correlationId) {

    log.info(
        "Starting comprehensive account check for account: {} with correlation: {}",
        accountNumber,
        correlationId);

    return CompletableFuture.supplyAsync(
            () -> {
              try {
                // Step 1: Validate account
                log.debug("Step 1: Validating account: {}", accountNumber);
                AccountValidationResponse validationResponse =
                    accountAdapterService
                        .validateAccount(accountNumber, tenantId, businessUnitId, correlationId)
                        .join();

                if (!validationResponse.isValid()) {
                  throw new AccountServiceException(
                      "Account validation failed: "
                          + String.join(", ", validationResponse.getValidationErrors()),
                      "ACCOUNT_VALIDATION_FAILED",
                      "VALIDATION_ERROR",
                      correlationId,
                      UUID.randomUUID().toString(),
                      accountNumber,
                      tenantId,
                      "getComprehensiveAccountInfo");
                }

                return validationResponse;
              } catch (Exception e) {
                log.error("Account validation failed for account: {}", accountNumber, e);
                throw new CompletionException(
                    errorMappingService.mapError(e, "account_validation"));
              }
            })
        .thenCompose(
            validationResponse -> {
              try {
                // Step 2: Get account status
                log.debug("Step 2: Getting account status for account: {}", accountNumber);
                return accountAdapterService
                    .getAccountStatus(accountNumber, tenantId, businessUnitId, correlationId)
                    .thenApply(
                        statusResponse ->
                            new AccountValidationStatusPair(validationResponse, statusResponse));
              } catch (Exception e) {
                log.error("Account status retrieval failed for account: {}", accountNumber, e);
                throw new CompletionException(errorMappingService.mapError(e, "account_status"));
              }
            })
        .thenCompose(
            validationStatusPair -> {
              try {
                // Step 3: Get account balance
                log.debug("Step 3: Getting account balance for account: {}", accountNumber);
                return accountAdapterService
                    .getAccountBalance(accountNumber, tenantId, businessUnitId, correlationId)
                    .thenApply(
                        balanceResponse ->
                            ComprehensiveAccountInfo.builder()
                                .accountNumber(accountNumber)
                                .tenantId(tenantId)
                                .businessUnitId(businessUnitId)
                                .correlationId(correlationId)
                                .validationResponse(validationStatusPair.getValidationResponse())
                                .statusResponse(validationStatusPair.getStatusResponse())
                                .balanceResponse(balanceResponse)
                                .timestamp(Instant.now())
                                .build());
              } catch (Exception e) {
                log.error("Account balance retrieval failed for account: {}", accountNumber, e);
                throw new CompletionException(errorMappingService.mapError(e, "account_balance"));
              }
            })
        .exceptionally(
            throwable -> {
              log.error(
                  "Comprehensive account check failed for account: {} with correlation: {}",
                  accountNumber,
                  correlationId,
                  throwable);

              // Return partial information if available
              return ComprehensiveAccountInfo.builder()
                  .accountNumber(accountNumber)
                  .tenantId(tenantId)
                  .businessUnitId(businessUnitId)
                  .correlationId(correlationId)
                  .error(
                      ErrorInfo.builder()
                          .errorCode("COMPREHENSIVE_CHECK_FAILED")
                          .errorMessage("Failed to retrieve comprehensive account information")
                          .errorType("ORCHESTRATION_ERROR")
                          .originalError(throwable.getMessage())
                          .timestamp(Instant.now())
                          .build())
                  .timestamp(Instant.now())
                  .build();
            });
  }

  /**
   * Batch account operations
   *
   * <p>Performs operations on multiple accounts in parallel: - Account validation for multiple
   * accounts - Parallel processing - Result aggregation
   *
   * @param accountNumbers List of account numbers
   * @param tenantId Tenant ID
   * @param businessUnitId Business unit ID
   * @param correlationId Correlation ID
   * @return Batch operation results
   */
  public CompletableFuture<BatchAccountResult> batchAccountValidation(
      List<String> accountNumbers, String tenantId, String businessUnitId, String correlationId) {

    log.info(
        "Starting batch account validation for {} accounts with correlation: {}",
        accountNumbers.size(),
        correlationId);

    List<CompletableFuture<AccountValidationResult>> futures =
        accountNumbers.stream()
            .map(
                accountNumber ->
                    accountAdapterService
                        .validateAccount(accountNumber, tenantId, businessUnitId, correlationId)
                        .thenApply(
                            response ->
                                AccountValidationResult.builder()
                                    .accountNumber(accountNumber)
                                    .isValid(response.isValid())
                                    .validationStatus(response.getValidationStatus())
                                    .validationErrors(response.getValidationErrors())
                                    .responseCode(response.getResponseCode())
                                    .responseMessage(response.getResponseMessage())
                                    .timestamp(Instant.now())
                                    .build())
                        .exceptionally(
                            throwable -> {
                              log.error(
                                  "Batch validation failed for account: {}",
                                  accountNumber,
                                  throwable);
                              return AccountValidationResult.builder()
                                  .accountNumber(accountNumber)
                                  .isValid(false)
                                  .validationStatus("ERROR")
                                  .validationErrors(
                                      List.of("Validation failed: " + throwable.getMessage()))
                                  .responseCode("VALIDATION_ERROR")
                                  .responseMessage("Failed to validate account")
                                  .timestamp(Instant.now())
                                  .build();
                            }))
            .toList();

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(
            v -> {
              List<AccountValidationResult> results =
                  futures.stream().map(CompletableFuture::join).toList();

              long validCount = results.stream().mapToLong(r -> r.isValid() ? 1 : 0).sum();
              long invalidCount = results.size() - validCount;

              return BatchAccountResult.builder()
                  .totalAccounts(accountNumbers.size())
                  .validAccounts(validCount)
                  .invalidAccounts(invalidCount)
                  .results(results)
                  .correlationId(correlationId)
                  .timestamp(Instant.now())
                  .build();
            });
  }

  /** Account validation status pair */
  @lombok.Builder
  @lombok.Data
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class AccountValidationStatusPair {
    private AccountValidationResponse validationResponse;
    private AccountStatusResponse statusResponse;
  }

  /** Comprehensive account information */
  @lombok.Builder
  @lombok.Data
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class ComprehensiveAccountInfo {
    private String accountNumber;
    private String tenantId;
    private String businessUnitId;
    private String correlationId;
    private AccountValidationResponse validationResponse;
    private AccountStatusResponse statusResponse;
    private AccountBalanceResponse balanceResponse;
    private ErrorInfo error;
    private Instant timestamp;
  }

  /** Account validation result */
  @lombok.Builder
  @lombok.Data
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class AccountValidationResult {
    private String accountNumber;
    private boolean isValid;
    private String validationStatus;
    private List<String> validationErrors;
    private String responseCode;
    private String responseMessage;
    private Instant timestamp;
  }

  /** Batch account result */
  @lombok.Builder
  @lombok.Data
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class BatchAccountResult {
    private long totalAccounts;
    private long validAccounts;
    private long invalidAccounts;
    private List<AccountValidationResult> results;
    private String correlationId;
    private Instant timestamp;
  }

  /** Error information */
  @lombok.Builder
  @lombok.Data
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class ErrorInfo {
    private String errorCode;
    private String errorMessage;
    private String errorType;
    private String originalError;
    private Instant timestamp;
  }
}
