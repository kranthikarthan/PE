package com.payments.config;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** Central configuration manager */
@Configuration
@EnableConfigurationProperties(PaymentsConfigurationProperties.class)
@RequiredArgsConstructor
@Slf4j
public class ConfigurationManager {

  private final Environment environment;
  private final SecretManager secretManager;

  @Value("${spring.profiles.active:local}")
  private String activeProfile;

  @Bean
  @RefreshScope
  public PaymentsConfigurationProperties configurationProperties() {
    return new PaymentsConfigurationProperties();
  }

  /** Get configuration for a specific service */
  public Map<String, Object> getServiceConfiguration(String serviceName) {
    Map<String, Object> config = new HashMap<>();

    // Add common configuration
    config.put("service.name", serviceName);
    config.put("service.profile", activeProfile);
    config.put("service.environment", environment.getProperty("payments.profiles.active", "local"));

    // Add database configuration
    config.put(
        "database.url",
        getSecretValue("DATABASE_URL", "jdbc:postgresql://localhost:5432/payments"));
    config.put("database.username", getSecretValue("DATABASE_USERNAME", "payments"));
    config.put("database.password", getSecretValue("DATABASE_PASSWORD", "payments"));

    // Add cache configuration
    config.put("cache.host", getSecretValue("REDIS_HOST", "localhost"));
    config.put("cache.port", Integer.parseInt(getSecretValue("REDIS_PORT", "6379")));
    config.put("cache.password", getSecretValue("REDIS_PASSWORD", ""));

    // Add messaging configuration
    config.put(
        "kafka.bootstrap.servers", getSecretValue("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"));
    config.put("kafka.group.id", getSecretValue("KAFKA_GROUP_ID", "payments-engine"));

    // Add security configuration
    config.put("jwt.secret", getSecretValue("JWT_SECRET", "default-secret"));
    config.put("oauth2.client.id", getSecretValue("OAUTH2_CLIENT_ID", "payments-engine"));
    config.put("oauth2.client.secret", getSecretValue("OAUTH2_CLIENT_SECRET", "default-secret"));

    // Add telemetry configuration
    config.put("telemetry.tracing.enabled", getBooleanValue("TELEMETRY_TRACING_ENABLED", true));
    config.put("telemetry.metrics.enabled", getBooleanValue("TELEMETRY_METRICS_ENABLED", true));
    config.put("telemetry.logging.enabled", getBooleanValue("TELEMETRY_LOGGING_ENABLED", true));

    return config;
  }

  /** Get environment-specific configuration */
  public Map<String, Object> getEnvironmentConfiguration() {
    Map<String, Object> config = new HashMap<>();

    config.put("environment", activeProfile);
    config.put("host", getSecretValue("HOST", "localhost"));
    config.put("port", Integer.parseInt(getSecretValue("PORT", "8080")));
    config.put("context.path", getSecretValue("CONTEXT_PATH", "/"));

    // Add environment-specific overrides
    switch (activeProfile.toLowerCase()) {
      case "local":
        config.put("debug.enabled", true);
        config.put("logging.level", "DEBUG");
        break;
      case "dev":
        config.put("debug.enabled", true);
        config.put("logging.level", "INFO");
        break;
      case "staging":
        config.put("debug.enabled", false);
        config.put("logging.level", "WARN");
        break;
      case "prod":
        config.put("debug.enabled", false);
        config.put("logging.level", "ERROR");
        break;
    }

    return config;
  }

