package com.paymentengine.middleware.service;

import com.paymentengine.middleware.entity.FraudRiskAssessment;

import java.util.Map;

/**
 * Service interface for risk assessment engine
 */
public interface RiskAssessmentEngine {
    
    /**
     * Evaluate risk rules and return risk factors
     */
    Map<String, Object> evaluateRiskRules(
            Map<String, Object> riskRules,
            Map<String, Object> paymentData,
            FraudRiskAssessment assessment);
    
    /**
     * Evaluate decision criteria and return decision
     */
    FraudRiskAssessment.Decision evaluateDecisionCriteria(
            Map<String, Object> decisionCriteria,
            FraudRiskAssessment assessment,
            Map<String, Object> paymentData);
    
    /**
     * Evaluate thresholds and return decision
     */
    FraudRiskAssessment.Decision evaluateThresholds(
            Map<String, Object> thresholds,
            FraudRiskAssessment assessment,
            Map<String, Object> paymentData);
    
    /**
     * Calculate risk score based on risk factors
     */
    java.math.BigDecimal calculateRiskScore(
            Map<String, Object> riskFactors,
            Map<String, Object> paymentData);
    
    /**
     * Determine risk level based on risk score
     */
    FraudRiskAssessment.RiskLevel determineRiskLevel(java.math.BigDecimal riskScore);
    
    /**
     * Evaluate conditional rules
     */
    boolean evaluateCondition(String condition, Map<String, Object> context);
    
    /**
     * Apply risk scoring rules
     */
    Map<String, Object> applyRiskScoringRules(
            Map<String, Object> scoringRules,
            Map<String, Object> paymentData);
    
    /**
     * Get risk assessment statistics
     */
    Map<String, Object> getRiskAssessmentStatistics(String tenantId);
}