package com.payments.accountadapter.service;

import com.payments.accountadapter.dto.AccountServiceException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Error Mapping Service
 *
 * <p>Maps and transforms errors for consistent handling: - Error classification - Error
 * transformation - Error context enrichment - Error logging
 */
@Slf4j
@Service
public class ErrorMappingService {

  private static final Map<String, ErrorMapping> ERROR_MAPPINGS =
      Map.of(
          "account_validation",
              new ErrorMapping(
                  "ACCOUNT_VALIDATION_ERROR", "VALIDATION_ERROR", "Account validation failed"),
          "account_status",
              new ErrorMapping(
                  "ACCOUNT_STATUS_ERROR", "SERVICE_ERROR", "Account status retrieval failed"),
          "account_balance",
              new ErrorMapping(
                  "ACCOUNT_BALANCE_ERROR", "SERVICE_ERROR", "Account balance retrieval failed"),
          "oauth_token",
              new ErrorMapping(
                  "OAUTH_TOKEN_ERROR", "AUTHENTICATION_ERROR", "OAuth2 token acquisition failed"),
          "cache_error",
              new ErrorMapping("CACHE_ERROR", "INFRASTRUCTURE_ERROR", "Cache operation failed"),
          "circuit_breaker",
              new ErrorMapping(
                  "CIRCUIT_BREAKER_ERROR", "RESILIENCE_ERROR", "Circuit breaker opened"),
          "timeout", new ErrorMapping("TIMEOUT_ERROR", "PERFORMANCE_ERROR", "Operation timed out"),
          "network",
              new ErrorMapping(
                  "NETWORK_ERROR", "CONNECTIVITY_ERROR", "Network communication failed"));

  /**
   * Map error to standardized format
   *
   * @param throwable Original exception
   * @param operation Operation context
   * @return Mapped exception
   */
  public AccountServiceException mapError(Throwable throwable, String operation) {
    log.debug("Mapping error for operation: {}", operation, throwable);

    ErrorMapping mapping = ERROR_MAPPINGS.get(operation);
    if (mapping == null) {
      mapping = new ErrorMapping("UNKNOWN_ERROR", "SYSTEM_ERROR", "Unknown error occurred");
    }

    // Determine error type based on exception
    String errorType = determineErrorType(throwable);
    String errorCode = mapping.getErrorCode();
    String errorMessage = mapping.getErrorMessage();

    // Enrich error message with original exception details
    if (throwable.getMessage() != null) {
      errorMessage += ": " + throwable.getMessage();
    }

    log.error("Mapped error: {} - {} for operation: {}", errorCode, errorMessage, operation);

    return new AccountServiceException(
        errorMessage,
        throwable,
        errorCode,
        errorType,
        null, // correlationId
        null, // requestId
        null, // accountNumber
        null, // tenantId
        operation);
  }

  /**
   * Map error with context
   *
   * @param throwable Original exception
   * @param operation Operation context
   * @param correlationId Correlation ID
   * @param requestId Request ID
   * @param accountNumber Account number
   * @param tenantId Tenant ID
   * @return Mapped exception
   */
  public AccountServiceException mapError(
      Throwable throwable,
      String operation,
      String correlationId,
      String requestId,
      String accountNumber,
      String tenantId) {
    log.debug("Mapping error with context for operation: {}", operation, throwable);

    ErrorMapping mapping = ERROR_MAPPINGS.get(operation);
    if (mapping == null) {
      mapping = new ErrorMapping("UNKNOWN_ERROR", "SYSTEM_ERROR", "Unknown error occurred");
    }

    String errorType = determineErrorType(throwable);
    String errorCode = mapping.getErrorCode();
    String errorMessage = mapping.getErrorMessage();

    if (throwable.getMessage() != null) {
      errorMessage += ": " + throwable.getMessage();
    }

    log.error(
        "Mapped error with context: {} - {} for operation: {} (account: {}, tenant: {})",
        errorCode,
        errorMessage,
        operation,
        accountNumber,
        tenantId);

    return new AccountServiceException(
        errorMessage,
        throwable,
        errorCode,
        errorType,
        correlationId,
        requestId,
        accountNumber,
        tenantId,
        operation);
  }

  /**
   * Determine error type based on exception
   *
   * @param throwable Exception
   * @return Error type
   */
  private String determineErrorType(Throwable throwable) {
    if (throwable instanceof TimeoutException) {
      return "TIMEOUT_ERROR";
    } else if (throwable instanceof CompletionException) {
      return "ASYNC_ERROR";
    } else if (throwable instanceof AccountServiceException) {
      return ((AccountServiceException) throwable).getErrorType();
    } else if (throwable.getMessage() != null && throwable.getMessage().contains("timeout")) {
      return "TIMEOUT_ERROR";
    } else if (throwable.getMessage() != null && throwable.getMessage().contains("network")) {
      return "NETWORK_ERROR";
    } else if (throwable.getMessage() != null && throwable.getMessage().contains("circuit")) {
      return "CIRCUIT_BREAKER_ERROR";
    } else if (throwable.getMessage() != null && throwable.getMessage().contains("cache")) {
      return "CACHE_ERROR";
    } else if (throwable.getMessage() != null && throwable.getMessage().contains("token")) {
      return "AUTHENTICATION_ERROR";
    } else {
      return "SYSTEM_ERROR";
    }
  }

  /**
   * Create error response
   *
   * @param throwable Exception
   * @param operation Operation context
   * @return Error response
   */
  public ErrorResponse createErrorResponse(Throwable throwable, String operation) {
    ErrorMapping mapping = ERROR_MAPPINGS.get(operation);
    if (mapping == null) {
      mapping = new ErrorMapping("UNKNOWN_ERROR", "SYSTEM_ERROR", "Unknown error occurred");
    }

    return ErrorResponse.builder()
        .errorCode(mapping.getErrorCode())
        .errorMessage(mapping.getErrorMessage())
        .errorType(determineErrorType(throwable))
        .originalError(throwable.getMessage())
        .operation(operation)
        .timestamp(Instant.now())
        .build();
  }

  /** Error Mapping */
  @lombok.AllArgsConstructor
  @lombok.Data
  public static class ErrorMapping {
    private String errorCode;
    private String errorType;
    private String errorMessage;
  }

  /** Error Response */
  @lombok.Builder
  @lombok.Data
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class ErrorResponse {
    private String errorCode;
    private String errorMessage;
    private String errorType;
    private String originalError;
    private String operation;
    private Instant timestamp;
  }
}