  /** Get database configuration */
  public Map<String, Object> getDatabaseConfiguration() {
    Map<String, Object> config = new HashMap<>();

    config.put("url", getSecretValue("DATABASE_URL", "jdbc:postgresql://localhost:5432/payments"));
    config.put("username", getSecretValue("DATABASE_USERNAME", "payments"));
    config.put("password", getSecretValue("DATABASE_PASSWORD", "payments"));
    config.put("driverClassName", "org.postgresql.Driver");
    config.put("maximumPoolSize", Integer.parseInt(getSecretValue("DATABASE_MAX_POOL_SIZE", "20")));
    config.put("minimumIdle", Integer.parseInt(getSecretValue("DATABASE_MIN_IDLE", "5")));
    config.put(
        "connectionTimeout",
        Long.parseLong(getSecretValue("DATABASE_CONNECTION_TIMEOUT", "30000")));
    config.put("idleTimeout", Long.parseLong(getSecretValue("DATABASE_IDLE_TIMEOUT", "600000")));
    config.put("maxLifetime", Long.parseLong(getSecretValue("DATABASE_MAX_LIFETIME", "1800000")));

    return config;
  }

  /** Get cache configuration */
  public Map<String, Object> getCacheConfiguration() {
    Map<String, Object> config = new HashMap<>();

    config.put("host", getSecretValue("REDIS_HOST", "localhost"));
    config.put("port", Integer.parseInt(getSecretValue("REDIS_PORT", "6379")));
    config.put("password", getSecretValue("REDIS_PASSWORD", ""));
    config.put("database", Integer.parseInt(getSecretValue("REDIS_DATABASE", "0")));
    config.put("timeout", Integer.parseInt(getSecretValue("REDIS_TIMEOUT", "2000")));
    config.put("maxTotal", Integer.parseInt(getSecretValue("REDIS_MAX_TOTAL", "8")));
    config.put("maxIdle", Integer.parseInt(getSecretValue("REDIS_MAX_IDLE", "8")));
    config.put("minIdle", Integer.parseInt(getSecretValue("REDIS_MIN_IDLE", "0")));

    return config;
  }

  /** Get messaging configuration */
  public Map<String, Object> getMessagingConfiguration() {
    Map<String, Object> config = new HashMap<>();

    // Kafka configuration
    config.put(
        "kafka.bootstrap.servers", getSecretValue("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"));
    config.put("kafka.group.id", getSecretValue("KAFKA_GROUP_ID", "payments-engine"));
    config.put("kafka.client.id", getSecretValue("KAFKA_CLIENT_ID", "payments-engine-client"));
    config.put(
        "kafka.session.timeout.ms",
        Integer.parseInt(getSecretValue("KAFKA_SESSION_TIMEOUT_MS", "30000")));
    config.put(
        "kafka.heartbeat.interval.ms",
        Integer.parseInt(getSecretValue("KAFKA_HEARTBEAT_INTERVAL_MS", "10000")));
    config.put(
        "kafka.max.poll.records",
        Integer.parseInt(getSecretValue("KAFKA_MAX_POLL_RECORDS", "500")));
    config.put("kafka.auto.offset.reset", getSecretValue("KAFKA_AUTO_OFFSET_RESET", "earliest"));
    config.put(
        "kafka.enable.auto.commit",
        Boolean.parseBoolean(getSecretValue("KAFKA_ENABLE_AUTO_COMMIT", "false")));
    config.put(
        "kafka.enable.idempotence",
        Boolean.parseBoolean(getSecretValue("KAFKA_ENABLE_IDEMPOTENCE", "true")));

    return config;
  }

  /** Get security configuration */
  public Map<String, Object> getSecurityConfiguration() {
    Map<String, Object> config = new HashMap<>();

    // JWT configuration
    config.put("jwt.secret", getSecretValue("JWT_SECRET", "default-secret"));
    config.put("jwt.expiration", Long.parseLong(getSecretValue("JWT_EXPIRATION", "86400000")));
    config.put("jwt.issuer", getSecretValue("JWT_ISSUER", "payments-engine"));
    config.put("jwt.audience", getSecretValue("JWT_AUDIENCE", "payments-engine"));

    // OAuth2 configuration
    config.put("oauth2.client.id", getSecretValue("OAUTH2_CLIENT_ID", "payments-engine"));
    config.put("oauth2.client.secret", getSecretValue("OAUTH2_CLIENT_SECRET", "default-secret"));
    config.put(
        "oauth2.token.uri",
        getSecretValue("OAUTH2_TOKEN_URI", "http://localhost:8080/oauth/token"));
    config.put(
        "oauth2.authorization.uri",
        getSecretValue("OAUTH2_AUTHORIZATION_URI", "http://localhost:8080/oauth/authorize"));
    config.put(
        "oauth2.user.info.uri",
        getSecretValue("OAUTH2_USER_INFO_URI", "http://localhost:8080/oauth/userinfo"));

    return config;
  }

