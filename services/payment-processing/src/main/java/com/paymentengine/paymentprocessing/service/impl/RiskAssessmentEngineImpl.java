package com.paymentengine.paymentprocessing.service.impl;

import com.paymentengine.paymentprocessing.entity.FraudRiskAssessment;
import com.paymentengine.paymentprocessing.service.RiskAssessmentEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of risk assessment engine
 */
@Service
public class RiskAssessmentEngineImpl implements RiskAssessmentEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(RiskAssessmentEngineImpl.class);
    
    @Override
    public Map<String, Object> evaluateRiskRules(
            Map<String, Object> riskRules,
            Map<String, Object> paymentData,
            FraudRiskAssessment assessment) {
        
        logger.debug("Evaluating risk rules for assessment: {}", assessment.getAssessmentId());
        
        Map<String, Object> riskFactors = new HashMap<>();
        
        try {
            // Evaluate amount-based rules
            if (riskRules.containsKey("amountRules")) {
                Map<String, Object> amountRules = (Map<String, Object>) riskRules.get("amountRules");
                Map<String, Object> amountRiskFactors = evaluateAmountRules(amountRules, paymentData);
                riskFactors.putAll(amountRiskFactors);
            }
            
            // Evaluate frequency-based rules
            if (riskRules.containsKey("frequencyRules")) {
                Map<String, Object> frequencyRules = (Map<String, Object>) riskRules.get("frequencyRules");
                Map<String, Object> frequencyRiskFactors = evaluateFrequencyRules(frequencyRules, paymentData);
                riskFactors.putAll(frequencyRiskFactors);
            }
            
            // Evaluate location-based rules
            if (riskRules.containsKey("locationRules")) {
                Map<String, Object> locationRules = (Map<String, Object>) riskRules.get("locationRules");
                Map<String, Object> locationRiskFactors = evaluateLocationRules(locationRules, paymentData);
                riskFactors.putAll(locationRiskFactors);
            }
            
            // Evaluate time-based rules
            if (riskRules.containsKey("timeRules")) {
                Map<String, Object> timeRules = (Map<String, Object>) riskRules.get("timeRules");
                Map<String, Object> timeRiskFactors = evaluateTimeRules(timeRules, paymentData);
                riskFactors.putAll(timeRiskFactors);
            }
            
            // Evaluate account-based rules
            if (riskRules.containsKey("accountRules")) {
                Map<String, Object> accountRules = (Map<String, Object>) riskRules.get("accountRules");
                Map<String, Object> accountRiskFactors = evaluateAccountRules(accountRules, paymentData);
                riskFactors.putAll(accountRiskFactors);
            }
            
            // Evaluate device-based rules
            if (riskRules.containsKey("deviceRules")) {
                Map<String, Object> deviceRules = (Map<String, Object>) riskRules.get("deviceRules");
                Map<String, Object> deviceRiskFactors = evaluateDeviceRules(deviceRules, paymentData);
                riskFactors.putAll(deviceRiskFactors);
            }
            
            // Evaluate pattern-based rules
            if (riskRules.containsKey("patternRules")) {
                Map<String, Object> patternRules = (Map<String, Object>) riskRules.get("patternRules");
                Map<String, Object> patternRiskFactors = evaluatePatternRules(patternRules, paymentData);
                riskFactors.putAll(patternRiskFactors);
            }
            
            // Calculate overall risk score
            BigDecimal riskScore = calculateRiskScore(riskFactors, paymentData);
            riskFactors.put("overallRiskScore", riskScore);
            riskFactors.put("riskLevel", determineRiskLevel(riskScore));
            
            logger.debug("Risk rules evaluation completed for assessment: {}, riskScore: {}", 
                        assessment.getAssessmentId(), riskScore);
            
            return riskFactors;
            
        } catch (Exception e) {
            logger.error("Error evaluating risk rules: {}", e.getMessage(), e);
            riskFactors.put("error", e.getMessage());
            riskFactors.put("overallRiskScore", BigDecimal.ZERO);
            riskFactors.put("riskLevel", FraudRiskAssessment.RiskLevel.LOW);
            return riskFactors;
        }
    }
    
    @Override
    public FraudRiskAssessment.Decision evaluateDecisionCriteria(
            Map<String, Object> decisionCriteria,
            FraudRiskAssessment assessment,
            Map<String, Object> paymentData) {
        
        logger.debug("Evaluating decision criteria for assessment: {}", assessment.getAssessmentId());
        
        try {
            // Check for automatic approval criteria
            if (decisionCriteria.containsKey("autoApprove")) {
                Map<String, Object> autoApprove = (Map<String, Object>) decisionCriteria.get("autoApprove");
                if (evaluateAutoApproveCriteria(autoApprove, assessment, paymentData)) {
                    return FraudRiskAssessment.Decision.APPROVE;
                }
            }
            
            // Check for automatic rejection criteria
            if (decisionCriteria.containsKey("autoReject")) {
                Map<String, Object> autoReject = (Map<String, Object>) decisionCriteria.get("autoReject");
                if (evaluateAutoRejectCriteria(autoReject, assessment, paymentData)) {
                    return FraudRiskAssessment.Decision.REJECT;
                }
            }
            
            // Check for manual review criteria
            if (decisionCriteria.containsKey("manualReview")) {
                Map<String, Object> manualReview = (Map<String, Object>) decisionCriteria.get("manualReview");
                if (evaluateManualReviewCriteria(manualReview, assessment, paymentData)) {
                    return FraudRiskAssessment.Decision.MANUAL_REVIEW;
                }
            }
            
            // Check for hold criteria
            if (decisionCriteria.containsKey("hold")) {
                Map<String, Object> hold = (Map<String, Object>) decisionCriteria.get("hold");
                if (evaluateHoldCriteria(hold, assessment, paymentData)) {
                    return FraudRiskAssessment.Decision.HOLD;
                }
            }
            
            // Check for escalation criteria
            if (decisionCriteria.containsKey("escalate")) {
                Map<String, Object> escalate = (Map<String, Object>) decisionCriteria.get("escalate");
                if (evaluateEscalateCriteria(escalate, assessment, paymentData)) {
                    return FraudRiskAssessment.Decision.ESCALATE;
                }
            }
            
            // Default to manual review if no criteria match
            return FraudRiskAssessment.Decision.MANUAL_REVIEW;
            
        } catch (Exception e) {
            logger.error("Error evaluating decision criteria: {}", e.getMessage(), e);
            return FraudRiskAssessment.Decision.MANUAL_REVIEW;
        }
    }
    
    @Override
    public FraudRiskAssessment.Decision evaluateThresholds(
            Map<String, Object> thresholds,
            FraudRiskAssessment assessment,
            Map<String, Object> paymentData) {
        
        logger.debug("Evaluating thresholds for assessment: {}", assessment.getAssessmentId());
        
        try {
            BigDecimal riskScore = assessment.getRiskScore();
            if (riskScore == null) {
                riskScore = BigDecimal.ZERO;
            }
            
            // Check approval threshold
            if (thresholds.containsKey("approveThreshold")) {
                BigDecimal approveThreshold = new BigDecimal(thresholds.get("approveThreshold").toString());
                if (riskScore.compareTo(approveThreshold) <= 0) {
                    return FraudRiskAssessment.Decision.APPROVE;
                }
            }
            
            // Check rejection threshold
            if (thresholds.containsKey("rejectThreshold")) {
                BigDecimal rejectThreshold = new BigDecimal(thresholds.get("rejectThreshold").toString());
                if (riskScore.compareTo(rejectThreshold) >= 0) {
                    return FraudRiskAssessment.Decision.REJECT;
                }
            }
            
            // Check manual review threshold
            if (thresholds.containsKey("manualReviewThreshold")) {
                BigDecimal manualReviewThreshold = new BigDecimal(thresholds.get("manualReviewThreshold").toString());
                if (riskScore.compareTo(manualReviewThreshold) >= 0) {
                    return FraudRiskAssessment.Decision.MANUAL_REVIEW;
                }
            }
            
            // Check hold threshold
            if (thresholds.containsKey("holdThreshold")) {
                BigDecimal holdThreshold = new BigDecimal(thresholds.get("holdThreshold").toString());
                if (riskScore.compareTo(holdThreshold) >= 0) {
                    return FraudRiskAssessment.Decision.HOLD;
                }
            }
            
            // Check escalation threshold
            if (thresholds.containsKey("escalateThreshold")) {
                BigDecimal escalateThreshold = new BigDecimal(thresholds.get("escalateThreshold").toString());
                if (riskScore.compareTo(escalateThreshold) >= 0) {
                    return FraudRiskAssessment.Decision.ESCALATE;
                }
            }
            
            // Default to manual review if no thresholds match
            return FraudRiskAssessment.Decision.MANUAL_REVIEW;
            
        } catch (Exception e) {
            logger.error("Error evaluating thresholds: {}", e.getMessage(), e);
            return FraudRiskAssessment.Decision.MANUAL_REVIEW;
        }
    }
    
    @Override
    public BigDecimal calculateRiskScore(
            Map<String, Object> riskFactors,
            Map<String, Object> paymentData) {
        
        logger.debug("Calculating risk score from risk factors");
        
        try {
            BigDecimal totalRiskScore = BigDecimal.ZERO;
            int factorCount = 0;
            
            // Calculate risk score from individual factors
            for (Map.Entry<String, Object> entry : riskFactors.entrySet()) {
                String factorName = entry.getKey();
                Object factorValue = entry.getValue();
                
                if (factorValue instanceof BigDecimal) {
                    BigDecimal factorScore = (BigDecimal) factorValue;
                    totalRiskScore = totalRiskScore.add(factorScore);
                    factorCount++;
                } else if (factorValue instanceof Number) {
                    BigDecimal factorScore = new BigDecimal(factorValue.toString());
                    totalRiskScore = totalRiskScore.add(factorScore);
                    factorCount++;
                } else if (factorValue instanceof Map) {
                    Map<String, Object> factorMap = (Map<String, Object>) factorValue;
                    if (factorMap.containsKey("score")) {
                        BigDecimal factorScore = new BigDecimal(factorMap.get("score").toString());
                        totalRiskScore = totalRiskScore.add(factorScore);
                        factorCount++;
                    }
                }
            }
            
            // Calculate average risk score
            if (factorCount > 0) {
                totalRiskScore = totalRiskScore.divide(new BigDecimal(factorCount), 4, BigDecimal.ROUND_HALF_UP);
            }
            
            // Normalize risk score to 0-1 range
            if (totalRiskScore.compareTo(BigDecimal.ONE) > 0) {
                totalRiskScore = BigDecimal.ONE;
            }
            
            logger.debug("Calculated risk score: {}", totalRiskScore);
            return totalRiskScore;
            
        } catch (Exception e) {
            logger.error("Error calculating risk score: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
    
    @Override
    public FraudRiskAssessment.RiskLevel determineRiskLevel(BigDecimal riskScore) {
        if (riskScore.compareTo(new BigDecimal("0.8")) >= 0) {
            return FraudRiskAssessment.RiskLevel.CRITICAL;
        } else if (riskScore.compareTo(new BigDecimal("0.6")) >= 0) {
            return FraudRiskAssessment.RiskLevel.HIGH;
        } else if (riskScore.compareTo(new BigDecimal("0.3")) >= 0) {
            return FraudRiskAssessment.RiskLevel.MEDIUM;
        } else {
            return FraudRiskAssessment.RiskLevel.LOW;
        }
    }
    
    @Override
    public boolean evaluateCondition(String condition, Map<String, Object> context) {
        try {
            // Replace variables in condition with actual values
            String processedCondition = processCondition(condition, context);
            
            // Evaluate the condition
            return evaluateProcessedCondition(processedCondition);
            
        } catch (Exception e) {
            logger.error("Error evaluating condition: {}", condition, e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> applyRiskScoringRules(
            Map<String, Object> scoringRules,
            Map<String, Object> paymentData) {
        
        Map<String, Object> riskScores = new HashMap<>();
        
        try {
            for (Map.Entry<String, Object> entry : scoringRules.entrySet()) {
                String ruleName = entry.getKey();
                Object ruleConfig = entry.getValue();
                
                if (ruleConfig instanceof Map) {
                    Map<String, Object> rule = (Map<String, Object>) ruleConfig;
                    BigDecimal score = evaluateScoringRule(rule, paymentData);
                    riskScores.put(ruleName, score);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error applying risk scoring rules: {}", e.getMessage(), e);
        }
        
        return riskScores;
    }
    
    @Override
    public Map<String, Object> getRiskAssessmentStatistics(String tenantId) {
        // This would typically query the database for statistics
        // For now, return mock data
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalAssessments", 1000);
        statistics.put("averageRiskScore", 0.35);
        statistics.put("highRiskPercentage", 15.5);
        statistics.put("criticalRiskPercentage", 2.1);
        statistics.put("approvalRate", 78.3);
        statistics.put("rejectionRate", 12.7);
        statistics.put("manualReviewRate", 9.0);
        return statistics;
    }
    
    // Private helper methods
    
    private Map<String, Object> evaluateAmountRules(Map<String, Object> amountRules, Map<String, Object> paymentData) {
        Map<String, Object> riskFactors = new HashMap<>();
        
        try {
            Object amountObj = paymentData.get("amount");
            if (amountObj == null) {
                return riskFactors;
            }
            
            BigDecimal amount = new BigDecimal(amountObj.toString());
            
            // High amount risk
            if (amountRules.containsKey("highAmountThreshold")) {
                BigDecimal highAmountThreshold = new BigDecimal(amountRules.get("highAmountThreshold").toString());
                if (amount.compareTo(highAmountThreshold) > 0) {
                    riskFactors.put("highAmount", new BigDecimal("0.3"));
                }
            }
            
            // Very high amount risk
            if (amountRules.containsKey("veryHighAmountThreshold")) {
                BigDecimal veryHighAmountThreshold = new BigDecimal(amountRules.get("veryHighAmountThreshold").toString());
                if (amount.compareTo(veryHighAmountThreshold) > 0) {
                    riskFactors.put("veryHighAmount", new BigDecimal("0.5"));
                }
            }
            
            // Round amount risk
            if (amountRules.containsKey("roundAmountRisk")) {
                boolean roundAmountRisk = (Boolean) amountRules.get("roundAmountRisk");
                if (roundAmountRisk && isRoundAmount(amount)) {
                    riskFactors.put("roundAmount", new BigDecimal("0.1"));
                }
            }
            
        } catch (Exception e) {
            logger.error("Error evaluating amount rules: {}", e.getMessage(), e);
        }
        
        return riskFactors;
    }
    
    private Map<String, Object> evaluateFrequencyRules(Map<String, Object> frequencyRules, Map<String, Object> paymentData) {
        Map<String, Object> riskFactors = new HashMap<>();
        
        try {
            // This would typically query the database for frequency data
            // For now, use mock data
            
            String accountNumber = (String) paymentData.get("fromAccountNumber");
            if (accountNumber == null) {
                return riskFactors;
            }
            
            // Mock frequency data
            int dailyTransactionCount = 5; // This would come from database
            int weeklyTransactionCount = 25; // This would come from database
            
            // High frequency risk
            if (frequencyRules.containsKey("dailyThreshold")) {
                int dailyThreshold = (Integer) frequencyRules.get("dailyThreshold");
                if (dailyTransactionCount > dailyThreshold) {
                    riskFactors.put("highDailyFrequency", new BigDecimal("0.2"));
                }
            }
            
            // Very high frequency risk
            if (frequencyRules.containsKey("weeklyThreshold")) {
                int weeklyThreshold = (Integer) frequencyRules.get("weeklyThreshold");
                if (weeklyTransactionCount > weeklyThreshold) {
                    riskFactors.put("highWeeklyFrequency", new BigDecimal("0.3"));
                }
            }
            
        } catch (Exception e) {
            logger.error("Error evaluating frequency rules: {}", e.getMessage(), e);
        }
        
        return riskFactors;
    }
    
    private Map<String, Object> evaluateLocationRules(Map<String, Object> locationRules, Map<String, Object> paymentData) {
        Map<String, Object> riskFactors = new HashMap<>();
        
        try {
            String ipAddress = (String) paymentData.get("ipAddress");
            String country = (String) paymentData.get("country");
            
            if (ipAddress == null && country == null) {
                return riskFactors;
            }
            
            // High-risk country
            if (locationRules.containsKey("highRiskCountries")) {
                List<String> highRiskCountries = (List<String>) locationRules.get("highRiskCountries");
                if (country != null && highRiskCountries.contains(country)) {
                    riskFactors.put("highRiskCountry", new BigDecimal("0.4"));
                }
            }
            
            // Suspicious IP address
            if (locationRules.containsKey("suspiciousIpRanges")) {
                List<String> suspiciousIpRanges = (List<String>) locationRules.get("suspiciousIpRanges");
                if (ipAddress != null && isIpInRanges(ipAddress, suspiciousIpRanges)) {
                    riskFactors.put("suspiciousIp", new BigDecimal("0.3"));
                }
            }
            
        } catch (Exception e) {
            logger.error("Error evaluating location rules: {}", e.getMessage(), e);
        }
        
        return riskFactors;
    }
    
    private Map<String, Object> evaluateTimeRules(Map<String, Object> timeRules, Map<String, Object> paymentData) {
        Map<String, Object> riskFactors = new HashMap<>();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            int hour = now.getHour();
            int dayOfWeek = now.getDayOfWeek().getValue();
            
            // Off-hours risk
            if (timeRules.containsKey("offHoursRisk")) {
                boolean offHoursRisk = (Boolean) timeRules.get("offHoursRisk");
                if (offHoursRisk && (hour < 6 || hour > 22)) {
                    riskFactors.put("offHours", new BigDecimal("0.1"));
                }
            }
            
            // Weekend risk
            if (timeRules.containsKey("weekendRisk")) {
                boolean weekendRisk = (Boolean) timeRules.get("weekendRisk");
                if (weekendRisk && (dayOfWeek == 6 || dayOfWeek == 7)) {
                    riskFactors.put("weekend", new BigDecimal("0.1"));
                }
            }
            
        } catch (Exception e) {
            logger.error("Error evaluating time rules: {}", e.getMessage(), e);
        }
        
        return riskFactors;
    }
    
    private Map<String, Object> evaluateAccountRules(Map<String, Object> accountRules, Map<String, Object> paymentData) {
        Map<String, Object> riskFactors = new HashMap<>();
        
        try {
            String fromAccount = (String) paymentData.get("fromAccountNumber");
            String toAccount = (String) paymentData.get("toAccountNumber");
            
            if (fromAccount == null || toAccount == null) {
                return riskFactors;
            }
            
            // New account risk
            if (accountRules.containsKey("newAccountRisk")) {
                boolean newAccountRisk = (Boolean) accountRules.get("newAccountRisk");
                if (newAccountRisk && isNewAccount(fromAccount)) {
                    riskFactors.put("newAccount", new BigDecimal("0.2"));
                }
            }
            
            // Cross-border risk
            if (accountRules.containsKey("crossBorderRisk")) {
                boolean crossBorderRisk = (Boolean) accountRules.get("crossBorderRisk");
                if (crossBorderRisk && isCrossBorder(fromAccount, toAccount)) {
                    riskFactors.put("crossBorder", new BigDecimal("0.3"));
                }
            }
            
        } catch (Exception e) {
            logger.error("Error evaluating account rules: {}", e.getMessage(), e);
        }
        
        return riskFactors;
    }
    
    private Map<String, Object> evaluateDeviceRules(Map<String, Object> deviceRules, Map<String, Object> paymentData) {
        Map<String, Object> riskFactors = new HashMap<>();
        
        try {
            String deviceId = (String) paymentData.get("deviceId");
            String userAgent = (String) paymentData.get("userAgent");
            
            if (deviceId == null && userAgent == null) {
                return riskFactors;
            }
            
            // New device risk
            if (deviceRules.containsKey("newDeviceRisk")) {
                boolean newDeviceRisk = (Boolean) deviceRules.get("newDeviceRisk");
                if (newDeviceRisk && isNewDevice(deviceId)) {
                    riskFactors.put("newDevice", new BigDecimal("0.2"));
                }
            }
            
            // Suspicious user agent
            if (deviceRules.containsKey("suspiciousUserAgents")) {
                List<String> suspiciousUserAgents = (List<String>) deviceRules.get("suspiciousUserAgents");
                if (userAgent != null && isSuspiciousUserAgent(userAgent, suspiciousUserAgents)) {
                    riskFactors.put("suspiciousUserAgent", new BigDecimal("0.3"));
                }
            }
            
        } catch (Exception e) {
            logger.error("Error evaluating device rules: {}", e.getMessage(), e);
        }
        
        return riskFactors;
    }
    
    private Map<String, Object> evaluatePatternRules(Map<String, Object> patternRules, Map<String, Object> paymentData) {
        Map<String, Object> riskFactors = new HashMap<>();
        
        try {
            String transactionReference = (String) paymentData.get("transactionReference");
            String description = (String) paymentData.get("description");
            
            // Suspicious transaction reference pattern
            if (patternRules.containsKey("suspiciousReferencePatterns")) {
                List<String> suspiciousPatterns = (List<String>) patternRules.get("suspiciousReferencePatterns");
                if (transactionReference != null && matchesPatterns(transactionReference, suspiciousPatterns)) {
                    riskFactors.put("suspiciousReference", new BigDecimal("0.2"));
                }
            }
            
            // Suspicious description pattern
            if (patternRules.containsKey("suspiciousDescriptionPatterns")) {
                List<String> suspiciousPatterns = (List<String>) patternRules.get("suspiciousDescriptionPatterns");
                if (description != null && matchesPatterns(description, suspiciousPatterns)) {
                    riskFactors.put("suspiciousDescription", new BigDecimal("0.2"));
                }
            }
            
        } catch (Exception e) {
            logger.error("Error evaluating pattern rules: {}", e.getMessage(), e);
        }
        
        return riskFactors;
    }
    
    private boolean evaluateAutoApproveCriteria(Map<String, Object> autoApprove, FraudRiskAssessment assessment, Map<String, Object> paymentData) {
        try {
            // Check risk score threshold
            if (autoApprove.containsKey("maxRiskScore")) {
                BigDecimal maxRiskScore = new BigDecimal(autoApprove.get("maxRiskScore").toString());
                if (assessment.getRiskScore() != null && assessment.getRiskScore().compareTo(maxRiskScore) > 0) {
                    return false;
                }
            }
            
            // Check amount threshold
            if (autoApprove.containsKey("maxAmount")) {
                BigDecimal maxAmount = new BigDecimal(autoApprove.get("maxAmount").toString());
                Object amountObj = paymentData.get("amount");
                if (amountObj != null) {
                    BigDecimal amount = new BigDecimal(amountObj.toString());
                    if (amount.compareTo(maxAmount) > 0) {
                        return false;
                    }
                }
            }
            
            // Check other criteria
            return evaluateCriteria(autoApprove, paymentData);
            
        } catch (Exception e) {
            logger.error("Error evaluating auto-approve criteria: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private boolean evaluateAutoRejectCriteria(Map<String, Object> autoReject, FraudRiskAssessment assessment, Map<String, Object> paymentData) {
        try {
            // Check risk score threshold
            if (autoReject.containsKey("minRiskScore")) {
                BigDecimal minRiskScore = new BigDecimal(autoReject.get("minRiskScore").toString());
                if (assessment.getRiskScore() != null && assessment.getRiskScore().compareTo(minRiskScore) >= 0) {
                    return true;
                }
            }
            
            // Check amount threshold
            if (autoReject.containsKey("minAmount")) {
                BigDecimal minAmount = new BigDecimal(autoReject.get("minAmount").toString());
                Object amountObj = paymentData.get("amount");
                if (amountObj != null) {
                    BigDecimal amount = new BigDecimal(amountObj.toString());
                    if (amount.compareTo(minAmount) >= 0) {
                        return true;
                    }
                }
            }
            
            // Check other criteria
            return evaluateCriteria(autoReject, paymentData);
            
        } catch (Exception e) {
            logger.error("Error evaluating auto-reject criteria: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private boolean evaluateManualReviewCriteria(Map<String, Object> manualReview, FraudRiskAssessment assessment, Map<String, Object> paymentData) {
        try {
            // Check risk score threshold
            if (manualReview.containsKey("minRiskScore")) {
                BigDecimal minRiskScore = new BigDecimal(manualReview.get("minRiskScore").toString());
                if (assessment.getRiskScore() != null && assessment.getRiskScore().compareTo(minRiskScore) >= 0) {
                    return true;
                }
            }
            
            // Check other criteria
            return evaluateCriteria(manualReview, paymentData);
            
        } catch (Exception e) {
            logger.error("Error evaluating manual review criteria: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private boolean evaluateHoldCriteria(Map<String, Object> hold, FraudRiskAssessment assessment, Map<String, Object> paymentData) {
        try {
            return evaluateCriteria(hold, paymentData);
        } catch (Exception e) {
            logger.error("Error evaluating hold criteria: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private boolean evaluateEscalateCriteria(Map<String, Object> escalate, FraudRiskAssessment assessment, Map<String, Object> paymentData) {
        try {
            return evaluateCriteria(escalate, paymentData);
        } catch (Exception e) {
            logger.error("Error evaluating escalate criteria: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private boolean evaluateCriteria(Map<String, Object> criteria, Map<String, Object> paymentData) {
        try {
            for (Map.Entry<String, Object> entry : criteria.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (key.startsWith("condition_")) {
                    String condition = value.toString();
                    if (!evaluateCondition(condition, paymentData)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Error evaluating criteria: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private String processCondition(String condition, Map<String, Object> context) {
        String result = condition;
        
        // Replace field references: ${field}
        Pattern fieldPattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher fieldMatcher = fieldPattern.matcher(result);
        while (fieldMatcher.find()) {
            String fieldName = fieldMatcher.group(1);
            Object fieldValue = context.get(fieldName);
            result = result.replace(fieldMatcher.group(0), fieldValue != null ? fieldValue.toString() : "");
        }
        
        return result;
    }
    
    private boolean evaluateProcessedCondition(String condition) {
        try {
            // Simple condition evaluation
            if (condition.contains("==")) {
                String[] parts = condition.split("==");
                if (parts.length == 2) {
                    String left = parts[0].trim().replaceAll("'", "").replaceAll("\"", "");
                    String right = parts[1].trim().replaceAll("'", "").replaceAll("\"", "");
                    return left.equals(right);
                }
            } else if (condition.contains("!=")) {
                String[] parts = condition.split("!=");
                if (parts.length == 2) {
                    String left = parts[0].trim().replaceAll("'", "").replaceAll("\"", "");
                    String right = parts[1].trim().replaceAll("'", "").replaceAll("\"", "");
                    return !left.equals(right);
                }
            } else if (condition.contains(">")) {
                String[] parts = condition.split(">");
                if (parts.length == 2) {
                    try {
                        double left = Double.parseDouble(parts[0].trim());
                        double right = Double.parseDouble(parts[1].trim());
                        return left > right;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            } else if (condition.contains("<")) {
                String[] parts = condition.split("<");
                if (parts.length == 2) {
                    try {
                        double left = Double.parseDouble(parts[0].trim());
                        double right = Double.parseDouble(parts[1].trim());
                        return left < right;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Error evaluating processed condition: {}", condition, e);
            return false;
        }
    }
    
    private BigDecimal evaluateScoringRule(Map<String, Object> rule, Map<String, Object> paymentData) {
        try {
            if (rule.containsKey("score")) {
                return new BigDecimal(rule.get("score").toString());
            }
            
            if (rule.containsKey("condition") && rule.containsKey("scoreIfTrue")) {
                String condition = rule.get("condition").toString();
                if (evaluateCondition(condition, paymentData)) {
                    return new BigDecimal(rule.get("scoreIfTrue").toString());
                }
            }
            
            return BigDecimal.ZERO;
        } catch (Exception e) {
            logger.error("Error evaluating scoring rule: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
    
    // Helper methods for rule evaluation
    
    private boolean isRoundAmount(BigDecimal amount) {
        return amount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
    }
    
    private boolean isIpInRanges(String ipAddress, List<String> ipRanges) {
        // Simple IP range checking - in production, use proper IP range library
        for (String range : ipRanges) {
            if (ipAddress.startsWith(range)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isNewAccount(String accountNumber) {
        // Mock implementation - in production, query database
        return accountNumber.endsWith("999");
    }
    
    private boolean isCrossBorder(String fromAccount, String toAccount) {
        // Mock implementation - in production, check account countries
        return !fromAccount.substring(0, 2).equals(toAccount.substring(0, 2));
    }
    
    private boolean isNewDevice(String deviceId) {
        // Mock implementation - in production, query database
        return deviceId != null && deviceId.endsWith("NEW");
    }
    
    private boolean isSuspiciousUserAgent(String userAgent, List<String> suspiciousUserAgents) {
        for (String suspicious : suspiciousUserAgents) {
            if (userAgent.toLowerCase().contains(suspicious.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean matchesPatterns(String text, List<String> patterns) {
        for (String pattern : patterns) {
            if (text.matches(pattern)) {
                return true;
            }
        }
        return false;
    }
}