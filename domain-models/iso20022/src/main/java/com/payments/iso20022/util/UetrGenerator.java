package com.payments.iso20022.util;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * UETR (Unique End-to-End Transaction Reference) Generator
 *
 * <p>Generates RFC 4122 compliant UUID v4 identifiers for ISO 20022 payment messages as per SWIFT
 * gpi requirements.
 *
 * <p>Format: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
 *
 * <ul>
 *   <li>x = any hexadecimal digit
 *   <li>4 = version identifier (UUID v4)
 *   <li>y = one of 8, 9, A, or B (variant bits)
 * </ul>
 *
 * <p>Used for:
 *
 * <ul>
 *   <li>UETR (Unique End-to-End Transaction Reference)
 *   <li>Message IDs (MsgId)
 *   <li>Transaction IDs (TxId)
 *   <li>Instruction IDs (InstrId)
 * </ul>
 */
@Slf4j
public class UetrGenerator {

  // RFC 4122 UUID v4 pattern with hyphens (8-4-4-4-12 format)
  private static final Pattern UETR_PATTERN =
      Pattern.compile(
          "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");

  // Pattern for any valid UUID (for broader validation)
  private static final Pattern UUID_PATTERN =
      Pattern.compile(
          "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  /**
   * Generate a new UETR (UUID v4)
   *
   * <p>Uses UUID.randomUUID() which generates cryptographically strong random UUIDs.
   *
   * @return UETR in format: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
   */
  public static String generate() {
    String uetr = UUID.randomUUID().toString().toLowerCase();
    log.debug("Generated UETR: {}", uetr);
    return uetr;
  }

  /**
   * Generate UETR with prefix for traceability
   *
   * <p>Useful for testing and debugging - adds a suffix while maintaining UUID format.
   *
   * <p>Note: This is NOT part of ISO 20022 standard - use for internal tracking only.
   *
   * @param prefix The prefix/context (e.g., "SAMOS", "PAYSHAP")
   * @return UETR with metadata suffix
   */
  public static String generateWithContext(String prefix) {
    String uetr = generate();
    log.debug("Generated UETR with context [{}]: {}", prefix, uetr);
    return uetr; // Return standard UETR (prefix for logging only)
  }

  /**
   * Generate multiple UETRs for batch processing
   *
   * @param count Number of UETRs to generate
   * @return Array of unique UETRs
   */
  public static String[] generateBatch(int count) {
    if (count <= 0 || count > 10000) {
      throw new IllegalArgumentException("Count must be between 1 and 10000");
    }

    String[] uetrs = new String[count];
    for (int i = 0; i < count; i++) {
      uetrs[i] = generate();
    }

    log.debug("Generated batch of {} UETRs", count);
    return uetrs;
  }

  /**
   * Validate UETR format (strict RFC 4122 UUID v4)
   *
   * <p>Validates:
   *
   * <ul>
   *   <li>36 characters total (32 hex + 4 hyphens)
   *   <li>Format: 8-4-4-4-12
   *   <li>Version nibble = 4 (position 14)
   *   <li>Variant bits = 8, 9, A, or B (position 19)
   * </ul>
   *
   * @param uetr The UETR to validate
   * @return true if valid UUID v4
   */
  public static boolean isValid(String uetr) {
    if (uetr == null || uetr.isEmpty()) {
      return false;
    }

    // Check basic format and UUID v4 requirements
    boolean valid = UETR_PATTERN.matcher(uetr).matches();

    if (!valid) {
      log.debug("Invalid UETR format: {}", uetr);
    }

    return valid;
  }

  /**
   * Validate UETR format (lenient - any UUID version)
   *
   * <p>Use for backward compatibility or when accepting UUIDs from external systems.
   *
   * @param uuid The UUID to validate
   * @return true if valid UUID (any version)
   */
  public static boolean isValidUuid(String uuid) {
    if (uuid == null || uuid.isEmpty()) {
      return false;
    }

    return UUID_PATTERN.matcher(uuid).matches();
  }

  /**
   * Validate and throw exception if invalid
   *
   * @param uetr The UETR to validate
   * @throws InvalidUetrException if UETR is invalid
   */
  public static void validateOrThrow(String uetr) {
    if (!isValid(uetr)) {
      throw new InvalidUetrException(
          "Invalid UETR format: "
              + uetr
              + ". Expected RFC 4122 UUID v4 (e.g., 550e8400-e29b-41d4-a716-446655440000)");
    }
  }

  /**
   * Extract UETR version
   *
   * @param uetr The UETR
   * @return UUID version (should be 4 for valid UETR)
   */
  public static int getVersion(String uetr) {
    if (!isValidUuid(uetr)) {
      throw new InvalidUetrException("Invalid UUID format: " + uetr);
    }

    // Version is at position 14 (0-indexed)
    char versionChar = uetr.charAt(14);
    return Character.digit(versionChar, 16);
  }

  /**
   * Extract UETR variant
   *
   * @param uetr The UETR
   * @return UUID variant bits
   */
  public static int getVariant(String uetr) {
    if (!isValidUuid(uetr)) {
      throw new InvalidUetrException("Invalid UUID format: " + uetr);
    }

    // Variant is at position 19 (0-indexed)
    char variantChar = uetr.charAt(19);
    return Character.digit(variantChar, 16);
  }

  /**
   * Format UETR (ensure lowercase and proper format)
   *
   * @param uetr The UETR to format
   * @return Formatted UETR
   */
  public static String format(String uetr) {
    if (uetr == null || uetr.isEmpty()) {
      throw new InvalidUetrException("UETR cannot be null or empty");
    }

    // Remove hyphens, convert to lowercase, re-add hyphens
    String normalized = uetr.replaceAll("-", "").toLowerCase();

    if (normalized.length() != 32) {
      throw new InvalidUetrException(
          "Invalid UETR length: " + normalized.length() + " (expected 32 hex digits)");
    }

    // Format as 8-4-4-4-12
    return String.format(
        "%s-%s-%s-%s-%s",
        normalized.substring(0, 8),
        normalized.substring(8, 12),
        normalized.substring(12, 16),
        normalized.substring(16, 20),
        normalized.substring(20, 32));
  }

  /**
   * Convert UETR to uppercase (some systems require uppercase)
   *
   * @param uetr The UETR
   * @return Uppercase UETR
   */
  public static String toUpperCase(String uetr) {
    validateOrThrow(uetr);
    return uetr.toUpperCase();
  }

  /**
   * Convert UETR to lowercase (ISO 20022 standard format)
   *
   * @param uetr The UETR
   * @return Lowercase UETR
   */
  public static String toLowerCase(String uetr) {
    validateOrThrow(uetr);
    return uetr.toLowerCase();
  }

  /**
   * Remove hyphens from UETR (for systems that require compact format)
   *
   * @param uetr The UETR
   * @return UETR without hyphens (32 hex chars)
   */
  public static String toCompactFormat(String uetr) {
    validateOrThrow(uetr);
    return uetr.replaceAll("-", "");
  }

  /**
   * Parse UUID from Java UUID object
   *
   * @param uuid The UUID object
   * @return UETR string
   */
  public static String fromUuid(UUID uuid) {
    if (uuid == null) {
      throw new InvalidUetrException("UUID cannot be null");
    }
    return uuid.toString().toLowerCase();
  }

  /**
   * Convert UETR string to UUID object
   *
   * @param uetr The UETR string
   * @return UUID object
   */
  public static UUID toUuid(String uetr) {
    validateOrThrow(uetr);
    return UUID.fromString(uetr);
  }

  /**
   * Check if two UETRs are equal (case-insensitive)
   *
   * @param uetr1 First UETR
   * @param uetr2 Second UETR
   * @return true if UETRs are equal
   */
  public static boolean equals(String uetr1, String uetr2) {
    if (uetr1 == null || uetr2 == null) {
      return false;
    }
    return uetr1.equalsIgnoreCase(uetr2);
  }

  /** Invalid UETR Exception */
  public static class InvalidUetrException extends RuntimeException {
    public InvalidUetrException(String message) {
      super(message);
    }
  }
}
