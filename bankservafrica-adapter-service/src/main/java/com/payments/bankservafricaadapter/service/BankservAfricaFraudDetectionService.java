package com.payments.bankservafricaadapter.service;

import com.payments.bankservafricaadapter.domain.BankservAfricaAdapter;
import com.payments.bankservafricaadapter.dto.BankservAfricaFraudDetectionRequest;
import com.payments.bankservafricaadapter.dto.BankservAfricaFraudDetectionResponse;
import com.payments.domain.shared.TenantContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * BankservAfrica Fraud Detection Service
 *
 * <p>Service for executing fraud detection rules for BankservAfrica clearing network: - Transaction
 * pattern analysis - Velocity checks - Geographic analysis - Behavioral analysis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankservAfricaFraudDetectionService {

  /**
   * Execute fraud detection rules for BankservAfrica clearing network
   *
   * @param adapter BankservAfrica adapter
   * @param request Fraud detection request
   * @param tenantContext Tenant context
   * @return Fraud detection response
   */
  public BankservAfricaFraudDetectionResponse executeFraudDetectionRules(
      BankservAfricaAdapter adapter,
      BankservAfricaFraudDetectionRequest request,
      TenantContext tenantContext) {

    log.debug(
        "Executing BankservAfrica fraud detection rules for adapter: {} and payment: {}",
        adapter.getId(),
        request.getPaymentId());

    List<String> appliedRules = new ArrayList<>();
    List<String> fraudAlerts = new ArrayList<>();
    List<String> fraudWarnings = new ArrayList<>();
    boolean isFraudDetected = false;
    double fraudScore = 0.0;

    try {
      // Rule 1: Transaction velocity check for BankservAfrica
      executeBankservAfricaVelocityCheck(
          adapter, request, appliedRules, fraudAlerts, fraudWarnings);

      // Rule 2: Geographic analysis for BankservAfrica
      executeBankservAfricaGeographicAnalysis(
          adapter, request, appliedRules, fraudAlerts, fraudWarnings);

      // Rule 3: Behavioral analysis for BankservAfrica
      executeBankservAfricaBehavioralAnalysis(
          adapter, request, appliedRules, fraudAlerts, fraudWarnings);

      // Rule 4: Pattern analysis for BankservAfrica
      executeBankservAfricaPatternAnalysis(
          adapter, request, appliedRules, fraudAlerts, fraudWarnings);

      // Rule 5: BankservAfrica-specific fraud detection rules
      executeBankservAfricaSpecificFraudRules(
          adapter, request, appliedRules, fraudAlerts, fraudWarnings);

      // Calculate fraud score
      fraudScore = calculateFraudScore(fraudAlerts, fraudWarnings);
      isFraudDetected = fraudScore > 0.7; // Threshold for fraud detection

      log.info(
          "BankservAfrica fraud detection completed: {} - Score: {}, Alerts: {}, Warnings: {}",
          isFraudDetected ? "FRAUD_DETECTED" : "CLEAN",
          fraudScore,
          fraudAlerts.size(),
          fraudWarnings.size());

    } catch (Exception e) {
      log.error("Error during BankservAfrica fraud detection: {}", e.getMessage(), e);
      fraudAlerts.add("Fraud detection service error: " + e.getMessage());
      isFraudDetected = true;
      fraudScore = 1.0;
    }

    return BankservAfricaFraudDetectionResponse.builder()
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

  /** Execute velocity check for BankservAfrica transactions */
  private void executeBankservAfricaVelocityCheck(
      BankservAfricaAdapter adapter,
      BankservAfricaFraudDetectionRequest request,
      List<String> appliedRules,
      List<String> fraudAlerts,
      List<String> fraudWarnings) {
    appliedRules.add("BANKSERVAFRICA_VELOCITY_CHECK");

    // BankservAfrica handles EFT batch processing - check for velocity anomalies
    if (request.getTransactionCount() != null && request.getTransactionCount() > 50) {
      fraudWarnings.add(
          "BankservAfrica velocity check: High transaction count for batch processing");
    }

    if (request.getTransactionAmount() != null
        && request.getTransactionAmount().doubleValue() > 100000) {
      fraudWarnings.add("BankservAfrica velocity check: High-value transaction in batch");
    }

    // Check for rapid successive transactions
    if (request.getTimeSinceLastTransaction() != null
        && request.getTimeSinceLastTransaction() < 30) { // Less than 30 seconds
      fraudAlerts.add("BankservAfrica velocity check: Rapid successive transactions detected");
    }
  }

  /** Execute geographic analysis for BankservAfrica transactions */
  private void executeBankservAfricaGeographicAnalysis(
      BankservAfricaAdapter adapter,
      BankservAfricaFraudDetectionRequest request,
      List<String> appliedRules,
      List<String> fraudAlerts,
      List<String> fraudWarnings) {
    appliedRules.add("BANKSERVAFRICA_GEOGRAPHIC_ANALYSIS");

    // BankservAfrica is primarily for domestic transactions
    if (request.getBeneficiaryCountry() != null && !"ZA".equals(request.getBeneficiaryCountry())) {
      fraudWarnings.add(
          "BankservAfrica geographic analysis: Cross-border transaction through BankservAfrica");
    }

    // Check for unusual geographic patterns
    if (request.getOriginCountry() != null && !"ZA".equals(request.getOriginCountry())) {
      fraudAlerts.add(
          "BankservAfrica geographic analysis: Unusual origin country for BankservAfrica transaction");
    }

    // Check for high-risk countries
    if (request.getBeneficiaryCountry() != null
        && isHighRiskCountry(request.getBeneficiaryCountry())) {
      fraudAlerts.add("BankservAfrica geographic analysis: High-risk beneficiary country");
    }
  }

  /** Execute behavioral analysis for BankservAfrica transactions */
  private void executeBankservAfricaBehavioralAnalysis(
      BankservAfricaAdapter adapter,
      BankservAfricaFraudDetectionRequest request,
      List<String> appliedRules,
      List<String> fraudAlerts,
      List<String> fraudWarnings) {
    appliedRules.add("BANKSERVAFRICA_BEHAVIORAL_ANALYSIS");

    // Check for unusual transaction times (BankservAfrica operates during business hours)
    if (request.getTransactionTime() != null
        && !isWithinBankservAfricaBusinessHours(request.getTransactionTime())) {
      fraudWarnings.add("BankservAfrica behavioral analysis: Transaction outside business hours");
    }

    // Check for unusual transaction patterns
    if (request.getTransactionType() != null
        && !isNormalBankservAfricaTransactionType(request.getTransactionType())) {
      fraudWarnings.add(
          "BankservAfrica behavioral analysis: Unusual transaction type for BankservAfrica");
    }

    // Check for account behavior anomalies
    if (request.getAccountAge() != null && request.getAccountAge() < 7) {
      fraudWarnings.add(
          "BankservAfrica behavioral analysis: New account with high-value transaction");
    }
  }

  /** Execute pattern analysis for BankservAfrica transactions */
  private void executeBankservAfricaPatternAnalysis(
      BankservAfricaAdapter adapter,
      BankservAfricaFraudDetectionRequest request,
      List<String> appliedRules,
      List<String> fraudAlerts,
      List<String> fraudWarnings) {
    appliedRules.add("BANKSERVAFRICA_PATTERN_ANALYSIS");

    // Check for round number transactions (potential structuring)
    if (request.getTransactionAmount() != null
        && isRoundNumber(request.getTransactionAmount().doubleValue())) {
      fraudWarnings.add(
          "BankservAfrica pattern analysis: Round number transaction may indicate structuring");
    }

    // Check for unusual payment references
    if (request.getPaymentReference() != null && request.getPaymentReference().length() < 3) {
      fraudWarnings.add("BankservAfrica pattern analysis: Suspicious payment reference");
    }

    // Check for duplicate transactions
    if (request.getIsDuplicateTransaction() != null && request.getIsDuplicateTransaction()) {
      fraudAlerts.add("BankservAfrica pattern analysis: Duplicate transaction detected");
    }
  }

  /** Execute BankservAfrica-specific fraud detection rules */
  private void executeBankservAfricaSpecificFraudRules(
      BankservAfricaAdapter adapter,
      BankservAfricaFraudDetectionRequest request,
      List<String> appliedRules,
      List<String> fraudAlerts,
      List<String> fraudWarnings) {
    appliedRules.add("BANKSERVAFRICA_SPECIFIC_FRAUD_RULES");

    // Check BankservAfrica adapter status
    if (!adapter.getStatus().name().equals("ACTIVE")) {
      fraudAlerts.add("BankservAfrica specific rule: Adapter is not active");
    }

    // Check for encryption requirements
    if (!adapter.getEncryptionEnabled()) {
      fraudWarnings.add("BankservAfrica specific rule: Unencrypted transaction");
    }

    // Check for API version compatibility
    if (adapter.getApiVersion() == null || adapter.getApiVersion().trim().isEmpty()) {
      fraudWarnings.add("BankservAfrica specific rule: Unknown API version");
    }
  }

  /** Calculate fraud score based on alerts and warnings */
  private double calculateFraudScore(List<String> fraudAlerts, List<String> fraudWarnings) {
    double score = 0.0;

    // Each alert contributes 0.3 to the score
    score += fraudAlerts.size() * 0.3;

    // Each warning contributes 0.1 to the score
    score += fraudWarnings.size() * 0.1;

    // Cap the score at 1.0
    return Math.min(score, 1.0);
  }

  /** Check if country is high-risk */
  private boolean isHighRiskCountry(String countryCode) {
    // Simplified high-risk country check
    return "XX".equals(countryCode) || "HIGH_RISK".equals(countryCode);
  }

  /** Check if within BankservAfrica business hours */
  private boolean isWithinBankservAfricaBusinessHours(Instant transactionTime) {
    // BankservAfrica operates during business hours (8 AM - 4 PM SAST)
    // Simplified check - in real implementation, this would check actual business hours
    return true; // Simplified for now
  }

  /** Check if transaction type is normal for BankservAfrica */
  private boolean isNormalBankservAfricaTransactionType(String transactionType) {
    // BankservAfrica typically handles EFT, ISO 8583, and ACH transactions
    return "EFT".equals(transactionType)
        || "ISO8583".equals(transactionType)
        || "ACH".equals(transactionType)
        || "TRANSFER".equals(transactionType);
  }

  /** Check if amount is a round number */
  private boolean isRoundNumber(double amount) {
    return amount % 1000 == 0; // Round thousands
  }
}
