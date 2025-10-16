package com.payments.accountadapter.api;

import com.payments.accountadapter.dto.AccountServiceException;
import com.payments.accountadapter.service.ErrorMappingService;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global Exception Handler
 *
 * <p>Handles all exceptions across the application: - Account service exceptions - Validation
 * exceptions - Timeout exceptions - Generic exceptions
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final ErrorMappingService errorMappingService;

  /** Handle AccountServiceException */
  @ExceptionHandler(AccountServiceException.class)
  public ResponseEntity<Map<String, Object>> handleAccountServiceException(
      AccountServiceException ex, WebRequest request) {

    log.error("Account service exception: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);

    Map<String, Object> errorResponse =
        Map.of(
            "errorCode",
            ex.getErrorCode(),
            "errorMessage",
            ex.getMessage(),
            "errorType",
            ex.getErrorType(),
            "correlationId",
            ex.getCorrelationId() != null ? ex.getCorrelationId() : "unknown",
            "requestId",
            ex.getRequestId() != null ? ex.getRequestId() : "unknown",
            "accountNumber",
            ex.getAccountNumber() != null ? ex.getAccountNumber() : "unknown",
            "tenantId",
            ex.getTenantId() != null ? ex.getTenantId() : "unknown",
            "operation",
            ex.getOperation() != null ? ex.getOperation() : "unknown",
            "timestamp",
            Instant.now(),
            "path",
            request.getDescription(false));

    HttpStatus status = determineHttpStatus(ex.getErrorType());
    return ResponseEntity.status(status).body(errorResponse);
  }

  /** Handle CompletionException */
  @ExceptionHandler(CompletionException.class)
  public ResponseEntity<Map<String, Object>> handleCompletionException(
      CompletionException ex, WebRequest request) {

    log.error("Completion exception", ex);

    Throwable cause = ex.getCause();
    if (cause instanceof AccountServiceException) {
      return handleAccountServiceException((AccountServiceException) cause, request);
    }

    Map<String, Object> errorResponse =
        Map.of(
            "errorCode",
            "ASYNC_ERROR",
            "errorMessage",
            "Asynchronous operation failed: " + ex.getMessage(),
            "errorType",
            "ASYNC_ERROR",
            "timestamp",
            Instant.now(),
            "path",
            request.getDescription(false));

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /** Handle TimeoutException */
  @ExceptionHandler(TimeoutException.class)
  public ResponseEntity<Map<String, Object>> handleTimeoutException(
      TimeoutException ex, WebRequest request) {

    log.error("Timeout exception", ex);

    Map<String, Object> errorResponse =
        Map.of(
            "errorCode",
            "TIMEOUT_ERROR",
            "errorMessage",
            "Operation timed out: " + ex.getMessage(),
            "errorType",
            "TIMEOUT_ERROR",
            "timestamp",
            Instant.now(),
            "path",
            request.getDescription(false));

    return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse);
  }

  /** Handle IllegalArgumentException */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {

    log.error("Illegal argument exception", ex);

    Map<String, Object> errorResponse =
        Map.of(
            "errorCode",
            "INVALID_ARGUMENT",
            "errorMessage",
            "Invalid argument: " + ex.getMessage(),
            "errorType",
            "VALIDATION_ERROR",
            "timestamp",
            Instant.now(),
            "path",
            request.getDescription(false));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /** Handle RuntimeException */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleRuntimeException(
      RuntimeException ex, WebRequest request) {

    log.error("Runtime exception", ex);

    Map<String, Object> errorResponse =
        Map.of(
            "errorCode",
            "RUNTIME_ERROR",
            "errorMessage",
            "Runtime error: " + ex.getMessage(),
            "errorType",
            "SYSTEM_ERROR",
            "timestamp",
            Instant.now(),
            "path",
            request.getDescription(false));

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /** Handle generic Exception */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(
      Exception ex, WebRequest request) {

    log.error("Generic exception", ex);

    Map<String, Object> errorResponse =
        Map.of(
            "errorCode",
            "UNKNOWN_ERROR",
            "errorMessage",
            "An unexpected error occurred: " + ex.getMessage(),
            "errorType",
            "SYSTEM_ERROR",
            "timestamp",
            Instant.now(),
            "path",
            request.getDescription(false));

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /**
   * Determine HTTP status based on error type
   *
   * @param errorType Error type
   * @return HTTP status
   */
  private HttpStatus determineHttpStatus(String errorType) {
    return switch (errorType) {
      case "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
      case "AUTHENTICATION_ERROR" -> HttpStatus.UNAUTHORIZED;
      case "AUTHORIZATION_ERROR" -> HttpStatus.FORBIDDEN;
      case "NOT_FOUND_ERROR" -> HttpStatus.NOT_FOUND;
      case "TIMEOUT_ERROR" -> HttpStatus.REQUEST_TIMEOUT;
      case "CIRCUIT_BREAKER_ERROR" -> HttpStatus.SERVICE_UNAVAILABLE;
      case "NETWORK_ERROR" -> HttpStatus.BAD_GATEWAY;
      case "CACHE_ERROR" -> HttpStatus.SERVICE_UNAVAILABLE;
      case "PERFORMANCE_ERROR" -> HttpStatus.REQUEST_TIMEOUT;
      case "RESILIENCE_ERROR" -> HttpStatus.SERVICE_UNAVAILABLE;
      case "INFRASTRUCTURE_ERROR" -> HttpStatus.SERVICE_UNAVAILABLE;
      default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }
}
