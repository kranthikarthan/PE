package com.payments.tenant.exception;

import com.payments.tenant.service.TenantService.TenantNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * Global Exception Handler - Maps exceptions to HTTP responses.
 *
 * <p>Handles all exceptions thrown by Tenant Management Service:
 * - TenantNotFoundException (404)
 * - IllegalArgumentException / validation errors (400)
 * - IllegalStateException (409 Conflict)
 * - Generic exceptions (500)
 *
 * <p>Returns structured error response with:
 * - timestamp: When error occurred
 * - status: HTTP status code
 * - error: Error message
 * - details: Field-level validation errors (if applicable)
 * - path: Request path
 */
@ControllerAdvice
@Slf4j
public class TenantExceptionHandler {

  /**
   * Handle tenant not found (404).
   *
   * @param ex TenantNotFoundException
   * @param request WebRequest context
   * @return 404 Not Found response
   */
  @ExceptionHandler(TenantNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleTenantNotFound(
      TenantNotFoundException ex, WebRequest request) {
    log.warn("Tenant not found: {}", ex.getMessage());

    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Tenant Not Found")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  /**
   * Handle validation errors (400).
   *
   * <p>Triggered by @Valid on DTOs when constraints are violated.
   *
   * @param ex MethodArgumentNotValidException
   * @param request WebRequest context
   * @return 400 Bad Request response with field errors
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationError(
      MethodArgumentNotValidException ex, WebRequest request) {
    log.warn("Validation error: {}", ex.getMessage());

    BindingResult bindingResult = ex.getBindingResult();
    String details =
        bindingResult.getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));

    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Invalid input parameters")
            .details(details)
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle validation errors from TenantValidator (400).
   *
   * <p>Triggered by TenantValidator.validateCreateRequest() or similar methods.
   *
   * @param ex IllegalArgumentException with validation message
   * @param request WebRequest context
   * @return 400 Bad Request response
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex, WebRequest request) {
    log.warn("Invalid argument: {}", ex.getMessage());

    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Invalid Argument")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle illegal state (409 Conflict).
   *
   * <p>Triggered when operation violates state machine rules (e.g., activate already active tenant).
   *
   * @param ex IllegalStateException
   * @param request WebRequest context
   * @return 409 Conflict response
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalState(
      IllegalStateException ex, WebRequest request) {
    log.warn("Illegal state: {}", ex.getMessage());

    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  /**
   * Handle all other exceptions (500).
   *
   * @param ex Exception
   * @param request WebRequest context
   * @return 500 Internal Server Error response
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex, WebRequest request) {
    log.error("Unexpected error", ex);

    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(OffsetDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred. Please contact support.")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
