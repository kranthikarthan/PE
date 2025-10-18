package com.payments.notification.exception;

import com.payments.notification.controller.NotificationController;
import com.payments.notification.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;

/**
 * Global exception handler for Notification Service REST API.
 */
@ControllerAdvice
@Slf4j
public class NotificationExceptionHandler {

  @ExceptionHandler(NotificationController.ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<ErrorResponse> handleResourceNotFound(
      NotificationController.ResourceNotFoundException ex) {
    log.warn("Resource not found: {}", ex.getMessage());

    ErrorResponse error =
        ErrorResponse.builder()
            .code("RESOURCE_NOT_FOUND")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleValidationError(
      MethodArgumentNotValidException ex) {
    log.warn("Validation error: {}", ex.getMessage());

    String message =
        ex.getBindingResult().getAllErrors().stream()
            .map(err -> err.getDefaultMessage())
            .findFirst()
            .orElse("Validation failed");

    ErrorResponse error =
        ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message(message)
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Illegal argument: {}", ex.getMessage());

    ErrorResponse error =
        ErrorResponse.builder()
            .code("INVALID_ARGUMENT")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);

    ErrorResponse error =
        ErrorResponse.builder()
            .code("INTERNAL_ERROR")
            .message("An unexpected error occurred")
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
