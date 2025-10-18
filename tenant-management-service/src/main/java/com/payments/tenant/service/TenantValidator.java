package com.payments.tenant.service;

import com.payments.tenant.entity.TenantEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Tenant Validator - Validates tenant operations at implementation time.
 *
 * <p>Purpose: Catch invalid operations, missing references, and constraint violations BEFORE
 * database operations occur.
 *
 * <p>Validation Rules:
 * 1. Required fields cannot be null
 * 2. Status transitions must be valid (state machine)
 * 3. Tenant must exist before updates/deletes
 * 4. Email must be in valid format
 * 5. Country code must be ISO 3166-1 alpha-3
 * 6. Enum values must be recognized
 *
 * <p>Throws: IllegalArgumentException with clear error messages
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantValidator {

  /**
   * Validate create tenant request.
   *
   * @param request Tenant to create
   * @throws IllegalArgumentException if validation fails
   */
  public void validateCreateRequest(TenantEntity request) {
    log.debug("Validating tenant creation request");

    // Check required fields
    if (request == null) {
      throw new IllegalArgumentException("Cannot create: tenant request is null");
    }

    if (request.getTenantName() == null || request.getTenantName().isBlank()) {
      throw new IllegalArgumentException(
          "Cannot create: tenantName is required and cannot be blank");
    }

    if (request.getTenantType() == null) {
      throw new IllegalArgumentException("Cannot create: tenantType is required");
    }

    if (request.getContactEmail() == null || request.getContactEmail().isBlank()) {
      throw new IllegalArgumentException(
          "Cannot create: contactEmail is required and cannot be blank");
    }

    // Validate email format
    if (!isValidEmail(request.getContactEmail())) {
      throw new IllegalArgumentException(
          "Cannot create: contactEmail '" + request.getContactEmail() + "' is not valid");
    }

    // Validate tenant type enum exists
    try {
      TenantEntity.TenantType.valueOf(request.getTenantType().toString());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Cannot create: tenantType '" + request.getTenantType() + "' is not recognized. "
              + "Valid types: BANK, FINANCIAL_INSTITUTION, FINTECH, CORPORATE");
    }

    // Validate country code if provided
    if (request.getCountry() != null && !request.getCountry().isBlank()) {
      if (!isValidCountryCode(request.getCountry())) {
        throw new IllegalArgumentException(
            "Cannot create: country code '" + request.getCountry() + "' must be ISO 3166-1 alpha-3");
      }
    }

    // Validate timezone if provided
    if (request.getTimezone() != null && !request.getTimezone().isBlank()) {
      if (!isValidTimezone(request.getTimezone())) {
        throw new IllegalArgumentException(
            "Cannot create: timezone '" + request.getTimezone() + "' is not recognized");
      }
    }

    log.debug("Tenant creation request validation passed");
  }

  /**
   * Validate tenant existence before update/delete.
   *
   * @param tenant Tenant to validate
   * @throws IllegalArgumentException if tenant is null
   */
  public void validateTenantExists(TenantEntity tenant) {
    if (tenant == null) {
      throw new IllegalArgumentException("Cannot process: tenant does not exist");
    }
  }

  /**
   * Validate status transition.
   *
   * <p>Valid transitions:
   * - PENDING_APPROVAL → ACTIVE
   * - PENDING_APPROVAL → INACTIVE
   * - ACTIVE → SUSPENDED
   * - ACTIVE → INACTIVE
   * - SUSPENDED → ACTIVE
   * - SUSPENDED → INACTIVE
   *
   * @param currentStatus Current status
   * @param targetStatus Target status
   * @throws IllegalArgumentException if transition is invalid
   */
  public void validateStatusTransition(
      TenantEntity.TenantStatus currentStatus, TenantEntity.TenantStatus targetStatus) {
    if (currentStatus == null || targetStatus == null) {
      throw new IllegalArgumentException(
          "Cannot validate: status cannot be null (current: "
              + currentStatus
              + ", target: "
              + targetStatus
              + ")");
    }

    boolean isValidTransition = false;

    // Define valid transitions
    switch (currentStatus) {
      case PENDING_APPROVAL:
        isValidTransition = (targetStatus == TenantEntity.TenantStatus.ACTIVE
            || targetStatus == TenantEntity.TenantStatus.INACTIVE);
        break;
      case ACTIVE:
        isValidTransition = (targetStatus == TenantEntity.TenantStatus.SUSPENDED
            || targetStatus == TenantEntity.TenantStatus.INACTIVE);
        break;
      case SUSPENDED:
        isValidTransition = (targetStatus == TenantEntity.TenantStatus.ACTIVE
            || targetStatus == TenantEntity.TenantStatus.INACTIVE);
        break;
      case INACTIVE:
        // Cannot transition from INACTIVE (terminal state)
        isValidTransition = false;
        break;
    }

    if (!isValidTransition) {
      throw new IllegalArgumentException(
          "Cannot transition: "
              + currentStatus
              + " → "
              + targetStatus
              + " is not allowed. "
              + "Valid transitions: PENDING_APPROVAL→ACTIVE/INACTIVE, ACTIVE→SUSPENDED/INACTIVE, "
              + "SUSPENDED→ACTIVE/INACTIVE");
    }
  }

  /**
   * Validate update request.
   *
   * @param tenant Current tenant
   * @param updates Update data
   * @throws IllegalArgumentException if update is invalid
   */
  public void validateUpdateRequest(TenantEntity tenant, TenantEntity updates) {
    if (tenant == null) {
      throw new IllegalArgumentException("Cannot update: tenant does not exist");
    }

    if (updates == null) {
      throw new IllegalArgumentException("Cannot update: update data is null");
    }

    // Validate email if being updated
    if (updates.getContactEmail() != null && !updates.getContactEmail().isBlank()) {
      if (!isValidEmail(updates.getContactEmail())) {
        throw new IllegalArgumentException(
            "Cannot update: contactEmail '" + updates.getContactEmail() + "' is not valid");
      }
    }

    // Validate timezone if being updated
    if (updates.getTimezone() != null && !updates.getTimezone().isBlank()) {
      if (!isValidTimezone(updates.getTimezone())) {
        throw new IllegalArgumentException(
            "Cannot update: timezone '" + updates.getTimezone() + "' is not recognized");
      }
    }

    // Ensure tenantId and status are NOT being changed
    if (updates.getTenantId() != null) {
      throw new IllegalArgumentException("Cannot update: tenantId cannot be changed");
    }

    if (updates.getStatus() != null) {
      throw new IllegalArgumentException(
          "Cannot update: status cannot be changed via update. Use activate/suspend/deactivate methods.");
    }
  }

  /**
   * Validate email format (basic validation).
   *
   * @param email Email to validate
   * @return true if valid
   */
  private boolean isValidEmail(String email) {
    if (email == null || email.isBlank()) {
      return false;
    }
    // Simple email validation: must contain @ and .
    return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
  }

  /**
   * Validate ISO 3166-1 alpha-3 country code.
   *
   * @param countryCode Country code to validate
   * @return true if valid
   */
  private boolean isValidCountryCode(String countryCode) {
    if (countryCode == null || countryCode.isBlank()) {
      return false;
    }
    // ISO 3166-1 alpha-3 codes are exactly 3 uppercase letters
    return countryCode.matches("^[A-Z]{3}$");
  }

  /**
   * Validate timezone (basic validation).
   *
   * @param timezone Timezone to validate
   * @return true if valid
   */
  private boolean isValidTimezone(String timezone) {
    if (timezone == null || timezone.isBlank()) {
      return false;
    }
    try {
      java.time.ZoneId.of(timezone);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Exception for validation failures.
   */
  public static class TenantValidationException extends IllegalArgumentException {
    public TenantValidationException(String message) {
      super(message);
    }

    public TenantValidationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
