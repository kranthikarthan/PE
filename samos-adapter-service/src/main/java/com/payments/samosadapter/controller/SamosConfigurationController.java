package com.payments.samosadapter.controller;

import com.payments.config.ConfigurationController;
import com.payments.config.ConfigurationManager;
import com.payments.config.SecretManager;
import com.payments.samosadapter.config.SamosAdapterConfig;
import com.payments.samosadapter.service.SamosAdapterService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

/**
 * Configuration management controller for SAMOS adapter
 * Extends shared ConfigurationController with adapter-specific endpoints
 */
@RestController
@RequestMapping("/api/v1/samos/config")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    name = "payments.config.management.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class SamosConfigurationController {

  private final ConfigurationManager configurationManager;
  private final SecretManager secretManager;
  private final SamosAdapterService samosAdapterService;
  private final SamosAdapterConfig samosAdapterConfig;

  @Value("${spring.profiles.active:local}")
  private String activeProfile;

  /** Get SAMOS adapter configuration */
  @GetMapping("/adapter")
  public Map<String, Object> getAdapterConfiguration() {
    log.debug("Getting SAMOS adapter configuration");
    
    Map<String, Object> config = new HashMap<>();
    config.put("service.name", "samos-adapter");
    config.put("service.profile", activeProfile);
    config.put("adapter.type", "SAMOS");
    config.put("adapter.network", "SAMOS");
    config.put("adapter.protocol", "ISO 20022");
    config.put("adapter.endpoint", samosAdapterConfig.getEndpoint());
    config.put("adapter.timeout", samosAdapterConfig.getTimeoutSeconds());
    config.put("adapter.retryAttempts", samosAdapterConfig.getRetryAttempts());
    config.put("adapter.encryptionEnabled", samosAdapterConfig.getEncryptionEnabled());
    config.put("adapter.apiVersion", samosAdapterConfig.getApiVersion());
    config.put("adapter.certificatePath", samosAdapterConfig.getCertificatePath());
    
    return config;
  }

  /** Get SAMOS adapter statistics */
  @GetMapping("/adapter/stats")
  public Map<String, Object> getAdapterStatistics() {
    log.debug("Getting SAMOS adapter statistics");
    
    try {
      long totalAdapters = samosAdapterService.getAdapterCount();
      long activeAdapters = samosAdapterService.getActiveAdapterCount();
      
      return Map.of(
          "totalAdapters", totalAdapters,
          "activeAdapters", activeAdapters,
          "inactiveAdapters", totalAdapters - activeAdapters,
          "timestamp", System.currentTimeMillis()
      );
    } catch (Exception e) {
      log.error("Error getting adapter statistics", e);
      return Map.of(
          "error", "Failed to get statistics",
          "message", e.getMessage(),
          "timestamp", System.currentTimeMillis()
      );
    }
  }

  /** Get SAMOS adapter health */
  @GetMapping("/adapter/health")
  public Map<String, Object> getAdapterHealth() {
    log.debug("Getting SAMOS adapter health");
    
    boolean endpointConfigured = samosAdapterConfig.getEndpoint() != null && !samosAdapterConfig.getEndpoint().isEmpty();
    boolean certificateConfigured = samosAdapterConfig.getCertificatePath() != null && !samosAdapterConfig.getCertificatePath().isEmpty();
    boolean encryptionConfigured = samosAdapterConfig.getEncryptionEnabled() != null;
    boolean timeoutConfigured = samosAdapterConfig.getTimeoutSeconds() != null && samosAdapterConfig.getTimeoutSeconds() > 0;
    
    boolean healthy = endpointConfigured && certificateConfigured && encryptionConfigured && timeoutConfigured;
    
    return Map.of(
        "healthy", healthy,
        "endpointConfigured", endpointConfigured,
        "certificateConfigured", certificateConfigured,
        "encryptionConfigured", encryptionConfigured,
        "timeoutConfigured", timeoutConfigured,
        "profile", activeProfile,
        "timestamp", System.currentTimeMillis()
    );
  }

  /** Get SAMOS adapter endpoints */
  @GetMapping("/adapter/endpoints")
  public Map<String, Object> getAdapterEndpoints() {
    log.debug("Getting SAMOS adapter endpoints");
    
    return Map.of(
        "primaryEndpoint", samosAdapterConfig.getEndpoint(),
        "apiVersion", samosAdapterConfig.getApiVersion(),
        "timeoutSeconds", samosAdapterConfig.getTimeoutSeconds(),
        "retryAttempts", samosAdapterConfig.getRetryAttempts(),
        "encryptionEnabled", samosAdapterConfig.getEncryptionEnabled(),
        "certificatePath", samosAdapterConfig.getCertificatePath(),
        "certificatePath", samosAdapterConfig.getCertificatePath()
    );
  }

  /** Get SAMOS adapter security configuration */
  @GetMapping("/adapter/security")
  public Map<String, Object> getAdapterSecurityConfiguration() {
    log.debug("Getting SAMOS adapter security configuration");
    
    return Map.of(
        "encryptionEnabled", samosAdapterConfig.getEncryptionEnabled(),
        "certificatePath", samosAdapterConfig.getCertificatePath(),
        "apiVersion", samosAdapterConfig.getApiVersion(),
        "timeoutSeconds", samosAdapterConfig.getTimeoutSeconds(),
        "retryAttempts", samosAdapterConfig.getRetryAttempts(),
        "hasCertificate", samosAdapterConfig.getCertificatePath() != null && !samosAdapterConfig.getCertificatePath().isEmpty(),
        "hasEncryption", samosAdapterConfig.getEncryptionEnabled() != null && samosAdapterConfig.getEncryptionEnabled()
    );
  }

  /** Get SAMOS adapter processing configuration */
  @GetMapping("/adapter/processing")
  public Map<String, Object> getAdapterProcessingConfiguration() {
    log.debug("Getting SAMOS adapter processing configuration");
    
    return Map.of(
        "certificatePath", samosAdapterConfig.getCertificatePath(),
        "timeoutSeconds", samosAdapterConfig.getTimeoutSeconds(),
        "retryAttempts", samosAdapterConfig.getRetryAttempts(),
        "apiVersion", samosAdapterConfig.getApiVersion(),
        "endpoint", samosAdapterConfig.getEndpoint()
    );
  }

  /** Get SAMOS adapter monitoring configuration */
  @GetMapping("/adapter/monitoring")
  public Map<String, Object> getAdapterMonitoringConfiguration() {
    log.debug("Getting SAMOS adapter monitoring configuration");
    
    return Map.of(
        "metricsEnabled", true,
        "tracingEnabled", true,
        "healthChecksEnabled", true,
        "adapterStats", Map.of(
            "totalAdapters", samosAdapterService.getAdapterCount(),
            "activeAdapters", samosAdapterService.getActiveAdapterCount()
        ),
        "profile", activeProfile,
        "timestamp", System.currentTimeMillis()
    );
  }

  /** Get SAMOS adapter configuration summary */
  @GetMapping("/adapter/summary")
  public Map<String, Object> getAdapterConfigurationSummary() {
    log.debug("Getting SAMOS adapter configuration summary");
    
    return Map.of(
        "adapter", getAdapterConfiguration(),
        "statistics", getAdapterStatistics(),
        "health", getAdapterHealth(),
        "endpoints", getAdapterEndpoints(),
        "security", getAdapterSecurityConfiguration(),
        "processing", getAdapterProcessingConfiguration(),
        "monitoring", getAdapterMonitoringConfiguration(),
        "timestamp", System.currentTimeMillis()
    );
  }
}
