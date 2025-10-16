package com.payments.routing.api;

import com.payments.routing.api.RoutingController.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global Exception Handler
 *
 * <p>Handles all exceptions across the routing service: - Validation errors (400) - Business logic
 * errors (400) - Internal server errors (500) - Security errors (401/403)
 *
 * <p>Ensures consistent error responses with correlation ID
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handle validation errors
   *
   * @param ex Validation exception
   * @param request Web request
   * @return Error response
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex, WebRequest request) {

    String correlationId = getCorrelationId(request);
    log.warn("Validation error: {}", ex.getMessage(), ex);

    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

    ErrorResponse error =
        new ErrorResponse(
            "VALIDATION_ERROR",
            "Validation failed: " + message,
            correlationId,
            Instant.now().toString());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle constraint violation errors
   *
   * @param ex Constraint violation exception
   * @param request Web request
   * @return Error response
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      ConstraintViolationException ex, WebRequest request) {

    String correlationId = getCorrelationId(request);
    log.warn("Constraint violation: {}", ex.getMessage(), ex);

    String message =
        ex.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.joining(", "));

    ErrorResponse error =
        new ErrorResponse(
            "CONSTRAINT_VIOLATION",
            "Constraint violation: " + message,
            correlationId,
            Instant.now().toString());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle illegal argument errors
   *
   * @param ex Illegal argument exception
   * @param request Web request
   * @return Error response
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {

    String correlationId = getCorrelationId(request);
    log.warn("Illegal argument: {}", ex.getMessage(), ex);

    ErrorResponse error =
        new ErrorResponse(
            "INVALID_ARGUMENT", ex.getMessage(), correlationId, Instant.now().toString());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle illegal state errors
   *
   * @param ex Illegal state exception
   * @param request Web request
   * @return Error response
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalStateException(
      IllegalStateException ex, WebRequest request) {

    String correlationId = getCorrelationId(request);
    log.warn("Illegal state: {}", ex.getMessage(), ex);

    ErrorResponse error =
        new ErrorResponse(
            "INVALID_STATE", ex.getMessage(), correlationId, Instant.now().toString());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  /**
   * Handle runtime exceptions
   *
   * @param ex Runtime exception
   * @param request Web request
   * @return Error response
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(
      RuntimeException ex, WebRequest request) {

    String correlationId = getCorrelationId(request);
    log.error("Runtime error: {}", ex.getMessage(), ex);

    ErrorResponse error =
        new ErrorResponse(
            "RUNTIME_ERROR",
            "An unexpected error occurred",
            correlationId,
            Instant.now().toString());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  /**
   * Handle all other exceptions
   *
   * @param ex Exception
   * @param request Web request
   * @return Error response
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {

    String correlationId = getCorrelationId(request);
    log.error("Unexpected error: {}", ex.getMessage(), ex);

    ErrorResponse error =
        new ErrorResponse(
            "INTERNAL_ERROR",
            "An internal server error occurred",
            correlationId,
            Instant.now().toString());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  /**
   * Get correlation ID from request
   *
   * @param request Web request
   * @return Correlation ID or null
   */
  private String getCorrelationId(WebRequest request) {
    return request.getHeader("X-Correlation-ID");
  }
}
