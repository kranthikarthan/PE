package com.payments.bankservafricaadapter.controller;

import com.payments.bankservafricaadapter.config.BankservAfricaAdapterConfig;
import com.payments.bankservafricaadapter.service.BankservAfricaAdapterService;
import com.payments.config.ConfigurationManager;
import com.payments.config.SecretManager;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

/**
 * Configuration management controller for BankservAfrica adapter Extends shared
 * ConfigurationController with adapter-specific endpoints
 */
@RestController
@RequestMapping("/api/v1/bankservafrica/config")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    name = "payments.config.management.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class BankservAfricaConfigurationController {

  private final ConfigurationManager configurationManager;
  private final SecretManager secretManager;
  private final BankservAfricaAdapterService bankservAfricaAdapterService;
  private final BankservAfricaAdapterConfig bankservAfricaAdapterConfig;

  @Value("${spring.profiles.active:local}")
  private String activeProfile;

  /** Get BankservAfrica adapter configuration */
  @GetMapping("/adapter")
  public Map<String, Object> getAdapterConfiguration() {
    log.debug("Getting BankservAfrica adapter configuration");

    Map<String, Object> config = new HashMap<>();
    config.put("service.name", "bankservafrica-adapter");
    config.put("service.profile", activeProfile);
    config.put("adapter.type", "BankservAfrica");
    config.put("adapter.network", "BankservAfrica");
    config.put("adapter.protocol", "EFT Batch, ISO 8583, ACH");
    config.put("adapter.endpoint", bankservAfricaAdapterConfig.getEndpoint());
    config.put("adapter.timeout", bankservAfricaAdapterConfig.getTimeoutSeconds());
    config.put("adapter.retryAttempts", bankservAfricaAdapterConfig.getRetryAttempts());
    config.put("adapter.encryptionEnabled", bankservAfricaAdapterConfig.getEncryptionEnabled());
    config.put("adapter.apiVersion", bankservAfricaAdapterConfig.getApiVersion());
    config.put("adapter.batchSize", bankservAfricaAdapterConfig.getBatchSize());
    config.put(
        "adapter.processingWindowStart", bankservAfricaAdapterConfig.getProcessingWindowStart());
    config.put("adapter.processingWindowEnd", bankservAfricaAdapterConfig.getProcessingWindowEnd());

    return config;
  }

  /** Get BankservAfrica adapter statistics */
  @GetMapping("/adapter/stats")
  public Map<String, Object> getAdapterStatistics() {
    log.debug("Getting BankservAfrica adapter statistics");

    try {
      long totalAdapters = bankservAfricaAdapterService.getAdapterCount();
      long activeAdapters = bankservAfricaAdapterService.getActiveAdapterCount();

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

  /** Get BankservAfrica adapter health */
  @GetMapping("/adapter/health")
  public Map<String, Object> getAdapterHealth() {
    log.debug("Getting BankservAfrica adapter health");

    boolean endpointConfigured =
        bankservAfricaAdapterConfig.getEndpoint() != null
            && !bankservAfricaAdapterConfig.getEndpoint().isEmpty();
    boolean batchSizeConfigured =
        bankservAfricaAdapterConfig.getBatchSize() != null
            && bankservAfricaAdapterConfig.getBatchSize() > 0;
    boolean encryptionConfigured = bankservAfricaAdapterConfig.getEncryptionEnabled() != null;
    boolean timeoutConfigured =
        bankservAfricaAdapterConfig.getTimeoutSeconds() != null
            && bankservAfricaAdapterConfig.getTimeoutSeconds() > 0;

    boolean healthy =
        endpointConfigured && batchSizeConfigured && encryptionConfigured && timeoutConfigured;

    return Map.of(
        "healthy", healthy,
        "endpointConfigured", endpointConfigured,
        "batchSizeConfigured", batchSizeConfigured,
        "encryptionConfigured", encryptionConfigured,
        "timeoutConfigured", timeoutConfigured,
        "profile", activeProfile,
        "timestamp", System.currentTimeMillis());
  }

  /** Get BankservAfrica adapter endpoints */
  @GetMapping("/adapter/endpoints")
  public Map<String, Object> getAdapterEndpoints() {
    log.debug("Getting BankservAfrica adapter endpoints");

    return Map.of(
        "primaryEndpoint", bankservAfricaAdapterConfig.getEndpoint(),
        "apiVersion", bankservAfricaAdapterConfig.getApiVersion(),
        "timeoutSeconds", bankservAfricaAdapterConfig.getTimeoutSeconds(),
        "retryAttempts", bankservAfricaAdapterConfig.getRetryAttempts(),
        "encryptionEnabled", bankservAfricaAdapterConfig.getEncryptionEnabled(),
        "batchSize", bankservAfricaAdapterConfig.getBatchSize(),
        "processingWindow",
            Map.of(
                "start", bankservAfricaAdapterConfig.getProcessingWindowStart(),
                "end", bankservAfricaAdapterConfig.getProcessingWindowEnd()));
  }

  /** Get BankservAfrica adapter security configuration */
  @GetMapping("/adapter/security")
  public Map<String, Object> getAdapterSecurityConfiguration() {
    log.debug("Getting BankservAfrica adapter security configuration");

    return Map.of(
        "encryptionEnabled", bankservAfricaAdapterConfig.getEncryptionEnabled(),
        "apiVersion", bankservAfricaAdapterConfig.getApiVersion(),
        "timeoutSeconds", bankservAfricaAdapterConfig.getTimeoutSeconds(),
        "retryAttempts", bankservAfricaAdapterConfig.getRetryAttempts(),
        "hasEncryption",
            bankservAfricaAdapterConfig.getEncryptionEnabled() != null
                && bankservAfricaAdapterConfig.getEncryptionEnabled());
  }

  /** Get BankservAfrica adapter processing configuration */
  @GetMapping("/adapter/processing")
  public Map<String, Object> getAdapterProcessingConfiguration() {
    log.debug("Getting BankservAfrica adapter processing configuration");

    return Map.of(
        "processingWindow",
            Map.of(
                "start", bankservAfricaAdapterConfig.getProcessingWindowStart(),
                "end", bankservAfricaAdapterConfig.getProcessingWindowEnd()),
        "timeoutSeconds", bankservAfricaAdapterConfig.getTimeoutSeconds(),
        "retryAttempts", bankservAfricaAdapterConfig.getRetryAttempts(),
        "apiVersion", bankservAfricaAdapterConfig.getApiVersion(),
        "endpoint", bankservAfricaAdapterConfig.getEndpoint(),
        "batchSize", bankservAfricaAdapterConfig.getBatchSize());
  }

  /** Get BankservAfrica adapter monitoring configuration */
  @GetMapping("/adapter/monitoring")
  public Map<String, Object> getAdapterMonitoringConfiguration() {
    log.debug("Getting BankservAfrica adapter monitoring configuration");

    return Map.of(
        "metricsEnabled",
        true,
        "tracingEnabled",
        true,
        "healthChecksEnabled",
        true,
        "adapterStats",
        Map.of(
            "totalAdapters", bankservAfricaAdapterService.getAdapterCount(),
            "activeAdapters", bankservAfricaAdapterService.getActiveAdapterCount()),
        "profile",
        activeProfile,
        "timestamp",
        System.currentTimeMillis());
  }

  /** Get BankservAfrica adapter configuration summary */
  @GetMapping("/adapter/summary")
  public Map<String, Object> getAdapterConfigurationSummary() {
    log.debug("Getting BankservAfrica adapter configuration summary");

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
