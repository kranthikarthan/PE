package com.paymentengine.paymentprocessing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Fraud/Risk Monitoring Configuration Entity
 * 
 * Provides configuration for external fraud/risk monitoring APIs,
 * risk assessment rules, and decision criteria for payment processing.
 */
@Entity
@Table(name = "fraud_risk_configurations", schema = "payment_engine")
public class FraudRiskConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "configuration_name", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String configurationName;
    
    @Column(name = "tenant_id", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String tenantId;
    
    @Column(name = "payment_type", length = 50)
    @Size(max = 50)
    private String paymentType;
    
    @Column(name = "local_instrumentation_code", length = 50)
    @Size(max = 50)
    private String localInstrumentationCode;
    
    @Column(name = "clearing_system_code", length = 50)
    @Size(max = 50)
    private String clearingSystemCode;
    
    @Column(name = "payment_source", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private PaymentSource paymentSource;
    
    @Column(name = "risk_assessment_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull
    private RiskAssessmentType riskAssessmentType;
    
    @Column(name = "bank_fraud_api_config", columnDefinition = "jsonb")
    private Map<String, Object> bankFraudApiConfig;
    
    @Column(name = "risk_rules", columnDefinition = "jsonb")
    private Map<String, Object> riskRules;
    
    @Column(name = "decision_criteria", columnDefinition = "jsonb")
    private Map<String, Object> decisionCriteria;
    
    @Column(name = "thresholds", columnDefinition = "jsonb")
    private Map<String, Object> thresholds;
    
    @Column(name = "timeout_config", columnDefinition = "jsonb")
    private Map<String, Object> timeoutConfig;
    
    @Column(name = "retry_config", columnDefinition = "jsonb")
    private Map<String, Object> retryConfig;
    
    @Column(name = "circuit_breaker_config", columnDefinition = "jsonb")
    private Map<String, Object> circuitBreakerConfig;
    
    @Column(name = "fallback_config", columnDefinition = "jsonb")
    private Map<String, Object> fallbackConfig;
    
    @Column(name = "monitoring_config", columnDefinition = "jsonb")
    private Map<String, Object> monitoringConfig;
    
    @Column(name = "alerting_config", columnDefinition = "jsonb")
    private Map<String, Object> alertingConfig;
    
    @Column(name = "is_enabled")
    private Boolean isEnabled = true;
    
    @Column(name = "priority")
    private Integer priority = 1;
    
    @Column(name = "version")
    private String version = "1.0";
    
    @Column(name = "description", length = 1000)
    @Size(max = 1000)
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    @Size(max = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    @Size(max = 100)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public FraudRiskConfiguration() {}
    
    public FraudRiskConfiguration(String configurationName, String tenantId, PaymentSource paymentSource, RiskAssessmentType riskAssessmentType) {
        this.configurationName = configurationName;
        this.tenantId = tenantId;
        this.paymentSource = paymentSource;
        this.riskAssessmentType = riskAssessmentType;
    }
    
    // Business methods
    public boolean matchesCriteria(String tenantId, String paymentType, String localInstrumentationCode, String clearingSystemCode, PaymentSource paymentSource) {
        if (!this.tenantId.equals(tenantId)) {
            return false;
        }
        
        if (this.paymentType != null && !this.paymentType.equals(paymentType)) {
            return false;
        }
        
        if (this.localInstrumentationCode != null && !this.localInstrumentationCode.equals(localInstrumentationCode)) {
            return false;
        }
        
        if (this.clearingSystemCode != null && !this.clearingSystemCode.equals(clearingSystemCode)) {
            return false;
        }
        
        if (!this.paymentSource.equals(paymentSource)) {
            return false;
        }
        
        return true;
    }
    
    public boolean isApplicableForRiskAssessment(RiskAssessmentType riskAssessmentType) {
        return this.riskAssessmentType == riskAssessmentType;
    }
    
    public boolean hasBankFraudApiConfig() {
        return bankFraudApiConfig != null && !bankFraudApiConfig.isEmpty();
    }
    
    public boolean hasRiskRules() {
        return riskRules != null && !riskRules.isEmpty();
    }
    
    public boolean hasDecisionCriteria() {
        return decisionCriteria != null && !decisionCriteria.isEmpty();
    }
    
    public boolean hasThresholds() {
        return thresholds != null && !thresholds.isEmpty();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getConfigurationName() {
        return configurationName;
    }
    
    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    public String getLocalInstrumentationCode() {
        return localInstrumentationCode;
    }
    
    public void setLocalInstrumentationCode(String localInstrumentationCode) {
        this.localInstrumentationCode = localInstrumentationCode;
    }
    
    public String getClearingSystemCode() {
        return clearingSystemCode;
    }
    
    public void setClearingSystemCode(String clearingSystemCode) {
        this.clearingSystemCode = clearingSystemCode;
    }
    
    public PaymentSource getPaymentSource() {
        return paymentSource;
    }
    
    public void setPaymentSource(PaymentSource paymentSource) {
        this.paymentSource = paymentSource;
    }
    
    public RiskAssessmentType getRiskAssessmentType() {
        return riskAssessmentType;
    }
    
    public void setRiskAssessmentType(RiskAssessmentType riskAssessmentType) {
        this.riskAssessmentType = riskAssessmentType;
    }
    
    public Map<String, Object> getBankFraudApiConfig() {
        return bankFraudApiConfig;
    }
    
    public void setBankFraudApiConfig(Map<String, Object> bankFraudApiConfig) {
        this.bankFraudApiConfig = bankFraudApiConfig;
    }
    
    public Map<String, Object> getRiskRules() {
        return riskRules;
    }
    
    public void setRiskRules(Map<String, Object> riskRules) {
        this.riskRules = riskRules;
    }
    
    public Map<String, Object> getDecisionCriteria() {
        return decisionCriteria;
    }
    
    public void setDecisionCriteria(Map<String, Object> decisionCriteria) {
        this.decisionCriteria = decisionCriteria;
    }
    
    public Map<String, Object> getThresholds() {
        return thresholds;
    }
    
    public void setThresholds(Map<String, Object> thresholds) {
        this.thresholds = thresholds;
    }
    
    public Map<String, Object> getTimeoutConfig() {
        return timeoutConfig;
    }
    
    public void setTimeoutConfig(Map<String, Object> timeoutConfig) {
        this.timeoutConfig = timeoutConfig;
    }
    
    public Map<String, Object> getRetryConfig() {
        return retryConfig;
    }
    
    public void setRetryConfig(Map<String, Object> retryConfig) {
        this.retryConfig = retryConfig;
    }
    
    public Map<String, Object> getCircuitBreakerConfig() {
        return circuitBreakerConfig;
    }
    
    public void setCircuitBreakerConfig(Map<String, Object> circuitBreakerConfig) {
        this.circuitBreakerConfig = circuitBreakerConfig;
    }
    
    public Map<String, Object> getFallbackConfig() {
        return fallbackConfig;
    }
    
    public void setFallbackConfig(Map<String, Object> fallbackConfig) {
        this.fallbackConfig = fallbackConfig;
    }
    
    public Map<String, Object> getMonitoringConfig() {
        return monitoringConfig;
    }
    
    public void setMonitoringConfig(Map<String, Object> monitoringConfig) {
        this.monitoringConfig = monitoringConfig;
    }
    
    public Map<String, Object> getAlertingConfig() {
        return alertingConfig;
    }
    
    public void setAlertingConfig(Map<String, Object> alertingConfig) {
        this.alertingConfig = alertingConfig;
    }
    
    public Boolean getIsEnabled() {
        return isEnabled;
    }
    
    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    @Override
    public String toString() {
        return "FraudRiskConfiguration{" +
                "id=" + id +
                ", configurationName='" + configurationName + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", paymentType='" + paymentType + '\'' +
                ", localInstrumentationCode='" + localInstrumentationCode + '\'' +
                ", clearingSystemCode='" + clearingSystemCode + '\'' +
                ", paymentSource=" + paymentSource +
                ", riskAssessmentType=" + riskAssessmentType +
                ", isEnabled=" + isEnabled +
                ", priority=" + priority +
                '}';
    }
    
    /**
     * Payment Source Enumeration
     */
    public enum PaymentSource {
        BANK_CLIENT,        // Payment from bank's own clients
        CLEARING_SYSTEM,    // Payment from clearing system
        BOTH                // Both sources
    }
    
    /**
     * Risk Assessment Type Enumeration
     */
    public enum RiskAssessmentType {
        REAL_TIME,          // Real-time risk assessment
        BATCH,              // Batch risk assessment
        HYBRID,             // Hybrid approach
        CUSTOM              // Custom risk assessment
    }
}