  /** Get telemetry configuration */
  public Map<String, Object> getTelemetryConfiguration() {
    Map<String, Object> config = new HashMap<>();

    // Tracing configuration
    config.put("tracing.enabled", getBooleanValue("TELEMETRY_TRACING_ENABLED", true));
    config.put("tracing.exporter", getSecretValue("TELEMETRY_TRACING_EXPORTER", "jaeger"));
    config.put("tracing.service.name", getSecretValue("TELEMETRY_SERVICE_NAME", "payments-engine"));
    config.put("tracing.service.version", getSecretValue("TELEMETRY_SERVICE_VERSION", "0.1.0"));
    config.put(
        "tracing.jaeger.endpoint",
        getSecretValue("TELEMETRY_JAEGER_ENDPOINT", "http://localhost:14250"));
    config.put(
        "tracing.otlp.endpoint",
        getSecretValue("TELEMETRY_OTLP_ENDPOINT", "http://localhost:4317"));
    config.put(
        "tracing.zipkin.endpoint",
        getSecretValue("TELEMETRY_ZIPKIN_ENDPOINT", "http://localhost:9411/api/v2/spans"));

    // Metrics configuration
    config.put("metrics.enabled", getBooleanValue("TELEMETRY_METRICS_ENABLED", true));
    config.put("metrics.prometheus.enabled", getBooleanValue("TELEMETRY_PROMETHEUS_ENABLED", true));
    config.put(
        "metrics.prometheus.endpoint",
        getSecretValue("TELEMETRY_PROMETHEUS_ENDPOINT", "/actuator/prometheus"));

    // Logging configuration
    config.put("logging.enabled", getBooleanValue("TELEMETRY_LOGGING_ENABLED", true));
    config.put("logging.structured", getBooleanValue("TELEMETRY_LOGGING_STRUCTURED", true));
    config.put("logging.level", getSecretValue("TELEMETRY_LOGGING_LEVEL", "INFO"));
    config.put(
        "logging.file.path",
        getSecretValue("TELEMETRY_LOGGING_FILE_PATH", "logs/payments-engine.log"));
    config.put("logging.file.max.size", getSecretValue("TELEMETRY_LOGGING_FILE_MAX_SIZE", "100MB"));
    config.put(
        "logging.file.max.history",
        Integer.parseInt(getSecretValue("TELEMETRY_LOGGING_FILE_MAX_HISTORY", "30")));

    return config;
  }

  /** Get a secret value with fallback */
  private String getSecretValue(String key, String defaultValue) {
    String value = secretManager.getSecret(key);
    return value != null ? value : defaultValue;
  }

  /** Get a boolean value with fallback */
  private boolean getBooleanValue(String key, boolean defaultValue) {
    String value = secretManager.getSecret(key);
    if (value == null) {
      return defaultValue;
    }
    return Boolean.parseBoolean(value);
  }

  /** Get configuration summary */
  public Map<String, Object> getConfigurationSummary() {
    Map<String, Object> summary = new HashMap<>();

    summary.put("activeProfile", activeProfile);
    summary.put("environment", environment.getProperty("payments.profiles.active", "local"));
    summary.put("serviceCount", 6); // Number of services
    summary.put("configurationSources", new String[] {"environment", "properties", "secrets"});
    summary.put("lastUpdated", System.currentTimeMillis());

    return summary;
  }
}
