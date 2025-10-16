package com.payments.paymentinitiation.api.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global Exception Handler
 *
 * <p>Handles all exceptions across the application: - Validation errors - Business logic errors -
 * System errors - Idempotency errors
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** Handle validation errors */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
    log.warn("Validation error: {}", ex.getMessage());

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .message("Validation failed")
            .code("VALIDATION_ERROR")
            .timestamp(Instant.now().toString())
            .details(errors)
            .build();

    return ResponseEntity.badRequest().body(errorResponse);
  }

  /** Handle binding errors */
  @ExceptionHandler(BindException.class)
  public ResponseEntity<ErrorResponse> handleBindErrors(BindException ex) {
    log.warn("Binding error: {}", ex.getMessage());

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .message("Binding failed")
            .code("BINDING_ERROR")
            .timestamp(Instant.now().toString())
            .details(errors)
            .build();

    return ResponseEntity.badRequest().body(errorResponse);
  }

  /** Handle JSON parsing errors */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleJsonParseError(HttpMessageNotReadableException ex) {
    log.warn("JSON parse error: {}", ex.getMessage());

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .message("Invalid JSON format")
            .code("JSON_PARSE_ERROR")
            .timestamp(Instant.now().toString())
            .build();

    return ResponseEntity.badRequest().body(errorResponse);
  }

  /** Handle business logic errors */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleBusinessError(IllegalArgumentException ex) {
    log.warn("Business logic error: {}", ex.getMessage());

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .message(ex.getMessage())
            .code("BUSINESS_ERROR")
            .timestamp(Instant.now().toString())
            .build();

    return ResponseEntity.badRequest().body(errorResponse);
  }

  /** Handle idempotency errors */
  @ExceptionHandler(IdempotencyException.class)
  public ResponseEntity<ErrorResponse> handleIdempotencyError(IdempotencyException ex) {
    log.warn("Idempotency error: {}", ex.getMessage());

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .message(ex.getMessage())
            .code("IDEMPOTENCY_ERROR")
            .timestamp(Instant.now().toString())
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }

  /** Handle payment not found errors */
  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePaymentNotFoundError(PaymentNotFoundException ex) {
    log.warn("Payment not found: {}", ex.getMessage());

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .message(ex.getMessage())
            .code("PAYMENT_NOT_FOUND")
            .timestamp(Instant.now().toString())
            .build();

    return ResponseEntity.notFound().build();
  }

  /** Handle generic exceptions */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericError(Exception ex, WebRequest request) {
    log.error("Unexpected error occurred", ex);

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .message("An unexpected error occurred")
            .code("INTERNAL_ERROR")
            .timestamp(Instant.now().toString())
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /** Error response DTO */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class ErrorResponse {
    private String message;
    private String code;
    private String timestamp;
    private Map<String, String> details;
  }

  /** Custom exception for idempotency errors */
  public static class IdempotencyException extends RuntimeException {
    public IdempotencyException(String message) {
      super(message);
    }
  }

  /** Custom exception for payment not found */
  public static class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) {
      super(message);
    }
  }
}
