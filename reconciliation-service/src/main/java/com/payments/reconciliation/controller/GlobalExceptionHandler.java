package com.payments.reconciliation.controller;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    log.warn("Bad request: {}", ex.getMessage());
    Map<String, String> errors = new HashMap<>();
    errors.put("error", ex.getMessage());
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, String>> handleConstraintViolationException(
      ConstraintViolationException ex) {
    log.warn("Constraint violation: {}", ex.getMessage());
    Map<String, String> errors = new HashMap<>();
    ex.getConstraintViolations()
        .forEach(
            violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    log.warn("Method argument not valid: {}", ex.getMessage());
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Map<String, String>> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException ex) {
    log.warn("Missing parameter: {}", ex.getMessage());
    Map<String, String> errors = new HashMap<>();
    errors.put("error", ex.getMessage());
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleAllUncaughtException(Exception ex) {
    log.error("Internal server error: {}", ex.getMessage(), ex);
    Map<String, String> errors = new HashMap<>();
    errors.put("error", "Internal server error");
    return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
