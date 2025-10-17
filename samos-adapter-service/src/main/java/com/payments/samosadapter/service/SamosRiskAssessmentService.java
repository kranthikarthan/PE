package com.payments.samosadapter.service;

import com.payments.samosadapter.domain.SamosAdapter;
import com.payments.samosadapter.dto.SamosRiskAssessmentRequest;
import com.payments.samosadapter.dto.SamosRiskAssessmentResponse;
import com.payments.domain.shared.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * SAMOS Risk Assessment Service
 *
 * <p>Service for executing risk assessment rules for SAMOS clearing network: - Credit risk assessment - Market risk analysis - Operational risk evaluation - Counterparty risk assessment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SamosRiskAssessmentService {

  /**
   * Execute risk assessment rules for SAMOS clearing network
   *
   * @param adapter SAMOS adapter
   * @param request Risk assessment request
   * @param tenantContext Tenant context
   * @return Risk assessment response
   */
  public SamosRiskAssessmentResponse executeRiskAssessmentRules(
      SamosAdapter adapter, SamosRiskAssessmentRequest request, TenantContext tenantContext) {
    
    log.debug("Executing SAMOS risk assessment rules for adapter: {} and payment: {}", 
              adapter.getId(), request.getPaymentId());

    List<String> appliedRules = new ArrayList<>();
    List<String> riskAlerts = new ArrayList<>();
    List<String> riskWarnings = new ArrayList<>();
    boolean isHighRisk = false;
    double riskScore = 0.0;
    String riskLevel = "LOW";

    try {
      // Rule 1: Credit risk assessment for SAMOS
      executeSAMOSCreditRiskAssessment(adapter, request, appliedRules, riskAlerts, riskWarnings);

      // Rule 2: Market risk analysis for SAMOS
      executeSAMOSMarketRiskAnalysis(adapter, request, appliedRules, riskAlerts, riskWarnings);

      // Rule 3: Operational risk evaluation for SAMOS
      executeSAMOSOperationalRiskEvaluation(adapter, request, appliedRules, riskAlerts, riskWarnings);

      // Rule 4: Counterparty risk assessment for SAMOS
      executeSAMOSCounterpartyRiskAssessment(adapter, request, appliedRules, riskAlerts, riskWarnings);

      // Rule 5: SAMOS-specific risk assessment rules
      executeSAMOSSpecificRiskRules(adapter, request, appliedRules, riskAlerts, riskWarnings);

      // Calculate risk score and determine risk level
      riskScore = calculateRiskScore(riskAlerts, riskWarnings);
      riskLevel = determineRiskLevel(riskScore);
      isHighRisk = riskScore > 0.7; // Threshold for high risk

      log.info("SAMOS risk assessment completed: {} - Score: {}, Level: {}, Alerts: {}, Warnings: {}", 
               isHighRisk ? "HIGH_RISK" : "LOW_RISK", riskScore, riskLevel, riskAlerts.size(), riskWarnings.size());

    } catch (Exception e) {
      log.error("Error during SAMOS risk assessment: {}", e.getMessage(), e);
      riskAlerts.add("Risk assessment service error: " + e.getMessage());
      isHighRisk = true;
      riskScore = 1.0;
      riskLevel = "CRITICAL";
    }

    return SamosRiskAssessmentResponse.builder()
        .adapterId(adapter.getId().toString())
        .paymentId(request.getPaymentId())
        .isHighRisk(isHighRisk)
        .riskScore(riskScore)
        .riskLevel(riskLevel)
        .appliedRules(appliedRules)
        .riskAlerts(riskAlerts)
        .riskWarnings(riskWarnings)
        .assessedAt(Instant.now())
        .tenantId(tenantContext.getTenantId())
        .businessUnitId(tenantContext.getBusinessUnitId())
        .build();
  }

  /**
   * Execute credit risk assessment for SAMOS transactions
   */
  private void executeSAMOSCreditRiskAssessment(SamosAdapter adapter, SamosRiskAssessmentRequest request, 
                                             List<String> appliedRules, List<String> riskAlerts, 
                                             List<String> riskWarnings) {
    appliedRules.add("SAMOS_CREDIT_RISK_ASSESSMENT");
    
    // SAMOS handles high-value RTGS payments - enhanced credit risk assessment
    if (request.getTransactionAmount() != null && request.getTransactionAmount().doubleValue() > 1000000) { // R1M threshold
      riskWarnings.add("SAMOS credit risk: High-value transaction requires enhanced credit assessment");
    }
    
    // Check for credit exposure limits
    if (request.getCreditExposure() != null && request.getCreditExposure().doubleValue() > 5000000) { // R5M threshold
      riskAlerts.add("SAMOS credit risk: Credit exposure exceeds recommended limits");
    }
    
    // Check for credit rating
    if (request.getCounterpartyCreditRating() != null && 
        isLowCreditRating(request.getCounterpartyCreditRating())) {
      riskWarnings.add("SAMOS credit risk: Low credit rating counterparty");
    }
  }

  /**
   * Execute market risk analysis for SAMOS transactions
   */
  private void executeSAMOSMarketRiskAnalysis(SamosAdapter adapter, SamosRiskAssessmentRequest request, 
                                            List<String> appliedRules, List<String> riskAlerts, 
                                            List<String> riskWarnings) {
    appliedRules.add("SAMOS_MARKET_RISK_ANALYSIS");
    
    // Check for currency risk
    if (request.getCurrency() != null && !"ZAR".equals(request.getCurrency())) {
      riskWarnings.add("SAMOS market risk: Non-ZAR currency transaction");
    }
    
    // Check for interest rate risk
    if (request.getInterestRateRisk() != null && request.getInterestRateRisk() > 0.05) { // 5% threshold
      riskWarnings.add("SAMOS market risk: High interest rate risk detected");
    }
    
    // Check for liquidity risk
    if (request.getLiquidityRisk() != null && request.getLiquidityRisk() > 0.1) { // 10% threshold
      riskAlerts.add("SAMOS market risk: High liquidity risk detected");
    }
  }

  /**
   * Execute operational risk evaluation for SAMOS transactions
   */
  private void executeSAMOSOperationalRiskEvaluation(SamosAdapter adapter, SamosRiskAssessmentRequest request, 
                                                    List<String> appliedRules, List<String> riskAlerts, 
                                                    List<String> riskWarnings) {
    appliedRules.add("SAMOS_OPERATIONAL_RISK_EVALUATION");
    
    // Check for system availability
    if (!adapter.getStatus().name().equals("ACTIVE")) {
      riskAlerts.add("SAMOS operational risk: Adapter is not active");
    }
    
    // Check for encryption requirements
    if (!adapter.getEncryptionEnabled()) {
      riskWarnings.add("SAMOS operational risk: Unencrypted high-value transaction");
    }
    
    // Check for timeout configuration
    if (adapter.getTimeoutSeconds() != null && adapter.getTimeoutSeconds() < 30) {
      riskWarnings.add("SAMOS operational risk: Short timeout configuration may cause operational issues");
    }
  }

  /**
   * Execute counterparty risk assessment for SAMOS transactions
   */
  private void executeSAMOSCounterpartyRiskAssessment(SamosAdapter adapter, SamosRiskAssessmentRequest request, 
                                                     List<String> appliedRules, List<String> riskAlerts, 
                                                     List<String> riskWarnings) {
    appliedRules.add("SAMOS_COUNTERPARTY_RISK_ASSESSMENT");
    
    // Check for counterparty reputation
    if (request.getCounterpartyReputation() != null && 
        isLowReputation(request.getCounterpartyReputation())) {
      riskWarnings.add("SAMOS counterparty risk: Low reputation counterparty");
    }
    
    // Check for counterparty financial strength
    if (request.getCounterpartyFinancialStrength() != null && 
        request.getCounterpartyFinancialStrength() < 0.6) { // 60% threshold
      riskAlerts.add("SAMOS counterparty risk: Weak financial strength counterparty");
    }
    
    // Check for counterparty country risk
    if (request.getCounterpartyCountry() != null && 
        isHighRiskCountry(request.getCounterpartyCountry())) {
      riskAlerts.add("SAMOS counterparty risk: High-risk country counterparty");
    }
  }

  /**
   * Execute SAMOS-specific risk assessment rules
   */
  private void executeSAMOSSpecificRiskRules(SamosAdapter adapter, SamosRiskAssessmentRequest request, 
                                            List<String> appliedRules, List<String> riskAlerts, 
                                            List<String> riskWarnings) {
    appliedRules.add("SAMOS_SPECIFIC_RISK_RULES");
    
    // Check for SAMOS business hours
    if (request.getTransactionTime() != null && 
        !isWithinSAMOSBusinessHours(request.getTransactionTime())) {
      riskWarnings.add("SAMOS specific risk: Transaction outside business hours");
    }
    
    // Check for SAMOS participant status
    if (request.getParticipantStatus() != null && 
        !"ACTIVE".equals(request.getParticipantStatus())) {
      riskAlerts.add("SAMOS specific risk: Inactive participant");
    }
    
    // Check for regulatory compliance
    if (request.getRegulatoryCompliance() != null && 
        !request.getRegulatoryCompliance()) {
      riskAlerts.add("SAMOS specific risk: Regulatory compliance issues");
    }
  }

  /**
   * Calculate risk score based on alerts and warnings
   */
  private double calculateRiskScore(List<String> riskAlerts, List<String> riskWarnings) {
    double score = 0.0;
    
    // Each alert contributes 0.4 to the score
    score += riskAlerts.size() * 0.4;
    
    // Each warning contributes 0.15 to the score
    score += riskWarnings.size() * 0.15;
    
    // Cap the score at 1.0
    return Math.min(score, 1.0);
  }

  /**
   * Determine risk level based on risk score
   */
  private String determineRiskLevel(double riskScore) {
    if (riskScore >= 0.8) {
      return "CRITICAL";
    } else if (riskScore >= 0.6) {
      return "HIGH";
    } else if (riskScore >= 0.4) {
      return "MEDIUM";
    } else if (riskScore >= 0.2) {
      return "LOW";
    } else {
      return "MINIMAL";
    }
  }

  /**
   * Check if credit rating is low
   */
  private boolean isLowCreditRating(String creditRating) {
    return "C".equals(creditRating) || "D".equals(creditRating) || "F".equals(creditRating);
  }

  /**
   * Check if reputation is low
   */
  private boolean isLowReputation(String reputation) {
    return "LOW".equals(reputation) || "POOR".equals(reputation);
  }

  /**
   * Check if country is high-risk
   */
  private boolean isHighRiskCountry(String countryCode) {
    return "XX".equals(countryCode) || "HIGH_RISK".equals(countryCode);
  }

  /**
   * Check if within SAMOS business hours
   */
  private boolean isWithinSAMOSBusinessHours(Instant transactionTime) {
    // SAMOS operates during business hours (8 AM - 4 PM SAST)
    // Simplified check - in real implementation, this would check actual business hours
    return true; // Simplified for now
  }
}
