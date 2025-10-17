package com.payments.payshapadapter.controller;

import com.payments.config.ConfigurationManager;
import com.payments.config.SecretManager;
import com.payments.payshapadapter.config.PayShapAdapterConfig;
import com.payments.payshapadapter.service.PayShapAdapterService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

/**
 * Configuration management controller for PayShap adapter
 * Extends shared ConfigurationController with adapter-specific endpoints
 */
@RestController
@RequestMapping("/api/v1/payshap/config")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    name = "payments.config.management.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class PayShapConfigurationController {

  private final ConfigurationManager configurationManager;
  private final SecretManager secretManager;
  private final PayShapAdapterService payShapAdapterService;
  private final PayShapAdapterConfig payShapAdapterConfig;

  @Value("${spring.profiles.active:local}")
  private String activeProfile;

  /** Get PayShap adapter configuration */
  @GetMapping("/adapter")
  public Map<String, Object> getAdapterConfiguration() {
    log.debug("Getting PayShap adapter configuration");
    
    Map<String, Object> config = new HashMap<>();
    config.put("service.name", "payshap-adapter");
    config.put("service.profile", activeProfile);
    config.put("adapter.type", "PayShap");
    config.put("adapter.network", "PayShap");
    config.put("adapter.protocol", "ISO 20022, Proxy Registry");
    config.put("adapter.endpoint", payShapAdapterConfig.getEndpoint());
    config.put("adapter.timeout", payShapAdapterConfig.getTimeoutSeconds());
    config.put("adapter.retryAttempts", payShapAdapterConfig.getRetryAttempts());
    config.put("adapter.encryptionEnabled", payShapAdapterConfig.getEncryptionEnabled());
    config.put("adapter.apiVersion", payShapAdapterConfig.getApiVersion());
    config.put("adapter.amountLimit", payShapAdapterConfig.getAmountLimit());
    config.put("adapter.processingWindowStart", payShapAdapterConfig.getProcessingWindowStart());
    config.put("adapter.processingWindowEnd", payShapAdapterConfig.getProcessingWindowEnd());
    
    return config;
  }

  /** Get PayShap adapter statistics */
  @GetMapping("/adapter/stats")
  public Map<String, Object> getAdapterStatistics() {
    log.debug("Getting PayShap adapter statistics");
    
    try {
      long totalAdapters = payShapAdapterService.getAdapterCount();
      long activeAdapters = payShapAdapterService.getActiveAdapterCount();
      
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

  /** Get PayShap adapter health */
  @GetMapping("/adapter/health")
  public Map<String, Object> getAdapterHealth() {
    log.debug("Getting PayShap adapter health");
    
    boolean endpointConfigured = payShapAdapterConfig.getEndpoint() != null && !payShapAdapterConfig.getEndpoint().isEmpty();
    boolean amountLimitConfigured = payShapAdapterConfig.getAmountLimit() != null && payShapAdapterConfig.getAmountLimit() > 0;
    boolean encryptionConfigured = payShapAdapterConfig.getEncryptionEnabled() != null;
    boolean timeoutConfigured = payShapAdapterConfig.getTimeoutSeconds() != null && payShapAdapterConfig.getTimeoutSeconds() > 0;
    
    boolean healthy = endpointConfigured && amountLimitConfigured && encryptionConfigured && timeoutConfigured;
    
    return Map.of(
        "healthy", healthy,
        "endpointConfigured", endpointConfigured,
        "amountLimitConfigured", amountLimitConfigured,
        "encryptionConfigured", encryptionConfigured,
        "timeoutConfigured", timeoutConfigured,
        "profile", activeProfile,
        "timestamp", System.currentTimeMillis()
    );
  }

  /** Get PayShap adapter endpoints */
  @GetMapping("/adapter/endpoints")
  public Map<String, Object> getAdapterEndpoints() {
    log.debug("Getting PayShap adapter endpoints");
    
    return Map.of(
        "primaryEndpoint", payShapAdapterConfig.getEndpoint(),
        "apiVersion", payShapAdapterConfig.getApiVersion(),
        "timeoutSeconds", payShapAdapterConfig.getTimeoutSeconds(),
        "retryAttempts", payShapAdapterConfig.getRetryAttempts(),
        "encryptionEnabled", payShapAdapterConfig.getEncryptionEnabled(),
        "amountLimit", payShapAdapterConfig.getAmountLimit(),
        "processingWindow", Map.of(
            "start", payShapAdapterConfig.getProcessingWindowStart(),
            "end", payShapAdapterConfig.getProcessingWindowEnd()
        )
    );
  }

  /** Get PayShap adapter security configuration */
  @GetMapping("/adapter/security")
  public Map<String, Object> getAdapterSecurityConfiguration() {
    log.debug("Getting PayShap adapter security configuration");
    
    return Map.of(
        "encryptionEnabled", payShapAdapterConfig.getEncryptionEnabled(),
        "apiVersion", payShapAdapterConfig.getApiVersion(),
        "timeoutSeconds", payShapAdapterConfig.getTimeoutSeconds(),
        "retryAttempts", payShapAdapterConfig.getRetryAttempts(),
        "hasEncryption", payShapAdapterConfig.getEncryptionEnabled() != null && payShapAdapterConfig.getEncryptionEnabled()
    );
  }

  /** Get PayShap adapter processing configuration */
  @GetMapping("/adapter/processing")
  public Map<String, Object> getAdapterProcessingConfiguration() {
    log.debug("Getting PayShap adapter processing configuration");
    
    return Map.of(
        "processingWindow", Map.of(
            "start", payShapAdapterConfig.getProcessingWindowStart(),
            "end", payShapAdapterConfig.getProcessingWindowEnd()
        ),
        "timeoutSeconds", payShapAdapterConfig.getTimeoutSeconds(),
        "retryAttempts", payShapAdapterConfig.getRetryAttempts(),
        "apiVersion", payShapAdapterConfig.getApiVersion(),
        "endpoint", payShapAdapterConfig.getEndpoint(),
        "amountLimit", payShapAdapterConfig.getAmountLimit()
    );
  }

  /** Get PayShap adapter monitoring configuration */
  @GetMapping("/adapter/monitoring")
  public Map<String, Object> getAdapterMonitoringConfiguration() {
    log.debug("Getting PayShap adapter monitoring configuration");
    
    return Map.of(
        "metricsEnabled", true,
        "tracingEnabled", true,
        "healthChecksEnabled", true,
        "adapterStats", Map.of(
            "totalAdapters", payShapAdapterService.getAdapterCount(),
            "activeAdapters", payShapAdapterService.getActiveAdapterCount()
        ),
        "profile", activeProfile,
        "timestamp", System.currentTimeMillis()
    );
  }

  /** Get PayShap adapter configuration summary */
  @GetMapping("/adapter/summary")
  public Map<String, Object> getAdapterConfigurationSummary() {
    log.debug("Getting PayShap adapter configuration summary");
    
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
