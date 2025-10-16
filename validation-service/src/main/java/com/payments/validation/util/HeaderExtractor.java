package com.payments.validation.util;

import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * Header Extractor Utility
 *
 * <p>Utility for extracting and validating headers from Kafka messages: - Correlation ID extraction
 * - Tenant context extraction - Header validation - Default value handling
 */
@Slf4j
public class HeaderExtractor {

  /**
   * Extract correlation ID from headers
   *
   * @param headers Kafka headers
   * @param eventId Event ID for logging
   * @return Correlation ID or generated one if not found
   */
  public static String extractCorrelationId(Map<String, Object> headers, String eventId) {
    Object correlationId = headers.get("X-Correlation-ID");
    if (correlationId == null || correlationId.toString().trim().isEmpty()) {
      String generatedCorrelationId = UUID.randomUUID().toString();
      log.warn(
          "No correlation ID found in headers for event: {}, generated: {}",
          eventId,
          generatedCorrelationId);
      return generatedCorrelationId;
    }
    return correlationId.toString();
  }

  /**
   * Extract tenant ID from headers
   *
   * @param headers Kafka headers
   * @param eventId Event ID for logging
   * @return Tenant ID
   * @throws IllegalArgumentException if tenant ID is not found
   */
  public static String extractTenantId(Map<String, Object> headers, String eventId) {
    Object tenantId = headers.get("X-Tenant-ID");
    if (tenantId == null || tenantId.toString().trim().isEmpty()) {
      log.error("No tenant ID found in headers for event: {}", eventId);
      throw new IllegalArgumentException("Tenant ID is required for payment validation");
    }
    return tenantId.toString();
  }

  /**
   * Extract business unit ID from headers
   *
   * @param headers Kafka headers
   * @param eventId Event ID for logging
   * @return Business unit ID
   * @throws IllegalArgumentException if business unit ID is not found
   */
  public static String extractBusinessUnitId(Map<String, Object> headers, String eventId) {
    Object businessUnitId = headers.get("X-Business-Unit-ID");
    if (businessUnitId == null || businessUnitId.toString().trim().isEmpty()) {
      log.error("No business unit ID found in headers for event: {}", eventId);
      throw new IllegalArgumentException("Business unit ID is required for payment validation");
    }
    return businessUnitId.toString();
  }

  /**
   * Extract source from headers
   *
   * @param headers Kafka headers
   * @return Source or "unknown" if not found
   */
  public static String extractSource(Map<String, Object> headers) {
    Object source = headers.get("X-Source");
    return source != null ? source.toString() : "unknown";
  }

  /**
   * Extract timestamp from headers
   *
   * @param headers Kafka headers
   * @return Timestamp or current time if not found
   */
  public static long extractTimestamp(Map<String, Object> headers) {
    Object timestamp = headers.get("X-Timestamp");
    if (timestamp != null) {
      try {
        return Long.parseLong(timestamp.toString());
      } catch (NumberFormatException e) {
        log.warn("Invalid timestamp in headers: {}", timestamp);
      }
    }
    return System.currentTimeMillis();
  }

  /**
   * Extract all relevant headers
   *
   * @param headers Kafka headers
   * @param eventId Event ID for logging
   * @return HeaderContext with extracted values
   */
  public static HeaderContext extractAllHeaders(Map<String, Object> headers, String eventId) {
    return HeaderContext.builder()
        .correlationId(extractCorrelationId(headers, eventId))
        .tenantId(extractTenantId(headers, eventId))
        .businessUnitId(extractBusinessUnitId(headers, eventId))
        .source(extractSource(headers))
        .timestamp(extractTimestamp(headers))
        .build();
  }

  /** Header Context */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class HeaderContext {
    private String correlationId;
    private String tenantId;
    private String businessUnitId;
    private String source;
    private long timestamp;
  }
}
