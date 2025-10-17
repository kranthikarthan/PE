package com.payments.samosadapter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * SAMOS Environment Configuration
 *
 * <p>Environment-specific configuration for SAMOS adapter: - Development environment settings -
 * Staging environment settings - Production environment settings - Environment-specific endpoints
 * and credentials
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "samos.environment")
public class SamosEnvironmentConfig {

  private String environment;
  private String region;
  private String dataCenter;
  private boolean debugMode;
  private boolean metricsEnabled;
  private boolean tracingEnabled;
  private String logLevel;
  private String timezone;
  private String currency;
  private String locale;

  // Development environment settings
  private Development development = new Development();

  // Staging environment settings
  private Staging staging = new Staging();

  // Production environment settings
  private Production production = new Production();

  @Data
  public static class Development {
    private String endpoint;
    private String apiVersion;
    private int timeoutSeconds;
    private int retryAttempts;
    private boolean encryptionEnabled;
    private String certificatePath;
    private String logLevel;
    private boolean mockMode;
    private String mockEndpoint;
  }

  @Data
  public static class Staging {
    private String endpoint;
    private String apiVersion;
    private int timeoutSeconds;
    private int retryAttempts;
    private boolean encryptionEnabled;
    private String certificatePath;
    private String logLevel;
    private boolean mockMode;
    private String mockEndpoint;
  }

  @Data
  public static class Production {
    private String endpoint;
    private String apiVersion;
    private int timeoutSeconds;
    private int retryAttempts;
    private boolean encryptionEnabled;
    private String certificatePath;
    private String logLevel;
    private boolean mockMode;
    private String mockEndpoint;
  }

  /** Get environment-specific configuration */
  public EnvironmentSettings getEnvironmentSettings() {
    switch (environment.toLowerCase()) {
      case "development":
      case "dev":
        return EnvironmentSettings.builder()
            .endpoint(development.getEndpoint())
            .apiVersion(development.getApiVersion())
            .timeoutSeconds(development.getTimeoutSeconds())
            .retryAttempts(development.getRetryAttempts())
            .encryptionEnabled(development.isEncryptionEnabled())
            .certificatePath(development.getCertificatePath())
            .logLevel(development.getLogLevel())
            .mockMode(development.isMockMode())
            .mockEndpoint(development.getMockEndpoint())
            .build();
      case "staging":
      case "stage":
        return EnvironmentSettings.builder()
            .endpoint(staging.getEndpoint())
            .apiVersion(staging.getApiVersion())
            .timeoutSeconds(staging.getTimeoutSeconds())
            .retryAttempts(staging.getRetryAttempts())
            .encryptionEnabled(staging.isEncryptionEnabled())
            .certificatePath(staging.getCertificatePath())
            .logLevel(staging.getLogLevel())
            .mockMode(staging.isMockMode())
            .mockEndpoint(staging.getMockEndpoint())
            .build();
      case "production":
      case "prod":
        return EnvironmentSettings.builder()
            .endpoint(production.getEndpoint())
            .apiVersion(production.getApiVersion())
            .timeoutSeconds(production.getTimeoutSeconds())
            .retryAttempts(production.getRetryAttempts())
            .encryptionEnabled(production.isEncryptionEnabled())
            .certificatePath(production.getCertificatePath())
            .logLevel(production.getLogLevel())
            .mockMode(production.isMockMode())
            .mockEndpoint(production.getMockEndpoint())
            .build();
      default:
        throw new IllegalArgumentException("Unknown environment: " + environment);
    }
  }

  @lombok.Builder
  @lombok.Data
  @lombok.AllArgsConstructor
  @lombok.NoArgsConstructor
  public static class EnvironmentSettings {
    private String endpoint;
    private String apiVersion;
    private int timeoutSeconds;
    private int retryAttempts;
    private boolean encryptionEnabled;
    private String certificatePath;
    private String logLevel;
    private boolean mockMode;
    private String mockEndpoint;
  }
}
