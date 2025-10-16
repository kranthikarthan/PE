package com.payments.validation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Rule Engine Configuration
 *
 * <p>Configuration for validation rule engines: - Rule execution settings - Performance tuning -
 * Timeout configurations - Parallel execution settings
 */
@Configuration
public class RuleEngineConfig {

  @Bean
  @ConfigurationProperties(prefix = "validation.rule-engine")
  public RuleEngineProperties ruleEngineProperties() {
    return new RuleEngineProperties();
  }

  /** Rule Engine Properties */
  @Data
  public static class RuleEngineProperties {

    // Execution Settings
    private boolean parallelExecution = true;
    private long maxExecutionTime = 30000; // 30 seconds
    private int maxRetries = 3;
    private long retryDelay = 1000; // 1 second

    // Performance Settings
    private int threadPoolSize = 10;
    private int queueCapacity = 100;
    private long keepAliveTime = 60; // 60 seconds
    private int maxParallelRules = 5;
    private boolean cacheEnabled = true;
    private int cacheSize = 1000;

    // Rule Settings
    private boolean enableBusinessRules = true;
    private boolean enableComplianceRules = true;
    private boolean enableFraudDetectionRules = true;
    private boolean enableRiskAssessmentRules = true;

    // Timeout Settings
    private long businessRulesTimeout = 5000; // 5 seconds
    private long complianceRulesTimeout = 10000; // 10 seconds
    private long fraudDetectionTimeout = 15000; // 15 seconds
    private long riskAssessmentTimeout = 10000; // 10 seconds

    // Threshold Settings
    private double maxFraudScore = 70.0;
    private double maxRiskScore = 80.0;
    private int maxFailedRules = 5;

    // Logging Settings
    private boolean enableRuleLogging = true;
    private boolean enablePerformanceLogging = true;
    private boolean enableDebugLogging = false;
  }
}
