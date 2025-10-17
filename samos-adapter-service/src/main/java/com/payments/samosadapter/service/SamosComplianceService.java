package com.payments.samosadapter.service;

import com.payments.samosadapter.domain.SamosAdapter;
import com.payments.samosadapter.dto.SamosComplianceRequest;
import com.payments.samosadapter.dto.SamosComplianceResponse;
import com.payments.domain.shared.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * SAMOS Compliance Service
 *
 * <p>Service for executing compliance rules for SAMOS clearing network: - AML (Anti-Money Laundering) checks - KYC (Know Your Customer) validation - Regulatory reporting requirements - Sanctions screening
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SamosComplianceService {

  /**
   * Execute compliance rules for SAMOS clearing network
   *
   * @param adapter SAMOS adapter
   * @param request Compliance request
   * @param tenantContext Tenant context
   * @return Compliance response
   */
  public SamosComplianceResponse executeComplianceRules(
      SamosAdapter adapter, SamosComplianceRequest request, TenantContext tenantContext) {
    
    log.debug("Executing SAMOS compliance rules for adapter: {} and payment: {}", 
              adapter.getId(), request.getPaymentId());

    List<String> appliedRules = new ArrayList<>();
    List<String> complianceErrors = new ArrayList<>();
    List<String> complianceWarnings = new ArrayList<>();
    boolean isCompliant = true;

    try {
      // Rule 1: AML check for SAMOS high-value payments
      executeSAMOSAMLCheck(adapter, request, appliedRules, complianceErrors, complianceWarnings);

      // Rule 2: KYC validation for SAMOS participants
      executeSAMOSKYCValidation(adapter, request, appliedRules, complianceErrors, complianceWarnings);

      // Rule 3: Sanctions screening for SAMOS transactions
      executeSAMOSSanctionsScreening(adapter, request, appliedRules, complianceErrors, complianceWarnings);

      // Rule 4: Regulatory reporting for SAMOS
      executeSAMOSRegulatoryReporting(adapter, request, appliedRules, complianceErrors, complianceWarnings);

      // Rule 5: SAMOS-specific compliance rules
      executeSAMOSSpecificRules(adapter, request, appliedRules, complianceErrors, complianceWarnings);

      // Determine overall compliance result
      isCompliant = complianceErrors.isEmpty();

      log.info("SAMOS compliance check completed: {} - Applied rules: {}, Errors: {}, Warnings: {}", 
               isCompliant ? "COMPLIANT" : "NON-COMPLIANT", appliedRules.size(), complianceErrors.size(), complianceWarnings.size());

    } catch (Exception e) {
      log.error("Error during SAMOS compliance check: {}", e.getMessage(), e);
      complianceErrors.add("Compliance service error: " + e.getMessage());
      isCompliant = false;
    }

    return SamosComplianceResponse.builder()
        .adapterId(adapter.getId().toString())
        .paymentId(request.getPaymentId())
        .isCompliant(isCompliant)
        .complianceStatus(isCompliant ? "COMPLIANT" : "NON-COMPLIANT")
        .appliedRules(appliedRules)
        .complianceErrors(complianceErrors)
        .complianceWarnings(complianceWarnings)
        .checkedAt(Instant.now())
        .tenantId(tenantContext.getTenantId())
        .businessUnitId(tenantContext.getBusinessUnitId())
        .build();
  }

  /**
   * Execute AML check for SAMOS high-value payments
   */
  private void executeSAMOSAMLCheck(SamosAdapter adapter, SamosComplianceRequest request, 
                                   List<String> appliedRules, List<String> complianceErrors, 
                                   List<String> complianceWarnings) {
    appliedRules.add("SAMOS_AML_CHECK");
    
    // SAMOS handles high-value RTGS payments - enhanced AML checks required
    if (request.getAmount() != null && request.getAmount().doubleValue() > 1000000) { // R1M threshold
      complianceWarnings.add("High-value SAMOS transaction requires enhanced AML monitoring");
    }
    
    // Check for suspicious patterns
    if (request.getBeneficiaryName() != null && 
        request.getBeneficiaryName().toLowerCase().contains("suspicious")) {
      complianceErrors.add("SAMOS AML check failed: Suspicious beneficiary pattern detected");
    }
  }

  /**
   * Execute KYC validation for SAMOS participants
   */
  private void executeSAMOSKYCValidation(SamosAdapter adapter, SamosComplianceRequest request, 
                                       List<String> appliedRules, List<String> complianceErrors, 
                                       List<String> complianceWarnings) {
    appliedRules.add("SAMOS_KYC_VALIDATION");
    
    // SAMOS requires full KYC for all participants
    if (request.getBeneficiaryAccount() == null || request.getBeneficiaryAccount().trim().isEmpty()) {
      complianceErrors.add("SAMOS KYC validation failed: Beneficiary account required");
    }
    
    if (request.getBeneficiaryName() == null || request.getBeneficiaryName().trim().isEmpty()) {
      complianceErrors.add("SAMOS KYC validation failed: Beneficiary name required");
    }
    
    // Check account format for SAMOS
    if (request.getBeneficiaryAccount() != null && 
        !request.getBeneficiaryAccount().matches("^[0-9]{10,12}$")) {
      complianceWarnings.add("SAMOS KYC warning: Beneficiary account format may be invalid");
    }
  }

  /**
   * Execute sanctions screening for SAMOS transactions
   */
  private void executeSAMOSSanctionsScreening(SamosAdapter adapter, SamosComplianceRequest request, 
                                             List<String> appliedRules, List<String> complianceErrors, 
                                             List<String> complianceWarnings) {
    appliedRules.add("SAMOS_SANCTIONS_SCREENING");
    
    // Check against sanctions lists
    if (request.getBeneficiaryName() != null && 
        isSanctionedEntity(request.getBeneficiaryName())) {
      complianceErrors.add("SAMOS sanctions screening failed: Beneficiary appears on sanctions list");
    }
    
    if (request.getBeneficiaryCountry() != null && 
        isSanctionedCountry(request.getBeneficiaryCountry())) {
      complianceErrors.add("SAMOS sanctions screening failed: Beneficiary country is sanctioned");
    }
  }

  /**
   * Execute regulatory reporting for SAMOS
   */
  private void executeSAMOSRegulatoryReporting(SamosAdapter adapter, SamosComplianceRequest request, 
                                             List<String> appliedRules, List<String> complianceErrors, 
                                             List<String> complianceWarnings) {
    appliedRules.add("SAMOS_REGULATORY_REPORTING");
    
    // SAMOS requires regulatory reporting for high-value transactions
    if (request.getAmount() != null && request.getAmount().doubleValue() > 25000) { // R25K threshold
      complianceWarnings.add("SAMOS regulatory reporting: Transaction exceeds reporting threshold");
    }
    
    // Check for cross-border transactions
    if (request.getBeneficiaryCountry() != null && 
        !"ZA".equals(request.getBeneficiaryCountry())) {
      complianceWarnings.add("SAMOS regulatory reporting: Cross-border transaction requires additional reporting");
    }
  }

  /**
   * Execute SAMOS-specific compliance rules
   */
  private void executeSAMOSSpecificRules(SamosAdapter adapter, SamosComplianceRequest request, 
                                        List<String> appliedRules, List<String> complianceErrors, 
                                        List<String> complianceWarnings) {
    appliedRules.add("SAMOS_SPECIFIC_RULES");
    
    // SAMOS is for RTGS - check business hours
    if (!isWithinSAMOSBusinessHours()) {
      complianceWarnings.add("SAMOS specific rule: Transaction outside SAMOS business hours");
    }
    
    // Check SAMOS participant status
    if (!adapter.getStatus().name().equals("ACTIVE")) {
      complianceErrors.add("SAMOS specific rule: Adapter is not active");
    }
    
    // Check encryption requirements
    if (!adapter.getEncryptionEnabled()) {
      complianceWarnings.add("SAMOS specific rule: Encryption not enabled for high-value transactions");
    }
  }

  /**
   * Check if entity is sanctioned
   */
  private boolean isSanctionedEntity(String entityName) {
    // Simplified sanctions check - in real implementation, this would query a sanctions database
    return entityName.toLowerCase().contains("sanctioned") || 
           entityName.toLowerCase().contains("blocked");
  }

  /**
   * Check if country is sanctioned
   */
  private boolean isSanctionedCountry(String countryCode) {
    // Simplified sanctions check - in real implementation, this would query a sanctions database
    return "XX".equals(countryCode) || "BLOCKED".equals(countryCode);
  }

  /**
   * Check if within SAMOS business hours
   */
  private boolean isWithinSAMOSBusinessHours() {
    // SAMOS operates during business hours (8 AM - 4 PM SAST)
    // Simplified check - in real implementation, this would check actual business hours
    return true; // Simplified for now
  }
}
