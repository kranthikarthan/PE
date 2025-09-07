package com.paymentengine.middleware.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Fraud/Risk Assessment Entity
 * 
 * Stores the results of fraud/risk assessments performed on payments,
 * including external API responses, risk scores, and decision outcomes.
 */
@Entity
@Table(name = "fraud_risk_assessments", schema = "payment_engine")
public class FraudRiskAssessment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "assessment_id", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String assessmentId;
    
    @Column(name = "transaction_reference", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String transactionReference;
    
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
    private FraudRiskConfiguration.PaymentSource paymentSource;
    
    @Column(name = "risk_assessment_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull
    private FraudRiskConfiguration.RiskAssessmentType riskAssessmentType;
    
    @Column(name = "configuration_id")
    private UUID configurationId;
    
    @Column(name = "external_api_used", length = 100)
    @Size(max = 100)
    private String externalApiUsed;
    
    @Column(name = "risk_score")
    private BigDecimal riskScore;
    
    @Column(name = "risk_level", length = 20)
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;
    
    @Column(name = "decision", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Decision decision;
    
    @Column(name = "decision_reason", length = 500)
    @Size(max = 500)
    private String decisionReason;
    
    @Column(name = "external_api_request", columnDefinition = "jsonb")
    private Map<String, Object> externalApiRequest;
    
    @Column(name = "external_api_response", columnDefinition = "jsonb")
    private Map<String, Object> externalApiResponse;
    
    @Column(name = "risk_factors", columnDefinition = "jsonb")
    private Map<String, Object> riskFactors;
    
    @Column(name = "assessment_details", columnDefinition = "jsonb")
    private Map<String, Object> assessmentDetails;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    @Column(name = "external_api_response_time_ms")
    private Long externalApiResponseTimeMs;
    
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private AssessmentStatus status;
    
    @Column(name = "error_message", length = 1000)
    @Size(max = 1000)
    private String errorMessage;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "assessed_at", nullable = false)
    private LocalDateTime assessedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (assessedAt == null) {
            assessedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public FraudRiskAssessment() {}
    
    public FraudRiskAssessment(String assessmentId, String transactionReference, String tenantId, 
                              FraudRiskConfiguration.PaymentSource paymentSource, 
                              FraudRiskConfiguration.RiskAssessmentType riskAssessmentType) {
        this.assessmentId = assessmentId;
        this.transactionReference = transactionReference;
        this.tenantId = tenantId;
        this.paymentSource = paymentSource;
        this.riskAssessmentType = riskAssessmentType;
        this.status = AssessmentStatus.PENDING;
    }
    
    // Business methods
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isSuccessful() {
        return status == AssessmentStatus.COMPLETED && decision != null;
    }
    
    public boolean isFailed() {
        return status == AssessmentStatus.FAILED || status == AssessmentStatus.ERROR;
    }
    
    public boolean isPending() {
        return status == AssessmentStatus.PENDING || status == AssessmentStatus.IN_PROGRESS;
    }
    
    public boolean isApproved() {
        return decision == Decision.APPROVE;
    }
    
    public boolean isRejected() {
        return decision == Decision.REJECT;
    }
    
    public boolean requiresManualReview() {
        return decision == Decision.MANUAL_REVIEW;
    }
    
    public boolean isHighRisk() {
        return riskLevel == RiskLevel.HIGH;
    }
    
    public boolean isMediumRisk() {
        return riskLevel == RiskLevel.MEDIUM;
    }
    
    public boolean isLowRisk() {
        return riskLevel == RiskLevel.LOW;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getAssessmentId() {
        return assessmentId;
    }
    
    public void setAssessmentId(String assessmentId) {
        this.assessmentId = assessmentId;
    }
    
    public String getTransactionReference() {
        return transactionReference;
    }
    
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
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
    
    public FraudRiskConfiguration.PaymentSource getPaymentSource() {
        return paymentSource;
    }
    
    public void setPaymentSource(FraudRiskConfiguration.PaymentSource paymentSource) {
        this.paymentSource = paymentSource;
    }
    
    public FraudRiskConfiguration.RiskAssessmentType getRiskAssessmentType() {
        return riskAssessmentType;
    }
    
    public void setRiskAssessmentType(FraudRiskConfiguration.RiskAssessmentType riskAssessmentType) {
        this.riskAssessmentType = riskAssessmentType;
    }
    
    public UUID getConfigurationId() {
        return configurationId;
    }
    
    public void setConfigurationId(UUID configurationId) {
        this.configurationId = configurationId;
    }
    
    public String getExternalApiUsed() {
        return externalApiUsed;
    }
    
    public void setExternalApiUsed(String externalApiUsed) {
        this.externalApiUsed = externalApiUsed;
    }
    
    public BigDecimal getRiskScore() {
        return riskScore;
    }
    
    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }
    
    public RiskLevel getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public Decision getDecision() {
        return decision;
    }
    
    public void setDecision(Decision decision) {
        this.decision = decision;
    }
    
    public String getDecisionReason() {
        return decisionReason;
    }
    
    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }
    
    public Map<String, Object> getExternalApiRequest() {
        return externalApiRequest;
    }
    
    public void setExternalApiRequest(Map<String, Object> externalApiRequest) {
        this.externalApiRequest = externalApiRequest;
    }
    
    public Map<String, Object> getExternalApiResponse() {
        return externalApiResponse;
    }
    
    public void setExternalApiResponse(Map<String, Object> externalApiResponse) {
        this.externalApiResponse = externalApiResponse;
    }
    
    public Map<String, Object> getRiskFactors() {
        return riskFactors;
    }
    
    public void setRiskFactors(Map<String, Object> riskFactors) {
        this.riskFactors = riskFactors;
    }
    
    public Map<String, Object> getAssessmentDetails() {
        return assessmentDetails;
    }
    
    public void setAssessmentDetails(Map<String, Object> assessmentDetails) {
        this.assessmentDetails = assessmentDetails;
    }
    
    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public Long getExternalApiResponseTimeMs() {
        return externalApiResponseTimeMs;
    }
    
    public void setExternalApiResponseTimeMs(Long externalApiResponseTimeMs) {
        this.externalApiResponseTimeMs = externalApiResponseTimeMs;
    }
    
    public AssessmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Integer getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
    
    public LocalDateTime getAssessedAt() {
        return assessedAt;
    }
    
    public void setAssessedAt(LocalDateTime assessedAt) {
        this.assessedAt = assessedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
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
    
    @Override
    public String toString() {
        return "FraudRiskAssessment{" +
                "id=" + id +
                ", assessmentId='" + assessmentId + '\'' +
                ", transactionReference='" + transactionReference + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", paymentSource=" + paymentSource +
                ", riskAssessmentType=" + riskAssessmentType +
                ", riskScore=" + riskScore +
                ", riskLevel=" + riskLevel +
                ", decision=" + decision +
                ", status=" + status +
                '}';
    }
    
    /**
     * Risk Level Enumeration
     */
    public enum RiskLevel {
        LOW,        // Low risk
        MEDIUM,     // Medium risk
        HIGH,       // High risk
        CRITICAL    // Critical risk
    }
    
    /**
     * Decision Enumeration
     */
    public enum Decision {
        APPROVE,        // Approve the payment
        REJECT,         // Reject the payment
        MANUAL_REVIEW,  // Send for manual review
        HOLD,           // Hold the payment
        ESCALATE        // Escalate to senior staff
    }
    
    /**
     * Assessment Status Enumeration
     */
    public enum AssessmentStatus {
        PENDING,        // Assessment is pending
        IN_PROGRESS,    // Assessment is in progress
        COMPLETED,      // Assessment completed successfully
        FAILED,         // Assessment failed
        ERROR,          // Assessment error
        TIMEOUT,        // Assessment timed out
        CANCELLED       // Assessment was cancelled
    }
}