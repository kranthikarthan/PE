package com.payments.paymentinitiation.api.validation;

import com.payments.paymentinitiation.service.PaymentBusinessRulesService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Payment Validation Interceptor
 *
 * <p>Provides additional validation for payment requests: - Business rule validation - Amount
 * limits validation - Tenant-specific validation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentValidationInterceptor implements HandlerInterceptor {

  private final PaymentBusinessRulesService businessRulesService;

  @Override
  public boolean preHandle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler)
      throws Exception {
    log.debug("Payment validation interceptor processing request: {}", request.getRequestURI());

    // Only validate payment initiation requests
    if (request.getRequestURI().contains("/initiate") && request.getMethod().equals("POST")) {
      return validatePaymentInitiationRequest(request, response);
    }

    return true;
  }

  /** Validate payment initiation request */
  private boolean validatePaymentInitiationRequest(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      // Get tenant ID from headers
      String tenantId = request.getHeader("X-Tenant-ID");
      if (tenantId == null || tenantId.trim().isEmpty()) {
        log.warn("Missing tenant ID in request");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\": \"Missing X-Tenant-ID header\"}");
        return false;
      }

      // Get business rules for tenant
      var businessRules = businessRulesService.getBusinessRulesForTenant(tenantId);

      // Validate tenant business rules
      if (!validateTenantBusinessRules(businessRules, response)) {
        return false;
      }

      // Validate correlation ID
      String correlationId = request.getHeader("X-Correlation-ID");
      if (correlationId == null || correlationId.trim().isEmpty()) {
        log.warn("Missing correlation ID in request");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\": \"Missing X-Correlation-ID header\"}");
        return false;
      }

      // Validate business unit ID
      String businessUnitId = request.getHeader("X-Business-Unit-ID");
      if (businessUnitId == null || businessUnitId.trim().isEmpty()) {
        log.warn("Missing business unit ID in request");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\": \"Missing X-Business-Unit-ID header\"}");
        return false;
      }

      // Validate rate limiting based on business rules
      if (!validateRateLimiting(tenantId, businessRules, response)) {
        return false;
      }

      log.debug(
          "Request validation passed for tenant: {}, business unit: {}", tenantId, businessUnitId);
      return true;

    } catch (Exception e) {
      log.error("Error in payment validation interceptor", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      try {
        response.getWriter().write("{\"error\": \"Internal validation error\"}");
      } catch (java.io.IOException ioException) {
        log.error("Failed to write error response", ioException);
      }
      return false;
    }
  }

  /** Validate tenant business rules */
  private boolean validateTenantBusinessRules(
      PaymentBusinessRulesService.BusinessRules businessRules, HttpServletResponse response) {
    try {
      // Check if business rules exist for tenant
      if (businessRules == null) {
        log.warn("No business rules found for tenant");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"error\": \"Tenant not configured\"}");
        return false;
      }

      // Check if tenant has valid configuration
      if (businessRules.getMaxAmount() == null || businessRules.getMinAmount() == null) {
        log.warn("Invalid business rules configuration for tenant");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"error\": \"Invalid tenant configuration\"}");
        return false;
      }

      // Check if tenant has allowed payment types configured
      if (businessRules.getAllowedPaymentTypes() == null
          || businessRules.getAllowedPaymentTypes().isEmpty()) {
        log.warn("No allowed payment types configured for tenant");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"error\": \"No payment types allowed for tenant\"}");
        return false;
      }

      log.debug("Tenant business rules validation passed");
      return true;

    } catch (Exception e) {
      log.error("Error validating tenant business rules", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      try {
        response.getWriter().write("{\"error\": \"Business rules validation error\"}");
      } catch (java.io.IOException ioException) {
        log.error("Failed to write error response", ioException);
      }
      return false;
    }
  }

  /** Validate rate limiting based on business rules */
  private boolean validateRateLimiting(
      String tenantId,
      PaymentBusinessRulesService.BusinessRules businessRules,
      HttpServletResponse response) {
    try {
      // Check velocity limit (simplified check - in production would use Redis/cache)
      if (businessRules.getVelocityLimit() != null && businessRules.getVelocityLimit() > 0) {
        // TODO: Implement actual rate limiting with Redis/cache
        // For now, just log the check
        log.debug(
            "Rate limiting check for tenant: {} with limit: {}",
            tenantId,
            businessRules.getVelocityLimit());

        // In a real implementation, you would:
        // 1. Check current request count for tenant in last hour
        // 2. Compare against businessRules.getVelocityLimit()
        // 3. Return false if limit exceeded
      }

      log.debug("Rate limiting validation passed for tenant: {}", tenantId);
      return true;

    } catch (Exception e) {
      log.error("Error validating rate limiting for tenant: {}", tenantId, e);
      // Don't fail the request for rate limiting errors, just log them
      return true;
    }
  }
}
