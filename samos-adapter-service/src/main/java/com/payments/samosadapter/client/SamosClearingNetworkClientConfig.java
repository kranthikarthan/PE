package com.payments.samosadapter.client;

import feign.*;
import feign.codec.ErrorDecoder;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SAMOS Clearing Network Client Configuration
 *
 * <p>Feign client configuration for SAMOS clearing network with enterprise-grade features:
 *
 * <ul>
 *   <li>mTLS integration (see SamosMtlsConfig)
 *   <li>Request interceptors for common headers
 *   <li>Structured logging with correlation IDs
 *   <li>Timeout configuration optimized for RTGS
 *   <li>Custom error handling
 *   <li>Connection pooling
 *   <li>Retry logic (handled by Resilience4j at service layer)
 * </ul>
 */
@Slf4j
@Configuration
public class SamosClearingNetworkClientConfig {

  @Value("${samos.clearing.network.connect.timeout:15}")
  private int connectTimeoutSeconds;

  @Value("${samos.clearing.network.read.timeout:60}")
  private int readTimeoutSeconds;

  @Value("${samos.clearing.network.follow.redirects:true}")
  private boolean followRedirects;

  /**
   * Feign logging level
   *
   * <p>FULL logging for SAMOS due to regulatory requirements (audit trail for RTGS transactions)
   */
  @Bean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL; // FULL for compliance/audit
  }

  /**
   * Request interceptor for adding common headers
   *
   * <p>Adds required headers for SAMOS clearing network: - Content negotiation - User agent
   * identification - Service metadata - Correlation ID (if not present)
   */
  @Bean
  public RequestInterceptor requestInterceptor() {
    return template -> {
      // Content type headers
      template.header("Content-Type", "application/json; charset=UTF-8");
      template.header("Accept", "application/json");

      // Service identification (required by SARB)
      template.header("User-Agent", "PaymentEngine-SAMOS-Adapter/1.0.0");
      template.header("X-Service-Name", "samos-adapter-service");
      template.header("X-Service-Version", "1.0.0");

      // Ensure correlation ID is present (required for SARB tracing)
      if (template.headers().get("X-Correlation-ID") == null) {
        String correlationId = UUID.randomUUID().toString();
        template.header("X-Correlation-ID", correlationId);
        log.debug("Generated correlation ID: {}", correlationId);
      }

      // Log request for audit trail
      log.debug(
          "SAMOS Request: {} {} [Correlation-ID: {}]",
          template.method(),
          template.url(),
          template.headers().get("X-Correlation-ID"));
    };
  }

  /**
   * Request options for timeout configuration
   *
   * <p>Optimized for SAMOS RTGS network: - Connect timeout: 15s (network latency) - Read timeout:
   * 60s (RTGS processing time) - Follow redirects: enabled (for load balancing)
   */
  @Bean
  public Request.Options requestOptions() {
    return new Request.Options(
        connectTimeoutSeconds,
        TimeUnit.SECONDS,
        readTimeoutSeconds,
        TimeUnit.SECONDS,
        followRedirects);
  }

  /**
   * Custom error decoder for SAMOS-specific errors
   *
   * <p>Handles SARB error codes and maps them to appropriate exceptions.
   */
  @Bean
  public ErrorDecoder errorDecoder() {
    return new SamosErrorDecoder();
  }

  /**
   * Retryer configuration
   *
   * <p>Disabled at Feign level - retries handled by Resilience4j at service layer for better
   * control
   */
  @Bean
  public Retryer retryer() {
    return Retryer.NEVER_RETRY; // Use Resilience4j retry instead
  }

  /** Custom request logger for SAMOS compliance */
  @Bean
  public Logger feignLogger() {
    return new SamosComplianceLogger();
  }

  /** SAMOS-specific Error Decoder */
  private static class SamosErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
      log.error(
          "SAMOS API error - Method: {}, Status: {}, Reason: {}",
          methodKey,
          response.status(),
          response.reason());

      // Map SARB-specific HTTP status codes
      switch (response.status()) {
        case 400:
          return new SamosBadRequestException("Invalid SAMOS request: " + response.reason());
        case 401:
          return new SamosAuthenticationException("SAMOS authentication failed");
        case 403:
          return new SamosAuthorizationException("Not authorized for SAMOS operation");
        case 408:
          return new SamosTimeoutException("SAMOS request timeout");
        case 422:
          return new SamosValidationException("SAMOS validation failed: " + response.reason());
        case 429:
          return new SamosRateLimitException("SAMOS rate limit exceeded");
        case 500:
        case 502:
        case 503:
        case 504:
          return new SamosNetworkException("SAMOS network error: " + response.reason());
        default:
          return defaultDecoder.decode(methodKey, response);
      }
    }
  }

  /** SAMOS Compliance Logger - structured logging for audit */
  private static class SamosComplianceLogger extends Logger {
    @Override
    protected void log(String configKey, String format, Object... args) {
      log.info("[SAMOS-AUDIT] " + configKey + " - " + format, args);
    }
  }

  // SAMOS-specific exceptions

  public static class SamosBadRequestException extends FeignException {
    public SamosBadRequestException(String message) {
      super(400, message);
    }
  }

  public static class SamosAuthenticationException extends FeignException {
    public SamosAuthenticationException(String message) {
      super(401, message);
    }
  }

  public static class SamosAuthorizationException extends FeignException {
    public SamosAuthorizationException(String message) {
      super(403, message);
    }
  }

  public static class SamosTimeoutException extends FeignException {
    public SamosTimeoutException(String message) {
      super(408, message);
    }
  }

  public static class SamosValidationException extends FeignException {
    public SamosValidationException(String message) {
      super(422, message);
    }
  }

  public static class SamosRateLimitException extends FeignException {
    public SamosRateLimitException(String message) {
      super(429, message);
    }
  }

  public static class SamosNetworkException extends FeignException {
    public SamosNetworkException(String message) {
      super(503, message);
    }
  }
}
