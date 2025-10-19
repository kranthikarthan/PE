package com.payments.iso20022.validation;

import com.payments.iso20022.config.Iso20022MessageType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * ISO 20022 Validation Result
 *
 * <p>Contains the result of XSD validation for an ISO 20022 message.
 */
@Data
@Builder
public class ValidationResult {

  /** The message type that was validated */
  private final Iso20022MessageType messageType;

  /** Whether the validation passed */
  private final boolean valid;

  /** Validation status */
  private final ValidationStatus status;

  /** Primary validation message */
  private final String message;

  /** List of validation errors/warnings */
  @Builder.Default private final List<String> validationErrors = new ArrayList<>();

  /** Timestamp of validation */
  @Builder.Default private final Instant validatedAt = Instant.now();

  /**
   * Create a successful validation result
   *
   * @param messageType The message type
   * @return Valid result
   */
  public static ValidationResult valid(Iso20022MessageType messageType) {
    return ValidationResult.builder()
        .messageType(messageType)
        .valid(true)
        .status(ValidationStatus.VALID)
        .message("XSD validation successful")
        .validationErrors(Collections.emptyList())
        .build();
  }

  /**
   * Create an invalid validation result
   *
   * @param messageType The message type
   * @param message Error message
   * @param errors List of validation errors
   * @return Invalid result
   */
  public static ValidationResult invalid(
      Iso20022MessageType messageType, String message, List<String> errors) {
    return ValidationResult.builder()
        .messageType(messageType)
        .valid(false)
        .status(ValidationStatus.INVALID)
        .message(message)
        .validationErrors(new ArrayList<>(errors))
        .build();
  }

  /**
   * Create an error validation result (validation could not be performed)
   *
   * @param messageType The message type
   * @param message Error message
   * @param errors List of error details
   * @return Error result
   */
  public static ValidationResult error(
      Iso20022MessageType messageType, String message, List<String> errors) {
    return ValidationResult.builder()
        .messageType(messageType)
        .valid(false)
        .status(ValidationStatus.ERROR)
        .message(message)
        .validationErrors(new ArrayList<>(errors))
        .build();
  }

  /**
   * Check if validation has errors
   *
   * @return true if there are errors
   */
  public boolean hasErrors() {
    return !validationErrors.isEmpty();
  }

  /**
   * Check if validation has warnings
   *
   * @return true if there are warnings
   */
  public boolean hasWarnings() {
    return validationErrors.stream().anyMatch(e -> e.contains("WARNING"));
  }

  /**
   * Get error count
   *
   * @return Number of errors
   */
  public int getErrorCount() {
    return (int) validationErrors.stream().filter(e -> e.contains("ERROR")).count();
  }

  /**
   * Get warning count
   *
   * @return Number of warnings
   */
  public int getWarningCount() {
    return (int) validationErrors.stream().filter(e -> e.contains("WARNING")).count();
  }

  /**
   * Get business-friendly error message
   *
   * @return User-friendly error description
   */
  public String getBusinessFriendlyMessage() {
    if (valid) {
      return String.format(
          "%s message is valid and compliant with ISO 20022 standards", messageType.getMessageId());
    }

    if (status == ValidationStatus.ERROR) {
      return String.format(
          "%s message validation failed: %s. Please check the message format.",
          messageType.getMessageId(), message);
    }

    // Extract first error for user-friendly message
    String firstError = validationErrors.isEmpty() ? message : validationErrors.get(0);

    // Map common XSD errors to business-friendly messages
    if (firstError.contains("cvc-")) {
      // XSD constraint violation
      if (firstError.contains("cvc-minLength") || firstError.contains("cvc-maxLength")) {
        return String.format(
            "%s message has invalid field length. %s", messageType.getMessageId(), message);
      } else if (firstError.contains("cvc-pattern")) {
        return String.format(
            "%s message has invalid field format. %s", messageType.getMessageId(), message);
      } else if (firstError.contains("cvc-type")) {
        return String.format(
            "%s message has invalid data type. %s", messageType.getMessageId(), message);
      } else if (firstError.contains("cvc-complex-type.2.4.a")) {
        return String.format(
            "%s message is missing required fields. %s", messageType.getMessageId(), message);
      }
    }

    return String.format(
        "%s message validation failed: %s. Please check ISO 20022 compliance.",
        messageType.getMessageId(), message);
  }

  /**
   * Get formatted error summary
   *
   * @return Formatted error summary
   */
  public String getErrorSummary() {
    if (valid) {
      return "Validation successful - no errors";
    }

    StringBuilder summary = new StringBuilder();
    summary
        .append(String.format("Validation Status: %s%n", status))
        .append(String.format("Message: %s%n", message))
        .append(String.format("Errors: %d, Warnings: %d%n", getErrorCount(), getWarningCount()));

    if (!validationErrors.isEmpty()) {
      summary.append("\nValidation Details:\n");
      for (int i = 0; i < Math.min(validationErrors.size(), 10); i++) {
        summary.append(String.format("  %d. %s%n", i + 1, validationErrors.get(i)));
      }
      if (validationErrors.size() > 10) {
        summary.append(String.format("  ... and %d more errors%n", validationErrors.size() - 10));
      }
    }

    return summary.toString();
  }

  @Override
  public String toString() {
    return String.format(
        "ValidationResult{messageType=%s, status=%s, valid=%s, errors=%d, warnings=%d}",
        messageType.getMessageId(), status, valid, getErrorCount(), getWarningCount());
  }

  /** Validation Status Enum */
  public enum ValidationStatus {
    /** Validation passed - message is compliant */
    VALID,

    /** Validation failed - message has errors */
    INVALID,

    /** Validation could not be performed - system error */
    ERROR
  }
}
