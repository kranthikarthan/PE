package com.payments.audit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Global Exception Handler for Audit Service.
 *
 * <p>Handles all exceptions and returns consistent error responses with:
 * - HTTP status code
 * - Timestamp
 * - Error message
 * - Request path
 */
@ControllerAdvice
@Slf4j
public class AuditExceptionHandler {

  /**
   * Handle IllegalArgumentException (validation errors).
   *
   * @param ex the exception
   * @param request the web request
   * @return 400 Bad Request response
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex, WebRequest request) {
    log.warn("Validation error: {}", ex.getMessage());

    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle AccessDeniedException (insufficient permissions).
   *
   * @param ex the exception
   * @param request the web request
   * @return 403 Forbidden response
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(
      AccessDeniedException ex, WebRequest request) {
    log.warn("Access denied: {}", ex.getMessage());

    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .status(HttpStatus.FORBIDDEN.value())
            .error("Access Denied")
            .message("Insufficient permissions to access this resource")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
  }

  /**
   * Handle IllegalStateException (invalid state operations).
   *
   * @param ex the exception
   * @param request the web request
   * @return 409 Conflict response
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalState(
      IllegalStateException ex, WebRequest request) {
    log.warn("Invalid state: {}", ex.getMessage());

    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  /**
   * Handle generic exceptions (unexpected errors).
   *
   * @param ex the exception
   * @param request the web request
   * @return 500 Internal Server Error response
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(
      Exception ex, WebRequest request) {
    log.error("Unexpected error occurred", ex);

    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred. Please contact support.")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Error Response DTO.
   */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class ErrorResponse {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
  }
}
