package com.payments.iam.exception;

import com.payments.iam.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all IAM service exceptions.
 *
 * <p>Maps domain exceptions to HTTP responses:
 * - ResourceNotFoundException → 404 Not Found
 * - ForbiddenException → 403 Forbidden
 * - MethodArgumentNotValidException → 400 Bad Request
 * - IllegalArgumentException → 400 Bad Request
 * - Generic Exception → 500 Internal Server Error
 */
@ControllerAdvice
@Slf4j
public class IamExceptionHandler {

  /**
   * Handle ResourceNotFoundException (404).
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFound(
      ResourceNotFoundException ex,
      WebRequest request) {
    log.warn("Resource not found: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(404)
        .error("Not Found")
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  /**
   * Handle ForbiddenException (403).
   */
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponse> handleForbidden(
      ForbiddenException ex,
      WebRequest request) {
    log.warn("Access denied: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(403)
        .error("Forbidden")
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  /**
   * Handle Spring Security AccessDeniedException (403).
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(
      AccessDeniedException ex,
      WebRequest request) {
    log.warn("Spring Security access denied: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(403)
        .error("Forbidden")
        .message("Access denied")
        .path(request.getDescription(false).replace("uri=", ""))
        .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  /**
   * Handle validation exceptions (400).
   * Thrown by Spring's @Valid annotation on request DTOs
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex,
      WebRequest request) {
    log.warn("Validation error: {}", ex.getMessage());

    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(error -> fieldErrors.put(
            error.getField(),
            error.getDefaultMessage()));

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(400)
        .error("Bad Request")
        .message("Validation failed")
        .details(fieldErrors.toString())
        .path(request.getDescription(false).replace("uri=", ""))
        .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle IllegalArgumentException (400).
   * Thrown by business logic validation (e.g., role not found)
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
      IllegalArgumentException ex,
      WebRequest request) {
    log.warn("Illegal argument: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(400)
        .error("Bad Request")
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle IllegalStateException (409).
   * Thrown when operation violates state constraints
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalState(
      IllegalStateException ex,
      WebRequest request) {
    log.warn("Illegal state: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(409)
        .error("Conflict")
        .message(ex.getMessage())
        .path(request.getDescription(false).replace("uri=", ""))
        .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  /**
   * Handle generic exceptions (500).
   * Fallback for unexpected errors
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex,
      WebRequest request) {
    log.error("Unexpected error", ex);

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(500)
        .error("Internal Server Error")
        .message("An unexpected error occurred")
        .path(request.getDescription(false).replace("uri=", ""))
        .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
