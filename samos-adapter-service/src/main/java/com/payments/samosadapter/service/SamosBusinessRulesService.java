package com.payments.samosadapter.service;

import com.payments.domain.shared.TenantContext;
import com.payments.samosadapter.domain.SamosAdapter;
import com.payments.samosadapter.dto.SamosAdapterValidationResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SAMOS Business Rules Service
 *
 * <p>Service for executing business rules validation for SAMOS adapters: - Adapter configuration
 * validation - Operational rules - Business logic constraints - Compliance checks
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SamosBusinessRulesService {

  /**
   * Validate SAMOS adapter configuration
   *
   * @param adapter SAMOS adapter to validate
   * @param tenantContext Tenant context
   * @return Validation response
   */
  public SamosAdapterValidationResponse validateAdapterConfiguration(
      SamosAdapter adapter, TenantContext tenantContext) {

    log.debug(
        "Validating SAMOS adapter configuration: {} for tenant: {}",
        adapter.getId(),
        tenantContext.getTenantId());

    List<String> appliedRules = new ArrayList<>();
    List<String> validationErrors = new ArrayList<>();
    List<String> validationWarnings = new ArrayList<>();
    boolean isValid = true;

    try {
      // Rule 1: Adapter name validation
      validateAdapterName(adapter, appliedRules, validationErrors, validationWarnings);

      // Rule 2: Endpoint validation
      validateEndpoint(adapter, appliedRules, validationErrors, validationWarnings);

      // Rule 3: Configuration validation
      validateConfiguration(adapter, appliedRules, validationErrors, validationWarnings);

      // Rule 4: Operational status validation
      validateOperationalStatus(adapter, appliedRules, validationErrors, validationWarnings);

      // Rule 5: Tenant context validation
      validateTenantContext(
          adapter, tenantContext, appliedRules, validationErrors, validationWarnings);

      // Determine overall validation result
      isValid = validationErrors.isEmpty();

      log.info(
          "SAMOS adapter validation completed: {} - Applied rules: {}, Errors: {}, Warnings: {}",
          isValid ? "PASSED" : "FAILED",
          appliedRules.size(),
          validationErrors.size(),
          validationWarnings.size());

    } catch (Exception e) {
      log.error("Error during SAMOS adapter validation: {}", e.getMessage(), e);
      validationErrors.add("Validation service error: " + e.getMessage());
      isValid = false;
    }

    return SamosAdapterValidationResponse.builder()
        .adapterId(adapter.getId().toString())
        .isValid(isValid)
        .validationStatus(isValid ? "PASSED" : "FAILED")
        .appliedRules(appliedRules)
        .validationErrors(validationErrors)
        .validationWarnings(validationWarnings)
        .validatedAt(Instant.now())
        .tenantId(tenantContext.getTenantId())
        .businessUnitId(tenantContext.getBusinessUnitId())
        .build();
  }

  /** Validate adapter name */
  private void validateAdapterName(
      SamosAdapter adapter,
      List<String> appliedRules,
      List<String> validationErrors,
      List<String> validationWarnings) {
    appliedRules.add("ADAPTER_NAME_VALIDATION");

    if (adapter.getAdapterName() == null || adapter.getAdapterName().trim().isEmpty()) {
      validationErrors.add("Adapter name is required");
    } else if (adapter.getAdapterName().length() < 3) {
      validationErrors.add("Adapter name must be at least 3 characters long");
    } else if (adapter.getAdapterName().length() > 50) {
      validationErrors.add("Adapter name must not exceed 50 characters");
    } else if (!adapter.getAdapterName().matches("^[a-zA-Z0-9_-]+$")) {
      validationErrors.add(
          "Adapter name must contain only alphanumeric characters, underscores, and hyphens");
    }
  }

  /** Validate endpoint configuration */
  private void validateEndpoint(
      SamosAdapter adapter,
      List<String> appliedRules,
      List<String> validationErrors,
      List<String> validationWarnings) {
    appliedRules.add("ENDPOINT_VALIDATION");

    if (adapter.getEndpoint() == null || adapter.getEndpoint().trim().isEmpty()) {
      validationErrors.add("Endpoint is required");
    } else if (!adapter.getEndpoint().startsWith("https://")) {
      validationWarnings.add("Endpoint should use HTTPS for security");
    } else if (!adapter.getEndpoint().matches("^https://[a-zA-Z0-9.-]+(:[0-9]+)?(/.*)?$")) {
      validationErrors.add("Endpoint must be a valid HTTPS URL");
    }
  }

  /** Validate adapter configuration */
  private void validateConfiguration(
      SamosAdapter adapter,
      List<String> appliedRules,
      List<String> validationErrors,
      List<String> validationWarnings) {
    appliedRules.add("CONFIGURATION_VALIDATION");

    if (adapter.getTimeoutSeconds() <= 0) {
      validationErrors.add("Timeout must be greater than 0");
    } else if (adapter.getTimeoutSeconds() > 300) {
      validationWarnings.add("Timeout exceeds 5 minutes - consider optimization");
    }

    if (adapter.getRetryAttempts() < 0) {
      validationErrors.add("Retry attempts cannot be negative");
    } else if (adapter.getRetryAttempts() > 10) {
      validationWarnings.add("Retry attempts exceed 10 - consider reducing for performance");
    }

    if (adapter.getApiVersion() == null || adapter.getApiVersion().trim().isEmpty()) {
      validationErrors.add("API version is required");
    }
  }

  /** Validate operational status */
  private void validateOperationalStatus(
      SamosAdapter adapter,
      List<String> appliedRules,
      List<String> validationErrors,
      List<String> validationWarnings) {
    appliedRules.add("OPERATIONAL_STATUS_VALIDATION");

    if (adapter.getStatus() == null) {
      validationErrors.add("Operational status is required");
    }
  }

  /** Validate tenant context */
  private void validateTenantContext(
      SamosAdapter adapter,
      TenantContext tenantContext,
      List<String> appliedRules,
      List<String> validationErrors,
      List<String> validationWarnings) {
    appliedRules.add("TENANT_CONTEXT_VALIDATION");

    if (tenantContext == null) {
      validationErrors.add("Tenant context is required");
    } else {
      if (tenantContext.getTenantId() == null || tenantContext.getTenantId().trim().isEmpty()) {
        validationErrors.add("Tenant ID is required");
      }
      if (tenantContext.getBusinessUnitId() == null
          || tenantContext.getBusinessUnitId().trim().isEmpty()) {
        validationErrors.add("Business unit ID is required");
      }
    }
  }
}
