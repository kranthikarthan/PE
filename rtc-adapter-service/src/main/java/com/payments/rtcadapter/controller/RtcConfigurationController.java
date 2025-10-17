package com.payments.rtcadapter.controller;

import com.payments.config.ConfigurationManager;
import com.payments.config.SecretManager;
import com.payments.rtcadapter.config.RtcAdapterConfig;
import com.payments.rtcadapter.service.RtcAdapterService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

/**
 * Configuration management controller for RTC adapter
 * Extends shared ConfigurationController with adapter-specific endpoints
 */
@RestController
@RequestMapping("/api/v1/rtc/config")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    name = "payments.config.management.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class RtcConfigurationController {

  private final ConfigurationManager configurationManager;
  private final SecretManager secretManager;
  private final RtcAdapterService rtcAdapterService;
  private final RtcAdapterConfig rtcAdapterConfig;

  @Value("${spring.profiles.active:local}")
  private String activeProfile;

  /** Get RTC adapter configuration */
  @GetMapping("/adapter")
  public Map<String, Object> getAdapterConfiguration() {
    log.debug("Getting RTC adapter configuration");
    
    Map<String, Object> config = new HashMap<>();
    config.put("service.name", "rtc-adapter");
    config.put("service.profile", activeProfile);
    config.put("adapter.type", "RTC");
    config.put("adapter.network", "RTC");
    config.put("adapter.protocol", "ISO 20022, REST API");
    config.put("adapter.endpoint", rtcAdapterConfig.getEndpoint());
    config.put("adapter.timeout", rtcAdapterConfig.getTimeoutSeconds());
    config.put("adapter.retryAttempts", rtcAdapterConfig.getRetryAttempts());
    config.put("adapter.encryptionEnabled", rtcAdapterConfig.getEncryptionEnabled());
    config.put("adapter.apiVersion", rtcAdapterConfig.getApiVersion());
    config.put("adapter.amountLimit", rtcAdapterConfig.getAmountLimit());
    config.put("adapter.processingWindowStart", rtcAdapterConfig.getProcessingWindowStart());
    config.put("adapter.processingWindowEnd", rtcAdapterConfig.getProcessingWindowEnd());
    
    return config;
  }

  /** Get RTC adapter statistics */
  @GetMapping("/adapter/stats")
  public Map<String, Object> getAdapterStatistics() {
    log.debug("Getting RTC adapter statistics");
    
    try {
      long totalAdapters = rtcAdapterService.getAdapterCount();
      long activeAdapters = rtcAdapterService.getActiveAdapterCount();
      
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

  /** Get RTC adapter health */
  @GetMapping("/adapter/health")
  public Map<String, Object> getAdapterHealth() {
    log.debug("Getting RTC adapter health");
    
    boolean endpointConfigured = rtcAdapterConfig.getEndpoint() != null && !rtcAdapterConfig.getEndpoint().isEmpty();
    boolean amountLimitConfigured = rtcAdapterConfig.getAmountLimit() != null && rtcAdapterConfig.getAmountLimit() > 0;
    boolean encryptionConfigured = rtcAdapterConfig.getEncryptionEnabled() != null;
    boolean timeoutConfigured = rtcAdapterConfig.getTimeoutSeconds() != null && rtcAdapterConfig.getTimeoutSeconds() > 0;
    
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

  /** Get RTC adapter endpoints */
  @GetMapping("/adapter/endpoints")
  public Map<String, Object> getAdapterEndpoints() {
    log.debug("Getting RTC adapter endpoints");
    
    return Map.of(
        "primaryEndpoint", rtcAdapterConfig.getEndpoint(),
        "apiVersion", rtcAdapterConfig.getApiVersion(),
        "timeoutSeconds", rtcAdapterConfig.getTimeoutSeconds(),
        "retryAttempts", rtcAdapterConfig.getRetryAttempts(),
        "encryptionEnabled", rtcAdapterConfig.getEncryptionEnabled(),
        "amountLimit", rtcAdapterConfig.getAmountLimit(),
        "processingWindow", Map.of(
            "start", rtcAdapterConfig.getProcessingWindowStart(),
            "end", rtcAdapterConfig.getProcessingWindowEnd()
        )
    );
  }

  /** Get RTC adapter security configuration */
  @GetMapping("/adapter/security")
  public Map<String, Object> getAdapterSecurityConfiguration() {
    log.debug("Getting RTC adapter security configuration");
    
    return Map.of(
        "encryptionEnabled", rtcAdapterConfig.getEncryptionEnabled(),
        "apiVersion", rtcAdapterConfig.getApiVersion(),
        "timeoutSeconds", rtcAdapterConfig.getTimeoutSeconds(),
        "retryAttempts", rtcAdapterConfig.getRetryAttempts(),
        "hasEncryption", rtcAdapterConfig.getEncryptionEnabled() != null && rtcAdapterConfig.getEncryptionEnabled()
    );
  }

  /** Get RTC adapter processing configuration */
  @GetMapping("/adapter/processing")
  public Map<String, Object> getAdapterProcessingConfiguration() {
    log.debug("Getting RTC adapter processing configuration");
    
    return Map.of(
        "processingWindow", Map.of(
            "start", rtcAdapterConfig.getProcessingWindowStart(),
            "end", rtcAdapterConfig.getProcessingWindowEnd()
        ),
        "timeoutSeconds", rtcAdapterConfig.getTimeoutSeconds(),
        "retryAttempts", rtcAdapterConfig.getRetryAttempts(),
        "apiVersion", rtcAdapterConfig.getApiVersion(),
        "endpoint", rtcAdapterConfig.getEndpoint(),
        "amountLimit", rtcAdapterConfig.getAmountLimit()
    );
  }

  /** Get RTC adapter monitoring configuration */
  @GetMapping("/adapter/monitoring")
  public Map<String, Object> getAdapterMonitoringConfiguration() {
    log.debug("Getting RTC adapter monitoring configuration");
    
    return Map.of(
        "metricsEnabled", true,
        "tracingEnabled", true,
        "healthChecksEnabled", true,
        "adapterStats", Map.of(
            "totalAdapters", rtcAdapterService.getAdapterCount(),
            "activeAdapters", rtcAdapterService.getActiveAdapterCount()
        ),
        "profile", activeProfile,
        "timestamp", System.currentTimeMillis()
    );
  }

  /** Get RTC adapter configuration summary */
  @GetMapping("/adapter/summary")
  public Map<String, Object> getAdapterConfigurationSummary() {
    log.debug("Getting RTC adapter configuration summary");
    
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
