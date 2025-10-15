package com.payments.validation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Rule Engine Configuration
 * 
 * Configures rule engine settings:
 * - Rule engine type (Drools, custom, etc.)
 * - Rule file locations
 * - Rule execution settings
 * - Performance tuning
 */
@Configuration
@ConfigurationProperties(prefix = "validation.rules.engine")
public class RuleEngineConfig {

    private String type = "drools";
    private String rulesPath = "classpath:rules/";
    private boolean cacheEnabled = true;
    private int cacheSize = 1000;
    private int maxExecutionTime = 5000; // 5 seconds
    private boolean parallelExecution = false;
    private int maxParallelRules = 10;
    private Map<String, Object> engineProperties;
    private List<String> ruleFiles;

    @Bean
    public RuleEngineProperties ruleEngineProperties() {
        return RuleEngineProperties.builder()
                .type(type)
                .rulesPath(rulesPath)
                .cacheEnabled(cacheEnabled)
                .cacheSize(cacheSize)
                .maxExecutionTime(maxExecutionTime)
                .parallelExecution(parallelExecution)
                .maxParallelRules(maxParallelRules)
                .engineProperties(engineProperties)
                .ruleFiles(ruleFiles)
                .build();
    }

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRulesPath() { return rulesPath; }
    public void setRulesPath(String rulesPath) { this.rulesPath = rulesPath; }

    public boolean isCacheEnabled() { return cacheEnabled; }
    public void setCacheEnabled(boolean cacheEnabled) { this.cacheEnabled = cacheEnabled; }

    public int getCacheSize() { return cacheSize; }
    public void setCacheSize(int cacheSize) { this.cacheSize = cacheSize; }

    public int getMaxExecutionTime() { return maxExecutionTime; }
    public void setMaxExecutionTime(int maxExecutionTime) { this.maxExecutionTime = maxExecutionTime; }

    public boolean isParallelExecution() { return parallelExecution; }
    public void setParallelExecution(boolean parallelExecution) { this.parallelExecution = parallelExecution; }

    public int getMaxParallelRules() { return maxParallelRules; }
    public void setMaxParallelRules(int maxParallelRules) { this.maxParallelRules = maxParallelRules; }

    public Map<String, Object> getEngineProperties() { return engineProperties; }
    public void setEngineProperties(Map<String, Object> engineProperties) { this.engineProperties = engineProperties; }

    public List<String> getRuleFiles() { return ruleFiles; }
    public void setRuleFiles(List<String> ruleFiles) { this.ruleFiles = ruleFiles; }

    /**
     * Rule Engine Properties
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RuleEngineProperties {
        private String type;
        private String rulesPath;
        private boolean cacheEnabled;
        private int cacheSize;
        private int maxExecutionTime;
        private boolean parallelExecution;
        private int maxParallelRules;
        private Map<String, Object> engineProperties;
        private List<String> ruleFiles;
    }
}
