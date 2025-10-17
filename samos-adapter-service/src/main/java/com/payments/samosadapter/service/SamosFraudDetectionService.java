package com.payments.samosadapter.service;

import com.payments.samosadapter.domain.SamosAdapter;
import com.payments.samosadapter.dto.SamosFraudDetectionRequest;
import com.payments.samosadapter.dto.SamosFraudDetectionResponse;
import com.payments.domain.shared.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * SAMOS Fraud Detection Service
 *
 * <p>Service for executing fraud detection rules for SAMOS clearing network: - Transaction pattern analysis - Velocity checks - Geographic analysis - Behavioral analysis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SamosFraudDetectionService {

  /**
   * Execute fraud detection rules for SAMOS clearing network
   *
   * @param adapter SAMOS adapter
   * @param request Fraud detection request
   * @param tenantContext Tenant context
   * @return Fraud detection response
   */
  public SamosFraudDetectionResponse executeFraudDetectionRules(
      SamosAdapter adapter, SamosFraudDetectionRequest request, TenantContext tenantContext) {
    
    log.debug("Executing SAMOS fraud detection rules for adapter: {} and payment: {}", 
              adapter.getId(), request.getPaymentId());

    List<String> appliedRules = new ArrayList<>();
    List<String> fraudAlerts = new ArrayList<>();
    List<String> fraudWarnings = new ArrayList<>();
    boolean isFraudDetected = false;
    double fraudScore = 0.0;

    try {
      // Rule 1: Transaction velocity check for SAMOS
      executeSAMOSVelocityCheck(adapter, request, appliedRules, fraudAlerts, fraudWarnings);

      // Rule 2: Geographic analysis for SAMOS
      executeSAMOSGeographicAnalysis(adapter, request, appliedRules, fraudAlerts, fraudWarnings);

      // Rule 3: Behavioral analysis for SAMOS
      executeSAMOSBehavioralAnalysis(adapter, request, appliedRules, fraudAlerts, fraudWarnings);

      // Rule 4: Pattern analysis for SAMOS
      executeSAMOSPatternAnalysis(adapter, request, appliedRules, fraudAlerts, fraudWarnings);

      // Rule 5: SAMOS-specific fraud detection rules
      executeSAMOSSpecificFraudRules(adapter, request, appliedRules, fraudAlerts, fraudWarnings);

      // Calculate fraud score
      fraudScore = calculateFraudScore(fraudAlerts, fraudWarnings);
      isFraudDetected = fraudScore > 0.7; // Threshold for fraud detection

      log.info("SAMOS fraud detection completed: {} - Score: {}, Alerts: {}, Warnings: {}", 
               isFraudDetected ? "FRAUD_DETECTED" : "CLEAN", fraudScore, fraudAlerts.size(), fraudWarnings.size());

    } catch (Exception e) {
      log.error("Error during SAMOS fraud detection: {}", e.getMessage(), e);
      fraudAlerts.add("Fraud detection service error: " + e.getMessage());
      isFraudDetected = true;
      fraudScore = 1.0;
    }

    return SamosFraudDetectionResponse.builder()
        .adapterId(adapter.getId().toString())
        .paymentId(request.getPaymentId())
        .isFraudDetected(isFraudDetected)
        .fraudScore(fraudScore)
        .fraudStatus(isFraudDetected ? "FRAUD_DETECTED" : "CLEAN")
        .appliedRules(appliedRules)
        .fraudAlerts(fraudAlerts)
        .fraudWarnings(fraudWarnings)
        .detectedAt(Instant.now())
        .tenantId(tenantContext.getTenantId())
        .businessUnitId(tenantContext.getBusinessUnitId())
        .build();
  }

  /**
   * Execute velocity check for SAMOS transactions
   */
  private void executeSAMOSVelocityCheck(SamosAdapter adapter, SamosFraudDetectionRequest request, 
                                        List<String> appliedRules, List<String> fraudAlerts, 
                                        List<String> fraudWarnings) {
    appliedRules.add("SAMOS_VELOCITY_CHECK");
    
    // SAMOS handles high-value transactions - check for velocity anomalies
    if (request.getTransactionCount() != null && request.getTransactionCount() > 10) {
      fraudWarnings.add("SAMOS velocity check: High transaction count detected");
    }
    
    if (request.getTransactionAmount() != null && request.getTransactionAmount().doubleValue() > 1000000) {
      fraudWarnings.add("SAMOS velocity check: High-value transaction requires additional verification");
    }
    
    // Check for rapid successive transactions
    if (request.getTimeSinceLastTransaction() != null && 
        request.getTimeSinceLastTransaction() < 60) { // Less than 1 minute
      fraudAlerts.add("SAMOS velocity check: Rapid successive transactions detected");
    }
  }

  /**
   * Execute geographic analysis for SAMOS transactions
   */
  private void executeSAMOSGeographicAnalysis(SamosAdapter adapter, SamosFraudDetectionRequest request, 
                                             List<String> appliedRules, List<String> fraudAlerts, 
                                             List<String> fraudWarnings) {
    appliedRules.add("SAMOS_GEOGRAPHIC_ANALYSIS");
    
    // SAMOS is primarily for domestic transactions
    if (request.getBeneficiaryCountry() != null && !"ZA".equals(request.getBeneficiaryCountry())) {
      fraudWarnings.add("SAMOS geographic analysis: Cross-border transaction through SAMOS");
    }
    
    // Check for unusual geographic patterns
    if (request.getOriginCountry() != null && !"ZA".equals(request.getOriginCountry())) {
      fraudAlerts.add("SAMOS geographic analysis: Unusual origin country for SAMOS transaction");
    }
    
    // Check for high-risk countries
    if (request.getBeneficiaryCountry() != null && 
        isHighRiskCountry(request.getBeneficiaryCountry())) {
      fraudAlerts.add("SAMOS geographic analysis: High-risk beneficiary country");
    }
  }

  /**
   * Execute behavioral analysis for SAMOS transactions
   */
  private void executeSAMOSBehavioralAnalysis(SamosAdapter adapter, SamosFraudDetectionRequest request, 
                                            List<String> appliedRules, List<String> fraudAlerts, 
                                            List<String> fraudWarnings) {
    appliedRules.add("SAMOS_BEHAVIORAL_ANALYSIS");
    
    // Check for unusual transaction times (SAMOS operates during business hours)
    if (request.getTransactionTime() != null && 
        !isWithinSAMOSBusinessHours(request.getTransactionTime())) {
      fraudWarnings.add("SAMOS behavioral analysis: Transaction outside business hours");
    }
    
    // Check for unusual transaction patterns
    if (request.getTransactionType() != null && 
        !isNormalSAMOSTransactionType(request.getTransactionType())) {
      fraudWarnings.add("SAMOS behavioral analysis: Unusual transaction type for SAMOS");
    }
    
    // Check for account behavior anomalies
    if (request.getAccountAge() != null && request.getAccountAge() < 30) {
      fraudWarnings.add("SAMOS behavioral analysis: New account with high-value transaction");
    }
  }

  /**
   * Execute pattern analysis for SAMOS transactions
   */
  private void executeSAMOSPatternAnalysis(SamosAdapter adapter, SamosFraudDetectionRequest request, 
                                        List<String> appliedRules, List<String> fraudAlerts, 
                                        List<String> fraudWarnings) {
    appliedRules.add("SAMOS_PATTERN_ANALYSIS");
    
    // Check for round number transactions (potential structuring)
    if (request.getTransactionAmount() != null && 
        isRoundNumber(request.getTransactionAmount().doubleValue())) {
      fraudWarnings.add("SAMOS pattern analysis: Round number transaction may indicate structuring");
    }
    
    // Check for unusual payment references
    if (request.getPaymentReference() != null && 
        request.getPaymentReference().length() < 5) {
      fraudWarnings.add("SAMOS pattern analysis: Suspicious payment reference");
    }
    
    // Check for duplicate transactions
    if (request.getIsDuplicateTransaction() != null && request.getIsDuplicateTransaction()) {
      fraudAlerts.add("SAMOS pattern analysis: Duplicate transaction detected");
    }
  }

  /**
   * Execute SAMOS-specific fraud detection rules
   */
  private void executeSAMOSSpecificFraudRules(SamosAdapter adapter, SamosFraudDetectionRequest request, 
                                             List<String> appliedRules, List<String> fraudAlerts, 
                                             List<String> fraudWarnings) {
    appliedRules.add("SAMOS_SPECIFIC_FRAUD_RULES");
    
    // Check SAMOS adapter status
    if (!adapter.getStatus().name().equals("ACTIVE")) {
      fraudAlerts.add("SAMOS specific rule: Adapter is not active");
    }
    
    // Check for encryption requirements
    if (!adapter.getEncryptionEnabled()) {
      fraudWarnings.add("SAMOS specific rule: Unencrypted high-value transaction");
    }
    
    // Check for API version compatibility
    if (adapter.getApiVersion() == null || adapter.getApiVersion().trim().isEmpty()) {
      fraudWarnings.add("SAMOS specific rule: Unknown API version");
    }
  }

  /**
   * Calculate fraud score based on alerts and warnings
   */
  private double calculateFraudScore(List<String> fraudAlerts, List<String> fraudWarnings) {
    double score = 0.0;
    
    // Each alert contributes 0.3 to the score
    score += fraudAlerts.size() * 0.3;
    
    // Each warning contributes 0.1 to the score
    score += fraudWarnings.size() * 0.1;
    
    // Cap the score at 1.0
    return Math.min(score, 1.0);
  }

  /**
   * Check if country is high-risk
   */
  private boolean isHighRiskCountry(String countryCode) {
    // Simplified high-risk country check
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

  /**
   * Check if transaction type is normal for SAMOS
   */
  private boolean isNormalSAMOSTransactionType(String transactionType) {
    // SAMOS typically handles RTGS transactions
    return "RTGS".equals(transactionType) || "TRANSFER".equals(transactionType);
  }

  /**
   * Check if amount is a round number
   */
  private boolean isRoundNumber(double amount) {
    return amount % 1000 == 0; // Round thousands
  }
}
