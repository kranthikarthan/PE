package com.paymentengine.middleware.service.impl;

import com.paymentengine.middleware.entity.FraudRiskAssessment;
import com.paymentengine.middleware.entity.FraudRiskConfiguration;
import com.paymentengine.middleware.repository.FraudRiskAssessmentRepository;
import com.paymentengine.middleware.repository.FraudRiskConfigurationRepository;
import com.paymentengine.middleware.service.FraudRiskMonitoringService;
import com.paymentengine.middleware.service.ExternalFraudApiService;
import com.paymentengine.middleware.service.RiskAssessmentEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of fraud/risk monitoring service
 */
@Service
@Transactional
public class FraudRiskMonitoringServiceImpl implements FraudRiskMonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(FraudRiskMonitoringServiceImpl.class);
    
    @Autowired
    private FraudRiskConfigurationRepository fraudRiskConfigurationRepository;
    
    @Autowired
    private FraudRiskAssessmentRepository fraudRiskAssessmentRepository;
    
    @Autowired
    private ExternalFraudApiService externalFraudApiService;
    
    @Autowired
    private RiskAssessmentEngine riskAssessmentEngine;
    
    @Override
    public CompletableFuture<FraudRiskAssessment> assessPaymentRisk(
            String transactionReference,
            String tenantId,
            String paymentType,
            String localInstrumentationCode,
            String clearingSystemCode,
            FraudRiskConfiguration.PaymentSource paymentSource,
            Map<String, Object> paymentData) {
        
        logger.info("Starting fraud/risk assessment for transaction: {}, tenant: {}, paymentType: {}, paymentSource: {}", 
                   transactionReference, tenantId, paymentType, paymentSource);
        
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            String assessmentId = "FRAUD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
            
            try {
                // Create assessment record
                FraudRiskAssessment assessment = new FraudRiskAssessment(
                        assessmentId, transactionReference, tenantId, paymentSource, 
                        FraudRiskConfiguration.RiskAssessmentType.REAL_TIME);
                
                assessment.setPaymentType(paymentType);
                assessment.setLocalInstrumentationCode(localInstrumentationCode);
                assessment.setClearingSystemCode(clearingSystemCode);
                assessment.setStatus(FraudRiskAssessment.AssessmentStatus.IN_PROGRESS);
                assessment.setAssessedAt(LocalDateTime.now());
                
                // Save initial assessment
                assessment = fraudRiskAssessmentRepository.save(assessment);
                
                // Get applicable configurations
                List<FraudRiskConfiguration> configurations = getApplicableConfigurations(
                        tenantId, paymentType, localInstrumentationCode, clearingSystemCode, paymentSource);
                
                if (configurations.isEmpty()) {
                    logger.warn("No fraud/risk configurations found for transaction: {}, using default approval", transactionReference);
                    assessment.setStatus(FraudRiskAssessment.AssessmentStatus.COMPLETED);
                    assessment.setDecision(FraudRiskAssessment.Decision.APPROVE);
                    assessment.setDecisionReason("No fraud/risk configuration found - default approval");
                    assessment.setRiskLevel(FraudRiskAssessment.RiskLevel.LOW);
                    assessment.setRiskScore(BigDecimal.ZERO);
                    assessment.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                    
                    return fraudRiskAssessmentRepository.save(assessment);
                }
                
                // Apply configurations in priority order
                FraudRiskAssessment finalAssessment = assessment;
                for (FraudRiskConfiguration config : configurations) {
                    finalAssessment = applyConfiguration(finalAssessment, config, paymentData);
                    
                    // If decision is made, break the loop
                    if (finalAssessment.getDecision() != null && 
                        finalAssessment.getDecision() != FraudRiskAssessment.Decision.MANUAL_REVIEW) {
                        break;
                    }
                }
                
                // If no decision made, default to manual review
                if (finalAssessment.getDecision() == null) {
                    finalAssessment.setDecision(FraudRiskAssessment.Decision.MANUAL_REVIEW);
                    finalAssessment.setDecisionReason("No automatic decision could be made - requires manual review");
                }
                
                finalAssessment.setStatus(FraudRiskAssessment.AssessmentStatus.COMPLETED);
                finalAssessment.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                
                logger.info("Completed fraud/risk assessment for transaction: {}, decision: {}, riskLevel: {}, riskScore: {}", 
                           transactionReference, finalAssessment.getDecision(), finalAssessment.getRiskLevel(), finalAssessment.getRiskScore());
                
                return fraudRiskAssessmentRepository.save(finalAssessment);
                
            } catch (Exception e) {
                logger.error("Error during fraud/risk assessment for transaction: {}: {}", transactionReference, e.getMessage(), e);
                
                // Update assessment with error
                try {
                    FraudRiskAssessment errorAssessment = fraudRiskAssessmentRepository.findByAssessmentId(assessmentId)
                            .orElse(assessment);
                    errorAssessment.setStatus(FraudRiskAssessment.AssessmentStatus.ERROR);
                    errorAssessment.setErrorMessage(e.getMessage());
                    errorAssessment.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                    
                    return fraudRiskAssessmentRepository.save(errorAssessment);
                } catch (Exception saveError) {
                    logger.error("Error saving failed assessment: {}", saveError.getMessage());
                    throw new RuntimeException("Failed to save fraud/risk assessment", saveError);
                }
            }
        });
    }
    
    /**
     * Apply fraud/risk configuration to assessment
     */
    private FraudRiskAssessment applyConfiguration(
            FraudRiskAssessment assessment,
            FraudRiskConfiguration config,
            Map<String, Object> paymentData) {
        
        logger.debug("Applying fraud/risk configuration: {} to assessment: {}", 
                    config.getConfigurationName(), assessment.getAssessmentId());
        
        try {
            assessment.setConfigurationId(config.getId());
            
            // Apply risk rules if present
            if (config.hasRiskRules()) {
                Map<String, Object> riskFactors = riskAssessmentEngine.evaluateRiskRules(
                        config.getRiskRules(), paymentData, assessment);
                assessment.setRiskFactors(riskFactors);
            }
            
            // Call external fraud API if configured
            if (config.hasExternalApiConfig()) {
                long apiStartTime = System.currentTimeMillis();
                
                try {
                    Map<String, Object> apiRequest = externalFraudApiService.buildApiRequest(
                            config.getExternalApiConfig(), paymentData, assessment);
                    assessment.setExternalApiRequest(apiRequest);
                    
                    Map<String, Object> apiResponse = externalFraudApiService.callExternalApi(
                            config.getExternalApiConfig(), apiRequest);
                    assessment.setExternalApiResponse(apiResponse);
                    
                    assessment.setExternalApiResponseTimeMs(System.currentTimeMillis() - apiStartTime);
                    assessment.setExternalApiUsed((String) config.getExternalApiConfig().get("apiName"));
                    
                    // Process external API response
                    processExternalApiResponse(assessment, apiResponse, config);
                    
                } catch (Exception e) {
                    logger.error("Error calling external fraud API: {}", e.getMessage());
                    assessment.setErrorMessage("External API call failed: " + e.getMessage());
                    
                    // Apply fallback configuration if available
                    if (config.getFallbackConfig() != null) {
                        applyFallbackConfiguration(assessment, config.getFallbackConfig());
                    } else {
                        assessment.setDecision(FraudRiskAssessment.Decision.MANUAL_REVIEW);
                        assessment.setDecisionReason("External API failed and no fallback configured");
                    }
                }
            }
            
            // Apply decision criteria if no decision made yet
            if (assessment.getDecision() == null && config.hasDecisionCriteria()) {
                FraudRiskAssessment.Decision decision = riskAssessmentEngine.evaluateDecisionCriteria(
                        config.getDecisionCriteria(), assessment, paymentData);
                assessment.setDecision(decision);
                assessment.setDecisionReason("Decision based on configured criteria");
            }
            
            // Apply thresholds if no decision made yet
            if (assessment.getDecision() == null && config.hasThresholds()) {
                FraudRiskAssessment.Decision decision = riskAssessmentEngine.evaluateThresholds(
                        config.getThresholds(), assessment, paymentData);
                assessment.setDecision(decision);
                assessment.setDecisionReason("Decision based on configured thresholds");
            }
            
            // Set risk level based on risk score
            if (assessment.getRiskScore() != null) {
                assessment.setRiskLevel(determineRiskLevel(assessment.getRiskScore()));
            }
            
            // Set expiration time if configured
            if (config.getTimeoutConfig() != null) {
                Integer timeoutMinutes = (Integer) config.getTimeoutConfig().get("timeoutMinutes");
                if (timeoutMinutes != null && timeoutMinutes > 0) {
                    assessment.setExpiresAt(LocalDateTime.now().plusMinutes(timeoutMinutes));
                }
            }
            
            return assessment;
            
        } catch (Exception e) {
            logger.error("Error applying fraud/risk configuration: {}", e.getMessage(), e);
            assessment.setErrorMessage("Configuration application failed: " + e.getMessage());
            assessment.setDecision(FraudRiskAssessment.Decision.MANUAL_REVIEW);
            assessment.setDecisionReason("Configuration application failed - requires manual review");
            return assessment;
        }
    }
    
    /**
     * Process external API response
     */
    private void processExternalApiResponse(
            FraudRiskAssessment assessment,
            Map<String, Object> apiResponse,
            FraudRiskConfiguration config) {
        
        try {
            // Extract risk score from API response
            Object riskScoreObj = apiResponse.get("riskScore");
            if (riskScoreObj != null) {
                BigDecimal riskScore = new BigDecimal(riskScoreObj.toString());
                assessment.setRiskScore(riskScore);
            }
            
            // Extract risk level from API response
            Object riskLevelObj = apiResponse.get("riskLevel");
            if (riskLevelObj != null) {
                String riskLevelStr = riskLevelObj.toString().toUpperCase();
                try {
                    assessment.setRiskLevel(FraudRiskAssessment.RiskLevel.valueOf(riskLevelStr));
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid risk level from API: {}", riskLevelStr);
                }
            }
            
            // Extract decision from API response
            Object decisionObj = apiResponse.get("decision");
            if (decisionObj != null) {
                String decisionStr = decisionObj.toString().toUpperCase();
                try {
                    assessment.setDecision(FraudRiskAssessment.Decision.valueOf(decisionStr));
                    assessment.setDecisionReason("Decision from external fraud API");
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid decision from API: {}", decisionStr);
                }
            }
            
            // Extract additional assessment details
            Object assessmentDetailsObj = apiResponse.get("assessmentDetails");
            if (assessmentDetailsObj instanceof Map) {
                assessment.setAssessmentDetails((Map<String, Object>) assessmentDetailsObj);
            }
            
        } catch (Exception e) {
            logger.error("Error processing external API response: {}", e.getMessage(), e);
            assessment.setErrorMessage("Failed to process external API response: " + e.getMessage());
        }
    }
    
    /**
     * Apply fallback configuration
     */
    private void applyFallbackConfiguration(
            FraudRiskAssessment assessment,
            Map<String, Object> fallbackConfig) {
        
        try {
            Object fallbackDecision = fallbackConfig.get("decision");
            if (fallbackDecision != null) {
                String decisionStr = fallbackDecision.toString().toUpperCase();
                try {
                    assessment.setDecision(FraudRiskAssessment.Decision.valueOf(decisionStr));
                    assessment.setDecisionReason("Fallback decision applied");
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid fallback decision: {}", decisionStr);
                }
            }
            
            Object fallbackRiskLevel = fallbackConfig.get("riskLevel");
            if (fallbackRiskLevel != null) {
                String riskLevelStr = fallbackRiskLevel.toString().toUpperCase();
                try {
                    assessment.setRiskLevel(FraudRiskAssessment.RiskLevel.valueOf(riskLevelStr));
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid fallback risk level: {}", riskLevelStr);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error applying fallback configuration: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Determine risk level based on risk score
     */
    private FraudRiskAssessment.RiskLevel determineRiskLevel(BigDecimal riskScore) {
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
    public Optional<FraudRiskAssessment> getAssessmentById(String assessmentId) {
        return fraudRiskAssessmentRepository.findByAssessmentId(assessmentId);
    }
    
    @Override
    public Optional<FraudRiskAssessment> getAssessmentByTransactionReference(String transactionReference) {
        List<FraudRiskAssessment> assessments = fraudRiskAssessmentRepository.findByTransactionReference(transactionReference);
        return assessments.isEmpty() ? Optional.empty() : Optional.of(assessments.get(0));
    }
    
    @Override
    @Cacheable(value = "fraud-risk-configurations", key = "#tenantId + '_' + #paymentType + '_' + #localInstrumentationCode + '_' + #clearingSystemCode + '_' + #paymentSource")
    public List<FraudRiskConfiguration> getApplicableConfigurations(
            String tenantId,
            String paymentType,
            String localInstrumentationCode,
            String clearingSystemCode,
            FraudRiskConfiguration.PaymentSource paymentSource) {
        
        List<FraudRiskConfiguration> allConfigurations = fraudRiskConfigurationRepository.findActiveByTenantId(tenantId);
        
        return allConfigurations.stream()
            .filter(config -> config.matchesCriteria(tenantId, paymentType, localInstrumentationCode, clearingSystemCode, paymentSource))
            .sorted(Comparator.comparing(FraudRiskConfiguration::getPriority))
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean isFraudRiskMonitoringEnabled(
            String tenantId,
            String paymentType,
            String localInstrumentationCode,
            String clearingSystemCode,
            FraudRiskConfiguration.PaymentSource paymentSource) {
        
        List<FraudRiskConfiguration> configurations = getApplicableConfigurations(
                tenantId, paymentType, localInstrumentationCode, clearingSystemCode, paymentSource);
        
        return !configurations.isEmpty();
    }
    
    @Override
    public Map<String, Object> getAssessmentStatistics(String tenantId) {
        return getAssessmentStatistics(tenantId, LocalDateTime.now().minusDays(30), LocalDateTime.now());
    }
    
    @Override
    public Map<String, Object> getAssessmentStatistics(
            String tenantId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        Map<String, Object> statistics = new HashMap<>();
        
        // Total assessments
        long totalAssessments = fraudRiskAssessmentRepository.countByTenantIdAndAssessedAtBetween(tenantId, startDate, endDate);
        statistics.put("totalAssessments", totalAssessments);
        
        // Assessments by status
        statistics.put("pendingAssessments", fraudRiskAssessmentRepository.countPendingAssessmentsByTenantId(tenantId));
        statistics.put("completedAssessments", fraudRiskAssessmentRepository.countCompletedAssessmentsByTenantId(tenantId));
        statistics.put("failedAssessments", fraudRiskAssessmentRepository.countFailedAssessmentsByTenantId(tenantId));
        
        // Assessments by decision
        statistics.put("approvedAssessments", fraudRiskAssessmentRepository.countApprovedAssessmentsByTenantId(tenantId));
        statistics.put("rejectedAssessments", fraudRiskAssessmentRepository.countRejectedAssessmentsByTenantId(tenantId));
        statistics.put("manualReviewAssessments", fraudRiskAssessmentRepository.countManualReviewAssessmentsByTenantId(tenantId));
        
        // Assessments by risk level
        statistics.put("highRiskAssessments", fraudRiskAssessmentRepository.countHighRiskAssessmentsByTenantId(tenantId));
        statistics.put("criticalRiskAssessments", fraudRiskAssessmentRepository.countCriticalRiskAssessmentsByTenantId(tenantId));
        
        // Calculate percentages
        if (totalAssessments > 0) {
            statistics.put("approvalRate", (double) fraudRiskAssessmentRepository.countApprovedAssessmentsByTenantId(tenantId) / totalAssessments * 100);
            statistics.put("rejectionRate", (double) fraudRiskAssessmentRepository.countRejectedAssessmentsByTenantId(tenantId) / totalAssessments * 100);
            statistics.put("manualReviewRate", (double) fraudRiskAssessmentRepository.countManualReviewAssessmentsByTenantId(tenantId) / totalAssessments * 100);
            statistics.put("highRiskRate", (double) fraudRiskAssessmentRepository.countHighRiskAssessmentsByTenantId(tenantId) / totalAssessments * 100);
        }
        
        statistics.put("startDate", startDate);
        statistics.put("endDate", endDate);
        statistics.put("generatedAt", LocalDateTime.now());
        
        return statistics;
    }
    
    @Override
    public CompletableFuture<FraudRiskAssessment> retryAssessment(String assessmentId) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<FraudRiskAssessment> assessmentOpt = fraudRiskAssessmentRepository.findByAssessmentId(assessmentId);
            if (!assessmentOpt.isPresent()) {
                throw new RuntimeException("Assessment not found: " + assessmentId);
            }
            
            FraudRiskAssessment assessment = assessmentOpt.get();
            assessment.setRetryCount(assessment.getRetryCount() + 1);
            assessment.setStatus(FraudRiskAssessment.AssessmentStatus.IN_PROGRESS);
            assessment.setErrorMessage(null);
            
            // Retry the assessment logic here
            // This would involve re-running the assessment process
            
            return fraudRiskAssessmentRepository.save(assessment);
        });
    }
    
    @Override
    public boolean cancelAssessment(String assessmentId) {
        Optional<FraudRiskAssessment> assessmentOpt = fraudRiskAssessmentRepository.findByAssessmentId(assessmentId);
        if (!assessmentOpt.isPresent()) {
            return false;
        }
        
        FraudRiskAssessment assessment = assessmentOpt.get();
        if (assessment.getStatus() == FraudRiskAssessment.AssessmentStatus.PENDING || 
            assessment.getStatus() == FraudRiskAssessment.AssessmentStatus.IN_PROGRESS) {
            
            assessment.setStatus(FraudRiskAssessment.AssessmentStatus.CANCELLED);
            fraudRiskAssessmentRepository.save(assessment);
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean updateAssessmentDecision(String assessmentId, FraudRiskAssessment.Decision decision, String reason) {
        Optional<FraudRiskAssessment> assessmentOpt = fraudRiskAssessmentRepository.findByAssessmentId(assessmentId);
        if (!assessmentOpt.isPresent()) {
            return false;
        }
        
        FraudRiskAssessment assessment = assessmentOpt.get();
        assessment.setDecision(decision);
        assessment.setDecisionReason(reason);
        assessment.setStatus(FraudRiskAssessment.AssessmentStatus.COMPLETED);
        
        fraudRiskAssessmentRepository.save(assessment);
        return true;
    }
    
    @Override
    public List<FraudRiskAssessment> getPendingManualReviews(String tenantId) {
        return fraudRiskAssessmentRepository.findManualReviewAssessmentsByTenantId(tenantId);
    }
    
    @Override
    public List<FraudRiskAssessment> getHighRiskAssessments(String tenantId) {
        return fraudRiskAssessmentRepository.findHighRiskAssessmentsByTenantId(tenantId);
    }
    
    @Override
    public List<FraudRiskAssessment> getCriticalRiskAssessments(String tenantId) {
        return fraudRiskAssessmentRepository.findCriticalRiskAssessmentsByTenantId(tenantId);
    }
    
    @Override
    public List<FraudRiskAssessment> getAssessmentsNeedingRetry(String tenantId) {
        return fraudRiskAssessmentRepository.findByTenantIdAndAssessmentsNeedingRetry(tenantId, 3);
    }
    
    @Override
    public List<FraudRiskAssessment> getExpiredAssessments() {
        return fraudRiskAssessmentRepository.findExpiredAssessments(LocalDateTime.now());
    }
    
    @Override
    public int cleanupExpiredAssessments() {
        List<FraudRiskAssessment> expiredAssessments = getExpiredAssessments();
        fraudRiskAssessmentRepository.deleteAll(expiredAssessments);
        return expiredAssessments.size();
    }
    
    @Override
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Check database connectivity
            long configCount = fraudRiskConfigurationRepository.count();
            long assessmentCount = fraudRiskAssessmentRepository.count();
            
            health.put("status", "UP");
            health.put("database", "UP");
            health.put("configurationsCount", configCount);
            health.put("assessmentsCount", assessmentCount);
            health.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", LocalDateTime.now());
        }
        
        return health;
    }
    
    @Override
    public Map<String, Object> getMetrics(String tenantId) {
        return getMetrics(tenantId, LocalDateTime.now().minusDays(7), LocalDateTime.now());
    }
    
    @Override
    public Map<String, Object> getMetrics(
            String tenantId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Performance metrics
        List<FraudRiskAssessment> assessments = fraudRiskAssessmentRepository.findByTenantIdAndAssessedAtBetween(tenantId, startDate, endDate);
        
        if (!assessments.isEmpty()) {
            double avgProcessingTime = assessments.stream()
                    .filter(a -> a.getProcessingTimeMs() != null)
                    .mapToLong(FraudRiskAssessment::getProcessingTimeMs)
                    .average()
                    .orElse(0.0);
            
            double avgApiResponseTime = assessments.stream()
                    .filter(a -> a.getExternalApiResponseTimeMs() != null)
                    .mapToLong(FraudRiskAssessment::getExternalApiResponseTimeMs)
                    .average()
                    .orElse(0.0);
            
            metrics.put("averageProcessingTimeMs", avgProcessingTime);
            metrics.put("averageApiResponseTimeMs", avgApiResponseTime);
            metrics.put("totalAssessments", assessments.size());
        }
        
        // Risk distribution
        Map<FraudRiskAssessment.RiskLevel, Long> riskDistribution = assessments.stream()
                .filter(a -> a.getRiskLevel() != null)
                .collect(Collectors.groupingBy(FraudRiskAssessment::getRiskLevel, Collectors.counting()));
        
        metrics.put("riskDistribution", riskDistribution);
        
        // Decision distribution
        Map<FraudRiskAssessment.Decision, Long> decisionDistribution = assessments.stream()
                .filter(a -> a.getDecision() != null)
                .collect(Collectors.groupingBy(FraudRiskAssessment::getDecision, Collectors.counting()));
        
        metrics.put("decisionDistribution", decisionDistribution);
        
        metrics.put("startDate", startDate);
        metrics.put("endDate", endDate);
        metrics.put("generatedAt", LocalDateTime.now());
        
        return metrics;
    }
}