package com.payments.swiftadapter.controller;

import com.payments.config.ConfigurationManager;
import com.payments.config.SecretManager;
import com.payments.swiftadapter.config.SwiftAdapterConfig;
import com.payments.swiftadapter.service.SwiftAdapterService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

/**
 * Configuration management controller for SWIFT adapter Extends shared ConfigurationController with
 * adapter-specific endpoints
 */
@RestController
@RequestMapping("/api/v1/swift/config")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    name = "payments.config.management.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class SwiftConfigurationController {

  private final ConfigurationManager configurationManager;
  private final SecretManager secretManager;
  private final SwiftAdapterService swiftAdapterService;
  private final SwiftAdapterConfig swiftAdapterConfig;

  @Value("${spring.profiles.active:local}")
  private String activeProfile;

  /** Get SWIFT adapter configuration */
  @GetMapping("/adapter")
  public Map<String, Object> getAdapterConfiguration() {
    log.debug("Getting SWIFT adapter configuration");

    Map<String, Object> config = new HashMap<>();
    config.put("service.name", "swift-adapter");
    config.put("service.profile", activeProfile);
    config.put("adapter.type", "SWIFT");
    config.put("adapter.network", "SWIFT");
    config.put("adapter.protocol", "MT103, pacs.008, pacs.002");
    config.put("adapter.endpoint", swiftAdapterConfig.getEndpoint());
    config.put("adapter.timeout", swiftAdapterConfig.getTimeoutSeconds());
    config.put("adapter.retryAttempts", swiftAdapterConfig.getRetryAttempts());
    config.put("adapter.encryptionEnabled", swiftAdapterConfig.getEncryptionEnabled());
    config.put("adapter.apiVersion", swiftAdapterConfig.getApiVersion());
    config.put("adapter.processingWindowStart", swiftAdapterConfig.getProcessingWindowStart());
    config.put("adapter.processingWindowEnd", swiftAdapterConfig.getProcessingWindowEnd());

    return config;
  }

  /** Get SWIFT adapter statistics */
  @GetMapping("/adapter/stats")
  public Map<String, Object> getAdapterStatistics() {
    log.debug("Getting SWIFT adapter statistics");

    try {
      long totalAdapters = swiftAdapterService.getAdapterCount();
      long activeAdapters = swiftAdapterService.getActiveAdapterCount();

      return Map.of(
          "totalAdapters",
          totalAdapters,
          "activeAdapters",
          activeAdapters,
          "inactiveAdapters",
          totalAdapters - activeAdapters,
          "timestamp",
          System.currentTimeMillis());
    } catch (Exception e) {
      log.error("Error getting adapter statistics", e);
      return Map.of(
          "error", "Failed to get statistics",
          "message", e.getMessage(),
          "timestamp", System.currentTimeMillis());
    }
  }

  /** Get SWIFT adapter health */
  @GetMapping("/adapter/health")
  public Map<String, Object> getAdapterHealth() {
    log.debug("Getting SWIFT adapter health");

    boolean endpointConfigured =
        swiftAdapterConfig.getEndpoint() != null && !swiftAdapterConfig.getEndpoint().isEmpty();
    boolean encryptionConfigured = swiftAdapterConfig.getEncryptionEnabled() != null;
    boolean timeoutConfigured =
        swiftAdapterConfig.getTimeoutSeconds() != null
            && swiftAdapterConfig.getTimeoutSeconds() > 0;
    boolean processingWindowConfigured =
        swiftAdapterConfig.getProcessingWindowStart() != null
            && swiftAdapterConfig.getProcessingWindowEnd() != null;

    boolean healthy =
        endpointConfigured
            && encryptionConfigured
            && timeoutConfigured
            && processingWindowConfigured;

    return Map.of(
        "healthy", healthy,
        "endpointConfigured", endpointConfigured,
        "encryptionConfigured", encryptionConfigured,
        "timeoutConfigured", timeoutConfigured,
        "processingWindowConfigured", processingWindowConfigured,
        "profile", activeProfile,
        "timestamp", System.currentTimeMillis());
  }

  /** Get SWIFT adapter endpoints */
  @GetMapping("/adapter/endpoints")
  public Map<String, Object> getAdapterEndpoints() {
    log.debug("Getting SWIFT adapter endpoints");

    return Map.of(
        "primaryEndpoint", swiftAdapterConfig.getEndpoint(),
        "apiVersion", swiftAdapterConfig.getApiVersion(),
        "timeoutSeconds", swiftAdapterConfig.getTimeoutSeconds(),
        "retryAttempts", swiftAdapterConfig.getRetryAttempts(),
        "encryptionEnabled", swiftAdapterConfig.getEncryptionEnabled(),
        "processingWindow",
            Map.of(
                "start", swiftAdapterConfig.getProcessingWindowStart(),
                "end", swiftAdapterConfig.getProcessingWindowEnd()));
  }

  /** Get SWIFT adapter security configuration */
  @GetMapping("/adapter/security")
  public Map<String, Object> getAdapterSecurityConfiguration() {
    log.debug("Getting SWIFT adapter security configuration");

    return Map.of(
        "encryptionEnabled", swiftAdapterConfig.getEncryptionEnabled(),
        "apiVersion", swiftAdapterConfig.getApiVersion(),
        "timeoutSeconds", swiftAdapterConfig.getTimeoutSeconds(),
        "retryAttempts", swiftAdapterConfig.getRetryAttempts(),
        "hasEncryption",
            swiftAdapterConfig.getEncryptionEnabled() != null
                && swiftAdapterConfig.getEncryptionEnabled());
  }

  /** Get SWIFT adapter processing configuration */
  @GetMapping("/adapter/processing")
  public Map<String, Object> getAdapterProcessingConfiguration() {
    log.debug("Getting SWIFT adapter processing configuration");

    return Map.of(
        "processingWindow",
            Map.of(
                "start", swiftAdapterConfig.getProcessingWindowStart(),
                "end", swiftAdapterConfig.getProcessingWindowEnd()),
        "timeoutSeconds", swiftAdapterConfig.getTimeoutSeconds(),
        "retryAttempts", swiftAdapterConfig.getRetryAttempts(),
        "apiVersion", swiftAdapterConfig.getApiVersion(),
        "endpoint", swiftAdapterConfig.getEndpoint());
  }

  /** Get SWIFT adapter monitoring configuration */
  @GetMapping("/adapter/monitoring")
  public Map<String, Object> getAdapterMonitoringConfiguration() {
    log.debug("Getting SWIFT adapter monitoring configuration");

    return Map.of(
        "metricsEnabled",
        true,
        "tracingEnabled",
        true,
        "healthChecksEnabled",
        true,
        "adapterStats",
        Map.of(
            "totalAdapters", swiftAdapterService.getAdapterCount(),
            "activeAdapters", swiftAdapterService.getActiveAdapterCount()),
        "profile",
        activeProfile,
        "timestamp",
        System.currentTimeMillis());
  }

  /** Get SWIFT adapter configuration summary */
  @GetMapping("/adapter/summary")
  public Map<String, Object> getAdapterConfigurationSummary() {
    log.debug("Getting SWIFT adapter configuration summary");

    return Map.of(
        "adapter", getAdapterConfiguration(),
        "statistics", getAdapterStatistics(),
        "health", getAdapterHealth(),
        "endpoints", getAdapterEndpoints(),
        "security", getAdapterSecurityConfiguration(),
        "processing", getAdapterProcessingConfiguration(),
        "monitoring", getAdapterMonitoringConfiguration(),
        "timestamp", System.currentTimeMillis());
  }
}
