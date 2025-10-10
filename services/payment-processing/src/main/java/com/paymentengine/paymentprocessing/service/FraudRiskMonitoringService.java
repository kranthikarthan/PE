package com.paymentengine.paymentprocessing.service;

import com.paymentengine.paymentprocessing.entity.FraudRiskAssessment;
import com.paymentengine.paymentprocessing.entity.FraudRiskConfiguration;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for fraud/risk monitoring operations
 */
public interface FraudRiskMonitoringService {
    
    /**
     * Perform fraud/risk assessment for a payment
     */
    CompletableFuture<FraudRiskAssessment> assessPaymentRisk(
            String transactionReference,
            String tenantId,
            String paymentType,
            String localInstrumentationCode,
            String clearingSystemCode,
            FraudRiskConfiguration.PaymentSource paymentSource,
            Map<String, Object> paymentData,
            String uetr);
    
    /**
     * Get fraud/risk assessment by assessment ID
     */
    Optional<FraudRiskAssessment> getAssessmentById(String assessmentId);
    
    /**
     * Get fraud/risk assessment by transaction reference
     */
    Optional<FraudRiskAssessment> getAssessmentByTransactionReference(String transactionReference);
    
    /**
     * Get fraud/risk configurations for a specific criteria
     */
    java.util.List<FraudRiskConfiguration> getApplicableConfigurations(
            String tenantId,
            String paymentType,
            String localInstrumentationCode,
            String clearingSystemCode,
            FraudRiskConfiguration.PaymentSource paymentSource);
    
    /**
     * Check if fraud/risk monitoring is enabled for the given criteria
     */
    boolean isFraudRiskMonitoringEnabled(
            String tenantId,
            String paymentType,
            String localInstrumentationCode,
            String clearingSystemCode,
            FraudRiskConfiguration.PaymentSource paymentSource);
    
    /**
     * Get fraud/risk assessment statistics for a tenant
     */
    Map<String, Object> getAssessmentStatistics(String tenantId);
    
    /**
     * Get fraud/risk assessment statistics for a tenant within a date range
     */
    Map<String, Object> getAssessmentStatistics(
            String tenantId,
            java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate);
    
    /**
     * Retry failed fraud/risk assessment
     */
    CompletableFuture<FraudRiskAssessment> retryAssessment(String assessmentId);
    
    /**
     * Cancel pending fraud/risk assessment
     */
    boolean cancelAssessment(String assessmentId);
    
    /**
     * Update fraud/risk assessment decision (for manual review)
     */
    boolean updateAssessmentDecision(String assessmentId, FraudRiskAssessment.Decision decision, String reason);
    
    /**
     * Get pending assessments that need manual review
     */
    java.util.List<FraudRiskAssessment> getPendingManualReviews(String tenantId);
    
    /**
     * Get high-risk assessments for a tenant
     */
    java.util.List<FraudRiskAssessment> getHighRiskAssessments(String tenantId);
    
    /**
     * Get critical-risk assessments for a tenant
     */
    java.util.List<FraudRiskAssessment> getCriticalRiskAssessments(String tenantId);
    
    /**
     * Get assessments that need retry
     */
    java.util.List<FraudRiskAssessment> getAssessmentsNeedingRetry(String tenantId);
    
    /**
     * Get expired assessments
     */
    java.util.List<FraudRiskAssessment> getExpiredAssessments();
    
    /**
     * Clean up expired assessments
     */
    int cleanupExpiredAssessments();
    
    /**
     * Get fraud/risk monitoring health status
     */
    Map<String, Object> getHealthStatus();
    
    /**
     * Get fraud/risk monitoring metrics
     */
    Map<String, Object> getMetrics(String tenantId);
    
    /**
     * Get fraud/risk monitoring metrics for a date range
     */
    Map<String, Object> getMetrics(
            String tenantId,
            java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate);
}